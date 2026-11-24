package com.emergency.views;

import com.emergency.components.StatusIndicator;
import com.emergency.enums.PriorityLevel;
import com.emergency.model.Emergency;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.ArrayList;

/**
 * Admin view showing live Priority Queue ordered by CRITICAL > HIGH > MEDIUM > LOW, older first.
 * Modern blue theme with 1200px centered container and full API compatibility.
 */
public class PriorityQueueView {
    private VBox root;
    private TableView<Emergency> queueTable;
    private Button refreshButton;
    private Label criticalCountLabel, highCountLabel, mediumCountLabel, lowCountLabel;

    public PriorityQueueView() {
        root = new VBox(16);
        root.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 24 32;");
        root.setAlignment(Pos.TOP_CENTER);

        VBox contentBox = new VBox(16);
        contentBox.setMaxWidth(1200);

        // Header
        Label title = new Label("Priority Dispatch Queue"); title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label sub = new Label("Live sorting: CRITICAL > HIGH > MEDIUM > LOW. Equal priority sorted by timestamp (FIFO)."); sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        refreshButton = new Button("🔄 Refresh Queue"); refreshButton.getStyleClass().add("btn-primary");
        HBox header = new HBox(12, new VBox(2, title, sub), sp, refreshButton); header.setAlignment(Pos.CENTER_LEFT);

        // Queue Breakdown Badges
        HBox summaryRow = new HBox(12); summaryRow.setAlignment(Pos.CENTER_LEFT);
        criticalCountLabel = createPill("CRITICAL: 0", "#DC2626", "#FEE2E2");
        highCountLabel = createPill("HIGH: 0", "#EA580C", "#FFEDD5");
        mediumCountLabel = createPill("MEDIUM: 0", "#D97706", "#FEF3C7");
        lowCountLabel = createPill("LOW: 0", "#059669", "#ECFDF5");
        summaryRow.getChildren().addAll(criticalCountLabel, highCountLabel, mediumCountLabel, lowCountLabel);

        // Table
        queueTable = new TableView<>(); queueTable.setPrefHeight(420); queueTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Emergency, String> rankCol = new TableColumn<>("Order");
        rankCol.setCellValueFactory(d -> new SimpleStringProperty("#" + (queueTable.getItems().indexOf(d.getValue()) + 1))); rankCol.setMaxWidth(60);
        TableColumn<Emergency, String> idCol = new TableColumn<>("ID"); idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getEmergencyId())); idCol.setMaxWidth(70);
        TableColumn<Emergency, String> userCol = new TableColumn<>("Citizen ID"); userCol.setCellValueFactory(d -> new SimpleStringProperty("User #" + d.getValue().getUserId()));
        TableColumn<Emergency, String> typeCol = new TableColumn<>("Type"); typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmergencyType().toString()));
        TableColumn<Emergency, String> priorityCol = new TableColumn<>("Priority Level");
        priorityCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPriority().toString()));
        priorityCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Emergency em = (empty || getTableRow() == null) ? null : (Emergency) getTableRow().getItem();
                setGraphic(em != null ? StatusIndicator.createPriorityBadge(em.getPriority()) : null); setText(null); setAlignment(Pos.CENTER);
            }
        });
        TableColumn<Emergency, String> locCol = new TableColumn<>("Location"); locCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLocation()));
        TableColumn<Emergency, String> descCol = new TableColumn<>("Description"); descCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        TableColumn<Emergency, String> peopleCol = new TableColumn<>("Affected"); peopleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAffectedPeople() + " ppl"));
        TableColumn<Emergency, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().toString()));
        statusCol.setCellFactory(c -> new TableCell<Emergency, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Emergency em = (empty || getTableRow() == null) ? null : (Emergency) getTableRow().getItem();
                setGraphic(em != null ? StatusIndicator.createStatusBadge(em.getStatus()) : null); setText(null); setAlignment(Pos.CENTER);
            }
        });

        queueTable.getColumns().addAll(rankCol, idCol, userCol, typeCol, priorityCol, locCol, descCol, peopleCol, statusCol);
        queueTable.setPlaceholder(new Label("Queue is empty. No emergencies awaiting dispatch."));

        VBox tableCard = new VBox(queueTable); tableCard.setPadding(new Insets(18)); tableCard.getStyleClass().add("card");
        contentBox.getChildren().addAll(header, summaryRow, tableCard);
        root.getChildren().add(contentBox);
    }

    private Label createPill(String text, String fg, String bg) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-font-weight: bold; -fx-padding: 6 14; -fx-background-radius: 20px; -fx-font-size: 11px;");
        return lbl;
    }

    public void setQueueList(ArrayList<Emergency> list) {
        queueTable.setItems(FXCollections.observableArrayList(list));
        int crit = 0, high = 0, med = 0, low = 0;
        for (Emergency em : list) {
            if (em.getPriority() == PriorityLevel.CRITICAL) crit++;
            else if (em.getPriority() == PriorityLevel.HIGH) high++;
            else if (em.getPriority() == PriorityLevel.MEDIUM) med++;
            else if (em.getPriority() == PriorityLevel.LOW) low++;
        }
        criticalCountLabel.setText("CRITICAL: " + crit); highCountLabel.setText("HIGH: " + high);
        mediumCountLabel.setText("MEDIUM: " + med); lowCountLabel.setText("LOW: " + low);
    }

    public void setQueue(ArrayList<Emergency> list) { setQueueList(list); }
    public Button getRefreshButton() { return refreshButton; }
    public TableView<Emergency> getQueueTable() { return queueTable; }
    public VBox getView() { return root; }
}
