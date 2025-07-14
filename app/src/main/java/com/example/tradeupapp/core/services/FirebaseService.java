package com.example.tradeupapp.core.services;

import android.net.Uri;
import android.util.Log;

import com.example.tradeupapp.models.CategoryModel;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.models.NotificationModel;
import com.example.tradeupapp.models.OfferModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

    // Haversine formula for distance in km
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Overload to support distance filter
    public void searchListingsWithFilters(String keyword, String category, double minPrice, double maxPrice, String condition, String sortBy, Double userLat, Double userLng, Double maxDistanceKm, ListingsCallback callback) {
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
        query = query.orderBy("createdAt", Query.Direction.DESCENDING);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<ListingModel> listings = new ArrayList<>();
            List<ListingModel> filteredListings = new ArrayList<>();
            List<Task<DocumentSnapshot>> itemTasks = new ArrayList<>();
            Map<String, ListingModel> listingByItemId = new HashMap<>();
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                ListingModel listing = document.toObject(ListingModel.class);
                if (listing != null) {
                    // Keyword filter (client-side)
                    if (keyword == null || keyword.isEmpty()) {
                        listings.add(listing);
                    } else {
                        listings.add(listing); // filter by keyword after joining with ItemModel in UI
                    }
                    // Prepare to fetch related ItemModel for distance filtering
                    if (userLat != null && userLng != null && maxDistanceKm != null && maxDistanceKm > 0) {
                        String itemId = listing.getItemId();
                        if (itemId != null) {
                            Task<DocumentSnapshot> itemTask = db.collection("items").document(itemId).get();
                            itemTasks.add(itemTask);
                            listingByItemId.put(itemId, listing);
                        }
                    }
                }
            }
            if (userLat != null && userLng != null && maxDistanceKm != null && maxDistanceKm > 0 && !itemTasks.isEmpty()) {
                Tasks.whenAllSuccess(itemTasks).addOnSuccessListener(results -> {
                    for (Object obj : results) {
                        if (obj instanceof DocumentSnapshot) {
                            DocumentSnapshot itemDoc = (DocumentSnapshot) obj;
                            ItemModel item = documentToItemModel(itemDoc);
                            if (item != null) {
                                Double lat = item.getLocationLatitude();
                                Double lng = item.getLocationLongitude();
                                if (lat != null && lng != null) {
                                    double distance = haversine(userLat, userLng, lat, lng);
                                    if (distance <= maxDistanceKm) {
                                        ListingModel listing = listingByItemId.get(item.getId());
                                        if (listing != null) {
                                            filteredListings.add(listing);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    callback.onSuccess(filteredListings);
                }).addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
            } else {
                callback.onSuccess(listings);
            }
        }).addOnFailureListener(e -> {
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
            // Parse location as Map<String, Object>
            Object locObj = document.get("location");
            if (locObj instanceof Map) {
                item.setLocation((Map<String, Object>) locObj);
            } else {
                item.setLocation(null);
            }
            Object weightObj = document.get("weight");
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

    // Helper to safely extract a Date from Firestore field
    private Date getDateFromField(Object field) {
        if (field == null) return null;
        if (field instanceof com.google.firebase.Timestamp) {
            return ((com.google.firebase.Timestamp) field).toDate();
        } else if (field instanceof Date) {
            return (Date) field;
        } else if (field instanceof Long) {
            return new Date((Long) field);
        } else if (field instanceof String) {
            String str = (String) field;
            // Try ISO8601 parse (e.g. 2023-07-12T23:18:51.452Z)
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                return sdf.parse(str);
            } catch (Exception e1) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US);
                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                    return sdf.parse(str);
                } catch (Exception e2) {
                    // Fallback: try parse as long
                    try {
                        return new Date(Long.parseLong(str));
                    } catch (Exception ignored) {}
                }
            }
        }
        return null;
    }

    // Helper method to convert Firestore document to ListingModel
    private ListingModel documentToListingModel(com.google.firebase.firestore.DocumentSnapshot document) {
        try {
            ListingModel listing = new ListingModel();
            listing.setId(document.getId());
            listing.setItemId(document.getString("itemId"));
            listing.setSellerId(document.getString("sellerId"));
            listing.setPrice(document.getDouble("price") != null ? document.getDouble("price") : 0.0);
            listing.setTransactionStatus(document.getString("transactionStatus"));
            listing.setCreatedAt(getDateFromField(document.get("createdAt")));
            listing.setUpdatedAt(getDateFromField(document.get("updatedAt")));
            // Set additional fields
            if (document.contains("viewCount")) {
                Number viewCount = document.getLong("viewCount");
                listing.setViewCount(viewCount != null ? viewCount.intValue() : 0);
            }
            if (document.contains("tags")) {
                Object tagsObj = document.get("tags");
                if (tagsObj instanceof List) {
                    listing.setTags(new ArrayList<>((List<String>) tagsObj));
                }
            }
            if (document.contains("distanceRadius")) {
                Number distance = document.getDouble("distanceRadius");
                listing.setDistanceRadius(distance != null ? distance.doubleValue() : 0.0);
            }
            if (document.contains("allowOffers")) {
                Boolean allowOffers = document.getBoolean("allowOffers");
                listing.setAllowOffers(allowOffers != null ? allowOffers : true);
            }
            if (document.contains("allowReturns")) {
                Boolean allowReturns = document.getBoolean("allowReturns");
                listing.setAllowReturns(allowReturns != null ? allowReturns : false);
            }
            if (document.contains("isActive")) {
                Boolean isActive = document.getBoolean("isActive");
                listing.setActive(isActive != null ? isActive : true);
            }
            // Interactions (nested object)
            if (document.contains("interactions")) {
                Object interactionsObj = document.get("interactions");
                if (interactionsObj instanceof Map) {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    String json = gson.toJson(interactionsObj);
                    ListingModel.Interactions interactions = gson.fromJson(json, ListingModel.Interactions.class);
                    listing.setInteractions(interactions);
                }
            }
            return listing;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to ListingModel", e);
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
                        ListingModel listing = documentToListingModel(document);
                        if (listing != null) {
                            listings.add(listing);
                        }
                    }
                    callback.onSuccess(listings);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting listings", e);
                    callback.onError(e.getMessage());
                });
    }
    // }

    // Get listings by category (category is a field of Item, not Listing)
    public void getListingsByCategory(String category, ListingsCallback callback) {
        // Step 1: Get all items with the given category
        db.collection("items")
                .whereEqualTo("category", category)
                .whereEqualTo("status", "Available")
                .get()
                .addOnSuccessListener(itemSnapshots -> {
                    List<String> itemIds = new ArrayList<>();
                    for (DocumentSnapshot doc : itemSnapshots) {
                        itemIds.add(doc.getId());
                    }
                    if (itemIds.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }
                    // Step 2: Get all listings with itemId in itemIds (handle Firestore limitation: max 10 for whereIn)
                    List<ListingModel> allListings = new ArrayList<>();
                    List<List<String>> batches = new ArrayList<>();
                    int batchSize = 10;
                    for (int i = 0; i < itemIds.size(); i += batchSize) {
                        batches.add(itemIds.subList(i, Math.min(itemIds.size(), i + batchSize)));
                    }
                    if (batches.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }
                    final int[] completed = {0};
                    final boolean[] failed = {false};
                    for (List<String> batch : batches) {
                        db.collection("listings")
                                .whereIn("itemId", batch)
                                .orderBy("createdAt", Query.Direction.DESCENDING)
                                .get()
                                .addOnSuccessListener(listingSnapshots -> {
                                    for (DocumentSnapshot doc : listingSnapshots) {
                                        ListingModel listing = doc.toObject(ListingModel.class);
                                        if (listing != null) {
                                            allListings.add(listing);
                                        }
                                    }
                                    completed[0]++;
                                    if (completed[0] == batches.size() && !failed[0]) {
                                        // Sort all listings by createdAt desc
                                        allListings.sort((a, b) -> {
                                            if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                                            if (a.getCreatedAt() == null) return 1;
                                            if (b.getCreatedAt() == null) return -1;
                                            Comparable bCreated = (Comparable) b.getCreatedAt();
                                            Comparable aCreated = (Comparable) a.getCreatedAt();
                                            if (bCreated == null && aCreated == null) return 0;
                                            if (bCreated == null) return 1;
                                            if (aCreated == null) return -1;
                                            return bCreated.compareTo(aCreated);
                                        });
                                        callback.onSuccess(allListings);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (!failed[0]) {
                                        failed[0] = true;
                                        Log.e(TAG, "Error getting listings by category (step 2)", e);
                                        callback.onError(e.getMessage());
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting items for category (step 1)", e);
                    callback.onError(e.getMessage());
                });
    }


    // Delete a listing by ID, only if not in transaction, and delete the item as well
    public void deleteListingWithItem(String listingId, Runnable onSuccess, java.util.function.Consumer<String> onError) {
        if (listingId == null) {
            if (onError != null) onError.accept("Listing ID is null");
            return;
        }
        db.collection("transactions").whereEqualTo("listingId", listingId).get()
            .addOnSuccessListener(transSnapshots -> {
                if (!transSnapshots.isEmpty()) {
                    if (onError != null) onError.accept("Cannot delete: Listing already has a transaction (sold or in progress)");
                    return;
                }
                // Get the listing to find the itemId
                db.collection("listings").document(listingId).get()
                    .addOnSuccessListener(listingDoc -> {
                        if (!listingDoc.exists()) {
                            if (onError != null) onError.accept("Listing not found");
                            return;
                        }
                        ListingModel listing = listingDoc.toObject(ListingModel.class);
                        String itemId = listing != null ? listing.getItemId() : null;
                        // Delete the listing
                        db.collection("listings").document(listingId).delete()
                            .addOnSuccessListener(aVoid -> {
                                if (itemId != null) {
                                    db.collection("items").document(itemId).delete()
                                        .addOnSuccessListener(aVoid2 -> {
                                            if (onSuccess != null) onSuccess.run();
                                        })
                                        .addOnFailureListener(e -> {
                                            if (onError != null) onError.accept("Failed to delete item: " + e.getMessage());
                                        });
                                } else {
                                    if (onSuccess != null) onSuccess.run();
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (onError != null) onError.accept("Failed to delete listing: " + e.getMessage());
                            });
                    })
                    .addOnFailureListener(e -> {
                        if (onError != null) onError.accept("Failed to fetch listing: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                if (onError != null) onError.accept("Failed to check transaction: " + e.getMessage());
            });
    }

    // Update both ListingModel and ItemModel in Firestore
    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }

    public void updateListingAndItem(ListingModel listing, ItemModel item, SimpleCallback callback) {
        if (listing == null || item == null || listing.getId() == null || item.getId() == null) {
            if (callback != null) callback.onError("Invalid listing or item data");
            return;
        }
        db.collection("items").document(item.getId()).set(item)
            .addOnSuccessListener(aVoid -> {
                db.collection("listings").document(listing.getId()).set(listing)
                    .addOnSuccessListener(aVoid2 -> {
                        if (callback != null) callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onError(e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    // Log a user interaction (e.g., view or search) to Firestore
    public void logUserInteraction(String userId, String type, String listingId, String query) {
        Map<String, Object> interaction = new HashMap<>();
        interaction.put("type", type); // e.g., "view", "search"
        interaction.put("timestamp", new Date());
        if (listingId != null) interaction.put("listingId", listingId);
        if (query != null) interaction.put("query", query);
        db.collection("browsingHistory")
                .document(userId)
                .collection("history")
                .add(interaction)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Interaction logged"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to log interaction", e));
        // If this is a view, also log to listing's userInteractions
        if ("view".equals(type) && listingId != null) {
            logListingViewInteraction(listingId, userId);
        }
    }

    // Update userInteractions in a listing's interactions field when a user views a listing
    public void logListingViewInteraction(String listingId, String userId) {
        if (listingId == null || userId == null) return;
        db.collection("listings").document(listingId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) return;
                Map<String, Object> data = documentSnapshot.getData();
                if (data == null) return;
                Map<String, Object> interactions = (Map<String, Object>) data.get("interactions");
                List<Map<String, Object>> userInteractions;
                if (interactions != null && interactions.get("userInteractions") instanceof List) {
                    userInteractions = (List<Map<String, Object>>) interactions.get("userInteractions");
                } else {
                    userInteractions = new ArrayList<>();
                }
                boolean found = false;
                for (Map<String, Object> entry : userInteractions) {
                    if (userId.equals(entry.get("userId"))) {
                        entry.put("viewedAt", new java.util.Date());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Map<String, Object> newEntry = new HashMap<>();
                    newEntry.put("userId", userId);
                    newEntry.put("viewedAt", new java.util.Date());
                    newEntry.put("savedAt", null);
                    newEntry.put("sharedAt", null);
                    userInteractions.add(newEntry);
                }
                if (interactions == null) interactions = new HashMap<>();
                interactions.put("userInteractions", userInteractions);
                db.collection("listings").document(listingId)
                    .update("interactions", interactions);
            });
    }

    // Log when a user saves a listing
    public void logListingSavedInteraction(String listingId, String userId) {
        if (listingId == null || userId == null) return;
        db.collection("listings").document(listingId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) return;
                Map<String, Object> data = documentSnapshot.getData();
                if (data == null) return;
                Map<String, Object> interactions = (Map<String, Object>) data.get("interactions");
                List<Map<String, Object>> userInteractions;
                if (interactions != null && interactions.get("userInteractions") instanceof List) {
                    userInteractions = (List<Map<String, Object>>) interactions.get("userInteractions");
                } else {
                    userInteractions = new ArrayList<>();
                }
                boolean found = false;
                for (Map<String, Object> entry : userInteractions) {
                    if (userId.equals(entry.get("userId"))) {
                        entry.put("savedAt", new java.util.Date());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Map<String, Object> newEntry = new HashMap<>();
                    newEntry.put("userId", userId);
                    newEntry.put("viewedAt", null);
                    newEntry.put("savedAt", new java.util.Date());
                    newEntry.put("sharedAt", null);
                    userInteractions.add(newEntry);
                }
                if (interactions == null) interactions = new HashMap<>();
                interactions.put("userInteractions", userInteractions);
                db.collection("listings").document(listingId)
                    .update("interactions", interactions);
            });
    }

    // Log when a user shares a listing
    public void logListingSharedInteraction(String listingId, String userId) {
        if (listingId == null || userId == null) return;
        db.collection("listings").document(listingId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) return;
                Map<String, Object> data = documentSnapshot.getData();
                if (data == null) return;
                Map<String, Object> interactions = (Map<String, Object>) data.get("interactions");
                List<Map<String, Object>> userInteractions;
                if (interactions != null && interactions.get("userInteractions") instanceof List) {
                    userInteractions = (List<Map<String, Object>>) interactions.get("userInteractions");
                } else {
                    userInteractions = new ArrayList<>();
                }
                boolean found = false;
                for (Map<String, Object> entry : userInteractions) {
                    if (userId.equals(entry.get("userId"))) {
                        entry.put("sharedAt", new java.util.Date());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Map<String, Object> newEntry = new HashMap<>();
                    newEntry.put("userId", userId);
                    newEntry.put("viewedAt", null);
                    newEntry.put("savedAt", null);
                    newEntry.put("sharedAt", new java.util.Date());
                    userInteractions.add(newEntry);
                }
                if (interactions == null) interactions = new HashMap<>();
                interactions.put("userInteractions", userInteractions);
                db.collection("listings").document(listingId)
                    .update("interactions", interactions);
            });
    }

    // Log a listing viewed interaction (for adapter usage)
    public void logListingViewedInteraction(String listingId, String userId) {
        logUserInteraction(userId, "view", listingId, null);
    }

    // Callback for browsing history
    public interface BrowsingHistoryCallback {
        void onSuccess(List<Map<String, Object>> history);
        void onFailure(Exception e);
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

    // Callback for offers
    public interface OffersCallback {
        void onSuccess(List<OfferModel> offers);
        void onError(String error);
    }

    // Lấy danh sách offer theo nhiều listingId
    public void getOffersByListingIds(List<String> listingIds, OffersCallback callback) {
        getOffersByListingIds(listingIds, 20, callback); // Default limit 20
    }

    // Overloaded method with limit
    public void getOffersByListingIds(List<String> listingIds, int limit, OffersCallback callback) {
        if (listingIds == null || listingIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        db.collection("offers")
            .whereIn("listingId", listingIds)
            .limit(limit)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<OfferModel> offers = new ArrayList<>();
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    OfferModel offer = document.toObject(OfferModel.class);
                    if (offer != null) {
                        offer.setId(document.getId());
                        offers.add(offer);
                    }
                }
                callback.onSuccess(offers);
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Phân trang lấy offer theo nhiều listingId, sắp xếp theo createdAt desc
    public void getOffersByListingIdsPaginated(List<String> listingIds, int limit, Object lastCreatedAt, OffersCallback callback) {
        if (listingIds == null || listingIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        Query query = db.collection("offers")
            .whereIn("listingId", listingIds)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit);
        if (lastCreatedAt != null) {
            query = query.startAfter(lastCreatedAt);
        }
        query.get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<OfferModel> offers = new ArrayList<>();
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    OfferModel offer = document.toObject(OfferModel.class);
                    if (offer != null) {
                        offer.setId(document.getId());
                        offers.add(offer);
                    }
                }
                callback.onSuccess(offers);
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Get all purchased listings for a user by querying the transaction collection
    public void getUserPurchasedListings(String userId, ListingsCallback callback) {
        db.collection("transactions")
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

    // Fetch multiple listings by their IDs and return as a list
    public void getListingsByIds(List<String> listingIds, ListingsCallback callback) {
        if (listingIds == null || listingIds.isEmpty()) {
            callback.onSuccess(new java.util.ArrayList<>());
            return;
        }
        db.collection("listings")
                .whereIn("id", listingIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    java.util.List<ListingModel> listingList = new java.util.ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                        ListingModel listing = documentToListingModel(document);
                        if (listing != null) {
                            listing.setId(document.getId());
                            listingList.add(listing);
                        }
                    }
                    callback.onSuccess(listingList);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Get all transactions for a listing
    public void getTransactionsByListingId(String listingId, TransactionsCallback callback) {
        db.collection("transactions")
            .whereEqualTo("listingId", listingId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<com.example.tradeupapp.models.TransactionModel> transactions = new ArrayList<>();
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    com.example.tradeupapp.models.TransactionModel txn = document.toObject(com.example.tradeupapp.models.TransactionModel.class);
                    if (txn != null) {
                        transactions.add(txn);
                    }
                }
                callback.onSuccess(transactions);
            })
            .addOnFailureListener(e -> {
                callback.onError(e.getMessage());
            });
    }

    // Callback for transactions
    public interface TransactionsCallback {
        void onSuccess(List<com.example.tradeupapp.models.TransactionModel> transactions);
        void onError(String error);
    }

    // Get popular listings sorted by interactions.aggregate.totalViews descending
    public void getPopularListingsByTotalViews(int limit, ListingsCallback callback) {
        db.collection("listings")
            .orderBy("interactions.aggregate.totalViews", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<ListingModel> listings = new ArrayList<>();
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    ListingModel listing = documentToListingModel(document);
                    if (listing != null) {
                        listings.add(listing);
                    }
                }
                callback.onSuccess(listings);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting popular listings by totalViews", e);
                callback.onError(e.getMessage());
            });
    }

    // Lấy danh sách listing user đã viewed/saved/shared (dựa vào interactions.userInteractions)
    public void getUserHistoryListings(String userId, String type, ListingsCallback callback) {
        // type: "viewedAt", "savedAt", "sharedAt"
        db.collection("listings").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<ListingModel> result = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Map<String, Object> data = doc.getData();
                    if (data == null) continue;
                    Map<String, Object> interactions = (Map<String, Object>) data.get("interactions");
                    if (interactions == null) continue;
                    List<Map<String, Object>> userInteractions = (List<Map<String, Object>>) interactions.get("userInteractions");
                    if (userInteractions == null) continue;
                    for (Map<String, Object> entry : userInteractions) {
                        if (userId.equals(entry.get("userId")) && entry.get(type) != null) {
                            ListingModel listing = doc.toObject(ListingModel.class);
                            if (listing != null) {
                                listing.setId(doc.getId());
                                result.add(listing);
                            }
                            break;
                        }
                    }
                }
                callback.onSuccess(result);
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Callback for search history
    public interface SearchHistoryCallback {
        void onSuccess(List<String> history);
        void onFailure(Exception e);
    }

    // Get user search history (latest 20 queries, sorted by timestamp desc)
    public void getUserSearchHistory(String userId, SearchHistoryCallback callback) {
        db.collection("browsingHistory")
            .document(userId)
            .collection("history")
            .whereEqualTo("type", "search")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<String> history = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    String query = doc.getString("query");
                    if (query != null && !history.contains(query)) {
                        history.add(query);
                    }
                }
                callback.onSuccess(history);
            })
            .addOnFailureListener(callback::onFailure);
    }

    // Delete a search query from user history
    public void deleteUserSearchQuery(String userId, String query, SimpleCallback callback) {
        db.collection("browsingHistory")
            .document(userId)
            .collection("history")
            .whereEqualTo("type", "search")
            .whereEqualTo("query", query)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Task<Void>> tasks = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    tasks.add(doc.getReference().delete());
                }
                Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Add a search query to user search history
    public void addUserSearchHistory(String userId, String query, SimpleCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("query", query);
        data.put("type", "search");
        data.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
        db.collection("browsingHistory")
            .document(userId)
            .collection("history")
            .add(data)
            .addOnSuccessListener(documentReference -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Lấy danh sách offer theo sellerId (dùng cho MyStore hoặc OfferHistory)
    public void getOffersBySellerId(String sellerId, OffersCallback callback) {
        db.collection("offers")
                .whereEqualTo("sellerId", sellerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<OfferModel> offers = new ArrayList<>();
                    for (var document : queryDocumentSnapshots.getDocuments()) {
                        OfferModel offer = document.toObject(OfferModel.class);
                        if (offer != null) {
                            offer.setId(document.getId());
                            offers.add(offer);
                        }
                    }
                    callback.onSuccess(offers);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting offers by sellerId", e);
                    callback.onError(e.getMessage());
                });
    }

    // Get all transactions for a user (by buyerId)
    public void getUserTransactions(String userId, TransactionsCallback callback) {
        db.collection("transactions")
                .whereEqualTo("buyerId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<com.example.tradeupapp.models.TransactionModel> transactions = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        com.example.tradeupapp.models.TransactionModel transaction = document.toObject(com.example.tradeupapp.models.TransactionModel.class);
                        if (transaction != null) {
                            transaction.setId(document.getId());
                            transactions.add(transaction);
                        }
                    }
                    callback.onSuccess(transactions);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting purchase transactions", e);
                    callback.onError(e.getMessage());
                });
    }

    // Callback for listing fetch
    public interface ListingCallback {
        void onSuccess(ListingModel listing);
        void onError(String error);
    }

    // Get listing by ID
    public void getListingById(String listingId, ListingCallback callback) {
        db.collection("listings")
                .document(listingId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ListingModel listing = documentToListingModel(documentSnapshot);
                        if (listing != null) {
                            callback.onSuccess(listing);
                        } else {
                            callback.onError("Failed to parse listing data");
                        }
                    } else {
                        callback.onError("Listing not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting listing by ID", e);
                    callback.onError(e.getMessage());
                });
    }

    // Lấy danh sách offer theo buyerId (dùng cho OfferHistory ở tab History)
    public void getOffersByBuyerId(String buyerId, OffersCallback callback) {
        db.collection("offers")
                .whereEqualTo("buyerId", buyerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<OfferModel> offers = new ArrayList<>();
                    for (var document : queryDocumentSnapshots.getDocuments()) {
                        OfferModel offer = document.toObject(OfferModel.class);
                        if (offer != null) {
                            offer.setId(document.getId());
                            offers.add(offer);
                        }
                    }
                    callback.onSuccess(offers);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting offers by buyerId", e);
                    callback.onError(e.getMessage());
                });
    }

    // Create a new offer in Firestore
    public void createOffer(OfferModel offer, SimpleCallback callback) {
        if (offer == null) {
            if (callback != null) callback.onError("Offer is null");
            return;
        }
        db.collection("offers")
            .add(offer)
            .addOnSuccessListener(documentReference -> {
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    // Update an existing offer in Firestore by its id
    public void updateOffer(OfferModel offer, SimpleCallback callback) {
        if (offer == null || offer.getId() == null) {
            if (callback != null) callback.onError("Offer or Offer ID is null");
            return;
        }
        db.collection("offers")
            .document(offer.getId())
            .set(offer)
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(e.getMessage());
            });
    }
}
