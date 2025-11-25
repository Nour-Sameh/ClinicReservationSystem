/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;
import database.DBConnection;
import model.WaitingList;
import model.Patient;
import model.Clinic;
import model.Status;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author noursameh
 */
public class WaitingListDAO implements GenericDAO<WaitingList> {

    private final PatientDAO patientDAO = new PatientDAO();
    private final ClinicDAO clinicDAO = new ClinicDAO();

    private WaitingList extractWaitingListFromResultSet(ResultSet rs) throws SQLException {
        int patientId = rs.getInt("patient_id");
        int clinicId = rs.getInt("clinic_id");
        
        Patient patient = patientDAO.getById(patientId);
        Clinic clinic = clinicDAO.getById(clinicId);
        
        LocalDateTime requestTime = rs.getTimestamp("request_time").toLocalDateTime();
        
        Status status = Status.valueOf(rs.getString("status"));

        return new WaitingList(
                rs.getInt("id"),
                patient,
                clinic,
                requestTime,
                status
        );
    }
    // هستخدمها في السيرفس
    public WaitingList getNextPendingRequestForClinic(int clinicId) throws SQLException {
        String sql = "SELECT * FROM WaitingList WHERE clinic_id = ? AND status = 'PENDING' ORDER BY request_time ASC LIMIT 1";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, clinicId);
            rs = ps.executeQuery();

            if (rs.next()) {
                return extractWaitingListFromResultSet(rs);
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }


    // Insert
    @Override
    public void add(WaitingList waitingList) throws SQLException {
        String sql = "INSERT INTO WaitingList (patient_id, clinic_id, request_time, status) VALUES (?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setInt(1, waitingList.getPatient().getID()); 
            ps.setInt(2, waitingList.getClinic().getID());
            ps.setTimestamp(3, Timestamp.valueOf(waitingList.getRequestTime())); 
            ps.setString(4, waitingList.getStatus().name()); 

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = null;
                try {
                    rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        waitingList.setId(rs.getInt(1));
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
    public WaitingList getById(int id) throws SQLException {
        String sql = "SELECT * FROM WaitingList WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return extractWaitingListFromResultSet(rs);
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
    public List<WaitingList> getAll() throws SQLException {
        String sql = "SELECT * FROM WaitingList ORDER BY request_time ASC"; 
        List<WaitingList> waitingLists = new ArrayList<>();
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            st = con.createStatement();
            rs = st.executeQuery(sql);

            while (rs.next()) {
                waitingLists.add(extractWaitingListFromResultSet(rs));
            }
            return waitingLists;
        } finally {
            if (rs != null) rs.close();
            if (st != null) st.close();
            DBConnection.closeConnection(con);
        }
    }

    // Update
    @Override
    public void update(WaitingList waitingList) throws SQLException {
        String sql = "UPDATE WaitingList SET patient_id = ?, clinic_id = ?, request_time = ?, status = ? WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);

            ps.setInt(1, waitingList.getPatient().getID());
            ps.setInt(2, waitingList.getClinic().getID());
            ps.setTimestamp(3, Timestamp.valueOf(waitingList.getRequestTime())); 
            ps.setString(4, waitingList.getStatus().name()); 
            ps.setInt(5, waitingList.getId());

            ps.executeUpdate();
            
        } finally {
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }

    // ️ Delete
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM WaitingList WHERE id = ?";
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
}