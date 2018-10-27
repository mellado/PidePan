package es.pablomellado.pandealfacar;

import es.pablomellado.pandealfacar.model.Product;

/**
 * Auxiliary class to represent a product row in the list view
 */
public class ProductInfoRow {

    public Product productInfo;

    public int quantity;

    public ProductInfoRow(Product product, int quantity) {
        this.productInfo = product;
        this.quantity = quantity;
    }
}
