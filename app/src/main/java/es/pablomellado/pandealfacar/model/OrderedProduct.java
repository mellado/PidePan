package es.pablomellado.pandealfacar.model;

import java.math.BigDecimal;

/**
 * Created by Pablo Mellado on 20/4/17.
 */

public class OrderedProduct {
    private String id;
    private String name;
    private BigDecimal price;
    private Integer quantity;


    public OrderedProduct() {
    }

    public OrderedProduct(String id, String name, BigDecimal price, Integer quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
