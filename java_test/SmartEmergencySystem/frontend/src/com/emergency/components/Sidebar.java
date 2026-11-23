package com.emergency.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable Fixed Sidebar Component for Citizen and Admin Dashboards.
 * Keeps navigation permanently fixed on the left side of the screen.
 */
public class Sidebar extends VBox {

    private VBox navContainer;
    private Button logoutButton;
    private Label userNameLabel;
    private Label userRoleLabel;
    private List<Button> navButtons;

    public Sidebar(String userName, String userRole) {
        super(10);
        navButtons = new ArrayList<>();

        // Lock sidebar width so it never shifts or resizes
        setPrefWidth(240);
        setMinWidth(240);
        setMaxWidth(240);
        setPadding(new Insets(20, 14, 20, 14));
        getStyleClass().add("sidebar");

        // 1. Branding Header
        Label brandTitle = new Label("EMERGENCY OPS");
        brandTitle.getStyleClass().add("sidebar-brand-title");

        Label brandSub = new Label("SMART RESPONSE SYSTEM");
        brandSub.getStyleClass().add("sidebar-brand-sub");

        VBox brandBox = new VBox(2, brandTitle, brandSub);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.setPadding(new Insets(0, 4, 12, 4));

        // 2. User Info Card
        userNameLabel = new Label(userName != null ? userName : "User");
        userNameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");

        userRoleLabel = new Label(userRole != null ? userRole.toUpperCase() : "CITIZEN");
        userRoleLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 10px; -fx-font-weight: bold;");

        VBox userCard = new VBox(3, userNameLabel, userRoleLabel);
        userCard.setPadding(new Insets(10, 12, 10, 12));
        userCard.setStyle(
                "-fx-background-color: #1E293B;" +
                "-fx-background-radius: 8px;" +
                "-fx-border-color: #334155;" +
                "-fx-border-radius: 8px;" +
                "-fx-border-width: 1px;"
        );

        // 3. Navigation Buttons Container
        Label navTitle = new Label("MAIN MENU");
        navTitle.getStyleClass().add("sidebar-category-header");

        navContainer = new VBox(6);
        navContainer.setAlignment(Pos.TOP_LEFT);

        // 4. Spacer pushing logout button to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // 5. Logout Button
        logoutButton = new Button("Log Out");
        logoutButton.getStyleClass().add("sidebar-logout-button");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setPrefHeight(40);

        getChildren().addAll(brandBox, userCard, navTitle, navContainer, spacer, logoutButton);
    }

    /**
     * Adds a navigation button to the sidebar.
     */
    public void addNavButton(Button button) {
        button.getStyleClass().add("sidebar-nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(40);
        navButtons.add(button);
        navContainer.getChildren().add(button);
    }

    /**
     * Highlights the active button and resets the other buttons.
     */
    public void setActiveButton(Button activeButton) {
        for (Button btn : navButtons) {
            btn.getStyleClass().remove("sidebar-nav-button-active");
            if (!btn.getStyleClass().contains("sidebar-nav-button")) {
                btn.getStyleClass().add("sidebar-nav-button");
            }
        }
        if (activeButton != null) {
            activeButton.getStyleClass().remove("sidebar-nav-button");
            activeButton.getStyleClass().add("sidebar-nav-button-active");
        }
    }

    public Button getLogoutButton() {
        return logoutButton;
    }

    public void setUserName(String name) {
        userNameLabel.setText(name);
    }

    public void setUserRole(String role) {
        userRoleLabel.setText(role.toUpperCase());
    }
}
