/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.*;
import jakarta.mail.MessagingException;
import java.util.*;
import model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import model.Appointment;
import model.Patient;

/**
 *
 * @author mariam
 */

public class ClinicService {

    private final ClinicDAO clinicDAO = new ClinicDAO();
    private final TimeSlotDAO timeSlotDAO = new TimeSlotDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final WorkingHoursRuleDAO ruleDAO = new WorkingHoursRuleDAO();
    private final WaitingListDAO waitingListDAO = new WaitingListDAO();
    private final AppointmentService appointmentService = new AppointmentService();
    private final NotificationService notificationService = new NotificationService();

   
//    public void cancelAppointmentsInDay(Clinic clinic, DayOfWeek day) throws SQLException {
//        List<Appointment> toCancel = new ArrayList<>();
//        for(Appointment a : clinic.getAppointments()) {
//            if(a.getAppointmentDateTime().getDay() == day) {
//                toCancel.add(a);
//            }
//        }
//        
//        for(Appointment a : toCancel) {
//            appointmentService.cancel(a);
//            clinic.getAppointments().remove(a);
//            System.out.println("Appointment for " + a.getPatient().getName() + " cancelled on " + day);
//        }
//    }

   public void notifyWaitingList(Clinic clinic, TimeSlot freedSlot) { // عظمههههههههههههههههههه
        try {
            if (clinic.getWaitingList().isEmpty()) return;

            WaitingList next = clinic.getWaitingList().peek();
            Patient p = next.getPatient();

            String subject = "Clinic Slot Available!";
            String body = "<h3>Hi " + p.getName() + ",</h3>"
                    + "<p>A time slot just became available at <b>" + freedSlot + "</b>.</p>"
                    + "<p>You have 1 minute to book it</p>";

            notificationService.sendEmail(p.getEmail(), subject, body);

            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    try {
                        boolean booked = appointmentService.isBooked(p, freedSlot);
                        if (booked) {
                            clinic.getWaitingList().remove(next);
                            waitingListDAO.delete(next.getId());
                        } else {
                            clinic.getWaitingList().poll();
                            waitingListDAO.delete(next.getId());
                            notifyWaitingList(clinic, freedSlot);
                        }
                    } catch (SQLException e) {
                        System.err.println("Error processing waiting list: " + e.getMessage());
                    }
                }
            }, 60 * 1000);
        } catch (Exception e) {
            System.err.println("Error notifying waiting list: " + e.getMessage());
        }
    }
   
   
    public void updateSchedule(Clinic clinic, int slotDuration, List<WorkingHoursRule> newRules, LocalDate startDate, LocalDate endDate) throws SQLException, MessagingException {
        
        List<TimeSlot> oldSlots = new ArrayList<>(timeSlotDAO.getSlotsByClinic(clinic.getID()));

        List<Appointment> oldAppointments = new ArrayList<>(clinic.getAppointments());
        oldAppointments.sort((a, b) -> {
            int dateCompare = a.getAppointmentDateTime().getDate()
                                .compareTo(b.getAppointmentDateTime().getDate());
            if (dateCompare != 0) {
                return dateCompare;
            }
            return a.getAppointmentDateTime().getStartTime()
                     .compareTo(b.getAppointmentDateTime().getStartTime());
        });

        clinic.getSchedule().setWeeklyRules(newRules);
        clinic.getSchedule().setSlotDurationInMinutes(slotDuration);

        clinic.getSchedule().generateTimeSlots(startDate, endDate);
        List<TimeSlot> newSlots = clinic.getSchedule().getSlots();
        newSlots.sort((s1, s2) -> {
            int cmp = s1.getDate().compareTo(s2.getDate());
            if (cmp != 0) return cmp;
            return s1.getStartTime().compareTo(s2.getStartTime());
        });
        
        for (TimeSlot oldSlot : oldSlots) {
            if (!newSlots.contains(oldSlot)) {
                timeSlotDAO.delete(oldSlot.getId());
            }
        }

        newSlots.forEach(TimeSlot::markAsAvailable);
        for (TimeSlot slot : newSlots) {
            timeSlotDAO.add(slot);
        }
        for (Appointment app : oldAppointments) {
            boolean scheduled = false;
            for (TimeSlot slot : newSlots) {
                if (!slot.isBooked() && !slot.isCancelled()) {
                    slot.markAsBooked();
                    app.setAppointmentDateTime(slot);
                    appointmentDAO.update(app);
                    scheduled = true;
                    timeSlotDAO.update(slot);
                    break;
                }
            }
            if (!scheduled) {
                addToWaitingList(clinic, app.getPatient()); 
                String subject = "Your appointment is cancelled !";
                String body = "<h3>Hi " + app.getPatient().getName() + ",</h3>"
                        + "<p>We already put you in waiting list </p>";

                notificationService.sendEmail(app.getPatient().getEmail(), subject, body); 
                appointmentService.cancelWithoutNoty(app); 
            }
        }

        clinic.setSchedule(clinic.getSchedule());
    }

    public void addToWaitingList(Clinic clinic, Patient patient) throws SQLException {
        WaitingList x = new WaitingList(patient,clinic);
        clinic.getWaitingList().add(x);
        waitingListDAO.add(x);
    }
}
        