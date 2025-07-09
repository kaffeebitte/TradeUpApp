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
import com.example.tradeupapp.models.ListingModel;
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
    private List<ListingModel> allListings = new ArrayList<>();
    private List<ListingModel> filteredListings = new ArrayList<>();

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
        firebaseService.getAllListings(new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(List<ListingModel> listings) {
                if (getActivity() != null && isAdded()) {
                    allListings = new ArrayList<>(listings);
                    filteredListings = new ArrayList<>(listings);
                    updateUI();
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), "Error loading listings: " + error, Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }
        });
    }

    private void performSearch() {
        if (currentQuery.isEmpty() && selectedFilter.equals("All")) {
            filteredListings = new ArrayList<>(allListings);
            updateUI();
        } else {
            searchWithFirestore();
        }
    }

    private void searchWithFirestore() {
        String categoryFilter = null;
        double minPrice = 0;
        double maxPrice = Double.MAX_VALUE;
        String conditionFilter = null;
        String sortBy = "Newest First";
        switch (selectedFilter) {
            case "Electronics":
            case "Clothing":
            case "Furniture":
            case "Books":
            case "Sports":
                categoryFilter = selectedFilter;
                break;
            case "Low Price":
                maxPrice = 1000000;
                break;
            case "High Price":
                minPrice = 1000000;
                break;
            case "New Condition":
                conditionFilter = "New";
                break;
            case "Recent":
                sortBy = "Newest First";
                break;
            case "Nearby":
                break;
        }
        firebaseService.searchListingsWithFilters(currentQuery, categoryFilter, minPrice, maxPrice,
                conditionFilter, sortBy, new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(List<ListingModel> listings) {
                if (getActivity() != null && isAdded()) {
                    filteredListings = new ArrayList<>(listings);
                    updateUI();
                }
            }
            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), "Error searching listings: " + error, Toast.LENGTH_SHORT).show();
                    performClientSideSearch();
                }
            }
        });
    }

    private void performClientSideSearch() {
        if (currentQuery.isEmpty()) {
            filteredListings = new ArrayList<>(allListings);
        } else {
            filteredListings = new ArrayList<>();
            for (ListingModel listing : allListings) {
                if (matchesSearchQuery(listing, currentQuery)) {
                    filteredListings.add(listing);
                }
            }
        }
        applyFilters();
    }

    private boolean matchesSearchQuery(ListingModel listing, String query) {
        String lowerQuery = query.toLowerCase();
        // Only matches tags and category here; for title/desc, fetch ItemModel if needed
        boolean tagMatch = false;
        for (String tag : listing.getTags()) {
            if (tag != null && tag.toLowerCase().contains(lowerQuery)) {
                tagMatch = true;
                break;
            }
        }
        return (listing.getTags() != null && tagMatch) ||
                (listing.getItemId() != null && listing.getItemId().toLowerCase().contains(lowerQuery)) ||
                (listing.getSellerId() != null && listing.getSellerId().toLowerCase().contains(lowerQuery)) ||
                (listing.getTransactionStatus() != null && listing.getTransactionStatus().toLowerCase().contains(lowerQuery));
    }

    private void applyFilters() {
        List<ListingModel> filtered = new ArrayList<>();
        for (ListingModel listing : filteredListings) {
            boolean matches = false;
            switch (selectedFilter) {
                case "All":
                case "Nearby":
                case "Recent":
                    matches = true;
                    break;
                case "Low Price":
                    matches = listing.getPrice() <= 1000000;
                    break;
                case "High Price":
                    matches = listing.getPrice() > 1000000;
                    break;
                case "New Condition":
                    matches = "New".equalsIgnoreCase(listing.getTransactionStatus());
                    break;
                case "Electronics":
                case "Clothing":
                case "Furniture":
                case "Books":
                case "Sports":
                    matches = categoryFilterMatch(listing, selectedFilter);
                    break;
                default:
                    matches = true;
                    break;
            }
            if (matches) {
                filtered.add(listing);
            }
        }
        filteredListings = filtered;
        updateUI();
    }
    private boolean categoryFilterMatch(ListingModel listing, String category) {
        // You may need to fetch ItemModel for real category, here just return true
        return true;
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
        firebaseService.searchListingsWithFilters(currentQuery, filterCategory, minPrice, maxPrice,
                condition, sortBy, new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(List<ListingModel> listings) {
                if (getActivity() != null && isAdded()) {
                    filteredListings = new ArrayList<>(listings);
                    updateUI();
                    Toast.makeText(requireContext(),
                            "Advanced filters applied: " + listings.size() + " listings found",
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
}
