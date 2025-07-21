package com.example.tradeupapp.features.search.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.shared.adapters.ListingAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private TextInputEditText etSearch;
    private RecyclerView recyclerSearchSuggestions;
    private SearchSuggestionAdapter suggestionAdapter;
    private List<String> suggestions = new ArrayList<>();

    private RecyclerView searchResultsRecyclerView;
    private TextView resultsCountTextView;
    private MaterialButton filterButton;
    private MaterialButton sortButton;
    private ConstraintLayout emptySearchState;

    private ListingAdapter searchAdapter;
    private FirebaseService firebaseService;
    private List<ListingModel> allListings = new ArrayList<>();
    private List<ListingModel> filteredListings = new ArrayList<>();

    private String currentQuery = "";
    private String selectedFilter = "All";
    private String currentSort = "Relevance"; // Default sort

    private enum SortType {
        RELEVANCE, NEWEST_DESC, NEWEST_ASC, PRICE_ASC, PRICE_DESC
    }
    private SortType currentSortType = SortType.RELEVANCE;

    // Helper class for search logic
    private static class ListingWithItem {
        ListingModel listing;
        com.example.tradeupapp.models.ItemModel item;
        ListingWithItem(ListingModel listing, com.example.tradeupapp.models.ItemModel item) {
            this.listing = listing;
            this.item = item;
        }
    }
    // Store all joined listings+items for search
    private List<ListingWithItem> allListingWithItems = new ArrayList<>();

    // Helper methods for string matching
    private boolean contains(String field, String keyword) {
        return field != null && field.toLowerCase().contains(keyword);
    }
    private boolean containsList(List<String> list, String keyword) {
        if (list == null) return false;
        for (String s : list) {
            if (s != null && s.toLowerCase().contains(keyword)) return true;
        }
        return false;
    }

    // Debounce handler for search input
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // User location and min/max distance for filtering
    private Double userLat = null;
    private Double userLng = null;
    private Integer minDistanceKm = null;
    private Integer maxDistanceKm = null;

    // Filter state
    private String currentCategory = "All Categories";
    private double minPrice = 0.0;
    private double maxPrice = Double.MAX_VALUE;
    private String currentCondition = "Any Condition";

    private List<String> searchHistory = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseService = FirebaseService.getInstance();
        initViews(view);
        setupSearchUI();
        loadAllListingsAndItems();
        getParentFragmentManager().setFragmentResultListener("filters_applied", this, (requestKey, bundle) -> {
            currentCategory = bundle.getString("category", "All Categories");
            minPrice = bundle.getDouble("minPrice", 0.0);
            maxPrice = bundle.getDouble("maxPrice", Double.MAX_VALUE);
            currentCondition = bundle.getString("condition", "Any Condition");
            // Get min/max distance
            minDistanceKm = bundle.containsKey("minDistance") ? bundle.getInt("minDistance") : null;
            maxDistanceKm = bundle.containsKey("maxDistance") ? bundle.getInt("maxDistance") : null;
            // Get location if provided
            if (bundle.containsKey("latitude") && bundle.containsKey("longitude")) {
                userLat = bundle.getDouble("latitude");
                userLng = bundle.getDouble("longitude");
            }
            performSearch();
        });
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        recyclerSearchSuggestions = view.findViewById(R.id.recycler_search_suggestions);
        recyclerSearchSuggestions.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        suggestionAdapter = new SearchSuggestionAdapter(suggestions, suggestion -> {
            etSearch.setText(suggestion);
            etSearch.setSelection(suggestion.length());
            recyclerSearchSuggestions.setVisibility(View.GONE);
            currentQuery = suggestion;
            performSearch();
        });
        recyclerSearchSuggestions.setAdapter(suggestionAdapter);

        // Setup RecyclerView
        searchResultsRecyclerView = view.findViewById(R.id.recycler_search_results);
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        searchAdapter = new ListingAdapter(
                requireContext(),
                new ArrayList<>(),
                this::navigateToItemDetail
        );
        searchResultsRecyclerView.setAdapter(searchAdapter);

        // Setup filter button
        filterButton = view.findViewById(R.id.btn_filter);
        filterButton.setOnClickListener(v -> showFilterDialog());
        // Setup sort button
        sortButton = view.findViewById(R.id.btn_sort);
        sortButton.setOnClickListener(v -> showSortMenu());

        // Remove chipGroupFilters reference since scroll_chips and chip_group_filters are removed from layout
        emptySearchState = view.findViewById(R.id.empty_search_state);
        resultsCountTextView = view.findViewById(R.id.tv_results_count);
    }

    private void setupSearchUI() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim();
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> performSearch();
                searchHandler.postDelayed(searchRunnable, 200);
                showSuggestions(currentQuery);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Always reload search history from Firestore when focusing
                loadSearchHistory(() -> showSuggestions(""));
            } else {
                recyclerSearchSuggestions.setVisibility(View.GONE);
            }
        });
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                // Save search history only when user presses Enter/Search
                if (currentQuery != null && !currentQuery.isEmpty()) {
                    String userId = firebaseService.getCurrentUserId();
                    if (userId != null) {
                        firebaseService.addUserSearchHistory(userId, currentQuery, new FirebaseService.SimpleCallback() {
                            @Override
                            public void onSuccess() { /* No-op */ }
                            @Override
                            public void onError(String error) { /* Optionally log error */ }
                        });
                    }
                }
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void showSuggestions(String query) {
        loadSearchHistory(() -> {
            List<String> filtered = new ArrayList<>();
            for (String s : searchHistory) {
                if (query.isEmpty() || s.toLowerCase().contains(query.toLowerCase())) filtered.add(s);
            }
            suggestions.clear();
            suggestions.addAll(filtered);
            suggestionAdapter.updateSuggestions(suggestions);
            recyclerSearchSuggestions.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    private void loadSearchHistory(Runnable onLoaded) {
        String userId = firebaseService.getCurrentUserId();
        if (userId == null) { onLoaded.run(); return; }
        firebaseService.getUserSearchHistory(userId, new FirebaseService.SearchHistoryCallback() {
            @Override
            public void onSuccess(List<String> history) {
                searchHistory.clear();
                searchHistory.addAll(history);
                onLoaded.run();
            }
            @Override
            public void onFailure(Exception e) {
                onLoaded.run();
            }
        });
    }

    // Load all listings and join with their ItemModel for correct search
    private void loadAllListingsAndItems() {
        firebaseService.getAllListings(new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(List<ListingModel> listings) {
                allListingWithItems.clear();
                if (listings == null || listings.isEmpty()) {
                    filteredListings.clear();
                    searchAdapter.notifyDataSetChanged();
                    showEmptyState();
                    return;
                }
                final int total = listings.size();
                final int[] loaded = {0};
                for (ListingModel listing : listings) {
                    firebaseService.getItemById(listing.getItemId(), new FirebaseService.ItemCallback() {
                        @Override
                        public void onSuccess(com.example.tradeupapp.models.ItemModel item) {
                            allListingWithItems.add(new ListingWithItem(listing, item));
                            loaded[0]++;
                            if (loaded[0] == total) {
                                simpleSearch(currentQuery);
                            }
                        }
                        public void onError(String error) {
                            loaded[0]++;
                            if (loaded[0] == total) {
                                simpleSearch(currentQuery);
                            }
                        }
                    });
                }
            }
            @Override
            public void onError(String error) {
                filteredListings.clear();
                searchAdapter.notifyDataSetChanged();
                showEmptyState();
            }
        });
    }

    // Enhanced search logic: filter by keyword, category, price, condition, and distance
    private List<ListingModel> simpleSearch(String keyword) {
        String lowerKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        List<ListingModel> result = new ArrayList<>();
        for (ListingWithItem li : allListingWithItems) {
            if (li.item == null) continue;
            // Only include listings with status "available"
            if (li.listing == null || li.listing.getTransactionStatus() == null || !"available".equalsIgnoreCase(li.listing.getTransactionStatus())) continue;
            boolean matches = lowerKeyword.isEmpty() || simpleMatch(li, lowerKeyword);
            // Category filter
            if (!"All Categories".equals(currentCategory) && !currentCategory.equalsIgnoreCase(li.item.getCategory())) matches = false;
            // Price filter
            double price = li.listing.getPrice();
            if (price < minPrice || price > maxPrice) matches = false;
            // Condition filter
            if (!"Any Condition".equals(currentCondition) && !currentCondition.equalsIgnoreCase(li.item.getCondition())) matches = false;
            if (matches) result.add(li.listing);
        }
        return result;
    }

    private boolean simpleMatch(ListingWithItem li, String keyword) {
        if (contains(li.item.getTitle(), keyword)) return true;
        if (contains(li.item.getDescription(), keyword)) return true;
        if (contains(li.item.getCategory(), keyword)) return true;
        if (contains(li.item.getBrand(), keyword)) return true;
        if (containsList(li.item.getTags(), keyword)) return true;
        if (containsList(li.listing.getTags(), keyword)) return true;
        if (contains(li.item.getAddress(), keyword)) return true;
        return false;
    }

    private void showSortMenu() {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(requireContext(), sortButton);
        popup.getMenu().add("Most relevant");
        popup.getMenu().add("Newest to Oldest");
        popup.getMenu().add("Oldest to Newest");
        popup.getMenu().add("Price ascending (low to high)");
        popup.getMenu().add("Price descending (high to low)");
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            switch (title) {
                case "Newest to Oldest":
                    currentSortType = SortType.NEWEST_DESC;
                    break;
                case "Oldest to Newest":
                    currentSortType = SortType.NEWEST_ASC;
                    break;
                case "Price ascending (low to high)":
                    currentSortType = SortType.PRICE_ASC;
                    break;
                case "Price descending (high to low)":
                    currentSortType = SortType.PRICE_DESC;
                    break;
                case "Most relevant":
                default:
                    currentSortType = SortType.RELEVANCE;
                    break;
            }
            performSearch();
            return true;
        });
        popup.show();
    }

    // In performSearch, always use simpleSearch with joined data
    private void performSearch() {
        List<ListingModel> result = simpleSearch(currentQuery);
        // Filter by distance if userLat/userLng/minDistanceKm/maxDistanceKm set
        List<ListingModel> distanceFiltered = result;
        if (userLat != null && userLng != null && maxDistanceKm != null && maxDistanceKm > 0) {
            List<ListingWithItem> joined = new ArrayList<>();
            for (ListingModel listing : result) {
                for (ListingWithItem li : allListingWithItems) {
                    if (li.listing.getId().equals(listing.getId())) {
                        joined.add(li);
                        break;
                    }
                }
            }
            List<ListingModel> filtered = filterByDistanceRange(joined, userLat, userLng, minDistanceKm, maxDistanceKm);
            if (filtered != null) distanceFiltered = filtered;
        }
        sortResults(distanceFiltered);
        filteredListings.clear();
        filteredListings.addAll(distanceFiltered);
        updateUI();
    }

    // Fills the search bar with the given query and triggers a search
    public void fillSearchQuery(String query) {
        if (etSearch != null) {
            etSearch.setText(query);
        }
        currentQuery = query;
        performSearch();
    }

    // Haversine formula for distance in km
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Filter by distance range after joining ListingModel with ItemModel
    private List<ListingModel> filterByDistanceRange(List<ListingWithItem> joined, Double userLat, Double userLng, Integer minDistanceKm, Integer maxDistanceKm) {
        if (userLat == null || userLng == null || maxDistanceKm == null || maxDistanceKm <= 0) return null;
        List<ListingModel> filtered = new ArrayList<>();
        for (ListingWithItem li : joined) {
            if (li.item != null) {
                Double lat = li.item.getLocationLatitude();
                Double lng = li.item.getLocationLongitude();
                if (lat != null && lng != null) {
                    double dist = haversine(userLat, userLng, lat, lng);
                    if ((minDistanceKm == null || dist >= minDistanceKm) && dist <= maxDistanceKm) {
                        filtered.add(li.listing);
                    }
                }
            }
        }
        return filtered;
    }

    // Helper to extract timestamp (milliseconds) from ListingModel.createdAt
    private long getCreatedAtMillis(ListingModel listing) {
        Object createdAt = listing.getCreatedAt();
        if (createdAt == null) return 0L;
        if (createdAt instanceof Long) {
            return (Long) createdAt;
        } else if (createdAt instanceof com.google.firebase.Timestamp) {
            return ((com.google.firebase.Timestamp) createdAt).toDate().getTime();
        } else if (createdAt instanceof String) {
            try {
                // Try parse ISO8601 string using ThreeTenBP for API < 26
                org.threeten.bp.Instant instant = org.threeten.bp.Instant.parse((String) createdAt);
                return instant.toEpochMilli();
            } catch (Exception e) {
                try {
                    return Long.parseLong((String) createdAt);
                } catch (Exception ignored) {}
            }
        }
        return 0L;
    }

    private void sortResults(List<ListingModel> listings) {
        switch (currentSortType) {
            case NEWEST_DESC:
                listings.sort((a, b) -> Long.compare(getCreatedAtMillis(b), getCreatedAtMillis(a)));
                break;
            case NEWEST_ASC:
                listings.sort((a, b) -> Long.compare(getCreatedAtMillis(a), getCreatedAtMillis(b)));
                break;
            case PRICE_ASC:
                listings.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
                break;
            case PRICE_DESC:
                listings.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                break;
            case RELEVANCE:
            default:
                // Relevance: giữ nguyên thứ tự tìm kiếm (hoặc có thể bổ sung thuật toán nâng cao nếu cần)
                break;
        }
    }

    private void updateUI() {
        if (filteredListings.isEmpty()) {
            showEmptyState();
        } else {
            showResults();
        }
        String countText = filteredListings.size() + " results found";
        if (!currentQuery.isEmpty()) {
            countText += " for \"" + currentQuery + "\"";
        }
        resultsCountTextView.setText(countText);
        searchAdapter.updateListings(filteredListings);
    }

    private void showResults() {
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
        emptySearchState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        searchResultsRecyclerView.setVisibility(View.GONE);
        emptySearchState.setVisibility(View.VISIBLE);
    }

    private void showFilterDialog() {
        FilterBottomSheetFragment filterFragment = new FilterBottomSheetFragment();
        filterFragment.setFilterAppliedListener((keyword, category, minPrice, maxPrice, condition, distance, sortBy) -> {
            Bundle bundle = new Bundle();
            bundle.putString("category", category);
            bundle.putDouble("minPrice", minPrice);
            bundle.putDouble("maxPrice", maxPrice);
            bundle.putString("condition", condition);
            bundle.putInt("distance", distance);
            // Add latitude and longitude to bundle if available
            if (userLat != null && userLng != null) {
                bundle.putDouble("latitude", userLat);
                bundle.putDouble("longitude", userLng);
            }
            getParentFragmentManager().setFragmentResult("filters_applied", bundle);
        });
        filterFragment.show(getParentFragmentManager(), filterFragment.getTag());
    }

    private void applyAdvancedFilters(String keyword, String filterCategory, double minPrice,
                                     double maxPrice, String condition, int distance, String sortBy) {
        if (keyword != null && !keyword.isEmpty()) {
            currentQuery = keyword;
            etSearch.setText(keyword);
        }
        // Always use the latest userLat, userLng, maxDistanceKm
        Double maxDistance = (distance > 0) ? (double) distance : maxDistanceKm;
        firebaseService.searchListingsWithFilters(currentQuery, filterCategory, minPrice, maxPrice,
                condition, sortBy, userLat, userLng, maxDistance, new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(List<ListingModel> listings) {
                if (getActivity() != null && isAdded()) {
                    // Nếu có filter theo vị trí, lọc lại bằng filterByDistance nếu backend chưa lọc đủ
                    List<ListingModel> result = listings;
                    if (userLat != null && userLng != null && maxDistance != null && maxDistance > 0) {
                        // Join lại với ItemModel để lọc chính xác
                        List<ListingWithItem> joined = new ArrayList<>();
                        for (ListingModel listing : listings) {
                            for (ListingWithItem li : allListingWithItems) {
                                if (li.listing.getId().equals(listing.getId())) {
                                    joined.add(li);
                                    break;
                                }
                            }
                        }
                        // Use filterByDistanceRange instead of filterByDistance, and cast maxDistance to Integer
                        Integer minDist = minDistanceKm;
                        Integer maxDist = (maxDistance != null) ? maxDistance.intValue() : null;
                        List<ListingModel> filtered = filterByDistanceRange(joined, userLat, userLng, minDist, maxDist);
                        if (filtered != null) result = filtered;
                    }
                    filteredListings = new ArrayList<>(result);
                    updateUI();
                    Toast.makeText(requireContext(),
                            "Advanced filters applied: " + filteredListings.size() + " listings found",
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), "Error applying filters: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navigateToItemDetail(ListingModel listing) {
        Bundle args = new Bundle();
        args.putString("listingId", listing.getId());
        Navigation.findNavController(requireView()).navigate(R.id.action_nav_search_to_itemDetailFragment, args);
    }

    // Add a method to set user location and distance from UI/filter dialog
    public void setUserLocationAndDistance(Double lat, Double lng, Double maxDistKm) {
        this.userLat = lat;
        this.userLng = lng;
        this.maxDistanceKm = (maxDistKm != null) ? maxDistKm.intValue() : null;
    }
    // Xóa showSearchHistoryDialog và SearchHistoryDialogFragment liên quan
}
