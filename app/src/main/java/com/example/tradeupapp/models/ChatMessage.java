package com.example.tradeupapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a chat message in the TradeUpApp.
 * This represents individual messages in the subcollection chats/{chatId}/messages
 */
public class ChatMessage implements Serializable {
    @DocumentId
    private String id;
    private String chatId;
    private String senderId;
    private String message; // text content
    private String messageType; // "text" or "image"
    private List<String> attachments; // image URLs
    private Timestamp timestamp;
    private boolean isRead;

    // Message type constants for UI rendering
    public static final String TYPE_SENT = "sent";
    public static final String TYPE_RECEIVED = "received";

    /**
     * Default constructor required for Firebase Firestore
     */
    public ChatMessage() {
        // Required empty constructor for Firestore
        this.attachments = new ArrayList<>();
        this.timestamp = Timestamp.now();
        this.isRead = false;
    }

    /**
     * Constructor for text and image message
     */
    public ChatMessage(String chatId, String senderId, String message, String messageType, List<String> attachments) {
        this.chatId = chatId;
        this.senderId = senderId;
        this.message = message;
        this.messageType = messageType;
        this.attachments = attachments != null ? attachments : new ArrayList<>();
        this.timestamp = Timestamp.now();
        this.isRead = false;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
