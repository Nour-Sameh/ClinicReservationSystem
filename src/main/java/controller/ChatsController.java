package controller;

import dao.ChatDAO;
import dao.MessageDAO;
import dao.PatientDAO;
import dao.PractitionerDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ChatsController implements Initializable {

    @FXML private ListView<?> chatListView;
    @FXML private Label listTitleLabel;
    @FXML private Label chatPartnerLabel;
    @FXML private VBox messageArea;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private Button profileButton;
    @FXML private ScrollPane messagesScrollPane;

    private final ChatDAO chatDAO = new ChatDAO();
    private final MessageDAO messageDAO = new MessageDAO();
    private List<Message> messages = new ArrayList<>();

    private enum Mode { PATIENT, PRACTITIONER }
    private Mode mode = Mode.PATIENT;

    private Patient currentPatient;
    private Practitioner currentPractitioner;
    private Practitioner selectedPractitioner;
    private Chat selectedChat;

    // ==================== FXML INIT ====================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage());
        if (chatListView != null) {
            chatListView.getSelectionModel().selectedItemProperty()
                    .addListener((obs, old, cur) -> onChatSelect());
        }
    }

    // ==================== EXTERNAL ENTRY POINTS ====================
    public void setPatient(Patient patient) {
        this.currentPatient = patient;
        this.mode = Mode.PATIENT;
        Platform.runLater(() -> {
            listTitleLabel.setText("Doctors");
            chatPartnerLabel.setText("Select a doctor to chat");
        });
        loadPatientChatList();
    }

    public void setCurrentPractitioner(Practitioner practitioner) {
        this.currentPractitioner = practitioner;
        this.mode = Mode.PRACTITIONER;
        Platform.runLater(() -> {
            listTitleLabel.setText("Your Chats");
            chatPartnerLabel.setText("Select a patient to chat");
        });
        loadPractitionerChatList();
    }

    public void initializeChatWithSpecificPractitioner(Patient patient, Practitioner practitioner) {
        this.currentPatient = patient;
        this.selectedPractitioner = practitioner;
        this.mode = Mode.PATIENT;

        Platform.runLater(() -> {
            listTitleLabel.setText("Doctors");
            chatPartnerLabel.setText("Dr. " + practitioner.getName());
        });

        loadPatientChatList();

        Platform.runLater(() -> {
            Platform.runLater(() -> {
                try {
                    ListView<Practitioner> lv = (ListView<Practitioner>) chatListView;
                    Practitioner found = lv.getItems().stream()
                            .filter(p -> p != null && p.getID() == practitioner.getID())
                            .findFirst()
                            .orElse(null);

                    if (found != null) {
                        lv.getSelectionModel().select(found);
                        loadAndDisplayMessagesForPatient(found);
                    } else {
                        lv.getItems().add(practitioner);
                        lv.getSelectionModel().select(practitioner);
                        loadAndDisplayMessagesForPatient(practitioner);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Navigation Error", "Could not open chat with doctor.");
                }
            });
        });
    }

    // ==================== CHAT LIST LOADING ====================
    private void loadPatientChatList() {
        if (currentPatient == null) {
            System.err.println("currentPatient is NULL in loadPatientChatList!");
            return;
        }
        try {
            PractitionerDAO practitionerDAO = new PractitionerDAO();
            List<Practitioner> practitioners = practitionerDAO.getAll();

            Platform.runLater(() -> {
                @SuppressWarnings("unchecked")
                ListView<Practitioner> lv = (ListView<Practitioner>) chatListView;
                lv.getItems().clear();
                lv.getItems().addAll(practitioners);

                lv.setCellFactory(param -> new ListCell<Practitioner>() {
                    @Override
                    protected void updateItem(Practitioner p, boolean empty) {
                        super.updateItem(p, empty);
                        if (empty || p == null) {
                            setText("");
                        } else {
                            setText("Dr. " + p.getName());
                        }
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to load doctor list.");
        }
    }

    private void loadPractitionerChatList() {
        if (currentPractitioner == null) return;
        try {
            List<Chat> chats = chatDAO.getChatsByPractitionerId(currentPractitioner.getID());
            Platform.runLater(() -> {
                @SuppressWarnings("unchecked")
                ListView<Chat> lv = (ListView<Chat>) chatListView;
                lv.getItems().clear();
                lv.getItems().addAll(chats);

                lv.setCellFactory(param -> new ListCell<Chat>() {
                    @Override
                    protected void updateItem(Chat chat, boolean empty) {
                        super.updateItem(chat, empty);
                        if (empty || chat == null || chat.getPatient() == null) {
                            setText("");
                            setGraphic(null);
                        } else {
                            setText("Patient: " + chat.getPatient().getName());
                            // Unread indicator can be added later
                        }
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to load chats.");
        }
    }

    // ==================== MESSAGE DISPLAY ====================
    @FXML
    public void onChatSelect() {
        if (mode == Mode.PATIENT) {
            @SuppressWarnings("unchecked")
            Practitioner p = ((ListView<Practitioner>) chatListView).getSelectionModel().getSelectedItem();
            if (p != null) {
                selectedPractitioner = p;
                chatPartnerLabel.setText("Dr. " + p.getName());
                loadAndDisplayMessagesForPatient(p);
            }
        } else if (mode == Mode.PRACTITIONER) {
            @SuppressWarnings("unchecked")
            Chat chat = ((ListView<Chat>) chatListView).getSelectionModel().getSelectedItem();
            if (chat != null) {
                selectedChat = chat;
                chatPartnerLabel.setText("Patient: " + chat.getPatient().getName());
                loadAndDisplayMessagesForPractitioner(chat);
            }
        }
    }

    private void loadAndDisplayMessagesForPatient(Practitioner practitioner) {
        if (currentPatient == null || practitioner == null) return;
        try {
            Chat chat = chatDAO.getChatByParticipants(currentPatient.getID(), practitioner.getID());
            messageArea.getChildren().clear();
            if (chat == null) {
                // ✅ إنشاء رسالة ترحيب أولية وحفظها
                chat = new Chat(currentPatient, practitioner);
                chatDAO.add(chat);

                Message welcome = new Message(
                        chat.getId(),
                        SenderType.PATIENT,
                        currentPatient.getID(),
                        practitioner.getID(),
                        "Hello Dr. " + practitioner.getName() + ", how can I help you today?"
                );
                messageDAO.add(welcome);
                loadAndDisplayMessagesForPatient(practitioner); // reload
                return;
            }

            List<Message> msgs = messageDAO.getMessagesByChatId(chat.getId());
            displayMessages(msgs, msg -> msg.getSenderType() == SenderType.PATIENT);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to load messages.");
        }
    }

    private void loadAndDisplayMessagesForPractitioner(Chat chat) {
        if (chat == null || currentPractitioner == null) return;
        try {
            List<Message> msgs = messageDAO.getMessagesByChatId(chat.getId());
            displayMessages(msgs, msg -> msg.getSenderType() == SenderType.PRACTITIONER);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to load messages.");
        }
    }

    private void displayMessages(List<Message> messages, java.util.function.Predicate<Message> isSelf) {
        Platform.runLater(() -> {
            messageArea.getChildren().clear();
            for (Message msg : messages) {
                boolean self = isSelf.test(msg);
                HBox box = new HBox(10);
                box.setPadding(new Insets(5, 10, 5, 10));

                String senderName = self ? "You" :
                        (mode == Mode.PATIENT ? (selectedPractitioner != null ? "Dr. " + selectedPractitioner.getName() : "Doctor") :
                                (selectedChat != null && selectedChat.getPatient() != null ? selectedChat.getPatient().getName() : "Patient"));

                Text sender = new Text(senderName + ": ");
                sender.setStyle("-fx-font-weight: bold; -fx-fill: #2D3436;");
                Text content = new Text(msg.getMessageText());
                content.setStyle("-fx-fill: #2D3436;");

                box.setStyle(self ?
                        "-fx-background-color: #DCF8C6; -fx-background-radius: 15 15 0 15; -fx-padding: 8 12;" :
                        "-fx-background-color: #FFFFFF; -fx-border-color: #E0E0E0; -fx-border-width: 1px; -fx-background-radius: 15 15 15 0; -fx-padding: 8 12;"
                );
                box.getChildren().addAll(sender, content);
                messageArea.getChildren().add(box);
            }
            if (!messageArea.getChildren().isEmpty()) {
                messagesScrollPane.setVvalue(1.0);
            }
        });
    }

    // ==================== SENDING MESSAGES ====================
    @FXML
    public void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty()) return;

        try {
            if (mode == Mode.PATIENT && selectedPractitioner != null) {
                Chat chat = chatDAO.getChatByParticipants(currentPatient.getID(), selectedPractitioner.getID());
                if (chat == null) {
                    chat = new Chat(currentPatient, selectedPractitioner);
                    chatDAO.add(chat);
                }
                Message msg = new Message(
                        chat.getId(),
                        SenderType.PATIENT,
                        currentPatient.getID(),
                        selectedPractitioner.getID(),
                        text
                );
                messageDAO.add(msg);
                loadAndDisplayMessagesForPatient(selectedPractitioner);
            } else if (mode == Mode.PRACTITIONER && selectedChat != null) {
                Message msg = new Message(
                        selectedChat.getId(),
                        SenderType.PRACTITIONER,
                        currentPractitioner.getID(),
                        selectedChat.getPatient().getID(),
                        text
                );
                messageDAO.add(msg);
                loadAndDisplayMessagesForPractitioner(selectedChat);
            } else {
                showError("No recipient", "Please select a chat first.");
                return;
            }
            messageField.clear();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Send Failed", "Could not send message: " + e.getMessage());
        }
    }

    // ==================== HELPERS ====================
    private void showError(String title, String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    @FXML
    private void handleProfile() {
        try {
            Stage stage = (Stage) profileButton.getScene().getWindow();
            if (mode == Mode.PATIENT && currentPatient != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Patient.fxml"));
                Parent root = loader.load();
                PatientController controller = loader.getController();
                controller.setPatient(currentPatient);
                stage.setScene(new Scene(root));
                stage.setTitle("Patient Dashboard — " + currentPatient.getName());
            } else if (mode == Mode.PRACTITIONER && currentPractitioner != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Doctor.fxml"));
                Parent root = loader.load();
                DoctorController controller = loader.getController();
                controller.setDoctor(currentPractitioner);
                stage.setScene(new Scene(root));
                stage.setTitle("Doctor Dashboard — Dr. " + currentPractitioner.getName());
            } else {
                showError("Navigation Error", "User session not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Navigation Failed", "Could not return to dashboard.");
        }
    }

    @FXML
    private void filterAllDoctors() {
        loadPatientChatList();
    }
    @FXML
    private void filterMyChats() {
        if (mode == Mode.PATIENT && currentPatient != null) {
            try {
                PractitionerDAO practitionerDAO = new PractitionerDAO();
                List<Practitioner> activeChats = practitionerDAO.getPractitionersByPatientId(currentPatient.getID());

                Platform.runLater(() -> {
                    @SuppressWarnings("unchecked")
                    ListView<Practitioner> lv = (ListView<Practitioner>) chatListView;
                    lv.getItems().clear();
                    lv.getItems().addAll(activeChats);

                    if (activeChats.isEmpty()) {
                        listTitleLabel.setText("No active chats");
                    } else {
                        listTitleLabel.setText("My Chats (" + activeChats.size() + ")");
                    }

                    lv.setCellFactory(param -> new ListCell<Practitioner>() {
                        @Override
                        protected void updateItem(Practitioner p, boolean empty) {
                            super.updateItem(p, empty);
                            if (empty || p == null) {
                                setText("");
                            } else {
                                setText("Dr. " + p.getName());
                            }
                        }
                    });
                });
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error", "Failed to load active chats.");
            }
        } else if (mode == Mode.PRACTITIONER && currentPractitioner != null) {
            try {
                PatientDAO patientDAO = new PatientDAO();
                List<Patient> activeChats = patientDAO.getPatientsWithChatsForPractitioner(currentPractitioner.getID());

                Platform.runLater(() -> {
                    @SuppressWarnings("unchecked")
                    ListView<Patient> lv = (ListView<Patient>) chatListView;
                    lv.getItems().clear();
                    lv.getItems().addAll(activeChats);

                    if (activeChats.isEmpty()) {
                        listTitleLabel.setText("No active chats");
                    } else {
                        listTitleLabel.setText("My Chats (" + activeChats.size() + ")");
                    }

                    lv.setCellFactory(param -> new ListCell<Patient>() {
                        @Override
                        protected void updateItem(Patient p, boolean empty) {
                            super.updateItem(p, empty);
                            if (empty || p == null) {
                                setText("");
                            } else {
                                setText("Patient: " + p.getName());
                            }
                        }
                    });
                });
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error", "Failed to load active chats.");
            }
        }
    }
}