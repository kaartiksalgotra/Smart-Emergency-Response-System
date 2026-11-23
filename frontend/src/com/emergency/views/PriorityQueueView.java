package com.emergency.views;

import com.emergency.components.StatusIndicator;
import com.emergency.enums.PriorityLevel;
import com.emergency.model.Emergency;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;

/**
 * Admin view showing live Priority Queue ordered by CRITICAL > HIGH > MEDIUM > LOW, older first.
 */
public class PriorityQueueView {

    private VBox root;
    private TableView<Emergency> queueTable;
    private Button refreshButton;
    private Label criticalCountLabel;
    private Label highCountLabel;
    private Label mediumCountLabel;
    private Label lowCountLabel;

    public PriorityQueueView() {
        root = new VBox(15);
        root.getStyleClass().add("content-pane");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(3);
        Label title = new Label("Priority Dispatch Queue");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Live sorting: CRITICAL > HIGH > MEDIUM > LOW. Equal priority sorted by timestamp (FIFO).");
        subtitle.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        refreshButton = new Button("↻ Refresh Queue");
        refreshButton.getStyleClass().add("btn-secondary");
        header.getChildren().addAll(titleBox, spacer, refreshButton);

        // Queue Breakdown Badges
        HBox summaryRow = new HBox(12);
        summaryRow.setAlignment(Pos.CENTER_LEFT);
        criticalCountLabel = createPill("CRITICAL: 0", "#EF4444", "#FEE2E2");
        highCountLabel = createPill("HIGH: 0", "#F97316", "#FFEDD5");
        mediumCountLabel = createPill("MEDIUM: 0", "#EAB308", "#FEF9C3");
        lowCountLabel = createPill("LOW: 0", "#10B981", "#D1FAE5");
        summaryRow.getChildren().addAll(criticalCountLabel, highCountLabel, mediumCountLabel, lowCountLabel);

        // Table
        queueTable = new TableView<>();
        queueTable.setPrefHeight(380);
        queueTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Emergency, String> rankCol = new TableColumn<>("Order");
        rankCol.setCellValueFactory(d -> {
            int idx = queueTable.getItems().indexOf(d.getValue()) + 1;
            return new SimpleStringProperty("#" + idx);
        });
        rankCol.setMaxWidth(60);

        TableColumn<Emergency, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getEmergencyId()));
        idCol.setMaxWidth(70);

        TableColumn<Emergency, String> userCol = new TableColumn<>("Citizen ID");
        userCol.setCellValueFactory(d -> new SimpleStringProperty("User #" + d.getValue().getUserId()));

        TableColumn<Emergency, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmergencyType().toString()));

        TableColumn<Emergency, String> priorityCol = new TableColumn<>("Priority Level");
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

        TableColumn<Emergency, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));

        TableColumn<Emergency, String> peopleCol = new TableColumn<>("Affected");
        peopleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAffectedPeople() + " ppl"));

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

        queueTable.getColumns().addAll(rankCol, idCol, userCol, typeCol, priorityCol, locCol, descCol, peopleCol, statusCol);
        queueTable.setPlaceholder(new Label("Queue is empty. No emergencies awaiting dispatch."));

        root.getChildren().addAll(header, summaryRow, queueTable);
    }

    private Label createPill(String text, String fg, String bg) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 12px; -fx-font-size: 12px;");
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
        criticalCountLabel.setText("CRITICAL: " + crit);
        highCountLabel.setText("HIGH: " + high);
        mediumCountLabel.setText("MEDIUM: " + med);
        lowCountLabel.setText("LOW: " + low);
    }

    public Button getRefreshButton() { return refreshButton; }
    public VBox getView() { return root; }
}
