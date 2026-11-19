package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.Scanner;

public class BorrowerMenu {
    private final Scanner sc;
    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final User currentUser;

    public BorrowerMenu(Scanner scanner, UserDao userDao, ActivityLogDao logDao, User currentUser) {
        this.sc = scanner;
        this.userDao = userDao;
        this.logDao = logDao;
        this.currentUser = currentUser;
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
                case "2" -> submitBorrowRequest();
                case "3" -> System.out.println("→ Loading your borrows...\n");
                case "4" -> { System.out.println("Logged out.\n"); return; }
                default -> System.out.println("⚠ Invalid option.\n");
            }
        }
    }

    // Domain action: BORROW_REQUESTED
    private void submitBorrowRequest() {
        System.out.print("Equipment name (0 = Cancel): ");
        String item = sc.nextLine().trim();
        if (item.isEmpty() || "0".equals(item)) {
            System.out.println("Cancelled.\n");
            return;
        }
        logDao.logAction(currentUser.id(), "BORROW_REQUESTED",
                "Borrower [" + currentUser.id() + "] requested to borrow " + item);
        System.out.println("✅ Borrow request submitted.\n");
    }
}