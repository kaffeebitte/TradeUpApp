package com.example.tradeupapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

/**
 * Model representing a report/complaint in the TradeUpApp.
 * Contains information about user reports for violations or issues.
 */
public class ReportModel implements Serializable {

    // Enum for report reasons
    public enum Reason {
        SPAM("spam"),
        FRAUD("fraud"),
        ABUSE("abuse"),
        INAPPROPRIATE_CONTENT("inappropriate_content"),
        COUNTERFEIT("counterfeit"),
        OTHER("other");

        private final String value;

        Reason(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Reason fromString(String value) {
            for (Reason reason : Reason.values()) {
                if (reason.value.equals(value)) {
                    return reason;
                }
            }
            return OTHER; // Default reason
        }
    }

    // Enum for report status
    public enum Status {
        PENDING("pending"),
        REVIEWED("reviewed");

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

    private String id; // optional - Firestore auto ID
    private String reporterId; // required - user filing the report
    private String reportedUserId; // required - user being reported
    private String reportedItemId; // optional - listing being reported (if applicable)
    private String reportType; // optional - type of report
    private String reason; // required - "spam", "fraud", "abuse", etc.
    private String description; // optional - additional notes
    private String status; // required - "pending", "reviewed"
    private Timestamp createdAt; // required - when report was submitted
    private Timestamp resolvedAt; // optional - when report was resolved
    private String adminNotes; // optional - notes from admin
    private java.util.List<String> evidence; // optional - list of evidence URLs

    /**
     * Default constructor required for Firebase Firestore
     */
    public ReportModel() {
        // Required empty constructor for Firestore
        this.status = Status.PENDING.getValue();
        this.createdAt = Timestamp.now();
    }

    /**
     * Constructor for reporting a user (without specific listing)
     */
    public ReportModel(String reporterId, String reportedUserId, String reason, String description) {
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.reason = reason;
        this.description = description;
        this.status = Status.PENDING.getValue();
        this.createdAt = Timestamp.now();
    }

    /**
     * Constructor for reporting a user with enum reason
     */
    public ReportModel(String reporterId, String reportedUserId, Reason reason, String description) {
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.reason = reason.getValue();
        this.description = description;
        this.status = Status.PENDING.getValue();
        this.createdAt = Timestamp.now();
    }

    /**
     * Constructor for reporting a specific listing
     */
    public ReportModel(String reporterId, String reportedUserId, String reportedItemId,
                      String reason, String description) {
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.reportedItemId = reportedItemId;
        this.reason = reason;
        this.description = description;
        this.status = Status.PENDING.getValue();
        this.createdAt = Timestamp.now();
    }

    /**
     * Constructor for reporting a specific listing with enum reason
     */
    public ReportModel(String reporterId, String reportedUserId, String reportedItemId,
                      Reason reason, String description) {
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.reportedItemId = reportedItemId;
        this.reason = reason.getValue();
        this.description = description;
        this.status = Status.PENDING.getValue();
        this.createdAt = Timestamp.now();
    }

    /**
     * Full constructor with all report properties
     */
    public ReportModel(String id, String reporterId, String reportedUserId, String reportedItemId,
                     String reason, String description, String status, Timestamp createdAt, Timestamp resolvedAt,
                     String adminNotes, java.util.List<String> evidence) {
        this.id = id;
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.reportedItemId = reportedItemId;
        this.reason = reason;
        this.description = description;
        this.status = status != null ? status : Status.PENDING.getValue();
        this.createdAt = createdAt != null ? createdAt : Timestamp.now();
        this.resolvedAt = resolvedAt;
        this.adminNotes = adminNotes;
        this.evidence = evidence;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(String reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public String getReportedItemId() {
        return reportedItemId;
    }

    public void setReportedItemId(String reportedItemId) {
        this.reportedItemId = reportedItemId;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    // Helper methods for reason
    @Exclude // Not stored in Firestore, computed
    public void setReasonEnum(Reason reasonEnum) {
        this.reason = reasonEnum.getValue();
    }

    @Exclude // Not stored in Firestore, computed
    public Reason getReasonEnum() {
        return Reason.fromString(reason);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Timestamp getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Timestamp resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public java.util.List<String> getEvidence() {
        return evidence;
    }

    public void setEvidence(java.util.List<String> evidence) {
        this.evidence = evidence;
    }

    /**
     * Check if this report is for a listing
     * @return true if report includes a listing ID, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isListingReport() {
        return reportedItemId != null && !reportedItemId.isEmpty();
    }

    /**
     * Mark the report as reviewed
     */
    public void markAsReviewed() {
        this.status = Status.REVIEWED.getValue();
    }

    /**
     * Check if the report is pending review
     * @return true if status is "pending", false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isPending() {
        return Status.PENDING.getValue().equals(status);
    }

    /**
     * Check if the report has been reviewed
     * @return true if status is "reviewed", false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isReviewed() {
        return Status.REVIEWED.getValue().equals(status);
    }

    /**
     * Check if the report reason is spam
     * @return true if reason is "spam", false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isSpam() {
        return Reason.SPAM.getValue().equals(reason);
    }

    /**
     * Check if the report reason is fraud
     * @return true if reason is "fraud", false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isFraud() {
        return Reason.FRAUD.getValue().equals(reason);
    }

    /**
     * Check if the report reason is abuse
     * @return true if reason is "abuse", false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isAbuse() {
        return Reason.ABUSE.getValue().equals(reason);
    }

    /**
     * Get a user-friendly display of the reason
     * @return formatted reason text
     */
    @Exclude // Not stored in Firestore, computed
    public String getReasonDisplay() {
        Reason reasonEnum = getReasonEnum();
        switch (reasonEnum) {
            case SPAM: return "Spam";
            case FRAUD: return "Fraud or Scam";
            case ABUSE: return "Harassment or Abuse";
            case INAPPROPRIATE_CONTENT: return "Inappropriate Content";
            case COUNTERFEIT: return "Counterfeit Item";
            case OTHER: return "Other";
            default: return reason;
        }
    }
}
