package com.example.tradeupapp.models;

public class CategoryModel {
    private String id;
    private String name;
    private int iconResourceId;
    private String backgroundColor;

    public CategoryModel() {
        // Default constructor
    }

    public CategoryModel(String id, String name, int iconResourceId) {
        this.id = id;
        this.name = name;
        this.iconResourceId = iconResourceId;
    }

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

    public int getIconResourceId() {
        return iconResourceId;
    }

    public void setIconResourceId(int iconResourceId) {
        this.iconResourceId = iconResourceId;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
