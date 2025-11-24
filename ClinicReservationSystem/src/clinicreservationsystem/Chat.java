/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clinicreservationsystem;

import java.util.*;

/**
 *
 * @author noursameh
 */
public class Chat {
    private int id;
    private int patientId;
    private int practitionerId;
    private List<Message> messages;

    public Chat(int id, int patientId, int practitionerId) {
        this.id = id;
        this.patientId = patientId;
        this.practitionerId = practitionerId;
        this.messages = new ArrayList<>();
    }

    public int getId() { return id; }
    public int getPatientId() { return patientId; }
    public int getPractitionerId() { return practitionerId; }
    public List<Message> getMessages() { return messages; }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public void showChat() {
        for (Message m : messages) {
            System.out.println(m.getTimestamp() + " | " + m.getSenderId() + ": " + m.getMessageText());
        }
    }
}
