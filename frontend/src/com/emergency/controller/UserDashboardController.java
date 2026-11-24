package com.emergency.controller;

import com.emergency.enums.EmergencyStatus;
import com.emergency.model.Emergency;
import com.emergency.model.User;
import com.emergency.service.EmergencyService;
import com.emergency.service.NotificationService;
import com.emergency.threading.EmergencyProcessor;
import com.emergency.views.*;
import javafx.collections.FXCollections;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Controller for Citizen Dashboard.
 * Manages fixed-sidebar navigation, statistics refreshes, and content swapping.
 */
public class UserDashboardController {

    private UserDashboardView dashboardView;
    private User user;
    private EmergencyService emergencyService;
    private NotificationService notificationService;
    private EmergencyProcessor emergencyProcessor;
    private Runnable logoutAction;

    public UserDashboardController(
            UserDashboardView dashboardView,
            User user,
            EmergencyService emergencyService,
            NotificationService notificationService,
            EmergencyProcessor emergencyProcessor,
            Runnable logoutAction) {

        this.dashboardView = dashboardView;
        this.user = user;
        this.emergencyService = emergencyService;
        this.notificationService = notificationService;
        this.emergencyProcessor = emergencyProcessor;
        this.logoutAction = logoutAction;

        dashboardView.getWelcomeLabel().setText("Welcome back, " + user.getName() + "!");
        dashboardView.getSidebar().setUserName(user.getName());
        dashboardView.getSidebar().setUserRole("CITIZEN USER");

        // Wire Navigation Buttons
        dashboardView.getDashboardButton().setOnAction(e -> openDashboardHome());
        dashboardView.getReportEmergencyButton().setOnAction(e -> openReportEmergency());
        if (dashboardView.getQuickReportButton() != null) {
            dashboardView.getQuickReportButton().setOnAction(e -> openReportEmergency());
        }
        dashboardView.getMyRequestsButton().setOnAction(e -> openMyRequests());
        dashboardView.getTrackEmergencyButton().setOnAction(e -> openTrackEmergency());
        dashboardView.getHistoryButton().setOnAction(e -> openEmergencyHistory());
        dashboardView.getNotificationButton().setOnAction(e -> openNotifications());
        dashboardView.getLogoutButton().setOnAction(e -> logoutAction.run());

        refreshDashboardData();
    }

    public void openDashboardHome() {
        refreshDashboardData();
        dashboardView.showDashboardHome();
    }

    private void openReportEmergency() {
        ReportEmergencyView reportView = new ReportEmergencyView();
        new ReportEmergencyController(
                reportView, emergencyService, notificationService,
                emergencyProcessor, user, getStage(), this::openDashboardHome
        );
        dashboardView.setContent(reportView.getView());
        dashboardView.getSidebar().setActiveButton(dashboardView.getReportEmergencyButton());
    }

    private void openMyRequests() {
        MyRequestsView myRequestsView = new MyRequestsView();
        ArrayList<Emergency> requests = emergencyService.getEmergenciesByUserId(user.getUserId());
        myRequestsView.setRequests(requests);
        myRequestsView.getRefreshButton().setOnAction(e -> {
            myRequestsView.setRequests(emergencyService.getEmergenciesByUserId(user.getUserId()));
        });
        myRequestsView.getBackButton().setOnAction(e -> openDashboardHome());
        dashboardView.setContent(myRequestsView.getView());
        dashboardView.getSidebar().setActiveButton(dashboardView.getMyRequestsButton());
    }

    private void openTrackEmergency() {
        TrackEmergencyView trackView = new TrackEmergencyView();
        new TrackEmergencyController(
                trackView, emergencyService, user, getStage(), this::openDashboardHome
        );
        dashboardView.setContent(trackView.getView());
        dashboardView.getSidebar().setActiveButton(dashboardView.getTrackEmergencyButton());
    }

    private void openEmergencyHistory() {
        EmergencyHistoryView historyView = new EmergencyHistoryView();
        new EmergencyHistoryController(
                historyView, emergencyService, user, this::openDashboardHome
        );
        dashboardView.setContent(historyView.getView());
        dashboardView.getSidebar().setActiveButton(dashboardView.getHistoryButton());
    }

    private void openNotifications() {
        NotificationView notifView = new NotificationView();
        new NotificationController(
                notifView, notificationService, user, this::openDashboardHome
        );
        dashboardView.setContent(notifView.getView());
        dashboardView.getSidebar().setActiveButton(dashboardView.getNotificationButton());
    }

    public void refreshDashboardData() {
        ArrayList<Emergency> userEmergencies = emergencyService.getEmergenciesByUserId(user.getUserId());

        int total = userEmergencies.size();
        int pending = 0;
        int resolved = 0;

        for (Emergency em : userEmergencies) {
            if (em.getStatus() == EmergencyStatus.REQUESTED
                    || em.getStatus() == EmergencyStatus.ACCEPTED
                    || em.getStatus() == EmergencyStatus.ASSIGNED
                    || em.getStatus() == EmergencyStatus.IN_PROGRESS) {
                pending++;
            } else if (em.getStatus() == EmergencyStatus.RESOLVED) {
                resolved++;
            }
        }

        int unread = notificationService != null ? notificationService.getUnreadNotifications(user.getUserId()).size() : 0;
        dashboardView.updateSummary(total, pending, resolved, unread);

        if (dashboardView.getRecentActivityTable() != null) {
            dashboardView.getRecentActivityTable().setItems(FXCollections.observableArrayList(userEmergencies));
        }
    }

    private Stage getStage() {
        if (dashboardView.getView().getScene() != null && dashboardView.getView().getScene().getWindow() != null) {
            return (Stage) dashboardView.getView().getScene().getWindow();
        }
        return null;
    }
}
