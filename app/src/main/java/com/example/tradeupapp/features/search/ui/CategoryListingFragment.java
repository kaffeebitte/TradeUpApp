package com.example.tradeupapp.features.search.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.features.listing.adapter.ListingAdapter;
import com.example.tradeupapp.models.ItemModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CategoryListingFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyStateTextView;
    private ListingAdapter adapter;
    private String category;
    private Toolbar toolbar;
    private FloatingActionButton fabFilter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString("category", "All Items");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_listing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupToolbar();
        loadListings();
        setupFilterButton();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_category_items);
        emptyStateTextView = view.findViewById(R.id.tv_empty_state);
        toolbar = view.findViewById(R.id.toolbar);
        fabFilter = view.findViewById(R.id.fab_filter);

        // Configure RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
    }

    private void setupToolbar() {
        // Set category name as title
        toolbar.setTitle(category);

        // Set up back navigation
        toolbar.setNavigationOnClickListener(v -> {
            // Navigate back when back button is clicked
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    private void loadListings() {
        // In a real app, you would fetch items from a database or API based on the category
        List<ItemModel> items = getDummyCategoryItems();

        if (items.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateTextView.setVisibility(View.VISIBLE);
            emptyStateTextView.setText(getString(R.string.no_items_found_in_category, category));
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateTextView.setVisibility(View.GONE);

            adapter = new ListingAdapter(requireContext(), items, this::navigateToItemDetail);
            recyclerView.setAdapter(adapter);
        }
    }

    private void navigateToItemDetail(ItemModel item) {
        // Navigate to item detail
        Bundle args = new Bundle();
        args.putString("itemId", "dummy_id"); // In a real app, use the actual item ID
        Navigation.findNavController(requireView())
                .navigate(R.id.action_categoryListingFragment_to_itemDetailFragment, args);

        // For demonstration purposes, show a toast
        Toast.makeText(requireContext(), "Selected: " + item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void showFilterDialog() {
        // Create and show the filter bottom sheet
        FilterBottomSheetFragment filterSheet = new FilterBottomSheetFragment();

        filterSheet.setFilterAppliedListener((keyword, filterCategory, minPrice, maxPrice,
                                             condition, distance, sortBy) -> {
            // Apply the filters to the category listings
            Toast.makeText(requireContext(),
                    "Filters applied to " + category + " category",
                    Toast.LENGTH_SHORT).show();

            // In a real app, you would filter the results based on these parameters
            // For demo purposes, just reload with the same dummy data
            loadCategoryItems();
        });

        filterSheet.show(getChildFragmentManager(), "FilterBottomSheet");
    }

    private void setupFilterButton() {
        fabFilter.setOnClickListener(v -> {
            showFilterDialog();
        });
    }

    private void loadCategoryItems() {
        // This method reloads items after filters are applied
        // For now, just call the original method
        loadListings();
    }

    private List<ItemModel> getDummyCategoryItems() {
        List<ItemModel> items = new ArrayList<>();

        // Create 6-10 dummy items for this category
        int itemCount = 6 + (int)(Math.random() * 5);
        for (int i = 1; i <= itemCount; i++) {
            ItemModel item = new ItemModel();
            item.setTitle(category + " Item " + i);
            item.setPrice(50.0 * i);

            // Add some variety to statuses
            if (i % 5 == 0) {
                item.setStatus("Sold");
            } else if (i % 3 == 0) {
                item.setStatus("Paused");
            } else {
                item.setStatus("Available");
            }

            item.setCategory(category);
            item.setCondition(i % 2 == 0 ? "New" : "Used");
            item.setViewCount(10 * i);
            item.setInteractionCount(i);

            items.add(item);
        }

        return items;
    }
}
