package com.emergency.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class for establishing a JDBC connection to the MySQL database.
 * Reads database credentials securely from db.properties.
 */
public class DatabaseConnection {

    private static String url;
    private static String username;
    private static String password;
    private static boolean initialized = false;

    private static synchronized void loadConfiguration() {
        if (initialized) {
            return;
        }

        Properties props = new Properties();
        boolean loaded = false;

        // 1. Try loading from file in working directory
        File file = new File("db.properties");
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
                loaded = true;
            } catch (Exception e) {
                System.err.println("Could not read db.properties from root directory: " + e.getMessage());
            }
        }

        // 2. Try loading from classpath as fallback
        if (!loaded) {
            try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) {
                    props.load(in);
                    loaded = true;
                }
            } catch (Exception e) {
                System.err.println("Could not read db.properties from classpath: " + e.getMessage());
            }
        }

        url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/smart_emergency_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        username = props.getProperty("db.username", "new_user");
        password = props.getProperty("db.password", "");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found in classpath: " + e.getMessage());
        }

        initialized = true;
    }

    /**
     * Gets a new JDBC Connection to the database.
     * Callers should close the Connection using try-with-resources.
     *
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            loadConfiguration();
        }
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Helper to test if database connection is available.
     *
     * @return true if connection succeeded, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
