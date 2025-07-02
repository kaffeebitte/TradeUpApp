package com.example.tradeupapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

/**
 * Model representing a user review in the TradeUpApp.
 * Contains information about ratings and comments users leave for each other after transactions.
 */
public class ReviewModel implements Serializable {

    // Rating range constants
    public static final float MIN_RATING = 1.0f;
    public static final float MAX_RATING = 5.0f;

    @DocumentId
    private String id; // optional - Firestore auto ID
    private String fromUserId; // required - user giving the review
    private String toUserId; // required - user receiving the review
    private String transactionId; // required - related transaction
    private float rating; // required - star rating (1-5)
    private String comment; // optional - text feedback
    private Timestamp createdAt; // required - when review was submitted

    /**
     * Default constructor required for Firebase Firestore
     */
    public ReviewModel() {
        // Required empty constructor for Firestore
        this.rating = MIN_RATING;
        this.createdAt = Timestamp.now();
    }

    /**
     * Constructor with essential review information
     */
    public ReviewModel(String fromUserId, String toUserId, String transactionId, float rating, String comment) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.transactionId = transactionId;
        setRating(rating); // Uses validation
        this.comment = comment;
        this.createdAt = Timestamp.now();
    }

    /**
     * Full constructor with all review properties
     */
    public ReviewModel(String id, String fromUserId, String toUserId, String transactionId,
                      float rating, String comment, Timestamp createdAt) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.transactionId = transactionId;
        setRating(rating); // Uses validation
        this.comment = comment;
        this.createdAt = createdAt != null ? createdAt : Timestamp.now();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        // Ensure rating is between 1 and 5
        if (rating < MIN_RATING) {
            this.rating = MIN_RATING;
        } else if (rating > MAX_RATING) {
            this.rating = MAX_RATING;
        } else {
            this.rating = rating;
        }
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt != null ? createdAt : Timestamp.now();
    }

    /**
     * Check if this review has a valid rating (1-5)
     * @return true if rating is valid, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean hasValidRating() {
        return rating >= MIN_RATING && rating <= MAX_RATING;
    }

    /**
     * Check if this review has a comment
     * @return true if comment exists and is not empty, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }

    /**
     * Get a description of the rating (excellent, good, average, etc.)
     * @return text description of the rating
     */
    @Exclude // Not stored in Firestore, computed
    public String getRatingDescription() {
        if (rating >= 4.5f) return "Excellent";
        if (rating >= 3.5f) return "Good";
        if (rating >= 2.5f) return "Average";
        if (rating >= 1.5f) return "Poor";
        return "Very Poor";
    }

    /**
     * Check if this is a positive review
     * @return true if rating is 4 or higher, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isPositiveReview() {
        return rating >= 4.0f;
    }
}
