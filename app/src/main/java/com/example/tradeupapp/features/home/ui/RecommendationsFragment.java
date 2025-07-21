package com.example.tradeupapp.features.home.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.features.home.viewmodel.HomeViewModel;
import com.example.tradeupapp.models.CategoryModel;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.shared.adapters.ListingAdapter;
import com.example.tradeupapp.utils.ListingFilterUtil;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecommendationsFragment extends Fragment {

    private ChipGroup chipGroupCategories;
    private RecyclerView personalizedRecyclerView;
    private RecyclerView nearbyRecyclerView;
    private RecyclerView recentRecyclerView;

    private TextView seeAllRecommended;
    private TextView seeAllNearby;
    private TextView seeAllRecent;
    private TextView tvLocation;

    private LinearLayout locationIndicator;

    private NavController navController;
    private FirebaseService firebaseService;
    private HomeViewModel homeViewModel;

    // Adapters
    private ListingAdapter personalizedAdapter;
    private ListingAdapter nearbyAdapter;
    private ListingAdapter recentAdapter;

    private FloatingActionButton fabAddItem;

    // Store all listings and items for filtering
    private List<ListingModel> allListings = new ArrayList<>();
    private List<ItemModel> allItems = new ArrayList<>();
    private List<String> selectedCategories = new ArrayList<>(java.util.Collections.singletonList("All Categories"));

    // Store all nearby and recent listings separately
    private List<ListingModel> allNearbyListings = new ArrayList<>();
    private List<ListingModel> allRecentListings = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase service
        firebaseService = FirebaseService.getInstance();

        // Initialize navigation controller
        navController = Navigation.findNavController(view);

        // Initialize views
        initViews(view);

        // FAB logic
        fabAddItem = view.findViewById(R.id.fab_add_item);
        updateFabForUserRole();
        updateFabClickListener();

        // Set up click listeners
        setupClickListeners();

        // Initialize ViewModel
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        observeLocation();

        // Load real data from Firestore
        loadDataFromFirestore();

        // Load static categories
        loadStaticCategories();
        // Removed premature call to filterListingsByCategories(selectedCategories)

        // Listen for location result from MapPickerFragment
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.getCurrentBackStackEntry().getSavedStateHandle().getLiveData("location_data").observe(getViewLifecycleOwner(), result -> {
            if (result != null && result instanceof Bundle) {
                Bundle locationData = (Bundle) result;
                double latitude = locationData.getDouble("latitude");
                double longitude = locationData.getDouble("longitude");
                String address = locationData.getString("address");
                homeViewModel.setLocation(latitude, longitude);
                if (address != null && !address.isEmpty() && tvLocation != null) {
                    tvLocation.setText(address);
                }
                // Clear result to avoid receiving multiple times
                navController.getCurrentBackStackEntry().getSavedStateHandle().set("location_data", null);
            }
        });
    }

    private void initViews(View view) {
        // ChipGroup for categories
        chipGroupCategories = view.findViewById(R.id.chipgroup_categories);
        // RecyclerViews
        personalizedRecyclerView = view.findViewById(R.id.recycler_personalized);
        nearbyRecyclerView = view.findViewById(R.id.recycler_nearby);
        recentRecyclerView = view.findViewById(R.id.recycler_recent);

        // "See All" TextView buttons
        seeAllRecommended = view.findViewById(R.id.tv_see_all_recommended);
        seeAllNearby = view.findViewById(R.id.tv_see_all_nearby);
        seeAllRecent = view.findViewById(R.id.tv_see_all_recent);

        // Location TextView
        tvLocation = view.findViewById(R.id.tv_location);

        locationIndicator = view.findViewById(R.id.location_indicator);
        // Add notification button
        View btnNotification = view.findViewById(R.id.btn_notification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                navController.navigate(R.id.action_nav_recommendations_to_notificationFragment);
            });
        }
    }

    private void updateFabForUserRole() {
        if (fabAddItem != null) {
            fabAddItem.setImageResource(R.drawable.ic_cart_24);
            fabAddItem.setContentDescription(getString(R.string.cart));
        }
    }

    private void updateFabClickListener() {
        if (fabAddItem != null) {
            fabAddItem.setOnClickListener(view -> {
                navController.navigate(R.id.nav_cart);
            });
        }
    }

    private void setupClickListeners() {
        // "See All" buttons to navigate to respective listing pages
        seeAllRecommended.setOnClickListener(v -> {
            // Navigate to all recommended items
            navController.navigate(R.id.action_nav_recommendations_to_recommendedListingFragment);
        });

        seeAllNearby.setOnClickListener(v -> {
            // Navigate to all nearby items
            navController.navigate(R.id.action_nav_recommendations_to_nearbyListingFragment);
        });

        seeAllRecent.setOnClickListener(v -> {
            // Navigate to all recent items
            navController.navigate(R.id.action_nav_recommendations_to_recentListingFragment);
        });

        locationIndicator.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.mapPickerFragment);
        });
    }

    private void observeLocation() {
        homeViewModel.getDistrict().observe(getViewLifecycleOwner(), district -> {
            if (district != null && tvLocation != null) {
                tvLocation.setText(district);
            }
        });
        // Try to get device location, fallback to default
        if (homeViewModel.getLatitude().getValue() == null || homeViewModel.getLongitude().getValue() == null) {
            // Default to Quận 12, Hồ Chí Minh
            homeViewModel.setLocation(10.8602, 106.7645);
        }
    }

    private void loadDataFromFirestore() {
        // Load recommended items (all available items for now, can be enhanced with ML)
        loadRecommendedItems();

        // Load nearby items (within user's location radius)
        loadNearbyItems();

        // Load recent items (recently added items)
        loadRecentItems();
    }

    // Extract user's viewed categories from listings' interactions (local, not Firestore)
    private void getUserViewedCategoriesFromListings(List<ListingModel> listings, List<ItemModel> allItems, String userId, UserViewedCategoriesCallback callback) {
        Set<String> viewedItemIds = new HashSet<>();
        for (ListingModel listing : listings) {
            if (listing.getInteractions() != null && listing.getInteractions().getUserInteractions() != null) {
                for (ListingModel.UserInteraction ui : listing.getInteractions().getUserInteractions()) {
                    if (userId.equals(ui.getUserId()) && ui.getViewedAt() != null) {
                        viewedItemIds.add(listing.getItemId());
                    }
                }
            }
        }
        Set<String> categorySet = new HashSet<>();
        for (ItemModel item : allItems) {
            if (viewedItemIds.contains(item.getId()) && item.getCategory() != null) {
                categorySet.add(item.getCategory());
            }
        }
        callback.onResult(new ArrayList<>(categorySet));
    }

    // Fetch user's viewed categories from Firestore user history
    private void getUserViewedCategoriesAsync(UserViewedCategoriesCallback callback) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            callback.onResult(new ArrayList<>());
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).collection("viewedItems").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Set<String> categorySet = new HashSet<>();
                List<String> itemIds = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String itemId = doc.getString("itemId");
                    if (itemId != null) itemIds.add(itemId);
                }
                if (itemIds.isEmpty()) {
                    callback.onResult(new ArrayList<>());
                    return;
                }
                // Fetch ItemModels for all viewed itemIds
                class Counter { int count = 0; }
                Counter counter = new Counter();
                for (String itemId : itemIds) {
                    firebaseService.getItemById(itemId, new FirebaseService.ItemCallback() {
                        @Override
                        public void onSuccess(ItemModel item) {
                            if (item.getCategory() != null) categorySet.add(item.getCategory());
                            counter.count++;
                            if (counter.count == itemIds.size()) {
                                callback.onResult(new ArrayList<>(categorySet));
                            }
                        }
                        @Override
                        public void onError(String error) {
                            counter.count++;
                            if (counter.count == itemIds.size()) {
                                callback.onResult(new ArrayList<>(categorySet));
                            }
                        }
                    });
                }
            })
            .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    private interface UserViewedCategoriesCallback {
        void onResult(List<String> categories);
    }

    private void loadRecommendedItems() {
        firebaseService.getAllListings(new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(List<ListingModel> listings) {
                String currentUserId = firebaseService.getCurrentUserId();
                List<ListingModel> availableListings = new ArrayList<>();
                for (ListingModel l : listings) {
                    if (l.getTransactionStatus() != null && l.getTransactionStatus().equalsIgnoreCase("available")
                            && l.getSellerId() != null && !l.getSellerId().equals(currentUserId)) {
                        availableListings.add(l);
                    }
                }
                allListings = new ArrayList<>(availableListings); // Store for filtering
                firebaseService.getAllItems(new FirebaseService.ItemsCallback() {
                    @Override
                    public void onSuccess(List<ItemModel> items) {
                        Log.d("CategoryDebug", "onSuccess getAllItems, items.size=" + items.size());
                        allItems = new ArrayList<>(items); // Store for filtering
                        // Log all items and their categories for debugging
                        for (ItemModel item : items) {
                            Log.d("CategoryDebug", "Item id: " + item.getId() + ", title: " + item.getTitle() + ", category: " + item.getCategory());
                        }
                        // Log all unique categories from items
                        java.util.Set<String> uniqueCategories = new java.util.HashSet<>();
                        for (ItemModel item : allItems) {
                            if (item.getCategory() != null) uniqueCategories.add(item.getCategory());
                        }
                        Log.d("CategoryFilter", "All categories in DB: " + uniqueCategories);
                        // Call filterListingsByCategories only after allListings and allItems are loaded
                        filterListingsByCategories(selectedCategories);
                        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                        if (userId == null) {
                            // fallback: show popular listings
                            showPersonalizedByPopularity(allListings);
                            return;
                        }
                        getUserViewedCategoriesFromListings(allListings, allItems, userId, viewedCategories -> {
                            Double userLat = homeViewModel.getLatitude().getValue();
                            Double userLng = homeViewModel.getLongitude().getValue();
                            if (userLat == null || userLng == null) {
                                userLat = 10.8602; userLng = 106.7645; // Quận 12 default
                            }
                            final Double finalUserLat = userLat;
                            final Double finalUserLng = userLng;
                            class ListingWithMeta {
                                ListingModel listing;
                                ItemModel item;
                                double distance;
                                ListingWithMeta(ListingModel l, ItemModel i, double d) { listing = l; item = i; distance = d; }
                            }
                            List<ListingWithMeta> metaList = new ArrayList<>();
                            for (ListingModel listing : allListings) {
                                ItemModel item = null;
                                for (ItemModel i : allItems) {
                                    if (i.getId().equals(listing.getItemId())) { item = i; break; }
                                }
                                double distance = Double.MAX_VALUE;
                                if (item != null && item.getLocationLatitude() != null && item.getLocationLongitude() != null) {
                                    distance = haversine(finalUserLat, finalUserLng, item.getLocationLatitude(), item.getLocationLongitude());
                                }
                                metaList.add(new ListingWithMeta(listing, item, distance));
                            }
                            metaList.sort((a, b) -> {
                                boolean aCat = a.item != null && viewedCategories.contains(a.item.getCategory());
                                boolean bCat = b.item != null && viewedCategories.contains(b.item.getCategory());
                                if (aCat && !bCat) return -1;
                                if (!aCat && bCat) return 1;
                                int popCompare = Integer.compare(b.listing.getViewCount(), a.listing.getViewCount());
                                if (popCompare != 0) return popCompare;
                                return Double.compare(a.distance, b.distance);
                            });
                            List<ListingModel> recommendedListings = new ArrayList<>();
                            for (ListingWithMeta lwm : metaList) {
                                recommendedListings.add(lwm.listing);
                            }
                            if (recommendedListings.size() > 5) recommendedListings = recommendedListings.subList(0, 5);
                            personalizedAdapter = new ListingAdapter(
                                    requireContext(),
                                    recommendedListings,
                                    listing -> navigateToItemDetail(listing.getId())
                            );
                            personalizedRecyclerView.setAdapter(personalizedAdapter);
                        });
                    }
                    @Override
                    public void onError(String error) {
                        Log.e("CategoryDebug", "onError getAllItems: " + error);
                        showPersonalizedByPopularity(allListings);
                    }
                });
            }
            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), "Error loading recommendations: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Fallback: show by popularity only
    private void showPersonalizedByPopularity(List<ListingModel> listings) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        List<ListingModel> sorted = new ArrayList<>();
        for (ListingModel l : listings) {
            if (currentUserId == null || !l.getSellerId().equals(currentUserId)) {
                sorted.add(l);
            }
        }
        sorted.sort((a, b) -> Integer.compare(b.getViewCount(), a.getViewCount()));
        if (sorted.size() > 5) sorted = sorted.subList(0, 5);
        personalizedAdapter = new ListingAdapter(
                requireContext(),
                sorted,
                listing -> navigateToItemDetail(listing.getId())
        );
        personalizedRecyclerView.setAdapter(personalizedAdapter);
    }

    private void loadNearbyItems() {
        firebaseService.getAllListings(new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(List<ListingModel> listings) {
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                List<ListingModel> filteredListings = new ArrayList<>();
                for (ListingModel l : listings) {
                    boolean isAvailable = false;
                    try {
                        java.lang.reflect.Method getStatusMethod = l.getClass().getMethod("getTransactionStatus");
                        String status = (String) getStatusMethod.invoke(l);
                        isAvailable = "available".equalsIgnoreCase(status);
                    } catch (Exception e) {
                        isAvailable = false;
                    }
                    if ((currentUserId == null || !l.getSellerId().equals(currentUserId)) && isAvailable) {
                        filteredListings.add(l);
                    }
                }
                if (getActivity() != null && isAdded()) {
                    Double userLat = homeViewModel.getLatitude().getValue();
                    Double userLng = homeViewModel.getLongitude().getValue();
                    if (userLat == null || userLng == null) {
                        userLat = 10.8021; userLng = 106.6944; // Bình Thạnh default
                    }
                    final Double finalUserLat = userLat;
                    final Double finalUserLng = userLng;
                    class ListingWithDistance {
                        ListingModel listing;
                        double distance;
                        ListingWithDistance(ListingModel l, double d) { listing = l; distance = d; }
                    }
                    List<ListingWithDistance> filteredWithDistance = new ArrayList<>();
                    List<ItemModel> itemModels = new ArrayList<>();
                    List<ListingModel> allListings = new ArrayList<>(filteredListings);
                    class Counter { int count = 0; }
                    Counter counter = new Counter();
                    for (ListingModel listing : allListings) {
                        firebaseService.getItemById(listing.getItemId(), new FirebaseService.ItemCallback() {
                            @Override
                            public void onSuccess(ItemModel item) {
                                Double lat = item.getLocationLatitude();
                                Double lng = item.getLocationLongitude();
                                if (lat != null && lng != null) {
                                    double distance = haversine(finalUserLat, finalUserLng, lat, lng);
                                    if (distance <= 30.0) {
                                        filteredWithDistance.add(new ListingWithDistance(listing, distance));
                                        itemModels.add(item);
                                    }
                                }
                                counter.count++;
                                if (counter.count == allListings.size()) {
                                    filteredWithDistance.sort((a, b) -> Double.compare(a.distance, b.distance));
                                    List<ListingModel> sortedNearby = new ArrayList<>();
                                    for (ListingWithDistance lwd : filteredWithDistance) {
                                        sortedNearby.add(lwd.listing);
                                    }
                                    // Filter by category
                                    List<ListingModel> nearbyListings = ListingFilterUtil.filterListingsByCategoriesForList(sortedNearby, selectedCategories, allItems);
                                    if (nearbyListings.size() > 5) nearbyListings = nearbyListings.subList(0, 5);
                                    nearbyAdapter = new ListingAdapter(
                                            requireContext(),
                                            nearbyListings,
                                            listing -> navigateToItemDetail(listing.getId())
                                    );
                                    nearbyRecyclerView.setAdapter(nearbyAdapter);
                                }
                            }
                            @Override
                            public void onError(String error) {
                                counter.count++;
                                if (counter.count == allListings.size()) {
                                    filteredWithDistance.sort((a, b) -> Double.compare(a.distance, b.distance));
                                    List<ListingModel> sortedNearby = new ArrayList<>();
                                    for (ListingWithDistance lwd : filteredWithDistance) {
                                        sortedNearby.add(lwd.listing);
                                    }
                                    // Filter by category
                                    List<ListingModel> nearbyListings = ListingFilterUtil.filterListingsByCategoriesForList(sortedNearby, selectedCategories, allItems);
                                    if (nearbyListings.size() > 5) nearbyListings = nearbyListings.subList(0, 5);
                                    nearbyAdapter = new ListingAdapter(
                                            requireContext(),
                                            nearbyListings,
                                            listing -> navigateToItemDetail(listing.getId())
                                    );
                                    nearbyRecyclerView.setAdapter(nearbyAdapter);
                                }
                            }
                        });
                    }
                }
            }
            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), "Error loading nearby items: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadRecentItems() {
        firebaseService.getAllListings(new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(List<ListingModel> listings) {
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                List<ListingModel> filteredListings = new ArrayList<>();
                for (ListingModel l : listings) {
                    boolean isAvailable = false;
                    try {
                        java.lang.reflect.Method getStatusMethod = l.getClass().getMethod("getTransactionStatus");
                        String status = (String) getStatusMethod.invoke(l);
                        isAvailable = "available".equalsIgnoreCase(status);
                    } catch (Exception e) {
                        isAvailable = false;
                    }
                    if ((currentUserId == null || !l.getSellerId().equals(currentUserId)) && isAvailable) {
                        filteredListings.add(l);
                    }
                }
                if (getActivity() != null && isAdded()) {
                    // Filter by category
                    List<ListingModel> recentListings = ListingFilterUtil.filterListingsByCategoriesForList(filteredListings, selectedCategories, allItems);
                    if (recentListings.size() > 3) recentListings = recentListings.subList(0, 3);
                    recentAdapter = new ListingAdapter(
                            requireContext(),
                            recentListings,
                            listing -> navigateToItemDetail(listing.getId())
                    );
                    recentRecyclerView.setAdapter(recentAdapter);
                }
            }
            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), "Error loading recent items: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadStaticCategories() {
        String[] categories = {"All Categories", "Electronics", "Clothing", "Home & Garden", "Toys", "Books", "Sports", "Automotive", "Other"};
        List<CategoryModel> staticCategories = new ArrayList<>();
        for (String cat : categories) {
            CategoryModel model = new CategoryModel();
            model.setId(cat.toLowerCase().replace(" ", "_").replace("&", "and"));
            model.setName(cat);
            model.setIconResourceId(R.drawable.ic_category_24);
            staticCategories.add(model);
        }
        if (chipGroupCategories != null) {
            chipGroupCategories.removeAllViews();
            for (CategoryModel category : staticCategories) {
                Chip chip = (Chip) LayoutInflater.from(getContext()).inflate(R.layout.item_tag_chip, chipGroupCategories, false);
                chip.setText(category.getName());
                chip.setCheckable(true);
                chip.setChecked(selectedCategories.contains(category.getName()));
                chip.setOnClickListener(v -> {
                    String catName = category.getName();
                    if ("All Categories".equals(catName)) {
                        selectedCategories.clear();
                        selectedCategories.add("All Categories");
                    } else {
                        if (selectedCategories.contains("All Categories")) {
                            selectedCategories.remove("All Categories");
                        }
                        if (selectedCategories.contains(catName)) {
                            selectedCategories.remove(catName);
                        } else {
                            selectedCategories.add(catName);
                        }
                        if (selectedCategories.isEmpty()) {
                            selectedCategories.add("All Categories");
                        }
                    }
                    // Update chip checked state
                    for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
                        Chip c = (Chip) chipGroupCategories.getChildAt(i);
                        c.setChecked(selectedCategories.contains(c.getText().toString()));
                    }
                    filterAllListsByCategories();
                });
                chipGroupCategories.addView(chip);
            }
        }
    }

    private void filterAllListsByCategories() {
        // Lọc theo category trước
        List<ListingModel> filteredByCategory = ListingFilterUtil.filterListingsByCategoriesForList(
            allListings, selectedCategories, allItems
        );

        // Personalized
        filterListingsByCategories(selectedCategories);

        // Nearby
        if (nearbyRecyclerView != null) {
            // Lọc theo khoảng cách từ filteredByCategory
            Double userLat = homeViewModel.getLatitude().getValue();
            Double userLng = homeViewModel.getLongitude().getValue();
            if (userLat == null || userLng == null) {
                userLat = 10.8021; userLng = 106.6944;
            }
            final Double finalUserLat = userLat;
            final Double finalUserLng = userLng;
            List<ListingModel> filteredNearby = new ArrayList<>();
            for (ListingModel listing : filteredByCategory) {
                ItemModel item = null;
                for (ItemModel i : allItems) {
                    if (i.getId().equals(listing.getItemId())) { item = i; break; }
                }
                if (item != null && item.getLocationLatitude() != null && item.getLocationLongitude() != null) {
                    double distance = haversine(finalUserLat, finalUserLng, item.getLocationLatitude(), item.getLocationLongitude());
                    if (distance <= 30.0) {
                        filteredNearby.add(listing);
                    }
                }
            }
            // Sắp xếp theo khoảng cách
            filteredNearby.sort((a, b) -> {
                ItemModel ia = null, ib = null;
                for (ItemModel i : allItems) {
                    if (i.getId().equals(a.getItemId())) ia = i;
                    if (i.getId().equals(b.getItemId())) ib = i;
                }
                double da = ia != null ? haversine(finalUserLat, finalUserLng, ia.getLocationLatitude(), ia.getLocationLongitude()) : Double.MAX_VALUE;
                double db = ib != null ? haversine(finalUserLat, finalUserLng, ib.getLocationLatitude(), ib.getLocationLongitude()) : Double.MAX_VALUE;
                return Double.compare(da, db);
            });
            if (filteredNearby.size() > 5) filteredNearby = filteredNearby.subList(0, 5);
            if (nearbyAdapter == null) {
                nearbyAdapter = new ListingAdapter(
                    requireContext(),
                    filteredNearby,
                    listing -> navigateToItemDetail(listing.getId())
                );
                nearbyRecyclerView.setAdapter(nearbyAdapter);
            } else {
                nearbyAdapter.setListings(filteredNearby);
            }
        }

        // Recent
        if (recentRecyclerView != null) {
            // Sắp xếp theo ngày đăng từ filteredByCategory
            List<ListingModel> filteredRecent = new ArrayList<>(filteredByCategory);
            filteredRecent.sort((a, b) -> {
                long aCreated = 0L;
                long bCreated = 0L;
                try {
                    aCreated = a.getCreatedAtTimestamp().getSeconds();
                } catch (Exception e) {}
                try {
                    bCreated = b.getCreatedAtTimestamp().getSeconds();
                } catch (Exception e) {}
                return Long.compare(bCreated, aCreated);
            });
            if (filteredRecent.size() > 3) filteredRecent = filteredRecent.subList(0, 3);
            if (recentAdapter == null) {
                recentAdapter = new ListingAdapter(
                    requireContext(),
                    filteredRecent,
                    listing -> navigateToItemDetail(listing.getId())
                );
                recentRecyclerView.setAdapter(recentAdapter);
            } else {
                recentAdapter.setListings(filteredRecent);
            }
        }
    }

    // Filter listings by all selected categories (multi-select)
    private void filterListingsByCategories(List<String> categoryNames) {
        Log.d("CategoryFilter", "Filtering for categories: " + categoryNames);
        // Personalized
        List<ListingModel> filteredPersonalized = ListingFilterUtil.filterListingsByCategoriesForList(
            allListings, categoryNames, allItems
        );
        Log.d("CategoryFilter", "Filtered personalized size: " + filteredPersonalized.size());
        personalizedAdapter = new ListingAdapter(
            requireContext(),
            filteredPersonalized,
            listing -> navigateToItemDetail(listing.getId())
        );
        personalizedRecyclerView.setAdapter(personalizedAdapter);
    }


    private void navigateToCategoryListing(CategoryModel category) {
        Bundle args = new Bundle();
        args.putString("categoryId", category.getId());
        args.putString("categoryName", category.getName());
        navController.navigate(R.id.action_nav_recommendations_to_categoryListingFragment, args);
    }

    private void navigateToItemDetail(String listingId) {
        Bundle args = new Bundle();
        args.putString("listingId", listingId);
        navController.navigate(R.id.action_nav_recommendations_to_itemDetailFragment, args);
    }

    // Haversine formula to calculate distance between two lat/lng points in km
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
