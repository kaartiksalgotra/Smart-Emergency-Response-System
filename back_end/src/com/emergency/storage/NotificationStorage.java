
package com.emergency.storage;

import com.emergency.model.Notification;
import java.util.ArrayList;

public class NotificationStorage {

    private ArrayList<Notification> notifications;
    private int nextNotificationId;

    public NotificationStorage() {
        notifications = new ArrayList<>();
        nextNotificationId = 1;
    }

    public int generateNotificationId() {
        return nextNotificationId++;
    }

    public void addNotification(Notification notification) {
        notifications.add(notification);
    }

    public Notification findNotificationById(int notificationId) {

        for (Notification notification : notifications) {

            if (notification.getNotificationId() == notificationId) {
                return notification;
            }
        }

        return null;
    }

    public ArrayList<Notification> getAllNotifications() {
        return new ArrayList<>(notifications);
    }

    public ArrayList<Notification> getNotificationsByUserId(int userId) {

        ArrayList<Notification> userNotifications = new ArrayList<>();

        for (Notification notification : notifications) {

            if (notification.getUserId() == userId) {
                userNotifications.add(notification);
            }
        }

        return userNotifications;
    }

    public ArrayList<Notification> getUnreadNotifications(int userId) {

        ArrayList<Notification> unreadNotifications = new ArrayList<>();

        for (Notification notification : notifications) {

            if (notification.getUserId() == userId && !notification.isRead()) {
                unreadNotifications.add(notification);
            }
        }

        return unreadNotifications;
    }

    public boolean markNotificationAsRead(int notificationId, int userId) {

        Notification notification = findNotificationById(notificationId);

        if (notification != null && notification.getUserId() == userId) {
            notification.markAsRead();
            return true;
        }

        return false;
    }

    public boolean removeNotification(int notificationId) {

        Notification notification = findNotificationById(notificationId);

        if (notification != null) {
            notifications.remove(notification);
            return true;
        }

        return false;
    }
}
