package es.pablomellado.pandealfacar.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.database.ServerValue;

import es.pablomellado.pandealfacar.FirebaseInterface;
import es.pablomellado.pandealfacar.order.Cart;

/**
 * Created by Pablo Mellado on 23/4/17.
 */

public class Order {


    public enum Status {
        ORDERED, CONFIRMED, DELIVERED
    }

    static final int [][] BANK_HOLIDAYS = new int[][]{
            {Calendar.JANUARY, 1},
            {Calendar.AUGUST, 15},
            {Calendar.OCTOBER, 12},
            {Calendar.JULY, 21},
            {Calendar.JULY, 22},
            {Calendar.JULY, 24},

    };

    private String clientId;
    private Address deliverAddress;
    //private List<String> orderedProducts;
    private List<Cart.CartItem> orderedProducts;
    private String deliverDate;
    private String deliverTimeRange;
    private Status status;
    //private Map<String,String> orderTime;
    private Long orderTime;



    private String note;

    private static int hourOrderLimit = 22;
    private static int minuteOrderLimit = 0;
    private static boolean deliveryOnSunday = false;
    private static boolean deliveryOnBankHoliday = false;


    public Order(){

    }

    public static Date getNextDayDeliveryDate(Date currentTime){
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(currentTime);   // assigns calendar to given date
        calendar.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);  // gets hour in 24h format
        int minute = calendar.get(Calendar.MINUTE);

        calendar.add(Calendar.DATE, 1);
        if (!deliveryOnSunday && calendar.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY){
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            Date recursiveDate = calendar.getTime();
            return getNextDayDeliveryDate(recursiveDate);
        }

        if (!deliveryOnBankHoliday && isBankHoliday(calendar.getTime())){
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            Date recursiveDate = calendar.getTime();
            return getNextDayDeliveryDate(recursiveDate);
        }

        //Check time
        if (hour>=hourOrderLimit && minute>minuteOrderLimit){
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            Date recursiveDate = calendar.getTime();
            return getNextDayDeliveryDate(recursiveDate);
        }
        else {
            return calendar.getTime();
        }
    }

    public static boolean isBankHoliday(Date date){

        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        calendar.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
        int dateMonth = calendar.get(Calendar.MONTH);
        int dateDay = calendar.get(Calendar.DAY_OF_MONTH);

        for (int[] bh:
             BANK_HOLIDAYS) {
            if (bh[0] == dateMonth && bh[1] == dateDay){
                return true;
            }
            
        }

        return false;
    }

    public Order(String cliendId, Address deliverAddress, List <Cart.CartItem> orderedProducts,
                 String deliverDate, String deliverTimeRange, String note) {
        this.clientId = cliendId;
        this.deliverAddress = deliverAddress;
        this.deliverDate = deliverDate;
        this.deliverTimeRange = deliverTimeRange;
        this.orderedProducts = new ArrayList<Cart.CartItem>();
        for (Cart.CartItem op:orderedProducts) {
            this.orderedProducts.add(op);
        }

        this.status = Status.ORDERED;

        this.note = note;
    }

    public String getClientId() {
        return clientId;
    }

    public Address getAddress() {
        return deliverAddress;
    }

    public List<Cart.CartItem> getOrderedProducts() {
        return orderedProducts;
    }

    public String getDeliverDate() {
        return deliverDate;
    }

    public String getDeliverTimeRange() {
        return deliverTimeRange;
    }

    public Status getStatus() {
        return status;
    }

    public Map<String, String> getOrderTime() {
        return FirebaseInterface.getServerTimestamp();
    }

    @JsonIgnore
    public Long getOrderTimeLong() {
        return orderTime;
    }

    public void setOrderTime(Long orderTime) {
        this.orderTime = orderTime;
    }

    public void setAddress(Address address) {
        this.deliverAddress = address;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }


}
