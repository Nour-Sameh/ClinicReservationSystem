package controller;

import dao.RatingDAO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import model.*;
import service.ClinicService;
import service.PatientService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AppointmentsController {

    @FXML private VBox appointmentsContainer;

    private Patient currentPatient;
    private final PatientService patientService = new PatientService();
    private final ClinicService clinicService = new ClinicService();
    private final SimpleIntegerProperty selectedRating = new SimpleIntegerProperty(0);

    public void setPatient(Patient patient) {
        this.currentPatient = patient;
        loadPatientAppointments();
    }

    private void loadPatientAppointments() {
        appointmentsContainer.getChildren().clear();
        if (currentPatient == null) {
            appointmentsContainer.getChildren().add(new Label("❌ No patient loaded."));
            return;
        }

        try {
            List<Appointment> apps = patientService.getPatientAppointments(currentPatient);
            if (apps.isEmpty()) {
                appointmentsContainer.getChildren().add(new Label("You have no scheduled appointments."));
            } else {
                for (Appointment a : apps) {
                    appointmentsContainer.getChildren().add(createAppointmentCard(a));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            appointmentsContainer.getChildren().add(new Label("Error loading appointments."));
        }
    }

    private HBox createAppointmentCard(Appointment appointment) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(10, 15, 10, 15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");

        TimeSlot slot = appointment.getAppointmentDateTime();
        Clinic clinic = appointment.getClinic();

        String dateStr = slot.getDate().format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"));
        String timeStr = slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")) + " - " +
                slot.getEndTime().format(DateTimeFormatter.ofPattern("hh:mm a"));

        VBox detailsVBox = new VBox(5);
        detailsVBox.setAlignment(Pos.CENTER_LEFT);
        Label doctorLabel = new Label("Dr. " + clinic.getDoctorName());
        doctorLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        HBox dateBox = new HBox(5, new Label("🗓️"), new Label(dateStr));
        HBox timeBox = new HBox(5, new Label("⏰"), new Label(timeStr));
        dateBox.setAlignment(Pos.CENTER_LEFT);
        timeBox.setAlignment(Pos.CENTER_LEFT);
        detailsVBox.getChildren().addAll(doctorLabel, dateBox, timeBox);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox rightVBox = new VBox(7);
        rightVBox.setAlignment(Pos.CENTER_RIGHT);

        String btnText = "Add Rating";
        try {
            Rating existing = new RatingDAO().getRatingByPatientAndClinic(currentPatient.getID(), clinic.getID());
            if (existing != null) btnText = "Update Rating";
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Label ratingBtn = new Label(btnText);
        ratingBtn.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 3 8; " +
                        "-fx-background-color: #FFD700; -fx-text-fill: #333; " +
                        "-fx-background-radius: 3; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 2, 0, 0, 1);"
        );
        ratingBtn.setOnMouseClicked(e -> showRatingDialog(clinic));

        Label clinicLabel = new Label(clinic.getName());
        clinicLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

//        Button rescheduleBtn = new Button("Reschedule");
//        rescheduleBtn.setStyle("-fx-background-color: linear-gradient(#0A84FF,#0066CC); -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px;");
//        rescheduleBtn.setOnAction(e -> System.out.println("Rescheduling: " + appointment.getId()));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: linear-gradient(#FF3B30,#C82010); -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px;");
        cancelBtn.setOnAction(e -> {
            Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Cancel this appointment?", ButtonType.OK, ButtonType.CANCEL);
            c.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    try {
                        patientService.cancelAppointment(currentPatient, appointment);
                        loadPatientAppointments();
                        new Alert(Alert.AlertType.INFORMATION, "Cancelled!").show();
                    } catch (SQLException ex) {
                        new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
                    }
                }
            });
        });

        HBox btns = new HBox(5, cancelBtn);
        btns.setAlignment(Pos.CENTER_RIGHT);
        rightVBox.getChildren().addAll(ratingBtn, clinicLabel, btns);

        card.getChildren().addAll(detailsVBox, spacer, rightVBox);
        return card;
    }

    private void showRatingDialog(Clinic clinic) {
        Rating existingRating = null;
        try {
            existingRating = new RatingDAO().getRatingByPatientAndClinic(currentPatient.getID(), clinic.getID());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        final Rating finalExistingRating = existingRating;

        Dialog<ButtonType> dialog = new Dialog<>();
        String title = finalExistingRating != null ? "Update Your Rating" : "Rate " + clinic.getName();
        dialog.setTitle(title);
        dialog.setHeaderText("How was your experience?");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");

        HBox starBox = new HBox(8);
        starBox.setAlignment(Pos.CENTER);
        Label[] stars = new Label[5];
        int initial = finalExistingRating != null ? finalExistingRating.getScore() : 0;
        selectedRating.set(initial);

        for (int i = 0; i < 5; i++) {
            final int idx = i;
            Label star = new Label(i < initial ? "★" : "☆");
            star.setFont(Font.font(32));
            star.setStyle(i < initial ? "-fx-text-fill: #FFD700;" : "-fx-text-fill: #ccc;");
            star.setOnMouseEntered(e -> star.setStyle("-fx-text-fill: #FFD700;"));
            star.setOnMouseExited(e -> {
                int r = selectedRating.get();
                star.setStyle(idx < r ? "-fx-text-fill: #FFD700;" : "-fx-text-fill: #ccc;");
            });
            star.setOnMouseClicked(e -> {
                int r = idx + 1;
                selectedRating.set(r);
                for (int j = 0; j < 5; j++) {
                    stars[j].setText(j < r ? "★" : "☆");
                    stars[j].setStyle(j < r ? "-fx-text-fill: #FFD700;" : "-fx-text-fill: #ccc;");
                }
            });
            stars[i] = star;
            starBox.getChildren().add(star);
        }

        TextArea commentArea = new TextArea();
        if (finalExistingRating != null && finalExistingRating.getComment() != null) {
            commentArea.setText(finalExistingRating.getComment());
        }
        commentArea.setPromptText("Write your feedback (optional)");
        commentArea.setPrefRowCount(3);
        commentArea.setStyle("-fx-border-color: #DDD; -fx-border-radius: 6; -fx-font-size: 14px; -fx-padding: 8;");

        Button submitBtn = new Button(finalExistingRating != null ? "Update Rating" : "Add Rating");
        submitBtn.setStyle("-fx-background-color: #1ABC9C; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 25; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");

        submitBtn.setOnAction(e -> {
            Integer r = selectedRating.get();
            if (r == null || r == 0) {
                new Alert(Alert.AlertType.WARNING, "Please select a rating.").showAndWait();
                return;
            }
            String comment = commentArea.getText().trim();

            try {
                RatingDAO dao = new RatingDAO();
                if (finalExistingRating != null) {
                    finalExistingRating.setScore(r);
                    finalExistingRating.setComment(comment);
                    dao.update(finalExistingRating);
                    new Alert(Alert.AlertType.INFORMATION, "✅ Rating updated successfully!").showAndWait();
                } else {
                    Rating newRating = new Rating(currentPatient, clinic, r, comment);
                    dao.add(newRating);
                    new Alert(Alert.AlertType.INFORMATION, "✅ Thank you for your rating!").showAndWait();
                }
                dialog.close();
                loadPatientAppointments();
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "❌ Failed: " + ex.getMessage()).showAndWait();
            }
        });

        content.getChildren().addAll(
                new Label("Your rating:"),
                starBox,
                new Label("Comment (optional):"),
                commentArea,
                submitBtn
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        dialog.showAndWait();
    }
}
