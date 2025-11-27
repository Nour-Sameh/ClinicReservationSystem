/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import database.DBConnection;
import model.Chat;
import model.Patient;
import model.Practitioner;

import java.sql.*;
import java.util.*;

public class ChatDAO implements GenericDAO<Chat> {

    private final PatientDAO patientDAO = new PatientDAO();
    private final PractitionerDAO practitionerDAO = new PractitionerDAO();

    private Chat extractChatFromResultSet(ResultSet rs) throws SQLException {
        int patientId = rs.getInt("patient_id");
        int practitionerId = rs.getInt("practitioner_id");
        
        Patient patient = patientDAO.getById(patientId);
        Practitioner practitioner = practitionerDAO.getById(practitionerId);

        return new Chat(
                rs.getInt("id"),
                patient,
                practitioner
        );
    }
    
    public Chat getChatByParticipants(int patientId, int practitionerId) throws SQLException {
        String sql = "SELECT * FROM Chats WHERE (patient_id = ? AND practitioner_id = ?) LIMIT 1";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, patientId);
            ps.setInt(2, practitionerId);
            rs = ps.executeQuery();

            if (rs.next()) {
                return extractChatFromResultSet(rs);
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }
    
    public List<Chat> getChatsByPatientId(int patientId) throws SQLException {
        List<Chat> chats = new ArrayList<>();
        String sql = "SELECT * FROM Chats WHERE patient_id = ? ORDER BY timestamp ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    chats.add(extractChatFromResultSet(rs));
                }
            }
        }
        return chats;
    }

    public List<Chat> getChatsByPractitionerId(int practitionerId) throws SQLException {
        List<Chat> chats = new ArrayList<>();
        String sql = "SELECT * FROM Chats WHERE practitioner_id = ? ORDER BY timestamp ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, practitionerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    chats.add(extractChatFromResultSet(rs));
                }
            }
        }
        return chats;
    }

    // Insert
    @Override
    public void add(Chat chat) throws SQLException {
        String sql = "INSERT INTO Chats (patient_id, practitioner_id) VALUES (?, ?)";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setInt(1, chat.getPatient().getID()); 
            ps.setInt(2, chat.getPractitioner().getID()); 

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = null;
                try {
                    rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        chat.setId(rs.getInt(1));
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
    public Chat getById(int id) throws SQLException {
        String sql = "SELECT * FROM Chats WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return extractChatFromResultSet(rs); 
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
    public List<Chat> getAll() throws SQLException { // احنا مش عايزن دي في حاجه 
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    //Update
    @Override
    public void update(Chat chat) throws SQLException { // مش محتاجه دا بردو 
        throw new UnsupportedOperationException("Chat entity is usually not updated, only messages are added.");
    }

    //  Delete
    @Override
    public void delete(int id) throws SQLException { // ولا دي 
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}