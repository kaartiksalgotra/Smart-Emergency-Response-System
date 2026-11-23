package com.emergency.service;

import com.emergency.enums.EmergencyStatus;
import com.emergency.model.Allocation;
import com.emergency.model.Emergency;
import com.emergency.model.Resource;
import com.emergency.storage.AllocationStorage;
import com.emergency.storage.EmergencyStorage;
import com.emergency.storage.ResourceStorage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Service for generating CSV reports of emergencies, resources, and allocations.
 */
public class ReportService {

    private EmergencyStorage emergencyStorage;
    private ResourceStorage resourceStorage;
    private AllocationStorage allocationStorage;

    public ReportService(EmergencyStorage emergencyStorage, ResourceStorage resourceStorage, AllocationStorage allocationStorage) {
        this.emergencyStorage = emergencyStorage;
        this.resourceStorage = resourceStorage;
        this.allocationStorage = allocationStorage;
    }

    private String createFileName(String fileName) throws IOException {
        File folder = new File("reports");
        if (!folder.exists()) folder.mkdirs();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "reports/" + fileName + "_" + time + ".csv";
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.startsWith("=") || val.startsWith("+") || val.startsWith("-") || val.startsWith("@")) val = "'" + val;
        return "\"" + val.replace("\"", "\"\"") + "\"";
    }

    public String generateAllEmergenciesReport() throws IOException {
        String path = createFileName("all_emergencies");
        BufferedWriter w = new BufferedWriter(new FileWriter(path));
        w.write("Emergency ID,User ID,Type,Priority,Location,Description,Affected People,Status,Created At\n");
        for (Emergency em : emergencyStorage.getAllEmergencies()) {
            w.write(em.getEmergencyId() + "," + em.getUserId() + "," + escapeCsv(em.getEmergencyType().toString()) + ","
                    + escapeCsv(em.getPriority().toString()) + "," + escapeCsv(em.getLocation()) + ","
                    + escapeCsv(em.getDescription()) + "," + em.getAffectedPeople() + ","
                    + escapeCsv(em.getStatus().toString()) + "," + escapeCsv(em.getCreatedAt().toString()) + "\n");
        }
        w.close();
        return path;
    }

    public String generatePendingEmergenciesReport() throws IOException {
        String path = createFileName("pending_emergencies");
        BufferedWriter w = new BufferedWriter(new FileWriter(path));
        w.write("Emergency ID,User ID,Type,Priority,Location,Description,Affected People,Status,Created At\n");
        for (Emergency em : emergencyStorage.getAllEmergencies()) {
            if (em.getStatus() == EmergencyStatus.REQUESTED || em.getStatus() == EmergencyStatus.ACCEPTED) {
                w.write(em.getEmergencyId() + "," + em.getUserId() + "," + escapeCsv(em.getEmergencyType().toString()) + ","
                        + escapeCsv(em.getPriority().toString()) + "," + escapeCsv(em.getLocation()) + ","
                        + escapeCsv(em.getDescription()) + "," + em.getAffectedPeople() + ","
                        + escapeCsv(em.getStatus().toString()) + "," + escapeCsv(em.getCreatedAt().toString()) + "\n");
            }
        }
        w.close();
        return path;
    }

    public String generateResolvedEmergenciesReport() throws IOException {
        String path = createFileName("resolved_emergencies");
        BufferedWriter w = new BufferedWriter(new FileWriter(path));
        w.write("Emergency ID,User ID,Type,Priority,Location,Affected People,Status,Created At,Updated At\n");
        for (Emergency em : emergencyStorage.getAllEmergencies()) {
            if (em.getStatus() == EmergencyStatus.RESOLVED) {
                w.write(em.getEmergencyId() + "," + em.getUserId() + "," + escapeCsv(em.getEmergencyType().toString()) + ","
                        + escapeCsv(em.getPriority().toString()) + "," + escapeCsv(em.getLocation()) + ","
                        + em.getAffectedPeople() + "," + escapeCsv(em.getStatus().toString()) + ","
                        + escapeCsv(em.getCreatedAt().toString()) + "," + escapeCsv(em.getUpdatedAt().toString()) + "\n");
            }
        }
        w.close();
        return path;
    }

    public String generateAllocationReport() throws IOException {
        String path = createFileName("allocation_history");
        BufferedWriter w = new BufferedWriter(new FileWriter(path));
        w.write("Allocation ID,Emergency ID,Resource ID,Assigned At,Released At,Active\n");
        for (Allocation al : allocationStorage.getAllAllocations()) {
            String rel = al.getReleasedAt() != null ? al.getReleasedAt().toString() : "";
            w.write(al.getAllocationId() + "," + al.getEmergencyId() + "," + al.getResourceId() + ","
                    + escapeCsv(al.getAssignedAt().toString()) + "," + escapeCsv(rel) + "," + al.isActive() + "\n");
        }
        w.close();
        return path;
    }

    public String generateResourceReport() throws IOException {
        String path = createFileName("resource_availability");
        BufferedWriter w = new BufferedWriter(new FileWriter(path));
        w.write("Resource ID,Resource Name,Resource Type,Status,Contact Number,Location\n");
        for (Resource r : resourceStorage.getAllResources()) {
            w.write(r.getResourceId() + "," + escapeCsv(r.getResourceName()) + "," + escapeCsv(r.getResourceType().toString()) + ","
                    + escapeCsv(r.getResourceStatus().toString()) + "," + escapeCsv(r.getContactNumber()) + ","
                    + escapeCsv(r.getLocation()) + "\n");
        }
        w.close();
        return path;
    }
}
