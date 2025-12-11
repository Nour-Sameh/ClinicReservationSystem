package service;

import dao.*;
import database.DBConnection;
import jakarta.mail.MessagingException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.sql.SQLException;
import java.time.LocalDate;
import model.*;

/**
 * Service class to manage clinics, schedules, waiting lists, and notifications.
 */
public class ClinicService {

    private final ClinicDAO clinicDAO = new ClinicDAO();
    private final TimeSlotDAO timeSlotDAO = new TimeSlotDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final WorkingHoursRuleDAO ruleDAO = new WorkingHoursRuleDAO();
    private final WaitingListDAO waitingListDAO = new WaitingListDAO();
    private AppointmentService appointmentService; // يتم تعيينه من الخارج لتجنب circular dependency
    private final NotificationService notificationService = new NotificationService();


    public void setAppointmentService(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Notifies the first patient in the waiting list about an available time slot.
     */
    public void notifyWaitingList(Clinic clinic, TimeSlot freedSlot) {
        if (appointmentService == null) {
            System.err.println("AppointmentService not set in ClinicService!");
            return;
        }

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
                            notifyWaitingList(clinic, freedSlot); // إعادة المحاولة
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

    /**
     * Updates the schedule of a clinic and reassigns appointments.
     */
    public void updateSchedule(Clinic clinic, int slotDuration, List<WorkingHoursRule> newRules,
                               LocalDate startDate, LocalDate endDate) throws SQLException, MessagingException {

        List<TimeSlot> oldSlots = new ArrayList<>(timeSlotDAO.getSlotsByClinic(clinic.getID()));
        List<Appointment> oldAppointments = new ArrayList<>(clinic.getAppointments());

        oldAppointments.sort((a, b) -> {
            int dateCompare = a.getAppointmentDateTime().getDate()
                    .compareTo(b.getAppointmentDateTime().getDate());
            if (dateCompare != 0) return dateCompare;
            return a.getAppointmentDateTime().getStartTime()
                    .compareTo(b.getAppointmentDateTime().getStartTime());
        });

        clinic.getSchedule().setWeeklyRules(newRules);
        clinic.getSchedule().setSlotDurationInMinutes(slotDuration);
        clinic.getSchedule().generateTimeSlots(startDate, endDate);

        List<TimeSlot> newSlots = clinic.getSchedule().getSlots();
        newSlots.sort(Comparator.comparing(TimeSlot::getDate)
                .thenComparing(TimeSlot::getStartTime));


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
                appointmentService.cancelWithoutNotify(app);
            }
        }

        clinic.setSchedule(clinic.getSchedule());
    }

    /**
     * Adds a patient to the waiting list of a clinic.
     */
    public void addToWaitingList(Clinic clinic, Patient patient) throws SQLException {
        WaitingList x = new WaitingList(patient, clinic);
        clinic.getWaitingList().add(x);
        waitingListDAO.add(x);
    }

    /**
     * Retrieves a clinic by the practitioner ID.
     */
    public Clinic getClinicByPractitionerId(int practitionerId) {
        try {
            return clinicDAO.getClinicByPractitionerId(practitionerId);
        } catch (SQLException e) {
            System.err.println("Error loading clinic: " + e.getMessage());
            return null;
        }

    }



    public void addClinicForDoctor(Clinic clinic, Practitioner doctor) throws SQLException {
        // 1. Insert clinic (without doctor info)
        clinicDAO.add(clinic);

        // 2. Try to update doctor_id and doctor_name (safe: ignores if columns don't exist)
        try (Connection con = DBConnection.getConnection()) {
            String sql = "UPDATE Clinics SET doctor_id = ?, doctor_name = ? WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, doctor.getID());
                ps.setString(2, doctor.getName());
                ps.setInt(3, clinic.getID());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            // Ignore if columns don't exist (e.g., "Unknown column 'doctor_id'") — still works
            System.out.println("⚠️ Note: doctor_id/doctor_name not saved (columns may be missing in DB).");
        }

        // 3. Link clinic to doctor in Practitioner table
        doctor.setClinic(clinic);
        PractitionerDAO practitionerDAO = new PractitionerDAO();
        practitionerDAO.update(doctor);
    }


