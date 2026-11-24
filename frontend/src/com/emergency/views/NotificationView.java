package com.emergency.views;

import com.emergency.components.StatusIndicator;
import com.emergency.model.Notification;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

/**
 * Notification Center View Component.
 * Polished layout centered within a 1200px max-width container with uniform buttons.
 */
public class NotificationView {

    private BorderPane root;
    private TableView<Notification> notificationTable;
    private Button refreshButton;
    private Button markReadButton;
    private Button backButton;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public NotificationView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");

        // Top Header
        backButton = new Button("← Return to Dashboard");
        backButton.getStyleClass().add("btn-back");

        Label heading = new Label("Notification Center");
        heading.getStyleClass().add("top-header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        refreshButton = new Button("🔄 Refresh");
        refreshButton.getStyleClass().add("btn-secondary");

        markReadButton = new Button("✓ Mark as Read");
        markReadButton.getStyleClass().add("btn-success");

        HBox actionGroup = new HBox(10, refreshButton, markReadButton);
        actionGroup.setAlignment(Pos.CENTER_RIGHT);

        HBox headerContent = new HBox(16, backButton, heading, spacer, actionGroup);
        headerContent.setAlignment(Pos.CENTER_LEFT);
        headerContent.setMaxWidth(1200);
        HBox.setHgrow(headerContent, Priority.ALWAYS);

        HBox topBar = new HBox(headerContent);
        topBar.getStyleClass().add("top-header-bar");
        topBar.setAlignment(Pos.CENTER);
        root.setTop(topBar);

        // Center Content Container (centered, max-width 1200px)
        VBox contentBox = new VBox(16);
        contentBox.setMaxWidth(1200);
        contentBox.setPadding(new Insets(24, 32, 28, 32));

        VBox tableCard = new VBox(10);
        tableCard.getStyleClass().add("card");

        notificationTable = new TableView<>();
        notificationTable.setPrefHeight(450);
        notificationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Notification, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getNotificationId()));
        idCol.setMaxWidth(60);

        TableColumn<Notification, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNotificationType().replace("_", " ")));
        typeCol.setPrefWidth(140);

        TableColumn<Notification, String> msgCol = new TableColumn<>("Message");
        msgCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMessage()));
        msgCol.setPrefWidth(350);

        TableColumn<Notification, String> dateCol = new TableColumn<>("Date & Time");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().format(FORMATTER) : "-"
        ));
        dateCol.setPrefWidth(140);

        TableColumn<Notification, String> readCol = new TableColumn<>("Status");
        readCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isRead() ? "READ" : "UNREAD"));
        readCol.setCellFactory(c -> new TableCell<Notification, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null); setText(null);
                } else {
                    Label badge = "READ".equalsIgnoreCase(item)
                            ? StatusIndicator.createCustomBadge("✓ READ", "badge-resolved")
                            : StatusIndicator.createCustomBadge("● UNREAD", "badge-priority-critical");
                    setGraphic(badge); setText(null); setAlignment(Pos.CENTER);
                }
            }
        });
        readCol.setMaxWidth(100);

        notificationTable.getColumns().addAll(idCol, typeCol, msgCol, dateCol, readCol);
        notificationTable.setPlaceholder(new Label("No notifications found. All alerts have been reviewed."));

        tableCard.getChildren().add(notificationTable);
        contentBox.getChildren().add(tableCard);

        StackPane centerWrapper = new StackPane(contentBox);
        centerWrapper.setAlignment(Pos.TOP_CENTER);
        centerWrapper.setStyle("-fx-background-color: #F8FAFC;");

        ScrollPane scroll = new ScrollPane(centerWrapper);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        root.setCenter(scroll);
    }

    public BorderPane getView() { return root; }
    public TableView<Notification> getNotificationTable() { return notificationTable; }
    public Button getRefreshButton() { return refreshButton; }
    public Button getMarkReadButton() { return markReadButton; }
    public Button getBackButton() { return backButton; }
}
