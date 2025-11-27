
package service;

import dao.ChatDAO;
import dao.MessageDAO;
import java.sql.SQLException;
import java.util.*;
import model.Chat;
import model.*;

/**
 *
 * @author noursameh
 */
public class ChatService {
    
    private final ChatDAO chatDAO = new ChatDAO();
    private final MessageDAO messageDAO = new MessageDAO();

    public void addChat(Chat chat) throws SQLException {
        chatDAO.add(chat);
    }
    
    public void sendMessage(Chat chat, Message message) throws SQLException {
        message.setChatId(chat.getId());
        messageDAO.add(message);
    }

   public List<Message> getChatHistory(Chat chat) throws SQLException { 
       return messageDAO.getMessagesByChatId(chat.getId()); 
   }

    public List<Chat> getChatsForPatient(Patient patient) {
        try {
            return chatDAO.getChatsByPatientId(patient.getID());
        } catch (SQLException e) {
            System.err.println("Error fetching chats: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Chat>getChatsForPractitioner(Practitioner practitioner) {
        try {
            return chatDAO.getChatsByPractitionerId(practitioner.getID());
        } catch (SQLException e) {
            System.err.println("Error fetching chats: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public Chat getFullChat(Chat chat) {
        try {
            List<Message> messages = getChatHistory(chat);
            chat.setMessages(messages);////
            return chat;
        } catch (SQLException e) {
            System.err.println("Error fetching chat messages: " + e.getMessage());
            chat.setMessages(new ArrayList<>()); // fallback
            return chat;
        }
    }

}