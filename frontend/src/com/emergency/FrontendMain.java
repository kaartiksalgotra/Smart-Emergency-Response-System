package com.emergency;

import com.emergency.controller.LoginController;
import com.emergency.controller.RegisterController;
import com.emergency.service.*;
import com.emergency.storage.*;
import com.emergency.threading.EmergencyQueue;
import com.emergency.threading.EmergencyProcessor;
import com.emergency.views.LoginView;
import com.emergency.views.RegisterView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FrontendMain extends Application {

    private UserStorage userStorage;
    private EmergencyStorage emergencyStorage;
    private ResourceStorage resourceStorage;
    private AllocationStorage allocationStorage;
    private NotificationStorage notificationStorage;

    private UserService userService;
    private AuthenticationService authenticationService;
    private EmergencyService emergencyService;
    private ResourceService resourceService;
    private AllocationService allocationService;
    private NotificationService notificationService;
    private DashboardService dashboardService;

    private EmergencyQueue emergencyQueue;
    private EmergencyProcessor emergencyProcessor;

    @Override
    public void start(Stage stage) {
        userStorage = new UserStorage(); // Auto-seeds ADMIN001 and USER001
        emergencyStorage = new EmergencyStorage();
        resourceStorage = new ResourceStorage();
        allocationStorage = new AllocationStorage();
        notificationStorage = new NotificationStorage();

        userService = new UserService(userStorage);
        authenticationService = new AuthenticationService(userStorage);
        emergencyService = new EmergencyService(emergencyStorage);
        resourceService = new ResourceService(resourceStorage);
        allocationService = new AllocationService(emergencyStorage, resourceStorage, allocationStorage);
        notificationService = new NotificationService(notificationStorage);
        dashboardService = new DashboardService(emergencyStorage, resourceStorage);

        emergencyQueue = new EmergencyQueue();
        emergencyQueue.loadPendingFromStorage(emergencyStorage);
        emergencyProcessor = new EmergencyProcessor(emergencyQueue, allocationService);
        emergencyProcessor.startProcessing();

        openLoginPage(stage);
    }

    public void openLoginPage(Stage stage) {
        LoginView loginView = new LoginView();
        new LoginController(
                loginView, authenticationService, userService, stage, emergencyService,
                resourceService, allocationService, notificationService,
                dashboardService, emergencyProcessor
        );

        Scene scene = new Scene(loginView.getView(), 960, 640);
        applyStylesheet(scene);
        stage.setTitle("Smart Emergency Response System");
        stage.setScene(scene);
        stage.setMinWidth(920);
        stage.setMinHeight(600);
        stage.setResizable(true);
        stage.show();
    }

    public void openRegisterPage(Stage stage) {
        RegisterView registerView = new RegisterView();
        new RegisterController(registerView, userService, () -> openLoginPage(stage));
        Scene scene = new Scene(registerView.getView(), 960, 680);
        applyStylesheet(scene);
        stage.setTitle("Citizen Registration - Smart Emergency System");
        stage.setScene(scene);
    }

    public static void applyStylesheet(Scene scene) {
        if (scene != null) {
            try {
                String css = FrontendMain.class.getResource("/com/emergency/css/style.css").toExternalForm();
                if (!scene.getStylesheets().contains(css)) scene.getStylesheets().add(css);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void stop() {
        if (emergencyProcessor != null) emergencyProcessor.stopProcessing();
    }

    public static void main(String[] args) {
        Application.launch(FrontendMain.class, args);
    }
}