    public int getDepartmentIdByName(String specialtyName) throws SQLException {

        String sql = "SELECT id FROM Departments WHERE name = ?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);

            ps.setString(1, specialtyName);
            rs = ps.executeQuery();

            if (rs.next()) {

                return rs.getInt("id");
            }

            return -1;

        } finally {

            if (rs != null) rs.close();
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }
    public List<Clinic> getAllClinicsForPatientView() throws SQLException {

        List<Clinic> clinics = clinicDAO.getAll();

        for (Clinic clinic : clinics) {

            if (clinic.getDepartmentID() > 0) {
                String department = DepartmentService.getDepartmentNameById(clinic.getDepartmentID());
                if (!"Unknown Specialty".equals(department)) {
                    clinic.setDepartmentName(department);
                }
            }


            int practitionerId = clinic.getDoctorID();


            PractitionerDAO practitionerDAO=new PractitionerDAO();
            Practitioner doctor = practitionerDAO.getById(practitionerId);

            if (doctor != null) {

                clinic.setDoctorName(doctor.getName());
            }


            RatingService rate=new RatingService();
            double avgRating = rate.calculateAverageRating(clinic.getID());
            clinic.setAvgRating(avgRating);
        }

        return clinics;

    }
    public List<Clinic> getClinicsBySpecialt(int departmentID) throws SQLException {


        List<Clinic> filteredClinics = clinicDAO.getByDepartmentId(departmentID);


        for (Clinic clinic : filteredClinics) {

            if (clinic.getDepartmentID() > 0) {
                String department = DepartmentService.getDepartmentNameById(clinic.getDepartmentID());
                if (department != "Unknown Specialty") {
                    clinic.setDepartmentName(department);
                }
            }


            int practitionerId = clinic.getDoctorID();


            PractitionerDAO practitionerDAO=new PractitionerDAO();
            Practitioner doctor = practitionerDAO.getById(practitionerId);

            if (doctor != null) {

                clinic.setDoctorName(doctor.getName());
            }

            try {
                RatingDAO ratingDAO = new RatingDAO();
                List<Rating> ratings = ratingDAO.getRatingsByClinicId(clinic.getID());
                double sum = 0;
                for (Rating r : ratings) {
                    sum += r.getScore();
                }
                double avg = ratings.isEmpty() ? 0.0 : sum / ratings.size();
                clinic.setAvgRating(avg);
            } catch (SQLException e) {
                System.err.println("Error loading ratings for clinic " + clinic.getID() + ": " + e.getMessage());
                clinic.setAvgRating(0.0);
            }
        }

        return filteredClinics;

    }
    public Clinic loadClinicSchedule(Clinic clinic) throws SQLException {



        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT schedule_id FROM Clinics WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, clinic.getID());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int scheduleId = rs.getInt("schedule_id");
                        if (scheduleId <= 0) {
                            System.out.println("⚠️ No schedule ID found for clinic ID: " + clinic.getID());
                            return clinic;
                        }

                        Schedule schedule = scheduleDAO.getById(scheduleId);
                        if (schedule == null) {
                            System.out.println("⚠️ Schedule not found for ID: " + scheduleId);
                            return clinic;
                        }


                        List<TimeSlot> slots = timeSlotDAO.getSlotsByClinic(clinic.getID());
                        schedule.setSlots(slots);

                        List<Appointment> appointments = appointmentDAO.getAppointmentsByClinicId(clinic.getID());
                        clinic.setAppointments(appointments);


                        clinic.setSchedule(schedule);

                        return clinic;

                    } else {
                        System.err.println("❌ Clinic ID " + clinic.getID() + " not found in DB!");
                        return clinic;
                    }
                }
            }
        }
    }
    public List<TimeSlot> getAvailableSlotsForClinic(int clinicId) throws SQLException {
        // 🟢 احضار كل السلوْتز الخاصة بالعيادة
        List<TimeSlot> slots = timeSlotDAO.getSlotsByClinic(clinicId);

        // 🔎 فلترة السلوْتز غير المحجوزة وغير الملغية فقط
        return slots.stream()
                .filter(slot -> !slot.isBooked() && !slot.isCancelled())
                .toList();
    }

    public void updateAverageRating(int clinicId) throws SQLException {
        String sql = """
        UPDATE Clinics 
        SET avg_rating = (
            SELECT COALESCE(AVG(score), 0) 
            FROM Ratings 
            WHERE clinic_id = ?
        )
        WHERE id = ?
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clinicId);
            ps.setInt(2, clinicId);
            ps.executeUpdate();
        }
    }
    // ✅ أضف هذه الدالة في ClinicService.java
    public double calculateAverageRatingForClinic(int clinicId) throws SQLException {
        RatingService ratingService = new RatingService();
        return ratingService.calculateAverageRating(clinicId);
    }
    public void deleteClinic(int clinicId) throws SQLException {
        Connection con = null; // ← نُعرّفها في المدى الخارجي
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // ✅ حذف cascade: appointments, ratings, time slots, working rules, schedule
            String[] tables = {
                    "Appointments",
                    "Ratings",
                    "TimeSlots",
                    "WorkingHoursRules",
                    "Schedules"
            };

            for (String table : tables) {
                String sql = "DELETE FROM " + table + " WHERE clinic_id = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, clinicId);
                    ps.executeUpdate();
                }
            }

            // ✅ حذف العيادة نفسها
            String deleteClinic = "DELETE FROM Clinics WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(deleteClinic)) {
                ps.setInt(1, clinicId);
                int rows = ps.executeUpdate();
                if (rows == 0) throw new SQLException("Clinic not found.");
            }

            con.commit();

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (con != null) {
                try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}
