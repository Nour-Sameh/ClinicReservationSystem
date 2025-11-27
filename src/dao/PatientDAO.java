/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;
import database.DBConnection;
import model.Patient;

import java.sql.*;
import java.util.*;
/**
 *
 * @author noursameh
 */
public class PatientDAO implements GenericDAO<Patient> {
    
    private Patient extractPatientFromResultSet(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("password")
        );
    }

    // Insert
    @Override
    public void add(Patient patient) throws SQLException {
        String sql = "INSERT INTO Patients (name, phone, email, password) VALUES (?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, patient.getName());
            ps.setString(2, patient.getPhone());
            ps.setString(3, patient.getEmail());
            ps.setString(4, patient.getPassword());

            int rowsAffected = ps.executeUpdate(); // برجع من الجدول الid الي تم انشاه
            if (rowsAffected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    patient.setId(rs.getInt(1));
                }
            }
        } finally {
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }

    // ٌRead
    @Override
    public Patient getById(int id) throws SQLException {
        String sql = "SELECT * FROM Patients WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return extractPatientFromResultSet(rs);
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }

    // Read All
    @Override
    public List<Patient> getAll() throws SQLException {
        String sql = "SELECT * FROM Patients";
        List<Patient> patients = new ArrayList<>();
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            st = con.createStatement();
            rs = st.executeQuery(sql);

            while (rs.next()) {
                patients.add(extractPatientFromResultSet(rs));
            }
            return patients;
        } finally {
            if (rs != null) rs.close();
            if (st != null) st.close();
            DBConnection.closeConnection(con);
        }
    }

    // Update
    @Override
    public void update(Patient patient) throws SQLException {
        String sql = "UPDATE Patients SET name = ?, phone = ?, email = ?, password = ? WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, patient.getName());
            ps.setString(2, patient.getPhone());
            ps.setString(3, patient.getEmail());
            ps.setString(4, patient.getPassword());
            ps.setInt(5, patient.getID());

            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }

    // Delete
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Patients WHERE id = ?";
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
