package com.example.tradeupapp.core.services;

import android.net.Uri;
import android.util.Log;

import com.example.tradeupapp.models.CategoryModel;
import com.example.tradeupapp.models.ItemModel;
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

                            // Price range filter
                            if (matchesFilters && minPrice > 0 && item.getPrice() < minPrice) {
                                matchesFilters = false;
                            }
                            if (matchesFilters && maxPrice < Double.MAX_VALUE && item.getPrice() > maxPrice) {
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
                                items.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
                                break;
                            case "Price: High to Low":
                                items.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
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

    // Helper method to check if item matches search query
    private boolean matchesSearchQuery(ItemModel item, String query) {
        if (query == null || query.isEmpty()) {
            return true;
        }

        String lowerQuery = query.toLowerCase();
        return (item.getTitle() != null && item.getTitle().toLowerCase().contains(lowerQuery)) ||
                (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerQuery)) ||
                (item.getCategory() != null && item.getCategory().toLowerCase().contains(lowerQuery)) ||
                (item.getTag() != null && item.getTag().toLowerCase().contains(lowerQuery));
    }

    // Helper method to convert Firestore document to ItemModel
    private ItemModel documentToItemModel(DocumentSnapshot document) {
        try {
            ItemModel item = new ItemModel();
            item.setId(document.getId());
            item.setUserId(document.getString("userId"));
            item.setSellerId(document.getString("sellerId")); // Use explicit sellerId or fallback to userId
            if (item.getSellerId() == null) {
                item.setSellerId(item.getUserId());
            }

            item.setTitle(document.getString("title"));
            item.setDescription(document.getString("description"));

            // Handle price conversion
            Object priceObj = document.get("price");
            if (priceObj instanceof Number) {
                item.setPrice(((Number) priceObj).doubleValue());
            }

            Object originalPriceObj = document.get("originalPrice");
            if (originalPriceObj instanceof Number) {
                item.setOriginalPrice(((Number) originalPriceObj).doubleValue());
            }

            item.setCategory(document.getString("category"));
            item.setSubcategory(document.getString("subcategory"));
            item.setCategoryId(document.getString("categoryId"));
            item.setCondition(document.getString("condition"));
            item.setLocation(document.getString("location"));
            item.setStatus(document.getString("status"));
            item.setTag(document.getString("tag"));

            // Handle view count and interaction count
            Object viewCountObj = document.get("viewCount");
            if (viewCountObj instanceof Number) {
                item.setViewCount(((Number) viewCountObj).intValue());
            }

            Object interactionCountObj = document.get("interactionCount");
            if (interactionCountObj instanceof Number) {
                item.setInteractionCount(((Number) interactionCountObj).intValue());
            }

            // Handle enhanced fields
            Object weightObj = document.get("weight");
            if (weightObj instanceof Number) {
                item.setWeight(((Number) weightObj).doubleValue());
            }

            // Handle dimensions
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

            // Handle shipping options and key features
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

            // Handle boolean fields
            Boolean isPromoted = document.getBoolean("isPromoted");
            item.setPromoted(isPromoted != null ? isPromoted : false);

            Boolean negotiable = document.getBoolean("negotiable");
            item.setNegotiable(negotiable != null ? negotiable : true);

            // FIX: Handle photo URIs - convert String URLs to Uri objects
            @SuppressWarnings("unchecked")
            List<String> photoUrls = (List<String>) document.get("photoUris");
            if (photoUrls != null) {
                List<Uri> photoUris = new ArrayList<>();
                for (String url : photoUrls) {
                    if (url != null && !url.isEmpty()) {
                        photoUris.add(Uri.parse(url));
                    }
                }
                item.setPhotoUris(photoUris);
            }

            // FIX: Handle date conversion properly - check field type first
            Object dateAddedField = document.get("dateAdded");
            if (dateAddedField instanceof com.google.firebase.Timestamp) {
                com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) dateAddedField;
                item.setDateAdded(timestamp.toDate());
            } else if (dateAddedField instanceof String) {
                String dateString = (String) dateAddedField;
                try {
                    // Use SimpleDateFormat for API level compatibility
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US);
                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                    item.setDateAdded(sdf.parse(dateString));
                } catch (Exception e) {
                    item.setDateAdded(new Date());
                }
            } else {
                item.setDateAdded(new Date());
            }

            // Handle promotion expiry
            Object promotionExpiryField = document.get("promotionExpiry");
            if (promotionExpiryField instanceof com.google.firebase.Timestamp) {
                com.google.firebase.Timestamp promotionTimestamp = (com.google.firebase.Timestamp) promotionExpiryField;
                item.setPromotionExpiry(promotionTimestamp.toDate());
            } else if (promotionExpiryField instanceof String) {
                String promotionDateString = (String) promotionExpiryField;
                try {
                    // Use SimpleDateFormat for API level compatibility
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US);
                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                    item.setPromotionExpiry(sdf.parse(promotionDateString));
                } catch (Exception e) {
                    // Leave promotion expiry as null if parsing fails
                }
            }

            // Handle relationship fields
            item.setListingId(document.getString("listingId"));
            item.setTransactionId(document.getString("transactionId"));

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
}
