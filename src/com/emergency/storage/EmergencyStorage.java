package com.emergency.storage;

import com.emergency.enums.EmergencyStatus;
import com.emergency.enums.EmergencyType;
import com.emergency.enums.PriorityLevel;
import com.emergency.model.Emergency;
import com.emergency.model.EmergencyStatusHistory;
import com.emergency.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Storage class for managing Emergencies and Status History via MySQL JDBC with in-memory fallback.
 */
public class EmergencyStorage {

    private final List<Emergency> memoryEmergencies = Collections.synchronizedList(new ArrayList<>());
    private final List<EmergencyStatusHistory> memoryHistory = Collections.synchronizedList(new ArrayList<>());
    private int nextEmergencyId = 1;
    private int nextHistoryId = 1;

    public EmergencyStorage() {
        // Ready for database or in-memory operation
    }

    public synchronized int generateEmergencyId() {
        if (DatabaseConnection.isAvailable()) {
            String sql = "SELECT COALESCE(GREATEST(" +
                         "(SELECT COALESCE(MAX(emergency_id), 0) FROM emergencies), " +
                         "(SELECT COALESCE(MAX(emergency_id), 0) FROM emergency_status_history)" +
                         "), 0) + 1";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int dbNext = rs.getInt(1);
                    int assigned = Math.max(dbNext, nextEmergencyId);
                    nextEmergencyId = assigned + 1;
                    return assigned;
                }
            } catch (SQLException ignored) {}
        }
        return nextEmergencyId++;
    }

    public synchronized void addEmergency(Emergency emergency) {
        if (emergency == null) return;

        if (emergency.getEmergencyId() <= 0) {
            emergency.setEmergencyId(generateEmergencyId());
        }

        // Clean up any stale or orphaned status history for this emergency ID in memory
        synchronized (memoryHistory) {
            memoryHistory.removeIf(h -> h.getEmergencyId() == emergency.getEmergencyId());
        }

        synchronized (memoryEmergencies) {
            boolean exists = false;
            for (int i = 0; i < memoryEmergencies.size(); i++) {
                if (memoryEmergencies.get(i).getEmergencyId() == emergency.getEmergencyId()) {
                    memoryEmergencies.set(i, emergency);
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                memoryEmergencies.add(0, emergency); // Most recent first
            }
            if (emergency.getEmergencyId() >= nextEmergencyId) {
                nextEmergencyId = emergency.getEmergencyId() + 1;
            }
        }

        if (DatabaseConnection.isAvailable()) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Delete any stale orphaned history for this ID in MySQL
                try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM emergency_status_history WHERE emergency_id = ?")) {
                    psDel.setInt(1, emergency.getEmergencyId());
                    psDel.executeUpdate();
                } catch (SQLException ignored) {}

                String sql = "INSERT INTO emergencies (emergency_id, user_id, emergency_type, priority, location, description, affected_people, status, created_at, updated_at) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, emergency.getEmergencyId());
                    ps.setInt(2, emergency.getUserId());
                    ps.setString(3, emergency.getEmergencyType().name());
                    ps.setString(4, emergency.getPriority().name());
                    ps.setString(5, emergency.getLocation());
                    ps.setString(6, emergency.getDescription());
                    ps.setInt(7, emergency.getAffectedPeople());
                    ps.setString(8, emergency.getStatus().name());
                    ps.setTimestamp(9, Timestamp.valueOf(emergency.getCreatedAt()));
                    ps.setTimestamp(10, Timestamp.valueOf(emergency.getUpdatedAt()));

                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("Warning: Could not persist emergency to database: " + e.getMessage());
            }
        }
    }

    public Emergency findEmergencyById(int emergencyId) {
        if (DatabaseConnection.isAvailable()) {
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
            } catch (SQLException ignored) {}
        }

        synchronized (memoryEmergencies) {
            for (Emergency em : memoryEmergencies) {
                if (em.getEmergencyId() == emergencyId) {
                    return new Emergency(em.getEmergencyId(), em.getUserId(), em.getEmergencyType(),
                            em.getPriority(), em.getLocation(), em.getDescription(), em.getAffectedPeople(),
                            em.getStatus(), em.getCreatedAt(), em.getUpdatedAt());
                }
            }
        }
        return null;
    }

    public ArrayList<Emergency> getAllEmergencies() {
        if (DatabaseConnection.isAvailable()) {
            ArrayList<Emergency> list = new ArrayList<>();
            String sql = "SELECT emergency_id, user_id, emergency_type, priority, location, description, affected_people, status, created_at, updated_at " +
                         "FROM emergencies ORDER BY emergency_id DESC";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEmergency(rs));
                }
                if (!list.isEmpty()) return list;
            } catch (SQLException ignored) {}
        }

        synchronized (memoryEmergencies) {
            ArrayList<Emergency> copy = new ArrayList<>();
            for (Emergency em : memoryEmergencies) {
                copy.add(new Emergency(em.getEmergencyId(), em.getUserId(), em.getEmergencyType(),
                        em.getPriority(), em.getLocation(), em.getDescription(), em.getAffectedPeople(),
                        em.getStatus(), em.getCreatedAt(), em.getUpdatedAt()));
            }
            return copy;
        }
    }

    public ArrayList<Emergency> getEmergenciesByUserId(int userId) {
        if (DatabaseConnection.isAvailable()) {
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
                if (!list.isEmpty()) return list;
            } catch (SQLException ignored) {}
        }

        ArrayList<Emergency> list = new ArrayList<>();
        synchronized (memoryEmergencies) {
            for (Emergency em : memoryEmergencies) {
                if (em.getUserId() == userId) {
                    list.add(new Emergency(em.getEmergencyId(), em.getUserId(), em.getEmergencyType(),
                            em.getPriority(), em.getLocation(), em.getDescription(), em.getAffectedPeople(),
                            em.getStatus(), em.getCreatedAt(), em.getUpdatedAt()));
                }
            }
        }
        return list;
    }

    public synchronized boolean updateEmergency(Emergency updatedEmergency) {
        if (updatedEmergency == null) return false;

        boolean updatedInMemory = false;
        EmergencyStatus previousStatus = null;

        synchronized (memoryEmergencies) {
            for (int i = 0; i < memoryEmergencies.size(); i++) {
                Emergency current = memoryEmergencies.get(i);
                if (current.getEmergencyId() == updatedEmergency.getEmergencyId()) {
                    previousStatus = current.getStatus();
                    memoryEmergencies.set(i, updatedEmergency);
                    updatedInMemory = true;
                    break;
                }
            }
        }

        if (DatabaseConnection.isAvailable()) {
            String selectStatusSql = "SELECT status FROM emergencies WHERE emergency_id = ?";
            String updateSql = "UPDATE emergencies SET emergency_type = ?, priority = ?, location = ?, description = ?, " +
                               "affected_people = ?, status = ?, updated_at = ? WHERE emergency_id = ?";

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                EmergencyStatus dbPrev = null;
                try (PreparedStatement psSelect = conn.prepareStatement(selectStatusSql)) {
                    psSelect.setInt(1, updatedEmergency.getEmergencyId());
                    try (ResultSet rs = psSelect.executeQuery()) {
                        if (rs.next()) {
                            dbPrev = EmergencyStatus.valueOf(rs.getString("status"));
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

                conn.commit();

                if (dbPrev != null) {
                    previousStatus = dbPrev;
                }
                if (rowsUpdated > 0) {
                    updatedInMemory = true;
                }
            } catch (SQLException ignored) {}
        }

        if (previousStatus != null && previousStatus != updatedEmergency.getStatus()) {
            recordStatusChange(updatedEmergency.getEmergencyId(), previousStatus, updatedEmergency.getStatus());
        }

        return updatedInMemory;
    }

    public synchronized boolean recordStatusChange(int emergencyId, EmergencyStatus previousStatus, EmergencyStatus newStatus) {
        if (previousStatus == null || newStatus == null || previousStatus == newStatus) return false;

        // Prevent duplicate transition logs in memory
        synchronized (memoryHistory) {
            for (int i = memoryHistory.size() - 1; i >= 0; i--) {
                EmergencyStatusHistory h = memoryHistory.get(i);
                if (h.getEmergencyId() == emergencyId) {
                    if (h.getPreviousStatus() == previousStatus && h.getNewStatus() == newStatus) {
                        return false;
                    }
                    break;
                }
            }
            int histId = nextHistoryId++;
            EmergencyStatusHistory entry = new EmergencyStatusHistory(histId, emergencyId, previousStatus, newStatus, LocalDateTime.now());
            memoryHistory.add(entry);
        }

        if (DatabaseConnection.isAvailable()) {
            String checkSql = "SELECT previous_status, new_status FROM emergency_status_history WHERE emergency_id = ? ORDER BY changed_at DESC, history_id DESC LIMIT 1";
            String sql = "INSERT INTO emergency_status_history (emergency_id, previous_status, new_status, changed_at) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection()) {
                try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                    psCheck.setInt(1, emergencyId);
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            String p = rs.getString("previous_status");
                            String n = rs.getString("new_status");
                            if (previousStatus.name().equals(p) && newStatus.name().equals(n)) {
                                return true;
                            }
                        }
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, emergencyId);
                    ps.setString(2, previousStatus.name());
                    ps.setString(3, newStatus.name());
                    ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    return ps.executeUpdate() > 0;
                }
            } catch (SQLException ignored) {}
        }
        return true;
    }

    public ArrayList<EmergencyStatusHistory> getStatusHistoryByEmergencyId(int emergencyId) {
        ArrayList<EmergencyStatusHistory> history = new ArrayList<>();
        if (DatabaseConnection.isAvailable()) {
            String sql = "SELECT h.history_id, h.emergency_id, h.previous_status, h.new_status, h.changed_at " +
                         "FROM emergency_status_history h " +
                         "INNER JOIN emergencies e ON h.emergency_id = e.emergency_id " +
                         "WHERE h.emergency_id = ? ORDER BY h.changed_at ASC, h.history_id ASC";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, emergencyId);
                try (ResultSet rs = ps.executeQuery()) {
                    EmergencyStatus lastPrev = null;
                    EmergencyStatus lastNext = null;
                    while (rs.next()) {
                        int hid = rs.getInt("history_id");
                        int eid = rs.getInt("emergency_id");
                        EmergencyStatus prev = EmergencyStatus.valueOf(rs.getString("previous_status"));
                        EmergencyStatus next = EmergencyStatus.valueOf(rs.getString("new_status"));
                        Timestamp ts = rs.getTimestamp("changed_at");
                        LocalDateTime changedAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                        if (prev == lastPrev && next == lastNext) {
                            continue;
                        }
                        lastPrev = prev;
                        lastNext = next;

                        history.add(new EmergencyStatusHistory(hid, eid, prev, next, changedAt));
                    }
                }
                if (!history.isEmpty()) return history;
            } catch (SQLException ignored) {}
        }

        boolean emergencyExists = false;
        synchronized (memoryEmergencies) {
            for (Emergency em : memoryEmergencies) {
                if (em.getEmergencyId() == emergencyId) {
                    emergencyExists = true;
                    break;
                }
            }
        }
        if (!emergencyExists) {
            return history;
        }

        synchronized (memoryHistory) {
            EmergencyStatus lastPrev = null;
            EmergencyStatus lastNext = null;
            for (EmergencyStatusHistory h : memoryHistory) {
                if (h.getEmergencyId() == emergencyId) {
                    if (h.getPreviousStatus() == lastPrev && h.getNewStatus() == lastNext) {
                        continue;
                    }
                    lastPrev = h.getPreviousStatus();
                    lastNext = h.getNewStatus();
                    history.add(h);
                }
            }
        }
        return history;
    }

    public ArrayList<EmergencyStatusHistory> getAllStatusHistory() {
        ArrayList<EmergencyStatusHistory> history = new ArrayList<>();
        if (DatabaseConnection.isAvailable()) {
            String sql = "SELECT h.history_id, h.emergency_id, h.previous_status, h.new_status, h.changed_at " +
                         "FROM emergency_status_history h " +
                         "INNER JOIN emergencies e ON h.emergency_id = e.emergency_id " +
                         "ORDER BY h.changed_at DESC, h.history_id DESC";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                int lastEid = -1;
                EmergencyStatus lastPrev = null;
                EmergencyStatus lastNext = null;
                while (rs.next()) {
                    int hid = rs.getInt("history_id");
                    int eid = rs.getInt("emergency_id");
                    EmergencyStatus prev = EmergencyStatus.valueOf(rs.getString("previous_status"));
                    EmergencyStatus next = EmergencyStatus.valueOf(rs.getString("new_status"));
                    Timestamp ts = rs.getTimestamp("changed_at");
                    LocalDateTime changedAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                    if (eid == lastEid && prev == lastPrev && next == lastNext) {
                        continue;
                    }
                    lastEid = eid;
                    lastPrev = prev;
                    lastNext = next;

                    history.add(new EmergencyStatusHistory(hid, eid, prev, next, changedAt));
                }
                if (!history.isEmpty()) return history;
            } catch (SQLException ignored) {}
        }

        java.util.Set<Integer> activeEmergencyIds = new java.util.HashSet<>();
        synchronized (memoryEmergencies) {
            for (Emergency em : memoryEmergencies) {
                activeEmergencyIds.add(em.getEmergencyId());
            }
        }

        synchronized (memoryHistory) {
            int lastEid = -1;
            EmergencyStatus lastPrev = null;
            EmergencyStatus lastNext = null;
            for (EmergencyStatusHistory h : memoryHistory) {
                if (!activeEmergencyIds.contains(h.getEmergencyId())) {
                    continue;
                }
                if (h.getEmergencyId() == lastEid && h.getPreviousStatus() == lastPrev && h.getNewStatus() == lastNext) {
                    continue;
                }
                lastEid = h.getEmergencyId();
                lastPrev = h.getPreviousStatus();
                lastNext = h.getNewStatus();
                history.add(h);
            }
        }
        return history;
    }

    public synchronized boolean removeEmergency(int emergencyId) {
        boolean removedFromMemory = false;
        synchronized (memoryEmergencies) {
            removedFromMemory = memoryEmergencies.removeIf(e -> e.getEmergencyId() == emergencyId);
        }
        synchronized (memoryHistory) {
            memoryHistory.removeIf(h -> h.getEmergencyId() == emergencyId);
        }

        if (DatabaseConnection.isAvailable()) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                try (PreparedStatement psHist = conn.prepareStatement("DELETE FROM emergency_status_history WHERE emergency_id = ?")) {
                    psHist.setInt(1, emergencyId);
                    psHist.executeUpdate();
                }
                try (PreparedStatement psEm = conn.prepareStatement("DELETE FROM emergencies WHERE emergency_id = ?")) {
                    psEm.setInt(1, emergencyId);
                    return psEm.executeUpdate() > 0;
                }
            } catch (SQLException ignored) {}
        }
        return removedFromMemory;
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
