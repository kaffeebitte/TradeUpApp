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
import com.example.tradeupapp.shared.adapters.ListingAdapter;
import com.example.tradeupapp.models.ListingModel;
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
    private List<ListingModel> allSavedListings;
    private List<ListingModel> filteredListings;

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
        allSavedListings = createSampleData();
        filteredListings = new ArrayList<>(allSavedListings);

        // Create and set adapter
        adapter = new ListingAdapter(requireContext(), filteredListings, this::onItemClicked);
        recyclerView.setAdapter(adapter);

        // Update UI
        updateUIState();
    }

    private void onItemClicked(ListingModel listing) {
        // Navigate to item detail
        Bundle args = new Bundle();
        args.putString("listingId", listing.getId());
        androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(requireView());
        navController.navigate(R.id.action_savedItemsFragment_to_itemDetailFragment, args);
    }

    private void updateUIState() {
        if (filteredListings.isEmpty()) {
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

    private List<ListingModel> createSampleData() {
        List<ListingModel> listings = new ArrayList<>();
        // Example dummy listings (replace with real ListingModel data)
        for (int i = 0; i < 5; i++) {
            ListingModel listing = new ListingModel();
            listing.setId("listing_" + (i + 1));
            listing.setPrice(100 + i * 50);
            listing.setSellerId("seller_" + (i + 1));
            listing.setItemId("item_" + (i + 1));
            listing.setViewCount(10 * (i + 1));
            // Set createdAt to different dates
            com.google.firebase.Timestamp ts = new com.google.firebase.Timestamp(new java.util.Date(System.currentTimeMillis() - i * 86400000L));
            listing.setCreatedAt(ts);
            listings.add(listing);
        }
        return listings;
    }

    @Override
    public void onFiltersApplied(List<String> statusList, String dateRange, double minPrice, double maxPrice, String sortBy) {
        // Apply filters to saved items
        applyFilters(statusList, dateRange, minPrice, maxPrice, sortBy);

        // Show feedback
        Toast.makeText(requireContext(), "Filters applied", Toast.LENGTH_SHORT).show();
    }

    private void applyFilters(List<String> statusList, String dateRange, double minPrice, double maxPrice, String sortBy) {
        // Start with all listings
        filteredListings = new ArrayList<>(allSavedListings);

        // Filter by status if not "All"
        if (!statusList.contains("All")) {
            filteredListings = filteredListings.stream()
                    .filter(listing -> statusList.contains(listing.getTransactionStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Filter by date range
        filteredListings = filterByDateRange(filteredListings, dateRange);

        // Filter by price range
        filteredListings = filteredListings.stream()
                .filter(listing -> listing.getPrice() >= minPrice && listing.getPrice() <= maxPrice)
                .collect(java.util.stream.Collectors.toList());

        // Apply sorting
        sortItems(filteredListings, sortBy);

        // Update adapter
        adapter.updateListings(filteredListings);

        // Update UI state
        updateUIState();
    }

    private List<ListingModel> filterByDateRange(List<ListingModel> listings, String dateRange) {
        if (dateRange.equals("All Time")) {
            return listings;
        }
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        java.util.Date currentDate = calendar.getTime();
        calendar = java.util.Calendar.getInstance();
        if (dateRange.equals("Today")) {
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calendar.set(java.util.Calendar.MINUTE, 0);
            calendar.set(java.util.Calendar.SECOND, 0);
        } else if (dateRange.equals("This Week")) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -7);
        } else if (dateRange.equals("This Month")) {
            calendar.add(java.util.Calendar.MONTH, -1);
        }
        java.util.Date startDate = calendar.getTime();
        return listings.stream()
                .filter(listing -> {
                    com.google.firebase.Timestamp ts = listing.getCreatedAt();
                    java.util.Date listingDate = ts != null ? ts.toDate() : null;
                    return listingDate != null && !listingDate.before(startDate) && !listingDate.after(currentDate);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private void sortItems(List<ListingModel> listings, String sortBy) {
        switch (sortBy) {
            case "Newest First":
                listings.sort((l1, l2) -> l2.getCreatedAt().toDate().compareTo(l1.getCreatedAt().toDate()));
                break;
            case "Oldest First":
                listings.sort((l1, l2) -> l1.getCreatedAt().toDate().compareTo(l2.getCreatedAt().toDate()));
                break;
            case "Price: Low to High":
                listings.sort((l1, l2) -> Double.compare(l1.getPrice(), l2.getPrice()));
                break;
            case "Price: High to Low":
                listings.sort((l1, l2) -> Double.compare(l2.getPrice(), l1.getPrice()));
                break;
            case "Most Popular":
                listings.sort((l1, l2) -> Integer.compare(l2.getViewCount(), l1.getViewCount()));
                break;
        }
    }
}
