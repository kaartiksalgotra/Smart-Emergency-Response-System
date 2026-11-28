package com.emergency.storage;

import com.emergency.enums.UserRole;
import com.emergency.model.User;
import com.emergency.util.DatabaseConnection;
import com.emergency.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Storage class for managing Users via MySQL JDBC with seamless in-memory
 * fallback.
 */
public final class UserStorage {

    private final List<User> memoryUsers = Collections.synchronizedList(new ArrayList<>());
    private int nextUserId = 1;

    public UserStorage() {
        if (DatabaseConnection.isAvailable()) {
            seedDefaultUsers();
        } else {
            seedMemoryUsers();
        }
    }

    private void seedMemoryUsers() {
        if (memoryUsers.isEmpty()) {
            String adminSalt = PasswordUtil.generateSalt();
            String adminHash = PasswordUtil.hashPassword("Admin123", adminSalt);
            memoryUsers.add(new User(1, "ADMIN001", "System Admin", "admin@emergency.com",
                    "9876543210", adminHash, adminSalt, UserRole.ADMIN, LocalDateTime.now()));

            String userSalt = PasswordUtil.generateSalt();
            String userHash = PasswordUtil.hashPassword("User123", userSalt);
            memoryUsers.add(new User(2, "USER001", "Citizen User", "user@emergency.com",
                    "9123456780", userHash, userSalt, UserRole.USER, LocalDateTime.now()));
            nextUserId = 3;
        }
    }

    public final void seedDefaultUsers() {
        String countSql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(countSql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next() && rs.getInt(1) == 0) {
                String adminSalt = PasswordUtil.generateSalt();
                String adminHash = PasswordUtil.hashPassword("Admin123", adminSalt);
                addUser(new User(0, "ADMIN001", "System Admin", "admin@emergency.com",
                        "9876543210", adminHash, adminSalt, UserRole.ADMIN));

                String userSalt = PasswordUtil.generateSalt();
                String userHash = PasswordUtil.hashPassword("User123", userSalt);
                addUser(new User(0, "USER001", "Citizen User", "user@emergency.com",
                        "9123456780", userHash, userSalt, UserRole.USER));
            }
        } catch (SQLException e) {
            System.err.println("Database unavailable, switching to in-memory store: " + e.getMessage());
            seedMemoryUsers();
        }
    }

    public synchronized int generateUserId() {
        if (DatabaseConnection.isAvailable()) {
            String sql = "SELECT COALESCE(MAX(user_id), 0) + 1 FROM users";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException ignored) {
            }
        }
        return nextUserId++;
    }

    public final synchronized void addUser(User user) {
        if (user == null)
            return;

        if (user.getUserId() <= 0) {
            user.setUserId(generateUserId());
        }
        if (user.getUserCode() == null || user.getUserCode().trim().isEmpty()) {
            user.setUserCode(
                    (user.getRole() == UserRole.ADMIN ? "ADMIN" : "USER") + String.format("%03d", user.getUserId()));
        }

        // Always keep in-memory cache updated
        synchronized (memoryUsers) {
            boolean exists = false;
            for (int i = 0; i < memoryUsers.size(); i++) {
                if (memoryUsers.get(i).getUserId() == user.getUserId()) {
                    memoryUsers.set(i, user);
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                memoryUsers.add(user);
            }
        }

        if (DatabaseConnection.isAvailable()) {
            String sql = "INSERT INTO users (user_code, name, email, phone, password_hash, password_salt, role, created_at) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, user.getUserCode());
                ps.setString(2, user.getName());
                ps.setString(3, user.getEmail());
                ps.setString(4, user.getPhone());
                ps.setString(5, user.getPasswordHash());
                ps.setString(6, user.getPasswordSalt());
                ps.setString(7, user.getRole().name());
                ps.setTimestamp(8, Timestamp.valueOf(user.getCreatedAt()));

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        user.setUserId(generatedId);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Warning: Could not persist user to database: " + e.getMessage());
            }
        }
    }

    public User findUserById(int userId) {
        if (DatabaseConnection.isAvailable()) {
            String sql = "SELECT user_id, user_code, name, email, phone, password_hash, password_salt, role, created_at "
                    +
                    "FROM users WHERE user_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                }
            } catch (SQLException ignored) {
            }
        }

        synchronized (memoryUsers) {
            for (User u : memoryUsers) {
                if (u.getUserId() == userId)
                    return u;
            }
        }
        return null;
    }

    public User findUserByEmail(String email) {
        if (email == null)
            return null;
        if (DatabaseConnection.isAvailable()) {
            String sql = "SELECT user_id, user_code, name, email, phone, password_hash, password_salt, role, created_at "
                    +
                    "FROM users WHERE LOWER(email) = LOWER(?)";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                }
            } catch (SQLException ignored) {
            }
        }

        synchronized (memoryUsers) {
            for (User u : memoryUsers) {
                if (u.getEmail() != null && u.getEmail().equalsIgnoreCase(email.trim())) {
                    return u;
                }
            }
        }
        return null;
    }

    public User findUserByIdentifier(String identifier) {
        if (identifier == null)
            return null;
        String query = identifier.trim();

        if (DatabaseConnection.isAvailable()) {
            String sql = "SELECT user_id, user_code, name, email, phone, password_hash, password_salt, role, created_at FROM users "
                    +
                    "WHERE LOWER(email) = LOWER(?) OR LOWER(user_code) = LOWER(?) OR CAST(user_id AS CHAR) = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, query);
                ps.setString(2, query);
                ps.setString(3, query);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                }
            } catch (SQLException ignored) {
            }
        }

        synchronized (memoryUsers) {
            for (User u : memoryUsers) {
                if ((u.getEmail() != null && u.getEmail().equalsIgnoreCase(query)) ||
                        (u.getUserCode() != null && u.getUserCode().equalsIgnoreCase(query)) ||
                        String.valueOf(u.getUserId()).equals(query)) {
                    return u;
                }
            }
        }
        return null;
    }

    public ArrayList<User> getAllUsers() {
        if (DatabaseConnection.isAvailable()) {
            ArrayList<User> list = new ArrayList<>();
            String sql = "SELECT user_id, user_code, name, email, phone, password_hash, password_salt, role, created_at "
                    +
                    "FROM users ORDER BY user_id ASC";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
                if (!list.isEmpty())
                    return list;
            } catch (SQLException ignored) {
            }
        }

        synchronized (memoryUsers) {
            return new ArrayList<>(memoryUsers);
        }
    }

    public boolean removeUser(int userId) {
        boolean removedFromMemory = false;
        synchronized (memoryUsers) {
            removedFromMemory = memoryUsers.removeIf(u -> u.getUserId() == userId);
        }

        if (DatabaseConnection.isAvailable()) {
            String sql = "DELETE FROM users WHERE user_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                return ps.executeUpdate() > 0;
            } catch (SQLException ignored) {
            }
        }
        return removedFromMemory;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("user_id");
        String code = rs.getString("user_code");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String hash = rs.getString("password_hash");
        String salt = rs.getString("password_salt");
        String roleStr = rs.getString("role");
        UserRole role = UserRole.valueOf(roleStr);
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime created = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        return new User(id, code, name, email, phone, hash, salt, role, created);
    }
}
