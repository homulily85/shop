package com.shop.service;

import com.shop.model.OrderItem;
import com.shop.model.Product;
import com.shop.redis.Client;
import redis.clients.jedis.RedisClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CartService {
    private final RedisClient redisClient = Client.getRedisClient();
    private final ProductService productService = ProductService.getInstance();

    private CartService() {
    }

    /**
     * Provides access to the singleton instance of CartService.
     * @return Singleton instance of CartService.
     */
    public static CartService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Get all items in the cart for the given cart ID.
     * @param cartId Cart ID to retrieve items for.
     * @return List of OrderItem objects representing the items in the cart. If the cart is empty, returns an empty list.
     */
    public List<OrderItem> getCartItems(String cartId) {
        Map<String, String> cartData = redisClient.hgetAll("cart:" + cartId);

        if (cartData.isEmpty()) {
            return new ArrayList<>();
        }

        return cartData.entrySet().stream()
                .map(entry -> {
                    String productIdStr = entry.getKey();
                    long quantity = Long.parseLong(entry.getValue());

                    Product product = productService.getProductById(productIdStr);

                    return new OrderItem(product, quantity);
                })
                .filter(item -> item.product() != null)
                .collect(Collectors.toList());
    }

    /**
     * Add a product to the cart with the specified quantity.
     * If the product already exists in the cart, the quantity will be updated.
     * @param cartId ID of the cart to add the product to.
     * @param productId ID of the product to add to the cart.
     * @param quantity Quantity of the product to add to the cart. If the quantity is positive, it will be added to the existing quantity.
     *                 If the quantity is negative, it will be subtracted from the existing quantity. If the resulting
     *                 quantity is less than or equal to zero, the product will be removed from the cart.
     * @throws IllegalArgumentException if the product does not exist or if the requested quantity exceeds available stock.
     */
    public void addToCart(String cartId, long productId, long quantity) {
        Product product = productService.getProductById(String.valueOf(productId));
        if (product == null) {
            throw new IllegalArgumentException("Product with ID " + productId + " does not exist.");
        }

        String key = "cart:" + cartId;
        String field = String.valueOf(productId);

        String currentQtyStr = redisClient.hget(key, field);
        long currentQty = (currentQtyStr != null) ? Long.parseLong(currentQtyStr) : 0;

        if (quantity > 0 && (currentQty + quantity > product.availableQuantity())) {
            throw new IllegalArgumentException("Cannot add to cart. Requested availableQuantity exceeds " +
                    "available stock (" + product.availableQuantity() + ").");
        }

        long newQuantity = redisClient.hincrBy(key, field, quantity);

        if (newQuantity <= 0) {
            redisClient.hdel(key, field);
        }

        redisClient.expire(key, 3600);
    }

    /**
     * Remove a specific product from the cart based on the cart ID and product ID.
     * @param cartId ID of the cart from which to remove the product.
     * @param productId ID of the product to be removed from the cart.
     */
    public void removeItem(String cartId, long productId) {
        redisClient.hdel("cart:" + cartId, String.valueOf(productId));
    }

    /**
     * Clear all items from the cart identified by the given cart ID.
     * @param cartId ID of the cart to be cleared.
     */
    public void clearCart(String cartId) {
        redisClient.del("cart:" + cartId);
    }

    private static class Holder {
        private static final CartService INSTANCE = new CartService();
    }
}