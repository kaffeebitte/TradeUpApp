package com.example.tradeupapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

/**
 * Model representing an offer in the TradeUpApp.
 * Contains information about price offers made by buyers on listings.
 */
public class OfferModel implements Serializable {

    // Enum for offer status
    public enum Status {
        PENDING("pending"),
        ACCEPTED("accepted"),
        REJECTED("rejected"),
        WITHDRAWN("withdrawn");

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

    @DocumentId
    private String id; // optional - Firestore auto ID
    private String listingId; // required - reference to listing
    private String buyerId; // required - user making the offer
    private double offeredPrice; // required - proposed price
    private String message; // optional - additional notes
    private String status; // required - "pending", "accepted", "rejected", "withdrawn"
    private Timestamp createdAt; // required - when offer was made

    /**
     * Default constructor required for Firebase Firestore
     */
    public OfferModel() {
        // Required empty constructor for Firestore
        this.status = Status.PENDING.getValue();
        this.createdAt = Timestamp.now();
    }

    /**
     * Constructor with essential offer information
     */
    public OfferModel(String listingId, String buyerId, double offeredPrice, String message) {
        this.listingId = listingId;
        this.buyerId = buyerId;
        this.offeredPrice = offeredPrice;
        this.message = message;
        this.status = Status.PENDING.getValue();
        this.createdAt = Timestamp.now();
    }

    /**
     * Full constructor with all offer properties
     */
    public OfferModel(String id, String listingId, String buyerId, double offeredPrice,
                     String message, String status, Timestamp createdAt) {
        this.id = id;
        this.listingId = listingId;
        this.buyerId = buyerId;
        this.offeredPrice = offeredPrice;
        this.message = message;
        this.status = status != null ? status : Status.PENDING.getValue();
        this.createdAt = createdAt != null ? createdAt : Timestamp.now();
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

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public double getOfferedPrice() {
        return offeredPrice;
    }

    public void setOfferedPrice(double offeredPrice) {
        this.offeredPrice = offeredPrice;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status : Status.PENDING.getValue();
    }

    // Helper methods for status
    @Exclude // Not stored in Firestore, computed
    public void setStatusEnum(Status statusEnum) {
        this.status = statusEnum.getValue();
    }

    @Exclude // Not stored in Firestore, computed
    public Status getStatusEnum() {
        return Status.fromString(status);
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt != null ? createdAt : Timestamp.now();
    }

    /**
     * Accept the offer
     */
    public void accept() {
        this.status = Status.ACCEPTED.getValue();
    }

    /**
     * Reject the offer
     */
    public void reject() {
        this.status = Status.REJECTED.getValue();
    }

    /**
     * Withdraw the offer (by buyer)
     */
    public void withdraw() {
        this.status = Status.WITHDRAWN.getValue();
    }

    /**
     * Check if the offer is pending
     * @return true if the offer is pending, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isPending() {
        return Status.PENDING.getValue().equals(this.status);
    }

    /**
     * Check if the offer is accepted
     * @return true if the offer is accepted, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isAccepted() {
        return Status.ACCEPTED.getValue().equals(this.status);
    }

    /**
     * Check if the offer is rejected
     * @return true if the offer is rejected, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isRejected() {
        return Status.REJECTED.getValue().equals(this.status);
    }

    /**
     * Check if the offer is withdrawn
     * @return true if the offer is withdrawn, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isWithdrawn() {
        return Status.WITHDRAWN.getValue().equals(this.status);
    }

    /**
     * Calculate the discount percentage compared to the original listing price
     * @param originalPrice the original price of the listing
     * @return the discount percentage (0-100)
     */
    @Exclude // Not stored in Firestore, computed
    public double getDiscountPercentage(double originalPrice) {
        if (originalPrice <= 0) {
            return 0;
        }
        double discount = originalPrice - offeredPrice;
        return Math.round((discount / originalPrice) * 100);
    }
}
