package com.scc.ui;

import com.scc.dao.UserDao;
import java.util.Scanner;

public class BorrowerMenu {
    private final Scanner sc;
    private final UserDao userDao;

    public BorrowerMenu(Scanner scanner, UserDao userDao) {
        this.sc = scanner;
        this.userDao = userDao;
    }

    public void show() {
        while (true) {
            System.out.println("=".repeat(50));
            System.out.println("        BORROWER DASHBOARD");
            System.out.println("=".repeat(50));
            System.out.println("1. View Available Equipment");
            System.out.println("2. Submit Borrow Request");
            System.out.println("3. My Borrowed Items");
            System.out.println("4. Logout");
            System.out.print("Choose an option (1-4): ");

            switch (sc.nextLine().trim()) {
                case "1" -> System.out.println("→ Loading equipment list...\n");
                case "2" -> System.out.println("→ Opening borrow form...\n");
                case "3" -> System.out.println("→ Loading your borrows...\n");
                case "4" -> { System.out.println("Logged out.\n"); return; }
                default -> System.out.println("⚠ Invalid option.\n");
            }
        }
    }
}