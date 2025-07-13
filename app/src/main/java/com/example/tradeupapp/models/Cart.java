package com.example.tradeupapp.models;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private String userId;
    private List<CartItem> items;

    public Cart() {
        items = new ArrayList<>();
    }

    public Cart(String userId, List<CartItem> items) {
        this.userId = userId;
        this.items = items;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public void addItem(CartItem item) {
        if (items == null) items = new ArrayList<>();
        items.add(item);
    }

    public void removeItem(String listingId) {
        if (items == null) return;
        items.removeIf(i -> i.getListingId().equals(listingId));
    }
}

