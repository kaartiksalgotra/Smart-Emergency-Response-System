
package com.emergency.controller;

import com.emergency.model.Emergency;
import com.emergency.model.User;

import com.emergency.service.EmergencyService;

import com.emergency.views.EmergencyHistoryView;

import javafx.collections.FXCollections;

import java.util.ArrayList;

public class EmergencyHistoryController {

    private EmergencyHistoryView historyView;
    private EmergencyService emergencyService;

    private User user;
    private Runnable backAction;

    public EmergencyHistoryController(
            EmergencyHistoryView historyView,
            EmergencyService emergencyService,
            User user,
            Runnable backAction) {

        this.historyView = historyView;
        this.emergencyService = emergencyService;
        this.user = user;
        this.backAction = backAction;

        historyView.getRefreshButton().setOnAction(e -> {
            loadHistory();
        });

        historyView.getBackButton().setOnAction(e -> {
            backAction.run();
        });

        loadHistory();

    }

    private void loadHistory() {

        ArrayList<Emergency> emergencies =
                emergencyService.getEmergenciesByUserId(
                        user.getUserId()
                );

        historyView.getHistoryTable().setItems(
                FXCollections.observableArrayList(emergencies)
        );

    }

}
