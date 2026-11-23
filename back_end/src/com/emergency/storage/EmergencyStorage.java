package com.emergency.storage;

import com.emergency.enums.EmergencyStatus;
import com.emergency.enums.EmergencyType;
import com.emergency.enums.PriorityLevel;
import com.emergency.model.Emergency;
import com.emergency.model.EmergencyStatusHistory;
import com.emergency.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Storage class for managing Emergencies and Status History via MySQL JDBC.
 */
public class EmergencyStorage {

    public EmergencyStorage() {
        // Database-backed storage: records are permanently persisted in MySQL
    }

    public int generateEmergencyId() {
        String sql = "SELECT COALESCE(MAX(emergency_id), 0) + 1 FROM emergencies";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error generating emergency id: " + e.getMessage());
        }
        return 1;
    }

    public void addEmergency(Emergency emergency) {
        if (emergency == null) return;
        String sql = "INSERT INTO emergencies (user_id, emergency_type, priority, location, description, affected_people, status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, emergency.getUserId());
            ps.setString(2, emergency.getEmergencyType().name());
            ps.setString(3, emergency.getPriority().name());
            ps.setString(4, emergency.getLocation());
            ps.setString(5, emergency.getDescription());
            ps.setInt(6, emergency.getAffectedPeople());
            ps.setString(7, emergency.getStatus().name());
            ps.setTimestamp(8, Timestamp.valueOf(emergency.getCreatedAt() != null ? emergency.getCreatedAt() : LocalDateTime.now()));
            ps.setTimestamp(9, Timestamp.valueOf(emergency.getUpdatedAt() != null ? emergency.getUpdatedAt() : LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    emergency.setEmergencyId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding emergency: " + e.getMessage());
        }
    }

    public Emergency findEmergencyById(int emergencyId) {
        String sql = "SELECT emergency_id, user_id, emergency_type, priority, location, description, affected_people, status, created_at, updated_at " +
                     "FROM emergencies WHERE emergency_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, emergencyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmergency(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding emergency by id: " + e.getMessage());
        }
        return null;
    }

    public ArrayList<Emergency> getAllEmergencies() {
        ArrayList<Emergency> list = new ArrayList<>();
        String sql = "SELECT emergency_id, user_id, emergency_type, priority, location, description, affected_people, status, created_at, updated_at " +
                     "FROM emergencies ORDER BY emergency_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToEmergency(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all emergencies: " + e.getMessage());
        }
        return list;
    }

    public ArrayList<Emergency> getEmergenciesByUserId(int userId) {
        ArrayList<Emergency> list = new ArrayList<>();
        String sql = "SELECT emergency_id, user_id, emergency_type, priority, location, description, affected_people, status, created_at, updated_at " +
                     "FROM emergencies WHERE user_id = ? ORDER BY emergency_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEmergency(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user emergencies: " + e.getMessage());
        }
        return list;
    }

    public boolean updateEmergency(Emergency updatedEmergency) {
        if (updatedEmergency == null) return false;

        String selectStatusSql = "SELECT status FROM emergencies WHERE emergency_id = ?";
        String updateSql = "UPDATE emergencies SET emergency_type = ?, priority = ?, location = ?, description = ?, " +
                           "affected_people = ?, status = ?, updated_at = ? WHERE emergency_id = ?";
        String insertHistorySql = "INSERT INTO emergency_status_history (emergency_id, previous_status, new_status, changed_at) " +
                                  "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            EmergencyStatus previousStatus = null;
            try (PreparedStatement psSelect = conn.prepareStatement(selectStatusSql)) {
                psSelect.setInt(1, updatedEmergency.getEmergencyId());
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        previousStatus = EmergencyStatus.valueOf(rs.getString("status"));
                    }
                }
            }

            int rowsUpdated = 0;
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setString(1, updatedEmergency.getEmergencyType().name());
                psUpdate.setString(2, updatedEmergency.getPriority().name());
                psUpdate.setString(3, updatedEmergency.getLocation());
                psUpdate.setString(4, updatedEmergency.getDescription());
                psUpdate.setInt(5, updatedEmergency.getAffectedPeople());
                psUpdate.setString(6, updatedEmergency.getStatus().name());
                psUpdate.setTimestamp(7, now);
                psUpdate.setInt(8, updatedEmergency.getEmergencyId());
                rowsUpdated = psUpdate.executeUpdate();
            }

            // Record status change in emergency_status_history if the status changed
            if (rowsUpdated > 0 && previousStatus != null && previousStatus != updatedEmergency.getStatus()) {
                try (PreparedStatement psHistory = conn.prepareStatement(insertHistorySql)) {
                    psHistory.setInt(1, updatedEmergency.getEmergencyId());
                    psHistory.setString(2, previousStatus.name());
                    psHistory.setString(3, updatedEmergency.getStatus().name());
                    psHistory.setTimestamp(4, now);
                    psHistory.executeUpdate();
                } catch (SQLException ex) {
                    System.err.println("Warning: Status history could not be recorded: " + ex.getMessage());
                }
            }

            conn.commit();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Error updating emergency: " + e.getMessage());
            return false;
        }
    }

    public boolean recordStatusChange(int emergencyId, EmergencyStatus previousStatus, EmergencyStatus newStatus) {
        if (previousStatus == null || newStatus == null || previousStatus == newStatus) return false;
        String sql = "INSERT INTO emergency_status_history (emergency_id, previous_status, new_status, changed_at) " +
                     "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, emergencyId);
            ps.setString(2, previousStatus.name());
            ps.setString(3, newStatus.name());
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error recording status change: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<EmergencyStatusHistory> getStatusHistoryByEmergencyId(int emergencyId) {
        ArrayList<EmergencyStatusHistory> history = new ArrayList<>();
        String sql = "SELECT history_id, emergency_id, previous_status, new_status, changed_at " +
                     "FROM emergency_status_history WHERE emergency_id = ? ORDER BY changed_at ASC, history_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, emergencyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int hid = rs.getInt("history_id");
                    int eid = rs.getInt("emergency_id");
                    EmergencyStatus prev = EmergencyStatus.valueOf(rs.getString("previous_status"));
                    EmergencyStatus next = EmergencyStatus.valueOf(rs.getString("new_status"));
                    Timestamp ts = rs.getTimestamp("changed_at");
                    LocalDateTime changedAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                    history.add(new EmergencyStatusHistory(hid, eid, prev, next, changedAt));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching status history for emergency " + emergencyId + ": " + e.getMessage());
        }
        return history;
    }

    public ArrayList<EmergencyStatusHistory> getAllStatusHistory() {
        ArrayList<EmergencyStatusHistory> history = new ArrayList<>();
        String sql = "SELECT history_id, emergency_id, previous_status, new_status, changed_at " +
                     "FROM emergency_status_history ORDER BY changed_at DESC, history_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int hid = rs.getInt("history_id");
                int eid = rs.getInt("emergency_id");
                EmergencyStatus prev = EmergencyStatus.valueOf(rs.getString("previous_status"));
                EmergencyStatus next = EmergencyStatus.valueOf(rs.getString("new_status"));
                Timestamp ts = rs.getTimestamp("changed_at");
                LocalDateTime changedAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                history.add(new EmergencyStatusHistory(hid, eid, prev, next, changedAt));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all status history: " + e.getMessage());
        }
        return history;
    }

    public boolean removeEmergency(int emergencyId) {
        String sql = "DELETE FROM emergencies WHERE emergency_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, emergencyId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing emergency: " + e.getMessage());
            return false;
        }
    }

    private Emergency mapResultSetToEmergency(ResultSet rs) throws SQLException {
        int id = rs.getInt("emergency_id");
        int userId = rs.getInt("user_id");
        EmergencyType type = EmergencyType.valueOf(rs.getString("emergency_type"));
        PriorityLevel priority = PriorityLevel.valueOf(rs.getString("priority"));
        String location = rs.getString("location");
        String description = rs.getString("description");
        int affectedPeople = rs.getInt("affected_people");
        EmergencyStatus status = EmergencyStatus.valueOf(rs.getString("status"));
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");

        LocalDateTime createdAt = createdTs != null ? createdTs.toLocalDateTime() : LocalDateTime.now();
        LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : LocalDateTime.now();

        return new Emergency(id, userId, type, priority, location, description, affectedPeople, status, createdAt, updatedAt);
    }
}
