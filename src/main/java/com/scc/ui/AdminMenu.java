package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.User;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final Scanner sc;
    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final User currentAdmin;

    // Allowed values per spec
    private static final List<String> VALID_ROLES = List.of("ADMIN", "STAFF", "BORROWER");
    private static final List<String> VALID_STATUSES = List.of("APPROVED", "PENDING");

    // Audit table format (per spec)
    private static final String LOG_FORMAT = "%-5s | %-8s | %-18s | %-35s | %-20s%n";
    private static final int DESC_WIDTH = 35;

    public AdminMenu(Scanner scanner, UserDao userDao, ActivityLogDao logDao, User currentAdmin) {
        this.sc = scanner;
        this.userDao = userDao;
        this.logDao = logDao;
        this.currentAdmin = currentAdmin;
    }

    public void show() {
        while (true) {
            System.out.println("=".repeat(50));
            System.out.println("           ADMIN DASHBOARD");
            System.out.println("=".repeat(50));
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role / Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. View System Audit Logs");
            System.out.println("8. Logout");
            System.out.print("Choose an option (1-8): ");

            switch (sc.nextLine().trim()) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "4" -> addNewUser();
                case "5" -> updateUserRoleStatus();
                case "6" -> deleteUser();
                case "7" -> auditLogMenu();
                case "8" -> {
                    System.out.println("Logged out.\n");
                    return;
                }
                default -> System.out.println("⚠ Invalid option.\n");
            }
        }
    }

    // === VALIDATION HELPERS ===
    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if ("0".equals(input)) return 0; // Cancel at any time
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("⚠ Invalid input — please enter a number or 0 to cancel.");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.print("Enter new role (admin/staff/borrower) or 0 to cancel: ");
            String input = sc.nextLine().trim();
            if ("0".equals(input)) return null;
            String upper = input.toUpperCase();
            if (VALID_ROLES.contains(upper)) return upper;
            System.out.println("⚠ Invalid role! Valid options: admin, staff, borrower. Try again.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new status (APPROVED/PENDING) or 0 to cancel: ");
            String input = sc.nextLine().trim();
            if ("0".equals(input)) return null;
            String upper = input.toUpperCase();
            if (VALID_STATUSES.contains(upper)) return upper;
            System.out.println("⚠ Invalid status! Valid options: APPROVED, PENDING. Try again.");
        }
    }

    private void viewAllUsers() {
        List<User> users = userDao.getAllUsers();
        System.out.println("\n--- All Users ---");
        if (users.isEmpty()) {
            System.out.println("No users found.\n");
            return;
        }
        System.out.printf("%-4s | %-15s | %-10s | %-10s%n", "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println("-".repeat(45));
        for (User u : users) {
            System.out.printf("%-4d | %-15s | %-10s | %-10s%n",
                    u.id(), u.username(), u.roles(), u.status());
        }
        System.out.println();
    }

    private void viewPendingUsers() {
        List<User> pending = userDao.getPendingUsers();
        System.out.println("\n--- Pending Approvals ---");
        if (pending.isEmpty()) {
            System.out.println("No pending accounts.\n");
            return;
        }
        for (User u : pending) {
            System.out.printf("ID: %d | User: %s | Role: %s%n",
                    u.id(), u.username(), u.roles());
        }
        System.out.println();
    }

    // Deliverable 5 → USER_APPROVED
    private void approveUser() {
        viewPendingUsers();
        int id = readIntInput("Enter User ID to approve (0 = Cancel): ");
        if (id == 0) return;
        if (userDao.updateStatus(id, "APPROVED")) {
            logDao.logAction(currentAdmin.id(), "USER_APPROVED",
                    "Admin [" + currentAdmin.id() + "] approved account for User ID " + id);
            System.out.println("✅ User approved!\n");
        } else {
            System.out.println("❌ User not found.\n");
        }
    }

    // → USER_CREATED
    private void addNewUser() {
        System.out.println("\n--- Add New User ---");
        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();
        String role = readValidRole();
        if (role == null) {
            System.out.println("Cancelled.\n");
            return;
        }
        if (userDao.register(username, password, role)) {
            List<User> all = userDao.getAllUsers();
            int newId = all.get(all.size() - 1).id();
            userDao.updateStatus(newId, "APPROVED");
            logDao.logAction(currentAdmin.id(), "USER_CREATED",
                    "Admin [" + currentAdmin.id() + "] created " + role.toLowerCase()
                            + " account '" + username + "' (User ID " + newId + ")");
            System.out.println("✅ User added & approved.\n");
        }
    }

    // Deliverable 7 → USER_UPDATED
    private void updateUserRoleStatus() {
        viewAllUsers();

        int targetId = readIntInput("Enter User ID to update (0 = Cancel): ");
        if (targetId == 0) {
            System.out.println("Update cancelled.\n");
            return;
        }

        // SELF-EDIT BLOCK
        if (targetId == currentAdmin.id()) {
            System.out.println("WARNING: Modifying the currently active admin account is restricted.\n");
            return;
        }

        String newRole = readValidRole();
        if (newRole == null) {
            System.out.println("Update cancelled.\n");
            return;
        }

        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println("Update cancelled.\n");
            return;
        }

        boolean success = userDao.updateUserRoleAndStatus(targetId, newRole, newStatus);
        if (success) {
            logDao.logAction(currentAdmin.id(), "USER_UPDATED",
                    "Admin [" + currentAdmin.id() + "] changed role of User " + targetId
                            + " to " + newRole.toLowerCase() + " (status: " + newStatus + ")");
            System.out.println("✅ User updated successfully!\n");
        } else {
            System.out.println("❌ Update failed — user not found.\n");
        }
    }

    // → USER_DELETED
    private void deleteUser() {
        viewAllUsers();
        int id = readIntInput("Enter User ID to delete (0 = Cancel): ");
        if (id == 0) return;

        if (id == currentAdmin.id()) {
            System.out.println("⚠ Cannot delete your own active admin account.\n");
            return;
        }

        System.out.print("Confirm delete? (yes/no): ");
        if ("yes".equalsIgnoreCase(sc.nextLine().trim())) {
            if (userDao.deleteUser(id)) {
                logDao.logAction(currentAdmin.id(), "USER_DELETED",
                        "Admin [" + currentAdmin.id() + "] deleted account of User ID " + id);
                System.out.println("✅ User deleted.\n");
            } else {
                System.out.println("❌ Failed.\n");
            }
        }
    }

    // ===================== AUDIT LOG INTERFACE =====================

    private void auditLogMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("           SYSTEM AUDIT LOGS");
            System.out.println("=".repeat(50));
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear All Logs");
            System.out.println("4. Back");
            System.out.print("Choose an option (1-4): ");

            switch (sc.nextLine().trim()) {
                case "1" -> printLogs(logDao.getAllLogs());
                case "2" -> filterLogsByUser();
                case "3" -> clearLogs();
                case "4" -> {
                    System.out.println();
                    return;
                }
                default -> System.out.println("⚠ Invalid option.");
            }
        }
    }

    private void filterLogsByUser() {
        int userId = readIntInput("Enter User ID to filter (0 = Cancel): ");
        if (userId == 0) return;
        printLogs(logDao.getLogsByUserId(userId));
    }

    // Safe clear: explicit confirmation, and the clearing itself is logged afterwards
    private void clearLogs() {
        System.out.print("⚠ This permanently deletes ALL audit logs. Type YES to confirm: ");
        if (!"YES".equals(sc.nextLine().trim())) {
            System.out.println("Cancelled. Logs were not deleted.");
            return;
        }
        if (logDao.clearLogs()) {
            logDao.logAction(currentAdmin.id(), "LOGS_CLEARED",
                    "Admin [" + currentAdmin.id() + "] cleared all audit logs");
            System.out.println("✅ Logs cleared.");
        } else {
            System.out.println("❌ Failed to clear logs.");
        }
    }

    private void printLogs(List<ActivityLog> logs) {
        System.out.println();
        if (logs.isEmpty()) {
            System.out.println("No logs found.");
            return;
        }
        System.out.printf(LOG_FORMAT, "ID", "USER ID", "ACTION", "DESCRIPTION", "TIMESTAMP");
        System.out.println("-".repeat(5 + 8 + 18 + DESC_WIDTH + 20 + 12));
        for (ActivityLog log : logs) {
            List<String> lines = wrap(log.description(), DESC_WIDTH);
            System.out.printf(LOG_FORMAT,
                    log.id(), log.userId(), log.actionType(), lines.get(0), log.timestamp());
            // Long descriptions continue on extra lines so the table stays aligned
            for (int i = 1; i < lines.size(); i++) {
                System.out.printf(LOG_FORMAT, "", "", "", lines.get(i), "");
            }
        }
        System.out.println("\nTotal: " + logs.size() + " log(s)");
    }

    // Word-wraps text into chunks of at most `width` characters
    private List<String> wrap(String text, int width) {
        List<String> lines = new java.util.ArrayList<>();
        if (text == null || text.isBlank()) {
            lines.add("");
            return lines;
        }
        StringBuilder line = new StringBuilder();
        for (String word : text.split("\\s+")) {
            while (word.length() > width) { // hard-split very long words
                if (line.length() > 0) { lines.add(line.toString()); line.setLength(0); }
                lines.add(word.substring(0, width));
                word = word.substring(width);
            }
            if (line.length() + word.length() + (line.length() > 0 ? 1 : 0) > width) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) line.append(' ');
            line.append(word);
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }
}