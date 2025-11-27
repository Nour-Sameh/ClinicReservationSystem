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

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author noursameh
 */
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
        
        Status status = Status.valueOf(rs.getString("status"));

        return new Appointment(
                rs.getInt("id"),
                patient,
                clinic,
                timeSlot,
                status
        );
    }

    // Insert
    @Override
    public void add(Appointment appointment) throws SQLException {
        String sql = "INSERT INTO Appointments (patient_id, clinic_id, timeslot_id, status) VALUES (?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setInt(1, appointment.getPatient().getID()); 
            ps.setInt(2, appointment.getClinic().getID());
            ps.setInt(3, appointment.getAppointmentDateTime().getId());
            // تحويل ENUM إلى String
            ps.setString(4, appointment.getStatus().name()); 

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = null;
                try {
                    rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        appointment.setId(rs.getInt(1));
                    }
                } finally {
                    if (rs != null) rs.close();
                }
            }
        } finally {
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
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
        String sql = "UPDATE Appointments SET patient_id = ?, clinic_id = ?, timeslot_id = ?, status = ? WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);

            ps.setInt(1, appointment.getPatient().getID());
            ps.setInt(2, appointment.getClinic().getID());
            ps.setInt(3, appointment.getAppointmentDateTime().getId());
            ps.setString(4, appointment.getStatus().name()); 
            ps.setInt(5, appointment.getId());

            ps.executeUpdate();
            
        } finally {
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
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

    public List<Appointment> getAppointmentList(Patient patient) throws SQLException {
        String sql = "SELECT * FROM Appointments WHERE patient_id = ?";
        List<Appointment> appointments = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, patient.getID());
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
}
