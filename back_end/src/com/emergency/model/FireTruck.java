package com.emergency.model;

import com.emergency.enums.ResourceType;
import com.emergency.enums.ResourceStatus;

public class FireTruck extends Resource {

    private int waterCapacity;

    public FireTruck(int resourceId, String resourceName,
            ResourceStatus resourceStatus, String contactNumber,
            String location, int waterCapacity) {

        super(resourceId, resourceName, ResourceType.FIRE_TRUCK,
                resourceStatus, contactNumber, location);

        this.waterCapacity = waterCapacity;
    }

    public int getWaterCapacity() {
        return waterCapacity;
    }

    public void setWaterCapacity(int waterCapacity) {
        this.waterCapacity = waterCapacity;
    }

    @Override
    public String getResourceDetails() {
        return "Fire Truck: " + getResourceName()
                + ", Water Capacity: " + waterCapacity + " Litres"
                + ", Status: " + getResourceStatus();
    }
}