package com.emergency;

import com.emergency.console.ConsoleApp;
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
import com.emergency.service.DashboardService;

import com.emergency.threading.EmergencyQueue;
import com.emergency.threading.EmergencyProcessor;

public class Main {

    public static void main(String[] args) {

        // 1. Creating storage objects (with resilient in-memory fallback)
        UserStorage userStorage = new UserStorage();
        EmergencyStorage emergencyStorage = new EmergencyStorage();
        ResourceStorage resourceStorage = new ResourceStorage();
        AllocationStorage allocationStorage = new AllocationStorage();
        NotificationStorage notificationStorage = new NotificationStorage();

        // 2. Creating service objects
        UserService userService = new UserService(userStorage);
        AuthenticationService authenticationService = new AuthenticationService(userStorage);
        EmergencyService emergencyService = new EmergencyService(emergencyStorage);
        ResourceService resourceService = new ResourceService(resourceStorage);
        AllocationService allocationService = new AllocationService(
                emergencyStorage,
                resourceStorage,
                allocationStorage
        );
        NotificationService notificationService = new NotificationService(notificationStorage);
        DashboardService dashboardService = new DashboardService(
                emergencyStorage,
                resourceStorage
        );

        // 3. Creating multithreading queue & background worker
        EmergencyQueue emergencyQueue = new EmergencyQueue();
        emergencyQueue.loadPendingFromStorage(emergencyStorage);

        EmergencyProcessor emergencyProcessor = new EmergencyProcessor(
                emergencyQueue,
                allocationService
        );
        emergencyProcessor.startProcessing();

        // 4. Starting Console Application
        ConsoleApp consoleApp = new ConsoleApp(
                userService,
                authenticationService,
                emergencyService,
                resourceService,
                allocationService,
                notificationService,
                dashboardService,
                emergencyStorage,
                resourceStorage,
                allocationStorage,
                notificationStorage,
                emergencyQueue,
                emergencyProcessor
        );

        consoleApp.start();
    }
}
