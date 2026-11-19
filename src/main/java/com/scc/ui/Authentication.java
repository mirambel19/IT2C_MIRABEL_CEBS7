package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.Optional;
import java.util.Scanner;

public class Authentication {
    private final Scanner sc;
    private final UserDao userDao;
    private final ActivityLogDao logDao;

    public Authentication(Scanner scanner, UserDao userDao, ActivityLogDao logDao) {
        this.sc = scanner;
        this.userDao = userDao;
        this.logDao = logDao;
    }

    public void start() {
        while (true) {
            System.out.println("=".repeat(50));
            System.out.println("      CAMPUS EQUIPMENT BORROWING SYSTEM");
            System.out.println("=".repeat(50));
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            switch (sc.nextLine().trim()) {
                case "1" -> loginForm();
                case "2" -> registerForm();
                case "3" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("⚠ Invalid option. Try again.\n");
            }
        }
    }

    private void registerForm() {
        System.out.println("\n--- Register ---");
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter password: ");
        String password = sc.nextLine().trim();

        System.out.println("\nSelect Role:");
        System.out.println("1. STAFF");
        System.out.println("2. BORROWER");
        System.out.print("Choose role (1-2): ");
        String roleChoice = sc.nextLine().trim();
        String role = switch (roleChoice) {
            case "1" -> "STAFF";
            case "2" -> "BORROWER";
            default -> {
                System.out.println("⚠ Defaulted to BORROWER");
                yield "BORROWER";
            }
        };

        if (userDao.register(username, password, role)) {
            System.out.println("✅ Registration successful! Status: PENDING\n");
        }
    }

    private void loginForm() {
        System.out.println("\n--- Login ---");
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = sc.nextLine().trim();

        Optional<User> user = userDao.login(username, password);
        if (user.isPresent()) {
            User u = user.get();

            if (!"APPROVED".equals(u.status()) && !"ADMIN".equals(u.roles())) {
                System.out.println("⚠ Account PENDING approval. Contact admin.\n");
                return;
            }

            System.out.printf("-> Welcome %s | ROLE: %s%n%n", u.username(), u.roles());
            routeToDashboard(u);
        } else {
            System.out.println("❌ Invalid username or password.\n");
        }
    }

    private void routeToDashboard(User user) {
        // [NEW] Log the login
        logDao.logAction(user.id(), "USER_LOGIN",
                user.roles() + " '" + user.username() + "' [ID " + user.id() + "] logged in");

        switch (user.roles()) {
            case "ADMIN" -> new AdminMenu(sc, userDao, logDao, user).show();
            case "STAFF" -> new StaffMenu(sc, userDao, logDao, user).show();
            case "BORROWER" -> new BorrowerMenu(sc, userDao, logDao, user).show();
            default -> {
                System.out.println("❌ Access denied.\n");
                return;
            }
        }

        // [NEW] Runs after the menu's show() returns (user chose Logout)
        logDao.logAction(user.id(), "USER_LOGOUT",
                user.roles() + " '" + user.username() + "' [ID " + user.id() + "] logged out");
    }
}