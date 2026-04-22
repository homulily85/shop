package com.shop.redis;

import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.RedisClient;

public class Client {
    private Client() {

    }

    private static RedisClient create() {
        ConnectionPoolConfig connectPoolConfig = new ConnectionPoolConfig();
        connectPoolConfig.setMaxTotal(128);
        connectPoolConfig.setMaxIdle(128);
        connectPoolConfig.setMinIdle(16);

        return RedisClient.builder()
                .hostAndPort(System.getenv("REDIS_HOST"), Integer.parseInt(System.getenv(
                        "REDIS_PORT")))
                .clientConfig(
                        DefaultJedisClientConfig.builder()
                                .socketTimeoutMillis(5000)
                                .connectionTimeoutMillis(5000)
                                .build())
                .poolConfig(connectPoolConfig)
                .build();
    }

    public static RedisClient getRedisClient() {
        return ClientHolder.HOLDER;
    }

    private static class ClientHolder {
        private static final RedisClient HOLDER = create();
    }
}