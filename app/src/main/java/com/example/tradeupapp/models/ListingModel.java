package com.example.tradeupapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a listing (posted item) in the TradeUpApp.
 * Contains all information about an item that a user has posted for sale.
 */
public class ListingModel implements Serializable {

    // Enum for transaction status
    public enum TransactionStatus {
        AVAILABLE("available"),
        PENDING("pending"),
        SOLD("sold");

        private final String value;

        TransactionStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static TransactionStatus fromString(String value) {
            for (TransactionStatus status : TransactionStatus.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            return AVAILABLE; // Default status
        }
    }

    private String id; // required - Firestore auto ID
    private String itemId; // Reference to the single ItemModel for this listing
    private double price; // required
    private String sellerId; // required - UID of seller
    private boolean isActive; // required - defaults to true
    private Timestamp createdAt; // required
    private Timestamp updatedAt; // required
    private int viewCount; // required - defaults to 0
    private List<String> tags; // optional - related keywords
    private String transactionStatus; // required - "available", "pending", "sold"
    private double distanceRadius; // required - delivery radius in km

    /**
     * Default constructor required for Firebase Firestore
     */
    public ListingModel() {
        // Required empty constructor for Firestore
        this.viewCount = 0;
        this.isActive = true;
        this.transactionStatus = TransactionStatus.AVAILABLE.getValue();
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
        this.tags = new ArrayList<>();
    }

    /**
     * Constructor with essential listing information
     */
    public ListingModel(String itemId, double price, String sellerId, double distanceRadius) {
        this.itemId = itemId;
        this.price = price;
        this.sellerId = sellerId;
        this.distanceRadius = distanceRadius;
        this.isActive = true;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
        this.viewCount = 0;
        this.tags = new ArrayList<>();
        this.transactionStatus = TransactionStatus.AVAILABLE.getValue();
    }

    /**
     * Full constructor with all listing properties
     */
    public ListingModel(String id, String itemId, double price, String sellerId, boolean isActive,
                      Timestamp createdAt, Timestamp updatedAt, int viewCount,
                      List<String> tags, String transactionStatus, double distanceRadius) {
        this.id = id;
        this.itemId = itemId;
        this.price = price;
        this.sellerId = sellerId;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.viewCount = viewCount;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.transactionStatus = transactionStatus;
        this.distanceRadius = distanceRadius;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    @PropertyName("isActive")
    public boolean isActive() {
        return isActive;
    }

    @PropertyName("isActive")
    public void setActive(boolean active) {
        isActive = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public List<String> getTags() {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
        }
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public double getDistanceRadius() {
        return distanceRadius;
    }

    public void setDistanceRadius(double distanceRadius) {
        this.distanceRadius = distanceRadius;
    }

    // Helper methods for transaction status
    @Exclude // Not stored in Firestore, computed
    public void setTransactionStatusEnum(TransactionStatus status) {
        this.transactionStatus = status.getValue();
    }

    @Exclude // Not stored in Firestore, computed
    public TransactionStatus getTransactionStatusEnum() {
        return TransactionStatus.fromString(transactionStatus);
    }

    /**
     * Mark listing as updated and set the updatedAt timestamp
     */
    public void markAsUpdated() {
        this.updatedAt = Timestamp.now();
    }

    /**
     * Mark listing as sold
     */
    public void markAsSold() {
        this.transactionStatus = TransactionStatus.SOLD.getValue();
        this.isActive = false;
        this.updatedAt = Timestamp.now();
    }

    /**
     * Mark listing as pending (in transaction)
     */
    public void markAsPending() {
        this.transactionStatus = TransactionStatus.PENDING.getValue();
        this.updatedAt = Timestamp.now();
    }

    /**
     * Mark listing as available again
     */
    public void markAsAvailable() {
        this.transactionStatus = TransactionStatus.AVAILABLE.getValue();
        this.isActive = true;
        this.updatedAt = Timestamp.now();
    }

    /**
     * Deactivate listing (hide from search results)
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = Timestamp.now();
    }

    /**
     * Activate listing (show in search results)
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = Timestamp.now();
    }

    /**
     * Check if this listing is available for purchase
     * @return true if available, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isAvailable() {
        return TransactionStatus.AVAILABLE.getValue().equals(transactionStatus) && isActive;
    }
}
