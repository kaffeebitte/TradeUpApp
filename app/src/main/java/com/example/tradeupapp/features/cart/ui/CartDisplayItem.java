package com.example.tradeupapp.features.cart.ui;

import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;

public class CartDisplayItem {
    private final ListingModel listing;
    private final ItemModel item;

    public CartDisplayItem(ListingModel listing, ItemModel item) {
        this.listing = listing;
        this.item = item;
    }

    public ListingModel getListing() {
        return listing;
    }

    public ItemModel getItem() {
        return item;
    }
}

