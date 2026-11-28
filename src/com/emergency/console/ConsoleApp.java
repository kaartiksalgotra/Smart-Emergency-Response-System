package com.emergency.console;

import com.emergency.enums.EmergencyStatus;
import com.emergency.enums.EmergencyType;
import com.emergency.enums.PriorityLevel;
import com.emergency.enums.ResourceStatus;
import com.emergency.enums.ResourceType;
import com.emergency.enums.UserRole;
import com.emergency.model.Allocation;
import com.emergency.model.Ambulance;
import com.emergency.model.Emergency;
import com.emergency.model.EmergencyStatusHistory;
import com.emergency.model.FireTruck;
import com.emergency.model.Notification;
import com.emergency.model.RescueTeam;
import com.emergency.model.Resource;
import com.emergency.model.User;
import com.emergency.service.AllocationService;
import com.emergency.service.AuthenticationService;
import com.emergency.service.DashboardService;
import com.emergency.service.EmergencyService;
import com.emergency.service.NotificationService;
import com.emergency.service.ResourceService;
import com.emergency.service.UserService;
import com.emergency.storage.AllocationStorage;
import com.emergency.storage.EmergencyStorage;
import com.emergency.storage.NotificationStorage;
import com.emergency.storage.ResourceStorage;
import com.emergency.threading.EmergencyProcessor;
import com.emergency.threading.EmergencyQueue;
import com.emergency.util.DatabaseConnection;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Interactive Console Interface for the Smart Emergency Response and Resource Allocation System (ERAS).
 * Provides full feature parity with the previous graphical user interface in a smooth, terminal-friendly experience.
 */
public class ConsoleApp {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final EmergencyService emergencyService;
    private final ResourceService resourceService;
    private final AllocationService allocationService;
    private final NotificationService notificationService;
    private final DashboardService dashboardService;
    private final EmergencyStorage emergencyStorage;
    private final ResourceStorage resourceStorage;
    private final AllocationStorage allocationStorage;
    private final NotificationStorage notificationStorage;

    private final EmergencyQueue emergencyQueue;
    private final EmergencyProcessor emergencyProcessor;

    private final Scanner scanner;
    private User currentUser = null;
    private boolean isRunning = true;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ConsoleApp(
            UserService userService,
            AuthenticationService authenticationService,
            EmergencyService emergencyService,
            ResourceService resourceService,
            AllocationService allocationService,
            NotificationService notificationService,
            DashboardService dashboardService,
            EmergencyStorage emergencyStorage,
            ResourceStorage resourceStorage,
            AllocationStorage allocationStorage,
            NotificationStorage notificationStorage,
            EmergencyQueue emergencyQueue,
            EmergencyProcessor emergencyProcessor) {

        this.userService = userService;
        this.authenticationService = authenticationService;
        this.emergencyService = emergencyService;
        this.resourceService = resourceService;
        this.allocationService = allocationService;
        this.notificationService = notificationService;
        this.dashboardService = dashboardService;
        this.emergencyStorage = emergencyStorage;
        this.resourceStorage = resourceStorage;
        this.allocationStorage = allocationStorage;
        this.notificationStorage = notificationStorage;
        this.emergencyQueue = emergencyQueue;
        this.emergencyProcessor = emergencyProcessor;

        this.scanner = new Scanner(System.in);
    }

    public void start() {
        printBanner();
        while (isRunning) {
            if (currentUser == null) {
                showMainMenu();
            } else if (currentUser.getRole() == UserRole.ADMIN) {
                showAdminMenu();
            } else {
                showCitizenMenu();
            }
        }
        shutdown();
    }

    private void printBanner() {
        System.out.println();
        System.out.println("================================================================================");
        System.out.println("   SMART EMERGENCY RESPONSE AND RESOURCE ALLOCATION SYSTEM (ERAS)");
        System.out.println("================================================================================");
        boolean dbOk = DatabaseConnection.isAvailable();
        System.out.println(" * Storage Engine : " + (dbOk ? "[MySQL Database Active]" : "[Standalone Mode - Resilient In-Memory Engine Active]"));
        System.out.println(" * Core Services  : [Authentication, Dispatch, Queue, Fleet, Notifications Online]");
        System.out.println(" * Queue Worker   : [Background Multi-Threaded Processor Running]");
        System.out.println("================================================================================");
    }

    // ==========================================
    // 1. MAIN / AUTHENTICATION MENU
    // ==========================================

    private void showMainMenu() {
        System.out.println("\n----------------- [ MAIN MENU ] -----------------");
        System.out.println(" 1. Sign In (Login)");
        System.out.println(" 2. Citizen Registration (New Account)");
        System.out.println(" 3. System Credentials & Quick Info");
        System.out.println(" 4. Exit Application");
        System.out.println("-------------------------------------------------");
        System.out.print("Select an option (1-4): ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                handleLogin();
                break;
            case "2":
                handleRegister();
                break;
            case "3":
                showCredentialsHelp();
                break;
            case "4":
                System.out.println("\nExiting Emergency Response System. Stay safe!");
                isRunning = false;
                break;
            default:
                System.out.println("[!] Invalid choice. Please select from 1 to 4.");
        }
    }

    private void handleLogin() {
        System.out.println("\n--- [ SIGN IN ] ---");
        System.out.println("Select Login Role:");
        System.out.println(" 1. Citizen User");
        System.out.println(" 2. System Administrator");
        System.out.print("Role choice (1 or 2): ");
        String roleInput = scanner.nextLine().trim();

        if (!roleInput.equals("1") && !roleInput.equals("2")) {
            System.out.println("[!] Invalid role choice. Please enter 1 for Citizen or 2 for Administrator.");
            return;
        }

        UserRole selectedRole = roleInput.equals("2") ? UserRole.ADMIN : UserRole.USER;
        String roleLabel = selectedRole == UserRole.ADMIN ? "Administrator" : "Citizen User";
        System.out.println("Role selected: " + roleLabel);

        System.out.print("Enter User ID or Email: ");
        String identifier = scanner.nextLine().trim();
        if (identifier.isEmpty()) {
            System.out.println("[!] Identifier cannot be empty.");
            return;
        }

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        String validationError = authenticationService.validateLogin(identifier, password, selectedRole);
        if (validationError != null) {
            System.out.println("\n[ERROR] " + validationError);
            return;
        }

        User user = authenticationService.login(identifier, password, selectedRole);
        if (user != null) {
            currentUser = user;
            System.out.println("\n[SUCCESS] Authentication successful! Welcome, " + user.getName() + " (" + user.getUserCode() + ")");
        } else {
            System.out.println("\n[ERROR] Invalid credentials. Please try again.");
        }
    }

