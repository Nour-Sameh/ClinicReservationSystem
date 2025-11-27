
package dao;
import database.DBConnection;
import model.Schedule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author noursameh
 */
public class ScheduleDAO implements GenericDAO<Schedule> {
    private final WorkingHoursRuleDAO workingHoursRuleDAO= new WorkingHoursRuleDAO();
    private Schedule extractScheduleFromResultSet(ResultSet rs) throws SQLException {
        return new Schedule(
                rs.getInt("id"),
                rs.getInt("slot_duration_in_minutes")
        );
    }

    // Insert 
    @Override
    public void add(Schedule schedule) throws SQLException {
        String sql = "INSERT INTO Schedules (slot_duration_in_minutes) VALUES (?)";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, schedule.getSlotDurationInMinutes());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = null;
                try {
                    rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        schedule.setID(rs.getInt(1));
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
    public Schedule getById(int id) throws SQLException {
        String sql = "SELECT * FROM Schedules WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                Schedule r = extractScheduleFromResultSet(rs);
                r.setWeeklyRules(workingHoursRuleDAO.getRulesByScheduleId(id));
                return extractScheduleFromResultSet(rs);
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
    public List<Schedule> getAll() throws SQLException {
        String sql = "SELECT * FROM Schedules";
        List<Schedule> schedules = new ArrayList<>();
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            st = con.createStatement();
            rs = st.executeQuery(sql);

            while (rs.next()) {
                schedules.add(extractScheduleFromResultSet(rs));
            }
            return schedules;
        } finally {
            if (rs != null) rs.close();
            if (st != null) st.close();
            DBConnection.closeConnection(con);
        }
    }

    // Update
    @Override
    public void update(Schedule schedule) throws SQLException {
        String sql = "UPDATE Schedules SET slot_duration_in_minutes = ? WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, schedule.getSlotDurationInMinutes());
            ps.setInt(2, schedule.getID());
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }

    //  Delete
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Schedules WHERE id = ?";
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