
package com.emergency.storage;

import com.emergency.model.Resource;
import com.emergency.enums.ResourceStatus;
import java.util.ArrayList;

public class ResourceStorage {

    private ArrayList<Resource> resources;
    private int nextResourceId;

    public ResourceStorage() {
        resources = new ArrayList<>();
        nextResourceId = 1;
    }

    public int generateResourceId() {
        return nextResourceId++;
    }

    public void addResource(Resource resource) {
        resources.add(resource);
    }

    public Resource findResourceById(int resourceId) {

        for (Resource resource : resources) {

            if (resource.getResourceId() == resourceId) {
                return resource;
            }
        }

        return null;
    }

    public ArrayList<Resource> getAllResources() {
        return new ArrayList<>(resources);
    }

    public ArrayList<Resource> getAvailableResources() {

        ArrayList<Resource> availableResources = new ArrayList<>();

        for (Resource resource : resources) {

            if (resource.getResourceStatus() == ResourceStatus.AVAILABLE) {
                availableResources.add(resource);
            }
        }

        return availableResources;
    }

    public boolean updateResource(Resource updatedResource) {

        Resource resource = findResourceById(updatedResource.getResourceId());

        if (resource != null) {
            int index = resources.indexOf(resource);
            resources.set(index, updatedResource);
            return true;
        }

        return false;
    }

    public boolean removeResource(int resourceId) {

        Resource resource = findResourceById(resourceId);

        if (resource != null) {
            resources.remove(resource);
            return true;
        }

        return false;
    }
}
