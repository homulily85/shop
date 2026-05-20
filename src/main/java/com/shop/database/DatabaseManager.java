package com.shop.database;

import com.shop.service.VaultService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = VaultService.getInstance().getSecret("DB_URL");
    private static final String USER = VaultService.getInstance().getSecret("DB_USERNAME");
    private static final String PASS = VaultService.getInstance().getSecret("DB_PASSWORD");

    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASS);

        config.setConnectionTimeout(5000);
        config.setMaximumPoolSize(60);
        config.setMaxLifetime(600000);
        config.setIdleTimeout(60000);
        config.setAutoCommit(true);

        dataSource = new HikariDataSource(config);
    }

    /**
     * Gets a connection from the HikariCP pool.
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Call this when shutting down your application to cleanly close all connections.
     */
    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}