
package controller;

import dao.*;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;

import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;
import model.*;
import service.*;
import util.ExcelExporter;
import util.PDFExporter;

import java.awt.event.MouseEvent;
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
    @FXML
    private Label clinicNameLabel;
    @FXML
    private Label specialtyLabel;
    @FXML
    private Label addressLabel;
    @FXML
    private Label slotDurationLabel;
    @FXML
    private Text scheduleText;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Button editButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Label debugLabel;
    @FXML
    private VBox clinicInfoBox;
    @FXML
    private VBox appointmentsBox;
    @FXML
    private VBox appointmentsList;
    @FXML
    private HBox starsContainer;
    @FXML
    private DatePicker appointmentsCalendar;
    @FXML
    private AnchorPane mainContentPane;
    @FXML
    private VBox reviewsBox;
    @FXML
    private VBox reviewsList;
    @FXML
    private Button appointmentsNavButton;
    @FXML
    private Button reviewsNavButton;
    @FXML
    private Button chatNavButton;
    @FXML
    private Label priceLabel;
    @FXML
    private Button ClinicInfoMainBox;
    @FXML
    private Button cancelAllButton;
    @FXML
    private Button exportPDFButton;
    @FXML
    private Button exportExcelButton;
    @FXML
    private Button reportButton;
    @FXML
    private VBox reportBox;
    @FXML
    private VBox reportContent;
    @FXML
    private Button settingsButton;
    @FXML
    private VBox settingsBox;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField genderField;
    @FXML
    private TextField dobField;
    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button deleteClinicButton;
    @FXML
    private VBox waitingListContainer;


    private Button activeButton = null;
    @FXML
    private VBox waitingListBox;
    private Stage stage;
    private final NotificationService notificationService = new NotificationService();
    private Practitioner currentDoctor;
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final WaitingListService waitingListService = new WaitingListService();
    private final PractitionerDAO practitionerDAO = new PractitionerDAO();

    private final AppointmentService appointmentService = new AppointmentService();
    private final PatientService patientService = new PatientService();

    //practitionerDAO
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
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.minutes(5), event -> {
                    if (currentDoctor != null && currentDoctor.getClinic() != null) {
                        try {
                            int clinicId = currentDoctor.getClinic().getID();
                            waitingListService.expireOldRequests(clinicId);
                            sendEmailToNextPatient(clinicId);
                            System.out.println("✅ Auto-expired old requests at " + LocalDateTime.now());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
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
                int score = r.getScore();
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
                t.setText(FULL_STAR);
                t.setStyle("-fx-fill: " + GOLD);
            } else if (i == full && half) {
                t.setText(FULL_STAR);
                t.setStyle("-fx-fill: " + GOLD + "; -fx-opacity: 0.5;");
            } else {
                t.setText(EMPTY_STAR);
                t.setStyle("-fx-fill: " + LIGHT_GRAY);
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
        clinicNameLabel.setText("—");
        specialtyLabel.setText("—");
        renderRatingStars(0);
        addressLabel.setText("—");
        slotDurationLabel.setText("—");
        scheduleText.setText("You don't have a clinic yet.");
    }


    @FXML
    private void onWaitingListClicked() {
        clinicInfoBox.setVisible(false);
        appointmentsBox.setVisible(false);
        reviewsBox.setVisible(false);
        reportBox.setVisible(false);
        settingsBox.setVisible(false);

        waitingListBox.setVisible(true);

        loadDoctorWaitingList();
    }

    private void loadDoctorWaitingList() {
        waitingListContainer.getChildren().clear();

        if (currentDoctor == null || currentDoctor.getClinic() == null) {
            waitingListContainer.getChildren().add(
                    new Label("⚠️ No clinic is linked to this account.")
            );
            return;
        }

        try {
            int clinicId = currentDoctor.getClinic().getID();
            List<WaitingList> requests = waitingListService.getWaitingListByClinicId(clinicId);

            if (requests == null || requests.isEmpty()) {
                Label empty = new Label("📭 There are no waiting lists at your clinic at the moment");
                empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d; -fx-alignment: center;");
                waitingListContainer.getChildren().add(empty);
                return;
            }

            for (WaitingList req : requests) {
                waitingListContainer.getChildren().add(createWaitingCard(req));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Label err = new Label("❌ Queue loading error" + e.getMessage());
            err.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            waitingListContainer.getChildren().add(err);
        }
    }

    private VBox createWaitingCard(WaitingList req) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #fdf9f0; -fx-border-color: #f1c40f; -fx-border-width: 1; -fx-border-radius: 8;");

        try {
            String patientName = "—";
            int patientId = req.getPatient().getID();
            if (patientId > 0) {
                try {
                    Patient p = req.getPatient();
                    patientName = (p != null) ? p.getName() : "Patient #" + patientId;
                } catch (Exception e) {
                    patientName = "Error loading patient";
                }
            }

            String timeStr = "—";
            if (req.getRequestTime() != null) {
                try {
                    timeStr = req.getRequestTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                } catch (Exception ignored) {
                }
            }

            WaitingStatus statusEnum = req.getStatus();
            String statusText = (statusEnum != null) ? statusEnum.name() : "UNKNOWN";

            String statusArabic = switch (statusEnum) {
                case PENDING -> "PENDING";
                case OFFERED -> "OFFERED";
                case CONFIRMED -> "CONFIRMED";
                case EXPIRED -> "EXPIRED";
                case CANCELLED -> "CANCELLED";
                default -> "Unkown";
            };

            String color = switch (statusEnum) {
                case PENDING -> "#E67E22";
                case OFFERED -> "#3498DB";
                case CONFIRMED -> "#27AE60";
                case EXPIRED, CANCELLED -> "#E74C3C";
                default -> "#95A5A6";
            };

            Label pLabel = new Label("👤 " + patientName);
            pLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Label timeLabel = new Label("🕒 " + timeStr);
            Label statusLabel = new Label("📊 " + statusArabic);
            statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + ";");

            HBox buttons = new HBox(8);
            buttons.setAlignment(Pos.CENTER_RIGHT);

            if (statusEnum == WaitingStatus.PENDING) {
                Button approveBtn = new Button("Aproved ✓ ");
                Button rejectBtn = new Button("Reject ✗ ");

                approveBtn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white;");
                rejectBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;");

                approveBtn.setOnAction(e -> offerAppointment(req));
                rejectBtn.setOnAction(e -> cancelRequest(req));

                buttons.getChildren().addAll(approveBtn, rejectBtn);
            }

            card.getChildren().addAll(pLabel, timeLabel, statusLabel, new Separator(), buttons);

        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("❗ Order display error");
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
            card.getChildren().add(errorLabel);
        }

        return card;
    }

    private void offerAppointment(WaitingList req) {
        try {
            waitingListService.updateStatus(req.getId(), WaitingStatus.OFFERED);

            Patient patient = patient = req.getPatient();
            if (patient == null || patient.getEmail() == null || patient.getEmail().trim().isEmpty()) {
                loadDoctorWaitingList();
                new Alert(Alert.AlertType.WARNING,
                        "Patient #" + req.getPatient().getID() + " has no email — no notification sent.")
                        .showAndWait();
                return;
            }

            String subject = "🎉 Appointment Offer – Action Required!";
            String body = String.format("""
                            <!DOCTYPE html>
                            <html>
                            <head>
                              <meta charset="UTF-8">
                              <meta name="viewport" content="width=device-width, initial-scale=1.0">
                              <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
                              <style>
                                body {
                                  margin: 0;
                                  padding: 0;
                                  background-color: #f8fafc;
                                  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
                                  color: #1e293b;
                                  line-height: 1.6;
                                }
                                .email-container {
                                  max-width: 650px;
                                  margin: 40px auto;
                                  background: #ffffff;
                                  border-radius: 16px;
                                  overflow: hidden;
                                  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
                                  border: 1px solid #e2e8f0;
                                }
                                .header {
                                  background: #f0fdf4;
                                  padding: 32px 30px 24px;
                                  text-align: center;
                                  border-bottom: 1px solid #f1f5f9;
                                }
                                .header h1 {
                                  margin: 0;
                                  font-size: 22px;
                                  font-weight: 600;
                                  color: #16a34a;
                                  display: flex;
                                  align-items: center;
                                  justify-content: center;
                                  gap: 10px;
                                }
                                .content {
                                  padding: 32px;
                                }
                                .content p {
                                  margin: 0 0 18px;
                                  font-size: 15px;
                                  color: #334155;
                                }
                                .clinic-name {
                                  font-weight: 600;
                                  color: #0f172a;
                                  background: #f8fafc;
                                  padding: 14px;
                                  border-radius: 10px;
                                  margin: 20px 0;
                                  text-align: center;
                                  border: 1px solid #e2e8f0;
                                }
                                .action-box {
                                  background: #ecfdf5;
                                  border-left: 4px solid #34d399;
                                  padding: 20px;
                                  border-radius: 0 10px 10px 0;
                                  margin: 26px 0;
                                  text-align: center;
                                }
                                .action-box p {
                                  margin: 0;
                                  font-weight: 600;
                                  color: #065f46;
                                  font-size: 16px;
                                }
                                .footer {
                                  padding: 24px 32px;
                                  background: #f8fafc;
                                  border-top: 1px solid #e2e8f0;
                                  text-align: center;
                                  font-size: 13px;
                                  color: #64748b;
                                }
                                .highlight {
                                  color: #16a34a;
                                  font-weight: 600;
                                }
                                .timer {
                                  display: inline-block;
                                  background: #dcfce7;
                                  color: #166534;
                                  padding: 4px 10px;
                                  border-radius: 6px;
                                  font-weight: 600;
                                  margin-top: 8px;
                                }
                              </style>
                            </head>
                            <body>
                              <div class="email-container">
                                <div class="header">
                                  <h1>🎉 Appointment Offer – Action Required!</h1>
                                </div>
                            
                                <div class="content">
                                  <p>Hi <strong>%s</strong>,</p>
                            
                                  <p>An appointment slot is now available for you at:</p>
                            
                                  <div class="clinic-name">%s</div>
                            
                                  <div class="action-box">
                                    <p>⏰ Please log in to your account to confirm your appointment.</p>
                                    <div class="timer">You have only 10 minutes to confirm!</div>
                                  </div>
                            
                                  <p>If you don’t confirm in time, this slot will be offered to the next patient on the waiting list.</p>
                                </div>
                            
                                <div class="footer">
                                  <p>Thank you,<br>
                                     <strong>Clinic Management Team</strong></p>
                                </div>
                              </div>
                            </body>
                            </html>
                            """, patient.getName(), (req.getClinic() != null ? req.getClinic().getName() : "Unknown Clinic"))
                    .stripIndent();

            notificationService.sendEmail(patient.getEmail(), subject, body);

            loadDoctorWaitingList();

            new Alert(Alert.AlertType.INFORMATION,
                    "✓ Appointment offered and email sent to patient.")
                    .showAndWait();

        } catch (SQLException | MessagingException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "❌ Failed to offer appointment: " + e.getMessage())
                    .showAndWait();
        }
    }

    private void cancelRequest(WaitingList req) {
        try {
            waitingListService.updateStatus(req.getId(), WaitingStatus.CANCELLED);

            Patient patient = req.getPatient();
            if (patient == null || patient.getEmail() == null || patient.getEmail().trim().isEmpty()) {
                loadDoctorWaitingList();
                new Alert(Alert.AlertType.WARNING,
                        "Patient #" + req.getPatient().getID() + " has no email — no notification sent.")
                        .showAndWait();
                return;
            }

            String subject = "❌ Your Waiting List Request Has Been Cancelled";
            String body = String.format("""
                            <!DOCTYPE html>
                            <html>
                            <head>
                              <meta charset="UTF-8">
                              <meta name="viewport" content="width=device-width, initial-scale=1.0">
                              <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
                              <style>
                                body {
                                  margin: 0;
                                  padding: 0;
                                  background-color: #f8fafc;
                                  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
                                  color: #1e293b;
                                  line-height: 1.6;
                                }
                                .email-container {
                                  max-width: 650px;
                                  margin: 40px auto;
                                  background: #ffffff;
                                  border-radius: 16px;
                                  overflow: hidden;
                                  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
                                  border: 1px solid #e2e8f0;
                                }
                                .header {
                                  background: #fef2f2;
                                  padding: 32px 30px 24px;
                                  text-align: center;
                                  border-bottom: 1px solid #f1f5f9;
                                }
                                .header h1 {
                                  margin: 0;
                                  font-size: 22px;
                                  font-weight: 600;
                                  color: #dc2626;
                                  display: flex;
                                  align-items: center;
                                  justify-content: center;
                                  gap: 10px;
                                }
                                .content {
                                  padding: 32px;
                                }
                                .content p {
                                  margin: 0 0 18px;
                                  font-size: 15px;
                                  color: #334155;
                                }
                                .clinic-name {
                                  font-weight: 600;
                                  color: #0f172a;
                                  background: #f8fafc;
                                  padding: 14px;
                                  border-radius: 10px;
                                  margin: 20px 0;
                                  text-align: center;
                                  border: 1px solid #e2e8f0;
                                }
                                .reasons-box {
                                  background: #fff5f5;
                                  border-left: 4px solid #f87171;
                                  padding: 18px;
                                  border-radius: 0 10px 10px 0;
                                  margin: 26px 0;
                                }
                                .reasons-box h3 {
                                  margin: 0 0 12px;
                                  font-size: 15px;
                                  color: #dc2626;
                                  font-weight: 600;
                                }
                                .reasons-box ul {
                                  margin: 0;
                                  padding-left: 22px;
                                  font-size: 14px;
                                  color: #475569;
                                }
                                .footer {
                                  padding: 24px 32px;
                                  background: #f8fafc;
                                  border-top: 1px solid #e2e8f0;
                                  text-align: center;
                                  font-size: 13px;
                                  color: #64748b;
                                }
                                .highlight {
                                  color: #dc2626;
                                  font-weight: 600;
                                }
                              </style>
                            </head>
                            <body>
                              <div class="email-container">
                                <div class="header">
                                  <h1>❌ Your Waiting List Request Was Cancelled</h1>
                                </div>
                            
                                <div class="content">
                                  <p>Hi <strong>%s</strong>,</p>
                            
                                  <p>We’re sorry to inform you that your waiting list request for:</p>
                            
                                  <div class="clinic-name">%s</div>
                            
                                  <p>has been cancelled.</p>
                            
                                  <div class="reasons-box">
                                    <h3>Possible reasons:</h3>
                                    <ul>
                                      <li>The doctor declined the request.</li>
                                      <li>You didn’t confirm the offered appointment within 10 minutes.</li>
                                    </ul>
                                  </div>
                            
                                  <p>No worries — you can submit a new request anytime from your patient dashboard.</p>
                                </div>
                            
                                <div class="footer">
                                  <p>Thank you for your understanding.<br>
                                     <strong>Clinic Management Team</strong></p>
                                </div>
                              </div>
                            </body>
                            </html>
                            """, patient.getName(), (req.getClinic() != null ? req.getClinic().getName() : "Unknown Clinic"))
                    .stripIndent();

            notificationService.sendEmail(patient.getEmail(), subject, body);

            loadDoctorWaitingList();

            new Alert(Alert.AlertType.INFORMATION,
                    "✓ Request cancelled and email sent to patient.")
                    .showAndWait();

        } catch (SQLException | MessagingException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "❌ Failed to cancel request: " + e.getMessage())
                    .showAndWait();
        }

    }


    @FXML
    private void checkExpiredRequests() {
        if (currentDoctor == null || currentDoctor.getClinic() == null) return;

        try {
            int clinicId = currentDoctor.getClinic().getID();
            waitingListService.expireOldRequests(clinicId);
            sendEmailToNextPatient(clinicId);
            loadDoctorWaitingList(); // refresh

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Updated Done !");
            success.setHeaderText(null);
            success.setContentText("✓ done.");
            success.showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Faild to ubdate " + e.getMessage());
        }
    }

    private void showErrorAlert(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
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

                Stage editStage = new Stage();
                editStage.initOwner(this.stage);
                editStage.setTitle("Edit Clinic");
                editStage.setScene(new Scene(root));
                editStage.show();

            } catch (IOException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Cannot open edit window").showAndWait();
            }
        }
    }

    private void sendEmailToNextPatient(int clinicId) {
        try {
            List<WaitingList> pendingRequests = waitingListService.getWaitingListByClinicId(clinicId);

            if (pendingRequests != null && !pendingRequests.isEmpty()) {

                WaitingList nextRequest = pendingRequests.get(0);
                Patient patient = nextRequest.getPatient();

                if (patient != null && patient.getEmail() != null) {

                    String subject = "🎉 Appointment Opportunity Available!";

                    String body = """
                            <html>
                            <body style="font-family: Arial; line-height: 1.6;">
                            
                                <h2 style="color:#2c7be5;">Hello {name},</h2>
                            
                                <p>
                                    Due to a <strong>cancellation</strong> or an expired request, 
                                    you now have a new opportunity to book an appointment in:
                                </p>
                            
                                <h3 style="color:#444;">Clinic: {clinicName}</h3>
                            
                                <p>Please log into your account and check your waiting list.</p>
                            
                                <br>
                                <p style="color:#777;">Thank you!</p>
                            
                            </body>
                            </html>
                            """;

                    body = body.replace("{name}", patient.getName())
                            .replace("{clinicName}", nextRequest.getClinic().getName());

                    notificationService.sendEmail(patient.getEmail(), subject, body);


                }
            }

        } catch (SQLException | MessagingException e) {
            e.printStackTrace();
            showErrorAlert("Failed to reject request: " + e.getMessage());
        }
    }

    private void confirmAndReject(WaitingList req) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reject Request");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Do you want to reject the request of patient: " + req.getPatient().getName() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    waitingListService.updateStatus(req.getId(), WaitingStatus.CANCELLED);
                    Patient patient = req.getPatient();

                    if (patient != null && patient.getEmail() != null) {

                        String subject = "❌ Your Appointment Request Was Rejected";

                        String body = """
                                <html>
                                <body style="font-family: Arial; line-height: 1.6;">
                                
                                    <h2 style="color:#d9534f;">Hello {name},</h2>
                                
                                    <p>
                                        We regret to inform you that your request for an appointment in:
                                    </p>
                                
                                    <h3 style="color:#444;">Clinic: {clinicName}</h3>
                                
                                    <p>has been <strong>rejected</strong> by the doctor.</p>
                                
                                    <p>You may submit a new request at any time.</p>
                                
                                    <br>
                                    <p style="color:#777;">Thank you!</p>
                                
                                </body>
                                </html>
                                """;

                        body = body.replace("{name}", patient.getName())
                                .replace("{clinicName}", req.getClinic().getName());

                        notificationService.sendEmail(patient.getEmail(), subject, body);
                    }

                    loadDoctorWaitingList();

                    sendEmailToNextPatient(currentDoctor.getClinic().getID());

                } catch (SQLException | MessagingException e) {
                    e.printStackTrace();
                    showErrorAlert("Failed to reject request: " + e.getMessage());
                }
            }
        });
    }


    @FXML
    private void handleLogout() {
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

    private void populateCalendarWithAppointments(List<Appointment> apps) {
        Set<LocalDate> datesWithAppointments = apps.stream()
                .map(Appointment::getAppointmentDateTime)
                .filter(Objects::nonNull)
                .map(TimeSlot::getDate)
                .filter(d -> !d.isBefore(LocalDate.now()))
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
                    setStyle("-fx-background-color: #d0f0d0; -fx-text-fill: #2e7d32;");
                    setDisable(false);
                } else {
                    setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #aaa;");
                    setDisable(true);
                }
            }
        });

        if (!datesWithAppointments.isEmpty()) {
            LocalDate first = datesWithAppointments.stream().min(LocalDate::compareTo).orElse(null);
            appointmentsCalendar.setValue(first);
            displayAppointments(allAppointments, first);
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
            case Absent:
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

        int durationMinutes = a.getClinic() != null && a.getClinic().getSchedule() != null
                ? a.getClinic().getSchedule().getSlotDurationInMinutes()
                : 30;

        LocalDateTime endTime = slot.getDate()
                .atTime(slot.getStartTime())
                .plusMinutes(durationMinutes);

        String endTimeText = endTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
        Label timeLabel = new Label("⏰ " + timeText + " – " + endTimeText);
        timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");

        Label statusLabel = new Label("📌 " + a.getStatus());
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

        Label expiryLabel = null;

        if (a.getConsultationExpiryDate() != null) {
            expiryLabel = new Label("⏳ Valid until: " +
                    a.getConsultationExpiryDate().format(DateTimeFormatter.ofPattern("dd/MM")));
            expiryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
            box.getChildren().add(expiryLabel);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-padding: 3 10; -fx-background-radius: 4;");
        cancelBtn.setOnAction(e -> confirmAndCancelAppointment(a));

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
                    Platform.runLater(() -> {
                        box.setStyle(box.getStyle().replace(bgColor, "#f6fef9").replace(borderColor, "#d5f5e3"));
                        statusLabel.setText("📌 " + a.getStatus());
                        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2E7D32;");
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
        box.getChildren().addAll(
                patientLabel,
                new Region(),
                timeLabel,
                new Region(),
                statusLabel
        );

        if (a.getConsultationExpiryDate() != null) {
            box.getChildren().add(expiryLabel);
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
            new Alert(Alert.AlertType.WARNING, "select day from calender ! ").show();
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
                    allAppointments = new AppointmentDAO().getAppointmentsByClinicId(currentDoctor.getClinic().getID());
                    displayAppointments(allAppointments, selectedDate);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "❌ فشل الإلغاء: " + ex.getMessage()).show();
                }
            }
        });
    }

    public void refreshClinicInfo() {
        try {
            // إعادة تحميل الدكتور (أو الكلاينك فقط)
            Practitioner updatedDoctor = practitionerDAO.getById(currentDoctor.getID());
            if (updatedDoctor != null && updatedDoctor.getClinic() != null) {
                currentDoctor = updatedDoctor; // أو حدّث فقط الـ clinic
                // حدّث العناصر في الواجهة:
                clinicNameLabel.setText(currentDoctor.getClinic().getName());
                priceLabel.setText(String.format("%.2f EGP", currentDoctor.getClinic().getPrice()));
                // ... وغيرهم حسب الحاجة
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void confirmAndCancelAppointment(Appointment a) {
        String patientName = (a.getPatient() != null) ? a.getPatient().getName() : "Anonymous";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Appointment");
        confirm.setHeaderText("Cancel with " + patientName + "?");
        confirm.setContentText("A notification will be sent to the patient.\n" +
                "If a waiting list exists, the next patient will be notified.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    practitionerService.cancelAppointmentAsPractitioner(a.getId(), "Cancellation by doctor");

                    waitingListService.onAppointmentCancelled(a);

                    sendCancellationEmailToPatient(a);

                    Platform.runLater(() -> {
                        new Alert(Alert.AlertType.INFORMATION, "✅ Appointment cancelled.").show();
                        try {
                            allAppointments = new AppointmentDAO().getAppointmentsByClinicId(currentDoctor.getClinic().getID());
                            LocalDate selectedDate = appointmentsCalendar.getValue();
                            displayAppointments(allAppointments, selectedDate);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() ->
                            new Alert(Alert.AlertType.ERROR, "❌ " + ex.getMessage()).show()
                    );
                }
            }
        });
    }

    private void sendCancellationEmailToPatient(Appointment a) {
        Patient p = a.getPatient();
        TimeSlot slot = a.getAppointmentDateTime();
        if (p == null || p.getEmail() == null || slot == null) return;

        String subject = "Your Appointment Has Been Cancelled";
        String body = String.format(
                "<h3>Dear %s,</h3>" +
                        "<p>Your appointment with <strong>Dr. %s</strong> on <strong>%s at %s</strong> has been cancelled.</p>" +
                        "<p>We apologize for the inconvenience.</p>",
                p.getName(), currentDoctor.getName(),
                slot.getDate(), slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"))
        );
        try {
            notificationService.sendEmail(p.getEmail(), subject, body);
        } catch (Exception e) {
            System.err.println("Failed to send cancellation email to: " + p.getEmail());
        }
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

            int clinicId = currentDoctor.getClinic().getID();
            List<Appointment> allAppointments = new AppointmentDAO().getAllAppointmentsForReport(clinicId);
            List<Rating> ratings = new RatingDAO().getRatingsByClinicId(clinicId);

            clinicInfoBox.setVisible(false);
            appointmentsBox.setVisible(false);
            reviewsBox.setVisible(false);

            if (reportBox == null) {
                showAlert("Error", "Report box is not initialized. Check FXML.");
                return;
            }

            reportContent.getChildren().clear();

            Label header = new Label("📊 Doctor Full Report");
            header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #15BF8F;");
            reportContent.getChildren().add(header);

            // statistics
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
                clinicInfoBox.setVisible(true);
            });

            exportButtons.getChildren().addAll(pdfBtn, excelBtn, backBtn);
            reportContent.getChildren().add(exportButtons);

            reportBox.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load report: " + e.getMessage());
        }
    }

    private void exportFullReport(String type, List<Appointment> appointments, List<Rating> ratings) {
        try {
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
            //System.out.println(">>> Doctor Gender: [" + currentDoctor.getGender() + "]");
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
            String newName = usernameField.getText().trim();
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
            }
            if (!newPass.isEmpty()) {
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
                currentDoctor.setPassword(newPass);
            }

            doctorDAO.update(currentDoctor);

            if (welcomeLabel != null) {
                welcomeLabel.setText("Welcome, Dr. " + currentDoctor.getName());
            }

            showAlert("Success", "Profile updated successfully!");
            handleSettingsCancel();

        } catch (Exception e) {
            e.printStackTrace();
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

                        cancelAllAppointmentsAndNotify(clinicId, clinicName);

                        deleteClinicFromDB(clinicId);

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

    private void cancelAllAppointmentsAndNotify(int clinicId, String clinicName) throws SQLException {
        AppointmentDAO appointmentDAO = new AppointmentDAO();
        List<Appointment> appointments = appointmentDAO.getAppointmentsByClinicId(clinicId);

        for (Appointment a : appointments) {
            if (a.getStatus() == Status.Booked) {
                a.cancelByDoctor();
                appointmentDAO.updateStatus(a.getId(), a.getStatus());

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

    private void deleteClinicFromDB(int clinicId) throws SQLException {

        try (java.sql.Connection con = database.DBConnection.getConnection()) {
            con.setAutoCommit(false);

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

            String delSchedule = "DELETE FROM Schedules WHERE id IN (SELECT schedule_id FROM Clinics WHERE id = ?)";
            try (java.sql.PreparedStatement ps = con.prepareStatement(delSchedule)) {
                ps.setInt(1, clinicId);
                ps.executeUpdate();
            }

            String delClinic = "DELETE FROM Clinics WHERE id = ?";
            try (java.sql.PreparedStatement ps = con.prepareStatement(delClinic)) {
                ps.setInt(1, clinicId);
                ps.executeUpdate();
            }

            con.commit();
        }
    }

    @FXML

    private void loadWaitingListForClinic() {
        waitingListBox.getChildren().clear();
        if (currentDoctor == null || currentDoctor.getClinic() == null) return;

        try {
            int clinicId = currentDoctor.getClinic().getID();
            WaitingListDAO wlDAO = new WaitingListDAO();
            List<WaitingList> entries = wlDAO.getAllByClinicId(clinicId);

            if (entries.isEmpty()) {
                waitingListBox.getChildren().add(new Label("✅ No patients in waiting list."));
            } else {
                entries.sort((a, b) -> b.getRequestTime().compareTo(a.getRequestTime()));
                for (WaitingList e : entries) {
                    waitingListBox.getChildren().add(createWaitingListCard(e));
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            waitingListBox.getChildren().add(new Label("❌ Error loading waiting list."));
        }
    }

    private HBox createWaitingListCard(WaitingList e) {
        HBox card = new HBox(10);
        card.setPadding(new Insets(8));
        card.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #e0e0e0; -fx-border-radius: 6;");

        Patient p = e.getPatient();
        String name = (p != null) ? p.getName() : "Unknown";
        String dateStr = e.getDate().format(DateTimeFormatter.ofPattern("dd/MM"));
        String timeStr = e.getRequestTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        String status = e.getStatus().name();

        Label nameLabel = new Label("👤 " + name);
        Label dateLabel = new Label("📅 " + dateStr);
        Label timeLabel = new Label("🕒 " + timeStr);
        Label statusLabel = new Label("📌 " + status);
        statusLabel.setStyle(switch (e.getStatus()) {
            case PENDING -> "-fx-text-fill: #FFA726;";
            case OFFERED -> "-fx-text-fill: #29B6F6;";
            case CONFIRMED -> "-fx-text-fill: #66BB6A;";
            case EXPIRED, CANCELLED -> "-fx-text-fill: #EF5350;";
            default -> "";
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (e.getStatus() == WaitingStatus.PENDING) {
            Button offerBtn = new Button("➡️ Offer Now");
            offerBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3 8;");
            offerBtn.setOnAction(ev -> offerSlotManually(e));
            card.getChildren().addAll(nameLabel, dateLabel, timeLabel, statusLabel, spacer, offerBtn);
        } else {
            card.getChildren().addAll(nameLabel, dateLabel, timeLabel, statusLabel, spacer);
        }

        return card;
    }

    private void offerSlotManually(WaitingList entry) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Manual Offer");
        confirm.setHeaderText("Offer slot to " + entry.getPatient().getName() + "?");
        confirm.setContentText("This will send an email and start the 10-minute timer.");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    LocalDate date = entry.getDate();
                    TimeSlot freeSlot = findFirstFreeSlotInDay(currentDoctor.getClinic(), date);
                    if (freeSlot == null) {
                        new Alert(Alert.AlertType.WARNING, "No free slots found on " + date).show();
                        return;
                    }

                    waitingListService.offerSlotTo(entry, freeSlot);
                    new Alert(Alert.AlertType.INFORMATION, "✅ Offer sent to " + entry.getPatient().getName()).show();

                    loadWaitingListForClinic();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "❌ Failed to offer slot: " + ex.getMessage()).show();
                }
            }
        });
    }

    private TimeSlot findFirstFreeSlotInDay(Clinic clinic, LocalDate date) throws SQLException {
        TimeSlotDAO dao = new TimeSlotDAO();
        List<TimeSlot> slots = dao.getSlotsByClinic(clinic.getID());
        AppointmentDAO appDAO = new AppointmentDAO();
        List<Integer> booked = appDAO.getBookedSlotIdsByClinicAndDate(clinic.getID(), date);

        return slots.stream()
                .filter(s -> s.getDate().isEqual(date))
                .filter(s -> !booked.contains(s.getId()))
                .findFirst()
                .orElse(null);
    }

    @FXML
    private void hoverNavButton(javafx.scene.input.MouseEvent e) {
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
    private void resetNavButton(javafx.scene.input.MouseEvent e) {
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
    private void pressNavButton(javafx.scene.input.MouseEvent e) {
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
    private void hoverLogoutButton(javafx.scene.input.MouseEvent e) {
        Button btn = (Button) e.getSource();
        btn.setStyle("-fx-background-color: #0FAF88; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 10 25;");
    }

    @FXML
    private void resetLogoutButton(javafx.scene.input.MouseEvent e) {
        Button btn = (Button) e.getSource();
        btn.setStyle("-fx-background-color: #0C7E5F; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 10 25;");
    }

    @FXML
    private void handleViewWaitingList() {
        clinicInfoBox.setVisible(false);
        appointmentsBox.setVisible(false);
        reviewsBox.setVisible(false);
        reportBox.setVisible(false);
        settingsBox.setVisible(false);

        if (waitingListBox == null) {
            System.err.println("❌ waitingListBox is null! Check FXML fx:id.");
            return;
        }

        waitingListBox.setVisible(true);
        waitingListBox.setManaged(true);

        loadWaitingListForClinic();
    }
}