package com.emergency.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Modern Registration Screen with role selection (Citizen / Admin).
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
        root.setStyle("-fx-background-color: #0F172A;");

        try {
            root.getStylesheets().add(getClass().getResource("/com/emergency/css/style.css").toExternalForm());
        } catch (Exception ignored) {}

        // Left Column: Citizen Portal Benefits
        VBox heroSection = new VBox(16);
        heroSection.setAlignment(Pos.CENTER_LEFT);
        heroSection.setPadding(new Insets(35));
        heroSection.setPrefWidth(360);
        heroSection.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 12px 0 0 12px;");

        Label badgeLabel = new Label("EMERGENCY RESPONSE NETWORK");
        badgeLabel.setStyle("-fx-background-color: rgba(37,99,235,0.2); -fx-text-fill: #60A5FA; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4px 10px; -fx-background-radius: 20px;");

        Label brandTitle = new Label("Create Response\nAccount");
        brandTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        Label brandDesc = new Label("Register as a Citizen to report emergencies, or as an Admin to coordinate emergency dispatch.");
        brandDesc.setWrapText(true);
        brandDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");

        VBox benefitsList = new VBox(10,
                createBenefitItem("Instant incident reporting & priority queueing"),
                createBenefitItem("Real-time ambulance, fire & rescue dispatch"),
                createBenefitItem("Role-based access: Citizen or System Admin"),
                createBenefitItem("Secure account credentials and notification alerts")
        );
        heroSection.getChildren().addAll(badgeLabel, brandTitle, brandDesc, benefitsList);

        // Right Column: Registration Form
        VBox formSection = new VBox(8);
        formSection.setAlignment(Pos.CENTER_LEFT);
        formSection.setPadding(new Insets(24, 30, 24, 30));
        formSection.setPrefWidth(420);
        formSection.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 0 12px 12px 0;");

        Label registerHeading = new Label("Create an Account");
        registerHeading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");
        Label registerSub = new Label("Select your role and enter your details");
        registerSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Citizen / User", "Admin");
        roleBox.setValue("Citizen / User");
        roleBox.setMaxWidth(Double.MAX_VALUE);
        roleBox.setPrefHeight(34);

        nameField = new TextField();
        nameField.setPromptText("Full Name");
        nameField.setPrefHeight(34);

        emailField = new TextField();
        emailField.setPromptText("Email Address");
        emailField.setPrefHeight(34);

        phoneField = new TextField();
        phoneField.setPromptText("10-Digit Phone Number");
        phoneField.setPrefHeight(34);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password (min 6 chars)");
        passwordField.setPrefHeight(34);

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setPrefHeight(34);

        registerButton = new Button("Create Account");
        registerButton.getStyleClass().add("btn-danger");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setPrefHeight(38);

        messageLabel = new Label("");
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("msg-error");

        loginButton = new Button("Sign In Here");
        loginButton.setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-background-color: transparent; -fx-cursor: hand;");
        HBox loginBox = new HBox(6, new Label("Already registered?"), loginButton);
        loginBox.setAlignment(Pos.CENTER);

        formSection.getChildren().addAll(
                registerHeading, registerSub,
                new VBox(2, new Label("Register As (Role)"), roleBox),
                new VBox(2, new Label("Full Name"), nameField),
                new VBox(2, new Label("Email Address"), emailField),
                new VBox(2, new Label("Phone Number"), phoneField),
                new VBox(2, new Label("Password"), passwordField),
                new VBox(2, new Label("Confirm Password"), confirmPasswordField),
                registerButton, messageLabel, loginBox
        );

        HBox authCard = new HBox(heroSection, formSection);
        authCard.setAlignment(Pos.CENTER);
        authCard.setMaxWidth(780);
        authCard.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 16, 0, 0, 4);");

        root.getChildren().add(authCard);
    }

    private HBox createBenefitItem(String text) {
        Label icon = new Label("✓");
        icon.setStyle("-fx-text-fill: #60A5FA; -fx-font-weight: bold; -fx-font-size: 12px;");
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 11px;");
        return new HBox(8, icon, label);
    }

    public StackPane getView() { return root; }
    public ComboBox<String> getRoleBox() { return roleBox; }
    public TextField getNameField() { return nameField; }
    public TextField getEmailField() { return emailField; }
    public TextField getPhoneField() { return phoneField; }
    public PasswordField getPasswordField() { return passwordField; }
    public PasswordField getConfirmPasswordField() { return confirmPasswordField; }
    public Button getRegisterButton() { return registerButton; }
    public Button getLoginButton() { return loginButton; }
    public Label getMessageLabel() { return messageLabel; }
}
