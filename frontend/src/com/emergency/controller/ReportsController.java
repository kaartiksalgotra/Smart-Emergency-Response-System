
package com.emergency.controller;

import com.emergency.service.ReportService;
import com.emergency.views.ReportsView;

import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.IOException;

public class ReportsController {

    private ReportsView reportsView;
    private ReportService reportService;

    private Runnable backAction;

    public ReportsController(
            ReportsView reportsView,
            ReportService reportService,
            Runnable backAction) {

        this.reportsView = reportsView;
        this.reportService = reportService;
        this.backAction = backAction;

        reportsView.getAllEmergenciesButton().setOnAction(e -> {
            generateAllEmergenciesReport();
        });

        reportsView.getPendingEmergenciesButton().setOnAction(e -> {
            generatePendingEmergenciesReport();
        });

        reportsView.getResolvedEmergenciesButton().setOnAction(e -> {
            generateResolvedEmergenciesReport();
        });

        reportsView.getAllocationReportButton().setOnAction(e -> {
            generateAllocationReport();
        });

        reportsView.getResourceReportButton().setOnAction(e -> {
            generateResourceReport();
        });

        reportsView.getBackButton().setOnAction(e -> {
            backAction.run();
        });

    }

    private void generateAllEmergenciesReport() {

        try {

            String filePath =
                    reportService.generateAllEmergenciesReport();

            showSuccessMessage(filePath);

        } catch (IOException e) {

            showErrorMessage(e.getMessage());

        }

    }

    private void generatePendingEmergenciesReport() {

        try {

            String filePath =
                    reportService.generatePendingEmergenciesReport();

            showSuccessMessage(filePath);

        } catch (IOException e) {

            showErrorMessage(e.getMessage());

        }

    }

    private void generateResolvedEmergenciesReport() {

        try {

            String filePath =
                    reportService.generateResolvedEmergenciesReport();

            showSuccessMessage(filePath);

        } catch (IOException e) {

            showErrorMessage(e.getMessage());

        }

    }

    private void generateAllocationReport() {

        try {

            String filePath =
                    reportService.generateAllocationReport();

            showSuccessMessage(filePath);

        } catch (IOException e) {

            showErrorMessage(e.getMessage());

        }

    }

    private void generateResourceReport() {

        try {

            String filePath =
                    reportService.generateResourceReport();

            showSuccessMessage(filePath);

        } catch (IOException e) {

            showErrorMessage(e.getMessage());

        }

    }

    private void showSuccessMessage(String filePath) {

        reportsView.getMessageLabel().setText(
                "Report generated successfully!\nFile location: "
                        + filePath
        );

    }

    private void showErrorMessage(String message) {

        reportsView.getMessageLabel().setText(
                "Error generating report: " + message
        );

    }

}
