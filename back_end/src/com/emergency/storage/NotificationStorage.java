package com.emergency.storage;

import com.emergency.model.Notification;
import com.emergency.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Storage class for managing Notifications via MySQL JDBC.
 */
public class NotificationStorage {

    public NotificationStorage() {
        // Database-backed storage: records are permanently persisted in MySQL
    }

    public int generateNotificationId() {
        String sql = "SELECT COALESCE(MAX(notification_id), 0) + 1 FROM notifications";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error generating notification id: " + e.getMessage());
        }
        return 1;
    }

    public void addNotification(Notification notification) {
        if (notification == null) return;
        String sql = "INSERT INTO notifications (user_id, emergency_id, message, notification_type, is_read, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, notification.getUserId());
            ps.setInt(2, notification.getEmergencyId());
            ps.setString(3, notification.getMessage());
            ps.setString(4, notification.getNotificationType());
            ps.setBoolean(5, notification.isRead());
            ps.setTimestamp(6, Timestamp.valueOf(notification.getCreatedAt() != null ? notification.getCreatedAt() : LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    notification.setNotificationId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding notification: " + e.getMessage());
        }
    }

    public Notification findNotificationById(int notificationId) {
        String sql = "SELECT notification_id, user_id, emergency_id, message, notification_type, is_read, created_at " +
                     "FROM notifications WHERE notification_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToNotification(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding notification by id: " + e.getMessage());
        }
        return null;
    }

    public ArrayList<Notification> getNotificationsByUserId(int userId) {
        ArrayList<Notification> list = new ArrayList<>();
        String sql = "SELECT notification_id, user_id, emergency_id, message, notification_type, is_read, created_at " +
                     "FROM notifications WHERE user_id = ? ORDER BY notification_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting notifications for user: " + e.getMessage());
        }
        return list;
    }

    public ArrayList<Notification> getAllNotifications() {
        ArrayList<Notification> list = new ArrayList<>();
        String sql = "SELECT notification_id, user_id, emergency_id, message, notification_type, is_read, created_at " +
                     "FROM notifications ORDER BY notification_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all notifications: " + e.getMessage());
        }
        return list;
    }

    public ArrayList<Notification> getUnreadNotifications(int userId) {
        ArrayList<Notification> list = new ArrayList<>();
        String sql = "SELECT notification_id, user_id, emergency_id, message, notification_type, is_read, created_at " +
                     "FROM notifications WHERE user_id = ? AND is_read = 0 ORDER BY notification_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting unread notifications: " + e.getMessage());
        }
        return list;
    }

    public boolean markNotificationAsRead(int notificationId, int userId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE notification_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            return false;
        }
    }

    public boolean removeNotification(int notificationId) {
        String sql = "DELETE FROM notifications WHERE notification_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing notification: " + e.getMessage());
            return false;
        }
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        int id = rs.getInt("notification_id");
        int userId = rs.getInt("user_id");
        int emergencyId = rs.getInt("emergency_id");
        String message = rs.getString("message");
        String type = rs.getString("notification_type");
        boolean isRead = rs.getBoolean("is_read");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime created = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        return new Notification(id, userId, emergencyId, message, type, isRead, created);
    }
}
