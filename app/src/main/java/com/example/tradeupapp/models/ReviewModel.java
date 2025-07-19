package com.example.tradeupapp.models;

import com.google.firebase.Timestamp;
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

    private String id; // Firestore auto ID
    private String reviewerId; // user who gives the review
    private String revieweeId; // user who receives the review
    private String transactionId; // required - related transaction
    private String listingId; // listing being reviewed
    private float rating; // required - star rating (1-5)
    private String comment; // optional - text feedback
    private Timestamp createdAt; // required - when review was submitted
    private int helpfulCount; // optional, default 0
    private boolean isVerified; // moderation status

    /**
     * Default constructor required for Firebase Firestore
     */
    public ReviewModel() {
        // Required empty constructor for Firestore
        this.rating = MIN_RATING;
        this.createdAt = Timestamp.now();
        this.helpfulCount = 0;
        this.isVerified = true;
    }

    /**
     * Constructor with essential review information
     */
    public ReviewModel(String transactionId, String reviewerId, String revieweeId, String listingId, float rating, String comment) {
        this.transactionId = transactionId;
        this.reviewerId = reviewerId;
        this.revieweeId = revieweeId;
        this.listingId = listingId;
        setRating(rating); // Uses validation
        this.comment = comment;
        this.createdAt = Timestamp.now();
        this.helpfulCount = 0;
        this.isVerified = true;
    }

    /**
     * Full constructor with all review properties
     */
    public ReviewModel(String id, String transactionId, String reviewerId, String revieweeId, String listingId, float rating, String comment, Timestamp createdAt, int helpfulCount, boolean isVerified) {
        this.id = id;
        this.transactionId = transactionId;
        this.reviewerId = reviewerId;
        this.revieweeId = revieweeId;
        this.listingId = listingId;
        setRating(rating); // Uses validation
        this.comment = comment;
        this.createdAt = createdAt != null ? createdAt : Timestamp.now();
        this.helpfulCount = helpfulCount;
        this.isVerified = isVerified;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getRevieweeId() {
        return revieweeId;
    }

    public void setRevieweeId(String revieweeId) {
        this.revieweeId = revieweeId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
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

    public int getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(int helpfulCount) {
        this.helpfulCount = helpfulCount;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
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
