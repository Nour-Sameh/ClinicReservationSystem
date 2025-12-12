package dao;

import database.DBConnection;
import model.WorkingHoursRule;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class WorkingHoursRuleDAO {

    private WorkingHoursRule extractRule(ResultSet rs) throws SQLException {
        return new WorkingHoursRule(
                rs.getInt("schedule_id"),
                DayOfWeek.valueOf(rs.getString("day")),
                rs.getTime("start_time").toLocalTime(),
                rs.getTime("end_time").toLocalTime()
        );
    }

    public void insertRule(int scheduleId, DayOfWeek day, LocalTime start, LocalTime end) throws SQLException {
        String sql = "INSERT INTO WorkingHoursRules (schedule_id, day, start_time, end_time) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, scheduleId);
            ps.setString(2, day.name());
            ps.setTime(3, Time.valueOf(start));
            ps.setTime(4, Time.valueOf(end));
            ps.executeUpdate();
        }
    }

    public WorkingHoursRule getById(int id) throws SQLException {
        String sql = "SELECT * FROM WorkingHoursRules WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return extractRule(rs);
            }
        }
        return null;
    }

    public List<WorkingHoursRule> getAll() throws SQLException {
        String sql = "SELECT * FROM WorkingHoursRules";
        List<WorkingHoursRule> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractRule(rs));
            }
        }
        return list;
    }

    public List<WorkingHoursRule> getByScheduleId(int scheduleId) throws SQLException {
        String sql = "SELECT * FROM WorkingHoursRules WHERE schedule_id = ?";
        List<WorkingHoursRule> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, scheduleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extractRule(rs));
            }
        }
        return list;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM WorkingHoursRules WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void deleteByScheduleId(int scheduleId) throws SQLException {
        String sql = "DELETE FROM WorkingHoursRules WHERE schedule_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, scheduleId);
            ps.executeUpdate();
        }
    }
}
