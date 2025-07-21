package com.example.tradeupapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.threeten.bp.Instant;

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
    private Object createdAt; // changed from Timestamp to Object to handle both String and Timestamp
    private Object updatedAt; // do the same for updatedAt if needed
    private int viewCount; // required - defaults to 0
    private List<String> tags; // optional - related keywords
    private String transactionStatus; // required - "available", "pending", "sold"
    private double distanceRadius; // required - delivery radius in km
    private boolean allowOffers; // new field
    private boolean allowReturns; // new field
    private Interactions interactions; // new field for embedded interactions
    private Double latitude; // latitude of the listing location
    private Double longitude; // longitude of the listing location
    // Location field (address, lat, lng)
    private java.util.Map<String, Object> location;
    private Boolean isBanned; // optional - whether the listing is banned
    private Long warningCount; // optional - number of warnings

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
        this.allowOffers = true;
        this.allowReturns = false;
        this.interactions = new Interactions();
        this.warningCount = 0L;
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
        this.allowOffers = true;
        this.allowReturns = false;
        this.interactions = new Interactions();
        this.warningCount = 0L;
    }

    /**
     * Full constructor with all listing properties
     */
    public ListingModel(String id, String itemId, double price, String sellerId, boolean isActive,
                      Object createdAt, Object updatedAt, int viewCount,
                      List<String> tags, String transactionStatus, double distanceRadius,
                      boolean allowOffers, boolean allowReturns, Interactions interactions,
                      Double latitude, Double longitude, Boolean isBanned, Long warningCount) {
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
        this.allowOffers = allowOffers;
        this.allowReturns = allowReturns;
        this.interactions = interactions != null ? interactions : new Interactions();
        this.latitude = latitude;
        this.longitude = longitude;
        this.isBanned = isBanned;
        this.warningCount = warningCount != null ? warningCount : 0L;
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

    public Object getCreatedAt() {
        return createdAt;
    }

    public Timestamp getCreatedAtTimestamp() {
        if (createdAt instanceof Timestamp) {
            return (Timestamp) createdAt;
        } else if (createdAt instanceof String) {
            try {
                Instant instant = Instant.parse((String) createdAt);
                return new Timestamp(instant.getEpochSecond(), instant.getNano());
            } catch (Exception e) {
                return Timestamp.now();
            }
        }
        return Timestamp.now();
    }

    public void setCreatedAt(Object createdAt) {
        this.createdAt = createdAt;
    }

    public Object getUpdatedAt() {
        return updatedAt;
    }

    public Timestamp getUpdatedAtTimestamp() {
        if (updatedAt instanceof Timestamp) {
            return (Timestamp) updatedAt;
        } else if (updatedAt instanceof String) {
            try {
                Instant instant = Instant.parse((String) updatedAt);
                return new Timestamp(instant.getEpochSecond(), instant.getNano());
            } catch (Exception e) {
                return Timestamp.now();
            }
        }
        return Timestamp.now();
    }

    public void setUpdatedAt(Object updatedAt) {
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

    public boolean getAllowOffers() {
        return allowOffers;
    }

    public void setAllowOffers(boolean allowOffers) {
        this.allowOffers = allowOffers;
    }

    public boolean isAllowReturns() {
        return allowReturns;
    }

    public void setAllowReturns(boolean allowReturns) {
        this.allowReturns = allowReturns;
    }

    public Interactions getInteractions() {
        return interactions;
    }

    public void setInteractions(Interactions interactions) {
        this.interactions = interactions;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public java.util.Map<String, Object> getLocation() {
        return location;
    }

    public void setLocation(java.util.Map<String, Object> location) {
        this.location = location;
    }

    public Boolean getIsBanned() {
        return isBanned;
    }

    public void setIsBanned(Boolean isBanned) {
        this.isBanned = isBanned;
    }

    public Long getWarningCount() {
        return warningCount != null ? warningCount : 0L;
    }

    public void setWarningCount(Long warningCount) {
        this.warningCount = warningCount;
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

    public static class Interactions implements Serializable {
        private Aggregate aggregate;
        private List<UserInteraction> userInteractions;

        public Interactions() {
            this.aggregate = new Aggregate();
            this.userInteractions = new ArrayList<>();
        }
        public Aggregate getAggregate() { return aggregate; }
        public void setAggregate(Aggregate aggregate) { this.aggregate = aggregate; }
        public List<UserInteraction> getUserInteractions() { return userInteractions; }
        public void setUserInteractions(List<UserInteraction> userInteractions) { this.userInteractions = userInteractions; }
    }

    public static class Aggregate implements Serializable {
        private int totalViews;
        private int totalSaves;
        private int totalShares;
        public Aggregate() { }
        public int getTotalViews() { return totalViews; }
        public void setTotalViews(int totalViews) { this.totalViews = totalViews; }
        public int getTotalSaves() { return totalSaves; }
        public void setTotalSaves(int totalSaves) { this.totalSaves = totalSaves; }
        public int getTotalShares() { return totalShares; }
        public void setTotalShares(int totalShares) { this.totalShares = totalShares; }
    }

    public static class UserInteraction implements Serializable {
        private String userId;
        private Object viewedAt;
        private Object savedAt;
        private Object sharedAt;
        public UserInteraction() { }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public Object getViewedAt() { return viewedAt; }
        public void setViewedAt(Object viewedAt) { this.viewedAt = viewedAt; }
        public Object getSavedAt() { return savedAt; }
        public void setSavedAt(Object savedAt) { this.savedAt = savedAt; }
        public Object getSharedAt() { return sharedAt; }
        public void setSharedAt(Object sharedAt) { this.sharedAt = sharedAt; }
    }
}
