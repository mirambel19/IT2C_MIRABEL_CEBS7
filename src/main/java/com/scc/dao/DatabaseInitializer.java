package com.scc.dao;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void init() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                roles TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING'
            );
            """;

        // [NEW] Audit trail table. No FOREIGN KEY on purpose: logs must survive
        // even after a user account is deleted.
        String createActivityLogsTable = """
            CREATE TABLE IF NOT EXISTS activity_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                action_type TEXT NOT NULL,
                description TEXT,
                timestamp DATETIME DEFAULT (datetime('now', 'localtime'))
            );
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            if (conn == null) {
                System.err.println("Database connection failed!");
                return;
            }

            stmt.execute(createUsersTable);
            stmt.execute(createActivityLogsTable); // [NEW]

            String insertAdmin = """
                INSERT OR IGNORE INTO users (username, password, roles, status)
                VALUES ('admin', 'admin123', 'ADMIN', 'APPROVED');
                """;
            stmt.execute(insertAdmin);

        } catch (Exception e) {
            System.err.println("Init Error: " + e.getMessage());
        }
    }
}