/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import database.DBConnection;
import model.Rating;
import model.Patient;
import model.Clinic;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author noursameh
 */
public class RatingDAO implements GenericDAO<Rating> {

    private final PatientDAO patientDAO = new PatientDAO();
    private final ClinicDAO clinicDAO = new ClinicDAO();

    private Rating extractRatingFromResultSet(ResultSet rs) throws SQLException {
        int patientId = rs.getInt("patient_id");
        int clinicId = rs.getInt("clinic_id");
        
        Patient patient = patientDAO.getById(patientId);
        Clinic clinic = clinicDAO.getById(clinicId);
        
        return new Rating(
                rs.getInt("id"),
                patient,
                clinic,
                rs.getInt("score"),
                rs.getString("comment")
        );
    }

    // Insert
    @Override
    public void add(Rating rating) throws SQLException {
        String sql = "INSERT INTO Ratings (patient_id, clinic_id, score, comment) VALUES (?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setInt(1, rating.getPatient().getID()); 
            ps.setInt(2, rating.getClinic().getID());
            ps.setInt(3, rating.getScore());
            ps.setString(4, rating.getComment()); 

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = null;
                try {
                    rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        rating.setId(rs.getInt(1));
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
    public Rating getById(int id) throws SQLException {
        String sql = "SELECT * FROM Ratings WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return extractRatingFromResultSet(rs);
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
    public List<Rating> getAll() throws SQLException {
        String sql = "SELECT * FROM Ratings";
        List<Rating> ratings = new ArrayList<>();
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            st = con.createStatement();
            rs = st.executeQuery(sql);

            while (rs.next()) {
                ratings.add(extractRatingFromResultSet(rs));
            }
            return ratings;
        } finally {
            if (rs != null) rs.close();
            if (st != null) st.close();
            DBConnection.closeConnection(con);
        }
    }

    // Update
    @Override
    public void update(Rating rating) throws SQLException {
        String sql = "UPDATE Ratings SET patient_id = ?, clinic_id = ?, score = ?, comment = ? WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);

            ps.setInt(1, rating.getPatient().getID());
            ps.setInt(2, rating.getClinic().getID());
            ps.setInt(3, rating.getScore());
            ps.setString(4, rating.getComment()); 
            ps.setInt(5, rating.getId());

            ps.executeUpdate();
            
        } finally {
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }

    // ️ Delete
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Ratings WHERE id = ?";
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
    public List<Rating> getRatingsByClinicId(int clinicId) throws SQLException {
        String sql = "SELECT * FROM Ratings WHERE clinic_id = ?";
        List<model.Rating> ratings = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, clinicId);
            rs = ps.executeQuery();

            while (rs.next()) {
                ratings.add(extractRatingFromResultSet(rs)); 
            }
            return ratings;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }
}
