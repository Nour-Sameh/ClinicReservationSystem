/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;
import database.DBConnection;
import model.Practitioner;
import model.Clinic;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author noursameh
 */
public class PractitionerDAO implements GenericDAO<Practitioner> {

    private final ClinicDAO clinicDAO = new ClinicDAO();

    private Practitioner extractPractitionerFromResultSet(ResultSet rs) throws SQLException {
        int clinicId = rs.getInt("clinic_id");
        Clinic clinic = null;

        if (!rs.wasNull()) {
            try {
                clinic = clinicDAO.getById(clinicId);
            } catch (SQLException e) {
                System.err.println("Error loading Clinic (ID: " + clinicId + ") for Practitioner: " + e.getMessage());
            }
        }
        
        return new Practitioner(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("password"),
                clinic 
        );
    }

    // Insert
    @Override
    public void add(Practitioner practitioner) throws SQLException {
        String sql = "INSERT INTO Practitioners (name, phone, email, password, clinic_id) VALUES (?, ?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setString(1, practitioner.getName());
            ps.setString(2, practitioner.getPhone());
            ps.setString(3, practitioner.getEmail());
            ps.setString(4, practitioner.getPassword());
            
            if (practitioner.getClinic() == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, practitioner.getClinic().getID());
            }

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = null;
                try {
                    rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        practitioner.setId(rs.getInt(1));
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
    public Practitioner getById(int id) throws SQLException {
        String sql = "SELECT * FROM Practitioners WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return extractPractitionerFromResultSet(rs);
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
    public List<Practitioner> getAll() throws SQLException {
        String sql = "SELECT * FROM Practitioners";
        List<Practitioner> practitioners = new ArrayList<>();
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            st = con.createStatement();
            rs = st.executeQuery(sql);

            while (rs.next()) {
                practitioners.add(extractPractitionerFromResultSet(rs));
            }
            return practitioners;
        } finally {
            if (rs != null) rs.close();
            if (st != null) st.close();
            DBConnection.closeConnection(con);
        }
    }

    // Update
    @Override
    public void update(Practitioner practitioner) throws SQLException {
        String sql = "UPDATE Practitioners SET name = ?, phone = ?, email = ?, password = ?, clinic_id = ? WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);

            ps.setString(1, practitioner.getName());
            ps.setString(2, practitioner.getPhone());
            ps.setString(3, practitioner.getEmail());
            ps.setString(4, practitioner.getPassword());
            
            if (practitioner.getClinic() == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, practitioner.getClinic().getID());
            }

            ps.setInt(6, practitioner.getID());
            ps.executeUpdate();
            
        } finally {
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }

    // Delete
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Practitioners WHERE id = ?";
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