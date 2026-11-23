package com.emergency.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Reusable Modern Metric Card for User and Admin Dashboards.
 * Displays a metric title, high-impact numerical value, and optional subtext
 * with an accent border.
 */
public class DashboardCard extends VBox {

    private Label titleLabel;
    private Label valueLabel;
    private Label subtextLabel;

    public DashboardCard(String title, String initialValue, String accentStyleClass) {
        this(title, initialValue, null, accentStyleClass);
    }

    public DashboardCard(String title, String initialValue, String subtext, String accentStyleClass) {
        super(6); // 6px spacing between elements

        // Style the card container
        getStyleClass().addAll("stat-card", accentStyleClass);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(16, 20, 16, 20));
        setPrefWidth(180);
        setMinHeight(95);

        // Title Label
        titleLabel = new Label(title.toUpperCase());
        titleLabel.getStyleClass().add("stat-card-title");

        // Value Label
        valueLabel = new Label(initialValue);
        valueLabel.getStyleClass().add("stat-card-value");

        getChildren().addAll(titleLabel, valueLabel);

        // Optional Subtext Label
        if (subtext != null && !subtext.trim().isEmpty()) {
            subtextLabel = new Label(subtext);
            subtextLabel.getStyleClass().add("stat-card-subtext");
            getChildren().add(subtextLabel);
        }
    }

    /**
     * Updates the displayed metric value with an integer.
     */
    public void setValue(int value) {
        this.valueLabel.setText(String.valueOf(value));
    }

    /**
     * Updates the displayed metric value with a string.
     */
    public void setValue(String value) {
        this.valueLabel.setText(value);
    }

    /**
     * Updates or sets the subtext description.
     */
    public void setSubtext(String subtext) {
        if (subtextLabel == null) {
            subtextLabel = new Label(subtext);
            subtextLabel.getStyleClass().add("stat-card-subtext");
            getChildren().add(subtextLabel);
        } else {
            subtextLabel.setText(subtext);
        }
    }

    public Label getTitleLabel() {
        return titleLabel;
    }

    public Label getValueLabel() {
        return valueLabel;
    }

    public Label getSubtextLabel() {
        return subtextLabel;
    }
}
