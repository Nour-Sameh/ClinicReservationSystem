/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clinicreservationsystem;

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
    private Patient patient;
    private Clinic clinic;
    private TimeSlot appointmentDateTime;
    
    // Constructor: creates an appointment and marks the time slot as booked
    public Appointment(Patient patient, Clinic clinic, TimeSlot appointmentDateTime) {
        this.patient = patient;
        this.clinic = clinic;
        this.appointmentDateTime = appointmentDateTime;
        appointmentDateTime.markAsBooked();
    }
    
    // Returns the patient associated with this appointment
    public Patient getPatient() {
        return patient;
    }

    // Returns the clinic associated with this appointment
    public Clinic getClinic() {
        return clinic;
    }

    // Returns the time slot of this appointment
    public TimeSlot getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void cancel() {
        appointmentDateTime.markAsCancelled();
        clinic.notifyWaitingList(appointmentDateTime);
        clinic.getAppointments().remove(this);
        patient.getAppointmentList().remove(this);

    }

    public void reschedule(TimeSlot newSlot) {
        appointmentDateTime.markAsAvailable();
        this.appointmentDateTime = newSlot;
        newSlot.markAsBooked();
    }
    
    
    
    // Returns a string representation of the appointment details
    @Override
    public String toString() {
        return "Appointment{" +
                "patient=" + patient +
                ", clinic=" + clinic +
                ", appointmentDateTime=" + appointmentDateTime +
                '}';
    }
}

