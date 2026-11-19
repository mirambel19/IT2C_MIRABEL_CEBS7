package com.scc.dao;

import com.scc.model.ActivityLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    /** Inserts a new audit entry (parameterized). */
    public void logAction(int userId, String actionType, String description) {
        String sql = "INSERT INTO activity_logs (user_id, action_type, description) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, actionType);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("❌ Log Error: " + e.getMessage());
        }
    }

    /** All logs, newest first. */
    public List<ActivityLog> getAllLogs() {
        String sql = "SELECT * FROM activity_logs ORDER BY timestamp DESC, id DESC";
        List<ActivityLog> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            System.out.println("❌ Load Logs Error: " + e.getMessage());
        }
        return list;
    }

    /** Logs of one user, newest first. */
    public List<ActivityLog> getLogsByUserId(int userId) {
        String sql = "SELECT * FROM activity_logs WHERE user_id = ? ORDER BY timestamp DESC, id DESC";
        List<ActivityLog> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Load Logs Error: " + e.getMessage());
        }
        return list;
    }

    /** Deletes all historical logs. Returns true on success. */
    public boolean clearLogs() {
        String sql = "DELETE FROM activity_logs";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Clear Logs Error: " + e.getMessage());
            return false;
        }
    }

    private ActivityLog map(ResultSet rs) throws SQLException {
        return new ActivityLog(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("action_type"),
                rs.getString("description"),
                rs.getString("timestamp")
        );
    }
}