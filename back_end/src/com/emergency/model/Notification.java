
package com.emergency.model;

import java.time.LocalDateTime;

public class Notification {

    private int notificationId;
    private int userId;
    private int emergencyId;
    private String message;
    private String notificationType;
    private boolean isRead;
    private LocalDateTime createdAt;

    public Notification(int notificationId, int userId, int emergencyId,
            String message, String notificationType) {

        this.notificationId = notificationId;
        this.userId = userId;
        this.emergencyId = emergencyId;
        this.message = message;
        this.notificationType = notificationType;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public int getNotificationId() {
        return notificationId;
    }

    public int getUserId() {
        return userId;
    }

    public int getEmergencyId() {
        return emergencyId;
    }

    public String getMessage() {
        return message;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public boolean isRead() {
        return isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
