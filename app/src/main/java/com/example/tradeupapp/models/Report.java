package com.example.tradeupapp.models;

import java.util.Date;

public class Report {
    private String id;
    private String reporterId;
    private String reportedItemId;
    private String reportedUserId;
    private String reason;
    private String details;
    private Date createdAt;
    private String status; // "pending", "reviewed", "resolved"

    public Report() {
        // Empty constructor needed for Firestore
    }

    public Report(String reporterId, String reportedItemId, String reportedUserId, String reason, String details) {
        this.reporterId = reporterId;
        this.reportedItemId = reportedItemId;
        this.reportedUserId = reportedUserId;
        this.reason = reason;
        this.details = details;
        this.createdAt = new Date();
        this.status = "pending";
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

    public String getReportedItemId() {
        return reportedItemId;
    }

    public void setReportedItemId(String reportedItemId) {
        this.reportedItemId = reportedItemId;
    }

    public String getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(String reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
