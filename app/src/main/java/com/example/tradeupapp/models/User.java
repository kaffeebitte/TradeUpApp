package com.example.tradeupapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.PropertyName;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

/**
 * Model representing a user in the TradeUpApp.
 * Contains user profile information and account status.
 */
public class User implements Serializable {

    // Add serialVersionUID for serialization
    private static final long serialVersionUID = 1L;

    // Enum for user roles
    public enum Role {
        USER("user"),
        ADMIN("admin");

        private final String value;

        Role(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Role fromString(String value) {
            for (Role role : Role.values()) {
                if (role.value.equals(value)) {
                    return role;
                }
            }
            return USER; // Default role
        }
    }

    // @DocumentId // Firestore document ID annotation
    private String uid; // required - UID from Firebase Auth
    private String displayName; // required
    private String email; // required
    private String phoneNumber; // optional
    private String photoUrl; // optional - Cloudinary URL
    private String bio; // optional
    private String role; // required - "user" or "admin"
    private double rating; // required - defaults to 0
    private int totalReviews; // required - defaults to 0

    // Mark Timestamp fields as transient to exclude them from Java serialization
    private transient Timestamp createdAt; // required
    private boolean isActive; // required - defaults to true

    // Mark GeoPoint as transient
    private transient GeoPoint location; // optional

    private transient Timestamp deactivatedAt; // optional - only set when account is deactivated
    private boolean isDeleted; // required - defaults to false
    private transient Timestamp deletedAt; // optional - only set when account is deleted
    private transient Timestamp updatedAt; // optional

    // Backup fields for serialization
    @Exclude private Date createdAtDate;
    @Exclude private Double locationLatitude;
    @Exclude private Double locationLongitude;
    @Exclude private Date deactivatedAtDate;
    @Exclude private Date deletedAtDate;
    @Exclude private Date updatedAtDate;

    // Add this field
    private boolean deactivated;

    private String id; // sample id, e.g. "user_001"

    private String status; // e.g. "active", "flagged", "suspended"
    private int warningCount; // number of warnings

    /**
     * Custom serialization method
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        // Save timestamp fields as Date objects which are Serializable
        if (createdAt != null) {
            createdAtDate = createdAt.toDate();
        }

        // Save GeoPoint fields as Double values
        if (location != null) {
            locationLatitude = location.getLatitude();
            locationLongitude = location.getLongitude();
        }

        if (deactivatedAt != null) {
            deactivatedAtDate = deactivatedAt.toDate();
        }

        if (deletedAt != null) {
            deletedAtDate = deletedAt.toDate();
        }

        if (updatedAt != null) {
            updatedAtDate = updatedAt.toDate();
        }

        // Write to stream
        out.defaultWriteObject();
    }

    /**
     * Custom deserialization method
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // Read from stream
        in.defaultReadObject();

        // Restore Timestamp fields from Date objects
        if (createdAtDate != null) {
            createdAt = new Timestamp(createdAtDate);
        }

        // Restore GeoPoint from latitude/longitude
        if (locationLatitude != null && locationLongitude != null) {
            location = new GeoPoint(locationLatitude, locationLongitude);
        }

        if (deactivatedAtDate != null) {
            deactivatedAt = new Timestamp(deactivatedAtDate);
        }

        if (deletedAtDate != null) {
            deletedAt = new Timestamp(deletedAtDate);
        }

        if (updatedAtDate != null) {
            updatedAt = new com.google.firebase.Timestamp(updatedAtDate);
        }
    }

    /**
     * Default constructor required for Firebase Firestore
     */
    public User() {
        // Required empty constructor for Firestore
        this.role = Role.USER.getValue();
        this.rating = 0.0;
        this.totalReviews = 0;
        this.isActive = true;
        this.isDeleted = false;
        this.deactivated = false;
    }

