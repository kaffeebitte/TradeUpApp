package com.example.tradeupapp.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ItemModel implements Parcelable {
    private String id;
    private String userId; // ID of the user who posted the item
    private String sellerId; // Explicit seller reference (same as userId for consistency)
    private String title;
    private String description;
    private double price;
    private double originalPrice; // Original listing price for discount tracking
    private String category;
    private String subcategory; // More specific categorization
    private String categoryId; // Reference to categories collection
    private String condition;
    private String location;
    private List<Uri> photoUris;
    private String status; // e.g. "Available", "Paused", "Sold"
    private int viewCount;
    private int interactionCount;
    private String tag; // Product tag for categorization
    private Date dateAdded; // Date when the item was added

    // Enhanced fields for marketplace functionality
    private double weight; // Weight in kg for shipping
    private Dimensions dimensions; // Size information
    private List<String> shippingOptions; // Available shipping methods
    private List<String> keyFeatures; // Searchable attributes
    private boolean isPromoted; // Paid promotions
    private Date promotionExpiry; // Promotion end date
    private boolean negotiable; // Price negotiation allowed

    // Add relationship field to connect with ListingModel
    private String listingId; // Reference to original ListingModel if applicable
    private String transactionId; // Reference to transaction that created this item record

    // Inner class for dimensions
    public static class Dimensions implements Parcelable {
        private double length;
        private double width;
        private double height;

        public Dimensions() {
        }

        public Dimensions(double length, double width, double height) {
            this.length = length;
            this.width = width;
            this.height = height;
        }

        protected Dimensions(Parcel in) {
            length = in.readDouble();
            width = in.readDouble();
            height = in.readDouble();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeDouble(length);
            dest.writeDouble(width);
            dest.writeDouble(height);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Dimensions> CREATOR = new Creator<Dimensions>() {
            @Override
            public Dimensions createFromParcel(Parcel in) {
                return new Dimensions(in);
            }

            @Override
            public Dimensions[] newArray(int size) {
                return new Dimensions[size];
            }
        };

        // Getters and setters
        public double getLength() {
            return length;
        }

        public void setLength(double length) {
            this.length = length;
        }

        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }
    }

    public ItemModel() {
        photoUris = new ArrayList<>();
        shippingOptions = new ArrayList<>();
        keyFeatures = new ArrayList<>();
    }

    public ItemModel(String title, String description, double price, String category,
                     String condition, String location, List<Uri> photoUris) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.location = location;
        this.photoUris = photoUris != null ? photoUris : new ArrayList<>();
    }

    // Parcelable implementation
    protected ItemModel(Parcel in) {
        id = in.readString();
        userId = in.readString();
        title = in.readString();
        description = in.readString();
        price = in.readDouble();
        category = in.readString();
        condition = in.readString();
        location = in.readString();
        photoUris = new ArrayList<>();
        in.readList(photoUris, Uri.class.getClassLoader());
        status = in.readString();
        viewCount = in.readInt();
        interactionCount = in.readInt();
        tag = in.readString();
        dateAdded = new Date(in.readLong());
        listingId = in.readString();
        transactionId = in.readString();
        sellerId = in.readString();
        originalPrice = in.readDouble();
        subcategory = in.readString();
        categoryId = in.readString();
        weight = in.readDouble();
        dimensions = in.readParcelable(Dimensions.class.getClassLoader());
        shippingOptions = in.createStringArrayList();
        keyFeatures = in.createStringArrayList();
        isPromoted = in.readByte() != 0;
        promotionExpiry = new Date(in.readLong());
        negotiable = in.readByte() != 0;
    }

    public static final Creator<ItemModel> CREATOR = new Creator<ItemModel>() {
        @Override
        public ItemModel createFromParcel(Parcel in) {
            return new ItemModel(in);
        }

        @Override
        public ItemModel[] newArray(int size) {
            return new ItemModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userId);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeDouble(price);
        dest.writeString(category);
        dest.writeString(condition);
        dest.writeString(location);
        dest.writeList(photoUris);
        dest.writeString(status);
        dest.writeInt(viewCount);
        dest.writeInt(interactionCount);
        dest.writeString(tag);
        dest.writeLong(dateAdded.getTime());
        dest.writeString(listingId);
        dest.writeString(transactionId);
        dest.writeString(sellerId);
        dest.writeDouble(originalPrice);
        dest.writeString(subcategory);
        dest.writeString(categoryId);
        dest.writeDouble(weight);
        dest.writeParcelable(dimensions, flags);
        dest.writeStringList(shippingOptions);
        dest.writeStringList(keyFeatures);
        dest.writeByte((byte) (isPromoted ? 1 : 0));
        dest.writeLong(promotionExpiry != null ? promotionExpiry.getTime() : -1);
        dest.writeByte((byte) (negotiable ? 1 : 0));
    }

    // Enhanced getters and setters
    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Dimensions getDimensions() {
        return dimensions;
    }

    public void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    public List<String> getShippingOptions() {
        return shippingOptions;
    }

    public void setShippingOptions(List<String> shippingOptions) {
        this.shippingOptions = shippingOptions;
    }

    public List<String> getKeyFeatures() {
        return keyFeatures;
    }

    public void setKeyFeatures(List<String> keyFeatures) {
        this.keyFeatures = keyFeatures;
    }

    public boolean isPromoted() {
        return isPromoted;
    }

    public void setPromoted(boolean promoted) {
        isPromoted = promoted;
    }

    public Date getPromotionExpiry() {
        return promotionExpiry;
    }

    public void setPromotionExpiry(Date promotionExpiry) {
        this.promotionExpiry = promotionExpiry;
    }

    public boolean isNegotiable() {
        return negotiable;
    }

    public void setNegotiable(boolean negotiable) {
        this.negotiable = negotiable;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<Uri> getPhotoUris() {
        return photoUris;
    }

    public void setPhotoUris(List<Uri> photoUris) {
        this.photoUris = photoUris;
    }

    public void addPhotoUri(Uri uri) {
        if (uri != null && !photoUris.contains(uri)) {
            photoUris.add(uri);
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getInteractionCount() {
        return interactionCount;
    }

    public void setInteractionCount(int interactionCount) {
        this.interactionCount = interactionCount;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
