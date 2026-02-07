package controller;

import dao.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.*;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.geometry.Pos;

public class EditClinicController {

    @FXML private TextField clinicNameField;
    @FXML private TextField clinicAddressField;
    @FXML private TextField slotDurationField;
    @FXML private TextField priceField;
    @FXML private VBox rulesContainer;
    @FXML private Label statusLabel;
    @FXML private CheckBox enableConsultationCheckBox;
    @FXML private VBox consultationFieldsBox;
    @FXML private TextField consultationPriceField;
    @FXML private TextField consultationDaysField;
    private DoctorController dashboardController;
    private Clinic clinic;
    private Practitioner doctor;

    private final ClinicDAO clinicDAO = new ClinicDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final WorkingHoursRuleDAO ruleDAO = new WorkingHoursRuleDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    private final List<RuleRow> ruleRows = new ArrayList<>();

    private class RuleRow {
        final ComboBox<DayOfWeek> dayCombo;
        final ComboBox<LocalTime> fromCombo;
        final ComboBox<LocalTime> toCombo;
        final Button deleteBtn;
        final HBox container;

        RuleRow(boolean isNew) {
            dayCombo = new ComboBox<>();
            dayCombo.getItems().addAll(DayOfWeek.values());
            dayCombo.setPrefWidth(110);
            dayCombo.setPromptText("Day");

            fromCombo = new ComboBox<>();
            toCombo = new ComboBox<>();
            initTimeCombos();

            deleteBtn = new Button("✖");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 14px;");
            deleteBtn.setPrefWidth(30);

            container = new HBox(8);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setStyle("-fx-padding: 8 10 8 10; -fx-background-color: #f8fbf9; -fx-border-color: #e0ebeb; -fx-border-width: 1; -fx-border-radius: 6; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);");
            container.getChildren().addAll(
                    new Label("Day:"), dayCombo,
                    new Label("From:"), fromCombo,
                    new Label("To:"), toCombo,
                    new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
                    deleteBtn
            );

            deleteBtn.setOnAction(e -> {
                rulesContainer.getChildren().remove(container);
                ruleRows.remove(this);
            });

            if (!isNew) {
                container.setStyle("-fx-padding: 8 10 8 10; -fx-background-color: #f0f7ff; -fx-border-color: #d0e3f5; -fx-border-width: 1; -fx-border-radius: 6; -fx-effect: none;");
            }
        }

        void setRule(WorkingHoursRule rule) {
            if (rule != null) {
                dayCombo.setValue(rule.getDay());
                fromCombo.setValue(rule.getStartTime());
                toCombo.setValue(rule.getEndTime());
            }
        }

        WorkingHoursRule getRule() {
            DayOfWeek d = dayCombo.getValue();
            LocalTime from = fromCombo.getValue();
            LocalTime to = toCombo.getValue();
            if (d == null || from == null || to == null || !from.isBefore(to)) return null;
            return new WorkingHoursRule(0, d, from, to);
        }

        private void initTimeCombos() {
            List<LocalTime> times = new ArrayList<>();
            for (int h = 6; h <= 22; h++) {
                times.add(LocalTime.of(h, 0));
                if (h < 22) times.add(LocalTime.of(h, 30));
            }
            fromCombo.getItems().setAll(times);
            toCombo.getItems().setAll(times);
            fromCombo.setPrefWidth(90);
            toCombo.setPrefWidth(90);
            fromCombo.setPromptText("HH:mm");
            toCombo.setPromptText("HH:mm");
        }
    }

    @FXML
    public void initialize() {

    }

    public void setClinic(Clinic clinic, Practitioner doctor) {
        this.clinic = clinic;
        this.doctor = doctor;
        this.dashboardController = dashboardController;
        clinicNameField.setText(clinic.getName());
        clinicAddressField.setText(clinic.getAddress());
        priceField.setText(String.format("%.2f", clinic.getPrice()));

        if (clinic.getSchedule() != null) {
            slotDurationField.setText(String.valueOf(clinic.getSchedule().getSlotDurationInMinutes()));
        }

        loadExistingRulesToUI();
        boolean hasConsultation = clinic.getConsultationPrice() > 0
                && clinic.getConsultationDurationDays() > 0;
        enableConsultationCheckBox.setSelected(hasConsultation);
        consultationPriceField.setText(String.format("%.2f", clinic.getConsultationPrice()));
        consultationDaysField.setText(String.valueOf(clinic.getConsultationDurationDays()));
        onConsultationToggle();
    }

