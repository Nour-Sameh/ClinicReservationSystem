package controller;

import dao.AppointmentDAO;
import dao.PatientDAO;
import dao.RatingDAO;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.*;
import service.*;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientController {

    @FXML private AnchorPane mainContentPane;
    @FXML private Button searchNavButton;
    @FXML private Button appointmentsNavButton;
    @FXML private Button waitingListNavButton;
    @FXML private Button chatNavButton;
    @FXML private VBox clinicsContainer;
    @FXML private Button logoutButton;
    @FXML private ComboBox<String> specialtyCombo;
    @FXML private Label patientNameLabel;
    @FXML private Button settingsButton;
    @FXML private VBox settingsBox;
    @FXML private TextField usernameField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField genderField;
    @FXML private TextField dobField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private VBox clinicInfoBox;
    @FXML private VBox appointmentsBox;
    @FXML private VBox reviewsBox;
    @FXML private VBox reportBox;

    private VBox searchView;

    private Patient currentPatient;
    private final ClinicService clinicService = new ClinicService();
    private final DepartmentService departmentService = new DepartmentService();
    private final PractitionerService practitionerService = new PractitionerService();
    private final RatingService ratingService = new RatingService();
    private Parent patientDashboardView;
    private Button activeButton = null;
    @FXML
    private void handleNavigation(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        String buttonId = sourceButton.getId();

        if ("searchNavButton".equals(buttonId)) {
            try {
                VBox searchView = new VBox(10);
                searchView.setPadding(new Insets(10, 0, 0, 0));

                Label label = new Label("Search for clinics by specialty");
                label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
                ComboBox<String> combo = new ComboBox<>();
                combo.getItems().addAll(specialtyCombo.getItems());
                combo.setValue(specialtyCombo.getValue());
                combo.setPrefWidth(220);
                combo.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-border-color: #cbd5e0;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-padding: 6 12;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4, 0, 0, 1);"
                );
                combo.setOnAction(e -> {
                    String selected = combo.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        specialtyCombo.setValue(selected);
                        filterClinicsBySpecialty();
                    }
                });

                ScrollPane scroll = new ScrollPane();
                scroll.setFitToWidth(true);
                scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scroll.setContent(clinicsContainer);
                scroll.setPrefHeight(600);

                searchView.getChildren().addAll(label, combo, scroll);
                mainContentPane.getChildren().setAll(searchView);
                AnchorPane.setTopAnchor(searchView, 0.0);
                AnchorPane.setBottomAnchor(searchView, 0.0);
                AnchorPane.setLeftAnchor(searchView, 0.0);
                AnchorPane.setRightAnchor(searchView, 0.0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if ("appointmentsNavButton".equals(buttonId)) {
            try {
                Label header = new Label("📅 My Appointments");
                header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #15BF8F; -fx-padding: 20 0 15 20;");

                FlowPane cardsContainer = new FlowPane();
                cardsContainer.setHgap(20);
                cardsContainer.setVgap(20);
                cardsContainer.setPadding(new Insets(0, 20, 20, 20));
                cardsContainer.setPrefWrapLength(650);

                AppointmentDAO appDAO = new AppointmentDAO();
                List<Appointment> apps = appDAO.getAppointmentsByPatientId(currentPatient.getID());

                if (apps.isEmpty()) {
                    cardsContainer.getChildren().add(new Label("No appointments yet."));
                } else {
                    for (Appointment a : apps) {
                        cardsContainer.getChildren().add(createAppointmentCardForPatient(a));
                    }
                }

                VBox content = new VBox(10);
                content.getChildren().addAll(header, cardsContainer);
                content.setStyle("-fx-background-color: white;");

                ScrollPane scrollPane = new ScrollPane(content);
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);
                scrollPane.setStyle("-fx-background: white;");

                mainContentPane.getChildren().setAll(scrollPane);
                AnchorPane.setTopAnchor(scrollPane, 0.0);
                AnchorPane.setBottomAnchor(scrollPane, 0.0);
                AnchorPane.setLeftAnchor(scrollPane, 0.0);
                AnchorPane.setRightAnchor(scrollPane, 0.0);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to load appointments.");
            }

        } else if ("waitingListNavButton".equals(buttonId)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/WaitingList.fxml"));
                Parent root = loader.load();
                WaitingListController controller = loader.getController();
                controller.setPatient(currentPatient);
                mainContentPane.getChildren().setAll(root);
                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if ("chatNavButton".equals(buttonId)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Chats.fxml"));
                Parent root = loader.load();
                ChatsController controller = loader.getController();
                controller.setPatient(currentPatient);
                mainContentPane.getChildren().setAll(root);
                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to open chat screen.");
            }
        }
    }

    @FXML
    public void initialize() {
        loadSpecialties();
        Platform.runLater(() -> {
            createSearchView();
            patientDashboardView = searchView; // ★★ احفظه هنا ★★
            mainContentPane.getChildren().setAll(searchView);
            AnchorPane.setTopAnchor(searchView, 0.0);
            AnchorPane.setBottomAnchor(searchView, 0.0);
            AnchorPane.setLeftAnchor(searchView, 0.0);
            AnchorPane.setRightAnchor(searchView, 0.0);
            loadAllClinics();
        });
    }
    public void setPatient(Patient patient) {
        this.currentPatient = patient;
        System.out.println("✅ currentPatient set: " + (patient != null ? patient.getName() : "null"));

        Platform.runLater(() -> {
            if (patientNameLabel != null) {
                patientNameLabel.setText("Hello " + patient.getName());
            }
        });
    }

    private void loadSpecialties() {
        try {
            List<String> names = departmentService.getAllUniqueSpecialtyNames();
            specialtyCombo.getItems().clear();
            specialtyCombo.getItems().add("All Specialties");
            specialtyCombo.getItems().addAll(names);
            specialtyCombo.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createSearchView() {
        searchView = new VBox(10);
        searchView.setPadding(new Insets(10, 0, 0, 0));

        Label label = new Label("Search for clinics by specialty");
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(specialtyCombo.getItems());
        combo.setValue(specialtyCombo.getValue());
        combo.setPrefWidth(220);
        combo.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #cbd5e0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 6 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4, 0, 0, 1);"
        );
        combo.setOnAction(e -> {
            String selected = combo.getSelectionModel().getSelectedItem();
            if (selected != null) {
                specialtyCombo.setValue(selected);
                filterClinicsBySpecialty();
            }
        });

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setContent(clinicsContainer);
        scroll.setPrefHeight(520);

        searchView.getChildren().addAll(label, combo, scroll);

        AnchorPane.setTopAnchor(searchView, 0.0);
        AnchorPane.setLeftAnchor(searchView, 0.0);
        AnchorPane.setRightAnchor(searchView, 0.0);
        AnchorPane.setBottomAnchor(searchView, 0.0);
    }

    private void loadAllClinics() {
        if (clinicsContainer == null) return;
        clinicsContainer.getChildren().clear();
        try {
            List<Clinic> allClinics = clinicService.getAllClinicsForPatientView();
            loadClinics(allClinics);
        } catch (SQLException e) {
            e.printStackTrace();
            clinicsContainer.getChildren().add(new Label("Error loading clinics."));
        }
    }
    private HBox createRatingView(double rating) {
        HBox container = new HBox(3);
        final String FULL_STAR = "★", EMPTY_STAR = "☆";
        final String GOLD = "#FFD700", LIGHT_GRAY = "#CCCCCC";

        rating = Math.max(0, Math.min(5, rating));
        double rounded = Math.round(rating * 2) / 2.0;
        int full = (int) rounded;
        boolean half = (rounded - full) == 0.5;

        for (int i = 0; i < 5; i++) {
            Text t = new Text();
            t.setFont(Font.font(16));
            if (i < full) {
                t.setText(FULL_STAR);
                t.setStyle("-fx-fill: " + GOLD);
            } else if (i == full && half) {
                t.setText(FULL_STAR);
                t.setStyle("-fx-fill: " + GOLD + "; -fx-opacity: 0.5;");
            } else {
                t.setText(EMPTY_STAR);
                t.setStyle("-fx-fill: " + LIGHT_GRAY);
            }
            container.getChildren().add(t);
        }

        if (rating > 0) {
            Label num = new Label(String.format(" (%.1f)", rounded));
            num.setStyle("-fx-font-size: 14px; -fx-text-fill: #555; -fx-padding: 0 0 0 3;");
            container.getChildren().add(num);
        } else {
            Label na = new Label(" (—)");
            na.setStyle("-fx-font-size: 14px; -fx-text-fill: #999; -fx-padding: 0 0 0 3;");
            container.getChildren().add(na);
        }

        return container;
    }
    private void loadClinics(List<Clinic> clinics) {
        if (clinicsContainer == null) return;
        clinicsContainer.getChildren().clear();

        for (Clinic c : clinics) {
            try {
                String deptName = c.getDepartmentName() != null ? c.getDepartmentName() : "General";
                Department dept = departmentService.getAllDepartments().stream()
                        .filter(d -> d.getID() == c.getDepartmentID())
                        .findFirst()
                        .orElse(null);
                if (dept != null) deptName = dept.getName();

                String doctorName = c.getDoctorName() != null ? c.getDoctorName() : "—";

                AnchorPane card = new AnchorPane();
                card.setPrefSize(750, 180);
                card.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1); -fx-padding: 15;");

                VBox info = new VBox(5);
                AnchorPane.setLeftAnchor(info, 15.0);
                AnchorPane.setTopAnchor(info, 15.0);
                info.setPrefWidth(500);

                HBox header = new HBox(10);
                Label name = new Label(c.getName());
                name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #34495E;");
                Label spec = new Label(deptName);
                spec.setStyle("-fx-background-color: #E6F8F5; -fx-padding: 3 8; -fx-background-radius: 5; -fx-text-fill: #1ABC9C; -fx-font-size: 12px; -fx-font-weight: bold;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                header.getChildren().addAll(name, spacer, spec);

                Label doctor = new Label("Dr. " + doctorName);
                doctor.setStyle("-fx-font-size: 14px; -fx-text-fill: #1ABC9C; -fx-font-weight: bold;");

                Label addr = new Label(c.getAddress());
                addr.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

                HBox details = new HBox(25);
                String dur = (c.getSchedule() != null) ? c.getSchedule().getSlotDurationInMinutes() + " min slots" : "N/A";
                Label slot = new Label(dur);
                slot.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

                double avgRat=ratingService.calculateAverageRating(c.getID());
                HBox ratingView = createRatingView(avgRat);

                Label priceLabel = new Label(String.format(" | %.2f EGP", c.getPrice()));
                priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

                HBox ratingAndPrice = new HBox(5, ratingView, priceLabel);
                priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
                details.getChildren().addAll(slot, ratingAndPrice);

                Label days = new Label("Working Hours: " + (c.getAvailableDaysString().isEmpty() ? "N/A" : c.getAvailableDaysString()));
                days.setStyle("-fx-font-size: 14px; -fx-text-fill: #2ECC71; -fx-font-weight: bold;");

                info.getChildren().addAll(header, doctor, addr, details, days);
                card.getChildren().add(info);

                HBox btns = new HBox(10);
                AnchorPane.setRightAnchor(btns, 15.0);
                AnchorPane.setBottomAnchor(btns, 15.0);

                Button chatBtn = new Button("Start Chat");
                chatBtn.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-text-fill: #333; -fx-border-radius: 5;");
                chatBtn.setPrefSize(120, 35);
                chatBtn.setOnAction(e -> openChatScreen(c));

                Button slotsBtn = new Button("View Slots");
                slotsBtn.setStyle("-fx-background-color: #1ABC9C; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5;");
                slotsBtn.setPrefSize(120, 35);
                slotsBtn.setOnAction(e -> {
                    try {
                        handleViewSlots(c);
                    } catch (SQLException | IOException ex) {
                        ex.printStackTrace();
                        showAlert("Error", "Could not open slots view.");
                    }
                });

                btns.getChildren().addAll(chatBtn, slotsBtn);
                card.getChildren().add(btns);

                clinicsContainer.getChildren().add(card);

            } catch (Exception e) {
                e.printStackTrace();
                clinicsContainer.getChildren().add(new Label("⚠️ Error loading clinic."));
            }
        }
    }

    private void openChatScreen(Clinic clinic) {
        try {
            Practitioner doc = practitionerService.getPractitionerById(clinic.getDoctorID());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Chats.fxml"));
            Parent root = loader.load();
            ChatsController controller = loader.getController();
            controller.initializeChatWithSpecificPractitioner(currentPatient, doc);

            Stage stage = (Stage) clinicsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Chat with Dr. " + doc.getName());
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Failed to open chat.");
        }
    }

    private void handleViewSlots(Clinic selectedClinic) throws SQLException, IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/clinic_slots_view.fxml"));
        Parent root = loader.load();
        ClinicSlotsController controller = loader.getController();
        Clinic fullyLoadedClinic = clinicService.loadClinicSchedule(selectedClinic);
        controller.setClinic(fullyLoadedClinic, currentPatient);

        Stage stage = (Stage) clinicsContainer.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("View Slots: " + selectedClinic.getName());
        stage.show();
    }

    private void filterClinicsBySpecialty() {
        String selected = specialtyCombo.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isEmpty()) return;

        try {
            int deptId = "All Specialties".equals(selected)
                    ? 0 : departmentService.getDepartmentIdByName(selected);

            List<Clinic> list = deptId == 0
                    ? clinicService.getAllClinicsForPatientView()
                    : clinicService.getClinicsBySpecialt(deptId);

            loadClinics(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSettings() {
        if (mainContentPane != null) mainContentPane.setVisible(false);
        if (settingsBox != null) {
            settingsBox.setVisible(true);
            loadCurrentUserSettings();
        } else {
            showAlert("Error", "Settings UI not loaded.");
        }
    }

    private void loadCurrentUserSettings() {
        if (currentPatient != null) {
            usernameField.setText(currentPatient.getName() != null ? currentPatient.getName() : "");

            emailField.setText(currentPatient.getEmail());
            phoneField.setText(currentPatient.getPhone());
            genderField.setText(currentPatient.getGender());
            dobField.setText(currentPatient.getDateOfBirth() != null ?
                    currentPatient.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—");

            usernameField.setEditable(true);

            emailField.setEditable(false);
            phoneField.setEditable(false);
            genderField.setEditable(false);
            dobField.setEditable(false);
            emailField.setOpacity(0.7);
            phoneField.setOpacity(0.7);
            genderField.setOpacity(0.7);
            dobField.setOpacity(0.7);
        }
    }
    @FXML
    private void handleSettingsCancel() {
        if (settingsBox != null) settingsBox.setVisible(false);

        if (mainContentPane != null) {
            mainContentPane.setVisible(true);
        } else if (clinicInfoBox != null) {
            clinicInfoBox.setVisible(true);
        }
    }

    @FXML
    private void handleSettingsSave() {
        try {
            String newUsername = usernameField.getText().trim();
            String currentPass = currentPasswordField.getText();
            String newPass = newPasswordField.getText();
            String confirmPass = confirmPasswordField.getText();

            if (newUsername.isEmpty()) {
                showAlert("Error", "Username cannot be empty.");
                return;
            }

            PatientDAO patientDAO = new PatientDAO();
            if (!newUsername.equals(currentPatient.getName())) {
                if (patientDAO.isNameTaken(newUsername, currentPatient.getID())) {
                    showAlert("Error", "This username is already taken.");
                    return;
                }
            }


            if (!newUsername.equals(currentPatient.getName())) {
                currentPatient.setName(newUsername);
            }


            if (!newPass.isEmpty()) {
                if (currentPass.isEmpty()) {
                    showAlert("Error", "Enter current password.");
                    return;
                }
                if (!newPass.equals(confirmPass)) {
                    showAlert("Error", "Passwords don’t match.");
                    return;
                }
                if (!currentPass.equals(currentPatient.getPassword())) {
                    showAlert("Error", "Current password is wrong.");
                    return;
                }
                currentPatient.setPassword(newPass);
            }

            patientDAO.update(currentPatient);
            if (patientNameLabel != null) {
                patientNameLabel.setText("Welcome, " + currentPatient.getName());
            }

            showAlert("Success", "Profile updated!");
            handleSettingsCancel();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Update failed."+ e.getMessage());

        }
    }
//    private void openGoogleMaps(String address) {
//        if (address == null || address.trim().isEmpty()) return;
//        try {
//            String encoded = java.net.URLEncoder.encode(address.trim(), "UTF-8").replace("+", "%20");
//            java.awt.Desktop.getDesktop().browse(new java.net.URI(
//                    "https://www.google.com/maps/search/?api=1&query=" + encoded));
//        } catch (Exception e) {
//            e.printStackTrace();
//            showAlert("Error", "Failed to open map.");
//        }
//    }
    private void openGoogleMaps(String address) {
        if (address == null || address.trim().isEmpty()) {
            showAlert("Warning", "Address is empty. Cannot open map.");
            return;
        }

        try {
            String encoded = java.net.URLEncoder.encode(address.trim(), "UTF-8")
                    .replace("+", "%20");

            String url = "https://www.google.com/maps/search/?api=1&query=" + encoded;

            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    desktop.browse(new java.net.URI(url));
                    return;
                }
            }

            String msg = "Cannot open map automatically on this device.\n"
                    + "Copy and paste the link below into your browser:\n\n"
                    + url;
            showAlert("Info", msg);

        } catch (java.net.URISyntaxException e) {
            showAlert("Error", "Invalid URL: " + e.getMessage());
        } catch (java.io.IOException e) {
            showAlert("Error", "Failed to open browser: " + e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "Unexpected error: " + e.getMessage());
        }
    }

    private String getStatusColor(String status) {
        return switch (status) {
            case "Booked" -> "#2E7D32";
            case "Completed" -> "#1565C0";
            case "Cancelled_by_Patient", "Cancelled_by_Doctor" -> "#D32F2F";
            default -> "#666";
        };
    }

    private String getStatusBorderColor(String status) {
        return switch (status) {
            case "Booked" -> "#4CAF50";
            case "Completed" -> "#2196F3";
            case "Cancelled_by_Patient", "Cancelled_by_Doctor" -> "#F44336";
            default -> "#E0E0E0";
        };
    }

    private void confirmCancelAndRate(Appointment appointment) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Appointment");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Do you want to cancel this appointment?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    appointment.setStatus(Status.Cancelled_by_Patient);
                    new AppointmentDAO().updateStatus(appointment.getId(), appointment.getStatus());
                    showRatingDialog(appointment.getClinic(), null);
                    showAlert("Success", "Appointment cancelled.");
                    handleNavigation(new ActionEvent(appointmentsNavButton, appointmentsNavButton));
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to cancel appointment.");
                }
            }
        });
    }

    private void showRatingDialog(Clinic clinic, Rating existingRating) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existingRating == null ? "Add Rating" : "Update Rating");
        dialog.setHeaderText("How was your experience with " + clinic.getName() + "?");

        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        Label q1 = new Label("⭐ Please rate:");
        HBox starsBox = new HBox(5);
        ToggleGroup group = new ToggleGroup();
        RadioButton[] stars = new RadioButton[5];
        for (int i = 0; i < 5; i++) {
            stars[i] = new RadioButton("★");
            stars[i].setToggleGroup(group);
            stars[i].setUserData(5 - i);
            starsBox.getChildren().add(stars[i]);
        }

        if (existingRating != null) {
            int prevScore = existingRating.getScore();
            if (prevScore >= 1 && prevScore <= 5) {
                stars[5 - prevScore].setSelected(true);
            }
        } else {
            stars[0].setSelected(true);
        }

        Label q2 = new Label("💬 Optional comment:");
        TextArea commentArea = new TextArea();
        commentArea.setPrefRowCount(3);
        commentArea.setPromptText("Tell us more...");
        if (existingRating != null && existingRating.getComment() != null) {
            commentArea.setText(existingRating.getComment());
        }

        ButtonType submitBtn = new ButtonType(existingRating == null ? "Add" : "Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtn, ButtonType.CANCEL);
        content.getChildren().addAll(q1, starsBox, q2, commentArea);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitBtn) {
                RadioButton selected = (RadioButton) group.getSelectedToggle();
                int score = selected != null ? (int) selected.getUserData() : 5;
                String comment = commentArea.getText().trim();

                try {
                    RatingDAO ratingDAO = new RatingDAO();
                    if (existingRating == null) {
                        Rating newRating = new Rating(
                                currentPatient, clinic, score,
                                comment.isEmpty() ? null : comment,
                                LocalDateTime.now()
                        );
                        ratingDAO.add(newRating);
                    } else {
                        existingRating.setScore(score);
                        existingRating.setComment(comment.isEmpty() ? null : comment);
                        ratingDAO.update(existingRating);
                    }
                    Platform.runLater(() -> showAlert("Success", existingRating == null ?
                            "Rating added!" : "Rating updated!"));
                } catch (SQLException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showAlert("Error", "Failed to save rating."));
                }
            }
            return dialogButton;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage s = (Stage) ((Node) event.getSource()).getScene().getWindow();
            s.setScene(new Scene(root));
            s.setTitle("Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // ==================== APPOINTMENT CARDS ====================
    private HBox createAppointmentCardForPatient(Appointment a) {
        TimeSlot slot = a.getAppointmentDateTime();
        Clinic appointmentClinic = a.getClinic();
        if (slot == null || appointmentClinic == null ) {
            return new HBox(new Label("⚠️ Incomplete appointment data"));
        }

        Clinic clinic;
        try {
            int doctorId = a.getClinic().getDoctorID();
            clinic = clinicService.getClinicByPractitionerId(doctorId);
            if (clinic == null) {

                return new HBox(new Label("⚠️ Clinic not found for this appointment"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new HBox(new Label("⚠️ Failed to load clinic info"));
        }

        Status status = a.getStatus();

        VBox card = new VBox(10);
        card.setPrefWidth(300);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; " +
                "-fx-border-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2); " +
                "-fx-border-width: 1px; " +
                "-fx-border-color: " + getStatusBorderColor(status.name()) + ";");

        HBox header = new HBox(10);
        Label clinicLabel = new Label("🏥 " + clinic.getName());
        clinicLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495E;");
        System.out.println(">>> Clinic ID: " + clinic.getID() + " | Name: ["+ clinic.getName() + "] | Doctor: " + clinic.getDoctorName());
        Button mapBtn = new Button("📍");
        mapBtn.setTooltip(new Tooltip("Open location"));
        mapBtn.setStyle("-fx-background-color: #15BF8F; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 6;");
        mapBtn.setOnAction(e -> openGoogleMaps(clinic.getAddress()));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(clinicLabel, spacer, mapBtn);

        Label doctorLabel = new Label("👤 Dr. " + clinic.getDoctorName());

        Label dateLabel = new Label("📅 " + slot.getDate() + " | ⏰ " +
                slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
        Label priceLabel = new Label("💰 " + String.format("%.2f EGP", clinic.getPrice()));
        Label statusLabel = new Label("📌 " + status);
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + getStatusColor(status.name()) + ";");

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);

        if (status == Status.Booked) {
            Button cancelBtn = new Button("❌ Cancel");
            cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px;");
            cancelBtn.setOnAction(e -> confirmCancelAndRate(a));
            actions.getChildren().add(cancelBtn);
        }

        if (status == Status.Completed) {
            try {
                Rating existingRating = new RatingDAO().getRatingByPatientAndClinic(
                        currentPatient.getID(), clinic.getID());
                Button rateBtn = new Button(existingRating == null ? "⭐ Add Rating" : "🔄 Update Rating");
                rateBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 12px;");
                rateBtn.setOnAction(e -> showRatingDialog(clinic, existingRating));
                actions.getChildren().add(rateBtn);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

//        if (clinic.getConsultationPrice() > 0 &&
//                (status == Status.Booked || status == Status.Completed)) {
//            Button returnBtn = new Button("🔁 Request Follow-up");
//            returnBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px;");
//            returnBtn.setOnAction(e -> showAlert("Success", "Follow-up request sent to Dr. " + clinic.getDoctorName()));
//            actions.getChildren().add(returnBtn);
//        }

        card.getChildren().addAll(header, doctorLabel, dateLabel, priceLabel, statusLabel, actions);
        return new HBox(card);
    }
    @FXML
    private void HoverNavButton(javafx.scene.input.MouseEvent e) {
        Button btn = (Button) e.getSource();
        if (btn != activeButton) {
            btn.setStyle("-fx-background-color: linear-gradient(to bottom, #e2f5f0, #cdeee7);" +
                    "-fx-text-fill: #333; -fx-font-size: 16px; -fx-font-weight: bold;" +
                    "-fx-padding: 8 24; -fx-background-radius: 25; -fx-border-radius: 25;" +
                    "-fx-border-color: #a3c5c0; -fx-border-width: 1;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0.3, 0, 1);");
        }
    }

    @FXML
    private void ResetNavButton(javafx.scene.input.MouseEvent e) {
        Button btn = (Button) e.getSource();
        if (btn != activeButton) {
            btn.setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #e8f6f3);" +
                    "-fx-text-fill: #444; -fx-font-size: 16px; -fx-font-weight: bold;" +
                    "-fx-padding: 8 24; -fx-background-radius: 25; -fx-border-radius: 25;" +
                    "-fx-border-color: #c8d6d5; -fx-border-width: 1;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0.2, 0, 2);");
        }
    }
    @FXML
    private void PressNavButton(javafx.scene.input.MouseEvent e) {
        Button btn = (Button) e.getSource();
        if (activeButton != null && activeButton != btn) {
            activeButton.setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #e8f6f3);" +
                    "-fx-text-fill: #444; -fx-font-size: 16px; -fx-font-weight: bold;" +
                    "-fx-padding: 8 24; -fx-background-radius: 25; -fx-border-radius: 25;" +
                    "-fx-border-color: #c8d6d5; -fx-border-width: 1;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0.2, 0, 2);");
        }
        activeButton = btn;
        btn.setStyle("-fx-background-color: linear-gradient(to bottom, #b9e5db, #9ad7cd);" +
                "-fx-text-fill: #222; -fx-font-size: 16px; -fx-font-weight: bold;" +
                "-fx-padding: 8 24; -fx-background-radius: 25; -fx-border-radius: 25;" +
                "-fx-border-color: #7ab7ad; -fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0.3, 0, 1);");
    }
    @FXML
    private void HoverLogoutButton(javafx.scene.input.MouseEvent e) {
        Button btn = (Button) e.getSource();
        btn.setStyle("-fx-background-color: #0FAF88; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 10 25;");
    }

    @FXML
    private void ResetLogoutButton(javafx.scene.input.MouseEvent e) {
        Button btn = (Button) e.getSource();
        btn.setStyle("-fx-background-color: #0C7E5F; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 10 25;");
    }
}