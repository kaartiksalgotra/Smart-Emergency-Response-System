
package com.emergency.storage;

import com.emergency.model.Allocation;
import java.util.ArrayList;

public class AllocationStorage {

    private ArrayList<Allocation> allocations;
    private int nextAllocationId;

    public AllocationStorage() {
        allocations = new ArrayList<>();
        nextAllocationId = 1;
    }

    public int generateAllocationId() {
        return nextAllocationId++;
    }

    public void addAllocation(Allocation allocation) {
        allocations.add(allocation);
    }

    public Allocation findAllocationById(int allocationId) {

        for (Allocation allocation : allocations) {

            if (allocation.getAllocationId() == allocationId) {
                return allocation;
            }
        }

        return null;
    }

    public ArrayList<Allocation> getAllAllocations() {
        return new ArrayList<>(allocations);
    }

    public ArrayList<Allocation> getAllocationsByEmergencyId(int emergencyId) {

        ArrayList<Allocation> emergencyAllocations = new ArrayList<>();

        for (Allocation allocation : allocations) {

            if (allocation.getEmergencyId() == emergencyId) {
                emergencyAllocations.add(allocation);
            }
        }

        return emergencyAllocations;
    }

    public ArrayList<Allocation> getAllocationsByResourceId(int resourceId) {

        ArrayList<Allocation> resourceAllocations = new ArrayList<>();

        for (Allocation allocation : allocations) {

            if (allocation.getResourceId() == resourceId) {
                resourceAllocations.add(allocation);
            }
        }

        return resourceAllocations;
    }

    public ArrayList<Allocation> getActiveAllocations() {

        ArrayList<Allocation> activeAllocations = new ArrayList<>();

        for (Allocation allocation : allocations) {

            if (allocation.isActive()) {
                activeAllocations.add(allocation);
            }
        }

        return activeAllocations;
    }

    public boolean removeAllocation(int allocationId) {

        Allocation allocation = findAllocationById(allocationId);

        if (allocation != null) {
            allocations.remove(allocation);
            return true;
        }

        return false;
    }
}
