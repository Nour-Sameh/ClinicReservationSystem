/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDateTime;
/**
 * Represents an entry in the WaitingList for a specific Clinic.
 * Stores patient, clinic, request time, and the current status of the request.
 * @author noursameh
 */
public class WaitingList {
    private int id; 
    private Patient patient;
    private Clinic clinic; 
    private LocalDateTime requestTime; 
    private Status status; 

    public WaitingList(int id, Patient patient, Clinic clinic, LocalDateTime requestTime, Status status) {
        this.id = id;
        this.patient = patient;
        this.clinic = clinic;
        this.requestTime = requestTime;
        this.status = status;
    }

    public WaitingList(Patient patient, Clinic clinic) {
        this(0, patient, clinic, LocalDateTime.now(), Status.Booked); 
    }


    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public Clinic getClinic() { return clinic; }
    public LocalDateTime getRequestTime() { return requestTime; }
    public Status getStatus() { return status; }
    
    public void setStatus(Status status) { this.status = status; }

    @Override
    public String toString() {
        return "WaitingList{" +
                "id=" + id +
                ", patientId=" + (patient != null ? patient.getID() : "N/A") +
                ", clinicId=" + (clinic != null ? clinic.getID() : "N/A") +
                ", requestTime=" + requestTime +
                ", status=" + status +
                '}';
    }
}