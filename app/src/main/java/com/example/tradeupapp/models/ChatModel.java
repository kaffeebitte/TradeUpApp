package com.example.tradeupapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a chat room in the TradeUpApp.
 * Contains information about the conversation between users regarding a listing.
 */
public class ChatModel implements Serializable {
    @DocumentId
    private String id; // required - Firestore auto ID
    private String listingId; // required - reference to the listing
    private List<String> userIds; // required - participants in the chat (buyer and seller)
    private String lastMessage; // required - most recent message text
    private Timestamp lastUpdated; // required - when the most recent activity occurred

    /**
     * Default constructor required for Firebase Firestore
     */
    public ChatModel() {
        // Required empty constructor for Firestore
        this.userIds = new ArrayList<>();
        this.lastMessage = "";
        this.lastUpdated = Timestamp.now();
    }

    /**
     * Constructor with essential chat information
     */
    public ChatModel(String listingId, List<String> userIds) {
        this.listingId = listingId;
        this.userIds = userIds != null ? userIds : new ArrayList<>();
        this.lastMessage = "";
        this.lastUpdated = Timestamp.now();
    }

    /**
     * Full constructor with all chat properties
     */
    public ChatModel(String id, String listingId, List<String> userIds,
                    String lastMessage, Timestamp lastUpdated) {
        this.id = id;
        this.listingId = listingId;
        this.userIds = userIds != null ? userIds : new ArrayList<>();
        this.lastMessage = lastMessage != null ? lastMessage : "";
        this.lastUpdated = lastUpdated != null ? lastUpdated : Timestamp.now();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public List<String> getUserIds() {
        if (userIds == null) {
            userIds = new ArrayList<>();
        }
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds != null ? userIds : new ArrayList<>();
    }

    public void addUserId(String userId) {
        if (this.userIds == null) {
            this.userIds = new ArrayList<>();
        }
        if (!this.userIds.contains(userId)) {
            this.userIds.add(userId);
        }
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage != null ? lastMessage : "";
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated != null ? lastUpdated : Timestamp.now();
    }

    /**
     * Update last message and timestamp when a new message is sent
     * @param messageText the text of the new message
     */
    public void updateLastMessage(String messageText) {
        this.lastMessage = messageText != null ? messageText : "";
        this.lastUpdated = Timestamp.now();
    }

    /**
     * Check if user is a participant in this chat
     * @param userId the ID of the user to check
     * @return true if the user is a participant, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean hasUser(String userId) {
        return userIds != null && userIds.contains(userId);
    }

    /**
     * Get the other user's ID in a two-person chat
     * @param currentUserId the ID of the current user
     * @return the ID of the other user, or null if not found
     */
    @Exclude // Not stored in Firestore, computed
    public String getOtherUserId(String currentUserId) {
        if (userIds != null && userIds.size() == 2) {
            return userIds.get(0).equals(currentUserId) ? userIds.get(1) : userIds.get(0);
        }
        return null;
    }

    /**
     * Get message preview for display in chat list
     * Limits the length of the last message for display purposes
     * @return formatted preview of the last message
     */
    @Exclude // Not stored in Firestore, computed
    public String getMessagePreview() {
        if (lastMessage == null || lastMessage.isEmpty()) {
            return "No messages yet";
        }

        // Limit message preview to 50 characters
        if (lastMessage.length() <= 50) {
            return lastMessage;
        }
        return lastMessage.substring(0, 47) + "...";
    }
}
