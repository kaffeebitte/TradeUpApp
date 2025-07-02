package com.example.tradeupapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
    private double finalPrice; // required - agreed price
    private String paymentMethod; // required - "cash", "bank_transfer"
    private Map<String, String> deliveryInfo; // required - name, address, phone for delivery
    private String status; // required - "in_progress", "completed", "cancelled"
    private Timestamp completedAt; // optional - only set when transaction is completed

    /**
     * Default constructor required for Firebase Firestore
     */
    public TransactionModel() {
        // Required empty constructor for Firestore
        this.deliveryInfo = new HashMap<>();
        this.status = Status.IN_PROGRESS.getValue();
    }

    /**
     * Constructor with essential transaction information
     */
    public TransactionModel(String listingId, String buyerId, String sellerId,
                          double finalPrice, String paymentMethod) {
        this.listingId = listingId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.finalPrice = finalPrice;
        this.paymentMethod = paymentMethod;
        this.deliveryInfo = new HashMap<>();
        this.status = Status.IN_PROGRESS.getValue();
    }

    /**
     * Full constructor with all transaction properties
     */
    public TransactionModel(String id, String listingId, String buyerId, String sellerId,
                         double finalPrice, String paymentMethod, Map<String, String> deliveryInfo,
                         String status, Timestamp completedAt) {
        this.id = id;
        this.listingId = listingId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.finalPrice = finalPrice;
        this.paymentMethod = paymentMethod;
        this.deliveryInfo = deliveryInfo != null ? deliveryInfo : new HashMap<>();
        this.status = status != null ? status : Status.IN_PROGRESS.getValue();
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

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // Helper methods for payment method
    @Exclude // Not stored in Firestore, computed
    public void setPaymentMethodEnum(PaymentMethod methodEnum) {
        this.paymentMethod = methodEnum.getValue();
    }

    @Exclude // Not stored in Firestore, computed
    public PaymentMethod getPaymentMethodEnum() {
        return PaymentMethod.fromString(paymentMethod);
    }

    public Map<String, String> getDeliveryInfo() {
        if (deliveryInfo == null) {
            deliveryInfo = new HashMap<>();
        }
        return deliveryInfo;
    }

    public void setDeliveryInfo(Map<String, String> deliveryInfo) {
        this.deliveryInfo = deliveryInfo != null ? deliveryInfo : new HashMap<>();
    }

    /**
     * Set a specific delivery information field
     * @param key field name (e.g., "name", "address", "phone")
     * @param value field value
     */
    public void setDeliveryInfoField(String key, String value) {
        if (this.deliveryInfo == null) {
            this.deliveryInfo = new HashMap<>();
        }
        this.deliveryInfo.put(key, value);
    }

    /**
     * Get a specific delivery information field
     * @param key field name (e.g., "name", "address", "phone")
     * @return value of the field, or null if not found
     */
    @Exclude // Not stored in Firestore, computed
    public String getDeliveryInfoField(String key) {
        if (this.deliveryInfo == null) {
            return null;
        }
        return this.deliveryInfo.get(key);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status : Status.IN_PROGRESS.getValue();
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

    /**
     * Check if delivery information is complete
     * @return true if all required delivery fields are present
     */
    @Exclude // Not stored in Firestore, computed
    public boolean hasCompleteDeliveryInfo() {
        if (deliveryInfo == null) {
            return false;
        }
        // Check for required delivery fields
        return deliveryInfo.containsKey("name") &&
               deliveryInfo.containsKey("address") &&
               deliveryInfo.containsKey("phone");
    }
}
