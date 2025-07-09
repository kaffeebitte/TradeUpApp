package com.example.tradeupapp.core.services;

import android.net.Uri;
import android.util.Log;

import com.example.tradeupapp.models.CategoryModel;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.models.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service class for handling Firebase operations
 */
public class FirebaseService {
    private static final String TAG = "FirebaseService";
    private static FirebaseService instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private FirebaseService() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }

    // Get all items - simplified query that doesn't require composite index
    public void getAllItems(ItemsCallback callback) {
        // First try the optimized query with index
        db.collection("items")
                .whereEqualTo("status", "Available")
                .orderBy("dateAdded", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ItemModel> items = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ItemModel item = documentToItemModel(document);
                        if (item != null) {
                            items.add(item);
                        }
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Indexed query failed, falling back to simple query", e);
                    // Fallback to simple query without ordering
                    getAllItemsSimple(callback);
                });
    }

    // Fallback method for getting all items without complex indexing
    private void getAllItemsSimple(ItemsCallback callback) {
        db.collection("items")
                .whereEqualTo("status", "Available")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ItemModel> items = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ItemModel item = documentToItemModel(document);
                        if (item != null) {
                            items.add(item);
                        }
                    }
                    // Remove sorting by getDateAdded() since ItemModel no longer has this field
                    callback.onSuccess(items);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting items", e);
                    callback.onError(e.getMessage());
                });
    }

    // Get all active categories - simplified query
    @SuppressWarnings("unchecked")
    public void getAllCategories(CategoriesCallback callback) {
        db.collection("categories")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CategoryModel> categories = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        CategoryModel category = documentToCategoryModel(document);
                        if (category != null) {
                            categories.add(category);
                        }
                    }
                    // Sort manually by name on client side
                    categories.sort((a, b) -> {
                        if (a.getName() == null && b.getName() == null) return 0;
                        if (a.getName() == null) return 1;
                        if (b.getName() == null) return -1;
                        return a.getName().compareToIgnoreCase(b.getName());
                    });
                    callback.onSuccess(categories);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting categories", e);
                    callback.onError(e.getMessage());
                });
    }

    // Get items by category - simplified query
    public void getItemsByCategory(String categoryName, ItemsCallback callback) {
        // First try the optimized query with index
        db.collection("items")
                .whereEqualTo("category", categoryName)
                .whereEqualTo("status", "Available")
                .orderBy("dateAdded", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ItemModel> items = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ItemModel item = documentToItemModel(document);
                        if (item != null) {
                            items.add(item);
                        }
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Indexed category query failed, falling back to simple query", e);
                    // Fallback to simple query
                    getItemsByCategorySimple(categoryName, callback);
                });
    }

    // Fallback method for getting items by category
    private void getItemsByCategorySimple(String categoryName, ItemsCallback callback) {
        db.collection("items")
                .whereEqualTo("category", categoryName)
                .whereEqualTo("status", "Available")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ItemModel> items = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ItemModel item = documentToItemModel(document);
                        if (item != null) {
                            items.add(item);
                        }
                    }
                    // Sort manually by date on client side
                    items.sort((a, b) -> {
                        if (a.getDateAdded() == null && b.getDateAdded() == null) return 0;
                        if (a.getDateAdded() == null) return 1;
                        if (b.getDateAdded() == null) return -1;
                        return b.getDateAdded().compareTo(a.getDateAdded());
                    });
                    callback.onSuccess(items);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting items by category", e);
                    callback.onError(e.getMessage());
                });
    }

    // Get item by ID
    public void getItemById(String itemId, ItemCallback callback) {
        db.collection("items")
                .document(itemId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ItemModel item = documentToItemModel(documentSnapshot);
                        if (item != null) {
                            callback.onSuccess(item);
                        } else {
                            callback.onError("Failed to parse item data");
                        }
                    } else {
                        callback.onError("Item not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting item by ID", e);
                    callback.onError(e.getMessage());
                });
    }

    // Get user's items
    public void getUserItems(String userId, ItemsCallback callback) {
        db.collection("items")
                .whereEqualTo("userId", userId)
                .orderBy("dateAdded", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ItemModel> items = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ItemModel item = documentToItemModel(document);
                        if (item != null) {
                            items.add(item);
                        }
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user items", e);
                    callback.onError(e.getMessage());
                });
    }

    // Get user's purchased items (items that the user has bought)
    public void getUserPurchasedItems(String userId, ItemsCallback callback) {
        db.collection("items")
                .whereEqualTo("buyerId", userId)
                .whereIn("status", java.util.Arrays.asList("Purchased", "Delivered", "Shipped", "Completed"))
                .orderBy("dateAdded", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ItemModel> items = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ItemModel item = documentToItemModel(document);
                        if (item != null) {
                            items.add(item);
                        }
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting purchased items", e);
                    callback.onError(e.getMessage());
                });
    }

    // Get items by seller
    public void getItemsBySeller(String sellerId, ItemsCallback callback) {
        db.collection("items")
                .whereEqualTo("userId", sellerId)
                .orderBy("dateAdded", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ItemModel> items = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ItemModel item = documentToItemModel(document);
                        if (item != null) {
                            items.add(item);
                        }
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting seller items", e);
                    callback.onError(e.getMessage());
                });
    }

    // Get notifications for current user
    public void getUserNotifications(NotificationsCallback callback) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("User not authenticated");
            return;
        }

        db.collection("notifications")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<NotificationModel> notifications = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        NotificationModel notification = documentToNotificationModel(document);
                        if (notification != null) {
                            notifications.add(notification);
                        }
                    }
                    callback.onSuccess(notifications);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting notifications", e);
                    callback.onError(e.getMessage());
                });
    }

    // Search items
    public void searchItems(String query, ItemsCallback callback) {
        db.collection("items")
                .whereEqualTo("status", "Available")
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ItemModel> items = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ItemModel item = documentToItemModel(document);
                        if (item != null) {
                            items.add(item);
                        }
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching items", e);
                    callback.onError(e.getMessage());
                });
    }

    // Search items with filters - simplified with fallback
    public void searchItemsWithFilters(String query, String category, double minPrice,
                                       double maxPrice, String condition, String sortBy,
                                       ItemsCallback callback) {
        // Start with a simple query and apply filters client-side for now
        Query firestoreQuery = db.collection("items")
                .whereEqualTo("status", "Available");

        // Only apply one server-side filter to avoid index requirements
        if (category != null && !category.equals("All Categories") && !category.isEmpty()) {
            firestoreQuery = firestoreQuery.whereEqualTo("category", category);
        }

        firestoreQuery.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ItemModel> items = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ItemModel item = documentToItemModel(document);
                        if (item != null) {
                            // Apply client-side filters
                            boolean matchesFilters = true;

                            // Text search filter
                            if (query != null && !query.isEmpty() && !matchesSearchQuery(item, query)) {
                                matchesFilters = false;
                            }

                            // Condition filter
                            if (matchesFilters && condition != null && !condition.equals("Any Condition") && !condition.isEmpty()) {
                                if (!condition.equalsIgnoreCase(item.getCondition())) {
                                    matchesFilters = false;
                                }
                            }

                            if (matchesFilters) {
                                items.add(item);
                            }
                        }
                    }

                    // Apply sorting client-side
                    if (sortBy != null) {
                        switch (sortBy) {
                            case "Price: Low to High":
                            case "Price: High to Low":
                                // Price sorting is not supported here because price is in ListingModel, not ItemModel
                                break;
                            case "Newest First":
                            default:
                                items.sort((a, b) -> {
                                    if (a.getDateAdded() == null && b.getDateAdded() == null) return 0;
                                    if (a.getDateAdded() == null) return 1;
                                    if (b.getDateAdded() == null) return -1;
                                    return b.getDateAdded().compareTo(a.getDateAdded());
                                });
                                break;
                        }
                    }

                    callback.onSuccess(items);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching items with filters", e);
                    callback.onError(e.getMessage());
                });
    }

    // Search listings with filters
    public void searchListingsWithFilters(String keyword, String category, double minPrice, double maxPrice, String condition, String sortBy, ListingsCallback callback) {
        Query query = db.collection("listings");
        if (category != null && !category.equals("All Categories") && !category.isEmpty()) {
            query = query.whereEqualTo("category", category);
        }
        if (minPrice > 0) {
            query = query.whereGreaterThanOrEqualTo("price", minPrice);
        }
        if (maxPrice > 0 && maxPrice >= minPrice) {
            query = query.whereLessThanOrEqualTo("price", maxPrice);
        }
        if (condition != null && !condition.equals("Any Condition") && !condition.isEmpty()) {
            query = query.whereEqualTo("condition", condition);
        }
        // Default sort by createdAt desc
        query = query.orderBy("createdAt", Query.Direction.DESCENDING);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<ListingModel> listings = new ArrayList<>();
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                ListingModel listing = document.toObject(ListingModel.class);
                if (listing != null) {
                    // Keyword filter (client-side) using ItemModel title
                    if (keyword == null || keyword.isEmpty()) {
                        listings.add(listing);
                    } else {
                        // Fetch the related ItemModel synchronously is not possible, so skip keyword filter here
                        // You should filter listings by keyword after fetching related ItemModel in your UI code
                        listings.add(listing);
                    }
                }
            }
            // Additional sort options (client-side)
            if (sortBy != null) {
                switch (sortBy) {
                    case "Price: Low to High":
                        listings.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
                        break;
                    case "Price: High to Low":
                        listings.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                        break;
                    case "Newest First":
                    default:
                        // Already sorted by createdAt desc
                        break;
                }
            }
            callback.onSuccess(listings);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error searching listings with filters", e);
            callback.onError(e.getMessage());
        });
    }

    // Helper method to check if item matches search query
    private boolean matchesSearchQuery(ItemModel item, String query) {
        if (query == null || query.isEmpty()) {
            return true;
        }
        String lowerQuery = query.toLowerCase();
        return (item.getTitle() != null && item.getTitle().toLowerCase().contains(lowerQuery)) ||
                (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerQuery)) ||
                (item.getCategory() != null && item.getCategory().toLowerCase().contains(lowerQuery)) ||
                (item.getSubcategory() != null && item.getSubcategory().toLowerCase().contains(lowerQuery)) ||
                (item.getBrand() != null && item.getBrand().toLowerCase().contains(lowerQuery));
    }

    // Helper method to convert Firestore document to ItemModel
    private ItemModel documentToItemModel(DocumentSnapshot document) {
        try {
            ItemModel item = new ItemModel();
            item.setId(document.getId());
            item.setTitle(document.getString("title"));
            item.setDescription(document.getString("description"));
            item.setCategory(document.getString("category"));
            item.setSubcategory(document.getString("subcategory"));
            item.setBrand(document.getString("brand"));
            item.setCondition(document.getString("condition"));
            item.setLocation(document.getString("location"));
            Object weightObj = document.get("weight");
            // If you need to access ListingModel for price, you can fetch it here:
            // Example (pseudo-code, async):
            // String listingId = document.getString("listingId");
            // if (listingId != null) {
            //     db.collection("listings").document(listingId).get().addOnSuccessListener(listingDoc -> {
            //         ListingModel listing = listingDoc.toObject(ListingModel.class);
            //         if (listing != null) {
            //             double price = listing.getPrice();
            //             // Use price as needed
            //         }
            //     });
            // }
            if (weightObj instanceof Number) {
                item.setWeight(((Number) weightObj).doubleValue());
            }
            Map<String, Object> dimensionsMap = (Map<String, Object>) document.get("dimensions");
            if (dimensionsMap != null) {
                ItemModel.Dimensions dimensions = new ItemModel.Dimensions();
                if (dimensionsMap.get("length") instanceof Number) {
                    dimensions.setLength(((Number) dimensionsMap.get("length")).doubleValue());
                }
                if (dimensionsMap.get("width") instanceof Number) {
                    dimensions.setWidth(((Number) dimensionsMap.get("width")).doubleValue());
                }
                if (dimensionsMap.get("height") instanceof Number) {
                    dimensions.setHeight(((Number) dimensionsMap.get("height")).doubleValue());
                }
                item.setDimensions(dimensions);
            }
            @SuppressWarnings("unchecked")
            List<String> shippingOptions = (List<String>) document.get("shippingOptions");
            if (shippingOptions != null) {
                item.setShippingOptions(new ArrayList<>(shippingOptions));
            }
            @SuppressWarnings("unchecked")
            List<String> keyFeatures = (List<String>) document.get("keyFeatures");
            if (keyFeatures != null) {
                item.setKeyFeatures(new ArrayList<>(keyFeatures));
            }
            @SuppressWarnings("unchecked")
            List<String> photoUrls = (List<String>) document.get("photoUris");
            if (photoUrls != null) {
                item.setPhotoUris(new ArrayList<>(photoUrls));
            }
            Object dateAddedField = document.get("dateAdded");
            if (dateAddedField instanceof com.google.firebase.Timestamp) {
                com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) dateAddedField;
                item.setDateAdded(timestamp.toDate());
            } else if (dateAddedField instanceof String) {
                String dateString = (String) dateAddedField;
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US);
                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                    item.setDateAdded(sdf.parse(dateString));
                } catch (Exception e) {
                    item.setDateAdded(new java.util.Date());
                }
            } else {
                item.setDateAdded(new java.util.Date());
            }

            return item;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to ItemModel", e);
            return null;
        }
    }

    // Helper method to convert Firestore document to NotificationModel
    private NotificationModel documentToNotificationModel(DocumentSnapshot document) {
        try {
            String id = document.getId();
            String userId = document.getString("userId");
            String type = document.getString("type");
            String title = document.getString("title");
            String body = document.getString("body");

            com.google.firebase.Timestamp timestamp = document.getTimestamp("createdAt");

            Boolean isRead = document.getBoolean("read");
            boolean read = isRead != null ? isRead : false;

            String relatedId = document.getString("relatedId");

            // Use the full constructor that matches NotificationModel
            return new NotificationModel(id, userId, title, body, type, read, timestamp, relatedId);
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to NotificationModel", e);
            return null;
        }
    }

    // Helper method to convert Firestore document to CategoryModel
    private CategoryModel documentToCategoryModel(DocumentSnapshot document) {
        try {
            CategoryModel category = new CategoryModel();
            category.setId(document.getId());
            category.setName(document.getString("name"));
            category.setIconUrl(document.getString("iconUrl"));
            category.setParentId(document.getString("parentId"));

            Boolean isActive = document.getBoolean("isActive");
            category.setActive(isActive != null ? isActive : true);

            return category;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to CategoryModel", e);
            return null;
        }
    }

    // Get current user ID
    public String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    // Get user by ID
    public void getUserById(String userId, UserCallback callback) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    com.example.tradeupapp.models.User user = documentSnapshot.toObject(com.example.tradeupapp.models.User.class);
                    callback.onSuccess(user);
                } else {
                    callback.onError("User not found");
                }
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Get listings by sellerId
    public void getListingsBySellerId(String sellerId, ListingsCallback callback) {
        db.collection("listings")
                .whereEqualTo("sellerId", sellerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ListingModel> listings = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ListingModel listing = document.toObject(ListingModel.class);
                        if (listing != null) {
                            listings.add(listing);
                        }
                    }
                    callback.onSuccess(listings);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting listings by sellerId", e);
                    callback.onError(e.getMessage());
                });
    }

    // Get all listings (for showing in all listings info in the app)
    public void getAllListings(ListingsCallback callback) {
        db.collection("listings")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ListingModel> listings = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ListingModel listing = document.toObject(ListingModel.class);
                        if (listing != null) {
                            listings.add(listing);
                        }
                    }
                    callback.onSuccess(listings);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting all listings", e);
                    callback.onError(e.getMessage());
                });
    }
    // To show item details with each listing, fetch the related ItemModel using listing.getItemId()
    // Example (pseudo-code):
    // for (ListingModel listing : listings) {
    //     db.collection("items").document(listing.getItemId()).get().addOnSuccessListener(itemDoc -> {
    //         ItemModel item = itemDoc.toObject(ItemModel.class);
    //         // Combine listing and item info as needed
    //     });
    // }

    // Get listings by category
    public void getListingsByCategory(String category, ListingsCallback callback) {
        db.collection("listings")
                .whereEqualTo("category", category)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ListingModel> listings = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ListingModel listing = document.toObject(ListingModel.class);
                        if (listing != null) {
                            listings.add(listing);
                        }
                    }
                    callback.onSuccess(listings);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting listings by category", e);
                    callback.onError(e.getMessage());
                });
    }

    // Callback interfaces
    public interface ItemCallback {
        void onSuccess(ItemModel item);
        void onError(String error);
    }

    public interface ItemsCallback {
        void onSuccess(List<ItemModel> items);
        void onError(String error);
    }

    public interface NotificationsCallback {
        void onSuccess(List<NotificationModel> notifications);
        void onError(String error);
    }

    public interface CategoriesCallback {
        void onSuccess(List<CategoryModel> categories);
        void onError(String error);
    }

    public interface UserCallback {
        void onSuccess(com.example.tradeupapp.models.User user);
        void onError(String error);
    }

    public interface ListingsCallback {
        void onSuccess(List<ListingModel> listings);
        void onError(String error);
    }

    // Get all purchased listings for a user by querying the transaction collection
    public void getUserPurchasedListings(String userId, ListingsCallback callback) {
        db.collection("transaction")
            .whereEqualTo("buyerId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<String> listingIds = new ArrayList<>();
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    String listingId = document.getString("listingId");
                    if (listingId != null) {
                        listingIds.add(listingId);
                    }
                }
                if (listingIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }
                // Fetch all listings by their IDs
                db.collection("listings")
                    .whereIn("id", listingIds)
                    .get()
                    .addOnSuccessListener(listingSnapshots -> {
                        List<ListingModel> listings = new ArrayList<>();
                        for (DocumentSnapshot doc : listingSnapshots) {
                            ListingModel listing = doc.toObject(ListingModel.class);
                            if (listing != null) {
                                listing.setId(doc.getId());
                                listings.add(listing);
                            }
                        }
                        callback.onSuccess(listings);
                    })
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Callback for fetching multiple items by IDs
    public interface ItemsByIdsCallback {
        void onSuccess(java.util.Map<String, ItemModel> itemMap);
        void onError(String error);
    }

    // Fetch multiple items by their IDs and return as a map (itemId -> ItemModel)
    public void getItemsByIds(List<String> itemIds, ItemsByIdsCallback callback) {
        if (itemIds == null || itemIds.isEmpty()) {
            callback.onSuccess(new java.util.HashMap<>());
            return;
        }
        db.collection("items")
                .whereIn("id", itemIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    java.util.Map<String, ItemModel> itemMap = new java.util.HashMap<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ItemModel item = documentToItemModel(document);
                        if (item != null && item.getId() != null) {
                            itemMap.put(item.getId(), item);
                        }
                    }
                    callback.onSuccess(itemMap);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
