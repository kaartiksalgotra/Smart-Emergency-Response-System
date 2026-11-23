package com.emergency.controller;

import com.emergency.enums.EmergencyStatus;
import com.emergency.model.*;
import com.emergency.service.*;
import com.emergency.threading.EmergencyProcessor;
import com.emergency.views.*;
import javafx.collections.FXCollections;

/**
 * Controller for Admin Command Center.
 * Handles fixed sidebar navigation, incoming request review, queue, allocation & monitoring.
 */
public class AdminDashboardController {

    private AdminDashboardView dashboardView;
    private EmergencyService emergencyService;
    private ResourceService resourceService;
    private AllocationService allocationService;
    private ReportService reportService;
    private DashboardService dashboardService;
    private NotificationService notificationService;
    private EmergencyProcessor emergencyProcessor;

    public AdminDashboardController(
            AdminDashboardView dashboardView, User admin, EmergencyService emergencyService,
            ResourceService resourceService, AllocationService allocationService,
            ReportService reportService, DashboardService dashboardService,
            NotificationService notificationService, EmergencyProcessor emergencyProcessor,
            Runnable logoutAction) {

        this.dashboardView = dashboardView;
        this.emergencyService = emergencyService;
        this.resourceService = resourceService;
        this.allocationService = allocationService;
        this.reportService = reportService;
        this.dashboardService = dashboardService;
        this.notificationService = notificationService;
        this.emergencyProcessor = emergencyProcessor;

        dashboardView.getSidebar().setUserName(admin.getName());
        dashboardView.getSidebar().setUserRole("SYSTEM ADMINISTRATOR");

        dashboardView.getDashboardButton().setOnAction(e -> openDashboardHome());
        dashboardView.getIncomingRequestsButton().setOnAction(e -> openIncomingRequests());
        dashboardView.getPriorityQueueButton().setOnAction(e -> openPriorityQueue());
        dashboardView.getManageResourcesButton().setOnAction(e -> openManageResources());
        dashboardView.getAllocationButton().setOnAction(e -> openAllocation());
        dashboardView.getMonitorEmergencyButton().setOnAction(e -> openMonitoring());
        dashboardView.getReportsButton().setOnAction(e -> openReports());
        dashboardView.getRefreshButton().setOnAction(e -> refreshDashboard());
        dashboardView.getLogoutButton().setOnAction(e -> logoutAction.run());

        refreshDashboard();
    }

    public void openDashboardHome() {
        refreshDashboard();
        dashboardView.showDashboardHome();
    }

    private void openIncomingRequests() {
        IncomingRequestsView view = new IncomingRequestsView();
        view.setRequests(emergencyService.getRequestedEmergencies());
        view.getRefreshButton().setOnAction(e -> view.setRequests(emergencyService.getRequestedEmergencies()));

        view.getAcceptButton().setOnAction(e -> {
            Emergency em = view.getSelectedEmergency();
            if (em != null && emergencyService.acceptEmergency(em.getEmergencyId())) {
                if (notificationService != null) notificationService.notifyEmergencyAccepted(em);
                view.getMessageLabel().setText("Request #" + em.getEmergencyId() + " ACCEPTED!");
                view.setRequests(emergencyService.getRequestedEmergencies());
                refreshDashboard();
            }
        });

        view.getRejectButton().setOnAction(e -> {
            Emergency em = view.getSelectedEmergency();
            if (em != null && emergencyService.rejectEmergency(em.getEmergencyId())) {
                if (notificationService != null) notificationService.notifyEmergencyRejected(em);
                view.getMessageLabel().setText("Request #" + em.getEmergencyId() + " REJECTED.");
                view.setRequests(emergencyService.getRequestedEmergencies());
                refreshDashboard();
            }
        });

        dashboardView.setContent(view.getView());
        dashboardView.getSidebar().setActiveButton(dashboardView.getIncomingRequestsButton());
    }

    private void openPriorityQueue() {
        PriorityQueueView queueView = new PriorityQueueView();
        Runnable load = () -> queueView.setQueueList(emergencyProcessor != null && emergencyProcessor.getEmergencyQueue() != null
                ? emergencyProcessor.getEmergencyQueue().getQueueAsSortedList() : emergencyService.getRequestedEmergencies());
        load.run();
        queueView.getRefreshButton().setOnAction(e -> load.run());
        dashboardView.setContent(queueView.getView());
        dashboardView.getSidebar().setActiveButton(dashboardView.getPriorityQueueButton());
    }

