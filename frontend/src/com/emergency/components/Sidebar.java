package com.emergency.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ERAS Horizontal Top Navbar Component.
 * Modern dark charcoal #1E293B bar with logo, nav links, user profile, and logout.
 */
public class Sidebar extends HBox {

    private String navBg = "#1E293B";
    private HBox navContainer;
    private Button logoutButton;
    private Label userNameLabel;
    private Label userRoleLabel;
    private List<Button> navButtons;

    public Sidebar(String userName, String userRole) {
        super(16);
        navButtons = new ArrayList<>();
        setAlignment(Pos.CENTER_LEFT);
        setPrefHeight(62);
        setMinHeight(62);
        setPadding(new Insets(10, 24, 10, 24));
        getStyleClass().add("top-navbar");
        setStyle("-fx-background-color: " + navBg + "; -fx-border-color: #334155; -fx-border-width: 0 0 1 0;");

        // 1. Brand Logo & Title on the left
        HBox brandBox = new HBox(10);
        brandBox.setAlignment(Pos.CENTER_LEFT);

        StackPane logoBox = new StackPane();
        logoBox.setStyle("-fx-background-color: linear-gradient(to bottom right, #38BDF8, #2563EB); -fx-background-radius: 8px; -fx-min-width: 36px; -fx-min-height: 36px;");
        Label plus = new Label("+");
        plus.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px;");
        logoBox.getChildren().add(plus);

        VBox brandText = new VBox(1);
        Label title = new Label("ERAS");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-letter-spacing: 0.5px;");
        Label sub = new Label("RESPONSE NETWORK");
        sub.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 9px; -fx-font-weight: bold;");
        brandText.getChildren().addAll(title, sub);
        brandBox.getChildren().addAll(logoBox, brandText);

        // 2. Navigation Links in the center
        navContainer = new HBox(6);
        navContainer.setAlignment(Pos.CENTER_LEFT);

        // Spacer pushing user info to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 3. User Info & Logout on the right
        userNameLabel = new Label(userName != null ? userName : "User");
        userNameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");

        userRoleLabel = new Label(userRole != null ? userRole.toUpperCase() : "CITIZEN");
        userRoleLabel.setStyle("-fx-text-fill: #38BDF8; -fx-font-size: 9px; -fx-font-weight: bold;");

        VBox userCard = new VBox(1, userNameLabel, userRoleLabel);
        userCard.setAlignment(Pos.CENTER_RIGHT);

        logoutButton = new Button("Log Out");
        logoutButton.getStyleClass().add("sidebar-logout-button");
        logoutButton.setStyle("-fx-background-color: transparent; -fx-border-color: #EF4444; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-text-fill: #EF4444; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 12; -fx-cursor: hand;");

        HBox userBox = new HBox(12, userCard, logoutButton);
        userBox.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(brandBox, navContainer, spacer, userBox);
    }

    /**
     * Adds a navigation button to the horizontal top bar.
     */
    public void addNavButton(Button button) {
        button.getStyleClass().add("sidebar-nav-button");
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 6px; -fx-cursor: hand;");
        navButtons.add(button);
        navContainer.getChildren().add(button);
    }

    /**
     * Highlights the active button with vibrant blue background.
     */
    public void setActiveButton(Button activeButton) {
        for (Button btn : navButtons) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 6px; -fx-cursor: hand;");
        }
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 6px; -fx-cursor: hand;");
        }
    }

    public Button getLogoutButton() { return logoutButton; }
    public void setUserName(String name) { userNameLabel.setText(name); }
    public void setUserRole(String role) { userRoleLabel.setText(role.toUpperCase()); }
}
