package com.example.tradeupapp.core.services;

import androidx.annotation.NonNull;

import com.example.tradeupapp.models.CartItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartService {
    private static CartService instance;
    private final FirebaseFirestore db;
    private static final String CARTS_COLLECTION = "carts";

    private CartService() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized CartService getInstance() {
        if (instance == null) {
            instance = new CartService();
        }
        return instance;
    }

    public interface CartCallback {
        void onSuccess(List<CartItem> items);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }

    public void getCart(String userId, CartCallback callback) {
        db.collection(CARTS_COLLECTION).document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                List<CartItem> items = new ArrayList<>();
                if (documentSnapshot.exists() && documentSnapshot.contains("items")) {
                    List<String> listingIds = (List<String>) documentSnapshot.get("items");
                    if (listingIds != null) {
                        for (String id : listingIds) {
                            items.add(new CartItem(id));
                        }
                    }
                }
                callback.onSuccess(items);
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void addToCart(String userId, CartItem cartItem, SimpleCallback callback) {
        DocumentReference cartRef = db.collection(CARTS_COLLECTION).document(userId);
        cartRef.get().addOnSuccessListener(documentSnapshot -> {
            List<String> items = new ArrayList<>();
            if (documentSnapshot.exists() && documentSnapshot.contains("items")) {
                items = (List<String>) documentSnapshot.get("items");
                if (items == null) items = new ArrayList<>();
            }
            if (!items.contains(cartItem.getListingId())) {
                items.add(cartItem.getListingId());
            }
            Map<String, Object> cartData = new HashMap<>();
            cartData.put("items", items);
            cartRef.set(cartData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void removeFromCart(String userId, String listingId, SimpleCallback callback) {
        DocumentReference cartRef = db.collection(CARTS_COLLECTION).document(userId);
        cartRef.get().addOnSuccessListener(documentSnapshot -> {
            List<String> items = new ArrayList<>();
            if (documentSnapshot.exists() && documentSnapshot.contains("items")) {
                items = (List<String>) documentSnapshot.get("items");
                if (items == null) items = new ArrayList<>();
            }
            items.remove(listingId);
            Map<String, Object> cartData = new HashMap<>();
            cartData.put("items", items);
            cartRef.set(cartData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void clearCart(String userId, SimpleCallback callback) {
        db.collection(CARTS_COLLECTION).document(userId)
            .set(new HashMap<String, Object>() {{
                put("items", new ArrayList<String>());
            }}, SetOptions.merge())
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
