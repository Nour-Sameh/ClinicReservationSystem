/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.AppointmentDAO;
import dao.TimeSlotDAO;
import java.sql.SQLException;
import java.util.List;
import model.Appointment;
import model.Patient;
import model.Status;
import model.TimeSlot;

/**
 *
 * @author noursameh
 */
public class AppointmentService {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final TimeSlotDAO timeSlotDAO = new TimeSlotDAO();
    private final ClinicService clinicService = new ClinicService();
    
    public boolean isBooked(Patient patient, TimeSlot slot) {
        try {
            List<Appointment> appointments = appointmentDAO.getAppointmentList(patient);

            for (Appointment a : appointments) {
                if (a.getAppointmentDateTime().equals(slot)) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error checking booked status: " + e.getMessage());
            return false;
        }
    }
    
    public void cancel(Appointment appointment) throws SQLException {
        
        appointment.setStatus(Status.Cancelled);
        appointment.getAppointmentDateTime().markAsCancelled();
        
        appointmentDAO.update(appointment);
        timeSlotDAO.update(appointment.getAppointmentDateTime());
        
        appointment.getClinic().getAppointments().remove(appointment);
        appointment.getPatient().getAppointmentList().remove(appointment);
            
        clinicService.notifyWaitingList(appointment.getClinic(), appointment.getAppointmentDateTime());
    }

    
    public boolean reschedule(Appointment appointment, TimeSlot newSlot) throws SQLException {
        if (appointment == null || newSlot == null || appointment.getClinic() == null) return false;
        if (newSlot.isBooked()) return false;

        TimeSlot oldSlot = appointment.getAppointmentDateTime();
        oldSlot.markAsAvailable();
        timeSlotDAO.update(oldSlot);

        appointment.setAppointmentDateTime(newSlot);
        newSlot.markAsBooked();
        timeSlotDAO.update(newSlot);

        appointmentDAO.update(appointment);

        clinicService.notifyWaitingList(appointment.getClinic(), newSlot);

        return true;
    }

}