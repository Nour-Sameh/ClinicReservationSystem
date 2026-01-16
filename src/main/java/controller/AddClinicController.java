package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import model.Clinic;
import model.Department;
import model.Practitioner;
import model.Schedule;
import model.WorkingHoursRule;

import service.ClinicService;
import dao.DepartmentDAO;
import dao.ScheduleDAO;
import dao.WorkingHoursRuleDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import service.NominatimService;

public class AddClinicController {

    @FXML private TextField nameField;
    @FXML private TextField addressField;
    @FXML private TextField priceField;
    @FXML private ComboBox<Department> departmentComboBox;

    private int doctorId;
    @FXML private TextField slotDurationField;
    @FXML private CheckBox satCheck, sunCheck, monCheck, tueCheck, wedCheck, thuCheck, friCheck;
    @FXML private TextField satStart, satEnd, sunStart, sunEnd, monStart, monEnd,
            tueStart, tueEnd, wedStart, wedEnd, thuStart, thuEnd, friStart, friEnd;

    @FXML private VBox Step1Container;
    @FXML private VBox Step2Container;
    @FXML private CheckBox enableConsultationCheck;
    @FXML private HBox consultationFieldsBox;
    @FXML private Label consultationHintLabel;
    @FXML private TextField consultationPriceField;
    @FXML private TextField consultationValidityField;
    @FXML private Button openMapButton;
    @FXML private Label addressStatusLabel;

    private ContextMenu addressSuggestionsMenu;
    private CompletableFuture<Void> currentRequestFuture;
    private ClinicService clinicService = new ClinicService();
    private DepartmentDAO departmentDAO = new DepartmentDAO();

    private ScheduleDAO scheduleDAO = new ScheduleDAO();
    private WorkingHoursRuleDAO workingRuleDAO = new WorkingHoursRuleDAO();

    private Practitioner currentDoctor;

    private List<WorkingHoursRule> workingRules = new ArrayList<>();
    private int slotDuration;
    private Integer tempScheduleId = null;

    public void setDoctor(Practitioner doctor) {
        System.out.println(doctor);
        this.currentDoctor = doctor;
    }

