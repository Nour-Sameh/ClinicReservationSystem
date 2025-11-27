/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author noursameh
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Javengers
 */
public class Appointment {
    private int id; //  تم إضافته
    private Patient patient;
    private Clinic clinic;
    private TimeSlot appointmentDateTime;
    private Status status;
    
    // Constructor: creates an appointment and marks the time slot as booked
    public Appointment(int id, Patient patient, Clinic clinic, TimeSlot appointmentDateTime, Status status) {
        this.id = id;
        this.patient = patient;
        this.clinic = clinic;
        this.appointmentDateTime = appointmentDateTime;
        this.status = status;
    }
    
    // Constructor للإضافة (بدون id، الحالة الافتراضية Booked)
    public Appointment(Patient patient, Clinic clinic, TimeSlot appointmentDateTime) {
        this(0, patient, clinic, appointmentDateTime, Status.Booked);
        appointmentDateTime.markAsBooked(); // يتم حجز الموعد في الـ TimeSlot
    }
    

    // Getters and Setters
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public Clinic getClinic() { return clinic; }
    public TimeSlot getAppointmentDateTime() { return appointmentDateTime; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public void setAppointmentDateTime(TimeSlot appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public void cancel() {
        this.status = Status.Cancelled;
        appointmentDateTime.markAsCancelled();
        clinic.notifyWaitingList(appointmentDateTime);//// in servise
        clinic.getAppointments().remove(this);
        patient.getAppointmentList().remove(this);

    }

    public void reschedule(TimeSlot newSlot) {
        appointmentDateTime.markAsAvailable();
        this.appointmentDateTime = newSlot;
        newSlot.markAsBooked();
        this.status = Status.Booked;
    }
    
    // Returns a string representation of the appointment details
    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", patient=" + (patient != null ? patient.getID() : "N/A") +
                ", clinic=" + (clinic != null ? clinic.getID() : "N/A") +
                ", timeslotId=" + (appointmentDateTime != null ? appointmentDateTime.getId() : "N/A") +
                ", status=" + status +
                '}';
    }
}

