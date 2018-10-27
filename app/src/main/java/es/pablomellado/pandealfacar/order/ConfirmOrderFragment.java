package es.pablomellado.pandealfacar.order;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import es.pablomellado.pandealfacar.ConnectedFragment;
import es.pablomellado.pandealfacar.FirebaseInterface;
import es.pablomellado.pandealfacar.R;
import es.pablomellado.pandealfacar.Utils;
import es.pablomellado.pandealfacar.model.Address;
import es.pablomellado.pandealfacar.model.Order;

public class ConfirmOrderFragment extends ConnectedFragment
        implements FirebaseInterface.ServerTimeCallBackListener,
            FirebaseInterface.AddOrderCallBackListener,
            FirebaseInterface.ClientAddressesCallBackListener,
            DatePickerDialog.OnDateSetListener{


    public enum AddressState {
        INIT, GETTING_NEEDED_ADDRESS
    }


    private Cart currentCart;
    private Address currentAddress;

    private TextView deliveryDateTextView;
    private EditText noteEditText;

    private ConfirmOrderFragmentListener mListener;

    private DatePickerDialog datePickerDialog;
    private boolean changingDate = false;
    private Date selectedDate;
    private MaterialDialog mProgressDialog = null;

    private AddressState mStatus = AddressState.INIT;

    public ConfirmOrderFragment() {
        // Required empty public constructor
    }

    public static ConfirmOrderFragment newInstance(Cart theCart) {
        ConfirmOrderFragment fragment = new ConfirmOrderFragment();
        fragment.currentCart = theCart;
        fragment.currentAddress = SessionHelper.getCurrentAddress();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_confirm_order, container, false);

        currentAddress = SessionHelper.getCurrentAddress();


        if (currentAddress == null){
            switch (mStatus){
                case INIT:
                    mStatus = AddressState.GETTING_NEEDED_ADDRESS;
                    mListener.openNewAddressNeededFragment();
                    break;
                case GETTING_NEEDED_ADDRESS:
                    mStatus = AddressState.INIT;
                    getFragmentManager().popBackStack();
                    break;
            }
        }
        else {
            deliveryDateTextView = (TextView) root.findViewById(R.id.deliveryDateTextView);
            noteEditText = (EditText) root.findViewById(R.id.noteEditText);

            updateAddressTextView(root);


            TableLayout table = (TableLayout) root.findViewById(R.id.productsTableLayout);
            List<Cart.CartItem> cartItems = currentCart.getCartItemsList();
            for (int i = 0; i < cartItems.size(); i++) {

                TableRow row = new TableRow(this.getContext());
                TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
                row.setLayoutParams(lp);
                // create a new TextView for showing xml data
                Cart.CartItem item = cartItems.get(i);
                String cell1 = Integer.toString(item.quantity) + " x " + item.product.getName();
                TextView t = new TextView(this.getContext());
                // set the text to "text xx"
                t.setText(cell1);
                // add the TextView  to the new TableRow
                row.addView(t);

                TextView u = new TextView(this.getContext());
                u.setText(NumberFormat.getCurrencyInstance().format(item.product.getPrice().multiply(new BigDecimal(item.quantity))));
                u.setGravity(Gravity.RIGHT);
                row.addView(u);

                // add the TableRow to the TableLayout
                table.addView(row, i);
            }
            String cell2 = getString(R.string.total_cost);
            cell2 += " " + NumberFormat.getCurrencyInstance().
                    format(currentCart.getTotalPrice());
            TextView totalOrderTextView = (TextView) root.findViewById(R.id.totalOrderTextView);
            totalOrderTextView.setText(cell2);

            Button btnChangeDate = (Button) root.findViewById(R.id.changeDayButton);

            datePickerDialog = new DatePickerDialog(
                    this.getContext(), this, 2017, 01, 01);

            btnChangeDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isConnected()) {
                        datePickerDialog.show();
                    } else {
                        mNoConnectionDialogBuilder.show();
                    }
                }
            });

            Button btnChangeAddress = (Button) root.findViewById(R.id.changeAddressButton);
            btnChangeAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isConnected()) {
                        showChangeAddressDialog();
                    } else {
                        mNoConnectionDialogBuilder.show();
                    }
                }
            });

            getServerTime();

            setHasOptionsMenu(true);
        }
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.confirm_order_title);

        inflater.inflate(R.menu.menu_confirm_order, menu);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        getActivity().onBackPressed();

                    }
                }
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_confirm:
                if (isConnected()) {
                    SharedPreferences sharedPref = getActivity().getSharedPreferences(
                            this.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    FirebaseInterface firebaseInterface = new FirebaseInterface(getActivity());
                    firebaseInterface.addOrder(new Order(sharedPref.getString(this.getString(
                            R.string.my_phone_number), ""),
                            currentAddress,
                            currentCart.getCartItemsList(),
                            Utils.getISOStringForDate(selectedDate), "", noteEditText.getText().toString()), this);

                    mProgressDialog = new MaterialDialog.Builder(getContext())
                            .title(R.string.submitting_order)
                            .content(R.string.please_wait)
                            .progress(true, 0)
                            .show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                                Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), R.string.couldnt_connect,
                                        Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }, 35000);
                } else {
                    mNoConnectionDialogBuilder
                            .show();
                }
                return true;

            default:
                break;
        }

        return false;
    }


    void updateAddressTextView(View view) {
        TextView deliveryAddressTextView = (TextView) view.findViewById(R.id.deliveryAdressTextView);
        String text;
        if (currentAddress!=null) {
            text = currentAddress.getLine1();
            if (currentAddress.getLine2().length() > 0)
                text += "\n" + currentAddress.getLine2();
            text += " " + currentAddress.getPostalcode();
            text += " " + currentAddress.getCity();
            text += "\n" + getString(R.string.caption_contact_colon) + " " + currentAddress.getContact();
        }
        else{
            text = getString(R.string.no_address_added_yet);
        }
        deliveryAddressTextView.setText(text);
    }

    @Override
    public void orderAdded(boolean successfully) {
        if(mProgressDialog!=null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        if (successfully){
            currentCart.clear();
            //Show dialog saying the order is confirmed
            new MaterialDialog.Builder(this.getContext())
                    .content(R.string.order_submitted_successfully)
                    .positiveText(android.R.string.ok)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        //Go home
                                        getActivity().finish();
                                    }
                                }
                    )
                    .show();
        }
        else{
            //Show dialog saying there was a problem submitting the order
            new MaterialDialog.Builder(this.getContext())
                    .content(R.string.problem_submitting_order_try_again)
                    .positiveText(android.R.string.ok)
                    .show();
        }
    }

    private void getServerTime(){
        if (isConnected()) {
            FirebaseInterface fb = new FirebaseInterface(this.getActivity());
            fb.getServerEpoch(this);
        } else {
            mNoConnectionDialogBuilder
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            getServerTime();
                        }
                    })
                    .show();
        }
    }

    public void gotServerTime(long epoch){
        Date date = new Date(epoch);

        Calendar calendarCurrent = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendarCurrent.setTime(date);   // assigns calendar to given date
        calendarCurrent.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
        date = calendarCurrent.getTime();
        if (!changingDate) {
            updateDeliveryDateTextView(date);
        }
        else{
            updateDeliveryDateTextViewWithSelectedDate(date);
            changingDate = false;
        }
    }

    @Override
    public void errorGettingServerTime() {

    }

    private void updateDeliveryDateTextViewWithSelectedDate(Date currentTime){
        Calendar calendarCurrent = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendarCurrent.setTime(currentTime);   // assigns calendar to given date
        calendarCurrent.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));

        Calendar calendarSelected = GregorianCalendar.getInstance();
        calendarSelected.setTime(this.selectedDate);
        calendarSelected.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));

        if (calendarCurrent.get(Calendar.YEAR) == calendarSelected.get(Calendar.YEAR) &&
                calendarCurrent.get(Calendar.DAY_OF_YEAR) ==
                        calendarSelected.get(Calendar.DAY_OF_YEAR)){
           //Show dialog saying you cannot select current day
            new MaterialDialog.Builder(this.getContext())
                    .content(R.string.you_cannot_select_current_day_for_delivery)
                    .positiveText(android.R.string.ok)
                    .show();
            return;
        }
        if (calendarCurrent.get(Calendar.YEAR) > calendarSelected.get(Calendar.YEAR) ||
                (calendarCurrent.get(Calendar.YEAR) == calendarSelected.get(Calendar.YEAR) &&
                 calendarCurrent.get(Calendar.DAY_OF_YEAR) > calendarSelected.get(Calendar.DAY_OF_YEAR))){
            //Show dialog saying you cannot select a day in the past
            new MaterialDialog.Builder(this.getContext())
                    .content(R.string.you_cannot_select_a_day_in_the_past_for_delivery)
                    .positiveText(android.R.string.ok)
                    .show();
            return;
        }

        calendarCurrent.add(Calendar.DAY_OF_MONTH, 1);
        if(calendarCurrent.get(Calendar.YEAR) == calendarSelected.get(Calendar.YEAR) &&
                calendarCurrent.get(Calendar.DAY_OF_YEAR) ==
                        calendarSelected.get(Calendar.DAY_OF_YEAR)){
            // The day selected is tomorrow, then we have to check if there is still time
            Date possibleDeliveryDate = Order.getNextDayDeliveryDate(currentTime);
            Calendar calendarNewDelivery = GregorianCalendar.getInstance();
            calendarNewDelivery.setTime(possibleDeliveryDate);
            calendarNewDelivery.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));

            if (!(calendarNewDelivery.get(Calendar.YEAR) == calendarSelected.get(Calendar.YEAR) &&
                    calendarNewDelivery.get(Calendar.DAY_OF_YEAR) ==
                            calendarSelected.get(Calendar.DAY_OF_YEAR))){
                // Show dialog saying you cannot make the delivery tomorrow
                new MaterialDialog.Builder(this.getContext())
                        .content(R.string.tomorrow_there_is_no_delivery)
                        .positiveText(android.R.string.ok)
                        .show();

            }
            // Update the text fo the DeliveryDateTextView
            String formatted = formatDeliveryDateText(possibleDeliveryDate, currentTime);
            deliveryDateTextView.setText(formatted);

            datePickerDialog.updateDate(calendarNewDelivery.get(Calendar.YEAR),
                    calendarNewDelivery.get(Calendar.MONTH),
                    calendarNewDelivery.get(Calendar.DAY_OF_MONTH));
            return;
        }

        // Finally the day  is in the future and it is not tomorrow
        calendarSelected.add(Calendar.DAY_OF_MONTH, -1);
        calendarSelected.set(Calendar.HOUR_OF_DAY, 10);
        calendarSelected.set(Calendar.MINUTE, 0);
        Date deliveryDate = Order.getNextDayDeliveryDate(calendarSelected.getTime());
        calendarSelected.add(Calendar.DAY_OF_MONTH, 1);

        Calendar calendarDelivery = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendarDelivery.setTime(deliveryDate);   // assigns calendar to given date
        calendarDelivery.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
        if (!(calendarDelivery.get(Calendar.YEAR) == calendarSelected.get(Calendar.YEAR) &&
                calendarDelivery.get(Calendar.DAY_OF_YEAR) ==
                        calendarSelected.get(Calendar.DAY_OF_YEAR))){
            // Show a dialog with info about the change of the day
            new MaterialDialog.Builder(this.getContext())
                    .content(R.string.selected_day_is_a_bank_holiday)
                    .positiveText(android.R.string.ok)
                    .show();
        }
        // Update the text fo the DeliveryDateTextView
        String formatted = formatDeliveryDateText(deliveryDate, currentTime);
        deliveryDateTextView.setText(formatted);

        datePickerDialog.updateDate(calendarDelivery.get(Calendar.YEAR),
                calendarDelivery.get(Calendar.MONTH),
                calendarDelivery.get(Calendar.DAY_OF_MONTH));
    }

    public void updateDeliveryDateTextView(Date currentTime){
        Date deliveryDate = Order.getNextDayDeliveryDate(currentTime);

        String formatted = formatDeliveryDateText(deliveryDate, currentTime);
        deliveryDateTextView.setText(formatted);

        Calendar calendarDelivery = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendarDelivery.setTime(deliveryDate);   // assigns calendar to given date
        calendarDelivery.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));

        datePickerDialog.updateDate(calendarDelivery.get(Calendar.YEAR),
                calendarDelivery.get(Calendar.MONTH),
                calendarDelivery.get(Calendar.DAY_OF_MONTH));
    }

    private String formatDeliveryDateText(Date deliveryDate, Date currentTime){
        this.selectedDate = deliveryDate;
        DateFormat format = new SimpleDateFormat("EEEE, d MMMM yyyy");
        String formatted = format.format(deliveryDate);
        formatted = formatted.toUpperCase();
        formatted = formatted +" "+ getString(R.string.during_the_morning);

        Calendar calendarCurrent = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendarCurrent.setTime(currentTime);   // assigns calendar to given date
        calendarCurrent.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
        calendarCurrent.add(Calendar.DATE, 1);

        Calendar calendarDelivery = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendarDelivery.setTime(deliveryDate);   // assigns calendar to given date
        calendarDelivery.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));

        if (calendarCurrent.get(Calendar.DAY_OF_YEAR)== calendarDelivery.get(Calendar.DAY_OF_YEAR)){
            formatted = getString(R.string.tomorrow) +" "+ formatted;
        }
        calendarCurrent.add(Calendar.DATE, 1);
        if (calendarCurrent.get(Calendar.DAY_OF_YEAR)== calendarDelivery.get(Calendar.DAY_OF_YEAR)){
            formatted = getString(R.string.day_after_tomorrow).toUpperCase() +" "+ formatted;
        }
        return formatted;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        this.changingDate = true;
        Calendar calendarSelectedDay = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendarSelectedDay.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
        calendarSelectedDay.set(year, month, dayOfMonth);
        this.selectedDate = calendarSelectedDay.getTime();
        if (isConnected()) {
            FirebaseInterface fb = new FirebaseInterface(this.getActivity());
            fb.getServerEpoch(this);
        } else {
            mNoConnectionDialogBuilder.show();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof ConfirmOrderFragmentListener) {
            mListener = (ConfirmOrderFragmentListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement ConfirmOrderFragmentListener.");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void showChangeAddressDialog(){
        getClientAddresses();
    }

    private void getClientAddresses(){
        if (isConnected()) {
            FirebaseInterface firebaseInterface = new FirebaseInterface(this.getActivity());
            SharedPreferences sharedPref = getActivity().getSharedPreferences(
                    getActivity().getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String myPhoneNumber = sharedPref.getString(getString(R.string.my_phone_number), "");
            if (myPhoneNumber.length() > 0) {
                firebaseInterface.getClientAddresses(myPhoneNumber, this);
                mProgressDialog = new MaterialDialog.Builder(getContext())
                        .title(R.string.getting_addresses)
                        .content(R.string.please_wait)
                        .progress(true, 0)
                        .show();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                            Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), R.string.couldnt_connect,
                                    Snackbar.LENGTH_LONG).show();
                        }
                    }
                }, 35000);
            }
        } else {
            mNoConnectionDialogBuilder
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            getClientAddresses();
                        }
                    })
                    .show();
        }
    }


    @Override
    public void gotClientAddresses(Address[] addresses) {
        if(mProgressDialog!=null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        showChangeAddressDialogCallback(addresses);
    }

    @Override
    public void errorGettingAddresses() {

    }

    private void showChangeAddressDialogCallback(final Address[] addresses){
        int selected = -1;
        String[] strAddresses = new String[addresses.length];

        for (int i=0;i<addresses.length;i++){
            if (addresses[i].getKey().compareTo(currentAddress.getKey())==0){
                selected = i;
            }
            strAddresses[i]=addresses[i].toString();
        }
        new MaterialDialog.Builder(this.getContext())
                .title(R.string.select_address)
                .items(strAddresses)
                .itemsCallbackSingleChoice(selected, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        /**
                         * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                         * returning false here won't allow the newly selected radio button to actually be selected.
                         **/
                        currentAddress = addresses[which];
                        SessionHelper.setCurrentAddress(currentAddress);
                        updateAddressTextView(getView());
                        return true;
                    }
                })
                .positiveText(R.string.button_change_address)
                .neutralText(R.string.new_address_title)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Log.d("DEB","Neutral clicked");

                        mListener.loadNewAddressFragment();
                    }
                })
                .show();
    }


    public interface ConfirmOrderFragmentListener {
        void loadNewAddressFragment();
        void openNewAddressNeededFragment();
    }
}
