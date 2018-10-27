package es.pablomellado.pandealfacar.order;

import es.pablomellado.pandealfacar.model.Address;

/**
 * A helper class to retrieve the static shopping cart. Call {@code getCart()} to retrieve the shopping cart before you perform any operation on the shopping cart.
 */
public class SessionHelper {
    private static Cart cart = new Cart();
    private static Address address = null;

    /**
     * Retrieve the shopping cart. Call this before perform any manipulation on the shopping cart.
     *
     * @return the shopping cart
     */
    public static Cart getCart() {
        if (cart == null) {
            cart = new Cart();
        }

        return cart;
    }

    public static void setCurrentAddress(Address address){
        SessionHelper.address = address;
    }

    public static Address getCurrentAddress(){
        return SessionHelper.address;
    }
}
