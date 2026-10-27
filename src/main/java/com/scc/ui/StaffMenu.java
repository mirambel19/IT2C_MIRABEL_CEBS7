package com.scc.ui;

import com.scc.dao.UserDao;
import java.util.Scanner;

public class StaffMenu {
    private final Scanner sc;
    private final UserDao userDao;

    public StaffMenu(Scanner scanner, UserDao userDao) {
        this.sc = scanner;
        this.userDao = userDao;
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
                case "1" -> System.out.println("→ Processing borrow request...\n");
                case "2" -> System.out.println("→ Recording return...\n");
                case "3" -> System.out.println("→ Checking availability...\n");
                case "4" -> { System.out.println("Logged out.\n"); return; }
                default -> System.out.println("⚠ Invalid option.\n");
            }
        }
    }
}