package com.shop.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = System.getenv("DB_URL");
    private static final String USER = System.getenv("DB_USERNAME");
    private static final String PASS = System.getenv("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
