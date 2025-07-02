package com.example.tradeupapp.features.search.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.shared.adapters.ListingAdapter;
import com.example.tradeupapp.models.ItemModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private SearchBar searchBar;
    private SearchView searchView;
    private RecyclerView searchResultsRecyclerView;
    private TextView resultsCountTextView;
    private MaterialButton filterSortButton;
    private ChipGroup chipGroupFilters;
    private ConstraintLayout emptySearchState;

    private ListingAdapter searchAdapter;
    private FirebaseService firebaseService;
    private List<ItemModel> allItems = new ArrayList<>();
    private List<ItemModel> filteredItems = new ArrayList<>();

    private String currentQuery = "";
    private String selectedFilter = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase service
        firebaseService = FirebaseService.getInstance();

        // Initialize views
        initViews(view);

        // Setup search functionality
        setupSearchListener();

        // Setup filter chips
        setupFilterChips();

        // Load all items initially
        loadAllItems();
    }

    private void initViews(View view) {
        searchBar = view.findViewById(R.id.search_bar);
        searchView = view.findViewById(R.id.search_view);
        searchResultsRecyclerView = view.findViewById(R.id.recycler_search_results);
        resultsCountTextView = view.findViewById(R.id.tv_results_count);
        filterSortButton = view.findViewById(R.id.btn_filter_sort);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        emptySearchState = view.findViewById(R.id.empty_search_state);

        // Setup RecyclerView
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        searchAdapter = new ListingAdapter(
                requireContext(),
                new ArrayList<>(),
                this::navigateToItemDetail
        );
        searchResultsRecyclerView.setAdapter(searchAdapter);

        // Setup filter button
        filterSortButton.setOnClickListener(v -> showFilterDialog());
    }

    private void setupSearchListener() {
        // Setup SearchBar click to expand SearchView
        searchBar.setOnClickListener(v -> searchView.show());

        // Setup SearchView text change listener
        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim();
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup search submit
        searchView.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            searchView.hide();
            return true;
        });
    }

    private void setupFilterChips() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = requireView().findViewById(checkedIds.get(0));
                selectedFilter = selectedChip.getText().toString();
                performSearch(); // Apply search with new filter
            }
        });
    }

    private void loadAllItems() {
        firebaseService.getAllItems(new FirebaseService.ItemsCallback() {
            @Override
            public void onSuccess(List<ItemModel> items) {
                if (getActivity() != null && isAdded()) {
                    allItems = new ArrayList<>(items);
                    filteredItems = new ArrayList<>(items);
                    updateUI();
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), "Error loading items: " + error, Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }
        });
    }

    private void performSearch() {
        if (currentQuery.isEmpty() && selectedFilter.equals("All")) {
            // Show all items when no search query and no specific filter
            filteredItems = new ArrayList<>(allItems);
            updateUI();
        } else {
            // Use Firestore search with filters for better performance
            searchWithFirestore();
        }
    }

    private void searchWithFirestore() {
        // Determine category filter based on chip selection
        String categoryFilter = null;
        double minPrice = 0;
        double maxPrice = Double.MAX_VALUE;
        String conditionFilter = null;
        String sortBy = "Newest First";

        // Apply chip-based filters
        switch (selectedFilter) {
            case "Electronics":
            case "Clothing":
            case "Furniture":
            case "Books":
            case "Sports":
                categoryFilter = selectedFilter;
                break;
            case "Low Price":
                maxPrice = 1000000; // Under 1M VND
                break;
            case "High Price":
                minPrice = 1000000; // Over 1M VND
                break;
            case "New Condition":
                conditionFilter = "New";
                break;
            case "Recent":
                sortBy = "Newest First";
                break;
            case "Nearby":
                // For now, just use default behavior - can be enhanced with location
                break;
        }

        firebaseService.searchItemsWithFilters(currentQuery, categoryFilter, minPrice, maxPrice,
                conditionFilter, sortBy, new FirebaseService.ItemsCallback() {
            @Override
            public void onSuccess(List<ItemModel> items) {
                if (getActivity() != null && isAdded()) {
                    filteredItems = new ArrayList<>(items);
                    updateUI();
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), "Error searching items: " + error, Toast.LENGTH_SHORT).show();
                    // Fallback to client-side search
                    performClientSideSearch();
                }
            }
        });
    }

    private void performClientSideSearch() {
        if (currentQuery.isEmpty()) {
            filteredItems = new ArrayList<>(allItems);
        } else {
            filteredItems = new ArrayList<>();
            for (ItemModel item : allItems) {
                if (matchesSearchQuery(item, currentQuery)) {
                    filteredItems.add(item);
                }
            }
        }
        applyFilters();
    }

    private boolean matchesSearchQuery(ItemModel item, String query) {
        String lowerQuery = query.toLowerCase();
        return (item.getTitle() != null && item.getTitle().toLowerCase().contains(lowerQuery)) ||
                (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerQuery)) ||
                (item.getCategory() != null && item.getCategory().toLowerCase().contains(lowerQuery)) ||
                (item.getTag() != null && item.getTag().toLowerCase().contains(lowerQuery));
    }

    private void applyFilters() {
        List<ItemModel> filtered = new ArrayList<>();

        for (ItemModel item : filteredItems) {
            boolean matches = false;

            switch (selectedFilter) {
                case "All":
                    matches = true;
                    break;
                case "Nearby":
                    // For now, just include all items - can be enhanced with location logic
                    matches = true;
                    break;
                case "Recent":
                    // Items are already sorted by date (newest first)
                    matches = true;
                    break;
                case "Low Price":
                    matches = item.getPrice() <= 1000000; // Under 1M VND
                    break;
                case "High Price":
                    matches = item.getPrice() > 1000000; // Over 1M VND
                    break;
                case "New Condition":
                    matches = "New".equalsIgnoreCase(item.getCondition()) ||
                            "Like New".equalsIgnoreCase(item.getCondition());
                    break;
                case "Electronics":
                case "Clothing":
                case "Furniture":
                case "Books":
                case "Sports":
                    matches = selectedFilter.equalsIgnoreCase(item.getCategory());
                    break;
                default:
                    matches = true;
                    break;
            }

            if (matches) {
                filtered.add(item);
            }
        }

        filteredItems = filtered;
        updateUI();
    }

    private void updateUI() {
        if (filteredItems.isEmpty()) {
            showEmptyState();
        } else {
            showResults();
        }

        // Update results count
        String countText = filteredItems.size() + " results found";
        if (!currentQuery.isEmpty()) {
            countText += " for \"" + currentQuery + "\"";
        }
        resultsCountTextView.setText(countText);

        // Update adapter
        searchAdapter.updateItems(filteredItems);
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
        // Create and show the filter bottom sheet
        FilterBottomSheetFragment filterSheet = new FilterBottomSheetFragment();

        filterSheet.setFilterAppliedListener((keyword, filterCategory, minPrice, maxPrice,
                                             condition, distance, sortBy) -> {
            // Apply advanced filters using Firestore
            applyAdvancedFilters(keyword, filterCategory, minPrice, maxPrice, condition, sortBy);
        });

        filterSheet.show(getChildFragmentManager(), "FilterBottomSheet");
    }

    private void applyAdvancedFilters(String keyword, String filterCategory, double minPrice,
                                     double maxPrice, String condition, String sortBy) {
        // Update current query if keyword is provided
        if (keyword != null && !keyword.isEmpty()) {
            currentQuery = keyword;
            searchView.getEditText().setText(keyword);
        }

        firebaseService.searchItemsWithFilters(currentQuery, filterCategory, minPrice, maxPrice,
                condition, sortBy, new FirebaseService.ItemsCallback() {
            @Override
            public void onSuccess(List<ItemModel> items) {
                if (getActivity() != null && isAdded()) {
                    filteredItems = new ArrayList<>(items);
                    updateUI();

                    Toast.makeText(requireContext(),
                            "Advanced filters applied: " + items.size() + " items found",
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

    private void navigateToItemDetail(ItemModel item) {
        Bundle args = new Bundle();
        args.putString("itemId", item.getId());
        Navigation.findNavController(requireView()).navigate(R.id.action_nav_search_to_itemDetailFragment, args);
    }
}
