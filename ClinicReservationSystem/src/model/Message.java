/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.*;
import java.util.*;
/**
 *
 * @author noursameh
 */


// Represents a single message between a patient and a practitioner
public class Message {
    private int id;
    private int senderId; // Patient ID or Practitioner ID
    private int receiverId; // Patient ID or Practitioner ID
    private String messageText;
    private LocalDateTime timestamp;

    public Message(int id, int senderId, int receiverId, String messageText) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.timestamp = LocalDateTime.now();
    }

    public int getId() { return id; }
    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }
    public String getMessageText() { return messageText; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setMessageText(String messageText) { this.messageText = messageText; }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", messageText='" + messageText + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

}

