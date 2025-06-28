package com.example.tradeupapp.models;

import java.util.Date;

public class ChatMessage {
    public static final int TYPE_SENT = 1;
    public static final int TYPE_RECEIVED = 2;

    private String id;
    private String senderId;
    private String receiverId;
    private String content;
    private String imageUrl;
    private Date timestamp;
    private int messageType;

    // Empty constructor for Firebase
    public ChatMessage() {
    }

    public ChatMessage(String senderId, String receiverId, String content, String imageUrl, int messageType) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.imageUrl = imageUrl;
        this.timestamp = new Date();
        this.messageType = messageType;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public boolean hasImage() {
        return imageUrl != null && !imageUrl.isEmpty();
    }
}
