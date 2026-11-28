package com.emergency.storage;

import com.emergency.enums.ResourceStatus;
import com.emergency.enums.ResourceType;
import com.emergency.model.Ambulance;
import com.emergency.model.FireTruck;
import com.emergency.model.RescueTeam;
import com.emergency.model.Resource;
import com.emergency.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Storage class for managing emergency Resources via MySQL JDBC with in-memory
 * fallback.
 */
public final class ResourceStorage {

    private final List<Resource> memoryResources = Collections.synchronizedList(new ArrayList<>());
    private int nextResourceId = 1;

    public ResourceStorage() {
        if (DatabaseConnection.isAvailable()) {
            seedDefaultResources();
        } else {
            seedMemoryResources();
        }
    }

    private void seedMemoryResources() {
        if (memoryResources.isEmpty()) {
            memoryResources.add(new Ambulance(1, "City Ambulance Alpha", ResourceStatus.AVAILABLE, "108-1001",
                    "City Center Hospital", 2));
            memoryResources.add(new Ambulance(2, "Metro Ambulance Beta", ResourceStatus.AVAILABLE, "108-1002",
                    "Northside Medical Center", 2));
            memoryResources.add(new FireTruck(3, "Fire Engine 101", ResourceStatus.AVAILABLE, "101-2001",
                    "Downtown Fire Station", 5000));
            memoryResources.add(new FireTruck(4, "Heavy Water Tender 102", ResourceStatus.AVAILABLE, "101-2002",
                    "East District Fire Post", 8000));
            memoryResources.add(new RescueTeam(5, "Disaster Response Squad 1", ResourceStatus.AVAILABLE, "112-3001",
                    "Central Emergency Hub", 8));
            memoryResources.add(new RescueTeam(6, "Rapid Rescue Unit 2", ResourceStatus.AVAILABLE, "112-3002",
                    "South Rescue Station", 6));
            nextResourceId = 7;
        }
    }

