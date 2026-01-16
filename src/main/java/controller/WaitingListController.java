package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.Separator;
import javafx.geometry.Insets;

import model.Patient;
import model.WaitingList;
import model.Clinic;

import service.WaitingListService;

import java.sql.SQLException;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class WaitingListController {

    @FXML
    private VBox waitingListContainer;

    private Patient currentPatient;
    private final WaitingListService waitingListService = new WaitingListService();

    public void setPatient(Patient patient) {
        this.currentPatient = patient;
        loadWaitingList();
    }

    private void loadWaitingList() {
        waitingListContainer.getChildren().clear();

        if (currentPatient == null) {
            waitingListContainer.getChildren().add(new Label("⚠️ Patient not loaded."));
            return;
        }

        try {
            List<WaitingList> requests = waitingListService.getPatientWaitingList(currentPatient.getID());

            if (requests == null || requests.isEmpty()) {
                Label emptyLabel = new Label("📭 No requests in your waiting list.");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-alignment: center;");
                waitingListContainer.getChildren().add(emptyLabel);
            } else {
                for (WaitingList request : requests) {
                    waitingListContainer.getChildren().add(createWaitingListCard(request));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("❌ Database error: failed to load waiting list.");
            errorLabel.setStyle("-fx-text-fill: red;");
            waitingListContainer.getChildren().add(errorLabel);
        }
    }

    private VBox createWaitingListCard(WaitingList request) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);");

        try {
            if (request == null) {
                return createErrorCard("Invalid request object");
            }

            Clinic clinic = request.getClinic();
            String clinicName = (clinic != null && clinic.getName() != null) ? clinic.getName() : "Unknown Clinic";
            String doctorName = (clinic != null && clinic.getDoctorName() != null) ? clinic.getDoctorName() : "Unknown Doctor";

            String timeStr = "—";
            try {
                if (request.getRequestTime() != null) {
                    timeStr = request.getRequestTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                }
            } catch (DateTimeParseException ex) {
                timeStr = "Invalid time";
            }

            String statusStr = (request.getStatus() != null) ? request.getStatus().name() : "UNKNOWN";

            Label clinicLabel = new Label("🏥 Clinic: " + clinicName);
            clinicLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1ABC9C;");

            Label doctorLabel = new Label("👨‍⚕️ Doctor: " + doctorName);
            doctorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

            Label timeLabel = new Label("🕒 Requested: " + timeStr);
            timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #777;");

            Label statusLabel = new Label("📊 Status: " + statusStr);
            statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #F39C12;");

            card.getChildren().addAll(clinicLabel, doctorLabel, timeLabel, statusLabel, new Separator());

        } catch (Exception e) {
            e.printStackTrace();
            return createErrorCard("Rendering error: " + e.getMessage());
        }

        return card;
    }

    private VBox createErrorCard(String message) {
        VBox errorCard = new VBox(5);
        errorCard.setPadding(new Insets(10));
        errorCard.setStyle("-fx-background-color: #fdf2f2; -fx-border-color: #e74c3c; -fx-border-width: 1;");
        Label errorLabel = new Label("❗ " + message);
        errorLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 12px;");
        errorCard.getChildren().add(errorLabel);
        return errorCard;
    }
}
