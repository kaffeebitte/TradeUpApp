package com.example.tradeupapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model representing a chat room in the TradeUpApp.
 * Contains information about the conversation between users regarding a listing.
 */
public class ChatModel implements Serializable {
    @DocumentId
    private String id;
    private List<String> participants; // user IDs
    private String relatedItemId; // item being discussed
    private String lastMessage;
    private Timestamp lastMessageTime;
    private String lastMessageSenderId;
    private Map<String, Integer> unreadCount; // userId -> count
    private Timestamp createdAt;
    private boolean isActive;

    /**
     * Default constructor required for Firebase Firestore
     */
    public ChatModel() {
        this.participants = new ArrayList<>();
        this.unreadCount = new HashMap<>();
        this.createdAt = Timestamp.now();
        this.isActive = true;
    }

    /**
     * Constructor with essential chat information
     */
    public ChatModel(List<String> participants, String relatedItemId) {
        this.participants = participants != null ? participants : new ArrayList<>();
        this.relatedItemId = relatedItemId;
        this.unreadCount = new HashMap<>();
        this.createdAt = Timestamp.now();
        this.isActive = true;
    }

    /**
     * Returns the other participant's userId in a 2-person chat.
     * If currentUserId is not in participants, returns null.
     */
    @Exclude
    public String getOtherUserId(String currentUserId) {
        if (participants == null || participants.size() != 2 || currentUserId == null) return null;
        for (String id : participants) {
            if (!id.equals(currentUserId)) return id;
        }
        return null;
    }

    /**
     * Returns the last message preview for the chat.
     * If lastMessage is null, returns an empty string.
     */
    @Exclude
    public String getMessagePreview() {
        return lastMessage != null ? lastMessage : "";
    }

    /**
     * Returns the last updated time for the chat (lastMessageTime).
     * If lastMessageTime is null, returns createdAt.
     */
    @Exclude
    public Timestamp getLastUpdated() {
        return lastMessageTime != null ? lastMessageTime : createdAt;
    }

    // Getters and setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public List<String> getParticipants() {
        return participants;
    }
    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }
    public String getRelatedItemId() {
        return relatedItemId;
    }
    public void setRelatedItemId(String relatedItemId) {
        this.relatedItemId = relatedItemId;
    }
    public String getLastMessage() {
        return lastMessage;
    }
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
    public Timestamp getLastMessageTime() {
        return lastMessageTime;
    }
    public void setLastMessageTime(Timestamp lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }
    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }
    public Map<String, Integer> getUnreadCount() {
        return unreadCount;
    }
    public void setUnreadCount(Map<String, Integer> unreadCount) {
        this.unreadCount = unreadCount;
    }
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }
}
