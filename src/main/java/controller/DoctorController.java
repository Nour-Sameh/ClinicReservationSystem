
package controller;
import dao.AppointmentDAO;
import dao.DepartmentDAO;
import dao.PractitionerDAO;
import dao.RatingDAO;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;

import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import model.*;
import service.PractitionerService;
import util.ExcelExporter;
import util.PDFExporter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import javafx.application.Platform;



public class DoctorController {

    @FXML private Label clinicNameLabel;
    @FXML private Label specialtyLabel;
    @FXML private Label addressLabel;
    @FXML private Label slotDurationLabel;
    @FXML private Text scheduleText;
    @FXML private Label welcomeLabel;
    @FXML private Button editButton;
    @FXML private Button logoutButton;

    @FXML private VBox clinicInfoBox;
    @FXML private VBox appointmentsBox;
    @FXML private VBox appointmentsList;
    @FXML private HBox starsContainer;
    @FXML private DatePicker appointmentsCalendar;
    @FXML private AnchorPane mainContentPane;
    @FXML private VBox reviewsBox;
    @FXML private VBox reviewsList;
    @FXML private Button appointmentsNavButton;
    @FXML private Button reviewsNavButton;
    @FXML private Button chatNavButton;
    @FXML private Label priceLabel;
    @FXML private Button ClinicInfoMainBox;
    @FXML private Button cancelAllButton;
    @FXML private Button exportPDFButton;
    @FXML private Button exportExcelButton;
    @FXML private Button reportButton;
    @FXML private VBox reportBox;
    @FXML private VBox reportContent;
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
    @FXML private Button deleteClinicButton;


    private Practitioner currentDoctor;
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final PractitionerService practitionerService = new PractitionerService();
    private List<Appointment> allAppointments;

    @FXML
    public void initialize() {
        System.out.println("=== DoctorController.initialize() ===");
        System.out.println("mainContentPane = " + mainContentPane);
        System.out.println("clinicNameLabel = " + clinicNameLabel);
        System.out.println("appointmentsCalendar = " + appointmentsCalendar);
        if (appointmentsCalendar != null) {
            appointmentsCalendar.setOnAction(e -> filterAppointmentsByDate());
        }

    }

    public void setDoctor(Practitioner doctor) {
        if (doctor == null) {
            showNoClinic();
            return;
        }
        this.currentDoctor = doctor;
        welcomeLabel.setText("Welcome, Dr. " + safeString(doctor.getName(), "—"));
        Clinic clinic = doctor.getClinic();
        if (clinic == null) {
            showNoClinic();
            return;
        }
        clinicNameLabel.setText(safeString(clinic.getName(), "Unnamed Clinic"));
        try {
            Department dept = departmentDAO.getById(clinic.getDepartmentID());
            specialtyLabel.setText((dept != null) ? safeString(dept.getName(), "General") : "General");
        } catch (SQLException e) {
            specialtyLabel.setText("General");
        }


        double total = 0.0;
        int count = 0;
        try {
            List<Rating> ratings = new RatingDAO().getRatingsByClinicId(clinic.getID());
            System.out.println("Found " + ratings.size() + " rating(s) for clinic ID: " + clinic.getID());
            for (Rating r : ratings) {
                int score = r.getScore(); // ← تأكدي أن الدالة اسمها getScore()
                System.out.println(" Score: " + score + ", Comment: " + r.getComment());
                total += score;
                count++;
            }
        } catch (Exception e) {
            System.err.println(" Error loading ratings: " + e.getMessage());
            e.printStackTrace();
        }
        double avg = (count > 0) ? total / count : 0.0;
        System.out.println(" Final avg rating = " + avg);
        renderRatingStars(avg);

        addressLabel.setText(safeString(clinic.getAddress(), "No address"));
        int duration = (clinic.getSchedule() != null) ? clinic.getSchedule().getSlotDurationInMinutes() : 30;
        slotDurationLabel.setText(duration + " min slots");
        priceLabel.setText(String.format("Price: %.2f EGP", clinic.getPrice()));
        scheduleText.setText(buildScheduleText(clinic));
    }

    private void renderRatingStars(double rating) {
        starsContainer.getChildren().clear();
        final String FULL_STAR = "★", EMPTY_STAR = "☆", GOLD = "#FFD700", LIGHT_GRAY = "#CCCCCC";
        rating = Math.max(0, Math.min(5, rating));
        double r = Math.round(rating * 2) / 2.0;
        int full = (int) r;
        boolean half = (r - full) == 0.5;
        for (int i = 0; i < 5; i++) {
            Text t = new Text();
            t.setFont(Font.font(18));
            if (i < full) {
                t.setText(FULL_STAR); t.setStyle("-fx-fill: " + GOLD);
            } else if (i == full && half) {
                t.setText(FULL_STAR); t.setStyle("-fx-fill: " + GOLD + "; -fx-opacity: 0.5;");
            } else {
                t.setText(EMPTY_STAR); t.setStyle("-fx-fill: " + LIGHT_GRAY);
            }
            starsContainer.getChildren().add(t);
        }
        Label n = new Label(String.format(" (%.1f)", r));
        n.setStyle("-fx-font-size: 15px; -fx-text-fill: #555; -fx-padding: 0 0 0 5;");
        starsContainer.getChildren().add(n);
    }

