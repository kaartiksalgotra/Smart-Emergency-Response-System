
package com.emergency.service;

import com.emergency.model.Resource;
import com.emergency.enums.ResourceStatus;
import com.emergency.storage.ResourceStorage;
import com.emergency.util.InputValidator;

import java.util.ArrayList;

public class ResourceService {

    private ResourceStorage resourceStorage;

    public ResourceService(ResourceStorage resourceStorage) {
        this.resourceStorage = resourceStorage;
    }

    public void addResource(Resource resource) {

        if (resource == null) {
            throw new IllegalArgumentException("Resource cannot be empty");
        }

        if (!InputValidator.isValidName(resource.getResourceName())) {
            throw new IllegalArgumentException("Invalid resource name");
        }

        if (!InputValidator.isValidPhone(resource.getContactNumber())) {
            throw new IllegalArgumentException("Invalid contact number");
        }

        if (!InputValidator.isValidName(resource.getLocation())) {
            throw new IllegalArgumentException("Invalid resource location");
        }

        if (resource.getResourceType() == null) {
            throw new IllegalArgumentException("Resource type is required");
        }

        int resourceId = resourceStorage.generateResourceId();

        resource.setResourceId(resourceId);
        resource.setResourceStatus(ResourceStatus.AVAILABLE);

        resourceStorage.addResource(resource);
    }

    public Resource getResourceById(int resourceId) {
        return resourceStorage.findResourceById(resourceId);
    }

    public ArrayList<Resource> getAllResources() {
        return resourceStorage.getAllResources();
    }

    public ArrayList<Resource> getAvailableResources() {
        return resourceStorage.getAvailableResources();
    }

    public boolean updateResource(Resource updatedResource) {

        if (updatedResource == null) {
            return false;
        }

        Resource existingResource = resourceStorage.findResourceById(
                updatedResource.getResourceId());

        if (existingResource == null) {
            return false;
        }

        if (!InputValidator.isValidName(updatedResource.getResourceName())) {
            return false;
        }

        if (!InputValidator.isValidPhone(updatedResource.getContactNumber())) {
            return false;
        }

        if (!InputValidator.isValidName(updatedResource.getLocation())) {
            return false;
        }

        if (updatedResource.getResourceType() != existingResource.getResourceType()) {
            return false;
        }

        if (updatedResource.getResourceStatus() == null) {
            return false;
        }

        if (existingResource.getResourceStatus() == ResourceStatus.ALLOCATED
                && updatedResource.getResourceStatus() != ResourceStatus.ALLOCATED) {
            return false;
        }

        return resourceStorage.updateResource(updatedResource);
    }

    public boolean removeResource(int resourceId) {

        Resource resource = resourceStorage.findResourceById(resourceId);

        if (resource == null) {
            return false;
        }

        if (resource.getResourceStatus() == ResourceStatus.ALLOCATED) {
            return false;
        }

        return resourceStorage.removeResource(resourceId);
    }
}
