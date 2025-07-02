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
    private String id; // optional - Firestore auto ID
    private String senderId; // required - UID of message sender
    private String receiverId; // required - UID of message receiver
    private String text; // required - message content
    private String imageUrl; // optional - Cloudinary URL if message contains an image
    private Timestamp createdAt; // required - when message was sent
    private List<String> readBy; // required - list of user IDs who read the message
    private int messageType; // to differentiate between sent and received messages in the UI

    // Constants for message types
    public static final int TYPE_SENT = 1;
    public static final int TYPE_RECEIVED = 2;

    /**
     * Default constructor required for Firebase Firestore
     */
    public ChatMessage() {
        // Required empty constructor for Firestore
        this.readBy = new ArrayList<>();
        this.createdAt = Timestamp.now();
    }

    /**
     * Constructor for text-only message
     */
    public ChatMessage(String senderId, String text) {
        this.senderId = senderId;
        this.text = text != null ? text : "";
        this.imageUrl = null;
        this.createdAt = Timestamp.now();
        this.readBy = new ArrayList<>();
        if (senderId != null) {
            this.readBy.add(senderId); // sender has already read their own message
        }
        this.messageType = TYPE_SENT; // default to sent message
    }

    /**
     * Constructor for image message
     */
    public ChatMessage(String senderId, String text, String imageUrl) {
        this.senderId = senderId;
        this.text = text != null ? text : "";
        this.imageUrl = imageUrl;
        this.createdAt = Timestamp.now();
        this.readBy = new ArrayList<>();
        if (senderId != null) {
            this.readBy.add(senderId); // sender has already read their own message
        }
        this.messageType = TYPE_SENT; // default to sent message
    }

    /**
     * Full constructor with all message properties
     */
    public ChatMessage(String id, String senderId, String text, String imageUrl,
                       Timestamp createdAt, List<String> readBy, int messageType) {
        this.id = id;
        this.senderId = senderId;
        this.text = text != null ? text : "";
        this.imageUrl = imageUrl;
        this.createdAt = createdAt != null ? createdAt : Timestamp.now();
        this.readBy = readBy != null ? readBy : new ArrayList<>();
        this.messageType = messageType;
    }

    /**
     * Constructor for chat UI with sender, receiver, text, image, and message type
     */
    public ChatMessage(String senderId, String receiverId, String text, String imageUrl, int messageType) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text != null ? text : "";
        this.imageUrl = imageUrl;
        this.messageType = messageType;
        this.createdAt = Timestamp.now();
        this.readBy = new ArrayList<>();
        if (senderId != null) {
            this.readBy.add(senderId); // sender has already read their own message
        }
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text != null ? text : "";
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt != null ? createdAt : Timestamp.now();
    }

    public List<String> getReadBy() {
        if (readBy == null) {
            readBy = new ArrayList<>();
        }
        return readBy;
    }

    public void setReadBy(List<String> readBy) {
        this.readBy = readBy != null ? readBy : new ArrayList<>();
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    /**
     * Mark message as read by a user
     * @param userId the ID of the user who read the message
     */
    public void markAsReadBy(String userId) {
        if (userId == null) {
            return;
        }

        if (this.readBy == null) {
            this.readBy = new ArrayList<>();
        }
        if (!this.readBy.contains(userId)) {
            this.readBy.add(userId);
        }
    }

    /**
     * Check if message has been read by a specific user
     * @param userId the ID of the user to check
     * @return true if the user has read the message, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isReadBy(String userId) {
        return userId != null && readBy != null && readBy.contains(userId);
    }

    /**
     * Check if message has an image
     * @return true if the message has an image, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.isEmpty();
    }

    /**
     * Get the number of readers
     * @return the count of users who have read this message
     */
    @Exclude // Not stored in Firestore, computed
    public int getReadCount() {
        return readBy != null ? readBy.size() : 0;
    }

    /**
     * Check if this is a system message (no sender)
     * @return true if this is a system message, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isSystemMessage() {
        return senderId == null || senderId.isEmpty();
    }
}
