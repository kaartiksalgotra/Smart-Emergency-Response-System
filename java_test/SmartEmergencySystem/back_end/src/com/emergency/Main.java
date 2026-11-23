
package com.emergency;

import com.emergency.storage.UserStorage;
import com.emergency.storage.EmergencyStorage;
import com.emergency.storage.ResourceStorage;
import com.emergency.storage.AllocationStorage;
import com.emergency.storage.NotificationStorage;

import com.emergency.service.UserService;
import com.emergency.service.AuthenticationService;
import com.emergency.service.EmergencyService;
import com.emergency.service.ResourceService;
import com.emergency.service.AllocationService;
import com.emergency.service.NotificationService;
import com.emergency.service.ReportService;
import com.emergency.service.DashboardService;

import com.emergency.threading.EmergencyQueue;
import com.emergency.threading.EmergencyProcessor;

import javafx.application.Application;

public class Main {

    public static void main(String[] args) {

        // Creating storage objects

        UserStorage userStorage = new UserStorage();

        EmergencyStorage emergencyStorage =
                new EmergencyStorage();

        ResourceStorage resourceStorage =
                new ResourceStorage();

        AllocationStorage allocationStorage =
                new AllocationStorage();

        NotificationStorage notificationStorage =
                new NotificationStorage();


        // Creating service objects

        UserService userService =
                new UserService(userStorage);

        AuthenticationService authenticationService =
                new AuthenticationService(userStorage);

        EmergencyService emergencyService =
                new EmergencyService(emergencyStorage);

        ResourceService resourceService =
                new ResourceService(resourceStorage);

        AllocationService allocationService =
                new AllocationService(
                        emergencyStorage,
                        resourceStorage,
                        allocationStorage
                );

        NotificationService notificationService =
                new NotificationService(notificationStorage);

        ReportService reportService =
                new ReportService(
                        emergencyStorage,
                        resourceStorage,
                        allocationStorage
                );

        DashboardService dashboardService =
                new DashboardService(
                        emergencyStorage,
                        resourceStorage
                );


        // Creating multithreading objects

        EmergencyQueue emergencyQueue =
                new EmergencyQueue();

        EmergencyProcessor emergencyProcessor =
                new EmergencyProcessor(
                        emergencyQueue,
                        allocationService
                );


        // Backend initialization

        System.out.println("==================================");
        System.out.println(" SMART EMERGENCY RESPONSE SYSTEM");
        System.out.println("==================================");

        System.out.println("Backend initialized successfully!");

        System.out.println("User Service: Ready");
        System.out.println("Authentication Service: Ready");
        System.out.println("Emergency Service: Ready");
        System.out.println("Resource Service: Ready");
        System.out.println("Allocation Service: Ready");
        System.out.println("Notification Service: Ready");
        System.out.println("Report Service: Ready");
        System.out.println("Dashboard Service: Ready");

        System.out.println("Emergency Queue: Ready");
        System.out.println("Emergency Processor: Ready");

        System.out.println("==================================");


        // Starting JavaFX Frontend

        Application.launch(FrontendMain.class, args);
    }
}
