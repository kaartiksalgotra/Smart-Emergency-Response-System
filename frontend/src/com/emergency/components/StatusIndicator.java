package com.emergency.components;

import com.emergency.enums.EmergencyStatus;
import com.emergency.enums.EmergencyType;
import com.emergency.enums.PriorityLevel;
import com.emergency.enums.ResourceStatus;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

/**
 * Reusable Status, Priority and Type Badge Component.
 * Styled with exact ERAS palette: filled pills for type/status, outlined pills for priority.
 */
public class StatusIndicator extends Label {

    public StatusIndicator(String text, String badgeStyleClass) {
        super(text);
        getStyleClass().addAll("badge", badgeStyleClass);
        setAlignment(Pos.CENTER);
    }

    public static Label createTypeBadge(EmergencyType type) {
        if (type == null) return new Label("-");
        String styleClass;
        switch (type) {
            case MEDICAL:
                styleClass = "badge-type-medical";
                break;
            case FIRE:
                styleClass = "badge-type-fire";
                break;
            case RESCUE:
                styleClass = "badge-type-rescue";
                break;
            case OTHER:
            default:
                styleClass = "badge-type-other";
                break;
        }
        return new StatusIndicator(type.toString(), styleClass);
    }

    public static Label createPriorityBadge(PriorityLevel priority) {
        if (priority == null) return new Label("-");
        String styleClass;
        switch (priority) {
            case CRITICAL:
                styleClass = "badge-priority-critical";
                break;
            case HIGH:
                styleClass = "badge-priority-high";
                break;
            case MEDIUM:
                styleClass = "badge-priority-medium";
                break;
            case LOW:
            default:
                styleClass = "badge-priority-low";
                break;
        }
        return new StatusIndicator(priority.toString(), styleClass);
    }

    public static Label createStatusBadge(EmergencyStatus status) {
        if (status == null) return new Label("-");
        String styleClass;
        switch (status) {
            case RESOLVED:
                styleClass = "badge-resolved";
                break;
            case IN_PROGRESS:
                styleClass = "badge-in-progress";
                break;
            case ASSIGNED:
                styleClass = "badge-assigned";
                break;
            case REQUESTED:
            case ACCEPTED:
                styleClass = "badge-pending";
                break;
            case REJECTED:
            case CANCELLED:
            default:
                styleClass = "badge-cancelled";
                break;
        }
        return new StatusIndicator(status.toString().replace("_", " "), styleClass);
    }

    public static Label createResourceStatusBadge(ResourceStatus status) {
        if (status == null) return new Label("-");
        String styleClass = (status == ResourceStatus.AVAILABLE) ? "badge-available" : "badge-allocated";
        return new StatusIndicator(status.toString(), styleClass);
    }

    public static Label createCustomBadge(String text, String badgeStyleClass) {
        return new StatusIndicator(text, badgeStyleClass);
    }
}
