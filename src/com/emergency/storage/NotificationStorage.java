package com.emergency.storage;

import com.emergency.model.Notification;
import com.emergency.util.DatabaseConnection;

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
 * Storage class for managing Notifications via MySQL JDBC with in-memory fallback.
 */
public class NotificationStorage {

    private final List<Notification> memoryNotifications = Collections.synchronizedList(new ArrayList<>());
    private int nextNotificationId = 1;

    public NotificationStorage() {
        // Ready for database or in-memory operation
    }

    public synchronized int generateNotificationId() {
        if (DatabaseConnection.isAvailable()) {
            String sql = "SELECT COALESCE(MAX(notification_id), 0) + 1 FROM notifications";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException ignored) {}
        }
        return nextNotificationId++;
    }

    public synchronized void addNotification(Notification notification) {
        if (notification == null) return;

        if (notification.getNotificationId() <= 0) {
            notification.setNotificationId(generateNotificationId());
        }

        synchronized (memoryNotifications) {
            boolean exists = false;
            for (int i = 0; i < memoryNotifications.size(); i++) {
                if (memoryNotifications.get(i).getNotificationId() == notification.getNotificationId()) {
                    memoryNotifications.set(i, notification);
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                memoryNotifications.add(0, notification);
            }
        }

        if (DatabaseConnection.isAvailable()) {
            String sql = "INSERT INTO notifications (user_id, emergency_id, message, notification_type, is_read, created_at) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, notification.getUserId());
                ps.setInt(2, notification.getEmergencyId());
                ps.setString(3, notification.getMessage());
                ps.setString(4, notification.getNotificationType());
                ps.setBoolean(5, notification.isRead());
                ps.setTimestamp(6, Timestamp.valueOf(notification.getCreatedAt()));

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        notification.setNotificationId(rs.getInt(1));
                    }
                }
            } catch (SQLException e) {
                System.err.println("Warning: Could not persist notification to database: " + e.getMessage());
            }
        }
    }

    public Notification findNotificationById(int notificationId) {
        if (DatabaseConnection.isAvailable()) {
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
            } catch (SQLException ignored) {}
        }

        synchronized (memoryNotifications) {
            for (Notification n : memoryNotifications) {
                if (n.getNotificationId() == notificationId) return n;
            }
        }
        return null;
    }

    public ArrayList<Notification> getNotificationsByUserId(int userId) {
        if (DatabaseConnection.isAvailable()) {
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
                if (!list.isEmpty()) return list;
            } catch (SQLException ignored) {}
        }

        ArrayList<Notification> list = new ArrayList<>();
        synchronized (memoryNotifications) {
            for (Notification n : memoryNotifications) {
                if (n.getUserId() == userId) {
                    list.add(n);
                }
            }
        }
        return list;
    }

    public ArrayList<Notification> getAllNotifications() {
        if (DatabaseConnection.isAvailable()) {
            ArrayList<Notification> list = new ArrayList<>();
            String sql = "SELECT notification_id, user_id, emergency_id, message, notification_type, is_read, created_at " +
                         "FROM notifications ORDER BY notification_id DESC";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
                if (!list.isEmpty()) return list;
            } catch (SQLException ignored) {}
        }

        synchronized (memoryNotifications) {
            return new ArrayList<>(memoryNotifications);
        }
    }

    public ArrayList<Notification> getUnreadNotifications(int userId) {
        if (DatabaseConnection.isAvailable()) {
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
                if (!list.isEmpty()) return list;
            } catch (SQLException ignored) {}
        }

        ArrayList<Notification> list = new ArrayList<>();
        synchronized (memoryNotifications) {
            for (Notification n : memoryNotifications) {
                if (n.getUserId() == userId && !n.isRead()) {
                    list.add(n);
                }
            }
        }
        return list;
    }

    public synchronized boolean markNotificationAsRead(int notificationId, int userId) {
        boolean updatedInMemory = false;
        synchronized (memoryNotifications) {
            for (Notification n : memoryNotifications) {
                if (n.getNotificationId() == notificationId && n.getUserId() == userId) {
                    n.markAsRead();
                    updatedInMemory = true;
                    break;
                }
            }
        }

        if (DatabaseConnection.isAvailable()) {
            String sql = "UPDATE notifications SET is_read = 1 WHERE notification_id = ? AND user_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, notificationId);
                ps.setInt(2, userId);
                return ps.executeUpdate() > 0;
            } catch (SQLException ignored) {}
        }

        return updatedInMemory;
    }

    public synchronized boolean removeNotification(int notificationId) {
        boolean removedFromMemory = false;
        synchronized (memoryNotifications) {
            removedFromMemory = memoryNotifications.removeIf(n -> n.getNotificationId() == notificationId);
        }

        if (DatabaseConnection.isAvailable()) {
            String sql = "DELETE FROM notifications WHERE notification_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, notificationId);
                return ps.executeUpdate() > 0;
            } catch (SQLException ignored) {}
        }

        return removedFromMemory;
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
