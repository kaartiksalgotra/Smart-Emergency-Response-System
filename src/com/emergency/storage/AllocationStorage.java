package com.emergency.storage;

import com.emergency.model.Allocation;
import com.emergency.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Storage class for managing Resource Allocations via MySQL JDBC with in-memory fallback.
 */
public class AllocationStorage {

    private final List<Allocation> memoryAllocations = Collections.synchronizedList(new ArrayList<>());
    private int nextAllocationId = 1;

    public AllocationStorage() {
        // Ready for database or in-memory operation
    }

    public synchronized int generateAllocationId() {
        if (DatabaseConnection.isAvailable()) {
            String sql = "SELECT COALESCE(MAX(allocation_id), 0) + 1 FROM allocations";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException ignored) {}
        }
        return nextAllocationId++;
    }

    public synchronized void addAllocation(Allocation allocation) {
        if (allocation == null) return;

        if (allocation.getAllocationId() <= 0) {
            allocation.setAllocationId(generateAllocationId());
        }

        synchronized (memoryAllocations) {
            boolean exists = false;
            for (int i = 0; i < memoryAllocations.size(); i++) {
                if (memoryAllocations.get(i).getAllocationId() == allocation.getAllocationId()) {
                    memoryAllocations.set(i, allocation);
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                memoryAllocations.add(0, allocation);
            }
        }

        if (DatabaseConnection.isAvailable()) {
            String sql = "INSERT INTO allocations (emergency_id, resource_id, assigned_at, released_at, active) " +
                         "VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, allocation.getEmergencyId());
                ps.setInt(2, allocation.getResourceId());
                ps.setTimestamp(3, Timestamp.valueOf(allocation.getAssignedAt()));
                if (allocation.getReleasedAt() != null) {
                    ps.setTimestamp(4, Timestamp.valueOf(allocation.getReleasedAt()));
                } else {
                    ps.setNull(4, Types.TIMESTAMP);
                }
                ps.setBoolean(5, allocation.isActive());

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        allocation.setAllocationId(rs.getInt(1));
                    }
                }
            } catch (SQLException e) {
                System.err.println("Warning: Could not persist allocation to database: " + e.getMessage());
            }
        }
    }

    public Allocation findAllocationById(int allocationId) {
        if (DatabaseConnection.isAvailable()) {
            String sql = "SELECT allocation_id, emergency_id, resource_id, assigned_at, released_at, active " +
                         "FROM allocations WHERE allocation_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, allocationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToAllocation(rs);
                    }
                }
            } catch (SQLException ignored) {}
        }

        synchronized (memoryAllocations) {
            for (Allocation a : memoryAllocations) {
                if (a.getAllocationId() == allocationId) return a;
            }
        }
        return null;
    }

    public ArrayList<Allocation> getAllAllocations() {
        if (DatabaseConnection.isAvailable()) {
            ArrayList<Allocation> list = new ArrayList<>();
            String sql = "SELECT allocation_id, emergency_id, resource_id, assigned_at, released_at, active " +
                         "FROM allocations ORDER BY allocation_id DESC";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAllocation(rs));
                }
                if (!list.isEmpty()) return list;
            } catch (SQLException ignored) {}
        }

        synchronized (memoryAllocations) {
            return new ArrayList<>(memoryAllocations);
        }
    }

    public ArrayList<Allocation> getAllocationsByEmergencyId(int emergencyId) {
        if (DatabaseConnection.isAvailable()) {
            ArrayList<Allocation> list = new ArrayList<>();
            String sql = "SELECT allocation_id, emergency_id, resource_id, assigned_at, released_at, active " +
                         "FROM allocations WHERE emergency_id = ? ORDER BY allocation_id DESC";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, emergencyId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToAllocation(rs));
                    }
                }
                if (!list.isEmpty()) return list;
            } catch (SQLException ignored) {}
        }

        ArrayList<Allocation> list = new ArrayList<>();
        synchronized (memoryAllocations) {
            for (Allocation a : memoryAllocations) {
                if (a.getEmergencyId() == emergencyId) {
                    list.add(a);
                }
            }
        }
        return list;
    }

    public ArrayList<Allocation> getAllocationsByResourceId(int resourceId) {
        if (DatabaseConnection.isAvailable()) {
            ArrayList<Allocation> list = new ArrayList<>();
            String sql = "SELECT allocation_id, emergency_id, resource_id, assigned_at, released_at, active " +
                         "FROM allocations WHERE resource_id = ? ORDER BY allocation_id DESC";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, resourceId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToAllocation(rs));
                    }
                }
                if (!list.isEmpty()) return list;
            } catch (SQLException ignored) {}
        }

        ArrayList<Allocation> list = new ArrayList<>();
        synchronized (memoryAllocations) {
            for (Allocation a : memoryAllocations) {
                if (a.getResourceId() == resourceId) {
                    list.add(a);
                }
            }
        }
        return list;
    }

    public ArrayList<Allocation> getActiveAllocations() {
        if (DatabaseConnection.isAvailable()) {
            ArrayList<Allocation> list = new ArrayList<>();
            String sql = "SELECT allocation_id, emergency_id, resource_id, assigned_at, released_at, active " +
                         "FROM allocations WHERE active = 1 ORDER BY allocation_id DESC";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAllocation(rs));
                }
                if (!list.isEmpty()) return list;
            } catch (SQLException ignored) {}
        }

        ArrayList<Allocation> list = new ArrayList<>();
        synchronized (memoryAllocations) {
            for (Allocation a : memoryAllocations) {
                if (a.isActive()) {
                    list.add(a);
                }
            }
        }
        return list;
    }

    public synchronized boolean updateAllocation(Allocation allocation) {
        if (allocation == null) return false;

        boolean updatedInMemory = false;
        synchronized (memoryAllocations) {
            for (int i = 0; i < memoryAllocations.size(); i++) {
                if (memoryAllocations.get(i).getAllocationId() == allocation.getAllocationId()) {
                    memoryAllocations.set(i, allocation);
                    updatedInMemory = true;
                    break;
                }
            }
        }

        if (DatabaseConnection.isAvailable()) {
            String sql = "UPDATE allocations SET emergency_id = ?, resource_id = ?, assigned_at = ?, released_at = ?, active = ? " +
                         "WHERE allocation_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, allocation.getEmergencyId());
                ps.setInt(2, allocation.getResourceId());
                ps.setTimestamp(3, Timestamp.valueOf(allocation.getAssignedAt()));
                if (allocation.getReleasedAt() != null) {
                    ps.setTimestamp(4, Timestamp.valueOf(allocation.getReleasedAt()));
                } else {
                    ps.setNull(4, Types.TIMESTAMP);
                }
                ps.setBoolean(5, allocation.isActive());
                ps.setInt(6, allocation.getAllocationId());

                return ps.executeUpdate() > 0;
            } catch (SQLException ignored) {}
        }

        return updatedInMemory;
    }

    public synchronized boolean removeAllocation(int allocationId) {
        boolean removedFromMemory = false;
        synchronized (memoryAllocations) {
            removedFromMemory = memoryAllocations.removeIf(a -> a.getAllocationId() == allocationId);
        }

        if (DatabaseConnection.isAvailable()) {
            String sql = "DELETE FROM allocations WHERE allocation_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, allocationId);
                return ps.executeUpdate() > 0;
            } catch (SQLException ignored) {}
        }

        return removedFromMemory;
    }

    private Allocation mapResultSetToAllocation(ResultSet rs) throws SQLException {
        int id = rs.getInt("allocation_id");
        int emergencyId = rs.getInt("emergency_id");
        int resourceId = rs.getInt("resource_id");
        Timestamp assignedTs = rs.getTimestamp("assigned_at");
        Timestamp releasedTs = rs.getTimestamp("released_at");
        boolean active = rs.getBoolean("active");

        LocalDateTime assignedAt = assignedTs != null ? assignedTs.toLocalDateTime() : LocalDateTime.now();
        LocalDateTime releasedAt = releasedTs != null ? releasedTs.toLocalDateTime() : null;

        return new Allocation(id, emergencyId, resourceId, assignedAt, releasedAt, active);
    }
}
