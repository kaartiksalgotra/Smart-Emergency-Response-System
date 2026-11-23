package com.emergency.service;

import com.emergency.model.Emergency;
import com.emergency.model.Resource;
import com.emergency.enums.EmergencyStatus;
import com.emergency.enums.ResourceStatus;
import com.emergency.storage.EmergencyStorage;
import com.emergency.storage.ResourceStorage;

import java.util.ArrayList;

public class DashboardService {

    private EmergencyStorage emergencyStorage;
    private ResourceStorage resourceStorage;

    public DashboardService(EmergencyStorage emergencyStorage, ResourceStorage resourceStorage) {
        this.emergencyStorage = emergencyStorage;
        this.resourceStorage = resourceStorage;
    }

    public int getTotalEmergencies() {
        return emergencyStorage.getAllEmergencies().size();
    }

    public int getRequestedEmergencies() {
        return getEmergencyCountByStatus(EmergencyStatus.REQUESTED);
    }

    public int getAcceptedEmergencies() {
        return getEmergencyCountByStatus(EmergencyStatus.ACCEPTED);
    }

    public int getRejectedEmergencies() {
        return getEmergencyCountByStatus(EmergencyStatus.REJECTED);
    }

    public int getAssignedEmergencies() {
        return getEmergencyCountByStatus(EmergencyStatus.ASSIGNED);
    }

    public int getInProgressEmergencies() {
        return getEmergencyCountByStatus(EmergencyStatus.IN_PROGRESS);
    }

    public int getResolvedEmergencies() {
        return getEmergencyCountByStatus(EmergencyStatus.RESOLVED);
    }

    public int getCancelledEmergencies() {
        return getEmergencyCountByStatus(EmergencyStatus.CANCELLED);
    }

    // Backward-compatible for pending counters
    public int getPendingEmergencies() {
        return getRequestedEmergencies() + getAcceptedEmergencies();
    }

    private int getEmergencyCountByStatus(EmergencyStatus status) {
        int count = 0;
        for (Emergency em : emergencyStorage.getAllEmergencies()) {
            if (em.getStatus() == status)
                count++;
        }
        return count;
    }

    public int getTotalResources() {
        return resourceStorage.getAllResources().size();
    }

    public int getAvailableResources() {
        return getResourceCountByStatus(ResourceStatus.AVAILABLE);
    }

    public int getAllocatedResources() {
        return getResourceCountByStatus(ResourceStatus.ALLOCATED);
    }

    public int getUnavailableResources() {
        return getResourceCountByStatus(ResourceStatus.UNAVAILABLE);
    }

    private int getResourceCountByStatus(ResourceStatus status) {
        int count = 0;
        for (Resource res : resourceStorage.getAllResources()) {
            if (res.getResourceStatus() == status)
                count++;
        }
        return count;
    }
}
