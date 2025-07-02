package com.example.tradeupapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

/**
 * Model representing a notification in the TradeUpApp.
 * Contains information about notifications sent to users.
 */
public class NotificationModel implements Serializable {

    // Enum for notification types
    public enum Type {
        CHAT("chat"),
        OFFER("offer"),
        REVIEW("review"),
        TRANSACTION("transaction"),
        SYSTEM("system");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Type fromString(String value) {
            for (Type type : Type.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            return SYSTEM; // Default type
        }
    }

    @DocumentId
    private String id; // optional - Firestore auto ID
    private String userId; // required - recipient user
    private String title; // required - notification title
    private String body; // required - notification content
    private String type; // required - "chat", "offer", "review", "transaction", "system"
    private boolean read; // required - defaults to false
    private Timestamp createdAt; // required - when notification was sent
    private String relatedId; // optional - reference to related item (listingId, chatId, etc.)

    /**
     * Default constructor required for Firebase Firestore
     */
    public NotificationModel() {
        // Required empty constructor for Firestore
        this.read = false;
        this.createdAt = Timestamp.now();
    }

    /**
     * Constructor with essential notification information
     */
    public NotificationModel(String userId, String title, String body, String type, String relatedId) {
        this.userId = userId;
        this.title = title != null ? title : "";
        this.body = body != null ? body : "";
        this.type = type;
        this.read = false;
        this.createdAt = Timestamp.now();
        this.relatedId = relatedId;
    }

    /**
     * Constructor with enum type
     */
    public NotificationModel(String userId, String title, String body, Type type, String relatedId) {
        this.userId = userId;
        this.title = title != null ? title : "";
        this.body = body != null ? body : "";
        this.type = type.getValue();
        this.read = false;
        this.createdAt = Timestamp.now();
        this.relatedId = relatedId;
    }

    /**
     * Full constructor with all notification properties
     */
    public NotificationModel(String id, String userId, String title, String body,
                          String type, boolean read, Timestamp createdAt, String relatedId) {
        this.id = id;
        this.userId = userId;
        this.title = title != null ? title : "";
        this.body = body != null ? body : "";
        this.type = type;
        this.read = read;
        this.createdAt = createdAt != null ? createdAt : Timestamp.now();
        this.relatedId = relatedId;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title != null ? title : "";
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body != null ? body : "";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Helper methods for type
    @Exclude // Not stored in Firestore, computed
    public void setTypeEnum(Type typeEnum) {
        this.type = typeEnum.getValue();
    }

    @Exclude // Not stored in Firestore, computed
    public Type getTypeEnum() {
        return Type.fromString(type);
    }

    @PropertyName("read")
    public boolean isRead() {
        return read;
    }

    @PropertyName("read")
    public void setRead(boolean read) {
        this.read = read;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt != null ? createdAt : Timestamp.now();
    }

    public String getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(String relatedId) {
        this.relatedId = relatedId;
    }

    /**
     * Mark the notification as read
     */
    public void markAsRead() {
        this.read = true;
    }

    /**
     * Check if this notification is related to a chat
     * @return true if notification type is "chat"
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isChatNotification() {
        return Type.CHAT.getValue().equals(this.type);
    }

    /**
     * Check if this notification is related to an offer
     * @return true if notification type is "offer"
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isOfferNotification() {
        return Type.OFFER.getValue().equals(this.type);
    }

    /**
     * Check if this notification is related to a review
     * @return true if notification type is "review"
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isReviewNotification() {
        return Type.REVIEW.getValue().equals(this.type);
    }

    /**
     * Check if this notification is related to a transaction
     * @return true if notification type is "transaction"
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isTransactionNotification() {
        return Type.TRANSACTION.getValue().equals(this.type);
    }

    /**
     * Check if this notification is a system notification
     * @return true if notification type is "system"
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isSystemNotification() {
        return Type.SYSTEM.getValue().equals(this.type);
    }

    /**
     * Create a chat notification
     * @param userId recipient user ID
     * @param senderName name of message sender
     * @param message preview of the message
     * @param chatId ID of the chat
     * @return a new notification object
     */
    public static NotificationModel createChatNotification(String userId, String senderName, String message, String chatId) {
        String title = senderName + " sent you a message";
        String body = message;
        return new NotificationModel(userId, title, body, Type.CHAT, chatId);
    }

    /**
     * Create an offer notification
     * @param userId recipient user ID
     * @param buyerName name of user making the offer
     * @param itemTitle title of the listing
     * @param offerId ID of the offer
     * @return a new notification object
     */
    public static NotificationModel createOfferNotification(String userId, String buyerName, String itemTitle, String offerId) {
        String title = "New offer on " + itemTitle;
        String body = buyerName + " made an offer on your item";
        return new NotificationModel(userId, title, body, Type.OFFER, offerId);
    }
}
