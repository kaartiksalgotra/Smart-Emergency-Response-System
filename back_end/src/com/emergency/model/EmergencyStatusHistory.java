package com.emergency.model;

import com.emergency.enums.EmergencyStatus;
import java.time.LocalDateTime;

/**
 * Model representing a historical emergency status transition.
 */
public class EmergencyStatusHistory {

    private int historyId;
    private int emergencyId;
    private EmergencyStatus previousStatus;
    private EmergencyStatus newStatus;
    private LocalDateTime changedAt;

    public EmergencyStatusHistory(int historyId, int emergencyId, EmergencyStatus previousStatus,
                                  EmergencyStatus newStatus, LocalDateTime changedAt) {
        this.historyId = historyId;
        this.emergencyId = emergencyId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedAt = changedAt != null ? changedAt : LocalDateTime.now();
    }

    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

    public int getEmergencyId() {
        return emergencyId;
    }

    public void setEmergencyId(int emergencyId) {
        this.emergencyId = emergencyId;
    }

    public EmergencyStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(EmergencyStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public EmergencyStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(EmergencyStatus newStatus) {
        this.newStatus = newStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    @Override
    public String toString() {
        return "EmergencyStatusHistory{" +
                "historyId=" + historyId +
                ", emergencyId=" + emergencyId +
                ", previousStatus=" + previousStatus +
                ", newStatus=" + newStatus +
                ", changedAt=" + changedAt +
                '}';
    }
}
