package com.emergency.model;

import java.time.LocalDateTime;

public class Allocation {

    private int allocationId;
    private int emergencyId;
    private int resourceId;
    private LocalDateTime assignedAt;
    private LocalDateTime releasedAt;
    private boolean active;

    public Allocation(int allocationId, int emergencyId, int resourceId) {
        this.allocationId = allocationId;
        this.emergencyId = emergencyId;
        this.resourceId = resourceId;
        this.assignedAt = LocalDateTime.now();
        this.releasedAt = null;
        this.active = true;
    }

    public int getAllocationId() { return allocationId; }
    public void setAllocationId(int allocationId) { this.allocationId = allocationId; }

    public int getEmergencyId() { return emergencyId; }
    public void setEmergencyId(int emergencyId) { this.emergencyId = emergencyId; }

    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public LocalDateTime getAllocatedAt() { return assignedAt; }

    public LocalDateTime getReleasedAt() { return releasedAt; }
    public boolean isActive() { return active; }

    public void releaseAllocation() {
        this.active = false;
        this.releasedAt = LocalDateTime.now();
    }
}