    private void loadExistingRulesToUI() {
        rulesContainer.getChildren().clear();
        ruleRows.clear();

        List<WorkingHoursRule> existing = (clinic.getSchedule() != null)
                ? clinic.getSchedule().getWeeklyRules()
                : new ArrayList<>();

        if (existing.isEmpty()) {
            RuleRow newRow = new RuleRow(true);
            ruleRows.add(newRow);
            rulesContainer.getChildren().add(newRow.container);
        } else {
            for (WorkingHoursRule rule : existing) {
                RuleRow row = new RuleRow(false);
                row.setRule(rule);
                ruleRows.add(row);
                rulesContainer.getChildren().add(row.container);
            }
            addNewRuleRow();
        }
    }

    @FXML
    private void onAddRule() {
        addNewRuleRow();
    }

    private void addNewRuleRow() {
        RuleRow newRow = new RuleRow(true);
        ruleRows.add(newRow);
        rulesContainer.getChildren().add(newRow.container);
    }

    @FXML
    private void onSave() {
        try {
            String name = clinicNameField.getText().trim();
            String address = clinicAddressField.getText().trim();
            String slotStr = slotDurationField.getText().trim();
            String priceStr = priceField.getText().trim();

            if (name.isEmpty() || address.isEmpty() || slotStr.isEmpty() || priceStr.isEmpty()) {
                showAlert("Validation Error", "All fields are required.");
                return;
            }
            if (name.isEmpty() || name.length() < 2 || name.matches("\\d+")) {
                showAlert("Error", "Name must be at least 3 letters and cannot be only numbers.");
                return;
            }
            if (address.length()>7) {
                showAlert("Error", "Address must be more detailed ");
                return;
            }
            int slotDuration;
            double price;
            try {
                slotDuration = Integer.parseInt(slotStr);
                price = Double.parseDouble(priceStr);
                if (slotDuration <= 5 || price < 50) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Slot duration must be a positive integer. Price must be non-negative.");
                return;
            }

            boolean enableConsultation = enableConsultationCheckBox.isSelected();
            double consultationPrice = 0.0;
            int consultationDays = 0;

            if (enableConsultation) {
                try {
                    consultationPrice = Double.parseDouble(consultationPriceField.getText().trim());
                } catch (NumberFormatException e) {
                    showAlert("Validation Error", "Consultation price must be a valid number.");
                    return;
                }

                try {
                    consultationDays = Integer.parseInt(consultationDaysField.getText().trim());
                } catch (NumberFormatException e) {
                    showAlert("Validation Error", "Consultation days must be a valid integer.");
                    return;
                }

                if (consultationPrice >= price) {
                    showAlert("Validation Error",
                            "❌ Consultation price must be LESS than the main visit price (" + String.format("%.2f", price) + " EGP).");
                    return;
                }
                if (consultationDays <= 0 || consultationDays > 30) {
                    showAlert("Validation Error", "❌ Consultation duration must be between 1 and 30 days.");
                    return;
                }
            }

            List<WorkingHoursRule> newRules = new ArrayList<>();
            for (RuleRow row : ruleRows) {
                WorkingHoursRule r = row.getRule();
                if (r != null) newRules.add(r);
            }

            if (newRules.isEmpty()) {
                showAlert("Validation Error", "At least one working hour rule is required.");
                return;
            }

            Schedule newPending = new Schedule(0, slotDuration);
            scheduleDAO.add(newPending);

            for (WorkingHoursRule r : newRules) {
                ruleDAO.insertRule(newPending.getID(), r.getDay(), r.getStartTime(), r.getEndTime());
            }

            clinic.setName(name);
            clinic.setAddress(address);
            clinic.setPrice(price);
            clinic.setConsultationPrice(consultationPrice);
            clinic.setConsultationDurationDays(consultationDays);
            clinicDAO.update(clinic);

            LocalDate nextMonthStart = LocalDate.now().plusMonths(1).withDayOfMonth(1);
            String formattedDate = nextMonthStart.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            statusLabel.setText("✅ Saved! Changes will be active from " + formattedDate + ".");
            statusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
            Platform.runLater(() -> {
                Stage thisStage = (Stage) clinicNameField.getScene().getWindow();
                Stage owner = (Stage) thisStage.getOwner();
                if (owner != null) {
                    try {
                        Object ownerController = owner.getScene().getRoot().getUserData();
                        if (ownerController instanceof DoctorController) {
                            ((DoctorController) ownerController).refreshClinicInfo();
                        }
                    } catch (Exception ignored) { }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to save: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) clinicNameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @FXML
    private void onConsultationToggle() {
        boolean enabled = enableConsultationCheckBox.isSelected();
        consultationFieldsBox.setVisible(enabled);
        consultationFieldsBox.setManaged(enabled);
    }
}