    private String buildScheduleText(Clinic c) {
        if (c.getSchedule() == null || c.getSchedule().getWeeklyRules() == null || c.getSchedule().getWeeklyRules().isEmpty()) {
            return "No working hours";
        }
        StringBuilder sb = new StringBuilder();
        var rules = c.getSchedule().getWeeklyRules();
        for (int i = 0; i < rules.size(); i++) {
            if (i > 0) sb.append("\n");
            sb.append(rules.get(i).getDay().toString().substring(0, 3))
                    .append(": ").append(rules.get(i).getStartTime())
                    .append("–").append(rules.get(i).getEndTime());
        }
        return sb.toString();
    }

    private void showNoClinic() {
        clinicNameLabel.setText("—"); specialtyLabel.setText("—");
        renderRatingStars(0); addressLabel.setText("—");
        slotDurationLabel.setText("—"); scheduleText.setText("You don't have a clinic yet.");
    }

    private String safeString(String s, String f) {
        return (s != null && !s.trim().isEmpty()) ? s.trim() : f;
    }

    @FXML
    private void handleEdit() {
        if (currentDoctor != null && currentDoctor.getClinic() != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditClinic.fxml"));
                Parent root = loader.load();
                EditClinicController controller = loader.getController();
                controller.setClinic(currentDoctor.getClinic(), currentDoctor);


                Stage stage = new Stage();
                stage.setTitle("Edit Clinic");
                stage.setScene(new Scene(root));
                stage.show();


            } catch (IOException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Cannot open edit window").showAndWait();
            }
        }
    }
    @FXML private void handleLogout() {
        try {
            Parent r = FXMLLoader.load(getClass().getResource("/home.fxml"));
            Stage s = (Stage) logoutButton.getScene().getWindow();
            s.setScene(new Scene(r));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChat() {
        clinicInfoBox.setVisible(false);
        appointmentsBox.setVisible(false);
        reviewsBox.setVisible(false);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Chats.fxml"));
            Parent root = loader.load();
            ChatsController controller = loader.getController();

            if (currentDoctor instanceof Practitioner) {
                controller.setCurrentPractitioner((Practitioner) currentDoctor);
            } else {
                throw new IllegalStateException("currentDoctor is not a Practitioner!");
            }

            Stage stage = (Stage) chatNavButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Chats — Dr. " + currentDoctor.getName());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Cannot open chat: " + e.getMessage()).showAndWait();
        }
    }
    @FXML
    private void handleAppointments() {
        clinicInfoBox.setVisible(false);
        appointmentsBox.setVisible(true);
        reviewsBox.setVisible(false);

        if (appointmentsCalendar == null) {
            System.err.println("❌ خطأ: appointmentsCalendar = null! تحقق من fx:id و @FXML.");
            return;
        }

        if (currentDoctor == null || currentDoctor.getClinic() == null) {
            showAlert("Error", "Doctor or clinic is not available.");
            return;
        }

        try {
            allAppointments = new AppointmentDAO().getAppointmentsByClinicId(currentDoctor.getClinic().getID());
            populateCalendarWithAppointments(allAppointments);
            displayAppointments(allAppointments, null);

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error loading appointments.").show();
        }
    }
    //
    private void populateCalendarWithAppointments(List<Appointment> apps) {
        Set<LocalDate> datesWithAppointments = apps.stream()
                .map(Appointment::getAppointmentDateTime)
                .filter(Objects::nonNull)
                .map(TimeSlot::getDate)
                .filter(d -> !d.isBefore(LocalDate.now())) // فقط الأيام الحالية والمستقبلية
                .collect(Collectors.toSet());

        appointmentsCalendar.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setDisable(true);
                    return;
                }

                if (datesWithAppointments.contains(item)) {
                    // ✅ يوم فيه مواعيد: مفعّل + لون جميل
                    setStyle("-fx-background-color: #d0f0d0; -fx-text-fill: #2e7d32;");
                    setDisable(false);
                } else {
                    // ❌ يوم فاضي: معطّل + لون باهت
                    setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #aaa;");
                    setDisable(true);
                }
            }
        });

        // اختيار تلقائي لأول يوم فيه مواعيد (اختياري لكن لطيف)
        if (!datesWithAppointments.isEmpty()) {
            LocalDate first = datesWithAppointments.stream().min(LocalDate::compareTo).orElse(null);
            appointmentsCalendar.setValue(first);
            displayAppointments(allAppointments, first); // عرض مواعيد أول يوم
        } else {
            appointmentsCalendar.setValue(null);
            displayAppointments(allAppointments, null);
        }
    }


    private void displayAppointments(List<Appointment> all, LocalDate filterDate) {

        appointmentsList.getChildren().clear();
        LocalDate today = LocalDate.now();

        for (Appointment a : all) {
            TimeSlot slot = a.getAppointmentDateTime();
            if (slot == null) continue;

            LocalDate appDate = slot.getDate();
            if (a.getStatus() == Status.Booked && !appDate.isBefore(today)) {
                if (filterDate == null) {
                    appointmentsList.getChildren().add(createAppointmentCard(a));
                } else if (appDate.isEqual(filterDate)) {
                    appointmentsList.getChildren().add(createAppointmentCard(a));
                }
            }
        }


        if (appointmentsList.getChildren().isEmpty()) {
            String msg = (filterDate == null)
                    ? "No upcoming appointments."
                    : "No appointments on " + filterDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d"));
            Label label = new Label(msg);
            label.setStyle("-fx-text-fill: #777; -fx-font-style: italic;");
            appointmentsList.getChildren().add(label);
        }
    }

    private HBox createAppointmentCard(Appointment a) {
        TimeSlot slot = a.getAppointmentDateTime();
        if (slot == null) return new HBox();

        HBox box = new HBox(10);
        box.setMinHeight(50);
        box.setPadding(new Insets(8, 12, 8, 12));

        // ✅ لون الخلفية حسب الحالة
        String bgColor, borderColor;
        switch (a.getStatus()) {
            case Cancelled_by_Patient:
            case Cancelled_by_Doctor:
                bgColor = "#fdf2f2";
                borderColor = "#fadbd8";
                break;
            case Completed:
                bgColor = "#f6fef9";
                borderColor = "#d5f5e3";
                break;
            case Absent: // ← دعم الحالة الجديدة في العرض (لو مستخدمة)
                bgColor = "#fef9e7";
                borderColor = "#f9e79f";
                break;
            default: // Booked
                bgColor = "#ffffff";
                borderColor = "#e0e0e0";
        }

        box.setStyle("-fx-background-color: " + bgColor + "; " +
                "-fx-border-color: " + borderColor + "; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-alignment: CENTER_LEFT; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 2, 0, 0, 1);");

        String patientName = (a.getPatient() != null) ? a.getPatient().getName() : "—";
        Label patientLabel = new Label("👤 " + patientName);
        patientLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        String timeText = slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
        // ✅ حساب وقت الانتهاء (استنادًا لمدة السلوت)
        int durationMinutes = a.getClinic() != null && a.getClinic().getSchedule() != null
                ? a.getClinic().getSchedule().getSlotDurationInMinutes()
                : 30;
        LocalDateTime endTime = LocalDateTime.from(slot.getStartTime().plusMinutes(durationMinutes));
        String endTimeText = endTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
        Label timeLabel = new Label("⏰ " + timeText + " – " + endTimeText);
        timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");

        Label statusLabel = new Label("📌 " + a.getStatus());
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

        if (a.getConsultationExpiryDate() != null) {
            Label expiryLabel = new Label("⏳ Valid until: " +
                    a.getConsultationExpiryDate().format(DateTimeFormatter.ofPattern("dd/MM")));
            expiryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
            box.getChildren().add(expiryLabel);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // ✅ زر الإلغاء (يبقى ظاهرًا دائمًا — الدكتور حر يلغي أي وقت)
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-padding: 3 10; -fx-background-radius: 4;");
        cancelBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Cancel Appointment");
            confirm.setHeaderText("Cancel by Doctor?");
            confirm.setContentText("Are you sure you want to cancel this appointment?");

            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK) {
                    a.cancelByDoctor();
                    try {
                        new AppointmentDAO().updateStatus(a.getId(), a.getStatus());
                        appointmentsList.getChildren().remove(box);
                        showAlert("Success", "Appointment cancelled by doctor.");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showAlert("Error", "Failed to cancel appointment.");
                    }
                }
            });
        });

        LocalDateTime now = LocalDateTime.now();
        boolean isOverdueAndBooked = a.getStatus() == Status.Booked && now.isAfter(endTime);

        if (isOverdueAndBooked) {
            Button completeBtn = new Button("✓ Completed");
            completeBtn.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; " +
                    "-fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 4;");
            completeBtn.setOnAction(e -> {
                a.setStatus(Status.Completed);
                try {
                    new AppointmentDAO().updateStatus(a.getId(), a.getStatus());
                    // تحديث العرض: غير اللون، وأعد رسم الحالة
                    Platform.runLater(() -> {
                        box.setStyle(box.getStyle().replace(bgColor, "#f6fef9").replace(borderColor, "#d5f5e3"));
                        statusLabel.setText("📌 " + a.getStatus());
                        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2E7D32;");
                        // إخفاء الزرارين بعد التحديث
                        box.getChildren().remove(completeBtn);
                        if (box.getChildren().contains(spacer)) {
                            int idx = box.getChildren().indexOf(spacer);
                            if (idx >= 0 && idx + 1 < box.getChildren().size() &&
                                    box.getChildren().get(idx + 1) instanceof Button &&
                                    ((Button) box.getChildren().get(idx + 1)).getText().contains("Absent")) {
                                box.getChildren().remove(idx + 1); // absentBtn
                            }
                        }
                    });
                    showAlert("Success", "Appointment marked as completed.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Error", "Failed to complete appointment.");
                }
            });

            // زر "Mark as Absent"
            Button absentBtn = new Button("✖️ Absent");
            absentBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                    "-fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 4;");
            absentBtn.setOnAction(e -> {
                a.setStatus(Status.Absent);
                try {
                    new AppointmentDAO().updateStatus(a.getId(), a.getStatus());
                    Platform.runLater(() -> {
                        box.setStyle(box.getStyle().replace(bgColor, "#fef9e7").replace(borderColor, "#f9e79f"));
                        statusLabel.setText("📌 " + a.getStatus());
                        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #FF8F00;");
                        // إخفاء الزرارين
                        box.getChildren().remove(completeBtn);
                        box.getChildren().remove(absentBtn);
                    });
                    showAlert("Success", "Patient marked as absent (No-Show).");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Error", "Failed to mark as absent.");
                }
            });


            int spacerIndex = box.getChildren().indexOf(spacer);
            if (spacerIndex >= 0) {
                box.getChildren().add(spacerIndex, absentBtn);
                box.getChildren().add(spacerIndex, completeBtn);
            } else {
                box.getChildren().addAll(completeBtn, absentBtn);
            }
        }

        box.getChildren().addAll(spacer, cancelBtn);
        return box;
    }
    @FXML
    private void filterAppointmentsByDate() {
        if (allAppointments == null) return;
        LocalDate selectedDate = appointmentsCalendar.getValue();
        displayAppointments(allAppointments, selectedDate);
    }

    private void showAlert(String title, String message) {
        new Alert(Alert.AlertType.INFORMATION, message).show();
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            welcomeLabel.setText("Welcome, Dr. " + name.trim());
        } else {
            welcomeLabel.setText("Welcome, Doctor");
        }
    }
    private void loadClinicView() {
        appointmentsBox.setVisible(false);
        clinicInfoBox.setVisible(true);
        try {
            VBox clinicView = new VBox(10);
            clinicView.setPadding(new Insets(10));

            Label label = new Label("Clinic Information");
            label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            Label nameLbl = new Label("Clinic: " + safeString(currentDoctor.getClinic().getName(), "—"));
            Label specLbl = new Label("Specialty: " + safeString(specialtyLabel.getText(), "—"));
            Label addrLbl = new Label("Address: " + safeString(addressLabel.getText(), "—"));

            Button editBtn = new Button("Edit Clinic");
            editBtn.setOnAction(e -> handleEdit());
            editBtn.setStyle("-fx-background-color: #15BF8F; -fx-text-fill: white;");

            clinicView.getChildren().addAll(label, nameLbl, specLbl, addrLbl, editBtn);
            mainContentPane.getChildren().setAll(clinicView);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    @FXML
    private void handleReviews() {
        clinicInfoBox.setVisible(false);
        appointmentsBox.setVisible(false);
        reviewsBox.setVisible(true);

        try {
            List<Rating> ratings = new RatingDAO().getRatingsByClinicId(currentDoctor.getClinic().getID());
            displayRatings(ratings);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load reviews.");
        }
    }
    private void displayRatings(List<Rating> ratings) {
        reviewsList.getChildren().clear();

        if (ratings == null || ratings.isEmpty()) {
            Label noReviews = new Label("No reviews yet. Be the first to leave a review!");
            noReviews.setStyle("-fx-text-fill: #777; -fx-font-style: italic;");
            reviewsList.getChildren().add(noReviews);
            return;
        }

        for (Rating r : ratings) {
            HBox card = new HBox(10);
            card.setStyle("-fx-padding: 12; -fx-background-color: #f7f9f9; -fx-border-radius: 8; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT;");

            Label patientLabel = new Label("👤 " + (r.getPatient() != null ? r.getPatient().getName() : "Anonymous"));
            patientLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            HBox stars = new HBox(2);
            int score = r.getScore();
            for (int i = 0; i < 5; i++) {
                Text star = new Text(i < score ? "★" : "☆");
                star.setFont(Font.font(14));
                star.setStyle(i < score ? "-fx-fill: #FFD700;" : "-fx-fill: #CCCCCC;");
                stars.getChildren().add(star);
            }

            // التعليق
            String comment = r.getComment();
            Label commentLabel;
            if (comment == null || comment.trim().isEmpty()) {
                commentLabel = new Label("⭐ Rated only");
                commentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaa;");
            } else {
                commentLabel = new Label("💬 " + comment);
                commentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
            }
            commentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

            // التاريخ
            Label dateLabel = new Label("📅 " + r.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

            card.getChildren().addAll(patientLabel, stars, commentLabel, dateLabel);
            reviewsList.getChildren().add(card);
        }
    }

    private void showRatingDialog(Clinic clinic) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Rate " + clinic.getName());
        dialog.setHeaderText("Share your experience with this clinic");

        // Set custom style for the dialog
        dialog.getDialogPane().setStyle(
                "-fx-background-color: #F8F9FA; " +
                        "-fx-border-color: #DDD; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);"
        );
    }
    public void ClinicInfoBox(ActionEvent actionEvent) {

        clinicInfoBox.setVisible(true);
        appointmentsBox.setVisible(false);
        reviewsBox.setVisible(false);
    }
    @FXML
    private void handleCancelAllForSelectedDate() {
        LocalDate selectedDate = appointmentsCalendar.getValue();
        if (selectedDate == null) {
            new Alert(Alert.AlertType.WARNING, "يرجى اختيار يوم من التقويم أولًا.").show();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText(
                "Are you sure to Cancel All today's Appointments?\n" +
                        selectedDate.format(DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", java.util.Locale.forLanguageTag("ar"))) +
                        "؟\nnotifications will send to the patients"
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    int count = practitionerService.cancelAllAppointmentsForDate(
                            currentDoctor, selectedDate, "اعتذار من العيادة"
                    );
                    new Alert(Alert.AlertType.INFORMATION, "✅ تم إلغاء " + count + " مواعيد.").show();
                    // إعادة تحميل المواعيد
                    allAppointments = new AppointmentDAO().getAppointmentsByClinicId(currentDoctor.getClinic().getID());
                    displayAppointments(allAppointments, selectedDate);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "❌ فشل الإلغاء: " + ex.getMessage()).show();
                }
            }
        });
    }
    private void confirmAndCancelAppointment(Appointment a) {
        String patientName = (a.getPatient() != null) ? a.getPatient().getName() : "مجهول";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("إلغاء موعد");
        confirm.setHeaderText("إلغاء موعد مع " + patientName);
        confirm.setContentText("هل تريد إلغاء هذا الموعد؟ سيتم إرسال إشعار للمريض.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    practitionerService.cancelAppointmentAsPractitioner(a.getId(), "اعتذار من العيادة");
                    new Alert(Alert.AlertType.INFORMATION, "✅ تم إلغاء الموعد بنجاح.").show();

                    // تحديث اللستة مباشرة
                    allAppointments = new AppointmentDAO().getAppointmentsByClinicId(currentDoctor.getClinic().getID());
                    LocalDate selectedDate = appointmentsCalendar.getValue();
                    displayAppointments(allAppointments, selectedDate);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "❌ " + ex.getMessage()).show();
                }
            }
        });
    }
    @FXML
    private void handleExportExcel() {
        try {
            if (allAppointments == null || allAppointments.isEmpty()) {
                showAlert("No Data", "No appointments to export.");
                return;
            }
            String fileName = "Appointments_Report_" + LocalDate.now() + ".xlsx";
            ExcelExporter.exportToExcel(allAppointments, fileName);
            openFileSafely(fileName);
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to export Excel: " + ex.getMessage()).show();
        }
    }
    @FXML
    private void handleExportPDF() {
        try {
            if (allAppointments == null || allAppointments.isEmpty()) {
                showAlert("No Data", "No appointments to export.");
                return;
            }
            String fileName = "Appointments_Report_" + LocalDate.now() + ".pdf";
            PDFExporter.exportAppointmentsToPDF(allAppointments, fileName);
            openFileSafely(fileName);
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to export PDF: " + ex.getMessage()).show();
        }
    }
    // ✅ دالة آمنة لفتح الملف بعد التأكد من وجوده
    private void openFileSafely(String fileName) {
        java.io.File file = new java.io.File(fileName);
        if (file.exists() && file.isFile()) {
            try {
                java.awt.Desktop.getDesktop().open(file);
                new Alert(Alert.AlertType.INFORMATION, "File opened successfully!").show();
            } catch (Exception e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.WARNING,
                        "File created, but couldn't open it automatically.\nPath: " + file.getAbsolutePath()
                ).show();
            }
        } else {
            new Alert(Alert.AlertType.ERROR,
                    "Export failed: file not found.\nExpected: " + fileName + "\n\nCheck if the exporter completed successfully."
            ).show();
        }
    }
    @FXML
    private void handleFullReport() {
        try {
            if (currentDoctor == null || currentDoctor.getClinic() == null) {
                showAlert("Error", "Doctor or clinic is not available.");
                return;
            }

            // ✅ جلب البيانات
            int clinicId = currentDoctor.getClinic().getID();
            List<Appointment> allAppointments = new AppointmentDAO().getAllAppointmentsForReport(clinicId);
            List<Rating> ratings = new RatingDAO().getRatingsByClinicId(clinicId);

            // ✅ إخفاء كل البوكسات
            clinicInfoBox.setVisible(false);
            appointmentsBox.setVisible(false);
            reviewsBox.setVisible(false);
            // reportBox سيكون visible أدناه

            // ✅ عرض التقرير في reportBox (لو موجود)، أو تهيئته أول مرة
            if (reportBox == null) {
                showAlert("Error", "Report box is not initialized. Check FXML.");
                return;
            }

            // ✅ ملء المحتوى
            reportContent.getChildren().clear();

            // 📊 عنوان
            Label header = new Label("📊 Doctor Full Report");
            header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #15BF8F;");
            reportContent.getChildren().add(header);

            // 📊 إحصائيات
            long total = allAppointments.size();
            long completed = allAppointments.stream().filter(a -> a.getStatus() == Status.Completed).count();
            long cancelledByPatient = allAppointments.stream().filter(a -> a.getStatus() == Status.Cancelled_by_Patient).count();
            long cancelledByDoctor = allAppointments.stream().filter(a -> a.getStatus() == Status.Cancelled_by_Doctor).count();
            double avgRating = ratings.stream().mapToInt(Rating::getScore).average().orElse(0.0);

            VBox statsBox = new VBox(5);
            statsBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-border-radius: 6;");
            statsBox.getChildren().addAll(
                    new Label("📈 Statistics"),
                    new Label("• Total Appointments: " + total),
                    new Label("• Completed: " + completed),
                    new Label("• Cancelled by Patient: " + cancelledByPatient),
                    new Label("• Cancelled by Doctor: " + cancelledByDoctor),
                    new Label("• Average Rating: " + String.format("%.1f", avgRating) + "/5")
            );
            reportContent.getChildren().add(statsBox);

            // 📋 جدول المواعيد (مختصر)
            Label apptLabel = new Label("📋 Appointments (" + total + " total)");
            apptLabel.setStyle("-fx-font-weight: bold;");
            reportContent.getChildren().add(apptLabel);

            VBox apptList = new VBox(5);
            for (Appointment a : allAppointments) {
                TimeSlot slot = a.getAppointmentDateTime();
                if (slot == null) continue;

                HBox card = new HBox(10);
                card.setPadding(new Insets(5));
                card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 4;");

                // لون الخلفية حسب الحالة
                String color = switch (a.getStatus()) {
                    case Completed -> "#e8f5e9";
                    case Cancelled_by_Patient, Cancelled_by_Doctor -> "#ffebee";
                    default -> "#ffffff";
                };
                card.setStyle("-fx-background-color: " + color + "; -fx-padding: 5; -fx-border-radius: 4;");

                Label patient = new Label("👤 " + (a.getPatient() != null ? a.getPatient().getName() : "—"));
                Label date = new Label("📅 " + slot.getDate());
                Label time = new Label("⏰ " + slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
                Label status = new Label("📌 " + a.getStatus());

                card.getChildren().addAll(patient, date, time, status);
                apptList.getChildren().add(card);
            }
            ScrollPane apptScroll = new ScrollPane(apptList);
            apptScroll.setFitToWidth(true);
            apptScroll.setPrefHeight(200);
            reportContent.getChildren().add(apptScroll);

            // ⭐ التقييمات
            if (!ratings.isEmpty()) {
                Label ratingLabel = new Label("⭐ Patient Ratings (" + ratings.size() + ")");
                ratingLabel.setStyle("-fx-font-weight: bold;");
                reportContent.getChildren().add(ratingLabel);

                VBox ratingList = new VBox(5);
                for (Rating r : ratings) {
                    HBox rCard = new HBox(10);
                    rCard.setPadding(new Insets(5));
                    rCard.setStyle("-fx-background-color: #f1f8e9; -fx-border-radius: 4;");

                    String stars = "★".repeat(r.getScore()) + "☆".repeat(5 - r.getScore());
                    Label rInfo = new Label(
                            "👤 " + (r.getPatient() != null ? r.getPatient().getName() : "Anonymous") +
                                    " — " + stars +
                                    (r.getComment() != null ? "\n💬 " + r.getComment() : "")
                    );
                    rInfo.setWrapText(true);
                    rCard.getChildren().add(rInfo);
                    ratingList.getChildren().add(rCard);
                }
                reportContent.getChildren().add(new ScrollPane(ratingList));
            }

            // 🖨️ أزرار التصدير في الأسفل
            HBox exportButtons = new HBox(10);
            exportButtons.setAlignment(Pos.BOTTOM_RIGHT);

            Button pdfBtn = new Button("Export PDF 📄");
            pdfBtn.setStyle("-fx-background-color: #15BF8F; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 6;");
            pdfBtn.setOnAction(e -> exportFullReport("PDF", allAppointments, ratings));

            Button excelBtn = new Button("Export Excel 📊");
            excelBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 6;");
            excelBtn.setOnAction(e -> exportFullReport("Excel", allAppointments, ratings));

            Button backBtn = new Button("← Back to Dashboard");
            backBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 6;");
            backBtn.setOnAction(e -> {
                reportBox.setVisible(false);
                clinicInfoBox.setVisible(true); // أو أي بوx تريدينه أول ما يرجع
            });

            exportButtons.getChildren().addAll(pdfBtn, excelBtn, backBtn);
            reportContent.getChildren().add(exportButtons);

            // ✅ إظهار التقرير
            reportBox.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load report: " + e.getMessage());
        }
    }
    private void exportFullReport(String type, List<Appointment> appointments, List<Rating> ratings) {
        try {
            // ✅ نستخدم نفس الدوال اللي عندك أصلاً (exportToExcel + exportAppointmentsToPDF)
            String fileName = "Doctor_Full_Report_" + LocalDate.now() + "." + (type.equals("PDF") ? "pdf" : "xlsx");

            if ("PDF".equals(type)) {
                PDFExporter.exportAppointmentsToPDF(appointments, fileName);
            } else {
                ExcelExporter.exportToExcel(appointments, fileName);
            }

            openFileSafely(fileName);

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Export Error: " + e.getMessage()).show();
        }
    }
    @FXML
    private void handleSettings() {
        // ✅ إخفاء كل الـ boxes
        clinicInfoBox.setVisible(false);
        appointmentsBox.setVisible(false);
        reviewsBox.setVisible(false);
        reportBox.setVisible(false);

        loadCurrentUserSettings();

        settingsBox.setVisible(true);
    }

    private void loadCurrentUserSettings() {

        if (currentDoctor != null) {
            String name = currentDoctor.getName() != null ? currentDoctor.getName() : "";

            usernameField.setText(name);


            emailField.setText(currentDoctor.getEmail() != null ? currentDoctor.getEmail() : "");
            phoneField.setText(currentDoctor.getPhone() != null ? currentDoctor.getPhone() : "");
            genderField.setText(currentDoctor.getGender() != null ? currentDoctor.getGender() : "");
            dobField.setText(currentDoctor.getDateOfBirth() != null ?
                    currentDoctor.getDateOfBirth().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—");
            usernameField.setEditable(true);

            emailField.setEditable(false);
            phoneField.setEditable(false);
            genderField.setEditable(false);
            dobField.setEditable(false);
            emailField.setOpacity(0.7);
            phoneField.setOpacity(0.7);
            genderField.setOpacity(0.7);
            dobField.setOpacity(0.7);
            System.out.println(">>> Doctor Gender: [" + currentDoctor.getGender() + "]");
        }
    }

            @FXML
    private void handleSettingsCancel() {
        settingsBox.setVisible(false);
        clinicInfoBox.setVisible(true);
    }

    @FXML
    private void handleSettingsSave() {
        try {
            String newName = nameField.getText().trim();
            String currentPass = currentPasswordField.getText();
            String newPass = newPasswordField.getText();
            String confirmPass = confirmPasswordField.getText();

            if (newName.isEmpty()) {
                showAlert("Error", "Name/Username cannot be empty.");
                return;
            }
            PractitionerDAO doctorDAO = new PractitionerDAO();
            if (!newName.equals(currentDoctor.getName())) {
                if (doctorDAO.isNameTaken(newName, currentDoctor.getID())) {
                    showAlert("Error", "This name is already taken. Please choose another one.");
                    return;
                }
            }
            if (!newName.equals(currentDoctor.getName())) {
                currentDoctor.setName(newName);
            }if (!newPass.isEmpty()) {
                if (currentPass.isEmpty()) {
                    showAlert("Error", "Please enter your current password.");
                    return;
                }
                if (!newPass.equals(confirmPass)) {
                    showAlert("Error", "New passwords do not match.");
                    return;
                }
                if (!currentPass.equals(currentDoctor.getPassword())) {
                    showAlert("Error", "Current password is incorrect.");
                    return;
                }
                currentDoctor.setPassword(newPass);}

            doctorDAO.update(currentDoctor);

            if (welcomeLabel != null) {
                welcomeLabel.setText("Welcome, Dr. " + currentDoctor.getName());
            }

            showAlert("Success", "Profile updated successfully!");
            handleSettingsCancel();

        } catch (Exception e) {  e.printStackTrace();
            showAlert("Error", "Failed to update profile.");
        }
    }


    public static void sendEmail(String to, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("your_email@gmail.com", "your-app-password");
            }
        });

        try {
            // ✅ استخدم MimeMessage مباشرةً — مش Message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreply@yourclinic.com"));
            message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            System.out.println("✅ Email sent to: " + to);

        } catch (Exception e) {
            System.err.println("❌ Email failed to: " + to);
            e.printStackTrace();
        }
    }
    @FXML
    private void handleDeleteClinic() {
        if (currentDoctor == null || currentDoctor.getClinic() == null) {
            showAlert("Error", "No clinic to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("⚠️ Delete Clinic");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText(
                "This will:\n" +
                        "• Cancel ALL appointments (patients will be notified)\n" +
                        "• Delete clinic info, ratings, and schedule\n" +
                        "• Remove you from the clinic list\n\n" +
                        "This action cannot be undone."
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        int clinicId = currentDoctor.getClinic().getID();
                        String clinicName = currentDoctor.getClinic().getName();

                        // ✅ 1. إلغاء كل المواعيد مع إرسال إشعارات
                        cancelAllAppointmentsAndNotify(clinicId, clinicName);

                        // ✅ 2. حذف العيادة من قاعدة البيانات
                        deleteClinicFromDB(clinicId);

                        // ✅ 3. تحديث الواجهة
                        Platform.runLater(() -> {
                            currentDoctor.setClinic(null);
                            showNoClinic();
                            showAlert("Success", "Clinic '" + clinicName + "' deleted successfully.");
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() ->
                                showAlert("Error", "Failed to delete clinic: " + e.getMessage())
                        );
                    }
                }).start();
            }
        });
    }

    // ✅ دالة مساعدة: إلغاء كل المواعيد وإرسال إيميلات
    private void cancelAllAppointmentsAndNotify(int clinicId, String clinicName) throws SQLException {
        AppointmentDAO appointmentDAO = new AppointmentDAO();
        List<Appointment> appointments = appointmentDAO.getAppointmentsByClinicId(clinicId);

        for (Appointment a : appointments) {
            if (a.getStatus() == Status.Booked) {
                a.cancelByDoctor();
                appointmentDAO.updateStatus(a.getId(), a.getStatus());

                // ✅ إرسال إيميل للمريض
                Patient patient = a.getPatient();
                TimeSlot slot = a.getAppointmentDateTime();
                if (patient != null && slot != null) {
                    String subject = "Appointment Cancelled — " + clinicName;
                    String body = "Dear " + patient.getName() + ",\n\n" +
                            "Your appointment on " + slot.getDate() + " at " + slot.getStartTime() +
                            " has been cancelled because the clinic was permanently closed.\n\n" +
                            "Reason: The doctor has closed the clinic permanently.\n\n" +
                            "We apologize for the inconvenience.\n\n" +
                            "— Clinic Management";
                    sendEmail(patient.getEmail(), subject, body);
                }
            }
        }
    }

    // ✅ دالة مساعدة: حذف العيادة من قاعدة البيانات
    private void deleteClinicFromDB(int clinicId) throws SQLException {
        // 📌 ملاحظة: لو عندك ClinicService — استخدميها
        // وإلا، استخدم DAO مباشر:
        try (java.sql.Connection con = database.DBConnection.getConnection()) {
            con.setAutoCommit(false);

            // حذف cascade: appointments, ratings, time slots, schedule
            String[] tables = {
                    "Appointments", "Ratings", "TimeSlots", "WorkingHoursRules"
            };
            for (String table : tables) {
                String sql = "DELETE FROM " + table + " WHERE clinic_id = ?";
                try (java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, clinicId);
                    ps.executeUpdate();
                }
            }

            // حذف schedule لو منفصل
            String delSchedule = "DELETE FROM Schedules WHERE id IN (SELECT schedule_id FROM Clinics WHERE id = ?)";
            try (java.sql.PreparedStatement ps = con.prepareStatement(delSchedule)) {
                ps.setInt(1, clinicId);
                ps.executeUpdate();
            }

            // حذف العيادة نفسها
            String delClinic = "DELETE FROM Clinics WHERE id = ?";
            try (java.sql.PreparedStatement ps = con.prepareStatement(delClinic)) {
                ps.setInt(1, clinicId);
                ps.executeUpdate();
            }

            con.commit();
        }
    }



}