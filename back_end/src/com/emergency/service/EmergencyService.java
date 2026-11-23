package com.emergency.service;

import com.emergency.model.Emergency;
import com.emergency.model.User;
import com.emergency.enums.EmergencyType;
import com.emergency.enums.PriorityLevel;
import com.emergency.enums.EmergencyStatus;
import com.emergency.storage.EmergencyStorage;
import com.emergency.util.InputValidator;

import java.util.ArrayList;

public class EmergencyService {

    private EmergencyStorage emergencyStorage;

    public EmergencyService(EmergencyStorage emergencyStorage) {
        this.emergencyStorage = emergencyStorage;
    }

    public Emergency reportEmergency(User user, EmergencyType type, PriorityLevel priority,
                                    String location, String desc, int affectedPeople) {
        if (user == null || type == null || priority == null) {
            throw new IllegalArgumentException("User, emergency type, and priority are required.");
        }
        if (!InputValidator.isValidName(location)) {
            throw new IllegalArgumentException("Invalid emergency location.");
        }
        if (!InputValidator.isValidDescription(desc)) {
            throw new IllegalArgumentException("Emergency description is required.");
        }
        if (!InputValidator.isValidAffectedPeople(affectedPeople)) {
            throw new IllegalArgumentException("Affected people must be at least 1.");
        }

        int emergencyId = emergencyStorage.generateEmergencyId();
        Emergency emergency = new Emergency(emergencyId, user.getUserId(), type, priority, location, desc, affectedPeople);
        emergencyStorage.addEmergency(emergency);
        return emergency;
    }

    public boolean acceptEmergency(int emergencyId) {
        Emergency emergency = emergencyStorage.findEmergencyById(emergencyId);
        if (emergency == null || emergency.getStatus() != EmergencyStatus.REQUESTED) {
            return false;
        }
        emergency.setStatus(EmergencyStatus.ACCEPTED);
        return emergencyStorage.updateEmergency(emergency);
    }

    public boolean rejectEmergency(int emergencyId) {
        Emergency emergency = emergencyStorage.findEmergencyById(emergencyId);
        if (emergency == null || emergency.getStatus() != EmergencyStatus.REQUESTED) {
            return false;
        }
        emergency.setStatus(EmergencyStatus.REJECTED);
        return emergencyStorage.updateEmergency(emergency);
    }

    public ArrayList<Emergency> getRequestedEmergencies() {
        ArrayList<Emergency> list = new ArrayList<>();
        for (Emergency em : emergencyStorage.getAllEmergencies()) {
            if (em.getStatus() == EmergencyStatus.REQUESTED) list.add(em);
        }
        return list;
    }

    public ArrayList<Emergency> getAcceptedEmergencies() {
        ArrayList<Emergency> list = new ArrayList<>();
        for (Emergency em : emergencyStorage.getAllEmergencies()) {
            if (em.getStatus() == EmergencyStatus.ACCEPTED) list.add(em);
        }
        return list;
    }

    public Emergency getEmergencyById(int id) {
        return emergencyStorage.findEmergencyById(id);
    }

    public ArrayList<Emergency> getAllEmergencies() {
        return emergencyStorage.getAllEmergencies();
    }

    public ArrayList<Emergency> getEmergenciesByUserId(int userId) {
        return emergencyStorage.getEmergenciesByUserId(userId);
    }

    public boolean updateEmergencyStatus(int emergencyId, EmergencyStatus status) {
        Emergency emergency = emergencyStorage.findEmergencyById(emergencyId);
        if (emergency == null || status == null) return false;
        if (emergency.getStatus() == EmergencyStatus.RESOLVED ||
            emergency.getStatus() == EmergencyStatus.CANCELLED ||
            emergency.getStatus() == EmergencyStatus.REJECTED) {
            return false;
        }
        emergency.setStatus(status);
        return emergencyStorage.updateEmergency(emergency);
    }

    public boolean cancelEmergency(int emergencyId, int userId) {
        Emergency emergency = emergencyStorage.findEmergencyById(emergencyId);
        if (emergency == null || emergency.getUserId() != userId) return false;
        if (emergency.getStatus() == EmergencyStatus.RESOLVED || emergency.getStatus() == EmergencyStatus.REJECTED) {
            return false;
        }
        emergency.setStatus(EmergencyStatus.CANCELLED);
        return emergencyStorage.updateEmergency(emergency);
    }

    public ArrayList<com.emergency.model.EmergencyStatusHistory> getStatusHistoryByEmergencyId(int emergencyId) {
        return emergencyStorage.getStatusHistoryByEmergencyId(emergencyId);
    }

    public ArrayList<com.emergency.model.EmergencyStatusHistory> getAllStatusHistory() {
        return emergencyStorage.getAllStatusHistory();
    }
}
