package com.example.tradeupapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a listing (posted item) in the TradeUpApp.
 * Contains all information about an item that a user has posted for sale.
 */
public class ListingModel implements Serializable {

    // Enum for item condition
    public enum Condition {
        NEW("new"),
        LIKE_NEW("like_new"),
        GOOD("good"),
        FAIR("fair"),
        POOR("poor");

        private final String value;

        Condition(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Condition fromString(String value) {
            for (Condition condition : Condition.values()) {
                if (condition.value.equals(value)) {
                    return condition;
                }
            }
            return GOOD; // Default condition
        }
    }

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

    @DocumentId
    private String id; // required - Firestore auto ID
    private String title; // required
    private String description; // required
    private List<String> images; // optional - Cloudinary URLs
    private String category; // required
    private double price; // required
    private String condition; // required - "new", "like_new", "good", "fair", "poor"
    private GeoPoint location; // required
    private double distanceRadius; // required - delivery radius in km
    private String sellerId; // required - UID of seller
    private boolean isActive; // required - defaults to true
    private Timestamp createdAt; // required
    private Timestamp updatedAt; // required
    private int viewCount; // required - defaults to 0
    private List<String> tags; // optional - related keywords
    private String transactionStatus; // required - "available", "pending", "sold"
    // Add items collection to represent multiple items in one listing
    private List<String> itemIds; // List of ItemModel IDs that belong to this listing

    /**
     * Default constructor required for Firebase Firestore
     */
    public ListingModel() {
        // Required empty constructor for Firestore
        this.images = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.viewCount = 0;
        this.isActive = true;
        this.transactionStatus = TransactionStatus.AVAILABLE.getValue();
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
        this.itemIds = new ArrayList<>();
    }

    /**
     * Constructor with essential listing information
     */
    public ListingModel(String title, String description, String category,
                        double price, String condition, GeoPoint location,
                        double distanceRadius, String sellerId) {
        this.title = title;
        this.description = description;
        this.images = new ArrayList<>();
        this.category = category;
        this.price = price;
        this.condition = condition;
        this.location = location;
        this.distanceRadius = distanceRadius;
        this.sellerId = sellerId;
        this.isActive = true;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
        this.viewCount = 0;
        this.tags = new ArrayList<>();
        this.transactionStatus = TransactionStatus.AVAILABLE.getValue();
        this.itemIds = new ArrayList<>();
    }

    /**
     * Full constructor with all listing properties
     */
    public ListingModel(String id, String title, String description, List<String> images,
                      String category, double price, String condition, GeoPoint location,
                      double distanceRadius, String sellerId, boolean isActive,
                      Timestamp createdAt, Timestamp updatedAt, int viewCount,
                      List<String> tags, String transactionStatus, List<String> itemIds) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.images = images != null ? images : new ArrayList<>();
        this.category = category;
        this.price = price;
        this.condition = condition;
        this.location = location;
        this.distanceRadius = distanceRadius;
        this.sellerId = sellerId;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.viewCount = viewCount;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.transactionStatus = transactionStatus;
        this.itemIds = itemIds != null ? itemIds : new ArrayList<>();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImages() {
        if (images == null) {
            images = new ArrayList<>();
        }
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images != null ? images : new ArrayList<>();
    }

    public void addImage(String imageUrl) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        this.images.add(imageUrl);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    // Helper methods for condition
    @Exclude // Not stored in Firestore, computed
    public void setConditionEnum(Condition conditionEnum) {
        this.condition = conditionEnum.getValue();
    }

    @Exclude // Not stored in Firestore, computed
    public Condition getConditionEnum() {
        return Condition.fromString(condition);
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public double getDistanceRadius() {
        return distanceRadius;
    }

    public void setDistanceRadius(double distanceRadius) {
        this.distanceRadius = distanceRadius;
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
     * Get the primary image URL (first image) or null if no images
     * @return the first image URL or null
     */
    @Exclude // Not stored in Firestore, computed
    public String getPrimaryImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return null;
    }

    /**
     * Check if this listing is available for purchase
     * @return true if available, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isAvailable() {
        return TransactionStatus.AVAILABLE.getValue().equals(transactionStatus) && isActive;
    }

    public List<String> getItemIds() {
        if (itemIds == null) {
            itemIds = new ArrayList<>();
        }
        return itemIds;
    }

    public void setItemIds(List<String> itemIds) {
        this.itemIds = itemIds != null ? itemIds : new ArrayList<>();
    }

    public void addItemId(String itemId) {
        if (this.itemIds == null) {
            this.itemIds = new ArrayList<>();
        }
        this.itemIds.add(itemId);
    }
}
