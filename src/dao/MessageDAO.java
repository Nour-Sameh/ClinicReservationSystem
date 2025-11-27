/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;
import database.DBConnection;
import model.Message;
import model.SenderType; 

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO implements GenericDAO<Message> {

    private Message extractMessageFromResultSet(ResultSet rs) throws SQLException {
        SenderType senderType = SenderType.valueOf(rs.getString("sender_type"));
        LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();

        return new Message(
                rs.getInt("id"),
                rs.getInt("chat_id"),
                senderType,
                rs.getInt("sender_id"),
                rs.getInt("receiver_id"),
                rs.getString("message_text"),
                timestamp
        );
    }
    
    // Get Messages By ChatId
    public List<Message> getMessagesByChatId(int chatId) throws SQLException {
        String sql = "SELECT * FROM Messages WHERE chat_id = ? ORDER BY timestamp ASC";
        List<Message> messages = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, chatId);
            rs = ps.executeQuery();

            while (rs.next()) {
                messages.add(extractMessageFromResultSet(rs));
            }
            return messages;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }


    // Insert
    @Override
    public void add(Message message) throws SQLException {
        String sql = "INSERT INTO Messages (chat_id, sender_type, sender_id, receiver_id, message_text, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setInt(1, message.getChatId());
            ps.setString(2, message.getSenderType().name()); 
            ps.setInt(3, message.getSenderId());
            ps.setInt(4, message.getReceiverId());
            ps.setString(5, message.getMessageText());
            ps.setTimestamp(6, Timestamp.valueOf(message.getTimestamp()));

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = null;
                try {
                    rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        message.setId(rs.getInt(1));
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

    // Get
        
    @Override
    public Message getById(int id) throws SQLException { // مش محتاجه اجيب رساله معينه في حاجه 
         throw new UnsupportedOperationException("Not implemented yet.");
    }
    
    @Override
    public List<Message> getAll() throws SQLException { // مش محتاجه كل المسدجات الي في الداتا بيز في حاجه 
         throw new UnsupportedOperationException("Not implemented yet.");
    }
    
    // Update
    @Override
    public void update(Message message) throws SQLException {
        String sql = "UPDATE Messages SET message_text = ? WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);

            ps.setString(1, message.getMessageText());
            ps.setInt(2, message.getId());

            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }

    // Delete
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Messages WHERE id = ?";
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
