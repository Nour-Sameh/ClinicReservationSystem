/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import database.DBConnection;
import model.WorkingHoursRule;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author noursameh
 */
public class WorkingHoursRuleDAO implements GenericDAO<WorkingHoursRule> {

    private WorkingHoursRule extractRuleFromResultSet(ResultSet rs) throws SQLException {
        
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(rs.getString("day"));
        
        LocalTime startTime = rs.getTime("start_time").toLocalTime();
        LocalTime endTime = rs.getTime("end_time").toLocalTime();

        return new WorkingHoursRule(
                rs.getInt("id"),
                rs.getInt("schedule_id"),
                dayOfWeek,
                startTime,
                endTime
        );
    }

    // Insert
    @Override
    public void add(WorkingHoursRule rule) throws SQLException {
        String sql = "INSERT INTO WorkingHoursRules (schedule_id, day, start_time, end_time) VALUES (?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setInt(1, rule.getScheduleId());
            ps.setString(2, rule.getDay().name()); 
            ps.setTime(3, java.sql.Time.valueOf(rule.getStartTime())); 
            ps.setTime(4, java.sql.Time.valueOf(rule.getEndtTime())); 

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = null;
                try {
                    rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        rule.setId(rs.getInt(1));
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
    public WorkingHoursRule getById(int id) throws SQLException {
        String sql = "SELECT * FROM WorkingHoursRules WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return extractRuleFromResultSet(rs);
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
    public List<WorkingHoursRule> getAll() throws SQLException {
        String sql = "SELECT * FROM WorkingHoursRules";
        List<WorkingHoursRule> rules = new ArrayList<>();
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            st = con.createStatement();
            rs = st.executeQuery(sql);

            while (rs.next()) {
                rules.add(extractRuleFromResultSet(rs));
            }
            return rules;
        } finally {
            if (rs != null) rs.close();
            if (st != null) st.close();
            DBConnection.closeConnection(con);
        }
    }
    
    // Get Rules By ScheduleId
    public List<WorkingHoursRule> getRulesByScheduleId(int scheduleId) throws SQLException {
        String sql = "SELECT * FROM WorkingHoursRules WHERE schedule_id = ?";
        List<WorkingHoursRule> rules = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, scheduleId);
            rs = ps.executeQuery();

            while (rs.next()) {
                rules.add(extractRuleFromResultSet(rs));
            }
            return rules;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }


    // Update
    @Override
    public void update(WorkingHoursRule rule) throws SQLException {
        String sql = "UPDATE WorkingHoursRules SET schedule_id = ?, day = ?, start_time = ?, end_time = ? WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);

            ps.setInt(1, rule.getScheduleId());
            ps.setString(2, rule.getDay().name()); 
            ps.setTime(3, java.sql.Time.valueOf(rule.getStartTime())); 
            ps.setTime(4, java.sql.Time.valueOf(rule.getEndtTime())); 
            ps.setInt(5, rule.getId());

            ps.executeUpdate();
            
        } finally {
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }

    //  Delete
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM WorkingHoursRules WHERE id = ?";
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
