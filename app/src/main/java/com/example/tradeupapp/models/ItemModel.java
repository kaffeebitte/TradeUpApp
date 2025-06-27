package com.example.tradeupapp.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ItemModel implements Parcelable {
    private String title;
    private String description;
    private double price;
    private String category;
    private String condition;
    private String location;
    private List<Uri> photoUris;

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
        title = in.readString();
        description = in.readString();
        price = in.readDouble();
        category = in.readString();
        condition = in.readString();
        location = in.readString();
        photoUris = new ArrayList<>();
        in.readList(photoUris, Uri.class.getClassLoader());
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
        dest.writeString(title);
        dest.writeString(description);
        dest.writeDouble(price);
        dest.writeString(category);
        dest.writeString(condition);
        dest.writeString(location);
        dest.writeList(photoUris);
    }

    // Getters and Setters
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
}
