package com.emergency.threading;

import com.emergency.model.Emergency;
import com.emergency.enums.PriorityLevel;
import com.emergency.enums.EmergencyStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Priority queue for incoming emergency requests.
 * Orders by: CRITICAL > HIGH > MEDIUM > LOW (older first for ties).
 */
public class EmergencyQueue {

    private PriorityBlockingQueue<Emergency> queue;

    private static final Comparator<Emergency> PRIORITY_COMPARATOR = new Comparator<Emergency>() {
        @Override
        public int compare(Emergency e1, Emergency e2) {
            int p1 = getPriorityValue(e1.getPriority());
            int p2 = getPriorityValue(e2.getPriority());
            if (p1 != p2) {
                return Integer.compare(p2, p1); // Higher number first
            }
            return e1.getCreatedAt().compareTo(e2.getCreatedAt()); // Older first
        }
    };

    public EmergencyQueue() {
        queue = new PriorityBlockingQueue<>(100, PRIORITY_COMPARATOR);
    }

    private static int getPriorityValue(PriorityLevel priority) {
        if (priority == PriorityLevel.CRITICAL) return 4;
        if (priority == PriorityLevel.HIGH) return 3;
        if (priority == PriorityLevel.MEDIUM) return 2;
        return 1;
    }

    public void addEmergency(Emergency emergency) {
        if (emergency != null && !queue.contains(emergency)) {
            queue.offer(emergency);
        }
    }

    public Emergency pollEmergency() {
        return queue.poll();
    }

    public Emergency getNextEmergency() throws InterruptedException {
        return queue.take();
    }

    /**
     * Returns all pending review requests sorted by priority.
     */
    public ArrayList<Emergency> getQueueAsSortedList() {
        ArrayList<Emergency> list = new ArrayList<>();
        for (Emergency em : queue) {
            if (em.getStatus() == EmergencyStatus.REQUESTED) {
                list.add(em);
            }
        }
        Collections.sort(list, PRIORITY_COMPARATOR);
        return list;
    }

    public int getQueueSize() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
