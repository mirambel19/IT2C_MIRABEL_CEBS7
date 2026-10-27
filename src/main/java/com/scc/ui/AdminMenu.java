package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final Scanner sc;
    private final UserDao userDao;
    private final User currentAdmin; // ✅ Needed to block self-edit

    // Allowed values per spec
    private static final List<String> VALID_ROLES = List.of("ADMIN", "STAFF", "BORROWER");
    private static final List<String> VALID_STATUSES = List.of("APPROVED", "PENDING");

    // ✅ Updated constructor — receives logged-in admin
    public AdminMenu(Scanner scanner, UserDao userDao, User currentAdmin) {
        this.sc = scanner;
        this.userDao = userDao;
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
            System.out.println("7. Logout");
            System.out.print("Choose an option (1-7): ");

            switch (sc.nextLine().trim()) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "4" -> addNewUser();
                case "5" -> updateUserRoleStatus();
                case "6" -> deleteUser();
                case "7" -> {
                    System.out.println("Logged out.\n");
                    return;
                }
                default -> System.out.println("⚠ Invalid option.\n");
            }
        }
    }

    // === ✅ VALIDATION HELPERS as required ===
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
        // Formatted table as required
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

    private void approveUser() {
        viewPendingUsers();
        int id = readIntInput("Enter User ID to approve (0 = Cancel): ");
        if (id == 0) return;
        if (userDao.updateStatus(id, "APPROVED")) {
            System.out.println("✅ User approved!\n");
        } else {
            System.out.println("❌ User not found.\n");
        }
    }

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
            userDao.updateStatus(all.get(all.size() - 1).id(), "APPROVED");
            System.out.println("✅ User added & approved.\n");
        }
    }

    private void updateUserRoleStatus() {
        viewAllUsers();

        int targetId = readIntInput("Enter User ID to update (0 = Cancel): ");
        if (targetId == 0) {
            System.out.println("Update cancelled.\n");
            return;
        }

        // ✅ SELF-EDIT BLOCK — exactly as specified
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

        // ✅ Use the NEW combined method
        boolean success = userDao.updateUserRoleAndStatus(targetId, newRole, newStatus);
        if (success) {
            System.out.println("✅ User updated successfully!\n");
        } else {
            System.out.println("❌ Update failed — user not found.\n");
        }
    }

    private void deleteUser() {
        viewAllUsers();
        int id = readIntInput("Enter User ID to delete (0 = Cancel): ");
        if (id == 0) return;

        // Optional: Also block self-delete for safety
        if (id == currentAdmin.id()) {
            System.out.println("⚠ Cannot delete your own active admin account.\n");
            return;
        }

        System.out.print("Confirm delete? (yes/no): ");
        if ("yes".equalsIgnoreCase(sc.nextLine().trim())) {
            if (userDao.deleteUser(id)) {
                System.out.println("✅ User deleted.\n");
            } else {
                System.out.println("❌ Failed.\n");
            }
        }
    }
}