    private void handleRegister() {
        System.out.println("\n--- [ CITIZEN REGISTRATION ] ---");
        System.out.print("Enter Full Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter Phone Number (10 digits): ");
        String phone = scanner.nextLine().trim();

        System.out.print("Enter Password (min 6 chars): ");
        String password = scanner.nextLine();

        System.out.print("Confirm Password: ");
        String confirmPassword = scanner.nextLine();

        if (!password.equals(confirmPassword)) {
            System.out.println("\n[ERROR] Passwords do not match!");
            return;
        }

        System.out.println("Select Account Type:");
        System.out.println(" 1. Citizen User (Standard)");
        System.out.println(" 2. System Administrator");
        System.out.print("Account type (1 or 2) [default 1]: ");
        String roleChoice = scanner.nextLine().trim();
        UserRole role = roleChoice.equals("2") ? UserRole.ADMIN : UserRole.USER;

        try {
            User newUser = userService.registerUser(name, email, phone, password, role);
            System.out.println("\n[SUCCESS] Registration Complete!");
            System.out.println(" Your User ID is : " + newUser.getUserCode());
            System.out.println(" Please sign in using your User ID or Email.");
        } catch (IllegalArgumentException e) {
            System.out.println("\n[ERROR] Registration Failed: " + e.getMessage());
        }
    }

    private void showCredentialsHelp() {
        System.out.println("\n========================================================");
        System.out.println("            DEFAULT SYSTEM CREDENTIALS");
        System.out.println("========================================================");
        System.out.println(" 1. Administrator Account:");
        System.out.println("    - User ID  : ADMIN001  or  admin@emergency.com");
        System.out.println("    - Password : Admin123");
        System.out.println("    - Role     : System Administrator");
        System.out.println(" 2. Citizen Account:");
        System.out.println("    - User ID  : USER001   or  user@emergency.com");
        System.out.println("    - Password : User123");
        System.out.println("    - Role     : Citizen User");
        System.out.println("--------------------------------------------------------");
        System.out.println(" Tip: You can also register a new Citizen or Admin account.");
        System.out.println("========================================================");
    }

    // ==========================================
    // 2. CITIZEN USER MENU & FEATURES
    // ==========================================

    private void showCitizenMenu() {
        int unread = notificationService.getUnreadNotifications(currentUser.getUserId()).size();
        String notifBadge = unread > 0 ? " (" + unread + " unread)" : "";

        System.out.println("\n========================================================");
        System.out.println(" CITIZEN COMMAND CENTER | Logged in: " + currentUser.getName() + " [" + currentUser.getUserCode() + "]");
        System.out.println("========================================================");
        System.out.println(" 1. Dashboard Overview & Summary");
        System.out.println(" 2. Report New Emergency");
        System.out.println(" 3. View My Emergencies / Track Status");
        System.out.println(" 4. View Emergency Details & Timeline (by ID)");
        System.out.println(" 5. Notifications & Dispatches" + notifBadge);
        System.out.println(" 6. Mark Notifications as Read");
        System.out.println(" 7. View Profile Information");
        System.out.println(" 8. Sign Out (Logout)");
        System.out.println("--------------------------------------------------------");
        System.out.print("Select an option (1-8): ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                showCitizenDashboardSummary();
                break;
            case "2":
                handleReportEmergency();
                break;
            case "3":
                showCitizenEmergencies();
                break;
            case "4":
                showEmergencyDetailsPrompt();
                break;
            case "5":
                showCitizenNotifications();
                break;
            case "6":
                handleMarkNotificationsRead();
                break;
            case "7":
                showUserProfile();
                break;
            case "8":
                System.out.println("\nLogging out from Citizen Session...");
                currentUser = null;
                break;
            default:
                System.out.println("[!] Invalid choice. Please enter a number from 1 to 8.");
        }
    }

    private void showCitizenDashboardSummary() {
        ArrayList<Emergency> myEmergencies = emergencyService.getEmergenciesByUserId(currentUser.getUserId());
        int total = myEmergencies.size();
        int pending = 0;
        int inProgress = 0;
        int resolved = 0;
        int cancelled = 0;

        for (Emergency em : myEmergencies) {
            if (em.getStatus() == EmergencyStatus.REQUESTED || em.getStatus() == EmergencyStatus.ACCEPTED) {
                pending++;
            } else if (em.getStatus() == EmergencyStatus.ASSIGNED || em.getStatus() == EmergencyStatus.IN_PROGRESS) {
                inProgress++;
            } else if (em.getStatus() == EmergencyStatus.RESOLVED) {
                resolved++;
            } else if (em.getStatus() == EmergencyStatus.CANCELLED) {
                cancelled++;
            }
        }

        int unreadNotifs = notificationService.getUnreadNotifications(currentUser.getUserId()).size();

        System.out.println("\n+-------------------------------------------------------------+");
        System.out.println("|                 CITIZEN DASHBOARD SUMMARY                   |");
        System.out.println("+-------------------------------------------------------------+");
        System.out.printf("| Total Emergencies Reported   : %-28d |\n", total);
        System.out.printf("| Pending Admin Review         : %-28d |\n", pending);
        System.out.printf("| Dispatched / In-Progress     : %-28d |\n", inProgress);
        System.out.printf("| Successfully Resolved        : %-28d |\n", resolved);
        System.out.printf("| Cancelled / Closed           : %-28d |\n", cancelled);
        System.out.printf("| Unread Alerts & Notifications: %-28d |\n", unreadNotifs);
        System.out.println("+-------------------------------------------------------------+");

        if (!myEmergencies.isEmpty()) {
            System.out.println("\n--- Recent Incidents (Latest 3) ---");
            int limit = Math.min(3, myEmergencies.size());
            for (int i = 0; i < limit; i++) {
                Emergency em = myEmergencies.get(i);
                System.out.printf("  #%-3d | %-12s | %-8s | %-12s | %s\n",
                        em.getEmergencyId(), em.getEmergencyType(), em.getPriority(), em.getStatus(), em.getLocation());
            }
        }
    }

