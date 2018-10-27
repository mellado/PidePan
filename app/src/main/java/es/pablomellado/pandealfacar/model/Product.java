package es.pablomellado.pandealfacar.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.math.BigDecimal;

import es.pablomellado.pandealfacar.order.Saleable;

/**
 * Created by Pablo Mellado on 25/4/17.
 */

public class Product implements Saleable{

    private String id;
    private String name;
    private double price;
    private String description;
    private String imgUrl;

    private int imgRes;

    public Product(){

    }

    public Product(String id, String name, double price, String description, String imgUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imgUrl = imgUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Exclude
    public BigDecimal getPrice() {
        return new BigDecimal(price);
    }

    @PropertyName("price")
    public double getPriceDouble() {
        return price;
    }


    public String getDescription() {
        return description;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setId(String id) {
        this.id = id;
    }

}
