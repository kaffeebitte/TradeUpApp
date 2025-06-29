package com.example.tradeupapp.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model class representing an offer made on an item
 */
public class OfferModel {
    private String offerId;
    private String itemId;
    private String sellerId;
    private String buyerId;
    private double offerAmount;
    private String message;
    private String status; // "pending", "accepted", "rejected", "canceled"
    private Date timestamp;
    private String itemTitle;
    private double itemPrice;

    // Empty constructor required for Firestore
    public OfferModel() {
    }

    // Constructor with all fields
    public OfferModel(String offerId, String itemId, String sellerId, String buyerId,
                      double offerAmount, String message, String status, Date timestamp,
                      String itemTitle, double itemPrice) {
        this.offerId = offerId;
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.offerAmount = offerAmount;
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
        this.itemTitle = itemTitle;
        this.itemPrice = itemPrice;
    }

    // Getters and setters
    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
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
        this.status = status;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }
}
