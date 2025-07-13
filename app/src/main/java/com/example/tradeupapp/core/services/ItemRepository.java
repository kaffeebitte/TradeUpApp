package com.example.tradeupapp.core.services;

import com.example.tradeupapp.models.ItemModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ItemRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface ItemCallback {
        void onItemLoaded(ItemModel item);
    }

    public void getItemById(String itemId, ItemCallback callback) {
        db.collection("items").document(itemId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    ItemModel item = documentSnapshot.toObject(ItemModel.class);
                    callback.onItemLoaded(item);
                } else {
                    callback.onItemLoaded(null);
                }
            })
            .addOnFailureListener(e -> callback.onItemLoaded(null));
    }

    public interface ListingCallback {
        void onListingLoaded(com.example.tradeupapp.models.ListingModel listing);
    }

    public void getListingById(String listingId, ListingCallback callback) {
        db.collection("listings").document(listingId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    com.example.tradeupapp.models.ListingModel listing = documentSnapshot.toObject(com.example.tradeupapp.models.ListingModel.class);
                    callback.onListingLoaded(listing);
                } else {
                    callback.onListingLoaded(null);
                }
            })
            .addOnFailureListener(e -> callback.onListingLoaded(null));
    }
}
