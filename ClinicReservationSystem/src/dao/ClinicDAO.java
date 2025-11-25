/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;
import database.DBConnection;
import model.Clinic;
import model.Schedule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author noursameh
 */
public class ClinicDAO implements GenericDAO<Clinic> {

    private final ScheduleDAO scheduleDAO = new ScheduleDAO(); 

    private Clinic extractClinicFromResultSet(ResultSet rs) throws SQLException {
        int scheduleId = rs.getInt("schedule_id");
        
        Schedule schedule = null;
        if (!rs.wasNull()) {
            try {
                schedule = scheduleDAO.getById(scheduleId); 
            } catch (SQLException e) {
                System.err.println("Failed to load associated Schedule for Clinic ID " + rs.getInt("id") + ": " + e.getMessage());
            }
        }
        
        return new Clinic(
                rs.getInt("id"),
                rs.getInt("department_id"),
                rs.getString("name"),
                rs.getString("address"),
                rs.getDouble("price"),
                schedule 
        );
    }

    // Insert
    @Override
    public void add(Clinic clinic) throws SQLException {
        String sql = "INSERT INTO Clinics (department_id, name, address, price, schedule_id) VALUES (?, ?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, clinic.getDepartmentID());
            ps.setString(2, clinic.getName());
            ps.setString(3, clinic.getAddress());
            ps.setDouble(4, clinic.getPrice());

            if (clinic.getSchedule() == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, clinic.getSchedule().getID()); 
            }

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = null;
                try {
                    rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        clinic.setID(rs.getInt(1));
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
    public Clinic getById(int id) throws SQLException {
        String sql = "SELECT * FROM Clinics WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return extractClinicFromResultSet(rs);
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }

    //  Get All
    @Override
    public List<Clinic> getAll() throws SQLException {
        String sql = "SELECT * FROM Clinics";
        List<Clinic> clinics = new ArrayList<>();
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            st = con.createStatement();
            rs = st.executeQuery(sql);

            while (rs.next()) {
                clinics.add(extractClinicFromResultSet(rs));
            }
            return clinics;
        } finally {
            if (rs != null) rs.close();
            if (st != null) st.close();
            DBConnection.closeConnection(con);
        }
    }

    // Update
    @Override
    public void update(Clinic clinic) throws SQLException {
        String sql = "UPDATE Clinics SET department_id = ?, name = ?, address = ?, price = ?, schedule_id = ? WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);

            ps.setInt(1, clinic.getDepartmentID());
            ps.setString(2, clinic.getName());
            ps.setString(3, clinic.getAddress());
            ps.setDouble(4, clinic.getPrice());

            if (clinic.getSchedule() == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, clinic.getSchedule().getID());
            }

            ps.setInt(6, clinic.getID());
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }

    // Delete
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Clinics WHERE id = ?";
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
