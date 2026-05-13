package com.shop.service;

import com.shop.model.Cart;
import com.shop.model.Product;
import com.shop.repository.CartRepository;

public class CartService {
    private final ProductService productService = ProductService.getInstance();
    private final CartRepository cartRepository = CartRepository.getInstance();

    private CartService() {
    }

    /**
     * Provides access to the singleton instance of CartService.
     *
     * @return Singleton instance of CartService.
     */
    public static CartService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Get the list of items in the ACTIVE cart for a given customer ID.
     *
     * @param customerId ID of the customer whose cart items are to be retrieved.
     * @return List of OrderItem objects representing the items in the cart.
     *         If the customer has no active cart or if the cart is empty, an empty
     *         list will be returned.
     * @throws NumberFormatException if the provided customer ID string cannot be
     *                               parsed to a long.
     */
    public Cart getCart(String customerId) {
        return cartRepository.getCartItemsInActiveCartOfCustomer(Long.parseLong(customerId));

    }

    /**
     * Update the cart by adding or updating the quantityInCart of a specific
     * product in the active
     * cart associated with the given customer ID.
     *
     * @param customerId ID of the customer whose cart is to be updated.
     * @param productId  ID of the product to be added or updated in the cart.
     *
     */
    public void updateCart(String customerId, long productId, long quantity) {
        Product product = productService.getProductById(String.valueOf(productId));
        if (product == null) {
            throw new IllegalArgumentException("Product with ID " + productId + " does not exist.");
        }

        var activeCartId = cartRepository.getActiveCartOfCustomer(Long.parseLong(customerId));

        if (activeCartId < 0) {
            activeCartId = cartRepository.createNewActiveCartForCustomer(Long.parseLong(customerId));
        }

        var currentQuantity = cartRepository.getQuantityOfAItem(activeCartId, productId);

        if (quantity <= 0) {
            cartRepository.removeItemFromCart(activeCartId, productId);
            return;
        }

        if (quantity > product.availableQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock for " +
                    "product ID " + productId);
        }

        if (currentQuantity < 0) {
            cartRepository.addItemToCart(activeCartId, productId, quantity);
        } else {
            cartRepository.updateCartItem(activeCartId, productId, quantity);
        }

    }

    /**
     * Remove a specific product from the active cart associated with the given
     * customer ID.
     *
     * @param customerId ID of the cart from which to remove the product.
     * @param productId  ID of the product to be removed from the cart.
     */
    public void removeItem(String customerId, long productId) {
        var activeCartId = cartRepository.getActiveCartOfCustomer(Long.parseLong(customerId));
        if (activeCartId < 0) {
            return;
        }

        cartRepository.removeItemFromCart(activeCartId, productId);
    }

    /**
     * Clear all items from the cart identified by the given cart ID.
     *
     * @param cartId ID of the cart to be cleared.
     */
    public void clearCart(String cartId) {
        var activeCartId = cartRepository.getActiveCartOfCustomer(Long.parseLong(cartId));
        if (activeCartId < 0) {
            return;
        }

        cartRepository.makeACartInactive(activeCartId);
    }

    private static class Holder {
        private static final CartService INSTANCE = new CartService();
    }
}