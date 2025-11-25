/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.util.List;

/**
 * Represents a unique chat session between a Patient and a Practitioner.
 */
public class Chat {
    private int id;
    private Patient patient;
    private Practitioner practitioner; 
    private List<Message> messages; 

    public Chat(int id, Patient patient, Practitioner practitioner) {
        this.id = id;
        this.patient = patient;
        this.practitioner = practitioner;
    }

    public Chat(Patient patient, Practitioner practitioner) {
        this(0, patient, practitioner);
    }

    // Getters and Setters:
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public Practitioner getPractitioner() { return practitioner; }

    public void setPatient(Patient patient) { this.patient = patient; }
    public void setPractitioner(Practitioner practitioner) { this.practitioner = practitioner; }
    
    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    @Override
    public String toString() {
        return "Chat{" +
                "id=" + id +
                ", patientId=" + (patient != null ? patient.getID() : "N/A") +
                ", practitionerId=" + (practitioner != null ? practitioner.getID() : "N/A") +
                '}';
    }
}