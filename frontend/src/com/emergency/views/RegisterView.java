package com.emergency.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * ERAS Registration Screen with role selection (Citizen / Admin).
 * Restyled with modern vibrant blue gradient (#38BDF8 to #2563EB) and clean card layout.
 */
public class RegisterView {

    private StackPane root;
    private TextField nameField;
    private TextField emailField;
    private TextField phoneField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private ComboBox<String> roleBox;
    private Button registerButton;
    private Button loginButton;
    private Label messageLabel;

    public RegisterView() {
        root = new StackPane();
        root.getStyleClass().add("main-container");
        root.setStyle("-fx-background-color: #F8FAFC;");

        try {
            root.getStylesheets().add(getClass().getResource("/com/emergency/css/style.css").toExternalForm());
        } catch (Exception ignored) {}

        // Left Column: Modern Blue Gradient Branding
        VBox heroSection = new VBox(16);
        heroSection.setAlignment(Pos.CENTER_LEFT);
        heroSection.setPadding(new Insets(35));
        heroSection.setPrefWidth(360);
        heroSection.setStyle("-fx-background-color: linear-gradient(to bottom right, #38BDF8, #2563EB); -fx-background-radius: 16px 0 0 16px;");

        // Logo
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        StackPane square = new StackPane();
        square.setStyle("-fx-background-color: rgba(255,255,255,0.22); -fx-background-radius: 8px; -fx-min-width: 36px; -fx-min-height: 36px;");
        Label plus = new Label("+");
        plus.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        square.getChildren().add(plus);

        Label brand = new Label("ERAS");
        brand.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        logoBox.getChildren().addAll(square, brand);

        Label brandTitle = new Label("Create Response\nAccount");
        brandTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        Label brandDesc = new Label("Register as a Citizen to report emergencies, or as an Admin to coordinate emergency dispatch.");
        brandDesc.setWrapText(true);
        brandDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.85);");

        VBox benefitsList = new VBox(10,
                createBenefitItem("Instant Emergency Request Dispatch"),
                createBenefitItem("Live Status Lifecycle Notifications"),
                createBenefitItem("Direct Ambulance, Fire & Rescue Access"),
                createBenefitItem("Transparent Response Time Tracking")
        );
        heroSection.getChildren().addAll(logoBox, brandTitle, brandDesc, benefitsList);

        // Right Column: Form
        VBox formSection = new VBox(8);
        formSection.setAlignment(Pos.CENTER_LEFT);
        formSection.setPadding(new Insets(24, 30, 24, 30));
        formSection.setPrefWidth(440);
        formSection.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 0 16px 16px 0; -fx-border-color: #E2E8F0; -fx-border-width: 1px 1px 1px 0;");

        Label regHeading = new Label("Create an Account");
        regHeading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        nameField = new TextField();
        nameField.setPromptText("Enter your full legal name");

        emailField = new TextField();
        emailField.setPromptText("Enter your active email address");

        phoneField = new TextField();
        phoneField.setPromptText("10-digit mobile number");

        passwordField = new PasswordField();
        passwordField.setPromptText("At least 6 characters");

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Repeat password");

        roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Citizen / User", "Admin");
        roleBox.setValue("Citizen / User");
        roleBox.setMaxWidth(Double.MAX_VALUE);

        registerButton = new Button("Complete Registration");
        registerButton.getStyleClass().add("btn-primary");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setPrefHeight(38);

        messageLabel = new Label("");
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-font-size: 11px;");

        loginButton = new Button("Sign In");
        loginButton.setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-background-color: transparent; -fx-cursor: hand;");
        HBox loginBox = new HBox(6, new Label("Already have an account?"), loginButton);
        loginBox.setAlignment(Pos.CENTER);

        formSection.getChildren().addAll(
                regHeading,
                new VBox(1, new Label("Account Type (Role)"), roleBox),
                new VBox(1, new Label("Full Name"), nameField),
                new VBox(1, new Label("Email Address"), emailField),
                new VBox(1, new Label("Phone Number"), phoneField),
                new VBox(1, new Label("Password"), passwordField),
                new VBox(1, new Label("Confirm Password"), confirmPasswordField),
                registerButton, messageLabel, loginBox
        );

        HBox authCard = new HBox(heroSection, formSection);
        authCard.setAlignment(Pos.CENTER);
        authCard.setMaxWidth(800);
        authCard.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 18, 0, 0, 4);");

        root.getChildren().add(authCard);
    }

    private HBox createBenefitItem(String text) {
        Label icon = new Label("✓");
        icon.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 12px;");
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 11px;");
        return new HBox(8, icon, label);
    }

    public StackPane getView() { return root; }
    public TextField getNameField() { return nameField; }
    public TextField getEmailField() { return emailField; }
    public TextField getPhoneField() { return phoneField; }
    public PasswordField getPasswordField() { return passwordField; }
    public PasswordField getConfirmPasswordField() { return confirmPasswordField; }
    public ComboBox<String> getRoleBox() { return roleBox; }
    public Button getRegisterButton() { return registerButton; }
    public Button getLoginButton() { return loginButton; }
    public Label getMessageLabel() { return messageLabel; }
}