    /**
     * Constructor with essential user information
     */
    public User(String uid, String displayName, String email) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
        this.role = Role.USER.getValue();
        this.rating = 0.0;
        this.totalReviews = 0;
        this.createdAt = Timestamp.now();
        this.isActive = true;
        this.isDeleted = false;
        this.deactivated = false;
    }

    /**
     * Full constructor with all user properties
     */
    public User(String uid, String displayName, String email, String phoneNumber,
                String photoUrl, String bio, String role, double rating, int totalReviews,
                Timestamp createdAt, boolean isActive, GeoPoint location,
                Timestamp deactivatedAt, boolean isDeleted, Timestamp deletedAt) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.photoUrl = photoUrl;
        this.bio = bio;
        this.role = role;
        this.rating = rating;
        this.totalReviews = totalReviews;
        this.createdAt = createdAt;
        this.isActive = isActive;
        this.location = location;
        this.deactivatedAt = deactivatedAt;
        this.isDeleted = isDeleted;
        this.deletedAt = deletedAt;
        this.deactivated = false;
    }

    // Getters and Setters

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Helper methods for role
    @Exclude // Not stored in Firestore, computed
    public boolean isAdmin() {
        return Role.ADMIN.getValue().equals(role);
    }

    @Exclude // Not stored in Firestore, computed
    public void setAdmin(boolean admin) {
        this.role = admin ? Role.ADMIN.getValue() : Role.USER.getValue();
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @PropertyName("isActive")
    public boolean isActive() {
        return isActive;
    }

    @PropertyName("isActive")
    public void setActive(boolean active) {
        isActive = active;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(Object locationObj) {
        if (locationObj instanceof GeoPoint) {
            this.location = (GeoPoint) locationObj;
        } else if (locationObj instanceof String) {
            // If location is stored as a string in Firestore, we set it to null for now
            // You could also parse the string if it has a known format (like "lat,lng")
            this.location = null;

            // Optional: Log the issue for debugging
            System.out.println("Warning: Location field is a String instead of GeoPoint: " + locationObj);
        } else if (locationObj == null) {
            this.location = null;
        }
    }

    public Timestamp getDeactivatedAt() {
        return deactivatedAt;
    }

    public void setDeactivatedAt(Timestamp deactivatedAt) {
        this.deactivatedAt = deactivatedAt;
    }

    @PropertyName("isDeleted")
    public boolean isDeleted() {
        return isDeleted;
    }

    @PropertyName("isDeleted")
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Timestamp getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Timestamp deletedAt) {
        this.deletedAt = deletedAt;
    }

    public com.google.firebase.Timestamp getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(com.google.firebase.Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Double getLocationLatitude() {
        return locationLatitude;
    }
    public Double getLocationLongitude() {
        return locationLongitude;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public int getWarningCount() {
        return warningCount;
    }
    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
    }

    /**
     * Update rating when a new review is added
     * @param newRating the rating value of the new review
     */
    public void addReview(double newRating) {
        double totalRating = this.rating * this.totalReviews;
        this.totalReviews++;
        this.rating = (totalRating + newRating) / this.totalReviews;
    }

    /**
     * Deactivate user account
     */
    public void deactivateAccount() {
        this.isActive = false;
        this.deactivatedAt = Timestamp.now();
    }

    /**
     * Reactivate user account
     */
    public void reactivateAccount() {
        this.isActive = true;
        this.deactivatedAt = null;
    }

    /**
     * Mark user account as deleted (soft delete)
     */
    public void markAsDeleted() {
        this.isDeleted = true;
        this.deletedAt = Timestamp.now();
        this.isActive = false;
    }

    public boolean isDeactivated() {
        return deactivated;
    }
    public void setDeactivated(boolean deactivated) {
        this.deactivated = deactivated;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get user ID or UID
     * @return UID if available, otherwise ID
     */
    public String getUserIdOrUid() {
        if (getUid() != null && !getUid().isEmpty()) {
            return getUid();
        }
        if (getId() != null && !getId().isEmpty()) {
            return getId();
        }
        return null;
    }
}
