package com.emergency.model;

import com.emergency.enums.EmergencyType;
import com.emergency.enums.PriorityLevel;
import com.emergency.enums.EmergencyStatus;

import java.time.LocalDateTime;

/**
 * Model representing an emergency incident request.
 */
public class Emergency {

    private int emergencyId;
    private int userId;
    private EmergencyType emergencyType;
    private PriorityLevel priority;
    private String location;
    private String description;
    private int affectedPeople;
    private EmergencyStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Emergency(int emergencyId, int userId, EmergencyType emergencyType,
                     PriorityLevel priority, String location, String description,
                     int affectedPeople) {

        this.emergencyId = emergencyId;
        this.userId = userId;
        this.emergencyType = emergencyType;
        this.priority = priority;
        this.location = location;
        this.description = description;
        this.affectedPeople = affectedPeople;
        this.status = EmergencyStatus.REQUESTED; // Initially set to REQUESTED
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public int getEmergencyId() { return emergencyId; }
    public void setEmergencyId(int emergencyId) { this.emergencyId = emergencyId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public EmergencyType getEmergencyType() { return emergencyType; }
    public void setEmergencyType(EmergencyType emergencyType) {
        this.emergencyType = emergencyType;
        this.updatedAt = LocalDateTime.now();
    }

    public PriorityLevel getPriority() { return priority; }
    public void setPriority(PriorityLevel priority) {
        this.priority = priority;
        this.updatedAt = LocalDateTime.now();
    }

    public String getLocation() { return location; }
    public void setLocation(String location) {
        this.location = location;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public int getAffectedPeople() { return affectedPeople; }
    public void setAffectedPeople(int affectedPeople) {
        this.affectedPeople = affectedPeople;
        this.updatedAt = LocalDateTime.now();
    }

    public EmergencyStatus getStatus() { return status; }
    public void setStatus(EmergencyStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
