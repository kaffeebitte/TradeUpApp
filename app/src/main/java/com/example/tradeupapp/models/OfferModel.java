package com.example.tradeupapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Date;

/**
 * Model representing an offer in the TradeUpApp.
 * Contains information about price offers made by buyers on listings.
 */
public class OfferModel implements Serializable {
    // Enum for offer status
    public enum Status {
        PENDING("pending"),
        ACCEPTED("accepted"),
        DECLINED("declined"),
        COUNTER_OFFERED("counter_offered"),
        WITHDRAWN("withdrawn"); // User cancelled the offer

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Status fromString(String value) {
            for (Status status : Status.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            return PENDING; // Default status
        }
    }

    // @DocumentId // <-- Remove this annotation to avoid Firestore mapping conflict
    private String id; // optional - Firestore auto ID
    private String listingId; // required - reference to listing
    private String sellerId; // required - user selling the item
    private String buyerId; // required - user making the offer
    private double offerAmount; // required - proposed price
    private String status; // required - "pending", "accepted", "declined", "counter_offered"
    private String message; // optional - additional notes
    private Date createdAt; // required - when offer was made
    private Date expiresAt; // optional - when the offer expires
    private Date respondedAt; // optional - when the offer was responded to
    private Double counterOffer; // optional - counter offer amount

    /**
     * Default constructor required for Firebase Firestore
     */
    public OfferModel() {
        // Required empty constructor for Firestore
        this.status = Status.PENDING.getValue();
        this.createdAt = new Date();
    }

    /**
     * Constructor with essential offer information
     */
    public OfferModel(String listingId, String sellerId, String buyerId, double offerAmount, String message) {
        this.listingId = listingId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.offerAmount = offerAmount;
        this.message = message;
        this.status = Status.PENDING.getValue();
        this.createdAt = new Date();
    }

    /**
     * Full constructor with all offer properties
     */
    public OfferModel(String id, String listingId, String sellerId, String buyerId, double offerAmount,
                     String message, String status, Object createdAt, Object expiresAt, Object respondedAt, Double counterOffer) {
        this.id = id;
        this.listingId = listingId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.offerAmount = offerAmount;
        this.message = message;
        this.status = status != null ? status : Status.PENDING.getValue();
        this.createdAt = parseDate(createdAt);
        this.expiresAt = parseDate(expiresAt);
        this.respondedAt = parseDate(respondedAt);
        this.counterOffer = counterOffer;
    }

    // Helper to parse Firestore Timestamp or String to Date
    private Date parseDate(Object field) {
        if (field == null) return null;
        if (field instanceof Timestamp) return ((Timestamp) field).toDate();
        if (field instanceof Date) return (Date) field;
        if (field instanceof Long) return new Date((Long) field);
        if (field instanceof String) {
            String str = (String) field;
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US);
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                return sdf.parse(str);
            } catch (Exception e) {
                try {
                    return new Date(Long.parseLong(str));
                } catch (Exception ignored) {}
            }
        }
        return null;
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

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public double getOfferAmount() {
        return offerAmount;
    }

    public void setOfferAmount(double offerAmount) {
        this.offerAmount = offerAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status : Status.PENDING.getValue();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Object createdAt) {
        this.createdAt = parseDate(createdAt);
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Object expiresAt) {
        this.expiresAt = parseDate(expiresAt);
    }

    public Date getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(Object respondedAt) {
        this.respondedAt = parseDate(respondedAt);
    }

    public Double getCounterOffer() {
        return counterOffer;
    }

    public void setCounterOffer(Double counterOffer) {
        this.counterOffer = counterOffer;
    }

    // Status helpers
    @Exclude // Not stored in Firestore, computed
    public void setStatusEnum(Status statusEnum) {
        this.status = statusEnum.getValue();
    }

    @Exclude // Not stored in Firestore, computed
    public Status getStatusEnum() {
        return Status.fromString(status);
    }

    @Exclude // Not stored in Firestore, computed
    public boolean isPending() {
        return Status.PENDING.getValue().equals(this.status);
    }

    @Exclude // Not stored in Firestore, computed
    public boolean isAccepted() {
        return Status.ACCEPTED.getValue().equals(this.status);
    }

    @Exclude // Not stored in Firestore, computed
    public boolean isDeclined() {
        return Status.DECLINED.getValue().equals(this.status);
    }

    @Exclude // Not stored in Firestore, computed
    public boolean isCounterOffered() {
        return Status.COUNTER_OFFERED.getValue().equals(this.status);
    }

    @Exclude // Not stored in Firestore, computed
    public boolean isWithdrawn() {
        return Status.WITHDRAWN.getValue().equals(this.status);
    }
}
