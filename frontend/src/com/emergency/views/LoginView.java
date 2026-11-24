package com.emergency.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * ERAS Login Screen with Role Selection and Demo Credentials.
 * Restyled with modern vibrant blue gradient (#38BDF8 to #2563EB) and clean card layout.
 */
public class LoginView {

    private StackPane root;
    private TextField emailField;
    private PasswordField passwordField;
    private TextField plainPasswordField;
    private CheckBox showPasswordCheckbox;
    private ComboBox<String> roleBox;
    private Button loginButton;
    private Button registerButton;
    private Label messageLabel;

    public LoginView() {
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
        heroSection.setPrefWidth(380);
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

        Label brandTitle = new Label("Emergency Response\nAdmin System");
        brandTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        Label brandDesc = new Label("Unified incident coordination, priority-based resource allocation, and live response lifecycle tracking.");
        brandDesc.setWrapText(true);
        brandDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.85);");

        VBox featuresList = new VBox(10,
                createFeatureItem("Real-time Emergency Reporting & Review"),
                createFeatureItem("Intelligent Multi-Resource Allocation"),
                createFeatureItem("Live Status Monitoring & History Tracking"),
                createFeatureItem("PriorityQueue Multi-threaded Processing")
        );
        heroSection.getChildren().addAll(logoBox, brandTitle, brandDesc, featuresList);

        // Right Column: Form
        VBox formSection = new VBox(10);
        formSection.setAlignment(Pos.CENTER_LEFT);
        formSection.setPadding(new Insets(28, 32, 28, 32));
        formSection.setPrefWidth(420);
        formSection.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 0 16px 16px 0; -fx-border-color: #E2E8F0; -fx-border-width: 1px 1px 1px 0;");

        Label loginHeading = new Label("Welcome Back");
        loginHeading.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label loginSub = new Label("Sign in to your emergency response account");
        loginSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Citizen / User", "Admin");
        roleBox.setValue("Citizen / User");
        roleBox.setMaxWidth(Double.MAX_VALUE);
        roleBox.setPrefHeight(36);

        emailField = new TextField();
        emailField.setPromptText("Enter User ID (e.g. USER001) or Email");
        emailField.setPrefHeight(36);

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(36);

        plainPasswordField = new TextField();
        plainPasswordField.setPromptText("Enter your password");
        plainPasswordField.setPrefHeight(36);
        plainPasswordField.setVisible(false);
        plainPasswordField.setManaged(false);
        passwordField.textProperty().bindBidirectional(plainPasswordField.textProperty());

        showPasswordCheckbox = new CheckBox("Show password");
        showPasswordCheckbox.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");
        showPasswordCheckbox.setOnAction(e -> {
            boolean show = showPasswordCheckbox.isSelected();
            plainPasswordField.setVisible(show); plainPasswordField.setManaged(show);
            passwordField.setVisible(!show); passwordField.setManaged(!show);
        });

        StackPane passPane = new StackPane(passwordField, plainPasswordField);

        loginButton = new Button("Sign In to Account");
        loginButton.getStyleClass().add("btn-primary");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setPrefHeight(38);

        messageLabel = new Label("");
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-font-size: 11px;");

        registerButton = new Button("Register Now");
        registerButton.setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-background-color: transparent; -fx-cursor: hand;");
        HBox registerBox = new HBox(6, new Label("Don't have an account?"), registerButton);
        registerBox.setAlignment(Pos.CENTER);

        // Demo Credentials Box
        VBox demoBox = new VBox(3);
        demoBox.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 8px; -fx-padding: 8 12; -fx-font-size: 11px;");
        Label demoHeader = new Label("🔑 Predefined Demo Accounts:");
        demoHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label demoCitizen = new Label("• Citizen: USER001 or user@emergency.com | Pass: User123");
        demoCitizen.setStyle("-fx-text-fill: #64748B;");
        Label demoAdmin = new Label("• Admin: ADMIN001 or admin@emergency.com | Pass: Admin123");
        demoAdmin.setStyle("-fx-text-fill: #64748B;");
        demoBox.getChildren().addAll(demoHeader, demoCitizen, demoAdmin);

        formSection.getChildren().addAll(
                loginHeading, loginSub,
                new VBox(2, new Label("Login As (Role)"), roleBox),
                new VBox(2, new Label("User ID or Email"), emailField),
                new VBox(2, new Label("Password"), passPane, showPasswordCheckbox),
                loginButton, messageLabel, registerBox, demoBox
        );

        HBox authCard = new HBox(heroSection, formSection);
        authCard.setAlignment(Pos.CENTER);
        authCard.setMaxWidth(800);
        authCard.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 18, 0, 0, 4);");

        root.getChildren().add(authCard);
    }

    private HBox createFeatureItem(String text) {
        Label icon = new Label("✓");
        icon.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 12px;");
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 11px;");
        return new HBox(8, icon, label);
    }

    public StackPane getView() { return root; }
    public ComboBox<String> getRoleBox() { return roleBox; }
    public TextField getEmailField() { return emailField; }
    public PasswordField getPasswordField() { return passwordField; }
    public String getEnteredPassword() {
        if (showPasswordCheckbox != null && showPasswordCheckbox.isSelected()) {
            return plainPasswordField.getText();
        }
        return passwordField.getText();
    }
    public Button getLoginButton() { return loginButton; }
    public Button getRegisterButton() { return registerButton; }
    public Label getMessageLabel() { return messageLabel; }
}
