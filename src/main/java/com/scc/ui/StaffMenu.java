package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.Scanner;

public class StaffMenu {
    private final Scanner sc;
    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final User currentUser;

    public StaffMenu(Scanner scanner, UserDao userDao, ActivityLogDao logDao, User currentUser) {
        this.sc = scanner;
        this.userDao = userDao;
        this.logDao = logDao;
        this.currentUser = currentUser;
    }

    public void show() {
        while (true) {
            System.out.println("=".repeat(50));
            System.out.println("         STAFF DASHBOARD");
            System.out.println("=".repeat(50));
            System.out.println("1. Process Borrow Request");
            System.out.println("2. Record Equipment Return");
            System.out.println("3. Check Equipment Availability");
            System.out.println("4. Logout");
            System.out.print("Choose an option (1-4): ");

            switch (sc.nextLine().trim()) {
                case "1" -> processBorrowRequest();
                case "2" -> recordReturn();
                case "3" -> System.out.println("→ Checking availability...\n");
                case "4" -> { System.out.println("Logged out.\n"); return; }
                default -> System.out.println("⚠ Invalid option.\n");
            }
        }
    }

    // Domain action: BORROW_PROCESSED
    private void processBorrowRequest() {
        System.out.print("Equipment name (0 = Cancel): ");
        String item = sc.nextLine().trim();
        if (item.isEmpty() || "0".equals(item)) {
            System.out.println("Cancelled.\n");
            return;
        }
        System.out.println("→ Processing borrow request...");
        logDao.logAction(currentUser.id(), "BORROW_PROCESSED",
                "Staff [" + currentUser.id() + "] processed borrow request for " + item);
        System.out.println("✅ Borrow request processed.\n");
    }

    // Domain action: EQUIPMENT_RETURNED
    private void recordReturn() {
        System.out.print("Equipment name (0 = Cancel): ");
        String item = sc.nextLine().trim();
        if (item.isEmpty() || "0".equals(item)) {
            System.out.println("Cancelled.\n");
            return;
        }
        System.out.println("→ Recording return...");
        logDao.logAction(currentUser.id(), "EQUIPMENT_RETURNED",
                "Staff [" + currentUser.id() + "] recorded return of " + item);
        System.out.println("✅ Return recorded.\n");
    }
}