package com.bank.util;

import java.io.InputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseUtil {
    private static final String DB_URL = loadDatabaseUrl();

    private static String loadDatabaseUrl() {
        try (InputStream input = DatabaseUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            String configuredUrl = "jdbc:sqlite:bank_system.db";
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                configuredUrl = prop.getProperty("db.url", configuredUrl);
            }
            return System.getProperty("bank.db.url", configuredUrl);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL);
        try (var statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
            return connection;
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
    }

    public static String getDatabaseUrl() {
        return DB_URL;
    }
}
