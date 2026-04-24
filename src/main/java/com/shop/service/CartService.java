package com.shop.service;

import com.shop.dto.CartItemDTO;
import com.shop.redis.Client;
import redis.clients.jedis.RedisClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CartService {
    private final RedisClient redisClient = Client.getRedisClient();

    private CartService() {
    }

    public static CartService getInstance() {
        return Holder.INSTANCE;
    }

    public List<CartItemDTO> getCartItems(String cartId) {
        Map<String, String> cartData = redisClient.hgetAll("cart:" + cartId);

        if (cartData.isEmpty()) {
            return new ArrayList<>();
        }

        return cartData.entrySet().stream()
                .map(entry -> new CartItemDTO(
                        Long.parseLong(entry.getKey()),
                        Long.parseLong(entry.getValue())
                ))
                .collect(Collectors.toList());
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