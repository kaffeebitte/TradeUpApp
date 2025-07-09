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
}

