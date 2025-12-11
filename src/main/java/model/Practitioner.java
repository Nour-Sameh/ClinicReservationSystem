/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDate;
import java.util.*;

/**
 * Represents a healthcare practitioner (e.g., a doctor) in the clinic system.
 * Inherits basic user attributes and associates the practitioner with a clinic.
 * Responsibilities include managing the clinic, retrieving appointments/ratings,
 * and updating clinic information.
 */
public class Practitioner extends User {

    private Clinic clinic; // The clinic associated with this practitioner

    // Constructor: creates a new practitioner with personal info and associated clinic
    public Practitioner(int ID, String name, String phone, String email, String password, String gender, LocalDate dateOfBirth) {
        super(ID, name, phone, email, password, gender, dateOfBirth);
        this.clinic = null;
    }
    public String getFullName() {
        return name;
    }
    public int getId(){
        return ID;
    }


    // Retrieves the list of appointments for this practitioner’s clinic
    public List<Appointment> getAppointments() {
        if (clinic == null || clinic.getSchedule() == null) {
            return null;
        }
        return clinic.getAppointments();
    }

    // Retrieves the list of ratings for this practitioner’s clinic
    public List<Rating> getRatings() {
        return clinic != null ? clinic.getRatings() : null;
    }

    // Returns the clinic associated with this practitioner
    public Clinic getClinic() {
        return clinic;
    }


    // Sets a new clinic for this practitioner
    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }

    // Updates the clinic's information (name, address, price) if a clinic is assigned
    public void updateClinicInfo(String name, String address, double price) {
        if (clinic != null) {
            clinic.setName(name);
            clinic.setAddress(address);
            clinic.setPrice(price);
        }
    }
}
