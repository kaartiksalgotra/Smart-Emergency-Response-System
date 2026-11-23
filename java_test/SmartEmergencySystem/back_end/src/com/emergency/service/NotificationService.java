package com.emergency.service;

import com.emergency.model.Notification;
import com.emergency.model.Emergency;
import com.emergency.storage.NotificationStorage;

import java.util.ArrayList;

public class NotificationService {

    private NotificationStorage notificationStorage;

    public NotificationService(NotificationStorage notificationStorage) {
        this.notificationStorage = notificationStorage;
    }

    private void createNotification(int userId, int emergencyId, String message, String type) {
        int id = notificationStorage.generateNotificationId();
        Notification notification = new Notification(id, userId, emergencyId, message, type);
        notificationStorage.addNotification(notification);
    }

    public void notifyEmergencyRequested(Emergency emergency) {
        if (emergency != null) {
            createNotification(emergency.getUserId(), emergency.getEmergencyId(),
                    "Your emergency request #" + emergency.getEmergencyId() + " has been submitted and queued for Admin review.",
                    "REQUEST_SUBMITTED");
        }
    }

    public void notifyEmergencyReported(Emergency emergency) {
        notifyEmergencyRequested(emergency);
    }

    public void notifyEmergencyAccepted(Emergency emergency) {
        if (emergency != null) {
            createNotification(emergency.getUserId(), emergency.getEmergencyId(),
                    "Your emergency request #" + emergency.getEmergencyId() + " has been ACCEPTED by Admin. Dispatching unit.",
                    "REQUEST_ACCEPTED");
        }
    }

    public void notifyEmergencyRejected(Emergency emergency) {
        if (emergency != null) {
            createNotification(emergency.getUserId(), emergency.getEmergencyId(),
                    "Your emergency request #" + emergency.getEmergencyId() + " has been REJECTED by Admin.",
                    "REQUEST_REJECTED");
        }
    }

    public void notifyResourceAssigned(Emergency emergency) {
        if (emergency != null) {
            createNotification(emergency.getUserId(), emergency.getEmergencyId(),
                    "A response unit has been assigned and dispatched to your emergency #" + emergency.getEmergencyId() + ".",
                    "RESOURCE_ASSIGNED");
        }
    }

    public void notifyStatusChanged(Emergency emergency) {
        if (emergency != null) {
            createNotification(emergency.getUserId(), emergency.getEmergencyId(),
                    "Emergency #" + emergency.getEmergencyId() + " status updated to " + emergency.getStatus() + ".",
                    "STATUS_CHANGED");
        }
    }

    public void notifyEmergencyResolved(Emergency emergency) {
        if (emergency != null) {
            createNotification(emergency.getUserId(), emergency.getEmergencyId(),
                    "Emergency #" + emergency.getEmergencyId() + " has been successfully RESOLVED.",
                    "EMERGENCY_RESOLVED");
        }
    }

    public void notifyEmergencyCancelled(Emergency emergency) {
        if (emergency != null) {
            createNotification(emergency.getUserId(), emergency.getEmergencyId(),
                    "Emergency #" + emergency.getEmergencyId() + " has been CANCELLED.",
                    "EMERGENCY_CANCELLED");
        }
    }

    public ArrayList<Notification> getUserNotifications(int userId) {
        return notificationStorage.getNotificationsByUserId(userId);
    }

    public ArrayList<Notification> getUnreadNotifications(int userId) {
        return notificationStorage.getUnreadNotifications(userId);
    }

    public boolean markAsRead(int notificationId, int userId) {
        return notificationStorage.markNotificationAsRead(notificationId, userId);
    }
}
