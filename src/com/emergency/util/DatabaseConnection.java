package com.emergency.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Utility class for establishing a JDBC connection to the MySQL database.
 * Reads database credentials securely from db.properties.
 * Provides fast status checks, automatic schema verification, and graceful fallback when database is not running.
 */
public class DatabaseConnection {

    private static String url;
    private static String username;
    private static String password;
    private static boolean initialized = false;
    private static Boolean available = null;
    private static boolean schemaVerified = false;

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

        url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/smart_emergency_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=2000&socketTimeout=2000");
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
     * Checks if the MySQL database is currently available and accessible.
     * Caches the result to prevent repeated connection timeouts.
     */
    public static synchronized boolean isAvailable() {
        if (available != null) {
            return available;
        }
        available = testConnection();
        return available;
    }

    /**
     * Resets the availability cache to re-check connectivity.
     */
    public static synchronized void resetAvailability() {
        available = null;
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
        DriverManager.setLoginTimeout(2);
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Helper to test if database connection is available and verify schema.
     *
     * @return true if connection succeeded, false otherwise
     */
    public static boolean testConnection() {
        try {
            if (!initialized) {
                loadConfiguration();
            }
            DriverManager.setLoginTimeout(2);
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                boolean ok = conn != null && !conn.isClosed();
                if (ok && !schemaVerified) {
                    ensureSchema(conn);
                    schemaVerified = true;
                }
                available = ok;
                return ok;
            }
        } catch (Exception e) {
            available = false;
            return false;
        }
    }

    private static void ensureSchema(Connection conn) {
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INT NOT NULL AUTO_INCREMENT, " +
                    "user_code VARCHAR(30) NOT NULL, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(150) NOT NULL, " +
                    "phone VARCHAR(20) NOT NULL, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "password_salt VARCHAR(255) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL, " +
                    "created_at DATETIME NOT NULL, " +
                    "PRIMARY KEY (user_id), " +
                    "UNIQUE KEY (user_code), " +
                    "UNIQUE KEY (email)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.execute("CREATE TABLE IF NOT EXISTS emergencies (" +
                    "emergency_id INT NOT NULL AUTO_INCREMENT, " +
                    "user_id INT NOT NULL, " +
                    "emergency_type VARCHAR(30) NOT NULL, " +
                    "priority VARCHAR(20) NOT NULL, " +
                    "location VARCHAR(255) NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "affected_people INT NOT NULL, " +
                    "status VARCHAR(30) NOT NULL, " +
                    "created_at DATETIME NOT NULL, " +
                    "updated_at DATETIME NOT NULL, " +
                    "PRIMARY KEY (emergency_id), " +
                    "KEY (user_id), " +
                    "CONSTRAINT fk_emergencies_user FOREIGN KEY (user_id) REFERENCES users (user_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.execute("CREATE TABLE IF NOT EXISTS resources (" +
                    "resource_id INT NOT NULL AUTO_INCREMENT, " +
                    "resource_name VARCHAR(100) NOT NULL, " +
                    "resource_type VARCHAR(30) NOT NULL, " +
                    "resource_status VARCHAR(30) NOT NULL, " +
                    "contact_number VARCHAR(20) NOT NULL, " +
                    "location VARCHAR(255) NOT NULL, " +
                    "patient_capacity INT DEFAULT NULL, " +
                    "water_capacity INT DEFAULT NULL, " +
                    "team_members INT DEFAULT NULL, " +
                    "PRIMARY KEY (resource_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.execute("CREATE TABLE IF NOT EXISTS allocations (" +
                    "allocation_id INT NOT NULL AUTO_INCREMENT, " +
                    "emergency_id INT NOT NULL, " +
                    "resource_id INT NOT NULL, " +
                    "assigned_at DATETIME NOT NULL, " +
                    "released_at DATETIME DEFAULT NULL, " +
                    "active TINYINT(1) NOT NULL DEFAULT 1, " +
                    "PRIMARY KEY (allocation_id), " +
                    "KEY (emergency_id), " +
                    "KEY (resource_id), " +
                    "CONSTRAINT fk_allocations_emergency FOREIGN KEY (emergency_id) REFERENCES emergencies (emergency_id), " +
                    "CONSTRAINT fk_allocations_resource FOREIGN KEY (resource_id) REFERENCES resources (resource_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.execute("CREATE TABLE IF NOT EXISTS notifications (" +
                    "notification_id INT NOT NULL AUTO_INCREMENT, " +
                    "user_id INT NOT NULL, " +
                    "emergency_id INT NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "notification_type VARCHAR(50) NOT NULL, " +
                    "is_read TINYINT(1) NOT NULL DEFAULT 0, " +
                    "created_at DATETIME NOT NULL, " +
                    "PRIMARY KEY (notification_id), " +
                    "KEY (user_id), " +
                    "KEY (emergency_id), " +
                    "CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (user_id), " +
                    "CONSTRAINT fk_notifications_emergency FOREIGN KEY (emergency_id) REFERENCES emergencies (emergency_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.execute("CREATE TABLE IF NOT EXISTS emergency_status_history (" +
                    "history_id INT NOT NULL AUTO_INCREMENT, " +
                    "emergency_id INT NOT NULL, " +
                    "previous_status VARCHAR(30) NOT NULL, " +
                    "new_status VARCHAR(30) NOT NULL, " +
                    "changed_at DATETIME NOT NULL, " +
                    "PRIMARY KEY (history_id), " +
                    "KEY (emergency_id), " +
                    "CONSTRAINT fk_status_history_emergency FOREIGN KEY (emergency_id) REFERENCES emergencies (emergency_id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // Clean up any stale orphaned status history whose emergency no longer exists
            st.executeUpdate("DELETE FROM emergency_status_history WHERE emergency_id NOT IN (SELECT emergency_id FROM emergencies)");

            // Clean up any duplicate consecutive history records from earlier runs
            st.executeUpdate("DELETE h1 FROM emergency_status_history h1 " +
                    "INNER JOIN emergency_status_history h2 " +
                    "ON h1.emergency_id = h2.emergency_id " +
                    "AND h1.previous_status = h2.previous_status " +
                    "AND h1.new_status = h2.new_status " +
                    "AND h1.history_id > h2.history_id");
        } catch (SQLException ignored) {
        }
    }
}