    public final void seedDefaultResources() {
        String countSql = "SELECT COUNT(*) FROM resources";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(countSql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next() && rs.getInt(1) == 0) {
                addResource(new Ambulance(0, "City Ambulance Alpha", ResourceStatus.AVAILABLE, "108-1001",
                        "City Center Hospital", 2));
                addResource(new Ambulance(0, "Metro Ambulance Beta", ResourceStatus.AVAILABLE, "108-1002",
                        "Northside Medical Center", 2));
                addResource(new FireTruck(0, "Fire Engine 101", ResourceStatus.AVAILABLE, "101-2001",
                        "Downtown Fire Station", 5000));
                addResource(new FireTruck(0, "Heavy Water Tender 102", ResourceStatus.AVAILABLE, "101-2002",
                        "East District Fire Post", 8000));
                addResource(new RescueTeam(0, "Disaster Response Squad 1", ResourceStatus.AVAILABLE, "112-3001",
                        "Central Emergency Hub", 8));
                addResource(new RescueTeam(0, "Rapid Rescue Unit 2", ResourceStatus.AVAILABLE, "112-3002",
                        "South Rescue Station", 6));
            }
        } catch (SQLException e) {
            System.err.println("Database unavailable for resources, switching to in-memory store: " + e.getMessage());
            seedMemoryResources();
        }
    }

    public synchronized int generateResourceId() {
        if (DatabaseConnection.isAvailable()) {
            String sql = "SELECT COALESCE(MAX(resource_id), 0) + 1 FROM resources";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException ignored) {
            }
        }
        return nextResourceId++;
    }

    public final synchronized void addResource(Resource resource) {
        if (resource == null)
            return;

        if (resource.getResourceId() <= 0) {
            resource.setResourceId(generateResourceId());
        }

        synchronized (memoryResources) {
            boolean exists = false;
            for (int i = 0; i < memoryResources.size(); i++) {
                if (memoryResources.get(i).getResourceId() == resource.getResourceId()) {
                    memoryResources.set(i, resource);
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                memoryResources.add(resource);
            }
        }

        if (DatabaseConnection.isAvailable()) {
            String sql = "INSERT INTO resources (resource_name, resource_type, resource_status, contact_number, location, "
                    +
                    "patient_capacity, water_capacity, team_members) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, resource.getResourceName());
                ps.setString(2, resource.getResourceType().name());
                ps.setString(3, resource.getResourceStatus().name());
                ps.setString(4, resource.getContactNumber());
                ps.setString(5, resource.getLocation());

                if (resource instanceof Ambulance) {
                    ps.setInt(6, ((Ambulance) resource).getPatientCapacity());
                    ps.setNull(7, Types.INTEGER);
                    ps.setNull(8, Types.INTEGER);
                } else if (resource instanceof FireTruck) {
                    ps.setNull(6, Types.INTEGER);
                    ps.setInt(7, ((FireTruck) resource).getWaterCapacity());
                    ps.setNull(8, Types.INTEGER);
                } else if (resource instanceof RescueTeam) {
                    ps.setNull(6, Types.INTEGER);
                    ps.setNull(7, Types.INTEGER);
                    ps.setInt(8, ((RescueTeam) resource).getTeamMembers());
                } else {
                    ps.setNull(6, Types.INTEGER);
                    ps.setNull(7, Types.INTEGER);
                    ps.setNull(8, Types.INTEGER);
                }

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        resource.setResourceId(rs.getInt(1));
                    }
                }
            } catch (SQLException e) {
                System.err.println("Warning: Could not persist resource to database: " + e.getMessage());
            }
        }
    }

    public Resource findResourceById(int resourceId) {
        if (DatabaseConnection.isAvailable()) {
            String sql = "SELECT resource_id, resource_name, resource_type, resource_status, contact_number, location, "
                    +
                    "patient_capacity, water_capacity, team_members FROM resources WHERE resource_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, resourceId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToResource(rs);
                    }
                }
            } catch (SQLException ignored) {
            }
        }

        synchronized (memoryResources) {
            for (Resource r : memoryResources) {
                if (r.getResourceId() == resourceId)
                    return r;
            }
        }
        return null;
    }

    public ArrayList<Resource> getAllResources() {
        if (DatabaseConnection.isAvailable()) {
            ArrayList<Resource> list = new ArrayList<>();
            String sql = "SELECT resource_id, resource_name, resource_type, resource_status, contact_number, location, "
                    +
                    "patient_capacity, water_capacity, team_members FROM resources ORDER BY resource_id ASC";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToResource(rs));
                }
                if (!list.isEmpty())
                    return list;
            } catch (SQLException ignored) {
            }
        }

        synchronized (memoryResources) {
            return new ArrayList<>(memoryResources);
        }
    }

    public ArrayList<Resource> getAvailableResources() {
        if (DatabaseConnection.isAvailable()) {
            ArrayList<Resource> list = new ArrayList<>();
            String sql = "SELECT resource_id, resource_name, resource_type, resource_status, contact_number, location, "
                    +
                    "patient_capacity, water_capacity, team_members FROM resources WHERE resource_status = 'AVAILABLE' ORDER BY resource_id ASC";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToResource(rs));
                }
                if (!list.isEmpty())
                    return list;
            } catch (SQLException ignored) {
            }
        }

        ArrayList<Resource> available = new ArrayList<>();
        synchronized (memoryResources) {
            for (Resource r : memoryResources) {
                if (r.getResourceStatus() == ResourceStatus.AVAILABLE) {
                    available.add(r);
                }
            }
        }
        return available;
    }

    public synchronized boolean updateResource(Resource updatedResource) {
        if (updatedResource == null)
            return false;

        boolean updatedInMemory = false;
        synchronized (memoryResources) {
            for (int i = 0; i < memoryResources.size(); i++) {
                if (memoryResources.get(i).getResourceId() == updatedResource.getResourceId()) {
                    memoryResources.set(i, updatedResource);
                    updatedInMemory = true;
                    break;
                }
            }
        }

        if (DatabaseConnection.isAvailable()) {
            String sql = "UPDATE resources SET resource_name = ?, resource_type = ?, resource_status = ?, contact_number = ?, "
                    +
                    "location = ?, patient_capacity = ?, water_capacity = ?, team_members = ? WHERE resource_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, updatedResource.getResourceName());
                ps.setString(2, updatedResource.getResourceType().name());
                ps.setString(3, updatedResource.getResourceStatus().name());
                ps.setString(4, updatedResource.getContactNumber());
                ps.setString(5, updatedResource.getLocation());

                if (updatedResource instanceof Ambulance) {
                    ps.setInt(6, ((Ambulance) updatedResource).getPatientCapacity());
                    ps.setNull(7, Types.INTEGER);
                    ps.setNull(8, Types.INTEGER);
                } else if (updatedResource instanceof FireTruck) {
                    ps.setNull(6, Types.INTEGER);
                    ps.setInt(7, ((FireTruck) updatedResource).getWaterCapacity());
                    ps.setNull(8, Types.INTEGER);
                } else if (updatedResource instanceof RescueTeam) {
                    ps.setNull(6, Types.INTEGER);
                    ps.setNull(7, Types.INTEGER);
                    ps.setInt(8, ((RescueTeam) updatedResource).getTeamMembers());
                } else {
                    ps.setNull(6, Types.INTEGER);
                    ps.setNull(7, Types.INTEGER);
                    ps.setNull(8, Types.INTEGER);
                }

                ps.setInt(9, updatedResource.getResourceId());
                return ps.executeUpdate() > 0;
            } catch (SQLException ignored) {
            }
        }
        return updatedInMemory;
    }

    public synchronized boolean removeResource(int resourceId) {
        boolean removedFromMemory = false;
        synchronized (memoryResources) {
            removedFromMemory = memoryResources.removeIf(r -> r.getResourceId() == resourceId);
        }

        if (DatabaseConnection.isAvailable()) {
            String sql = "DELETE FROM resources WHERE resource_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, resourceId);
                return ps.executeUpdate() > 0;
            } catch (SQLException ignored) {
            }
        }
        return removedFromMemory;
    }

    private Resource mapResultSetToResource(ResultSet rs) throws SQLException {
        int id = rs.getInt("resource_id");
        String name = rs.getString("resource_name");
        String typeStr = rs.getString("resource_type");
        ResourceType type = ResourceType.valueOf(typeStr);
        String statusStr = rs.getString("resource_status");
        ResourceStatus status = ResourceStatus.valueOf(statusStr);
        String contact = rs.getString("contact_number");
        String location = rs.getString("location");

        if (type == ResourceType.AMBULANCE) {
            int capacity = rs.getInt("patient_capacity");
            return new Ambulance(id, name, status, contact, location, capacity);
        } else if (type == ResourceType.FIRE_TRUCK) {
            int water = rs.getInt("water_capacity");
            return new FireTruck(id, name, status, contact, location, water);
        } else {
            int members = rs.getInt("team_members");
            return new RescueTeam(id, name, status, contact, location, members);
        }
    }
}
