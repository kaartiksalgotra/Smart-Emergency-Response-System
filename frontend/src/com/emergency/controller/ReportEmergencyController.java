
package com.emergency.controller;

import com.emergency.model.User;
import com.emergency.model.Emergency;

import com.emergency.service.EmergencyService;
import com.emergency.service.NotificationService;
import com.emergency.threading.EmergencyProcessor;

import com.emergency.views.ReportEmergencyView;

import javafx.stage.Stage;

public class ReportEmergencyController {

    private ReportEmergencyView reportView;
    private EmergencyService emergencyService;
    private NotificationService notificationService;
    private EmergencyProcessor emergencyProcessor;
    private User user;
    private Stage stage;
    private Runnable backAction;

    public ReportEmergencyController(
            ReportEmergencyView reportView,
            EmergencyService emergencyService,
            NotificationService notificationService,
            EmergencyProcessor emergencyProcessor,
            User user,
            Stage stage,
            Runnable backAction) {

        this.reportView = reportView;
        this.emergencyService = emergencyService;
        this.notificationService = notificationService;
        this.emergencyProcessor = emergencyProcessor;
        this.user = user;
        this.stage = stage;
        this.backAction = backAction;

        reportView.getSubmitButton().setOnAction(
                e -> reportEmergency()
        );

        reportView.getBackButton().setOnAction(
                e -> backAction.run()
        );
    }

    private void reportEmergency() {

        try {

            String location = reportView.getLocationField().getText();
            String description = reportView.getDescriptionField().getText();
            String affectedText =
                    reportView.getAffectedPeopleField().getText();

            if (location.isEmpty() || description.isEmpty()
                    || affectedText.isEmpty()
                    || reportView.getEmergencyTypeBox().getValue() == null
                    || reportView.getPriorityBox().getValue() == null) {

                reportView.getMessageLabel().setText(
                        "Please fill all the fields."
                );
                return;
            }

            int affectedPeople = Integer.parseInt(affectedText);

            Emergency emergency = emergencyService.reportEmergency(
                    user,
                    reportView.getEmergencyTypeBox().getValue(),
                    reportView.getPriorityBox().getValue(),
                    location,
                    description,
                    affectedPeople
            );

            notificationService.notifyEmergencyReported(emergency);

            emergencyProcessor.addEmergency(emergency);

            reportView.getMessageLabel().setText(
                    "Emergency reported successfully! Request #" + emergency.getEmergencyId() + " queued for Admin review."
            );

            reportView.getLocationField().clear();
            reportView.getDescriptionField().clear();
            reportView.getAffectedPeopleField().clear();

            reportView.getEmergencyTypeBox().setValue(null);
            reportView.getPriorityBox().setValue(null);

        } catch (NumberFormatException e) {

            reportView.getMessageLabel().setText(
                    "Please enter a valid number of affected people."
            );

        } catch (IllegalArgumentException e) {

            reportView.getMessageLabel().setText(e.getMessage());

        }
    }
}
