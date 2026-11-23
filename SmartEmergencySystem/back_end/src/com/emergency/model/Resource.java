package com.emergency.model;

import com.emergency.enums.ResourceType;
import com.emergency.enums.ResourceStatus;

public abstract class Resource {

    private int resourceId;
    private String resourceName;
    private ResourceType resourceType;
    private ResourceStatus resourceStatus;
    private String contactNumber;
    private String location;

    public Resource(int resourceId, String resourceName,
            ResourceType resourceType, ResourceStatus resourceStatus,
            String contactNumber, String location) {

        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.resourceStatus = resourceStatus;
        this.contactNumber = contactNumber;
        this.location = location;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public ResourceStatus getResourceStatus() {
        return resourceStatus;
    }

    public void setResourceStatus(ResourceStatus resourceStatus) {
        this.resourceStatus = resourceStatus;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public abstract String getResourceDetails();
}