/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.ClinicDAO;
import dao.RatingDAO;
import dao.TimeSlotDAO;
import dao.WaitingListDAO;
import java.util.List;
import model.Clinic;
import model.Rating;
import model.TimeSlot;
import model.WaitingList;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import model.Appointment;
import model.Patient;

/**
 *
 * @author mariam
 */
public class ClinicService {

    private final ClinicDAO clinicDAO = new ClinicDAO();
    private final TimeSlotDAO timeSlotDAO = new TimeSlotDAO();
    private final RatingDAO ratingDAO = new RatingDAO();
    private final WaitingListDAO waitingListDAO = new WaitingListDAO();
    private final AppointmentService appointmentService = new AppointmentService();
    
    public void addRating(Clinic clinic, Rating rating) throws SQLException {
        clinic.addToRatings(rating);
        ratingDAO.update(rating); 
    }
    
    public void cancelAppointmentsInDay(Clinic clinic, DayOfWeek day) throws SQLException {
        List<Appointment> toCancel = new ArrayList<>();
        for(Appointment a : clinic.getAppointments()) {
            if(a.getAppointmentDateTime().getDay() == day) {
                toCancel.add(a);
            }
        }
        
        for(Appointment a : toCancel) {
            appointmentService.cancel(a);
            clinic.getAppointments().remove(a);
            System.out.println("Appointment for " + a.getPatient().getName() + " cancelled on " + day);
        }
    }

    public void addToWaitingList(Clinic clinic, Patient patient) throws SQLException {
        WaitingList x = new WaitingList(patient,clinic);
        clinic.getWaitingList().add(x);
        waitingListDAO.add(x);
    }
    
    public void notifyWaitingList(Clinic clinic, TimeSlot freedSlot) throws SQLException {
        if(clinic.getWaitingList().isEmpty()) return;

        WaitingList next = clinic.getWaitingList().poll();
        Patient p = next.getPatient();

        waitingListDAO.delete(next.getId());
        
        // الفكرة هنا: تبعتي Notification (GUI message أو Email)
        System.out.println("Notification to " + p.getEmail() +
                           ": A slot became available at " + freedSlot +
                           ". Would you like to book it?");

    }
    
    public void shiftSchedule(Clinic clinic, int minutes) throws SQLException {

        for (TimeSlot slot : clinic.getSchedule().getSlots()) {

            LocalTime newStart = slot.getStartTime().plusMinutes(minutes);
            LocalTime newEnd = slot.getEndTime().plusMinutes(minutes);

            // خارج اليوم → cancel appointments
            if (newStart.isBefore(LocalTime.MIN) || newEnd.isAfter(LocalTime.MAX)) {

                for (Appointment app : clinic.getAppointments()) {

                    if (app.getAppointmentDateTime().equals(slot)) {
                        appointmentService.cancel(app);
                    }
                }
            }

            slot.setStartTime(newStart);
            slot.setEndTime(newEnd);

            timeSlotDAO.update(slot);
        }
    }
    
    public void removeDay(Clinic clinic, DayOfWeek day) throws SQLException {

        List<TimeSlot> removedSlots =
                clinic.getSchedule().getSlots().stream()
                        .filter(s -> s.getDay() == day)
                        .toList();

        // Cancel appointments in these slots
        for (Appointment app : new ArrayList<>(clinic.getAppointments())) {
            if (removedSlots.contains(app.getAppointmentDateTime())) {
                appointmentService.cancel(app);
            }
        }
        
        clinic.getSchedule().getSlots().removeIf(s -> s.getDay() == day);

        for (TimeSlot s : removedSlots) {
            timeSlotDAO.delete(s.getId());
        }
    }
    
    public void regenerateSlots(Clinic clinic, LocalDate start, LocalDate end) throws SQLException {
        List<TimeSlot> oldSlots = new ArrayList<>(clinic.getSchedule().getSlots());

        clinic.getSchedule().generateTimeSlots(start, end);

        List<TimeSlot> newSlots = clinic.getSchedule().getSlots();

        for (Appointment a : new ArrayList<>(clinic.getAppointments())) {
            if (!newSlots.contains(a.getAppointmentDateTime())) {
                appointmentService.cancel(a);
            }
        }

        for (TimeSlot s : oldSlots) {
            if (!newSlots.contains(s)) {
                timeSlotDAO.delete(s.getId());
            }
        }

        for (TimeSlot s : newSlots) {
            if (s.getId() == 0) { 
                timeSlotDAO.add(s);
            }
        }
    }
    
    
}

