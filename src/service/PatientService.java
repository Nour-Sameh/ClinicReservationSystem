/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.AppointmentDAO;
import dao.ClinicDAO;
import dao.RatingDAO;
import dao.TimeSlotDAO;
import dao.WaitingListDAO;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import model.Appointment;
import model.Clinic;
import model.Patient;
import model.Rating;
import model.Status;
import model.TimeSlot;
import model.WaitingList;

/**
 *
 * @author mariam
 */
public class PatientService {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final TimeSlotDAO timeSlotDAO = new TimeSlotDAO();
    private final ClinicDAO clinicDAO = new ClinicDAO();
    private final RatingDAO ratingDAO = new RatingDAO();
    private final RatingService ratingService = new RatingService();
    private final WaitingListDAO waitingListDAO = new WaitingListDAO(); 

    public boolean bookAppointment(Patient patient, Clinic clinic, TimeSlot slot) throws SQLException {
        if (slot.isBooked()) return false;

        Appointment appointment = new Appointment(patient, clinic, slot);
        appointment.setStatus(Status.Booked);
        appointmentDAO.add(appointment);
        
        slot.markAsBooked();
        timeSlotDAO.update(slot);
        
        clinic.getAppointments().add(appointment);
        patient.getAppointmentList().add(appointment);

        return true;
    }
    
    public void cancelAppointment(Patient patient, Appointment appointment) throws SQLException {
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.cancel(appointment);
        
        patient.getAppointmentList().remove(appointment);
        appointment.getClinic().getAppointments().remove(appointment);   
    }

    
    public boolean addRating(Patient patient, Clinic clinic, Rating rating) throws SQLException {
        if (ratingService.isDuplicateRating(patient, clinic)) {
            return false;
        }
        
        ratingDAO.add(rating);
        clinic.getRatings().add(rating);
        return true;
    }
    
    public void deleteRating(Patient patient, Clinic clinic) throws SQLException {
        List<Rating> rAll = ratingDAO.getRatingsByClinicId(clinic.getID());
        for(Rating r : rAll){
            if(r.getPatient()==patient){
                ratingService.deleteRating(r);
                break;
            }
        }   
    }    
}