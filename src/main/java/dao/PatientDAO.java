/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;
import database.DBConnection;
import model.Patient;

import java.sql.*;
import java.sql.Date;
import java.util.*;

import static database.DBConnection.getConnection;

public class PatientDAO implements GenericDAO<Patient> {
    
    private Patient extractPatientFromResultSet(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("gender"),
                rs.getDate("date_of_birth").toLocalDate()
        );
    }

    // Insert
    @Override
    public void add(Patient patient) throws SQLException {
        String sql = "INSERT INTO Patients (name, phone, email, password, gender, date_of_birth) VALUES (?, ?, ?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = getConnection();
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, patient.getName());
            ps.setString(2,patient.getPhone() );
            ps.setString(3,patient.getEmail() );
            ps.setString(4, patient.getPassword());
            ps.setString(5, patient.getGender());
            ps.setDate(6, Date.valueOf(patient.getDateOfBirth()));

            int rowsAffected = ps.executeUpdate(); // return the generative id
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        patient.setId(rs.getInt(1));
                    }
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
            con = getConnection();
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
            con = getConnection();
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

    @Override
    public void update(Patient patient) throws SQLException {
        String sql = """
        UPDATE Patients 
        SET name = ?, phone = ?, email = ?, password = ?, gender = ?, date_of_birth = ?
        WHERE id = ?
        """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, patient.getName());
            ps.setString(2, patient.getPhone());
            ps.setString(3, patient.getEmail());
            ps.setString(4, patient.getPassword());
            ps.setString(5, patient.getGender());

            if (patient.getDateOfBirth() != null) {
                ps.setDate(6, java.sql.Date.valueOf(patient.getDateOfBirth()));
            } else {
                ps.setNull(6, Types.DATE);
            }

            ps.setInt(7, patient.getID());

            ps.executeUpdate();
        }
    }

    // Delete
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Patients WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }

    }



    public List<Patient> getPatientsWithChatsForPractitioner(int practitionerId) throws SQLException {
        String sql = """
        SELECT DISTINCT p.*
        FROM Patients p
        INNER JOIN Chats c ON p.id = c.patient_id
        WHERE c.practitioner_id = ?
        """;

        List<Patient> patients = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, practitionerId);
            rs = ps.executeQuery();

            while (rs.next()) {
                patients.add(extractPatientFromResultSet(rs));
            }
            return patients;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }
    // PatientDAO.java
    public boolean isNameTaken(String name, int excludePatientId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Patients WHERE name = ? AND id != ?";

        // ✅ try-with-resources: يفتح connection ويقفله تلقائيًا
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name.trim());
            stmt.setInt(2, excludePatientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
