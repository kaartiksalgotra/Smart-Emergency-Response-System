package com.emergency.components;

import com.emergency.enums.EmergencyStatus;
import com.emergency.enums.PriorityLevel;
import com.emergency.enums.ResourceStatus;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

/**
 * Reusable Status & Priority Badge Component.
 * Color-coded pill indicators for all workflow states.
 */
public class StatusIndicator extends Label {

    public StatusIndicator(String text, String badgeStyleClass) {
        super(text);
        getStyleClass().addAll("badge", badgeStyleClass);
        setAlignment(Pos.CENTER);
    }

    public static Label createStatusBadge(EmergencyStatus status) {
        if (status == null) return new Label("-");
        String styleClass;
        switch (status) {
            case REQUESTED:
                styleClass = "badge-pending"; // Amber
                break;
            case ACCEPTED:
                styleClass = "badge-in-progress"; // Blue
                break;
            case REJECTED:
                styleClass = "badge-cancelled"; // Soft Red
                break;
            case ASSIGNED:
                styleClass = "badge-assigned"; // Indigo
                break;
            case IN_PROGRESS:
                styleClass = "badge-in-progress"; // Blue
                break;
            case RESOLVED:
                styleClass = "badge-resolved"; // Emerald
                break;
            case CANCELLED:
            default:
                styleClass = "badge-cancelled";
                break;
        }
        return new StatusIndicator(status.toString().replace("_", " "), styleClass);
    }

    public static Label createPriorityBadge(PriorityLevel priority) {
        if (priority == null) return new Label("-");
        String styleClass;
        switch (priority) {
            case CRITICAL: styleClass = "badge-priority-critical"; break;
            case HIGH: styleClass = "badge-priority-high"; break;
            case MEDIUM: styleClass = "badge-priority-medium"; break;
            case LOW:
            default: styleClass = "badge-priority-low"; break;
        }
        return new StatusIndicator(priority.toString(), styleClass);
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
