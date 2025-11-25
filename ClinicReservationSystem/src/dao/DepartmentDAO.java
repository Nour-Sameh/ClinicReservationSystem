
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import database.DBConnection;
import model.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author noursameh
 */
public class DepartmentDAO implements GenericDAO<Department> {

    private Department extractDepartmentFromResultSet(ResultSet rs) throws SQLException {
        return new Department(
                rs.getInt("id"),
                rs.getString("name")
        );
    }

    // Insert
    @Override
    public void add(Department department) throws SQLException {
        String sql = "INSERT INTO Departments (name) VALUES (?)";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, department.getName());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    department.setID(rs.getInt(1));
                }
            }
        } finally {
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }

    // Get by ID
    @Override
    public Department getById(int id) throws SQLException {
        String sql = "SELECT * FROM Departments WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return extractDepartmentFromResultSet(rs);
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
    public List<Department> getAll() throws SQLException {
        String sql = "SELECT * FROM Departments";
        List<Department> departments = new ArrayList<>();
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            st = con.createStatement();
            rs = st.executeQuery(sql);

            while (rs.next()) {
                departments.add(extractDepartmentFromResultSet(rs));
            }
            return departments;
        } finally {
            if (rs != null) rs.close();
            if (st != null) st.close();
            DBConnection.closeConnection(con);
        }
    }

    // Update
    @Override
    public void update(Department department) throws SQLException {
        String sql = "UPDATE Departments SET name = ? WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, department.getName());
            ps.setInt(2, department.getID());
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }

    // Delete
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Departments WHERE id = ?";
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