    private void openAllocation() {
        ResourceAllocationView allocView = new ResourceAllocationView();
        Runnable refresh = () -> {
            allocView.setAcceptedEmergencies(emergencyService.getAcceptedEmergencies());
            allocView.setAvailableResources(resourceService.getAvailableResources());
            allocView.setAllocations(allocationService.getActiveAllocations());
        };
        refresh.run();

        allocView.getAutoAssignButton().setOnAction(e -> {
            Emergency em = allocView.getEmergencyBox().getValue();
            if (em == null) { allocView.getMessageLabel().setText("Select an accepted emergency first."); return; }
            if (allocationService.allocateResource(em.getEmergencyId())) {
                if (notificationService != null) notificationService.notifyResourceAssigned(em);
                allocView.getMessageLabel().setText("Unit auto-allocated for Incident #" + em.getEmergencyId());
                refresh.run(); refreshDashboard();
            } else allocView.getMessageLabel().setText("No matching available resource found.");
        });

        allocView.getManualAssignButton().setOnAction(e -> {
            Emergency em = allocView.getEmergencyBox().getValue();
            Resource res = allocView.getResourceBox().getValue();
            if (em == null || res == null) { allocView.getMessageLabel().setText("Select both emergency & resource."); return; }
            if (allocationService.manualAllocation(em.getEmergencyId(), res.getResourceId())) {
                if (notificationService != null) notificationService.notifyResourceAssigned(em);
                allocView.getMessageLabel().setText("Unit #" + res.getResourceId() + " manually assigned!");
                refresh.run(); refreshDashboard();
            } else allocView.getMessageLabel().setText("Incompatible unit type or unit unavailable.");
        });

        allocView.getReleaseButton().setOnAction(e -> {
            Allocation sel = allocView.getAllocationTable().getSelectionModel().getSelectedItem();
            if (sel != null && allocationService.releaseResource(sel.getEmergencyId())) {
                allocView.getMessageLabel().setText("Resource released back to available.");
                refresh.run(); refreshDashboard();
            }
        });

        dashboardView.setContent(allocView.getView());
        dashboardView.getSidebar().setActiveButton(dashboardView.getAllocationButton());
    }

    private void openMonitoring() {
        EmergencyMonitoringView monView = new EmergencyMonitoringView();
        monView.setEmergencies(emergencyService.getAllEmergencies());
        monView.getRefreshButton().setOnAction(e -> monView.setEmergencies(emergencyService.getAllEmergencies()));

        monView.getUpdateStatusButton().setOnAction(e -> {
            Emergency em = monView.getSelectedEmergency();
            EmergencyStatus st = monView.getStatusBox().getValue();
            if (em != null && st != null && emergencyService.updateEmergencyStatus(em.getEmergencyId(), st)) {
                if (st == EmergencyStatus.RESOLVED || st == EmergencyStatus.CANCELLED) allocationService.releaseResource(em.getEmergencyId());
                if (notificationService != null) notificationService.notifyStatusChanged(em);
                monView.getMessageLabel().setText("Emergency #" + em.getEmergencyId() + " updated to " + st);
                monView.setEmergencies(emergencyService.getAllEmergencies());
                refreshDashboard();
            }
        });

        dashboardView.setContent(monView.getView());
        dashboardView.getSidebar().setActiveButton(dashboardView.getMonitorEmergencyButton());
    }

    private void openManageResources() {
        ResourceManagementView view = new ResourceManagementView();
        new ResourceManagementController(view, resourceService, this::openDashboardHome);
        dashboardView.setContent(view.getView());
        dashboardView.getSidebar().setActiveButton(dashboardView.getManageResourcesButton());
    }

    private void openReports() {
        ReportsView view = new ReportsView();
        new ReportsController(view, reportService, this::openDashboardHome);
        dashboardView.setContent(view.getView());
        dashboardView.getSidebar().setActiveButton(dashboardView.getReportsButton());
    }

    private void refreshDashboard() {
        dashboardView.getEmergencyTable().setItems(FXCollections.observableArrayList(emergencyService.getAllEmergencies()));
        dashboardView.updateStatistics(
                dashboardService.getTotalEmergencies(), dashboardService.getRequestedEmergencies(),
                dashboardService.getAcceptedEmergencies(), dashboardService.getAssignedEmergencies(),
                dashboardService.getResolvedEmergencies(), dashboardService.getCancelledEmergencies(),
                dashboardService.getTotalResources(), dashboardService.getAvailableResources(),
                dashboardService.getAllocatedResources()
        );
    }
}
