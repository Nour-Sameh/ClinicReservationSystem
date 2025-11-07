/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.clinicsystem;

/**
 *
 * @author Ahmed Mamdouh
 */
public class Appointment {
    private Patient patient;
    private Clinic clinic;
    private TimeSlot appointmentDateTime;
    
    
    // Constructor
    public Appointment(Patient patient, Clinic clinic, TimeSlot appointmentDateTime) {
        this.patient = patient;
        this.clinic = clinic;
        this.appointmentDateTime = appointmentDateTime;
    }
    
    
     // Getters
    public Patient getPatient() {
        return patient;
    }

    public Clinic getClinic() {
        return clinic;
    }

    public TimeSlot getAppointmentDateTime() {
        return appointmentDateTime;
    }

    // Setters
    public void setAppointmentDateTime(TimeSlot appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }
    
    
    // tostring()
    @Override
    public String toString() {
        return "Appointment{" +
                "patient=" + patient +
                ", clinic=" + clinic +
                ", appointmentDateTime=" + appointmentDateTime +
                '}';
    }
}