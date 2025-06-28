package com.example.tradeupapp.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.adapters.ListingAdapter;
import com.example.tradeupapp.models.ItemModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private MaterialToolbar searchToolbar;
    private EditText searchEditText;
    private ImageButton clearSearchButton;
    private RecyclerView searchResultsRecyclerView;
    private FloatingActionButton filterButton;
    private ListingAdapter searchAdapter;
    private List<ItemModel> searchResults;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo các view
        initViews(view);

        // Thiết lập sự kiện tìm kiếm
        setupSearchListener();

        // Có thể hiển thị các mục tìm kiếm gần đây hoặc phổ biến
        showRecentSearches();
    }

    private void initViews(View view) {
        searchToolbar = view.findViewById(R.id.search_toolbar);
        // Update to use the correct SearchBar ID from layout
        searchEditText = view.findViewById(R.id.search_bar);
        // The clear button appears to be missing in the layout - you may need to handle this differently
        // clearSearchButton = view.findViewById(R.id.btn_clear_search);
        searchResultsRecyclerView = view.findViewById(R.id.recycler_search_results);
        filterButton = view.findViewById(R.id.fab_filter);

        // Setup RecyclerView with GridLayoutManager for search results
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        // Initialize adapter with empty list
        searchAdapter = new ListingAdapter(
                requireContext(),
                new ArrayList<>(),
                this::navigateToItemDetail
        );
        searchResultsRecyclerView.setAdapter(searchAdapter);

        // Setup filter button click listener
        if (filterButton != null) {
            filterButton.setOnClickListener(v -> showFilterBottomSheet());
        }

        // Setup clear search button click listener
        if (clearSearchButton != null) {
            clearSearchButton.setOnClickListener(v -> {
                searchEditText.setText("");
                clearSearchButton.setVisibility(View.GONE);
            });
        }
    }

    private void setupSearchListener() {
        // Set up toolbar navigation (back button)
        searchToolbar.setNavigationOnClickListener(v -> {
            // Handle back navigation
            requireActivity().onBackPressed();
        });

        // Set up search EditText
        if (searchEditText != null) {
            // Handle IME action (search button on keyboard)
            searchEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = textView.getText().toString().trim();
                    if (!query.isEmpty()) {
                        performSearch(query);
                    }
                    return true;
                }
                return false;
            });

            // Text change listener for search EditText
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // No action needed
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Show/hide clear button based on text content
                    if (s.length() > 0) {
                        clearSearchButton.setVisibility(View.VISIBLE);
                    } else {
                        clearSearchButton.setVisibility(View.GONE);
                        showRecentSearches();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // No action needed
                }
            });
        }
    }

    private void performSearch(String query) {
        // Simulate search results with dummy data
        searchResults = getSearchResults(query);

        // Update adapter with new search results
        searchAdapter = new ListingAdapter(
                requireContext(),
                searchResults,
                this::navigateToItemDetail
        );
        searchResultsRecyclerView.setAdapter(searchAdapter);

        // Show a toast with the number of results
        Toast.makeText(requireContext(),
                "Found " + searchResults.size() + " results for '" + query + "'",
                Toast.LENGTH_SHORT).show();
    }

    private void showRecentSearches() {
        // Show popular items as suggestions
        List<ItemModel> popularItems = getPopularItems();
        searchAdapter = new ListingAdapter(
                requireContext(),
                popularItems,
                this::navigateToItemDetail
        );
        searchResultsRecyclerView.setAdapter(searchAdapter);
    }

    private void navigateToItemDetail(ItemModel item) {
        // Navigate to item detail page
        Toast.makeText(requireContext(), "Selected: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        // TODO: Navigate to item detail when available
    }

    private void showFilterBottomSheet() {
        FilterBottomSheetFragment filterSheet = new FilterBottomSheetFragment();
        filterSheet.setFilterAppliedListener((keyword, category, minPrice, maxPrice, condition, distance, sortBy) -> {
            // Apply the filters to the search results
            applyFilters(keyword, category, minPrice, maxPrice, condition, distance, sortBy);
        });
        filterSheet.show(getChildFragmentManager(), "FilterBottomSheet");
    }

    private void applyFilters(String keyword, String category, double minPrice,
                             double maxPrice, String condition, int distance, String sortBy) {
        // Apply filters to search results
        // In a real app, this would query a database or API with the filter parameters

        // For now, we'll just show a toast with the filter parameters
        String filterSummary = String.format("Filters applied - Category: %s, Price: $%.2f-$%.2f, Condition: %s",
                                            category, minPrice, maxPrice, condition);
        Toast.makeText(requireContext(), filterSummary, Toast.LENGTH_LONG).show();

        // Simulate filtered results
        List<ItemModel> filteredResults = getFilteredResults(keyword, category, minPrice, maxPrice, condition);

        // Update adapter with filtered results
        searchAdapter = new ListingAdapter(
                requireContext(),
                filteredResults,
                this::navigateToItemDetail
        );
        searchResultsRecyclerView.setAdapter(searchAdapter);
    }

    private List<ItemModel> getFilteredResults(String keyword, String category,
                                              double minPrice, double maxPrice, String condition) {
        // In a real app, this would filter from a database or API
        // For demo purposes, we'll create a filtered dummy list
        List<ItemModel> results = new ArrayList<>();

        // Create 4 items matching the filters
        for (int i = 1; i <= 4; i++) {
            ItemModel item = new ItemModel();
            item.setTitle(category + " Item " + i);

            // Set price within the filter range
            double price = minPrice + (Math.random() * (maxPrice - minPrice));
            // If maxPrice is Integer.MAX_VALUE, set a reasonable price
            if (maxPrice > 10000) {
                price = minPrice + (Math.random() * 500);
            }
            item.setPrice(price);

            item.setCategory(category);
            item.setCondition(condition);
            item.setStatus("Available");
            item.setViewCount((int)(Math.random() * 100));
            item.setInteractionCount((int)(Math.random() * 20));

            results.add(item);
        }

        return results;
    }

    // Helper method to generate search results based on query
    private List<ItemModel> getSearchResults(String query) {
        List<ItemModel> results = new ArrayList<>();
        // Simulate 6 search results
        for (int i = 1; i <= 6; i++) {
            ItemModel item = new ItemModel();
            item.setTitle(query + " Result " + i);
            item.setPrice(75.0 * i);
            item.setStatus(i % 3 == 0 ? "Sold" : "Available");
            item.setViewCount(15 * i);
            item.setInteractionCount(i * 2);
            results.add(item);
        }
        return results;
    }

    // Helper method to get popular items for initial display
    private List<ItemModel> getPopularItems() {
        List<ItemModel> items = new ArrayList<>();
        // Simulate 4 popular items
        for (int i = 1; i <= 4; i++) {
            ItemModel item = new ItemModel();
            item.setTitle("Popular Item " + i);
            item.setPrice(100.0 * i);
            item.setStatus("Available");
            item.setViewCount(50 * i);
            item.setInteractionCount(10 * i);
            items.add(item);
        }
        return items;
    }
}
