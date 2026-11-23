
package com.emergency.controller;

import com.emergency.model.Emergency;
import com.emergency.model.User;

import com.emergency.service.EmergencyService;

import com.emergency.views.TrackEmergencyView;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;

public class TrackEmergencyController {

    private TrackEmergencyView trackView;
    private EmergencyService emergencyService;

    private User user;
    private Stage stage;
    private Runnable backAction;

    public TrackEmergencyController(
            TrackEmergencyView trackView,
            EmergencyService emergencyService,
            User user,
            Stage stage,
            Runnable backAction) {

        this.trackView = trackView;
        this.emergencyService = emergencyService;
        this.user = user;
        this.stage = stage;
        this.backAction = backAction;

        trackView.getRefreshButton().setOnAction(e -> {
            loadEmergencies();
        });

        trackView.getBackButton().setOnAction(e -> {
            backAction.run();
        });

        loadEmergencies();

    }

    private void loadEmergencies() {

        ArrayList<Emergency> emergencies =
                emergencyService.getEmergenciesByUserId(
                        user.getUserId()
                );

        trackView.getEmergencyTable().setItems(
                FXCollections.observableArrayList(emergencies)
        );

    }

}
