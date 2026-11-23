package com.emergency.storage;

import com.emergency.model.Allocation;
import com.emergency.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Storage class for managing Resource Allocations via MySQL JDBC.
 */
public class AllocationStorage {

    public AllocationStorage() {
        // Database-backed storage: records are permanently persisted in MySQL
    }

    public int generateAllocationId() {
        String sql = "SELECT COALESCE(MAX(allocation_id), 0) + 1 FROM allocations";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error generating allocation id: " + e.getMessage());
        }
        return 1;
    }

    public void addAllocation(Allocation allocation) {
        if (allocation == null) return;
        String sql = "INSERT INTO allocations (emergency_id, resource_id, assigned_at, released_at, active) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, allocation.getEmergencyId());
            ps.setInt(2, allocation.getResourceId());
            ps.setTimestamp(3, Timestamp.valueOf(allocation.getAssignedAt() != null ? allocation.getAssignedAt() : LocalDateTime.now()));
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
            System.err.println("Error adding allocation: " + e.getMessage());
        }
    }

    public Allocation findAllocationById(int allocationId) {
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
        } catch (SQLException e) {
            System.err.println("Error finding allocation by id: " + e.getMessage());
        }
        return null;
    }

    public ArrayList<Allocation> getAllAllocations() {
        ArrayList<Allocation> list = new ArrayList<>();
        String sql = "SELECT allocation_id, emergency_id, resource_id, assigned_at, released_at, active " +
                     "FROM allocations ORDER BY allocation_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAllocation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all allocations: " + e.getMessage());
        }
        return list;
    }

    public ArrayList<Allocation> getAllocationsByEmergencyId(int emergencyId) {
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
        } catch (SQLException e) {
            System.err.println("Error getting allocations by emergency id: " + e.getMessage());
        }
        return list;
    }

    public ArrayList<Allocation> getAllocationsByResourceId(int resourceId) {
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
        } catch (SQLException e) {
            System.err.println("Error getting allocations by resource id: " + e.getMessage());
        }
        return list;
    }

    public ArrayList<Allocation> getActiveAllocations() {
        ArrayList<Allocation> list = new ArrayList<>();
        String sql = "SELECT allocation_id, emergency_id, resource_id, assigned_at, released_at, active " +
                     "FROM allocations WHERE active = 1 ORDER BY allocation_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAllocation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting active allocations: " + e.getMessage());
        }
        return list;
    }

    public boolean updateAllocation(Allocation allocation) {
        if (allocation == null) return false;
        String sql = "UPDATE allocations SET emergency_id = ?, resource_id = ?, assigned_at = ?, released_at = ?, active = ? " +
                     "WHERE allocation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, allocation.getEmergencyId());
            ps.setInt(2, allocation.getResourceId());
            ps.setTimestamp(3, Timestamp.valueOf(allocation.getAssignedAt() != null ? allocation.getAssignedAt() : LocalDateTime.now()));
            if (allocation.getReleasedAt() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(allocation.getReleasedAt()));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }
            ps.setBoolean(5, allocation.isActive());
            ps.setInt(6, allocation.getAllocationId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating allocation: " + e.getMessage());
            return false;
        }
    }

    public boolean removeAllocation(int allocationId) {
        String sql = "DELETE FROM allocations WHERE allocation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, allocationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing allocation: " + e.getMessage());
            return false;
        }
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
