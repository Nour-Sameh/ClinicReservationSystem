/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.AppointmentDAO;
import dao.ClinicDAO;
import dao.PractitionerDAO;
import dao.RatingDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Appointment;
import model.Clinic;
import model.Practitioner;
import model.Rating;

/**
 *
 * @author mariam
 */
public class PractitionerService {
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PractitionerDAO practitionreDAO = new PractitionerDAO();
    private final ClinicDAO clinicDAO = new ClinicDAO();
    private final RatingDAO ratingDAO = new RatingDAO();

    public List<Appointment> getAppointments(Practitioner practitioner) throws SQLException {
        List<Appointment> appointments = appointmentDAO.getAll();
        List<Appointment> r = new ArrayList<Appointment>();

        for(Appointment a: appointments) {
            if(a.getClinic().getID() == practitioner.getClinic().getID()) {
                r.add(a);
            }
        }
        return r;
    }

    
    public List<Rating> getRatings(Practitioner practitioner) throws SQLException {
        int clinicID = practitioner.getClinic().getID();

        List<Rating> all = ratingDAO.getAll();
        List<Rating> r = new ArrayList<>();

        for (Rating rating : all) {
            if (rating.getClinic().getID() == clinicID) {
                r.add(rating);
            }
        }
        
        return r;
    }
    
    public void updateClinicInfo(Clinic clinic, String name, String address, double price) throws SQLException {
        clinic.setName(name);
        clinic.setAddress(address);
        clinic.setPrice(price);
        
        clinicDAO.update(clinic);
    }
}
