package com.emergency.controller;

import com.emergency.enums.ResourceStatus;
import com.emergency.enums.ResourceType;
import com.emergency.model.Ambulance;
import com.emergency.model.FireTruck;
import com.emergency.model.RescueTeam;
import com.emergency.model.Resource;
import com.emergency.service.ResourceService;
import com.emergency.views.ResourceManagementView;
import javafx.collections.FXCollections;

/**
 * Controller for Resource Management.
 * Manages fleet inventory, prevents modification of allocated units, and updates view tables.
 */
public class ResourceManagementController {

    private ResourceManagementView resourceView;
    private ResourceService resourceService;
    private Runnable backAction;

    public ResourceManagementController(
            ResourceManagementView resourceView,
            ResourceService resourceService,
            Runnable backAction) {

        this.resourceView = resourceView;
        this.resourceService = resourceService;
        this.backAction = backAction;

        resourceView.getAddButton().setOnAction(e -> addResource());
        resourceView.getUpdateButton().setOnAction(e -> updateResource());
        resourceView.getRemoveButton().setOnAction(e -> removeResource());
        resourceView.getRefreshButton().setOnAction(e -> loadResources());
        resourceView.getBackButton().setOnAction(e -> backAction.run());

        resourceView.getResourceTable().getSelectionModel().selectedItemProperty().addListener((o, oldV, newV) -> {
            if (newV != null) fillForm(newV);
        });

        loadResources();
    }

    private void addResource() {
        try {
            String name = resourceView.getNameField().getText().trim();
            String contact = resourceView.getContactField().getText().trim();
            String location = resourceView.getLocationField().getText().trim();
            String details = resourceView.getDetailsField().getText().trim();
            String typeStr = resourceView.getTypeBox().getValue();

            if (name.isEmpty() || contact.isEmpty() || location.isEmpty() || details.isEmpty() || typeStr == null) {
                resourceView.getMessageLabel().setText("Please fill all resource fields.");
                return;
            }

            int capacity = Integer.parseInt(details);
            if (capacity <= 0) {
                resourceView.getMessageLabel().setText("Capacity must be greater than zero.");
                return;
            }

            ResourceType type = ResourceType.valueOf(typeStr);
            Resource resource;
            if (type == ResourceType.AMBULANCE) {
                resource = new Ambulance(0, name, ResourceStatus.AVAILABLE, contact, location, capacity);
            } else if (type == ResourceType.FIRE_TRUCK) {
                resource = new FireTruck(0, name, ResourceStatus.AVAILABLE, contact, location, capacity);
            } else {
                resource = new RescueTeam(0, name, ResourceStatus.AVAILABLE, contact, location, capacity);
            }

            resourceService.addResource(resource);
            resourceView.getMessageLabel().setText("Unit '" + name + "' added successfully!");
            clearForm();
            loadResources();

        } catch (NumberFormatException e) {
            resourceView.getMessageLabel().setText("Please enter a valid number for capacity.");
        } catch (Exception e) {
            resourceView.getMessageLabel().setText(e.getMessage());
        }
    }

    private void updateResource() {
        Resource selected = resourceView.getResourceTable().getSelectionModel().getSelectedItem();
        if (selected == null) {
            resourceView.getMessageLabel().setText("Please select a resource from the table.");
            return;
        }

        if (selected.getResourceStatus() == ResourceStatus.ALLOCATED) {
            resourceView.getMessageLabel().setText("Cannot modify an actively allocated resource.");
            return;
        }

        try {
            String name = resourceView.getNameField().getText().trim();
            String contact = resourceView.getContactField().getText().trim();
            String location = resourceView.getLocationField().getText().trim();
            int capacity = Integer.parseInt(resourceView.getDetailsField().getText().trim());

            if (name.isEmpty() || contact.isEmpty() || location.isEmpty()) {
                resourceView.getMessageLabel().setText("Please fill all fields.");
                return;
            }

            selected.setResourceName(name);
            selected.setContactNumber(contact);
            selected.setLocation(location);

            if (selected instanceof Ambulance) ((Ambulance) selected).setPatientCapacity(capacity);
            else if (selected instanceof FireTruck) ((FireTruck) selected).setWaterCapacity(capacity);
            else if (selected instanceof RescueTeam) ((RescueTeam) selected).setTeamMembers(capacity);

            resourceService.updateResource(selected);
            resourceView.getMessageLabel().setText("Unit '" + name + "' updated successfully!");
            clearForm();
            loadResources();

        } catch (Exception e) {
            resourceView.getMessageLabel().setText("Error updating resource: " + e.getMessage());
        }
    }

    private void removeResource() {
        Resource selected = resourceView.getResourceTable().getSelectionModel().getSelectedItem();
        if (selected == null) {
            resourceView.getMessageLabel().setText("Please select a resource to remove.");
            return;
        }

        if (selected.getResourceStatus() == ResourceStatus.ALLOCATED) {
            resourceView.getMessageLabel().setText("Cannot remove an actively allocated resource.");
            return;
        }

        resourceService.removeResource(selected.getResourceId());
        resourceView.getMessageLabel().setText("Resource removed successfully!");
        clearForm();
        loadResources();
    }

    private void loadResources() {
        resourceView.getResourceTable().setItems(
                FXCollections.observableArrayList(resourceService.getAllResources())
        );
    }

    private void fillForm(Resource res) {
        resourceView.getNameField().setText(res.getResourceName());
        resourceView.getContactField().setText(res.getContactNumber());
        resourceView.getLocationField().setText(res.getLocation());
        resourceView.getTypeBox().setValue(res.getResourceType().toString());

        if (res instanceof Ambulance) {
            resourceView.getDetailsField().setText(String.valueOf(((Ambulance) res).getPatientCapacity()));
        } else if (res instanceof FireTruck) {
            resourceView.getDetailsField().setText(String.valueOf(((FireTruck) res).getWaterCapacity()));
        } else if (res instanceof RescueTeam) {
            resourceView.getDetailsField().setText(String.valueOf(((RescueTeam) res).getTeamMembers()));
        }
    }

    private void clearForm() {
        resourceView.getNameField().clear();
        resourceView.getContactField().clear();
        resourceView.getLocationField().clear();
        resourceView.getDetailsField().clear();
        resourceView.getTypeBox().setValue(null);
        resourceView.getResourceTable().getSelectionModel().clearSelection();
    }
}
