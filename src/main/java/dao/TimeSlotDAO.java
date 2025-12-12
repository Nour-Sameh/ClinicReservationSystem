package dao;

import database.DBConnection;
import model.TimeSlot;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


public class TimeSlotDAO implements GenericDAO<TimeSlot> {

    private TimeSlot extractTimeSlotFromResultSet(ResultSet rs) throws SQLException {

        LocalDate localDate = rs.getDate("date").toLocalDate();
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(rs.getString("day"));
        LocalTime localStartTime = rs.getTime("start_time").toLocalTime();
        LocalTime localEndTime = rs.getTime("end_time").toLocalTime();

        return new TimeSlot(
                rs.getInt("id"),
                rs.getInt("clinic_id"),
                localDate,
                dayOfWeek,
                localStartTime,
                localEndTime,
                rs.getBoolean("is_booked"),
                rs.getBoolean("is_cancelled")
        );
    }

    // Insert

    @Override
    public void add(TimeSlot timeSlot) throws SQLException {
        String sql = "INSERT INTO TimeSlots (clinic_id, date, day, start_time, end_time, is_booked, is_cancelled) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, timeSlot.getClinicId());
            ps.setDate(2, java.sql.Date.valueOf(timeSlot.getDate()));
            ps.setString(3, timeSlot.getDay().name());
            ps.setTime(4, java.sql.Time.valueOf(timeSlot.getStartTime()));
            ps.setTime(5, java.sql.Time.valueOf(timeSlot.getEndTime()));
            ps.setBoolean(6, timeSlot.isBooked());
            ps.setBoolean(7, timeSlot.isCancelled());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        timeSlot.setId(generatedId); // ✅ حفظ الـ ID في الـ object
                        return; // ✅ خروج ناجح — بدون Exception
                    }
                }
            }
            // فقط لو مفيش rows أو مفيش ID → نلقى Exception
            throw new SQLException("Failed to insert TimeSlot: no rows affected or ID not generated.");
        } finally {
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }

    public List<TimeSlot> getSlotsByClinic(int clinicId) throws SQLException {
        List<TimeSlot> slots = new ArrayList<>();
        String sql = "SELECT * FROM TimeSlots WHERE clinic_id=?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, clinicId);
            rs = ps.executeQuery();

            while (rs.next()) {
                TimeSlot slot = new TimeSlot(
                        rs.getInt("id"),
                        rs.getInt("clinic_id"),
                        rs.getDate("date").toLocalDate(),
                        DayOfWeek.valueOf(rs.getString("day")),
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime(),
                        rs.getBoolean("is_booked"),
                        rs.getBoolean("is_cancelled")
                );
                slots.add(slot);
            }
            return slots;

        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }


    // Get by ID
    @Override
    public TimeSlot getById(int id) throws SQLException {
        String sql = "SELECT * FROM TimeSlots WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return extractTimeSlotFromResultSet(rs);
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
    public List<TimeSlot> getAll() throws SQLException {
        String sql = "SELECT * FROM TimeSlots";
        List<TimeSlot> timeSlots = new ArrayList<>();
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            st = con.createStatement();
            rs = st.executeQuery(sql);

            while (rs.next()) {
                timeSlots.add(extractTimeSlotFromResultSet(rs));
            }
            return timeSlots;
        } finally {
            if (rs != null) rs.close();
            if (st != null) st.close();
            DBConnection.closeConnection(con);
        }
    }

    // Update
    @Override
    public void update(TimeSlot timeSlot) throws SQLException {
        String sql = "UPDATE TimeSlots SET clinic_id = ?, date = ?, day = ?, start_time = ?, end_time = ?, is_booked = ?, is_cancelled = ? WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);

            ps.setInt(1, timeSlot.getClinicId());
            ps.setDate(2, java.sql.Date.valueOf(timeSlot.getDate()));
            ps.setString(3, timeSlot.getDay().name());
            ps.setTime(4, java.sql.Time.valueOf(timeSlot.getStartTime()));
            ps.setTime(5, java.sql.Time.valueOf(timeSlot.getEndTime()));
            ps.setBoolean(6, timeSlot.isBooked());
            ps.setBoolean(7, timeSlot.isCancelled());

            ps.setInt(8, timeSlot.getId());

            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }

    // Delete
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM TimeSlots WHERE id = ?";
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

    public TimeSlot getByUniqueFields(int clinicId, LocalDate date, LocalTime start, LocalTime end) throws SQLException {
        String sql = "SELECT * FROM TimeSlots WHERE clinic_id = ? AND date = ? AND start_time = ? AND end_time = ? LIMIT 1";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        TimeSlot slot = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);

            ps.setInt(1, clinicId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            ps.setTime(3, java.sql.Time.valueOf(start));
            ps.setTime(4, java.sql.Time.valueOf(end));

            rs = ps.executeQuery();
            if (rs.next()) {
                slot = new TimeSlot(
                        rs.getInt("id"),
                        rs.getInt("clinic_id"),
                        rs.getDate("date").toLocalDate(),
                        DayOfWeek.valueOf(rs.getString("day")),
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime(),
                        rs.getBoolean("is_booked"),
                        rs.getBoolean("is_cancelled")
                );
            }

        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }

        return slot;
    }
    public boolean isSlotAvailable(int slotId) throws SQLException {
        String sql = "SELECT is_booked, is_cancelled FROM TimeSlots WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, slotId);
            rs = ps.executeQuery();

            if (rs.next()) {
                boolean isBooked = rs.getBoolean("is_booked");
                boolean isCancelled = rs.getBoolean("is_cancelled");
                return !isBooked && !isCancelled; // متاح لو مش محجوز ومش ملغى
            }
            return false; // مش موجود

        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }

}
