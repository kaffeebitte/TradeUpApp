package com.example.tradeupapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Model representing an administrative action log in the TradeUpApp.
 * Contains information about actions performed by administrators.
 */
public class AdminLogModel implements Serializable {
    @DocumentId
    private String id;
    private String adminId;
    private String action;
    private String targetId;
    private Map<String, Object> details;
    private Timestamp timestamp;

    /**
     * Default constructor required for Firebase Firestore
     */
    public AdminLogModel() {
        // Required empty constructor for Firestore
        this.details = new HashMap<>();
    }

    /**
     * Constructor with essential admin log information
     */
    public AdminLogModel(String adminId, String action, String targetId) {
        this.adminId = adminId;
        this.action = action;
        this.targetId = targetId;
        this.details = new HashMap<>();
        this.timestamp = Timestamp.now();
    }

    /**
     * Constructor with details
     */
    public AdminLogModel(String adminId, String action, String targetId, Map<String, Object> details) {
        this.adminId = adminId;
        this.action = action;
        this.targetId = targetId;
        this.details = details;
        this.timestamp = Timestamp.now();
    }

    /**
     * Full constructor with all admin log properties
     */
    public AdminLogModel(String id, String adminId, String action, String targetId,
                       Map<String, Object> details, Timestamp timestamp) {
        this.id = id;
        this.adminId = adminId;
        this.action = action;
        this.targetId = targetId;
        this.details = details;
        this.timestamp = timestamp;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    /**
     * Add a detail to the admin log
     * @param key detail key
     * @param value detail value
     */
    public void addDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get a specific detail value
     * @param key the detail key
     * @return the detail value, or null if not found
     */
    public Object getDetailValue(String key) {
        if (details != null) {
            return details.get(key);
        }
        return null;
    }

    /**
     * Check if this log has additional details
     * @return true if details are present, false otherwise
     */
    public boolean hasDetails() {
        return details != null && !details.isEmpty();
    }

    /**
     * Create a log for user management actions
     * @param adminId the admin's ID
     * @param action the action performed (e.g., "ban_user", "delete_account")
     * @param userId the target user's ID
     * @param reason the reason for the action
     * @return a new AdminLogModel instance
     */
    public static AdminLogModel createUserActionLog(String adminId, String action, String userId, String reason) {
        AdminLogModel log = new AdminLogModel(adminId, action, userId);
        log.addDetail("reason", reason);
        return log;
    }

    /**
     * Create a log for content moderation actions
     * @param adminId the admin's ID
     * @param action the action performed (e.g., "remove_listing", "hide_review")
     * @param contentId the target content ID
     * @param contentType the type of content (e.g., "listing", "review")
     * @param reason the reason for the action
     * @return a new AdminLogModel instance
     */
    public static AdminLogModel createContentActionLog(String adminId, String action,
                                                    String contentId, String contentType, String reason) {
        AdminLogModel log = new AdminLogModel(adminId, action, contentId);
        log.addDetail("contentType", contentType);
        log.addDetail("reason", reason);
        return log;
    }
}
