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

public class PractitionerService {
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PractitionerDAO practitionerDAO = new PractitionerDAO();
    private final ClinicDAO clinicDAO = new ClinicDAO();
    private final RatingDAO ratingDAO = new RatingDAO();
    
    public Clinic getClinic(Practitioner practitioner) {
        try {
            if(practitioner == null) {
                System.err.println("Practitioner cannot be null.");
                return null;
            }
            Clinic clinic =clinicDAO.getById(practitioner.getClinic().getID());
            return clinic;
        } catch (SQLException e) {
            System.err.println("Failed to set clinic: " + e.getMessage());
            return null;
        }
    }
    public boolean setClinic(Practitioner practitioner, Clinic clinic) {
        try {
            if(practitioner == null || clinic == null) {
                System.err.println("Practitioner or Clinic cannot be null.");
                return false;
            }
            clinicDAO.add(clinic);
            practitioner.setClinic(clinic);
            practitionerDAO.update(practitioner);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to set clinic: " + e.getMessage());
            return false;
        }
    }
    
    public List<Appointment> getAppointments(Practitioner practitioner) {
        try {
            if(practitioner.getClinic() == null) {
                System.err.println("Practitioner has no clinic assigned.");
                return new ArrayList<>();
            }
            return appointmentDAO.getAppointmentsByClinicId(practitioner.getClinic().getID());
        } catch (SQLException e) {
            System.err.println("Error fetching appointments: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    
    public List<Rating> getRatings(Practitioner practitioner) {
        try {
            if(practitioner.getClinic() == null) {
                System.err.println("Practitioner has no clinic assigned.");
                return new ArrayList<>();
            }
            int clinicID = practitioner.getClinic().getID();
            return ratingDAO.getRatingsByClinicId(clinicID);
        } catch (SQLException e) {
            System.err.println("Error fetching ratings: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public void updateClinicInfo(Clinic clinic, String name, String address, double price) {
        try {
            if(clinic == null) {
                System.err.println("Clinic cannot be null.");
                return;
            }
            if(name == null || name.isEmpty()) {
                System.err.println("Clinic name cannot be empty.");
                return;
            }
            if(address == null || address.isEmpty()) {
                System.err.println("Clinic address cannot be empty.");
                return;
            }
            if(price < 0) {
                System.err.println("Clinic price cannot be negative.");
                return;
            }
            
            clinic.setName(name);
            clinic.setAddress(address);
            clinic.setPrice(price);
            clinicDAO.update(clinic);

            System.out.println("Clinic updated successfully.");
        } catch (SQLException e) {
            System.err.println("Error updating clinic info: " + e.getMessage());
        }
    }
}
