package com.emergency.views;

import com.emergency.components.StatusIndicator;
import com.emergency.model.Emergency;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Unified "My Emergencies" View Component for the Citizen Portal.
 * Displays all emergency requests submitted by the citizen in a clean, single-view table
 * with status tracking, search, and filtering, without redundant sub-tabs.
 */
public class MyEmergenciesView {

    private BorderPane root;
    private Button backButton;
    private Button refreshButton;

    private TextField searchField;
    private ComboBox<String> statusFilterBox;

    private ObservableList<Emergency> masterList;
    private FilteredList<Emergency> filteredList;
    private TableView<Emergency> emergenciesTable;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public MyEmergenciesView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");

        masterList = FXCollections.observableArrayList();
        filteredList = new FilteredList<>(masterList, p -> true);

        buildHeader();
        buildContent();
    }

    private void buildHeader() {
        backButton = new Button("← Return to Dashboard");
        backButton.getStyleClass().add("btn-back");

        Label heading = new Label("My Emergencies");
        heading.getStyleClass().add("top-header-title");

        Label subHeading = new Label("Track real-time status and view all your reported emergency incidents");
        subHeading.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        VBox titleBox = new VBox(2, heading, subHeading);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        refreshButton = new Button("🔄 Refresh Records");
        refreshButton.getStyleClass().add("btn-primary");

        HBox headerContent = new HBox(16, backButton, titleBox, spacer, refreshButton);
        headerContent.setAlignment(Pos.CENTER_LEFT);
        headerContent.setMaxWidth(1200);
        HBox.setHgrow(headerContent, Priority.ALWAYS);

        HBox topBar = new HBox(headerContent);
        topBar.getStyleClass().add("top-header-bar");
        topBar.setAlignment(Pos.CENTER);
        root.setTop(topBar);
    }

    @SuppressWarnings("unchecked")
    private void buildContent() {
        VBox mainContainer = new VBox(16);
        mainContainer.setMaxWidth(1200);
        mainContainer.setPadding(new Insets(24, 32, 28, 32));

        // Workflow / Status Lifecycle Guide
        Label workflowHint = new Label("Status Guide: REQUESTED (Awaiting Review) → ACCEPTED / REJECTED → ASSIGNED → IN PROGRESS → RESOLVED");
        workflowHint.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748B;");

        // Filter Bar Card
        searchField = new TextField();
        searchField.setPromptText("Search by incident ID, type, location, or description...");
        searchField.setPrefHeight(38);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        statusFilterBox = new ComboBox<>();
        statusFilterBox.getItems().addAll("ALL STATUSES", "REQUESTED", "ACCEPTED", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "REJECTED");
        statusFilterBox.setValue("ALL STATUSES");
        statusFilterBox.setPrefHeight(38);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        statusFilterBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        HBox filterCard = new HBox(14, searchField, statusFilterBox);
        filterCard.setAlignment(Pos.CENTER_LEFT);
        filterCard.setPadding(new Insets(14, 18, 14, 18));
        filterCard.getStyleClass().add("card");

        // Emergencies Table Card
        VBox tableCard = new VBox(10);
        tableCard.getStyleClass().add("card");

        emergenciesTable = new TableView<>();
        emergenciesTable.setPrefHeight(460);
        emergenciesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Emergency, String> idCol = new TableColumn<>("INCIDENT ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getEmergencyId()));
        idCol.setMaxWidth(90);

        TableColumn<Emergency, String> typeCol = new TableColumn<>("TYPE");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmergencyType().toString()));
        typeCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Emergency em = (empty || getTableRow() == null) ? null : (Emergency) getTableRow().getItem();
                setGraphic(em != null ? StatusIndicator.createTypeBadge(em.getEmergencyType()) : null);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<Emergency, String> priorityCol = new TableColumn<>("PRIORITY");
        priorityCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPriority().toString()));
        priorityCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Emergency em = (empty || getTableRow() == null) ? null : (Emergency) getTableRow().getItem();
                setGraphic(em != null ? StatusIndicator.createPriorityBadge(em.getPriority()) : null);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<Emergency, String> locCol = new TableColumn<>("LOCATION");
        locCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLocation()));

        TableColumn<Emergency, String> dateCol = new TableColumn<>("REPORTED AT");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().format(FORMATTER) : "-"
        ));

        TableColumn<Emergency, String> statusCol = new TableColumn<>("CURRENT STATUS");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().toString()));
        statusCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Emergency em = (empty || getTableRow() == null) ? null : (Emergency) getTableRow().getItem();
                setGraphic(em != null ? StatusIndicator.createStatusBadge(em.getStatus()) : null);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });

        emergenciesTable.getColumns().addAll(idCol, typeCol, priorityCol, locCol, dateCol, statusCol);
        emergenciesTable.setPlaceholder(new Label("You have not reported any emergencies yet."));
        emergenciesTable.setItems(filteredList);

        tableCard.getChildren().addAll(workflowHint, emergenciesTable);

        mainContainer.getChildren().addAll(filterCard, tableCard);

        StackPane centerWrapper = new StackPane(mainContainer);
        centerWrapper.setAlignment(Pos.TOP_CENTER);
        centerWrapper.setStyle("-fx-background-color: #F8FAFC;");

        ScrollPane scroll = new ScrollPane(centerWrapper);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        root.setCenter(scroll);
    }

    private void applyFilter() {
        String keyword = searchField != null && searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String status = statusFilterBox != null ? statusFilterBox.getValue() : "ALL STATUSES";

        filteredList.setPredicate(e -> {
            if (e == null) return false;

            // Status filter check
            if (status != null && !status.equals("ALL STATUSES")) {
                if (e.getStatus() == null || !e.getStatus().name().equalsIgnoreCase(status)) {
                    return false;
                }
            }

            // Keyword filter check
            if (keyword.isEmpty()) {
                return true;
            }

            String idStr = String.valueOf(e.getEmergencyId());
            String typeStr = e.getEmergencyType() != null ? e.getEmergencyType().name().toLowerCase() : "";
            String locStr = e.getLocation() != null ? e.getLocation().toLowerCase() : "";
            String descStr = e.getDescription() != null ? e.getDescription().toLowerCase() : "";
            String stStr = e.getStatus() != null ? e.getStatus().name().toLowerCase() : "";

            return idStr.contains(keyword) || ("#" + idStr).contains(keyword)
                    || typeStr.contains(keyword) || locStr.contains(keyword)
                    || descStr.contains(keyword) || stStr.contains(keyword);
        });
    }

    public void setData(ArrayList<Emergency> list) {
        masterList.setAll(list != null ? list : new ArrayList<>());
        applyFilter();
    }

    public void setRequests(ArrayList<Emergency> list) {
        setData(list);
    }

    public BorderPane getView() { return root; }
    public Button getBackButton() { return backButton; }
    public Button getRefreshButton() { return refreshButton; }
    public TableView<Emergency> getEmergenciesTable() { return emergenciesTable; }
    public TableView<Emergency> getRequestsTable() { return emergenciesTable; }
    public TextField getSearchField() { return searchField; }
    public ComboBox<String> getStatusFilterBox() { return statusFilterBox; }
}
