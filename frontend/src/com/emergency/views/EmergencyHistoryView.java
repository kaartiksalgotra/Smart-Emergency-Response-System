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

/**
 * Emergency History Archive View with search and status filtering.
 */
public class EmergencyHistoryView {

    private BorderPane root;
    private TableView<Emergency> historyTable;
    private Button refreshButton;
    private Button backButton;
    private TextField searchField;
    private ComboBox<String> statusFilterBox;

    private ObservableList<Emergency> masterList;
    private FilteredList<Emergency> filteredList;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public EmergencyHistoryView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");

        masterList = FXCollections.observableArrayList();
        filteredList = new FilteredList<>(masterList, p -> true);

        // Header
        HBox topBar = new HBox(16);
        topBar.getStyleClass().add("top-header-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPrefHeight(60);

        backButton = new Button("← Return to Dashboard");
        backButton.getStyleClass().add("btn-back");

        Label heading = new Label("Emergency History Log");
        heading.getStyleClass().add("top-header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        refreshButton = new Button("🔄 Refresh Log");
        refreshButton.getStyleClass().add("btn-primary");
        topBar.getChildren().addAll(backButton, heading, spacer, refreshButton);
        root.setTop(topBar);

        // Content
        VBox contentBox = new VBox(14);
        contentBox.getStyleClass().add("content-pane");

        // Filter Bar
        HBox filterCard = new HBox(12);
        filterCard.setAlignment(Pos.CENTER_LEFT);
        filterCard.setPadding(new Insets(10, 14, 10, 14));
        filterCard.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #E2E8F0; -fx-border-radius: 8px;");

        searchField = new TextField();
        searchField.setPromptText("Search location, type, or ID...");
        searchField.setPrefWidth(240);

        statusFilterBox = new ComboBox<>();
        statusFilterBox.getItems().addAll("ALL STATUSES", "REQUESTED", "ACCEPTED", "REJECTED", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "CANCELLED");
        statusFilterBox.setValue("ALL STATUSES");

        Button clearBtn = new Button("Clear");
        clearBtn.getStyleClass().add("btn-secondary");
        clearBtn.setOnAction(e -> {
            searchField.clear();
            statusFilterBox.setValue("ALL STATUSES");
        });

        filterCard.getChildren().addAll(new Label("Filter:"), searchField, statusFilterBox, clearBtn);
        searchField.textProperty().addListener((o, oldV, newV) -> applyFilter());
        statusFilterBox.valueProperty().addListener((o, oldV, newV) -> applyFilter());

        // Table
        historyTable = new TableView<>();
        historyTable.setPrefHeight(440);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.setItems(filteredList);

        TableColumn<Emergency, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getEmergencyId()));
        idCol.setMaxWidth(60);

        TableColumn<Emergency, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmergencyType().toString()));

        TableColumn<Emergency, String> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPriority().toString()));
        priorityCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); setText(null);
                } else {
                    Emergency em = (Emergency) getTableRow().getItem();
                    setGraphic(StatusIndicator.createPriorityBadge(em.getPriority()));
                    setText(null); setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Emergency, String> locCol = new TableColumn<>("Location");
        locCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLocation()));

        TableColumn<Emergency, String> affCol = new TableColumn<>("Affected");
        affCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAffectedPeople() + " ppl"));

        TableColumn<Emergency, String> dateCol = new TableColumn<>("Date Logged");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().format(FORMATTER) : "-"
        ));

        TableColumn<Emergency, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().toString()));
        statusCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); setText(null);
                } else {
                    Emergency em = (Emergency) getTableRow().getItem();
                    setGraphic(StatusIndicator.createStatusBadge(em.getStatus()));
                    setText(null); setAlignment(Pos.CENTER);
                }
            }
        });

        historyTable.getColumns().addAll(idCol, typeCol, priorityCol, locCol, affCol, dateCol, statusCol);
        historyTable.setPlaceholder(new Label("No past emergency records found."));

        VBox tableCard = new VBox(historyTable);
        tableCard.getStyleClass().add("card");
        contentBox.getChildren().addAll(filterCard, tableCard);
        root.setCenter(contentBox);
    }

    private void applyFilter() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedStatus = statusFilterBox.getValue();

        filteredList.setPredicate(em -> {
            if (em == null) return false;
            if (selectedStatus != null && !selectedStatus.equals("ALL STATUSES")) {
                if (!em.getStatus().toString().equalsIgnoreCase(selectedStatus)) return false;
            }
            if (!query.isEmpty()) {
                String idStr = String.valueOf(em.getEmergencyId());
                String loc = em.getLocation() == null ? "" : em.getLocation().toLowerCase();
                String type = em.getEmergencyType() == null ? "" : em.getEmergencyType().toString().toLowerCase();
                return idStr.contains(query) || loc.contains(query) || type.contains(query);
            }
            return true;
        });
    }

    public BorderPane getView() { return root; }
    public TableView<Emergency> getHistoryTable() { return historyTable; }
    public Button getRefreshButton() { return refreshButton; }
    public Button getBackButton() { return backButton; }
}
