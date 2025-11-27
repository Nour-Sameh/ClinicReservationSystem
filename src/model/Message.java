/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.*;
/**
 *
 * @author noursameh
 */


// Represents a single message between a patient and a practitioner
public class Message {
    private int id;
    private int chatId;
    private SenderType senderType;
    private int senderId;
    private int receiverId; 
    private String messageText;
    private LocalDateTime timestamp;

    public Message(int id, int chatId, SenderType senderType, int senderId, int receiverId, String messageText, LocalDateTime timestamp) {
        this.id = id;
        this.chatId = chatId;
        this.senderType = senderType;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    public Message(int chatId, SenderType senderType, int senderId, int receiverId, String messageText) {
        this(0, chatId, senderType, senderId, receiverId, messageText, LocalDateTime.now());
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getChatId() { return chatId; }
    public SenderType getSenderType() { return senderType; }
    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }
    public String getMessageText() { return messageText; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setMessageText(String messageText) { this.messageText = messageText; }
    public void setChatId(int chatId) {
        this.chatId = chatId;
    }
    

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", senderType=" + senderType +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", messageText='" + messageText + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

