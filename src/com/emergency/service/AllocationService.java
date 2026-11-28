package com.emergency.service;

import com.emergency.model.Emergency;
import com.emergency.model.Resource;
import com.emergency.model.Allocation;
import com.emergency.enums.EmergencyType;
import com.emergency.enums.EmergencyStatus;
import com.emergency.enums.ResourceType;
import com.emergency.enums.ResourceStatus;
import com.emergency.storage.EmergencyStorage;
import com.emergency.storage.ResourceStorage;
import com.emergency.storage.AllocationStorage;

import java.util.ArrayList;

/**
 * Service for allocating resources to ACCEPTED emergency requests.
 */
public class AllocationService {

    private EmergencyStorage emergencyStorage;
    private ResourceStorage resourceStorage;
    private AllocationStorage allocationStorage;

    public AllocationService(EmergencyStorage emergencyStorage,
                             ResourceStorage resourceStorage,
                             AllocationStorage allocationStorage) {
        this.emergencyStorage = emergencyStorage;
        this.resourceStorage = resourceStorage;
        this.allocationStorage = allocationStorage;
    }

    public synchronized boolean allocateResource(int emergencyId) {
        Emergency emergency = emergencyStorage.findEmergencyById(emergencyId);
        if (emergency == null || emergency.getStatus() != EmergencyStatus.ACCEPTED) {
            return false;
        }

        if (hasActiveAllocation(emergencyId)) {
            return false;
        }

        ResourceType requiredType = getRequiredResourceType(emergency.getEmergencyType());
        if (requiredType == null) return false;

        ArrayList<Resource> resources = resourceStorage.getAvailableResources();
        for (Resource resource : resources) {
            if (resource.getResourceType() == requiredType) {
                assignResource(emergency, resource);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean manualAllocation(int emergencyId, int resourceId) {
        Emergency emergency = emergencyStorage.findEmergencyById(emergencyId);
        Resource resource = resourceStorage.findResourceById(resourceId);

        if (emergency == null || resource == null) return false;
        if (emergency.getStatus() != EmergencyStatus.ACCEPTED) return false;
        if (resource.getResourceStatus() != ResourceStatus.AVAILABLE) return false;
        if (hasActiveAllocation(emergencyId)) return false;

        ResourceType requiredType = getRequiredResourceType(emergency.getEmergencyType());
        if (resource.getResourceType() != requiredType && emergency.getEmergencyType() != EmergencyType.OTHER) {
            return false;
        }

        assignResource(emergency, resource);
        return true;
    }

    private boolean hasActiveAllocation(int emergencyId) {
        ArrayList<Allocation> allocations = allocationStorage.getAllocationsByEmergencyId(emergencyId);
        for (Allocation alloc : allocations) {
            if (alloc.isActive()) return true;
        }
        return false;
    }

    private void assignResource(Emergency emergency, Resource resource) {
        int allocationId = allocationStorage.generateAllocationId();
        Allocation allocation = new Allocation(allocationId, emergency.getEmergencyId(), resource.getResourceId());
        allocationStorage.addAllocation(allocation);

        resource.setResourceStatus(ResourceStatus.ALLOCATED);
        resourceStorage.updateResource(resource);

        emergency.setStatus(EmergencyStatus.ASSIGNED);
        emergencyStorage.updateEmergency(emergency);
    }

    public ResourceType getRequiredResourceType(EmergencyType emergencyType) {
        if (emergencyType == EmergencyType.MEDICAL) return ResourceType.AMBULANCE;
        if (emergencyType == EmergencyType.FIRE) return ResourceType.FIRE_TRUCK;
        if (emergencyType == EmergencyType.RESCUE) return ResourceType.RESCUE_TEAM;
        return ResourceType.RESCUE_TEAM; // default for other
    }

    public synchronized boolean releaseResource(int emergencyId) {
        ArrayList<Allocation> allocations = allocationStorage.getAllocationsByEmergencyId(emergencyId);
        for (Allocation allocation : allocations) {
            if (allocation.isActive()) {
                Resource resource = resourceStorage.findResourceById(allocation.getResourceId());
                if (resource != null) {
                    resource.setResourceStatus(ResourceStatus.AVAILABLE);
                    resourceStorage.updateResource(resource);
                }
                allocation.releaseAllocation();
                allocationStorage.updateAllocation(allocation);
                return true;
            }
        }
        return false;
    }

    public ArrayList<Allocation> getAllAllocations() { return allocationStorage.getAllAllocations(); }
    public ArrayList<Allocation> getActiveAllocations() { return allocationStorage.getActiveAllocations(); }
}
