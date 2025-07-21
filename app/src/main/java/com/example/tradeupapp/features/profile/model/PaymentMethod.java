package com.example.tradeupapp.features.profile.model;

public class PaymentMethod {
    private String id;
    private String type; // e.g. "card", "upi", "wallet"
    private String displayName; // e.g. "Visa ****1234", "PhonePe"
    private String details; // e.g. last4, upiId, walletId
    private boolean isDefault;

    public PaymentMethod() {}

    public PaymentMethod(String id, String type, String displayName, String details, boolean isDefault) {
        this.id = id;
        this.type = type;
        this.displayName = displayName;
        this.details = details;
        this.isDefault = isDefault;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
}

