package com.shop.service;

import com.shop.redis.Client;
import redis.clients.jedis.RedisClient;

import java.util.Map;
import java.util.stream.Collectors;

public class CartService {
    private final RedisClient redisClient = Client.getRedisClient();

    private CartService() {
    }

    public static CartService getInstance() {
        return Holder.INSTANCE;
    }

    public String getCartItems(String cartId) {
        if (redisClient.exists("cart:" + cartId)) {
            Map<String, String> cartData = redisClient.hgetAll("cart:" + cartId);

            return cartData.entrySet().stream()
                    .map(entry -> String.format("""
                                    {
                                      "productId": %s,
                                      "quantity": %s
                                    }""",
                            entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining(",\n", "[\n", "\n]"));
        } else {
            return "[]";
        }
    }

    public void addToCart(String cartId, long productId, long quantity) {
        String key = "cart:" + cartId;
        String field = String.valueOf(productId);

        long newQuantity = redisClient.hincrBy(key, field, quantity);

        // If the quantity drops to 0 or below, remove the item entirely
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
