package com.emergency.views;

import com.emergency.enums.EmergencyType;
import com.emergency.enums.PriorityLevel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Emergency Reporting View Component.
 * Embedded seamlessly into the Fixed Dashboard layout.
 */
public class ReportEmergencyView {

    private BorderPane root;

    private ComboBox<EmergencyType> emergencyTypeBox;
    private ComboBox<PriorityLevel> priorityBox;
    private TextField locationField;
    private TextField affectedPeopleField;
    private TextArea descriptionField;

    private Button submitButton;
    private Button backButton;
    private Label messageLabel;

    public ReportEmergencyView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");

        // Top Header
        HBox topBar = new HBox(16);
        topBar.getStyleClass().add("top-header-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPrefHeight(60);

        backButton = new Button("← Return to Dashboard");
        backButton.getStyleClass().add("btn-back");

        Label title = new Label("Report Emergency Incident");
        title.getStyleClass().add("top-header-title");

        topBar.getChildren().addAll(backButton, title);
        root.setTop(topBar);

        // Center Form
        VBox contentBox = new VBox(16);
        contentBox.getStyleClass().add("content-pane");
        contentBox.setAlignment(Pos.TOP_CENTER);
        contentBox.setMaxWidth(720);

        // Alert notice
        Label alertNotice = new Label("⚠️ PRIORITY NOTICE: For life-threatening emergencies, specify precise location and choose CRITICAL priority.");
        alertNotice.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #991B1B; -fx-padding: 10px 14px; -fx-background-radius: 6px; -fx-font-weight: bold; -fx-font-size: 11px;");
        alertNotice.setMaxWidth(Double.MAX_VALUE);

        // Form Card
        VBox formCard = new VBox(14);
        formCard.getStyleClass().add("card");
        formCard.setPadding(new Insets(24));

        Label formTitle = new Label("Incident Details");
        formTitle.getStyleClass().add("section-title");

        // Form fields
        emergencyTypeBox = new ComboBox<>();
        emergencyTypeBox.getItems().addAll(EmergencyType.values());
        emergencyTypeBox.setPromptText("Select emergency type");
        emergencyTypeBox.setMaxWidth(Double.MAX_VALUE);
        emergencyTypeBox.setPrefHeight(38);

        priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll(PriorityLevel.values());
        priorityBox.setPromptText("Select priority level");
        priorityBox.setMaxWidth(Double.MAX_VALUE);
        priorityBox.setPrefHeight(38);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);

        VBox typeBox = new VBox(4, new Label("Emergency Type"), emergencyTypeBox);
        VBox priorityContainer = new VBox(4, new Label("Priority Level"), priorityBox);
        GridPane.setHgrow(typeBox, Priority.ALWAYS);
        GridPane.setHgrow(priorityContainer, Priority.ALWAYS);
        grid.add(typeBox, 0, 0);
        grid.add(priorityContainer, 1, 0);

        locationField = new TextField();
        locationField.setPromptText("e.g. 123 Main St, Near Central Station");
        locationField.setPrefHeight(38);

        affectedPeopleField = new TextField();
        affectedPeopleField.setPromptText("e.g. 2");
        affectedPeopleField.setPrefHeight(38);

        VBox locBox = new VBox(4, new Label("Incident Location"), locationField);
        VBox affBox = new VBox(4, new Label("Number of Affected People"), affectedPeopleField);
        GridPane.setHgrow(locBox, Priority.ALWAYS);
        GridPane.setHgrow(affBox, Priority.ALWAYS);
        grid.add(locBox, 0, 1);
        grid.add(affBox, 1, 1);

        descriptionField = new TextArea();
        descriptionField.setPromptText("Describe the nature of the emergency and any special hazards...");
        descriptionField.setPrefHeight(90);
        descriptionField.setWrapText(true);

        submitButton = new Button("Submit Emergency & Dispatch");
        submitButton.getStyleClass().add("btn-danger");
        submitButton.setPrefHeight(40);
        submitButton.setPrefWidth(240);

        messageLabel = new Label("");
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        formCard.getChildren().addAll(
                formTitle,
                grid,
                new VBox(4, new Label("Emergency Description"), descriptionField),
                submitButton,
                messageLabel
        );

        contentBox.getChildren().addAll(alertNotice, formCard);

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        root.setCenter(scroll);
    }

    public BorderPane getView() { return root; }
    public ComboBox<EmergencyType> getEmergencyTypeBox() { return emergencyTypeBox; }
    public ComboBox<PriorityLevel> getPriorityBox() { return priorityBox; }
    public TextField getLocationField() { return locationField; }
    public TextField getAffectedPeopleField() { return affectedPeopleField; }
    public TextArea getDescriptionField() { return descriptionField; }
    public Button getSubmitButton() { return submitButton; }
    public Button getBackButton() { return backButton; }
    public Label getMessageLabel() { return messageLabel; }
}
