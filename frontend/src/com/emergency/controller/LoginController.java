package com.emergency.controller;

import com.emergency.enums.UserRole;
import com.emergency.model.User;
import com.emergency.service.*;
import com.emergency.threading.EmergencyProcessor;
import com.emergency.views.*;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginController {

    private LoginView loginView;
    private AuthenticationService authenticationService;
    private UserService userService;
    private Stage stage;
    private EmergencyService emergencyService;
    private ResourceService resourceService;
    private AllocationService allocationService;
    private NotificationService notificationService;
    private DashboardService dashboardService;
    private EmergencyProcessor emergencyProcessor;

    public LoginController(
            LoginView loginView, AuthenticationService authenticationService, UserService userService,
            Stage stage, EmergencyService emergencyService, ResourceService resourceService,
            AllocationService allocationService, NotificationService notificationService,
            DashboardService dashboardService,
            EmergencyProcessor emergencyProcessor) {

        this.loginView = loginView;
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.stage = stage;
        this.emergencyService = emergencyService;
        this.resourceService = resourceService;
        this.allocationService = allocationService;
        this.notificationService = notificationService;
        this.dashboardService = dashboardService;
        this.emergencyProcessor = emergencyProcessor;

        loginView.getLoginButton().setOnAction(e -> loginUser());
        loginView.getRegisterButton().setOnAction(e -> openRegisterPage());
    }

    private void loginUser() {
        String identifier = loginView.getEmailField().getText();
        if (identifier != null) identifier = identifier.trim();
        String password = loginView.getEnteredPassword();
        String roleStr = loginView.getRoleBox().getValue();

        UserRole selectedRole = (roleStr != null && roleStr.equalsIgnoreCase("Admin")) ? UserRole.ADMIN : UserRole.USER;

        String validationError = authenticationService.validateLogin(identifier, password, selectedRole);
        if (validationError != null) {
            loginView.getMessageLabel().setText(validationError);
            loginView.getMessageLabel().setStyle("-fx-text-fill: #DC2626;");
            return;
        }

        User user = authenticationService.getUserByIdentifier(identifier);
        if (user == null) {
            loginView.getMessageLabel().setText("Account could not be retrieved.");
            return;
        }

        if (user.getRole() == UserRole.ADMIN) {
            openAdminDashboard(user);
        } else {
            openUserDashboard(user);
        }
    }

    private void openUserDashboard(User user) {
        UserDashboardView view = new UserDashboardView();
        new UserDashboardController(view, user, emergencyService, notificationService,
                emergencyProcessor, this::openLoginPage);

        stage.setTitle("User Dashboard - Smart Emergency System");
        Scene scene = new Scene(view.getView(), 1080, 700);
        applyStylesheet(scene);
        stage.setScene(scene);
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.setResizable(true);
    }

    private void openAdminDashboard(User admin) {
        AdminDashboardView view = new AdminDashboardView();
        new AdminDashboardController(
                view, admin, emergencyService, resourceService, allocationService,
                dashboardService, notificationService, emergencyProcessor,
                this::openLoginPage
        );

        stage.setTitle("Admin Command Center - Smart Emergency System");
        Scene scene = new Scene(view.getView(), 1140, 720);
        applyStylesheet(scene);
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setResizable(true);
    }

    private void openRegisterPage() {
        RegisterView regView = new RegisterView();
        new RegisterController(regView, userService, this::openLoginPage);
        Scene regScene = new Scene(regView.getView(), 960, 680);
        applyStylesheet(regScene);
        stage.setTitle("Citizen Registration - Smart Emergency System");
        stage.setScene(regScene);
    }

    private void openLoginPage() {
        LoginView newView = new LoginView();
        new LoginController(
                newView, authenticationService, userService, stage, emergencyService,
                resourceService, allocationService, notificationService,
                dashboardService, emergencyProcessor
        );
        Scene scene = new Scene(newView.getView(), 960, 640);
        applyStylesheet(scene);
        stage.setTitle("Smart Emergency Response System");
        stage.setScene(scene);
    }

    private void applyStylesheet(Scene scene) {
        if (scene != null) {
            try {
                String css = getClass().getResource("/com/emergency/css/style.css").toExternalForm();
                if (!scene.getStylesheets().contains(css)) scene.getStylesheets().add(css);
            } catch (Exception ignored) {}
        }
    }
}
