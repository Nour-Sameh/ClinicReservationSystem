    /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import database.DBConnection;
import model.Appointment;
import model.Patient;
import model.Clinic;
import model.TimeSlot;
import model.Status;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.sql.Date;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


public class AppointmentDAO implements GenericDAO<Appointment> {

    private final PatientDAO patientDAO = new PatientDAO();
    private final ClinicDAO clinicDAO = new ClinicDAO();
    private final TimeSlotDAO timeSlotDAO = new TimeSlotDAO();

    private Appointment extractAppointmentFromResultSet(ResultSet rs) throws SQLException {
        int patientId = rs.getInt("patient_id");
        int clinicId = rs.getInt("clinic_id");
        int timeslotId = rs.getInt("timeslot_id");

        Patient patient = patientDAO.getById(patientId);
        Clinic clinic = clinicDAO.getById(clinicId);
        TimeSlot timeSlot = timeSlotDAO.getById(timeslotId);

        Status status = Status.fromDatabase(rs.getString("status"));

        Appointment appointment = new Appointment(
                rs.getInt("id"),
                patient,
                clinic,
                timeSlot,
                status
        );

        // ★★ إضافة جديدة: قراءة نوع الحجز و expiry ★★
        String typeStr = rs.getString("appointment_type");
        if (typeStr != null) {
            appointment.setAppointmentType(
                    "CONSULTATION".equals(typeStr) ?
                            Appointment.AppointmentType.CONSULTATION : Appointment.AppointmentType.VISIT
            );
        }

        Date expiryDate = rs.getDate("consultation_expiry_date");
        if (expiryDate != null) {
            appointment.setConsultationExpiryDate(expiryDate.toLocalDate());
        }

        return appointment;
    }

    // Insert
    @Override
    public void add(Appointment appointment) throws SQLException {
        String sql = """
        INSERT INTO Appointments 
        (patient_id, clinic_id, timeslot_id, status, appointment_type, consultation_expiry_date) 
        VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, appointment.getPatient().getID());
            ps.setInt(2, appointment.getClinic().getID());
            ps.setInt(3, appointment.getAppointmentDateTime().getId());
            ps.setString(4, appointment.getStatus().toDatabaseValue());
            ps.setString(5, appointment.getAppointmentType().name()); // VISIT / CONSULTATION
            if (appointment.getConsultationExpiryDate() != null) {
                ps.setDate(6, Date.valueOf(appointment.getConsultationExpiryDate()));
            } else {
                ps.setNull(6, Types.DATE);
            }

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        appointment.setId(rs.getInt(1));
                    }
                }
            }
        }
    }

    // Get by ID
    @Override
    public Appointment getById(int id) throws SQLException {
        String sql = "SELECT * FROM Appointments WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return extractAppointmentFromResultSet(rs);
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }

    // Get All
    @Override
    public List<Appointment> getAll() throws SQLException {
        String sql = "SELECT * FROM Appointments";
        List<Appointment> appointments = new ArrayList<>();
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            st = con.createStatement();
            rs = st.executeQuery(sql);

            while (rs.next()) {
                appointments.add(extractAppointmentFromResultSet(rs));
            }
            return appointments;
        } finally {
            if (rs != null) rs.close();
            if (st != null) st.close();
            DBConnection.closeConnection(con);
        }
    }

    //  Update
    @Override
    public void update(Appointment appointment) throws SQLException {
        String sql = """
        UPDATE Appointments 
        SET patient_id = ?, clinic_id = ?, timeslot_id = ?, status = ?, 
            appointment_type = ?, consultation_expiry_date = ?
        WHERE id = ?
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, appointment.getPatient().getID());
            ps.setInt(2, appointment.getClinic().getID());
            ps.setInt(3, appointment.getAppointmentDateTime().getId());
            ps.setString(4, appointment.getStatus().toDatabaseValue());
            ps.setString(5, appointment.getAppointmentType().name());
            if (appointment.getConsultationExpiryDate() != null) {
                ps.setDate(6, Date.valueOf(appointment.getConsultationExpiryDate()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setInt(7, appointment.getId());

            ps.executeUpdate();
        }
    }

    // Delete
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Appointments WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }
    public List<Appointment> getAppointmentsByClinicId(int clinicId) throws SQLException {
        
        String sql = "SELECT * FROM Appointments WHERE clinic_id = ?";
        List<Appointment> appointments = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, clinicId);
            rs = ps.executeQuery();

