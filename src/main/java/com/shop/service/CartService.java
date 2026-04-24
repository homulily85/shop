package com.shop.service;

import com.shop.model.CartItem;
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

    public static CartService getInstance() {
        return Holder.INSTANCE;
    }

    public List<CartItem> getCartItems(String cartId) {
        Map<String, String> cartData = redisClient.hgetAll("cart:" + cartId);

        if (cartData.isEmpty()) {
            return new ArrayList<>();
        }

        return cartData.entrySet().stream()
                .map(entry -> {
                    String productIdStr = entry.getKey();
                    long quantity = Long.parseLong(entry.getValue());

                    Product product = productService.getProductById(productIdStr);

                    return new CartItem(product, quantity);
                })
                .filter(item -> item.product() != null)
                .collect(Collectors.toList());
    }

    public void addToCart(String cartId, long productId, long quantity) {
        Product product = productService.getProductById(String.valueOf(productId));
        if (product == null) {
            throw new IllegalArgumentException("Product with ID " + productId + " does not exist.");
        }

        String key = "cart:" + cartId;
        String field = String.valueOf(productId);

        String currentQtyStr = redisClient.hget(key, field);
        long currentQty = (currentQtyStr != null) ? Long.parseLong(currentQtyStr) : 0;

        if (quantity > 0 && (currentQty + quantity > product.quantity())) {
            throw new IllegalArgumentException("Cannot add to cart. Requested quantity exceeds " +
                    "available stock (" + product.quantity() + ").");
        }

        long newQuantity = redisClient.hincrBy(key, field, quantity);

        if (newQuantity <= 0) {
            redisClient.hdel(key, field);
        }

        redisClient.expire(key, 3600);
    }

    public void removeItem(String cartId, long productId) {
        redisClient.hdel("cart:" + cartId, String.valueOf(productId));
    }

    private static class Holder {
        private static final CartService INSTANCE = new CartService();
    }
}