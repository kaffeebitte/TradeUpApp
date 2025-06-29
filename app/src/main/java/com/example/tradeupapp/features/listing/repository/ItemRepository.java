package com.example.tradeupapp.features.listing.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tradeupapp.models.ItemModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for accessing item data from Firestore
 * Implements caching and proper error handling
 */
public class ItemRepository {
    private static final String COLLECTION_ITEMS = "items";
    private static ItemRepository instance;
    private final FirebaseFirestore firestore;

    // Cache for recently loaded items
    private final MutableLiveData<List<ItemModel>> recentItemsLiveData = new MutableLiveData<>(new ArrayList<>());

    private ItemRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public static synchronized ItemRepository getInstance() {
        if (instance == null) {
            instance = new ItemRepository();
        }
        return instance;
    }

    /**
     * Get a specific item by ID
     */
    public LiveData<ItemModel> getItem(String itemId) {
        MutableLiveData<ItemModel> itemLiveData = new MutableLiveData<>();

        firestore.collection(COLLECTION_ITEMS)
                .document(itemId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ItemModel item = documentSnapshot.toObject(ItemModel.class);
                        if (item != null) {
                            item.setId(documentSnapshot.getId());
                            itemLiveData.setValue(item);
                        } else {
                            itemLiveData.setValue(null);
                        }
                    } else {
                        itemLiveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> itemLiveData.setValue(null));

        return itemLiveData;
    }

    /**
     * Get recent items, limited to a specified count
     */
    public LiveData<List<ItemModel>> getRecentItems(int limit) {
        firestore.collection(COLLECTION_ITEMS)
                .orderBy("dateAdded", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ItemModel> items = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ItemModel item = document.toObject(ItemModel.class);
                        item.setId(document.getId());
                        items.add(item);
                    }
                    recentItemsLiveData.setValue(items);
                })
                .addOnFailureListener(e -> {
                    // If there's an error, keep the existing data
                    // This helps with offline support
                });

        return recentItemsLiveData;
    }

    /**
     * Get items by category
     */
    public LiveData<List<ItemModel>> getItemsByCategory(String category) {
        MutableLiveData<List<ItemModel>> categoryItemsLiveData = new MutableLiveData<>(new ArrayList<>());

        firestore.collection(COLLECTION_ITEMS)
                .whereEqualTo("category", category)
                .orderBy("dateAdded", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ItemModel> items = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ItemModel item = document.toObject(ItemModel.class);
                        item.setId(document.getId());
                        items.add(item);
                    }
                    categoryItemsLiveData.setValue(items);
                })
                .addOnFailureListener(e -> {
                    // Keep empty list on failure
                });

        return categoryItemsLiveData;
    }

    /**
     * Save a new item to Firestore
     */
    public LiveData<Boolean> saveItem(ItemModel item) {
        MutableLiveData<Boolean> successLiveData = new MutableLiveData<>();

        // Don't save if item is null
        if (item == null) {
            successLiveData.setValue(false);
            return successLiveData;
        }

        // If no ID, create a new document
        if (item.getId() == null || item.getId().isEmpty()) {
            firestore.collection(COLLECTION_ITEMS)
                    .add(item)
                    .addOnSuccessListener(documentReference -> {
                        // Update the item with the new ID
                        item.setId(documentReference.getId());
                        successLiveData.setValue(true);
                    })
                    .addOnFailureListener(e -> successLiveData.setValue(false));
        } else {
            // Update existing document
            firestore.collection(COLLECTION_ITEMS)
                    .document(item.getId())
                    .set(item)
                    .addOnSuccessListener(aVoid -> successLiveData.setValue(true))
                    .addOnFailureListener(e -> successLiveData.setValue(false));
        }

        return successLiveData;
    }

    /**
     * Search for items by title or description
     */
    public LiveData<List<ItemModel>> searchItems(String query) {
        MutableLiveData<List<ItemModel>> searchResultsLiveData = new MutableLiveData<>(new ArrayList<>());

        // Convert query to lowercase for case-insensitive search
        String lowerCaseQuery = query.toLowerCase();

        firestore.collection(COLLECTION_ITEMS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ItemModel> matchingItems = new ArrayList<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ItemModel item = document.toObject(ItemModel.class);
                        if (item != null) {
                            item.setId(document.getId());

                            // Check if title or description contains the query
                            if ((item.getTitle() != null && item.getTitle().toLowerCase().contains(lowerCaseQuery)) ||
                                (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerCaseQuery))) {
                                matchingItems.add(item);
                            }
                        }
                    }

                    searchResultsLiveData.setValue(matchingItems);
                })
                .addOnFailureListener(e -> {
                    // Keep empty list on failure
                });

        return searchResultsLiveData;
    }
}