            while (rs.next()) {
                appointments.add(extractAppointmentFromResultSet(rs));
            }
            return appointments;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }
    public List<Appointment> getAppointmentsByPatientId(int patientId) throws SQLException {
        String sql = "SELECT * FROM Appointments WHERE patient_id = ?";
        List<Appointment> appointments = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    appointments.add(extractAppointmentFromResultSet(rs));
                }
            }
        }
        return appointments;
    }
    public List<Integer> getBookedSlotIdsByClinicAndDate(int clinicId, LocalDate date) throws SQLException {
        String sql = """
        SELECT DISTINCT a.timeslot_id
        FROM Appointments a
        INNER JOIN TimeSlots t ON a.timeslot_id = t.id
        WHERE a.clinic_id = ?
          AND t.date = ?
          AND a.status = 'Booked'
        """;  // ← 'Booked' بالحروف الكبيرة

        List<Integer> ids = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clinicId);
            ps.setDate(2, java.sql.Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("timeslot_id"));
                }
            }
        }
        return ids;
    }

    // هل توجد حجوزات مستقبلية في فترة زمنية محددة بيوم معين؟
    public boolean hasUpcomingAppointmentsInRule(int clinicId, DayOfWeek day, LocalTime from, LocalTime to) throws SQLException {
        String sql = """
        SELECT 1 FROM Appointments a
        JOIN TimeSlots ts ON a.time_slot_id = ts.id
        WHERE a.clinic_id = ?
          AND DATE(ts.appointment_time) >= CURDATE()
          AND DAYOFWEEK(ts.appointment_time) = ?
          AND TIME(ts.appointment_time) >= ?
          AND TIME(ts.appointment_time) < ?
          AND a.status NOT IN ('Cancelled_by_Patient', 'Cancelled_by_Doctor', 'Completed')
        LIMIT 1
        """;

        // تحويل DayOfWeek (Mon=1 → Sun=7) إلى DAYOFWEEK (Sun=1 → Sat=7)
        int sqlDay = (day.getValue() % 7) + 1; // Mon(1) → 2, Sun(7) → 1

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clinicId);
            ps.setInt(2, sqlDay);
            ps.setTime(3, Time.valueOf(from));
            ps.setTime(4, Time.valueOf(to));

            return ps.executeQuery().next(); // true إذا وُجدت حجوزات
        }
    }
    // ✅ جديدة: جلب كل المواعيد (غير الملغاة) لعيادة محددة في يوم محدد
    public List<Appointment> getAppointmentsByClinicIdAndDate(int clinicId, LocalDate date) throws SQLException {
        String sql = """
        SELECT * FROM Appointments a
        INNER JOIN TimeSlots t ON a.timeslot_id = t.id
        WHERE a.clinic_id = ?
          AND t.date = ?
          AND a.status NOT IN ('Cancelled_by_Patient', 'Cancelled_by_Doctor')
        """;

        List<Appointment> appointments = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clinicId);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    appointments.add(extractAppointmentFromResultSet(rs));
                }
            }
        }
        return appointments;
    }
    public void updateStatus(int appointmentId, Status status) throws SQLException {
        String sql = "UPDATE Appointments SET status = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.toDatabaseValue());
            ps.setInt(2, appointmentId);
            ps.executeUpdate();
        }
    }

    /**
     * جلب آخر كشف (VISIT) مكتمل للمريض مع دكتور معين
     */
    public Appointment getLastCompletedVisit(int patientId, int doctorId) throws SQLException {
        String sql = """
        SELECT a.*, ts.id AS timeslot_id, ts.clinic_id AS ts_clinic_id, ts.date, ts.day, ts.start_time, ts.end_time, ts.is_booked, ts.is_cancelled
        FROM Appointments a
        JOIN TimeSlots ts ON a.timeslot_id = ts.id
        JOIN Clinics c ON a.clinic_id = c.id
        WHERE a.patient_id = ? 
          AND c.doctor_id = ?
          AND a.status = 'COMPLETED'
          AND a.appointment_type = 'VISIT'
        ORDER BY ts.date DESC, ts.start_time DESC
        LIMIT 1
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // ★★ إنشاء الـ TimeSlot بنفس الـ constructor اللي في TimeSlotDAO ★★
                TimeSlot timeSlot = new TimeSlot(
                        rs.getInt("timeslot_id"),
                        rs.getInt("ts_clinic_id"),
                        rs.getDate("date").toLocalDate(),
                        DayOfWeek.valueOf(rs.getString("day")),
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime(),
                        rs.getBoolean("is_booked"),
                        rs.getBoolean("is_cancelled")
                );

                Appointment appointment = new Appointment(
                        rs.getInt("id"),
                        patientDAO.getById(rs.getInt("patient_id")),
                        clinicDAO.getById(rs.getInt("clinic_id")),
                        timeSlot,
                        Status.valueOf(rs.getString("status"))
                );

                // تعيين النوع و expiry
                String typeStr = rs.getString("appointment_type");
                appointment.setAppointmentType(
                        "CONSULTATION".equals(typeStr) ?
                                Appointment.AppointmentType.CONSULTATION : Appointment.AppointmentType.VISIT
                );

                Date expiryDate = rs.getDate("consultation_expiry_date");
                if (expiryDate != null) {
                    appointment.setConsultationExpiryDate(expiryDate.toLocalDate());
                }

                return appointment;
            }
            return null;
        }
    }
    public List<Appointment> getAllAppointmentsForReport(int clinicId) throws SQLException {
        String sql = "SELECT * FROM Appointments WHERE clinic_id = ?";
        List<Appointment> appointments = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clinicId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    appointments.add(extractAppointmentFromResultSet(rs));
                }
            }
        }
        return appointments;
    }
}
