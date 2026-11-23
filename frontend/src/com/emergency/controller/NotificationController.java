
package com.emergency.controller;

import com.emergency.model.Notification;
import com.emergency.model.User;

import com.emergency.service.NotificationService;

import com.emergency.views.NotificationView;

import javafx.collections.FXCollections;

import java.util.ArrayList;

public class NotificationController {

    private NotificationView notificationView;
    private NotificationService notificationService;

    private User user;
    private Runnable backAction;

    public NotificationController(
            NotificationView notificationView,
            NotificationService notificationService,
            User user,
            Runnable backAction) {

        this.notificationView = notificationView;
        this.notificationService = notificationService;
        this.user = user;
        this.backAction = backAction;

        notificationView.getRefreshButton().setOnAction(e -> {
            loadNotifications();
        });

        notificationView.getMarkReadButton().setOnAction(e -> {
            markAsRead();
        });

        notificationView.getBackButton().setOnAction(e -> {
            backAction.run();
        });

        loadNotifications();

    }

    private void loadNotifications() {

        ArrayList<Notification> notifications =
                notificationService.getUserNotifications(
                        user.getUserId()
                );

        notificationView.getNotificationTable().setItems(
                FXCollections.observableArrayList(notifications)
        );

    }

    private void markAsRead() {

        Notification selectedNotification =
                notificationView.getNotificationTable()
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedNotification == null) {
            return;
        }

        notificationService.markAsRead(
                selectedNotification.getNotificationId(),
                user.getUserId()
        );

        loadNotifications();

    }

}
