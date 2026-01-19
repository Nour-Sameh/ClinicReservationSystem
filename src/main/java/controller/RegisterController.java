package controller;

import dao.PatientDAO;
import dao.PractitionerDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Patient;
import model.Practitioner;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;

public class RegisterController {

    @FXML private CheckBox patientCheck;
    @FXML private CheckBox doctorCheck;

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;

    @FXML private Button registerButton;
    @FXML private Button backButton;
    @FXML private Hyperlink loginLink;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private DatePicker dobPicker;

    @FXML
    public void initialize() {
        patientCheck.setOnAction(e -> {
            if (patientCheck.isSelected()) doctorCheck.setSelected(false);
        });
        doctorCheck.setOnAction(e -> {
            if (doctorCheck.isSelected()) patientCheck.setSelected(false);
        });
        genderComboBox.getItems().addAll("Male", "Female", "Other");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        loadPage("/home.fxml", event, "Clinic Reservation System");
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        loadPage("/login.fxml", event, "Login");
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String name = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();
        String gender = genderComboBox.getValue();
        LocalDate dob = dobPicker.getValue();
        int age = Period.between(dob, LocalDate.now()).getYears();

        if (!patientCheck.isSelected() && !doctorCheck.isSelected()) {
            showAlert("Error", "Please select Patient or Practitioner.");
            return;
        }

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || gender == null || dob == null) {
            showAlert("Error", "All fields are required, including gender and date of birth.");
            return;
        }

        if (!name.matches("^[A-Za-z]+( [A-Za-z]+)*$") || name.length() < 3) {
            showAlert("Error", "Full Name must contain only letters and spaces, and be at least 3 characters.");
            return;
        }
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showAlert("Error", "Invalid email format.");
            return;
        }
        if (!phone.matches("^01[0-2,5][0-9]{8}$")) {
            showAlert("Error", "Invalid phone number.");
            return;
        }
        if (!password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$")) {
            showAlert("Error",
                    "Password must be at least 8 characters and include:\n" +
                            "- Uppercase letter (A-Z)\n" +
                            "- Lowercase letter (a-z)\n" +
                            "- Number (0-9)\n" +
                            "- Special character (@#$%^&+=!)"
            );
            return;
        }
        if (dob.isAfter(LocalDate.now())) {
            showAlert("Error", "Date of birth cannot be in the future.");
            return;
        }

        // age number validation
        if(age < 0 || age > 120|| ObjectUtils.isEmpty(age)) {
            showAlert("Error", "Please enter a valid date of birth.");
            return;
        }


        if (patientCheck.isSelected()) {
            if (age < 18) {
                showAlert("Error", "Patients must be at least 18 years old.");
                return;
            }
        } else if (doctorCheck.isSelected()) {
            if (age < 24) {
                showAlert("Error", "Practitioners must be at least 24 years old.");
                return;
            }
        }

        try {
            PatientDAO patientDAO = new PatientDAO();
            PractitionerDAO practitionerDAO = new PractitionerDAO();

            boolean exists = false;
            if (patientCheck.isSelected()) {
                for (Patient p : patientDAO.getAll()) {
                    if (p.getEmail().equals(email) || p.getPhone().equals(phone)) {
                        exists = true;
                        break;
                    }
                }
            } else {
                for (Practitioner d : practitionerDAO.getAll()) {
                    if (d.getEmail().equals(email) || d.getPhone().equals(phone)) {
                        exists = true;
                        break;
                    }
                }
            }

            if (exists) {
                showAlert("Warning", "This email or phone is already registered.");
                return;
            }
            if (patientCheck.isSelected()) {
                Patient patient = new Patient(0, name, phone, email, password, gender, dob);
                patientDAO.add(patient);
            } else {
                Practitioner practitioner = new Practitioner(0, name, phone, email, password, gender, dob);
                practitionerDAO.add(practitioner);
            }

            showAlert("Success", "Account created successfully!");
            loadPage("/login.fxml", event, "Login");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Registration failed: " + e.getMessage());
        }
    }

    private void loadPage(String fxmlPath, ActionEvent event, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load page: " + fxmlPath);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void onHoverIn(javafx.scene.input.MouseEvent event) {
        Button btn = (Button) event.getSource();
        String style = btn.getStyle();
        if (style.contains("#15BF8F")) {
            btn.setStyle(style.replace("#15BF8F", "#12A075"));
        } else if (style.contains("#ff5555")) {
            btn.setStyle(style.replace("#ff5555", "#e04444"));
        }
    }

    @FXML
    private void onHoverOut(javafx.scene.input.MouseEvent event) {
        Button btn = (Button) event.getSource();
        String style = btn.getStyle();
        if (style.contains("#12A075")) {
            btn.setStyle(style.replace("#12A075", "#15BF8F"));
        } else if (style.contains("#e04444")) {
            btn.setStyle(style.replace("#e04444", "#ff5555"));
        }
    }
}