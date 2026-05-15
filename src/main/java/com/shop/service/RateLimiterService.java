package com.shop.service;

import com.shop.redis.Client;
import redis.clients.jedis.RedisClient;

public class RateLimiterService {
    private final RedisClient redisClient = Client.getRedisClient();

    private static final int MAX_REQUESTS = 100;
    private static final int WINDOW_SECONDS = 60;

    private RateLimiterService() {}

    public static RateLimiterService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Checks if the given client is allowed to make a request.
     * * @param clientId An identifier for the client (e.g., IP address, customer ID, or API key).
     * @return true if the request is allowed, false if the rate limit is exceeded.
     */
    public boolean isAllowed(String clientId) {
        String key = "rate_limit:" + clientId;

        long currentCount = redisClient.incr(key);

        if (currentCount == 1) {
            redisClient.expire(key, WINDOW_SECONDS);
        }

        return currentCount <= MAX_REQUESTS;
    }

    private static class Holder {
        private static final RateLimiterService INSTANCE = new RateLimiterService();
    }
}