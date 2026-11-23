
package com.emergency.storage;

import com.emergency.model.Emergency;
import java.util.ArrayList;

public class EmergencyStorage {

    private ArrayList<Emergency> emergencies;
    private int nextEmergencyId;

    public EmergencyStorage() {
        emergencies = new ArrayList<>();
        nextEmergencyId = 1;
    }

    public int generateEmergencyId() {
        return nextEmergencyId++;
    }

    public void addEmergency(Emergency emergency) {
        emergencies.add(emergency);
    }

    public Emergency findEmergencyById(int emergencyId) {

        for (Emergency emergency : emergencies) {

            if (emergency.getEmergencyId() == emergencyId) {
                return emergency;
            }
        }

        return null;
    }

    public ArrayList<Emergency> getAllEmergencies() {
        return new ArrayList<>(emergencies);
    }

    public ArrayList<Emergency> getEmergenciesByUserId(int userId) {

        ArrayList<Emergency> userEmergencies = new ArrayList<>();

        for (Emergency emergency : emergencies) {

            if (emergency.getUserId() == userId) {
                userEmergencies.add(emergency);
            }
        }

        return userEmergencies;
    }

    public boolean updateEmergency(Emergency updatedEmergency) {

        Emergency emergency = findEmergencyById(updatedEmergency.getEmergencyId());

        if (emergency != null) {
            int index = emergencies.indexOf(emergency);
            emergencies.set(index, updatedEmergency);
            return true;
        }

        return false;
    }

    public boolean removeEmergency(int emergencyId) {

        Emergency emergency = findEmergencyById(emergencyId);

        if (emergency != null) {
            emergencies.remove(emergency);
            return true;
        }

        return false;
    }
}
