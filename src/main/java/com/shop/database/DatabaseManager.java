package com.shop.database;

import com.shop.service.VaultService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = VaultService.getInstance().getSecret("DB_URL");
    private static final String USER = VaultService.getInstance().getSecret("DB_USERNAME");
    private static final String PASS = VaultService.getInstance().getSecret("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