    @FXML
    public void initialize() {
        try {
            List<Department> departments = departmentDAO.getAll();
            departmentComboBox.getItems().addAll(departments);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load departments.");
            e.printStackTrace();
            return;
        }

        enableConsultationCheck.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            boolean visible = isNowSelected;
            consultationFieldsBox.setVisible(visible);
            consultationFieldsBox.setManaged(visible);
            consultationHintLabel.setVisible(visible);
        });

        addressSuggestionsMenu = new ContextMenu();
        addressSuggestionsMenu.setAutoHide(true);

        addressField.textProperty().addListener((obs, oldVal, newVal) -> {
            final String query = newVal;

            if (query == null || query.trim().length() < 3) {
                addressSuggestionsMenu.hide();
                return;
            }

            if (currentRequestFuture != null && !currentRequestFuture.isDone()) {
                currentRequestFuture.cancel(true);
            }

            currentRequestFuture = CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException ignored) {}

                List<String> suggestions = new ArrayList<>();
                try {
                    suggestions = NominatimService.getAddressSuggestions(query.trim());
                } catch (Exception e) {
                    System.err.println("Search failed: " + e.getMessage());
                }

                final TextField af = addressField;
                final Label statusLbl = addressStatusLabel;
                final ContextMenu menu = addressSuggestionsMenu;

                List<String> finalSuggestions = suggestions;
                Platform.runLater(() -> {
                    menu.getItems().clear();
                    if (finalSuggestions.isEmpty()) {
                        menu.hide();
                        return;
                    }

                    for (String s : finalSuggestions) {
                        MenuItem item = new MenuItem(s);
                        item.setOnAction(e -> {
                            af.setText(s);
                            menu.hide();
                            if (statusLbl != null) {
                                statusLbl.setText("✓ addres selected");
                            }
                        });
                        menu.getItems().add(item);
                    }

                    menu.show(af, Side.BOTTOM, 0, 5);
                });
            });
        });
    }
    @FXML
    private void handleNextStep(ActionEvent event) {
        System.out.println("handle next step function");

        String durationText = slotDurationField.getText().trim();
        if (durationText.isEmpty()) {
            showAlert("Error", "Please enter slot duration.");
            return;
        }

        try {
            slotDuration = Integer.parseInt(durationText);
            if (slotDuration <= 5) {
                showAlert("Error", "Slot duration must be positive.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Slot duration must be a number.");
            return;
        }

        workingRules.clear();

        try {
            addDayRule(workingRules, satCheck, satStart, satEnd, DayOfWeek.SATURDAY);
            addDayRule(workingRules, sunCheck, sunStart, sunEnd, DayOfWeek.SUNDAY);
            addDayRule(workingRules, monCheck, monStart, sunEnd, DayOfWeek.MONDAY);
            addDayRule(workingRules, tueCheck, tueStart, tueEnd, DayOfWeek.TUESDAY);
            addDayRule(workingRules, wedCheck, wedStart, wedEnd, DayOfWeek.WEDNESDAY);
            addDayRule(workingRules, thuCheck, thuStart, thuEnd, DayOfWeek.THURSDAY);
            addDayRule(workingRules, friCheck, friStart, friEnd, DayOfWeek.FRIDAY);
        }
        catch (IllegalArgumentException ex) {
            showAlert("Error", ex.getMessage());
            return;
        }

        if (workingRules.isEmpty()) {
            showAlert("Error", "Please select at least one working day.");
            return;
        }

        try {
            tempScheduleId = scheduleDAO.createSchedule(slotDuration);

            for (WorkingHoursRule rule : workingRules) {
                workingRuleDAO.insertRule(tempScheduleId, rule.getDay(), rule.getStartTime(), rule.getEndTime());
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Database Error", "Failed to save schedule.");
            return;
        }

        Step1Container.setVisible(false);
        Step2Container.setVisible(true);
    }

    @FXML
    private void handleAddClinic(ActionEvent event) {
        if (currentDoctor == null) {
            showAlert("Error", "Doctor session expired. Please log in again.");
            return;
        }

        if (tempScheduleId == null) {
            showAlert("Error", "Schedule not created. Please complete Step 1.");
            return;
        }

        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String priceText = priceField.getText().trim();
        Department selectedDept = departmentComboBox.getValue();

        if (name.isEmpty() || address.isEmpty() || priceText.isEmpty() || selectedDept == null) {
            showAlert("Error", "All fields are required!");
            return;
        }
        if (name.isEmpty() || name.length() < 2 || name.matches("\\d+")) {
            showAlert("Error", "Name must be at least 3 letters and cannot be only numbers.");
            return;
        }
        if (!address.matches("\\d+-[a-zA-Z]+")) {
            showAlert("Error", "Address must be digits, followed by '-', then letters only (e.g., 123-ABC).");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price < 50) {
                showAlert("Error", "Price should be at least 50.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Price must be a number.");
            return;
        }

        Schedule schedule = new Schedule(tempScheduleId, slotDuration, workingRules);
        Clinic clinic = new Clinic(0, selectedDept.getID(), name, address, price, schedule);
        clinic.setDoctorID(currentDoctor.getID());
        clinic.setDoctorName(currentDoctor.getName());

        if (enableConsultationCheck != null && enableConsultationCheck.isSelected()) {
            String consPriceText = consultationPriceField.getText().trim();
            String consDaysText = consultationValidityField.getText().trim();

            if (consPriceText.isEmpty() || consDaysText.isEmpty()) {
                showAlert("Error", "Consultation Price and Validity (Days) are required when enabled.");
                return;
            }
            double visitPrice = price;

            try {
                double consultationPrice = Double.parseDouble(consPriceText);
                if (consultationPrice < 0) {
                    showAlert("Error", "Consultation price cannot be negative.");
                    return;
                }
                if (consultationPrice >= visitPrice) {
                    showAlert("Error", "Consultation price must be less than visit price (" + visitPrice + " EGP).");
                    return;
                }
                clinic.setConsultationPrice(consultationPrice);
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid consultation price format.");
                return;
            }

            try {
                int consultationDays = Integer.parseInt(consDaysText);
                if (consultationDays < 1) {
                    showAlert("Error", "Consultation validity must be at least 1 day.");
                    return;
                }
                if (consultationDays > 30) {
                    showAlert("Error", "Consultation validity cannot exceed 30 days.");
                    return;
                }
                clinic.setConsultationDurationDays(consultationDays);
            } catch (NumberFormatException e) {
                showAlert("Error", "Consultation validity must be a positive integer.");
                return;
            }
        } else {
            clinic.setConsultationPrice(0.0);
            clinic.setConsultationDurationDays(0);
        }

        try {
            clinicService.addClinicForDoctor(clinic, currentDoctor);

            Clinic freshClinic = clinicService.getClinicByPractitionerId(currentDoctor.getID());
            if (freshClinic == null) {
                throw new IllegalStateException("Clinic was created but cannot be reloaded.");
            }

            currentDoctor.setClinic(freshClinic);

            showAlert("Success", "Clinic added successfully!");

            clearFields();
            tempScheduleId = null;

            loadDoctorDashboard(event);

        } catch (SQLException e) {
            showAlert("Error", "Failed to add clinic: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Unexpected Error", "An error occurred: " + e.getMessage());
        }
    }
    private void loadDoctorDashboard(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Doctor.fxml"));
        Parent root = loader.load();

        DoctorController controller = loader.getController();
        controller.setDoctor(currentDoctor);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Doctor Dashboard");
        stage.show();
    }

    private void addDayRule(List<WorkingHoursRule> rules, CheckBox check, TextField startField,
                            TextField endField, DayOfWeek day) {

        if (!check.isSelected()) return;

        String startText = startField.getText().trim();
        String endText = endField.getText().trim();

        if (startText.isEmpty() || endText.isEmpty()) {
            throw new IllegalArgumentException("Start and End times required for " + day);
        }

        LocalTime start = parseTime(startText, "start time for " + day);
        LocalTime end = parseTime(endText, "end time for " + day);

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End must be after start for " + day);
        }

        rules.add(new WorkingHoursRule(0, day, start, end));
    }

    private LocalTime parseTime(String text, String fieldName) {
        try {
            String[] parts = text.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = (parts.length == 2) ? Integer.parseInt(parts[1]) : 0;

            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                throw new IllegalArgumentException("Invalid time in " + fieldName);
            }

            return LocalTime.of(hour, minute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid time format in " + fieldName);
        }
    }

    private void clearFields() {
        nameField.clear();
        addressField.clear();
        priceField.clear();
        slotDurationField.clear();

        departmentComboBox.getSelectionModel().clearSelection();
        CheckBox[] checks = {satCheck, sunCheck, monCheck, tueCheck, wedCheck, thuCheck, friCheck};
        TextField[] starts = {satStart, sunStart, monStart, tueStart, wedStart, thuStart, friStart};
        TextField[] ends = {satEnd, sunEnd, monEnd, tueEnd, wedEnd, thuEnd, friEnd};

        for (CheckBox cb : checks) cb.setSelected(false);
        for (TextField t : starts) t.clear();
        for (TextField t : ends) t.clear();

        workingRules.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBackStep2(ActionEvent event) {
        System.out.println("Back from Step 2");

        if (tempScheduleId != null) {
            try {
                workingRuleDAO.deleteByScheduleId(tempScheduleId);
                scheduleDAO.deleteScheduleById(tempScheduleId);
                System.out.println("Deleted schedule and working hours rule for ID: " + tempScheduleId);
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to delete schedule data.");
            }
        }

        Step2Container.setVisible(false);
        Step1Container.setVisible(true);

        clearFields();

        tempScheduleId = null;
    }

    @FXML
    private void handleBackStep1(ActionEvent event) {
        System.out.println("Back from Step 1");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Clinic.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Clinic Page");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load Clinic page: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenMap() {
        String address = addressField.getText().trim();

        if (address.isEmpty()) {
            if (addressStatusLabel != null) {
                addressStatusLabel.setText("⚠️ Please enter an address first.");
            }
            return;
        }

        try {
            String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8)
                    .replace("+", "%20")
                    .replace(" ", "%20");

            String url = "https://www.google.com/maps/search/?api=1&query=" + encoded;
            java.awt.Desktop.getDesktop().browse(new URI(url));

            if (addressStatusLabel != null) {
                addressStatusLabel.setText("✅ Map opened — return after verification.");
            }

        } catch (Exception e) {
            String msg = e instanceof java.net.URISyntaxException ?
                    "❌ Invalid address (avoid special characters)." :
                    "❌ Failed to open map link.";
            if (addressStatusLabel != null) {
                addressStatusLabel.setText(msg);
            }
            e.printStackTrace();
        }
    }
}


