package com.emergency.storage;

import com.emergency.enums.UserRole;
import com.emergency.model.User;
import com.emergency.util.DatabaseConnection;
import com.emergency.util.PasswordUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Storage class for managing Users via MySQL JDBC.
 */
public class UserStorage {

    public UserStorage() {
        seedDefaultUsers();
    }

    public void seedDefaultUsers() {
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
            System.err.println("Error seeding default users: " + e.getMessage());
        }
    }

    public int generateUserId() {
        String sql = "SELECT COALESCE(MAX(user_id), 0) + 1 FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error generating user id: " + e.getMessage());
        }
        return 1;
    }

    public void addUser(User user) {
        if (user == null) return;
        String sql = "INSERT INTO users (user_code, name, email, phone, password_hash, password_salt, role, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String code = user.getUserCode();
            if (code == null || code.trim().isEmpty()) {
                code = (user.getRole() == UserRole.ADMIN ? "ADMIN" : "USER") + System.currentTimeMillis() % 10000;
            }

            ps.setString(1, code);
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getPasswordHash());
            ps.setString(6, user.getPasswordSalt());
            ps.setString(7, user.getRole().name());
            ps.setTimestamp(8, Timestamp.valueOf(user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    user.setUserId(generatedId);
                    if (user.getUserCode() == null || user.getUserCode().trim().isEmpty()) {
                        user.setUserCode((user.getRole() == UserRole.ADMIN ? "ADMIN" : "USER") + String.format("%03d", generatedId));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
        }
    }

    public User findUserById(int userId) {
        String sql = "SELECT user_id, user_code, name, email, phone, password_hash, password_salt, role, created_at " +
                     "FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by id: " + e.getMessage());
        }
        return null;
    }

    public User findUserByEmail(String email) {
        if (email == null) return null;
        String sql = "SELECT user_id, user_code, name, email, phone, password_hash, password_salt, role, created_at " +
                     "FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by email: " + e.getMessage());
        }
        return null;
    }

    public User findUserByIdentifier(String identifier) {
        if (identifier == null) return null;
        String query = identifier.trim();
        String sql = "SELECT user_id, user_code, name, email, phone, password_hash, password_salt, role, created_at FROM users " +
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
        } catch (SQLException e) {
            System.err.println("Error finding user by identifier: " + e.getMessage());
        }
        return null;
    }

    public ArrayList<User> getAllUsers() {
        ArrayList<User> list = new ArrayList<>();
        String sql = "SELECT user_id, user_code, name, email, phone, password_hash, password_salt, role, created_at " +
                     "FROM users ORDER BY user_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        return list;
    }

    public boolean removeUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing user: " + e.getMessage());
            return false;
        }
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
