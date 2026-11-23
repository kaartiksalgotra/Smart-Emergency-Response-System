package com.emergency.model;

import com.emergency.enums.ResourceType;
import com.emergency.enums.ResourceStatus;

public class Ambulance extends Resource {

    private int patientCapacity;

    public Ambulance(int resourceId, String resourceName,
            ResourceStatus resourceStatus, String contactNumber,
            String location, int patientCapacity) {

        super(resourceId, resourceName, ResourceType.AMBULANCE,
                resourceStatus, contactNumber, location);

        this.patientCapacity = patientCapacity;
    }

    public int getPatientCapacity() {
        return patientCapacity;
    }

    public void setPatientCapacity(int patientCapacity) {
        this.patientCapacity = patientCapacity;
    }

    @Override
    public String getResourceDetails() {
        return "Ambulance: " + getResourceName()
                + ", Patient Capacity: " + patientCapacity
                + ", Status: " + getResourceStatus();
    }
}