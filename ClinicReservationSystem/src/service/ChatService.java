/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.ChatDAO;
import dao.MessageDAO;
import java.sql.SQLException;
import java.util.List;
import model.Chat;
import model.Message;

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
        return chatDAO.getById(chat.getId()).getMessages();
    }
}
