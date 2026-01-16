package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.Practitioner;
import java.io.IOException;

public class ClinicController {

    @FXML private Label welcomeLabel;
    @FXML private Label clinicMessage;
    @FXML private Button addClinicButton;
    @FXML private Button handleLogOut;

    private Practitioner doctor;

    public void setDoctor(Practitioner doctor) {
        this.doctor = doctor;
        System.out.printf(String.valueOf(doctor));
        if (doctor != null) {
            welcomeLabel.setText("Welcome, Dr. " + doctor.getName());
        }

    }

    @FXML
    private void handleAddClinic() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddClinic.fxml"));
            Parent root = loader.load();
            AddClinicController controller = loader.getController();
            controller.setDoctor(doctor);
            Stage stage = (Stage) addClinicButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Add Clinic");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home.fxml"));
            Stage stage = (Stage) handleLogOut.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Home");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}