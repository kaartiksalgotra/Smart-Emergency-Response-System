package com.emergency.threading;

import com.emergency.model.Emergency;
import com.emergency.enums.EmergencyStatus;
import com.emergency.service.AllocationService;

/**
 * Background thread worker that manages the emergency queue.
 * Keeps incoming requests queued for Admin review without blocking the JavaFX UI.
 */
public class EmergencyProcessor implements Runnable {

    private EmergencyQueue emergencyQueue;
    private AllocationService allocationService;
    private Thread workerThread;
    private volatile boolean running;

    public EmergencyProcessor(EmergencyQueue emergencyQueue) {
        this.emergencyQueue = emergencyQueue;
        this.running = false;
    }

    public EmergencyProcessor(EmergencyQueue emergencyQueue, AllocationService allocationService) {
        this(emergencyQueue);
        this.allocationService = allocationService;
    }

    public void startProcessing() {
        if (!running) {
            running = true;
            workerThread = new Thread(this);
            workerThread.setDaemon(true);
            workerThread.start();
        }
    }

    public void stopProcessing() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    public void addEmergency(Emergency emergency) {
        if (emergency != null && emergency.getStatus() == EmergencyStatus.REQUESTED) {
            emergencyQueue.addEmergency(emergency);
        }
    }

    public EmergencyQueue getEmergencyQueue() {
        return emergencyQueue;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
