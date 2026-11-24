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
 * ERAS Emergency History Archive View.
 * Filterable incident ledger with centered 1200px container and uniform styling.
 */
public class EmergencyHistoryView {
    private BorderPane root;
    private TableView<Emergency> historyTable;
    private Button refreshButton, backButton;
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
        backButton = new Button("← Back to Dashboard");
        backButton.getStyleClass().add("btn-back");

        Label heading = new Label("Emergency History Log");
        heading.getStyleClass().add("top-header-title");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        refreshButton = new Button("🔄 Refresh Log");
        refreshButton.getStyleClass().add("btn-primary");

        HBox headerContent = new HBox(16, backButton, heading, spacer, refreshButton);
        headerContent.setAlignment(Pos.CENTER_LEFT);
        headerContent.setMaxWidth(1200);
        HBox.setHgrow(headerContent, Priority.ALWAYS);

        HBox topBar = new HBox(headerContent);
        topBar.getStyleClass().add("top-header-bar");
        topBar.setAlignment(Pos.CENTER);
        root.setTop(topBar);

        // Content (centered, max-width 1200px)
        VBox contentBox = new VBox(16);
        contentBox.setMaxWidth(1200);
        contentBox.setPadding(new Insets(24, 32, 28, 32));

        // Filter Bar Card
        searchField = new TextField();
        searchField.setPromptText("Search by incident ID, citizen, location, or description...");
        searchField.setPrefHeight(38);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        statusFilterBox = new ComboBox<>();
        statusFilterBox.getItems().addAll("ALL STATUSES", "REQUESTED", "ACCEPTED", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "REJECTED");
        statusFilterBox.setValue("ALL STATUSES");
        statusFilterBox.setPrefHeight(38);

        HBox filterCard = new HBox(14, searchField, statusFilterBox);
        filterCard.setAlignment(Pos.CENTER_LEFT);
        filterCard.setPadding(new Insets(14, 18, 14, 18));
        filterCard.getStyleClass().add("card");

        // Table
        historyTable = new TableView<>();
        historyTable.setPrefHeight(450);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Emergency, String> idCol = new TableColumn<>("INCIDENT ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getEmergencyId()));
        idCol.setMaxWidth(80);

        TableColumn<Emergency, String> typeCol = new TableColumn<>("TYPE");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmergencyType().toString()));
        typeCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Emergency e = (empty || getTableRow() == null) ? null : (Emergency) getTableRow().getItem();
                setGraphic(e != null ? StatusIndicator.createTypeBadge(e.getEmergencyType()) : null);
                setText(null); setAlignment(Pos.CENTER);
            }
        });

        TableColumn<Emergency, String> prioCol = new TableColumn<>("PRIORITY");
        prioCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPriority().toString()));
        prioCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Emergency e = (empty || getTableRow() == null) ? null : (Emergency) getTableRow().getItem();
                setGraphic(e != null ? StatusIndicator.createPriorityBadge(e.getPriority()) : null);
                setText(null); setAlignment(Pos.CENTER);
            }
        });

        TableColumn<Emergency, String> locCol = new TableColumn<>("LOCATION");
        locCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLocation()));

        TableColumn<Emergency, String> dateCol = new TableColumn<>("DATE & TIME");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().format(FORMATTER) : "-"
        ));

        TableColumn<Emergency, String> statusCol = new TableColumn<>("FINAL STATUS");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().toString()));
        statusCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Emergency e = (empty || getTableRow() == null) ? null : (Emergency) getTableRow().getItem();
                setGraphic(e != null ? StatusIndicator.createStatusBadge(e.getStatus()) : null);
                setText(null); setAlignment(Pos.CENTER);
            }
        });

        historyTable.getColumns().addAll(idCol, typeCol, prioCol, locCol, dateCol, statusCol);
        historyTable.setPlaceholder(new Label("No emergency records match your filters."));

        VBox tableCard = new VBox(historyTable);
        tableCard.getStyleClass().add("card");
        tableCard.setPadding(new Insets(16));

        contentBox.getChildren().addAll(filterCard, tableCard);

        StackPane centerWrapper = new StackPane(contentBox);
        centerWrapper.setAlignment(Pos.TOP_CENTER);
        centerWrapper.setStyle("-fx-background-color: #F8FAFC;");

        ScrollPane scroll = new ScrollPane(centerWrapper);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        root.setCenter(scroll);
    }

    public BorderPane getView() { return root; }
    public TableView<Emergency> getHistoryTable() { return historyTable; }
    public Button getRefreshButton() { return refreshButton; }
    public Button getBackButton() { return backButton; }
    public TextField getSearchField() { return searchField; }
    public ComboBox<String> getStatusFilterBox() { return statusFilterBox; }
    public ObservableList<Emergency> getMasterList() { return masterList; }
    public FilteredList<Emergency> getFilteredList() { return filteredList; }
}
