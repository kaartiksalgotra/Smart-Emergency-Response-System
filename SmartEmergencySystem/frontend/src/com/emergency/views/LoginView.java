package com.emergency.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Modern Login Screen with Role Selection and Demo Credentials.
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
        root.setStyle("-fx-background-color: #0F172A;");

        try {
            root.getStylesheets().add(getClass().getResource("/com/emergency/css/style.css").toExternalForm());
        } catch (Exception ignored) {}

        // Left Column: Branding
        VBox heroSection = new VBox(16);
        heroSection.setAlignment(Pos.CENTER_LEFT);
        heroSection.setPadding(new Insets(35));
        heroSection.setPrefWidth(380);
        heroSection.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 12px 0 0 12px;");

        Label badgeLabel = new Label("RAPID DISPATCH & ALLOCATION");
        badgeLabel.setStyle("-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #F87171; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4px 10px; -fx-background-radius: 20px;");

        Label brandTitle = new Label("Smart Emergency\nResponse System");
        brandTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        Label brandDesc = new Label("Unified incident coordination, automated resource management, and live response tracking.");
        brandDesc.setWrapText(true);
        brandDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");

        VBox featuresList = new VBox(10,
                createFeatureItem("Real-time Emergency Reporting & Review"),
                createFeatureItem("Intelligent Multi-Resource Allocation"),
                createFeatureItem("Live Status Monitoring & History Tracking"),
                createFeatureItem("Automated Multi-threaded Priority Processing")
        );
        heroSection.getChildren().addAll(badgeLabel, brandTitle, brandDesc, featuresList);

        // Right Column: Form
        VBox formSection = new VBox(10);
        formSection.setAlignment(Pos.CENTER_LEFT);
        formSection.setPadding(new Insets(28, 32, 28, 32));
        formSection.setPrefWidth(420);
        formSection.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 0 12px 12px 0;");

        Label loginHeading = new Label("Welcome Back");
        loginHeading.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");
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
        loginButton.getStyleClass().add("btn-danger");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setPrefHeight(38);

        messageLabel = new Label("");
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("msg-error");

        registerButton = new Button("Register Now");
        registerButton.setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-background-color: transparent; -fx-cursor: hand;");
        HBox registerBox = new HBox(6, new Label("Don't have an account?"), registerButton);
        registerBox.setAlignment(Pos.CENTER);

        // Demo Credentials Box
        VBox demoBox = new VBox(3);
        demoBox.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 6px; -fx-padding: 8 12; -fx-font-size: 11px;");
        Label demoHeader = new Label("🔑 Predefined Demo Accounts:");
        demoHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        Label demoCitizen = new Label("• Citizen: USER001 or user@emergency.com | Pass: User123");
        demoCitizen.setStyle("-fx-text-fill: #475569;");
        Label demoAdmin = new Label("• Admin: ADMIN001 or admin@emergency.com | Pass: Admin123");
        demoAdmin.setStyle("-fx-text-fill: #475569;");
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
        authCard.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 16, 0, 0, 4);");

        root.getChildren().add(authCard);
    }

    private HBox createFeatureItem(String text) {
        Label icon = new Label("✓");
        icon.setStyle("-fx-text-fill: #34D399; -fx-font-weight: bold; -fx-font-size: 12px;");
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 11px;");
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
