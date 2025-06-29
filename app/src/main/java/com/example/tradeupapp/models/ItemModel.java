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
    private String title;
    private String description;
    private double price;
    private String category;
    private String condition;
    private String location;
    private List<Uri> photoUris;
    private String status; // e.g. "Available", "Paused", "Sold"
    private int viewCount;
    private int interactionCount;
    private String tag; // Product tag for categorization
    private Date dateAdded; // Date when the item was added

    public ItemModel() {
        photoUris = new ArrayList<>();
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
}
