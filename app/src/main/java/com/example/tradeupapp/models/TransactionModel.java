package com.example.tradeupapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

/**
 * Model representing a transaction in the TradeUpApp.
 * Contains information about completed sales between buyers and sellers.
 */
public class TransactionModel implements Serializable {

    // Enum for transaction status
    public enum Status {
        IN_PROGRESS("in_progress"),
        COMPLETED("completed"),
        CANCELLED("cancelled");

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
            return IN_PROGRESS; // Default status
        }
    }

    // Enum for payment methods
    public enum PaymentMethod {
        CASH("cash"),
        BANK_TRANSFER("bank_transfer");

        private final String value;

        PaymentMethod(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static PaymentMethod fromString(String value) {
            for (PaymentMethod method : PaymentMethod.values()) {
                if (method.value.equals(value)) {
                    return method;
                }
            }
            return CASH; // Default payment method
        }
    }

    @DocumentId
    private String id; // optional - Firestore auto ID
    private String listingId; // required - reference to the listing
    private String buyerId; // required - user buying the item
    private String sellerId; // required - user selling the item
    private double amount; // required - agreed price (was finalPrice)
    private String paymentMethod; // required - "cash", "bank_transfer"
    private String shippingMethod; // e.g., "pickup", "delivery"
    private double transactionFee; // transaction fee
    private double sellerEarnings; // seller's earnings after fee
    private String offerId; // optional - reference to offer
    private String address; // delivery address
    private String phone; // delivery phone
    private String status; // required - "in_progress", "completed", "cancelled"
    private Timestamp createdAt; // created time
    private Timestamp completedAt; // optional - only set when transaction is completed

    /**
     * Default constructor required for Firebase Firestore
     */
    public TransactionModel() {
        // Required empty constructor for Firestore
        this.status = Status.IN_PROGRESS.getValue();
    }

    /**
     * Constructor with essential transaction information
     */
    public TransactionModel(String listingId, String buyerId, String sellerId,
                          double amount, String paymentMethod) {
        this.listingId = listingId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = Status.IN_PROGRESS.getValue();
    }

    /**
     * Full constructor with all transaction properties
     */
    public TransactionModel(String id, String listingId, String buyerId, String sellerId,
                         double amount, String paymentMethod, String shippingMethod, double transactionFee, double sellerEarnings, String offerId, String address, String phone, String status, Timestamp createdAt, Timestamp completedAt) {
        this.id = id;
        this.listingId = listingId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.shippingMethod = shippingMethod;
        this.transactionFee = transactionFee;
        this.sellerEarnings = sellerEarnings;
        this.offerId = offerId;
        this.address = address;
        this.phone = phone;
        this.status = status != null ? status : Status.IN_PROGRESS.getValue();
        this.createdAt = createdAt;
        this.completedAt = completedAt;
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

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public double getTransactionFee() {
        return transactionFee;
    }

    public void setTransactionFee(double transactionFee) {
        this.transactionFee = transactionFee;
    }

    public double getSellerEarnings() {
        return sellerEarnings;
    }

    public void setSellerEarnings(double sellerEarnings) {
        this.sellerEarnings = sellerEarnings;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status : Status.IN_PROGRESS.getValue();
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    /**
     * Mark transaction as completed
     */
    public void complete() {
        this.status = Status.COMPLETED.getValue();
        this.completedAt = Timestamp.now();
    }

    /**
     * Mark transaction as cancelled
     */
    public void cancel() {
        this.status = Status.CANCELLED.getValue();
    }

    /**
     * Check if the transaction is completed
     * @return true if completed, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isCompleted() {
        return Status.COMPLETED.getValue().equals(this.status);
    }

    /**
     * Check if the transaction is cancelled
     * @return true if cancelled, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isCancelled() {
        return Status.CANCELLED.getValue().equals(this.status);
    }

    /**
     * Check if the transaction is in progress
     * @return true if in progress, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isInProgress() {
        return Status.IN_PROGRESS.getValue().equals(this.status);
    }
}
