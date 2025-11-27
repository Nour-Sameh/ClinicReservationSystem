/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.AppointmentDAO;
import dao.TimeSlotDAO;
import java.sql.SQLException;
import model.Appointment;
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
    
    public void cancel(Appointment appointment) throws SQLException {
        appointment.setStatus(Status.Cancelled);
        appointment.getAppointmentDateTime().markAsCancelled();
        
        clinicService.notifyWaitingList(appointment.getClinic(), appointment.getAppointmentDateTime());
        
        appointment.getClinic().getAppointments().remove(appointment);
        appointment.getPatient().getAppointmentList().remove(appointment);
        
        appointmentDAO.update(appointment);
        timeSlotDAO.update(appointment.getAppointmentDateTime());
    }

    public boolean reschedule(Appointment appointment, TimeSlot newSlot) throws SQLException {
        if (newSlot.isBooked())
            return false;
        
        appointment.getAppointmentDateTime().markAsAvailable();
        
        appointment.setAppointmentDateTime(newSlot);
        newSlot.markAsBooked();

        appointment.setStatus(Status.Booked);
        
        appointmentDAO.update(appointment);
        return true;
    }
}
