package com.example.tradeupapp.features.listing.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.features.listing.adapter.UserListingAdapter;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.features.common.ListFilterBottomSheetFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ManageListingsFragment extends Fragment implements ListFilterBottomSheetFragment.FilterAppliedListener {

    private RecyclerView recyclerView;
    private UserListingAdapter adapter;
    private List<ItemModel> allListings;
    private List<ItemModel> filteredListings;
    private LinearLayout emptyStateView;
    private MaterialButton btnCreateListing;
    private ExtendedFloatingActionButton fabAddListing;
    private FloatingActionButton fabFilterListings;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_listings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_user_listings);
        emptyStateView = view.findViewById(R.id.empty_state);
        btnCreateListing = view.findViewById(R.id.btn_create_listing);
        fabAddListing = view.findViewById(R.id.fab_add_listing);
        fabFilterListings = view.findViewById(R.id.fab_filter_listings);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize navigation controller
        navController = Navigation.findNavController(view);

        // Set up click listeners for adding new listings
        fabAddListing.setOnClickListener(v -> navigateToAddItem());
        btnCreateListing.setOnClickListener(v -> navigateToAddItem());

        // Set up filter button click listener
        fabFilterListings.setOnClickListener(v -> showFilterBottomSheet());

        // Load user listings (using dummy data for now)
        loadUserListings();
    }

    private void showFilterBottomSheet() {
        ListFilterBottomSheetFragment filterSheet = new ListFilterBottomSheetFragment();
        filterSheet.setFilterAppliedListener(this);
        filterSheet.show(getParentFragmentManager(), "FilterBottomSheet");
    }

    private void loadUserListings() {
        // In a real app, you would fetch this data from your backend or local database
        allListings = createSampleData();
        filteredListings = new ArrayList<>(allListings);

        // Set up the adapter
        adapter = new UserListingAdapter(requireContext(), filteredListings, new UserListingAdapter.OnItemActionListener() {
            @Override
            public void onView(ItemModel item) {
                // Navigate to item details/preview
                Bundle args = new Bundle();
                args.putParcelable("item", item);
                navController.navigate(R.id.action_manageListingsFragment_to_itemPreviewFragment, args);
            }

            @Override
            public void onEdit(ItemModel item) {
                // Navigate to edit item screen with the item data
                Bundle args = new Bundle();
                args.putParcelable("item", item);
                navController.navigate(R.id.action_manageListingsFragment_to_nav_add, args);
            }

            @Override
            public void onDelete(ItemModel item) {
                showDeleteConfirmationDialog(item);
            }
        });

        recyclerView.setAdapter(adapter);

        // Update UI based on whether we have listings
        updateEmptyState();
    }

    private List<ItemModel> createSampleData() {
        List<ItemModel> items = new ArrayList<>();

        // Create items with different statuses, dates, and prices for filtering
        ItemModel item1 = new ItemModel("MacBook Pro M2", "Powerful laptop with 16GB RAM", 1299.99, "Electronics", "Like New", "Available", new ArrayList<>());
        item1.setDateAdded(new Date()); // Today
        items.add(item1);

        ItemModel item2 = new ItemModel("PS5 Console", "PlayStation 5 with 2 controllers", 450.00, "Gaming", "Good", "Available", new ArrayList<>());
        // Set date to 5 days ago
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -5);
        item2.setDateAdded(cal.getTime());
        items.add(item2);

        ItemModel item3 = new ItemModel("Leather Jacket", "Vintage black leather jacket", 120.00, "Clothing", "Good", "Sold", new ArrayList<>());
        // Set date to 15 days ago
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -15);
        item3.setDateAdded(cal.getTime());
        items.add(item3);

        ItemModel item4 = new ItemModel("Mountain Bike", "Trek mountain bike, barely used", 350.00, "Sports", "Like New", "Reserved", new ArrayList<>());
        // Set date to 25 days ago
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -25);
        item4.setDateAdded(cal.getTime());
        items.add(item4);

        ItemModel item5 = new ItemModel("Dining Table", "Wooden dining table with 6 chairs", 249.99, "Furniture", "Good", "Available", new ArrayList<>());
        // Set date to 40 days ago
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -40);
        item5.setDateAdded(cal.getTime());
        items.add(item5);

        return items;
    }

    private void showDeleteConfirmationDialog(ItemModel item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Listing")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Remove the item from the list
                    allListings.remove(item);
                    filteredListings.remove(item);
                    adapter.notifyDataSetChanged();

                    // In a real app, you would also delete from your backend/database
                    // apiService.deleteItem(item.getId());

                    // Update empty state if needed
                    updateEmptyState();

                    // Show confirmation message
                    showFeedbackMessage("Item deleted successfully");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void navigateToAddItem() {
        navController.navigate(R.id.action_manageListingsFragment_to_nav_add);
    }

    private void updateEmptyState() {
        if (filteredListings.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
            fabAddListing.setVisibility(View.GONE);
            fabFilterListings.setVisibility(View.GONE); // Ẩn nút filter khi không có dữ liệu
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
            fabAddListing.setVisibility(View.VISIBLE);
            fabFilterListings.setVisibility(View.VISIBLE); // Hiển thị nút filter khi có dữ liệu
        }
    }

    private void showFeedbackMessage(String message) {
        // In a real app, you might use a Snackbar or Toast
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFiltersApplied(List<String> statusList, String dateRange, double minPrice, double maxPrice, String sortBy) {
        // Apply filters to listings
        applyFilters(statusList, dateRange, minPrice, maxPrice, sortBy);

        // Show feedback
        Toast.makeText(requireContext(), "Filters applied", Toast.LENGTH_SHORT).show();
    }

    private void applyFilters(List<String> statusList, String dateRange, double minPrice, double maxPrice, String sortBy) {
        // Start with all items
        filteredListings = new ArrayList<>(allListings);

        // Filter by status if not "All"
        if (!statusList.contains("All")) {
            filteredListings = filteredListings.stream()
                    .filter(item -> statusList.contains(item.getStatus()))
                    .collect(Collectors.toList());
        }

        // Filter by date range
        filteredListings = filterByDateRange(filteredListings, dateRange);

        // Filter by price range
        filteredListings = filteredListings.stream()
                .filter(item -> item.getPrice() >= minPrice && item.getPrice() <= maxPrice)
                .collect(Collectors.toList());

        // Apply sorting
        sortItems(filteredListings, sortBy);

        // Update adapter and refresh
        adapter.updateItems(filteredListings);

        // Update UI state
        updateEmptyState();
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
