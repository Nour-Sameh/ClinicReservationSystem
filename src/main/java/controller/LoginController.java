package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Patient;
import model.Practitioner;
import model.Clinic;
import service.NotificationService;
import service.PatientService;
import service.PractitionerService;
import service.ClinicService;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private PatientService patientService;
    private PractitionerService practitionerService;
    private ClinicService clinicService;

    @FXML
    private void initialize() {
        patientService = new PatientService();
        practitionerService = new PractitionerService();
        clinicService = new ClinicService();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String input = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (input.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both Email and Password.");
            return;
        }

        try {
            boolean isPhone = input.matches("^01[0-25]\\d{8}$"); // 010, 011, 012, 015 + 8 أرقام
            boolean isEmail = input.contains("@") && input.contains(".");

            if (!isPhone && !isEmail) {
                showAlert("Invalid Input", "Please enter a valid Email or Egyptian Phone Number (e.g., 01012345678).");
                return;
            }
            // ---------------- Patient Login ----------------
            for (Patient p : patientService.getAllPatients()) {
                if (p.getEmail().equalsIgnoreCase(input) && p.getPassword().equals(password)) {
                    loadPatientDashboard(p, event);
                    return;
                }
               if (isPhone && (p.getPhone().equals(input) || p.getEmail().equals(input))){
                    loadPatientDashboard(p, event);
                    return;
                }
            }

            // ---------------- Practitioner Login ----------------
            for (Practitioner d : practitionerService.getAllPractitioners()) {
                if (d.getEmail().equalsIgnoreCase(input) && d.getPassword().equals(password)) {

                    // get clinic for practitioner
                    Clinic clinic = clinicService.getClinicByPractitionerId(d.getId());

                    if (clinic != null) {
                        System.out.println("go to doctorClinic Dashboard");
                        loadDoctorDashboard(d, event);

                    } else {
                        System.out.println("go to clinicDashboard");
                        loadClinicDashboard(d,clinic,event);
                    }
                    return;
                }
            }

            showAlert("Login Failed", "Incorrect Email or Password.");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while logging in.");
        }
    }

    private void loadPatientDashboard(Patient patient, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Patient.fxml"));
        Parent root = loader.load();

        PatientController controller = loader.getController();
        controller.setPatientName(patient.getName(), patient); // ✔ نمرر الاسم + كائن المريض

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Patient Dashboard");
        stage.show();
    }


    private void loadDoctorDashboard(Practitioner doctor, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Doctor.fxml"));
        Parent root = loader.load();

        DoctorController controller = loader.getController();

        Clinic clinic = clinicService.getClinicByPractitionerId(doctor.getID());
        if (clinic != null) {
            doctor.setClinic(clinic); // ← هذا هو السر!
        }
        controller.setDoctor(doctor);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Doctor Dashboard");
        stage.show();
    }

    private void loadClinicDashboard(Practitioner doctor, Clinic clinic, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Clinic.fxml"));
        Parent root = loader.load();

        ClinicController controller = loader.getController();
        controller.setDoctor(doctor);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("My Clinic");
        stage.show();
    }


    @FXML
    private void handleRegister(ActionEvent event) {
        loadPage("/register.fxml", event, "Create Account");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        loadPage("/home.fxml", event, "Clinic Reservation System");
    }

    private void loadPage(String fxml, ActionEvent event, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load page: " + fxml);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showAlert("Error", "Please enter your email to recover password.");
            return;
        }

        try {
            String password = null;

            // Check in Patients
            for (Patient p : patientService.getAllPatients()) {
                if (p.getEmail().equalsIgnoreCase(email)) {
                    password = p.getPassword();
                    break;
                }
            }

            // Check in Practitioners
            if (password == null) {
                for (Practitioner d : practitionerService.getAllPractitioners()) {
                    if (d.getEmail().equalsIgnoreCase(email)) {
                        password = d.getPassword();
                        break;
                    }
                }
            }

            if (password == null) {
                showAlert("Error", "Email not found in our system.");
                return;
            }

            // Send password via email
            NotificationService notificationService = new NotificationService();
            notificationService.sendEmail(email, "Password Recovery", "<h3>Your password is: "+password+"</h3>");

            showAlert("Success", "Your password has been sent to your email!");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not send email: " + e.getMessage());
        }
    }
}

