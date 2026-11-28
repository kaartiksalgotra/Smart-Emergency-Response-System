package com.emergency.model;

import com.emergency.enums.ResourceType;
import com.emergency.enums.ResourceStatus;

public class RescueTeam extends Resource {

    private int teamMembers;

    public RescueTeam(int resourceId, String resourceName,
            ResourceStatus resourceStatus, String contactNumber,
            String location, int teamMembers) {

        super(resourceId, resourceName, ResourceType.RESCUE_TEAM,
                resourceStatus, contactNumber, location);

        this.teamMembers = teamMembers;
    }

    public int getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(int teamMembers) {
        this.teamMembers = teamMembers;
    }

    @Override
    public String getResourceDetails() {
        return "Rescue Team: " + getResourceName()
                + ", Team Members: " + teamMembers
                + ", Status: " + getResourceStatus();
    }
}