    private void handleReportEmergency() {
        System.out.println("\n========================================================");
        System.out.println("                 REPORT NEW EMERGENCY");
        System.out.println("========================================================");

        System.out.println("Select Emergency Type:");
        System.out.println(" 1. FIRE            (Building fire, vehicle fire, explosion)");
        System.out.println(" 2. MEDICAL         (Cardiac arrest, trauma, severe injury)");
        System.out.println(" 3. RESCUE          (Trapped individuals, flood, structural collapse)");
        System.out.println(" 4. OTHER           (Hazardous condition, general public distress)");
        System.out.print("Choice (1-4): ");
        String typeChoice = scanner.nextLine().trim();
        EmergencyType type;
        switch (typeChoice) {
            case "1": type = EmergencyType.FIRE; break;
            case "2": type = EmergencyType.MEDICAL; break;
            case "3": type = EmergencyType.RESCUE; break;
            case "4": type = EmergencyType.OTHER; break;
            default:
                System.out.println("[!] Invalid emergency type.");
                return;
        }

        System.out.println("\nSelect Priority Level:");
        System.out.println(" 1. LOW        (Minor non-life-threatening situation)");
        System.out.println(" 2. MEDIUM     (Moderate incident needing timely response)");
        System.out.println(" 3. HIGH       (Urgent situation with severe threat)");
        System.out.println(" 4. CRITICAL   (Immediate threat to life or major disaster)");
        System.out.print("Choice (1-4): ");
        String prioChoice = scanner.nextLine().trim();
        PriorityLevel priority;
        switch (prioChoice) {
            case "1": priority = PriorityLevel.LOW; break;
            case "2": priority = PriorityLevel.MEDIUM; break;
            case "3": priority = PriorityLevel.HIGH; break;
            case "4": priority = PriorityLevel.CRITICAL; break;
            default:
                System.out.println("[!] Invalid priority level.");
                return;
        }

        System.out.print("\nEnter Precise Incident Location: ");
        String location = scanner.nextLine().trim();
        if (location.isEmpty()) {
            System.out.println("[!] Location cannot be empty.");
            return;
        }

        System.out.print("Enter Incident Description: ");
        String description = scanner.nextLine().trim();
        if (description.isEmpty()) {
            System.out.println("[!] Description cannot be empty.");
            return;
        }

        System.out.print("Enter Number of Affected People: ");
        String affectedStr = scanner.nextLine().trim();
        int affected;
        try {
            affected = Integer.parseInt(affectedStr);
            if (affected < 1) {
                System.out.println("[!] Affected people must be at least 1.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("[!] Please enter a valid whole number for affected people.");
            return;
        }

        try {
            Emergency emergency = emergencyService.reportEmergency(
                    currentUser, type, priority, location, description, affected
            );

            notificationService.notifyEmergencyReported(emergency);
            emergencyProcessor.addEmergency(emergency);

            System.out.println("\n========================================================");
            System.out.println(" [SUCCESS] EMERGENCY REPORT SUBMITTED!");
            System.out.println(" Incident ID : #" + emergency.getEmergencyId());
            System.out.println(" Status      : " + emergency.getStatus() + " (Queued for Administrator Review)");
            System.out.println(" Priority    : " + emergency.getPriority());
            System.out.println(" Type        : " + emergency.getEmergencyType());
            System.out.println(" Dispatches and alerts will update in your dashboard.");
            System.out.println("========================================================");
        } catch (Exception e) {
            System.out.println("\n[ERROR] Failed to report emergency: " + e.getMessage());
        }
    }

    private void showCitizenEmergencies() {
        ArrayList<Emergency> list = emergencyService.getEmergenciesByUserId(currentUser.getUserId());
        System.out.println("\n==========================================================================================");
        System.out.println("                             MY REPORTED EMERGENCIES");
        System.out.println("==========================================================================================");
        if (list.isEmpty()) {
            System.out.println(" You have not reported any emergencies yet.");
            System.out.println("==========================================================================================");
            return;
        }

        System.out.printf("%-4s | %-16s | %-9s | %-12s | %-6s | %-16s | %-15s\n",
                "ID", "TYPE", "PRIORITY", "STATUS", "PEOPLE", "REPORTED AT", "ASSIGNED UNIT");
        System.out.println("-----+------------------+-----------+--------------+--------+------------------+----------------");

        for (Emergency em : list) {
            String assignedUnit = getAssignedResourceName(em.getEmergencyId());
            String createdAtStr = em.getCreatedAt() != null ? em.getCreatedAt().format(DATE_FMT) : "N/A";
            System.out.printf("#%-3d | %-16s | %-9s | %-12s | %-6d | %-16s | %-15s\n",
                    em.getEmergencyId(),
                    em.getEmergencyType(),
                    em.getPriority(),
                    em.getStatus(),
                    em.getAffectedPeople(),
                    createdAtStr,
                    assignedUnit);
        }
        System.out.println("==========================================================================================");
    }

    private void showEmergencyDetailsPrompt() {
        System.out.print("\nEnter Emergency ID to view full timeline & details: ");
        String input = scanner.nextLine().trim();
        try {
            int id = Integer.parseInt(input);
            Emergency em = emergencyService.getEmergencyById(id);
            if (em == null) {
                System.out.println("[!] Emergency #" + id + " not found.");
                return;
            }
            if (currentUser.getRole() != UserRole.ADMIN && em.getUserId() != currentUser.getUserId()) {
                System.out.println("[!] Access denied. You can only view your own emergencies.");
                return;
            }
            displayEmergencyDetails(em);
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid ID format.");
        }
    }

    private void displayEmergencyDetails(Emergency em) {
        System.out.println("\n========================================================");
        System.out.println("            INCIDENT REPORT #" + em.getEmergencyId());
        System.out.println("========================================================");
        System.out.println(" Incident ID     : #" + em.getEmergencyId());
        System.out.println(" Reporter User ID: " + em.getUserId());
        System.out.println(" Emergency Type  : " + em.getEmergencyType());
        System.out.println(" Priority Level  : " + em.getPriority());
        System.out.println(" Current Status  : " + em.getStatus());
        System.out.println(" Location        : " + em.getLocation());
        System.out.println(" Description     : " + em.getDescription());
        System.out.println(" Affected People : " + em.getAffectedPeople());
        System.out.println(" Reported At     : " + (em.getCreatedAt() != null ? em.getCreatedAt().format(DATE_FMT) : "N/A"));
        System.out.println(" Last Updated    : " + (em.getUpdatedAt() != null ? em.getUpdatedAt().format(DATE_FMT) : "N/A"));

        // Allocation details
        ArrayList<Allocation> allocs = allocationStorage.getAllocationsByEmergencyId(em.getEmergencyId());
        System.out.println("\n--- Dispatched Unit / Resource ---");
        if (allocs.isEmpty()) {
            System.out.println(" No unit currently dispatched.");
        } else {
            for (Allocation a : allocs) {
                Resource res = resourceService.getResourceById(a.getResourceId());
                String statusStr = a.isActive() ? "[DISPATCHED / ACTIVE]" : "[RELEASED / RETURNED]";
                if (res != null) {
                    System.out.printf("  * %s - %s (%s) | Contact: %s | Loc: %s\n",
                            statusStr, res.getResourceName(), res.getResourceType(), res.getContactNumber(), res.getLocation());
                } else {
                    System.out.printf("  * %s - Unit #%d\n", statusStr, a.getResourceId());
                }
            }
        }

        // Timeline / Status History
        ArrayList<EmergencyStatusHistory> history = emergencyStorage.getStatusHistoryByEmergencyId(em.getEmergencyId());
        System.out.println("\n--- Status History & Audit Log ---");
        if (history.isEmpty()) {
            System.out.println(" No status transitions recorded yet.");
        } else {
            for (EmergencyStatusHistory h : history) {
                System.out.printf("  [%s] Status changed: %s -> %s\n",
                        h.getChangedAt().format(DATE_FMT), h.getPreviousStatus(), h.getNewStatus());
            }
        }
        System.out.println("========================================================");
    }

    private void showCitizenNotifications() {
        ArrayList<Notification> notifs = notificationService.getUserNotifications(currentUser.getUserId());
        System.out.println("\n==========================================================================================");
        System.out.println("                               NOTIFICATIONS & DISPATCH ALERTS");
        System.out.println("==========================================================================================");
        if (notifs.isEmpty()) {
            System.out.println(" No notifications to display.");
            System.out.println("==========================================================================================");
            return;
        }

        for (Notification n : notifs) {
            String badge = n.isRead() ? "[READ]  " : "[UNREAD]";
            String dateStr = n.getCreatedAt() != null ? n.getCreatedAt().format(DATE_FMT) : "N/A";
            System.out.printf(" %s #%-3d (%s) [%s]\n    Message: %s\n\n",
                    badge, n.getNotificationId(), dateStr, n.getNotificationType(), n.getMessage());
        }
        System.out.println("==========================================================================================");
    }

    private void handleMarkNotificationsRead() {
        System.out.println("\n--- [ MARK NOTIFICATIONS AS READ ] ---");
        System.out.println(" 1. Mark a specific notification as read by ID");
        System.out.println(" 2. Mark ALL unread notifications as read");
        System.out.println(" 3. Cancel");
        System.out.print("Choice (1-3): ");
        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            System.out.print("Enter Notification ID: ");
            String idStr = scanner.nextLine().trim();
            try {
                int id = Integer.parseInt(idStr);
                boolean ok = notificationService.markAsRead(id, currentUser.getUserId());
                if (ok) {
                    System.out.println("[SUCCESS] Notification #" + id + " marked as read.");
                } else {
                    System.out.println("[!] Notification #" + id + " not found or already read.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid ID.");
            }
        } else if (choice.equals("2")) {
            ArrayList<Notification> unread = notificationService.getUnreadNotifications(currentUser.getUserId());
            int count = 0;
            for (Notification n : unread) {
                if (notificationService.markAsRead(n.getNotificationId(), currentUser.getUserId())) {
                    count++;
                }
            }
            System.out.println("[SUCCESS] Marked " + count + " notification(s) as read.");
        }
    }

    private void showUserProfile() {
        System.out.println("\n========================================================");
        System.out.println("                    MY PROFILE");
        System.out.println("========================================================");
        System.out.println(" User ID Code : " + currentUser.getUserCode());
        System.out.println(" Full Name    : " + currentUser.getName());
        System.out.println(" Email        : " + currentUser.getEmail());
        System.out.println(" Phone Number : " + currentUser.getPhone());
        System.out.println(" Role         : " + currentUser.getRole());
        System.out.println(" Registered At: " + (currentUser.getCreatedAt() != null ? currentUser.getCreatedAt().format(DATE_FMT) : "N/A"));
        System.out.println("========================================================");
    }

    // ==========================================
    // 3. ADMINISTRATOR MENU & COMMAND CENTER
    // ==========================================

    private void showAdminMenu() {
        int pendingReview = emergencyService.getRequestedEmergencies().size();
        String pendingBadge = pendingReview > 0 ? " (" + pendingReview + " PENDING!)" : "";

        System.out.println("\n========================================================");
        System.out.println(" ADMIN COMMAND CENTER | Administrator: " + currentUser.getName() + " [" + currentUser.getUserCode() + "]");
        System.out.println("========================================================");
        System.out.println(" 1. Command Center Live Metrics & Statistics");
        System.out.println(" 2. Review Incoming Emergency Requests" + pendingBadge);
        System.out.println(" 3. Resource Allocation Center (Auto/Manual Dispatch & Release)");
        System.out.println(" 4. Emergency Monitoring & Status Controller");
        System.out.println(" 5. Resource Fleet Management (View / Add / Edit / Remove)");
        System.out.println(" 6. View Priority Queue Status");
        System.out.println(" 7. Global Status History & Audit Log");
        System.out.println(" 8. Sign Out (Logout)");
        System.out.println("--------------------------------------------------------");
        System.out.print("Select an option (1-8): ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                showAdminDashboardOverview();
                break;
            case "2":
                handleReviewIncomingRequests();
                break;
            case "3":
                handleResourceAllocationCenter();
                break;
            case "4":
                handleEmergencyMonitoring();
                break;
            case "5":
                handleFleetManagement();
                break;
            case "6":
                showPriorityQueueStatus();
                break;
            case "7":
                showGlobalAuditLog();
                break;
            case "8":
                System.out.println("\nLogging out from Administrator Command Center...");
                currentUser = null;
                break;
            default:
                System.out.println("[!] Invalid choice. Please enter a number from 1 to 8.");
        }
    }

    private void showAdminDashboardOverview() {
        System.out.println("\n+-------------------------------------------------------------+");
        System.out.println("|             ADMIN COMMAND CENTER - LIVE METRICS             |");
        System.out.println("+-------------------------------------------------------------+");
        System.out.printf("| Total Emergencies Logged     : %-28d |\n", dashboardService.getTotalEmergencies());
        System.out.printf("| Pending Review (REQUESTED)   : %-28d |\n", dashboardService.getRequestedEmergencies());
        System.out.printf("| Accepted & Awaiting Units    : %-28d |\n", dashboardService.getAcceptedEmergencies());
        System.out.printf("| Units Dispatched (ASSIGNED)  : %-28d |\n", dashboardService.getAssignedEmergencies());
        System.out.printf("| In-Progress Operations       : %-28d |\n", dashboardService.getInProgressEmergencies());
        System.out.printf("| Successfully Resolved        : %-28d |\n", dashboardService.getResolvedEmergencies());
        System.out.printf("| Rejected Requests            : %-28d |\n", dashboardService.getRejectedEmergencies());
        System.out.printf("| Cancelled Incidents          : %-28d |\n", dashboardService.getCancelledEmergencies());
        System.out.println("+-------------------------------------------------------------+");
        System.out.printf("| Total Fleet Units            : %-28d |\n", dashboardService.getTotalResources());
        System.out.printf("| Units Available for Dispatch : %-28d |\n", dashboardService.getAvailableResources());
        System.out.printf("| Units Actively Allocated     : %-28d |\n", dashboardService.getAllocatedResources());
        System.out.println("+-------------------------------------------------------------+");
    }

    private void handleReviewIncomingRequests() {
        ArrayList<Emergency> requested = emergencyService.getRequestedEmergencies();
        System.out.println("\n==========================================================================================");
        System.out.println("                    INCOMING EMERGENCY REQUESTS AWAITING REVIEW");
        System.out.println("==========================================================================================");
        if (requested.isEmpty()) {
            System.out.println(" No pending emergency requests awaiting review. All clear!");
            System.out.println("==========================================================================================");
            return;
        }

        System.out.printf("%-4s | %-16s | %-9s | %-6s | %-22s | %-16s\n",
                "ID", "TYPE", "PRIORITY", "PEOPLE", "LOCATION", "REPORTED AT");
        System.out.println("-----+------------------+-----------+--------+------------------------+-----------------");

        for (Emergency em : requested) {
            String dateStr = em.getCreatedAt() != null ? em.getCreatedAt().format(DATE_FMT) : "N/A";
            System.out.printf("#%-3d | %-16s | %-9s | %-6d | %-22s | %-16s\n",
                    em.getEmergencyId(), em.getEmergencyType(), em.getPriority(),
                    em.getAffectedPeople(), truncate(em.getLocation(), 22), dateStr);
        }
        System.out.println("==========================================================================================");

        System.out.println("Actions:");
        System.out.println(" [A] Accept Request (Approves request for unit dispatch)");
        System.out.println(" [R] Reject Request (Rejects invalid/duplicate request)");
        System.out.println(" [B] Back to Admin Menu");
        System.out.print("Select action: ");
        String action = scanner.nextLine().trim().toUpperCase();

        if (action.equals("A")) {
            System.out.print("Enter Emergency ID to ACCEPT: ");
            String idStr = scanner.nextLine().trim();
            try {
                int id = Integer.parseInt(idStr);
                Emergency em = emergencyService.getEmergencyById(id);
                if (em != null && emergencyService.acceptEmergency(id)) {
                    notificationService.notifyEmergencyAccepted(em);
                    System.out.println("[SUCCESS] Incident #" + id + " has been ACCEPTED! It is now ready for resource allocation.");
                } else {
                    System.out.println("[ERROR] Could not accept emergency #" + id + ". Check status.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid ID.");
            }
        } else if (action.equals("R")) {
            System.out.print("Enter Emergency ID to REJECT: ");
            String idStr = scanner.nextLine().trim();
            try {
                int id = Integer.parseInt(idStr);
                Emergency em = emergencyService.getEmergencyById(id);
                if (em != null && emergencyService.rejectEmergency(id)) {
                    notificationService.notifyEmergencyRejected(em);
                    System.out.println("[SUCCESS] Incident #" + id + " has been REJECTED.");
                } else {
                    System.out.println("[ERROR] Could not reject emergency #" + id + ". Check status.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid ID.");
            }
        }
    }

    private void handleResourceAllocationCenter() {
        System.out.println("\n==========================================================================================");
        System.out.println("                             RESOURCE ALLOCATION CENTER");
        System.out.println("==========================================================================================");

        ArrayList<Emergency> accepted = emergencyService.getAcceptedEmergencies();
        ArrayList<Resource> available = resourceService.getAvailableResources();
        ArrayList<Allocation> activeAllocations = allocationService.getActiveAllocations();

        System.out.println("--- 1. Accepted Emergencies Awaiting Unit Dispatch (" + accepted.size() + ") ---");
        if (accepted.isEmpty()) {
            System.out.println(" None.");
        } else {
            for (Emergency em : accepted) {
                System.out.printf("  * Emergency #%-3d | %-16s | Priority: %-8s | Loc: %s\n",
                        em.getEmergencyId(), em.getEmergencyType(), em.getPriority(), em.getLocation());
            }
        }

        System.out.println("\n--- 2. Available Units for Dispatch (" + available.size() + ") ---");
        if (available.isEmpty()) {
            System.out.println(" No units currently available!");
        } else {
            for (Resource r : available) {
                System.out.printf("  * Unit #%-3d | %-24s | Type: %-12s | Base: %s\n",
                        r.getResourceId(), r.getResourceName(), r.getResourceType(), r.getLocation());
            }
        }

        System.out.println("\n--- 3. Active Dispatches (" + activeAllocations.size() + ") ---");
        if (activeAllocations.isEmpty()) {
            System.out.println(" No active dispatches.");
        } else {
            for (Allocation a : activeAllocations) {
                Resource r = resourceService.getResourceById(a.getResourceId());
                String rName = r != null ? r.getResourceName() : "Unit #" + a.getResourceId();
                System.out.printf("  * Allocation #%-3d | Emergency #%-3d -> %s (Dispatched: %s)\n",
                        a.getAllocationId(), a.getEmergencyId(), rName, a.getAssignedAt().format(DATE_FMT));
            }
        }
        System.out.println("==========================================================================================");

        System.out.println("Dispatch Options:");
        System.out.println(" 1. Auto-Allocate Resource (Automatically matches and dispatches appropriate unit)");
        System.out.println(" 2. Manual Resource Allocation (Select specific incident and unit)");
        System.out.println(" 3. Release Resource (Return unit to available pool)");
        System.out.println(" 4. Back to Admin Menu");
        System.out.print("Select choice (1-4): ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                if (accepted.isEmpty()) {
                    System.out.println("[!] No accepted emergencies awaiting allocation.");
                    return;
                }
                System.out.print("Enter Emergency ID to Auto-Allocate: ");
                try {
                    int emId = Integer.parseInt(scanner.nextLine().trim());
                    Emergency em = emergencyService.getEmergencyById(emId);
                    if (em == null || em.getStatus() != EmergencyStatus.ACCEPTED) {
                        System.out.println("[!] Emergency must be in ACCEPTED status.");
                        return;
                    }
                    if (allocationService.allocateResource(emId)) {
                        notificationService.notifyResourceAssigned(em);
                        String assignedUnit = getAssignedResourceName(emId);
                        System.out.println("\n[SUCCESS] Unit '" + assignedUnit + "' successfully AUTO-ALLOCATED to Emergency #" + emId + "!");
                        System.out.println(" Emergency status advanced to ASSIGNED.");
                    } else {
                        System.out.println("\n[ERROR] Could not auto-allocate: No compatible available units found for type " + em.getEmergencyType() + ".");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[!] Invalid Emergency ID.");
                }
                break;

            case "2":
                System.out.print("Enter Emergency ID: ");
                try {
                    int emId = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Enter Resource ID to assign: ");
                    int resId = Integer.parseInt(scanner.nextLine().trim());

                    Emergency em = emergencyService.getEmergencyById(emId);
                    if (allocationService.manualAllocation(emId, resId)) {
                        if (em != null) notificationService.notifyResourceAssigned(em);
                        System.out.println("\n[SUCCESS] Unit #" + resId + " manually assigned to Incident #" + emId + "!");
                    } else {
                        System.out.println("\n[ERROR] Manual allocation failed. Ensure emergency is ACCEPTED and unit type matches and is AVAILABLE.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[!] Invalid number format.");
                }
                break;

            case "3":
                System.out.print("Enter Emergency ID to RELEASE resource from: ");
                try {
                    int emId = Integer.parseInt(scanner.nextLine().trim());
                    if (allocationService.releaseResource(emId)) {
                        System.out.println("\n[SUCCESS] Unit released successfully! Marked as AVAILABLE.");
                    } else {
                        System.out.println("\n[ERROR] No active allocation found for Emergency #" + emId + ".");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[!] Invalid Emergency ID.");
                }
                break;

            case "4":
                break;
            default:
                System.out.println("[!] Invalid choice.");
        }
    }

    private void handleEmergencyMonitoring() {
        System.out.println("\n==========================================================================================");
        System.out.println("                         EMERGENCY MONITORING & STATUS CONTROL");
        System.out.println("==========================================================================================");
        System.out.println("Filter by status:");
        System.out.println(" 1. View ALL Emergencies");
        System.out.println(" 2. View ACTIVE Only (REQUESTED, ACCEPTED, ASSIGNED, IN_PROGRESS)");
        System.out.println(" 3. View RESOLVED / CLOSED");
        System.out.print("Choice (1-3) [default 1]: ");
        String filter = scanner.nextLine().trim();

        ArrayList<Emergency> all = emergencyService.getAllEmergencies();
        ArrayList<Emergency> displayed = new ArrayList<>();

        for (Emergency em : all) {
            if (filter.equals("2")) {
                if (em.getStatus() != EmergencyStatus.RESOLVED && em.getStatus() != EmergencyStatus.CANCELLED && em.getStatus() != EmergencyStatus.REJECTED) {
                    displayed.add(em);
                }
            } else if (filter.equals("3")) {
                if (em.getStatus() == EmergencyStatus.RESOLVED || em.getStatus() == EmergencyStatus.CANCELLED || em.getStatus() == EmergencyStatus.REJECTED) {
                    displayed.add(em);
                }
            } else {
                displayed.add(em);
            }
        }

        if (displayed.isEmpty()) {
            System.out.println(" No emergencies matching the filter.");
            return;
        }

        System.out.printf("%-4s | %-14s | %-8s | %-12s | %-20s | %-16s | %-14s\n",
                "ID", "TYPE", "PRIORITY", "STATUS", "LOCATION", "REPORTED AT", "ASSIGNED UNIT");
        System.out.println("-----+----------------+----------+--------------+----------------------+------------------+---------------");

        for (Emergency em : displayed) {
            String dateStr = em.getCreatedAt() != null ? em.getCreatedAt().format(DATE_FMT) : "N/A";
            String unit = getAssignedResourceName(em.getEmergencyId());
            System.out.printf("#%-3d | %-14s | %-8s | %-12s | %-20s | %-16s | %-14s\n",
                    em.getEmergencyId(), em.getEmergencyType(), em.getPriority(),
                    em.getStatus(), truncate(em.getLocation(), 20), dateStr, unit);
        }
        System.out.println("==========================================================================================");

        System.out.println("Options:");
        System.out.println(" 1. Update Incident Status");
        System.out.println(" 2. View Detailed Incident Timeline (by ID)");
        System.out.println(" 3. Back to Admin Menu");
        System.out.print("Select choice (1-3): ");
        String action = scanner.nextLine().trim();

        if (action.equals("1")) {
            System.out.print("Enter Emergency ID to update: ");
            try {
                int id = Integer.parseInt(scanner.nextLine().trim());
                Emergency em = emergencyService.getEmergencyById(id);
                if (em == null) {
                    System.out.println("[!] Emergency #" + id + " not found.");
                    return;
                }

                System.out.println("\nCurrent Status: " + em.getStatus());
                System.out.println("Select New Status:");
                System.out.println(" 1. REQUESTED");
                System.out.println(" 2. ACCEPTED");
                System.out.println(" 3. ASSIGNED");
                System.out.println(" 4. IN_PROGRESS");
                System.out.println(" 5. RESOLVED   (Will automatically release any assigned unit back to Available pool)");
                System.out.println(" 6. CANCELLED  (Will automatically release assigned unit)");
                System.out.print("New status (1-6): ");
                String stChoice = scanner.nextLine().trim();
                EmergencyStatus newStatus = null;
                switch (stChoice) {
                    case "1": newStatus = EmergencyStatus.REQUESTED; break;
                    case "2": newStatus = EmergencyStatus.ACCEPTED; break;
                    case "3": newStatus = EmergencyStatus.ASSIGNED; break;
                    case "4": newStatus = EmergencyStatus.IN_PROGRESS; break;
                    case "5": newStatus = EmergencyStatus.RESOLVED; break;
                    case "6": newStatus = EmergencyStatus.CANCELLED; break;
                    default:
                        System.out.println("[!] Invalid status option.");
                        return;
                }

                if (emergencyService.updateEmergencyStatus(id, newStatus)) {
                    if (newStatus == EmergencyStatus.RESOLVED || newStatus == EmergencyStatus.CANCELLED) {
                        allocationService.releaseResource(id);
                    }
                    notificationService.notifyStatusChanged(em);
                    System.out.println("\n[SUCCESS] Emergency #" + id + " status successfully updated to " + newStatus + "!");
                } else {
                    System.out.println("\n[ERROR] Failed to update status.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid ID.");
            }
        } else if (action.equals("2")) {
            showEmergencyDetailsPrompt();
        }
    }

    private void handleFleetManagement() {
        System.out.println("\n==========================================================================================");
        System.out.println("                              FLEET RESOURCE MANAGEMENT");
        System.out.println("==========================================================================================");

        ArrayList<Resource> all = resourceService.getAllResources();
        System.out.printf("%-4s | %-24s | %-12s | %-10s | %-12s | %-20s | %-10s\n",
                "ID", "RESOURCE NAME", "TYPE", "STATUS", "CONTACT", "BASE LOCATION", "CAPACITY");
        System.out.println("-----+--------------------------+--------------+------------+--------------+----------------------+-----------");

        for (Resource r : all) {
            String capacity = getCapacityString(r);
            System.out.printf("#%-3d | %-24s | %-12s | %-10s | %-12s | %-20s | %-10s\n",
                    r.getResourceId(), r.getResourceName(), r.getResourceType(),
                    r.getResourceStatus(), r.getContactNumber(), truncate(r.getLocation(), 20), capacity);
        }
        System.out.println("==========================================================================================");

        System.out.println("Fleet Actions:");
        System.out.println(" 1. Add New Response Unit");
        System.out.println(" 2. Update Existing Response Unit");
        System.out.println(" 3. Remove Response Unit");
        System.out.println(" 4. Back to Admin Menu");
        System.out.print("Select choice (1-4): ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                handleAddResource();
                break;
            case "2":
                handleUpdateResource();
                break;
            case "3":
                handleRemoveResource();
                break;
            case "4":
                break;
            default:
                System.out.println("[!] Invalid choice.");
        }
    }

    private void handleAddResource() {
        System.out.println("\n--- [ ADD RESPONSE UNIT ] ---");
        System.out.println("Select Unit Type:");
        System.out.println(" 1. AMBULANCE");
        System.out.println(" 2. FIRE_TRUCK");
        System.out.println(" 3. RESCUE_TEAM");
        System.out.print("Type (1-3): ");
        String typeChoice = scanner.nextLine().trim();
        ResourceType type;
        switch (typeChoice) {
            case "1": type = ResourceType.AMBULANCE; break;
            case "2": type = ResourceType.FIRE_TRUCK; break;
            case "3": type = ResourceType.RESCUE_TEAM; break;
            default:
                System.out.println("[!] Invalid type.");
                return;
        }

        System.out.print("Enter Unit Name (e.g. Rapid Ambulance 05): ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Contact Number: ");
        String contact = scanner.nextLine().trim();

        System.out.print("Enter Base Station / Location: ");
        String location = scanner.nextLine().trim();

        int capacity;
        if (type == ResourceType.AMBULANCE) {
            System.out.print("Enter Patient Capacity (beds/patients): ");
        } else if (type == ResourceType.FIRE_TRUCK) {
            System.out.print("Enter Water Capacity (liters): ");
        } else {
            System.out.print("Enter Number of Team Members: ");
        }
        try {
            capacity = Integer.parseInt(scanner.nextLine().trim());
            if (capacity <= 0) {
                System.out.println("[!] Capacity must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("[!] Please enter a valid whole number for capacity.");
            return;
        }

        try {
            Resource res;
            if (type == ResourceType.AMBULANCE) {
                res = new Ambulance(0, name, ResourceStatus.AVAILABLE, contact, location, capacity);
            } else if (type == ResourceType.FIRE_TRUCK) {
                res = new FireTruck(0, name, ResourceStatus.AVAILABLE, contact, location, capacity);
            } else {
                res = new RescueTeam(0, name, ResourceStatus.AVAILABLE, contact, location, capacity);
            }

            resourceService.addResource(res);
            System.out.println("\n[SUCCESS] Response Unit '" + name + "' added successfully with ID #" + res.getResourceId() + "!");
        } catch (Exception e) {
            System.out.println("\n[ERROR] Failed to add unit: " + e.getMessage());
        }
    }

    private void handleUpdateResource() {
        System.out.print("\nEnter Resource ID to update: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Resource res = resourceService.getResourceById(id);
            if (res == null) {
                System.out.println("[!] Resource #" + id + " not found.");
                return;
            }
            if (res.getResourceStatus() == ResourceStatus.ALLOCATED) {
                System.out.println("[!] Cannot modify an actively allocated response unit. Release it first.");
                return;
            }

            System.out.println("Updating Unit: " + res.getResourceName() + " (" + res.getResourceType() + ")");
            System.out.print("New Name [current: " + res.getResourceName() + "]: ");
            String name = scanner.nextLine().trim();
            if (!name.isEmpty()) res.setResourceName(name);

            System.out.print("New Contact [current: " + res.getContactNumber() + "]: ");
            String contact = scanner.nextLine().trim();
            if (!contact.isEmpty()) res.setContactNumber(contact);

            System.out.print("New Location [current: " + res.getLocation() + "]: ");
            String loc = scanner.nextLine().trim();
            if (!loc.isEmpty()) res.setLocation(loc);

            if (res instanceof Ambulance) {
                System.out.print("New Patient Capacity [current: " + ((Ambulance) res).getPatientCapacity() + "]: ");
                String capStr = scanner.nextLine().trim();
                if (!capStr.isEmpty()) ((Ambulance) res).setPatientCapacity(Integer.parseInt(capStr));
            } else if (res instanceof FireTruck) {
                System.out.print("New Water Capacity [current: " + ((FireTruck) res).getWaterCapacity() + "]: ");
                String capStr = scanner.nextLine().trim();
                if (!capStr.isEmpty()) ((FireTruck) res).setWaterCapacity(Integer.parseInt(capStr));
            } else if (res instanceof RescueTeam) {
                System.out.print("New Team Members [current: " + ((RescueTeam) res).getTeamMembers() + "]: ");
                String capStr = scanner.nextLine().trim();
                if (!capStr.isEmpty()) ((RescueTeam) res).setTeamMembers(Integer.parseInt(capStr));
            }

            if (resourceService.updateResource(res)) {
                System.out.println("\n[SUCCESS] Resource #" + id + " updated successfully!");
            } else {
                System.out.println("\n[ERROR] Failed to update resource.");
            }
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid number format.");
        }
    }

    private void handleRemoveResource() {
        System.out.print("\nEnter Resource ID to remove: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Resource res = resourceService.getResourceById(id);
            if (res == null) {
                System.out.println("[!] Resource #" + id + " not found.");
                return;
            }
            if (res.getResourceStatus() == ResourceStatus.ALLOCATED) {
                System.out.println("[!] Cannot remove an actively allocated unit. Release it first.");
                return;
            }

            System.out.print("Are you sure you want to permanently delete '" + res.getResourceName() + "'? (Y/N): ");
            String conf = scanner.nextLine().trim().toUpperCase();
            if (conf.equals("Y")) {
                if (resourceService.removeResource(id)) {
                    System.out.println("\n[SUCCESS] Resource #" + id + " deleted from fleet.");
                } else {
                    System.out.println("\n[ERROR] Failed to delete resource.");
                }
            } else {
                System.out.println("Operation cancelled.");
            }
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid ID.");
        }
    }

    private void showPriorityQueueStatus() {
        System.out.println("\n========================================================");
        System.out.println("           EMERGENCY PRIORITY QUEUE STATUS");
        System.out.println("========================================================");
        System.out.println(" Total Queue Size : " + emergencyQueue.getQueueSize());
        ArrayList<Emergency> sortedList = emergencyQueue.getQueueAsSortedList();

        if (sortedList.isEmpty()) {
            System.out.println(" The priority queue is currently empty.");
        } else {
            System.out.println("\nQueue Dispatch Priority Order (Highest Priority First):");
            int rank = 1;
            for (Emergency em : sortedList) {
                System.out.printf("  #%-2d | Incident #%-3d | Priority: %-8s | Type: %-14s | Loc: %s\n",
                        rank++, em.getEmergencyId(), em.getPriority(), em.getEmergencyType(), em.getLocation());
            }
        }
        System.out.println("========================================================");
    }

    private void showGlobalAuditLog() {
        ArrayList<EmergencyStatusHistory> history = emergencyStorage.getAllStatusHistory();
        System.out.println("\n==========================================================================================");
        System.out.println("                             GLOBAL INCIDENT AUDIT TRAIL");
        System.out.println("==========================================================================================");
        if (history.isEmpty()) {
            System.out.println(" No status change logs found.");
            System.out.println("==========================================================================================");
            return;
        }

        System.out.printf("%-4s | %-12s | %-16s | %-16s | %-16s\n",
                "LOG", "INCIDENT ID", "PREVIOUS STATUS", "NEW STATUS", "TIMESTAMP");
        System.out.println("-----+--------------+------------------+------------------+-----------------");

        for (EmergencyStatusHistory h : history) {
            System.out.printf("#%-3d | Incident #%-4d | %-16s | %-16s | %-16s\n",
                    h.getHistoryId(), h.getEmergencyId(), h.getPreviousStatus(), h.getNewStatus(),
                    h.getChangedAt().format(DATE_FMT));
        }
        System.out.println("==========================================================================================");
    }

    // ==========================================
    // HELPERS & LIFECYCLE
    // ==========================================

    private String getAssignedResourceName(int emergencyId) {
        ArrayList<Allocation> allocs = allocationStorage.getAllocationsByEmergencyId(emergencyId);
        for (Allocation a : allocs) {
            if (a.isActive()) {
                Resource r = resourceService.getResourceById(a.getResourceId());
                return r != null ? r.getResourceName() : "Unit #" + a.getResourceId();
            }
        }
        return "None";
    }

    private String getCapacityString(Resource r) {
        if (r instanceof Ambulance) {
            return ((Ambulance) r).getPatientCapacity() + " beds";
        } else if (r instanceof FireTruck) {
            return ((FireTruck) r).getWaterCapacity() + " L";
        } else if (r instanceof RescueTeam) {
            return ((RescueTeam) r).getTeamMembers() + " members";
        }
        return "N/A";
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }

    public void shutdown() {
        System.out.println("[*] Stopping emergency background processor...");
        if (emergencyProcessor != null) {
            emergencyProcessor.stopProcessing();
        }
        System.out.println("[*] Console application terminated gracefully.");
    }
}
