package es.pablomellado.pandealfacar.order;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import es.pablomellado.pandealfacar.model.Product;


/**
 * A representation of shopping cart.
 * <p/>
 * A shopping cart has a map of {@link Saleable} products to their corresponding quantities.
 */
public class Cart implements Serializable {
    private static final long serialVersionUID = 42L;

    private Map<String, Integer> cartItemMap = new HashMap<String, Integer>();
    private List<Saleable> cartSaleables = new ArrayList<>();
    private BigDecimal totalPrice = BigDecimal.ZERO;
    private int totalQuantity = 0;

    /**
     * Add a quantity of a certain {@link Saleable} product to this shopping cart
     *
     * @param sellable the product will be added to this shopping cart
     * @param quantity the amount to be added
     */
    public void add(final Saleable sellable, int quantity) {
        if (cartItemMap.containsKey(sellable.getId())) {
            cartItemMap.put(sellable.getId(), cartItemMap.get(sellable.getId()) + quantity);
        } else {
            cartItemMap.put(sellable.getId(), quantity);
            cartSaleables.add(sellable);
        }

        totalPrice = totalPrice.add(sellable.getPrice().multiply(BigDecimal.valueOf(quantity)));
        totalQuantity += quantity;
    }

    /**
     * Set new quantity for a {@link Saleable} product in this shopping cart
     *
     * @param sellable the product which quantity will be updated
     * @param quantity the new quantity will be assigned for the product
     * @throws ProductNotFoundException    if the product is not found in this shopping cart.
     * @throws QuantityOutOfRangeException if the quantity is negative
     */
    public void update(final Saleable sellable, int quantity) throws ProductNotFoundException, QuantityOutOfRangeException {
        if (!cartItemMap.containsKey(sellable.getId())) throw new ProductNotFoundException();
        if (quantity < 0)
            throw new QuantityOutOfRangeException(quantity + " is not a valid quantity. It must be non-negative.");

        int productQuantity = cartItemMap.get(sellable.getId());
        BigDecimal productPrice = sellable.getPrice().multiply(BigDecimal.valueOf(productQuantity));

        cartItemMap.put(sellable.getId(), quantity);

        totalQuantity = totalQuantity - productQuantity + quantity;
        totalPrice = totalPrice.subtract(productPrice).add(sellable.getPrice().multiply(BigDecimal.valueOf(quantity)));
    }

    /**
     * Remove a certain quantity of a {@link Saleable} product from this shopping cart
     *
     * @param sellable the product which will be removed
     * @param quantity the quantity of product which will be removed
     * @throws ProductNotFoundException    if the product is not found in this shopping cart
     * @throws QuantityOutOfRangeException if the quantity is negative or more than the existing quantity of the product in this shopping cart
     */
    public void remove(final Saleable sellable, int quantity) throws ProductNotFoundException, QuantityOutOfRangeException {
        if (!cartItemMap.containsKey(sellable.getId())) throw new ProductNotFoundException();

        int productQuantity = cartItemMap.get(sellable.getId());

        if (quantity < 0 || quantity > productQuantity)
            throw new QuantityOutOfRangeException(quantity + " is not a valid quantity. It must be non-negative and less than the current quantity of the product in the shopping cart.");

        if (productQuantity == quantity) {
            cartItemMap.remove(sellable.getId());
            removeFromSellablesList(sellable.getId());
        } else {
            cartItemMap.put(sellable.getId(), productQuantity - quantity);
        }

        totalPrice = totalPrice.subtract(sellable.getPrice().multiply(BigDecimal.valueOf(quantity)));
        totalQuantity -= quantity;
    }

    private void removeFromSellablesList(String sellableId){
        Iterator<Saleable> it = cartSaleables.iterator();
        while (it.hasNext()) {
            if (it.next().getId() == sellableId) {
                it.remove();
                break;
            }
        }
    }

