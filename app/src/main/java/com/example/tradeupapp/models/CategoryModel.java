package com.example.tradeupapp.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a product category in the TradeUpApp.
 * Contains information about item categories and their hierarchy.
 */
public class CategoryModel implements Serializable {
    @DocumentId
    private String id; // optional - Firestore auto ID
    private String name; // required - category name
    private String iconUrl; // optional - Cloudinary URL for category icon
    private String parentId; // optional - reference to parent category if this is a subcategory
    private boolean isActive; // required - visibility status (defaults to true)
    private List<CategoryModel> subcategories; // not stored in Firestore, computed at runtime

    @Exclude // Not stored in Firestore, used only for UI
    private int iconResourceId; // Resource ID for local drawables

    /**
     * Default constructor required for Firebase Firestore
     */
    public CategoryModel() {
        // Required empty constructor for Firestore
        this.subcategories = new ArrayList<>();
        this.isActive = true;
    }

    /**
     * Constructor for a main category (no parent)
     */
    public CategoryModel(String name, String iconUrl) {
        this.name = name;
        this.iconUrl = iconUrl;
        this.isActive = true;
        this.subcategories = new ArrayList<>();
    }

    /**
     * Constructor for a subcategory
     */
    public CategoryModel(String name, String iconUrl, String parentId) {
        this.name = name;
        this.iconUrl = iconUrl;
        this.parentId = parentId;
        this.isActive = true;
        this.subcategories = new ArrayList<>();
    }

    /**
     * Full constructor with all category properties
     */
    public CategoryModel(String id, String name, String iconUrl, String parentId, boolean isActive) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.parentId = parentId;
        this.isActive = isActive;
        this.subcategories = new ArrayList<>();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @PropertyName("isActive")
    public boolean isActive() {
        return isActive;
    }

    @PropertyName("isActive")
    public void setActive(boolean active) {
        isActive = active;
    }

    @Exclude // Not stored in Firestore, computed at runtime
    public List<CategoryModel> getSubcategories() {
        if (subcategories == null) {
            subcategories = new ArrayList<>();
        }
        return subcategories;
    }

    @Exclude // Not stored in Firestore, computed at runtime
    public void setSubcategories(List<CategoryModel> subcategories) {
        this.subcategories = subcategories != null ? subcategories : new ArrayList<>();
    }

    /**
     * Add a subcategory to this category
     * @param subcategory the subcategory to add
     */
    @Exclude // Not stored in Firestore, computed at runtime
    public void addSubcategory(CategoryModel subcategory) {
        if (this.subcategories == null) {
            this.subcategories = new ArrayList<>();
        }
        subcategory.setParentId(this.id);
        this.subcategories.add(subcategory);
    }

    /**
     * Check if this category is a main category (has no parent)
     * @return true if this is a main category, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean isMainCategory() {
        return parentId == null || parentId.isEmpty();
    }

    /**
     * Check if this category has subcategories
     * @return true if this category has subcategories, false otherwise
     */
    @Exclude // Not stored in Firestore, computed
    public boolean hasSubcategories() {
        return subcategories != null && !subcategories.isEmpty();
    }

    /**
     * Activate the category to make it visible
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivate the category to hide it
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Get the count of subcategories
     * @return the number of subcategories
     */
    @Exclude // Not stored in Firestore, computed
    public int getSubcategoryCount() {
        return subcategories != null ? subcategories.size() : 0;
    }

    /**
     * Get the full path of this category (e.g. "Electronics > Smartphones")
     * @param categories list of all categories to find parent names
     * @return the full path of this category
     */
    @Exclude // Not stored in Firestore, computed
    public String getFullPath(List<CategoryModel> categories) {
        if (isMainCategory()) {
            return name;
        }

        // Find parent category
        CategoryModel parent = null;
        for (CategoryModel category : categories) {
            if (category.getId() != null && category.getId().equals(parentId)) {
                parent = category;
                break;
            }
        }

        if (parent != null) {
            return parent.getFullPath(categories) + " > " + name;
        } else {
            return name;
        }
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public void setIconResourceId(int iconResourceId) {
        this.iconResourceId = iconResourceId;
    }
}
