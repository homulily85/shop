package com.shop.redis;

import com.shop.service.VaultService;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.RedisClient;

/**
 * Wrapper for RedisClient.
 */
public class Client {
    private static final String REDIS_HOST = VaultService.getInstance().getSecret("REDIS_HOST");
    private static final int REDIS_PORT = Integer.parseInt(VaultService.getInstance().getSecret("REDIS_PORT"));
    private Client() {

    }

    /**
     * Initializes the Redis client.
     * @return Initialized Redis client ready for use.
     */
    private static RedisClient create() {
        ConnectionPoolConfig connectPoolConfig = new ConnectionPoolConfig();
        connectPoolConfig.setMaxTotal(128);
        connectPoolConfig.setMaxIdle(128);
        connectPoolConfig.setMinIdle(16);

        return RedisClient.builder()
                .hostAndPort(REDIS_HOST, REDIS_PORT)
                .clientConfig(
                        DefaultJedisClientConfig.builder()
                                .socketTimeoutMillis(5000)
                                .connectionTimeoutMillis(5000)
                                .build())
                .poolConfig(connectPoolConfig)
                .build();
    }

    /**
     * Provides access to the singleton Redis client instance.
     * @return Singleton instance of the Redis client.
     */
    public static RedisClient getRedisClient() {
        return ClientHolder.HOLDER;
    }

    private static class ClientHolder {
        private static final RedisClient HOLDER = create();
    }
}