    /**
     * Remove a {@link Saleable} product from this shopping cart totally
     *
     * @param sellable the product to be removed
     * @throws ProductNotFoundException if the product is not found in this shopping cart
     */
    public void remove(final Saleable sellable) throws ProductNotFoundException {
        if (!cartItemMap.containsKey(sellable.getId())) throw new ProductNotFoundException();

        int quantity = cartItemMap.get(sellable.getId());
        cartItemMap.remove(sellable.getId());
        removeFromSellablesList(sellable.getId());
        totalPrice = totalPrice.subtract(sellable.getPrice().multiply(BigDecimal.valueOf(quantity)));
        totalQuantity -= quantity;
    }

    /**
     * Remove all products from this shopping cart
     */
    public void clear() {
        cartItemMap.clear();
        cartSaleables.clear();
        totalPrice = BigDecimal.ZERO;
        totalQuantity = 0;
    }

    /**
     * Get quantity of a {@link Saleable} product in this shopping cart
     *
     * @param sellable the product of interest which this method will return the quantity
     * @return The product quantity in this shopping cart
     * @throws ProductNotFoundException if the product is not found in this shopping cart
     */
    public int getQuantity(final Saleable sellable) throws ProductNotFoundException {
        if (!cartItemMap.containsKey(sellable.getId())) throw new ProductNotFoundException();
        return cartItemMap.get(sellable.getId());
    }

    /**
     * Get total cost of a {@link Saleable} product in this shopping cart
     *
     * @param sellable the product of interest which this method will return the total cost
     * @return Total cost of the product
     * @throws ProductNotFoundException if the product is not found in this shopping cart
     */
    public BigDecimal getCost(final Saleable sellable) throws ProductNotFoundException {
        if (!cartItemMap.containsKey(sellable.getId())) throw new ProductNotFoundException();
        return sellable.getPrice().multiply(BigDecimal.valueOf(cartItemMap.get(sellable.getId())));
    }

    /**
     * Get total price of all products in this shopping cart
     *
     * @return Total price of all products in this shopping cart
     */
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    /**
     * Get total quantity of all products in this shopping cart
     *
     * @return Total quantity of all products in this shopping cart
     */
    public int getTotalQuantity() {
        return totalQuantity;
    }

    /**
     * Get set of products in this shopping cart
     *
     * @return Set of {@link Saleable} products in this shopping cart
     */
    public Set<String> getProductsIds() {
        return cartItemMap.keySet();
    }

    /**
     * Get a map of products to their quantities in the shopping cart
     *
     * @return A map from product to its quantity in this shopping cart
     */
    public Map<String, Integer> getItemWithQuantity() {
        Map<String, Integer> cartItemMap = new HashMap<String, Integer>();
        cartItemMap.putAll(this.cartItemMap);
        return cartItemMap;
    }

    public List<Saleable> getSaleableList() {
        List<Saleable> result = new ArrayList<Saleable>(cartSaleables);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        for (Entry<String, Integer> entry : cartItemMap.entrySet()) {
            strBuilder.append(String.format("Product: %s, Quantity: %d%n", entry.getKey(), entry.getValue()));
        }
        strBuilder.append(String.format("Total Quantity: %d, Total Price: %f", totalQuantity, totalPrice));

        return strBuilder.toString();
    }


    public List<CartItem> getCartItemsList(){
        List<CartItem> cartItems = new ArrayList<CartItem>();
        Map<String, Integer> items = getItemWithQuantity();
        List<Saleable> products = getSaleableList();

        Iterator<Saleable> it = products.iterator();
        while (it.hasNext()) {
            Saleable saleable = it.next();
            CartItem cartItem = new CartItem((Product)saleable, items.get(saleable.getId()));
            cartItems.add(cartItem);
        }
        return cartItems;

    }

    public static class CartItem{
        public final Product product;
        public final int quantity;

        public CartItem(){
            this.product = null;
            this.quantity = 0;
        }

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        @Override
        public String toString() { return product.getName();}
    }

}
