package com.example.tradeupapp.models;

public class CartItem {
    private String listingId;

    public CartItem() {}
    public CartItem(String listingId) {
        this.listingId = listingId;
    }
    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }
}
