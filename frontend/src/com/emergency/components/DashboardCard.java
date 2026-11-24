package com.emergency.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Modern Metric Stat Card for ERAS Admin & Citizen Dashboards.
 * Clean card with dark charcoal text, vibrant blue highlights, and subtle borders.
 */
public class DashboardCard extends VBox {

    private String blueTrend = "#2563EB";
    private String redTrend = "#EF4444";

    private Label titleLabel;
    private Label valueLabel;
    private Label trendLabel;
    private Label subtextLabel;

    public DashboardCard(String title, String initialValue, String accentStyleClass) {
        this(title, initialValue, null, accentStyleClass);
    }

    public DashboardCard(String title, String initialValue, String subtext, String accentStyleClass) {
        this(title, initialValue, "", subtext, accentStyleClass);
    }

    public DashboardCard(String title, String initialValue, String trend, String subtext, String accentStyleClass) {
        super(4);
        getStyleClass().addAll("stat-card", accentStyleClass);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(16, 20, 16, 20));
        setPrefWidth(200);
        setMinHeight(100);
        setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 14px; -fx-border-color: #E2E8F0; -fx-border-radius: 14px;");

        // 1. Small uppercase title
        titleLabel = new Label(title != null ? title.toUpperCase() : "");
        titleLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748B;");

        // 2. Large bold metric number
        valueLabel = new Label(initialValue);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        // 3. Trend and subtext row
        HBox bottomRow = new HBox(6);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        trendLabel = new Label(trend != null ? trend : "");
        if (trend != null && trend.contains("▲")) {
            trendLabel.setStyle("-fx-text-fill: " + blueTrend + "; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else if (trend != null && trend.contains("▼")) {
            trendLabel.setStyle("-fx-text-fill: " + redTrend + "; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else {
            trendLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");
        }

        subtextLabel = new Label(subtext != null ? subtext : "");
        subtextLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");

        bottomRow.getChildren().addAll(trendLabel, subtextLabel);
        getChildren().addAll(titleLabel, valueLabel, bottomRow);
    }

    public void setValue(int value) {
        this.valueLabel.setText(String.valueOf(value));
    }

    public void setValue(String value) {
        this.valueLabel.setText(value);
    }

    public void setTrend(String trendText, boolean isPositive) {
        trendLabel.setText(trendText);
        trendLabel.setStyle("-fx-text-fill: " + (isPositive ? blueTrend : redTrend) + "; -fx-font-weight: bold; -fx-font-size: 11px;");
    }

    public void setSubtext(String subtext) {
        if (subtextLabel == null) {
            subtextLabel = new Label(subtext);
            subtextLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");
            getChildren().add(subtextLabel);
        } else {
            subtextLabel.setText(subtext);
        }
    }

    public Label getTitleLabel() { return titleLabel; }
    public Label getValueLabel() { return valueLabel; }
    public Label getSubtextLabel() { return subtextLabel; }
    public Label getTrendLabel() { return trendLabel; }
}
