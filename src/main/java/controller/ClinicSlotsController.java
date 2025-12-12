package controller;

import dao.AppointmentDAO;
import dao.TimeSlotDAO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.*;
import service.PatientService;
import service.WaitingListService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

public class ClinicSlotsController {

    @FXML private Label clinicNameLabel;
    @FXML private DatePicker datePicker;
    @FXML private VBox slotsContainer;

    private Clinic selectedClinic;
    private Patient currentPatient;
    private final PatientService patientService = new PatientService();
    private final WaitingListService waitingListService = new WaitingListService();

    public void setClinic(Clinic clinic, Patient patient) {
        this.selectedClinic = clinic;
        this.currentPatient = patient;

        clinicNameLabel.setText(clinic.getName());
        loadAvailableSlots(LocalDate.now());

        LocalDate today = LocalDate.now();
        datePicker.setValue(today);
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || selectedClinic.getSchedule() == null) {
                    setDisable(true);
                    return;
                }
                if (date.isBefore(LocalDate.now()) || date.isAfter(endOfMonth)) {
                    setDisable(true);
                    return;
                }
                List<DayOfWeek> workingDays = selectedClinic.getSchedule().getWeeklyRules()
                        .stream()
                        .map(rule -> rule.getDay())
                        .toList();
                if (!workingDays.contains(date.getDayOfWeek())) {
                    setDisable(true);
                }
            }
        });

        loadAvailableSlots(LocalDate.now());
    }

    private boolean isWorkingDay(LocalDate date) {
        if (selectedClinic.getSchedule() == null) return false;
        return selectedClinic.getSchedule().getWeeklyRules().stream()
                .anyMatch(rule -> rule.getDay() == date.getDayOfWeek());
    }

    @FXML
    private void handleDateChange() {
        if (datePicker.getValue() != null) {
            loadAvailableSlots(datePicker.getValue());
        }
    }

    private void loadAvailableSlots(LocalDate date) {
        slotsContainer.getChildren().clear();

        if (selectedClinic == null || selectedClinic.getSchedule() == null) {
            slotsContainer.getChildren().add(new Label("⚠️ Invalid clinic or schedule."));
            return;
        }

        try {
            TimeSlotDAO timeSlotDAO = new TimeSlotDAO();
            AppointmentDAO appointmentDAO = new AppointmentDAO();
            LocalDate today = LocalDate.now();

            List<TimeSlot> dbSlots = timeSlotDAO.getSlotsByClinic(selectedClinic.getID());
            boolean hasSlotsForDate = dbSlots.stream()
                    .anyMatch(slot -> slot.getDate().isEqual(date));

            if (!hasSlotsForDate) {
                LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
                if (date.isAfter(endOfMonth)) {
                    slotsContainer.getChildren().add(new Label("❌ Booking beyond current month is not allowed."));
                    return;
                }
                if (!isWorkingDay(date)) {
                    slotsContainer.getChildren().add(new Label("Doctor not working on selected date."));
                    return;
                }

                Schedule schedule = selectedClinic.getSchedule();
                schedule.generateTimeSlots(date, date);

                for (TimeSlot slot : schedule.getSlots()) {
                    if (!slot.getDate().isEqual(date)) continue;
                    boolean exists = dbSlots.stream()
                            .anyMatch(s -> s.getDate().equals(slot.getDate())
                                    && s.getStartTime().equals(slot.getStartTime())
                                    && s.getClinicId() == selectedClinic.getID());
                    if (!exists) {
                        slot.setClinicId(selectedClinic.getID());
                        slot.markAsAvailable();
                        timeSlotDAO.add(slot);
                    }
                }
                dbSlots = timeSlotDAO.getSlotsByClinic(selectedClinic.getID());
            }

            List<Integer> bookedSlotIds = appointmentDAO.getBookedSlotIdsByClinicAndDate(
                    selectedClinic.getID(), date
            );

            List<TimeSlot> availableForDay = dbSlots.stream()
                    .filter(slot -> slot.getDate().isEqual(date))
                    .filter(slot -> !bookedSlotIds.contains(slot.getId()))
                    .collect(Collectors.toList());

            if (availableForDay.isEmpty()) {
                boolean isWorking = selectedClinic.getSchedule().getWeeklyRules().stream()
                        .anyMatch(rule -> rule.getDay() == date.getDayOfWeek());
                String msg = isWorking ? "No slots available — all booked." : "Doctor not working.";
                Label label = new Label(msg);
                label.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                slotsContainer.getChildren().add(label);
            } else {
                availableForDay.forEach(slot -> slotsContainer.getChildren().add(createSlotCard(slot)));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            slotsContainer.getChildren().add(new Label("❌ Database error."));
        }
    }

    private HBox createSlotCard(TimeSlot slot) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);"
        );

        String timeText = slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")) +
                " - " + slot.getEndTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
        Label timeLabel = new Label(timeText);
        timeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button bookButton = new Button();
        String buttonText;
        String buttonStyle = "-fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 20; -fx-padding: 8 20;";

        try {
            AppointmentDAO appDAO = new AppointmentDAO();
            Appointment lastVisit = appDAO.getLastCompletedVisit(
                    currentPatient.getID(),
                    selectedClinic.getDoctorID()
            );

            boolean hasValidConsultation = selectedClinic.getConsultationPrice() > 0
                    && selectedClinic.getConsultationDurationDays() > 0;

            if (!hasValidConsultation || lastVisit == null) {
                buttonText = "Book Visit (" + String.format("%.2f", selectedClinic.getPrice()) + " EGP)";
                bookButton.setStyle("-fx-background-color: #1ABC9C; " + buttonStyle);
            } else {
                LocalDate expiry = lastVisit.getAppointmentDateTime().getDate()
                        .plusDays(selectedClinic.getConsultationDurationDays());
                if (!LocalDate.now().isAfter(expiry)) {
                    buttonText = "Book Consultation (" + String.format("%.2f", selectedClinic.getConsultationPrice()) + " EGP)";
                    bookButton.setStyle("-fx-background-color: #3498DB; " + buttonStyle);
                    bookButton.setUserData(Appointment.AppointmentType.CONSULTATION);
                } else {
                    buttonText = "Book Visit (" + String.format("%.2f", selectedClinic.getPrice()) + " EGP)";
                    bookButton.setStyle("-fx-background-color: #1ABC9C; " + buttonStyle);
                }
            }
            bookButton.setText(buttonText);

        } catch (SQLException e) {
            e.printStackTrace();
            buttonText = "Book Now (Error)";
            bookButton.setStyle("-fx-background-color: #E74C3C; " + buttonStyle);
            bookButton.setText(buttonText);
        }

        bookButton.setOnAction(e -> {
            bookButton.setDisable(true);
            handleBookSlot(slot, bookButton);
        });

        card.getChildren().addAll(timeLabel, spacer, bookButton);
        return card;
    }

    private void handleBookSlot(TimeSlot slot, Button bookButton) {
        if (currentPatient == null || selectedClinic == null) {
            System.err.println("❌ currentPatient or clinic is null");
            Platform.runLater(() -> {
                showAlert("Error", "Session expired. Please reopen slots.", Alert.AlertType.ERROR);
                bookButton.setDisable(false);
            });
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Booking");
        confirm.setHeaderText("Book this appointment?");
        String timeText = slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"))
                + " – " + slot.getEndTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
        confirm.setContentText(String.format(
                "Clinic: %s\nDate: %s\nTime: %s",
                selectedClinic.getName(),
                datePicker.getValue().format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")),
                timeText
        ));
        confirm.getButtonTypes().setAll(ButtonType.NO, ButtonType.YES);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Platform.runLater(() -> {
                    bookButton.setText("Booking...");
                    bookButton.setDisable(true);
                });

                new Thread(() -> {
                    try {
                        AppointmentDAO appDAO = new AppointmentDAO();

                        // ✅ التحقق: هل المريض عنده موعد سابق في نفس اليوم مع نفس العيادة؟
                        LocalDate selectedDate = slot.getDate();
                        List<Appointment> todayAppointments = appDAO.getAppointmentsByPatientId(currentPatient.getID());

                        boolean alreadyBooked = todayAppointments.stream()
                                .anyMatch(appt ->
                                        appt.getClinic().getID() == selectedClinic.getID() &&
                                                appt.getAppointmentDateTime().getDate().isEqual(selectedDate) &&
                                                appt.getStatus() == Status.Booked
                                );

                        if (alreadyBooked) {
                            Platform.runLater(() ->
                                    showAlert("Booking Limit",
                                            "You already have an appointment today at this clinic.\nOnly one appointment per day is allowed.",
                                            Alert.AlertType.WARNING)
                            );
                            Platform.runLater(() -> {
                                bookButton.setText("Book Now");
                                bookButton.setDisable(false);
                            });
                            return;
                        }

                        // ✅ لو مش محجوز — استمر في الحجز
                        Appointment newAppointment = new Appointment(currentPatient, selectedClinic, slot);

                        // ... (باقي الكود زي ما هو: نوع الاستشارة، expiry، الحفظ، إلخ)
                        appDAO.add(newAppointment);

                        Platform.runLater(() -> {
                            showAlert("Success", "Appointment booked!", Alert.AlertType.INFORMATION);
                            // ... التوجيه لشاشة المريض
                        });

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Platform.runLater(() ->
                                showAlert("Booking Failed", "Error: " + ex.getMessage(), Alert.AlertType.ERROR)
                        );
                    } finally {
                        Platform.runLater(() -> {
                            bookButton.setText("Book Now");
                            bookButton.setDisable(false);
                        });
                    }
                }).start();

            } else {
                Platform.runLater(() -> bookButton.setDisable(false));
            }
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Patient.fxml"));
            Parent root = loader.load();

            PatientController controller = loader.getController();
            // ★★ هذا هو السر ★★
            controller.setPatient(this.currentPatient); // ← مرّر نفس المريض اللي جاي من Dashboard

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Patient Dashboard");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load dashboard.", Alert.AlertType.ERROR);
        }
    }

    private void handleJoinWaitingList(Clinic clinic, LocalDate date) {
        if (clinic == null || date == null || currentPatient == null) {
            System.err.println("Error: Clinic, date, or patient data is missing.");
            return;
        }

        try {
            WaitingList newRequest = new WaitingList(currentPatient, clinic);
            waitingListService.addPatient(newRequest);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("✅ Successfully Joined Waiting List");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Dear " + currentPatient.getName() + ", you have been added to the waiting list for " +
                            clinic.getName() + " on " + date + ".\n" +
                            "We will notify you when a slot is available."
            );
            alert.showAndWait();

            loadAvailableSlots(date);

        } catch (SQLException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Database Error");
            errorAlert.setHeaderText("Failed to Join Waiting List");
            errorAlert.setContentText("Error: " + e.getMessage());
            errorAlert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Unexpected Error");
            errorAlert.setContentText("Error: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }
}