package com.example.tradeupapp.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemModel implements Parcelable {
    private String id;
    private String title;
    private String description;
    private String category;
    private String subcategory;
    private String brand;
    private String condition;
    private Map<String, Object> location;
    private List<String> photoUris;
    private double weight;
    private Dimensions dimensions;
    private List<String> shippingOptions;
    private List<String> keyFeatures;
    private java.util.Date dateAdded;
    private java.util.List<String> tags = new java.util.ArrayList<>();

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
        this.dateAdded = new java.util.Date();
    }

    // NOTE: price is not part of ItemModel. Use ListingModel for price and listing-specific data.
    // If you need price, fetch the related ListingModel using itemId.

    public ItemModel(String id, String title, String description, String category, String subcategory, String brand, String condition, Map<String, Object> location, List<String> photoUris, double weight, Dimensions dimensions, List<String> shippingOptions, List<String> keyFeatures, java.util.Date dateAdded) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.subcategory = subcategory;
        this.brand = brand;
        this.condition = condition;
        this.location = location;
        this.photoUris = photoUris != null ? photoUris : new ArrayList<>();
        this.weight = weight;
        this.dimensions = dimensions;
        this.shippingOptions = shippingOptions != null ? shippingOptions : new ArrayList<>();
        this.keyFeatures = keyFeatures != null ? keyFeatures : new ArrayList<>();
        this.dateAdded = dateAdded;
    }

    // Parcelable implementation
    protected ItemModel(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        category = in.readString();
        subcategory = in.readString();
        brand = in.readString();
        condition = in.readString();
        // Read location as Map
        location = (Map<String, Object>) in.readHashMap(Map.class.getClassLoader());
        photoUris = new ArrayList<>();
        in.readList(photoUris, String.class.getClassLoader());
        weight = in.readDouble();
        dimensions = in.readParcelable(Dimensions.class.getClassLoader());
        shippingOptions = in.createStringArrayList();
        keyFeatures = in.createStringArrayList();
        long tmpDate = in.readLong();
        this.dateAdded = tmpDate == -1 ? null : new java.util.Date(tmpDate);
        in.readList(tags, String.class.getClassLoader());
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
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(category);
        dest.writeString(subcategory);
        dest.writeString(brand);
        dest.writeString(condition);
        dest.writeMap(location);
        dest.writeList(photoUris);
        dest.writeDouble(weight);
        dest.writeParcelable(dimensions, flags);
        dest.writeStringList(shippingOptions);
        dest.writeStringList(keyFeatures);
        dest.writeLong(dateAdded != null ? dateAdded.getTime() : -1);
        dest.writeList(tags);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Map<String, Object> getLocation() {
        return location;
    }

    public void setLocation(Map<String, Object> location) {
        this.location = location;
    }

    // Convenience getters for latitude/longitude
    public Double getLocationLatitude() {
        if (location != null && location.get("_latitude") instanceof Number) {
            return ((Number) location.get("_latitude")).doubleValue();
        }
        return null;
    }

    public Double getLocationLongitude() {
        if (location != null && location.get("_longitude") instanceof Number) {
            return ((Number) location.get("_longitude")).doubleValue();
        }
        return null;
    }

    public String getAddress() {
        if (location != null && location.get("address") instanceof String) {
            return (String) location.get("address");
        }
        return null;
    }

    public List<String> getPhotoUris() {
        return photoUris;
    }

    public void setPhotoUris(List<String> photoUris) {
        this.photoUris = photoUris;
    }

    public void addPhotoUri(String uri) {
        if (uri != null && !photoUris.contains(uri)) {
            photoUris.add(uri);
        }
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

    public java.util.Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(java.util.Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public java.util.List<String> getTags() {
        return tags;
    }

    public void setTags(java.util.List<String> tags) {
        this.tags = tags;
    }
}
