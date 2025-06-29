package com.example.tradeupapp.features.home.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.features.listing.adapter.ListingAdapter;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.features.common.ListFilterBottomSheetFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SavedItemsFragment extends Fragment implements ListFilterBottomSheetFragment.FilterAppliedListener {

    private RecyclerView recyclerView;
    private MaterialTextView emptyStateTextView;
    private FloatingActionButton fabFilter;
    private ListingAdapter adapter;
    private List<ItemModel> allSavedItems;
    private List<ItemModel> filteredItems;

    public static SavedItemsFragment newInstance() {
        return new SavedItemsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved_items, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view);
        emptyStateTextView = view.findViewById(R.id.empty_state_text);
        fabFilter = view.findViewById(R.id.fab_filter);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Load saved items data
        loadSavedItems();

        // Setup filter button click listener
        fabFilter.setOnClickListener(v -> showFilterBottomSheet());
    }

    private void showFilterBottomSheet() {
        ListFilterBottomSheetFragment filterSheet = new ListFilterBottomSheetFragment();
        filterSheet.setFilterAppliedListener(this);
        filterSheet.show(getParentFragmentManager(), "FilterBottomSheet");
    }

    private void loadSavedItems() {
        // In a real app, load from database or API
        // For now, create sample data
        allSavedItems = createSampleData();
        filteredItems = new ArrayList<>(allSavedItems);

        // Create and set adapter
        adapter = new ListingAdapter(requireContext(), filteredItems, this::onItemClicked);
        recyclerView.setAdapter(adapter);

        // Update UI
        updateUIState();
    }

    private void onItemClicked(ItemModel item) {
        // Handle item click
        Toast.makeText(requireContext(), "Clicked: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        // Navigate to item detail (will be implemented later)
    }

    private void updateUIState() {
        if (filteredItems.isEmpty()) {
            showEmptyState();
        } else {
            showContent();
        }
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.VISIBLE);
        emptyStateTextView.setText(R.string.no_saved_items);
    }

    private void showContent() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateTextView.setVisibility(View.GONE);
    }

    private List<ItemModel> createSampleData() {
        List<ItemModel> items = new ArrayList<>();

        // Add sample items with different properties for filtering
        ItemModel item1 = new ItemModel("iPhone 13 Pro", "64GB Graphite", 899.99, "Electronics", "Like New", "Available", new ArrayList<>());
        item1.setDateAdded(new Date()); // Today
        items.add(item1);

        ItemModel item2 = new ItemModel("Sony Headphones", "WH-1000XM4", 299.99, "Electronics", "Good", "Available", new ArrayList<>());
        // Set date to 3 days ago
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -3);
        item2.setDateAdded(cal.getTime());
        items.add(item2);

        ItemModel item3 = new ItemModel("Nike Air Jordan", "Size 10.5, Red/Black", 199.99, "Clothing", "New", "Sold", new ArrayList<>());
        // Set date to 10 days ago
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -10);
        item3.setDateAdded(cal.getTime());
        items.add(item3);

        ItemModel item4 = new ItemModel("Ikea Desk Lamp", "LED Adjustable", 49.99, "Home", "Good", "Reserved", new ArrayList<>());
        // Set date to 30 days ago
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -30);
        item4.setDateAdded(cal.getTime());
        items.add(item4);

        ItemModel item5 = new ItemModel("Gaming Mouse", "Logitech G502", 79.99, "Electronics", "Like New", "Available", new ArrayList<>());
        // Set date to 45 days ago
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -45);
        item5.setDateAdded(cal.getTime());
        items.add(item5);

        return items;
    }

    @Override
    public void onFiltersApplied(List<String> statusList, String dateRange, double minPrice, double maxPrice, String sortBy) {
        // Apply filters to saved items
        applyFilters(statusList, dateRange, minPrice, maxPrice, sortBy);

        // Show feedback
        Toast.makeText(requireContext(), "Filters applied", Toast.LENGTH_SHORT).show();
    }

    private void applyFilters(List<String> statusList, String dateRange, double minPrice, double maxPrice, String sortBy) {
        // Start with all items
        filteredItems = new ArrayList<>(allSavedItems);

        // Filter by status if not "All"
        if (!statusList.contains("All")) {
            filteredItems = filteredItems.stream()
                    .filter(item -> statusList.contains(item.getStatus()))
                    .collect(Collectors.toList());
        }

        // Filter by date range
        filteredItems = filterByDateRange(filteredItems, dateRange);

        // Filter by price range
        filteredItems = filteredItems.stream()
                .filter(item -> item.getPrice() >= minPrice && item.getPrice() <= maxPrice)
                .collect(Collectors.toList());

        // Apply sorting
        sortItems(filteredItems, sortBy);

        // Update adapter
        adapter.updateItems(filteredItems);

        // Update UI state
        updateUIState();
    }

    private List<ItemModel> filterByDateRange(List<ItemModel> items, String dateRange) {
        if (dateRange.equals("All Time")) {
            return items;
        }

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        // Calculate start date based on selected range
        calendar = Calendar.getInstance();
        if (dateRange.equals("Today")) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        } else if (dateRange.equals("This Week")) {
            calendar.add(Calendar.DAY_OF_YEAR, -7);
        } else if (dateRange.equals("This Month")) {
            calendar.add(Calendar.MONTH, -1);
        }
        Date startDate = calendar.getTime();

        // Filter items by date
        return items.stream()
                .filter(item -> {
                    Date itemDate = item.getDateAdded();
                    return itemDate != null && !itemDate.before(startDate) && !itemDate.after(currentDate);
                })
                .collect(Collectors.toList());
    }

    private void sortItems(List<ItemModel> items, String sortBy) {
        switch (sortBy) {
            case "Newest First":
                items.sort((item1, item2) -> item2.getDateAdded().compareTo(item1.getDateAdded()));
                break;
            case "Oldest First":
                items.sort((item1, item2) -> item1.getDateAdded().compareTo(item2.getDateAdded()));
                break;
            case "Price: Low to High":
                items.sort((item1, item2) -> Double.compare(item1.getPrice(), item2.getPrice()));
                break;
            case "Price: High to Low":
                items.sort((item1, item2) -> Double.compare(item2.getPrice(), item1.getPrice()));
                break;
            case "Most Popular":
                items.sort((item1, item2) -> Integer.compare(item2.getViewCount(), item1.getViewCount()));
                break;
        }
    }
}
