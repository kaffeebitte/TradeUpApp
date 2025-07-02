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
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.shared.adapters.ListingAdapter;
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
    private FirebaseService firebaseService;

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

        // Initialize Firebase service
        firebaseService = FirebaseService.getInstance();

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
        // Load items from Firestore based on category
        firebaseService.getItemsByCategory(category, new FirebaseService.ItemsCallback() {
            @Override
            public void onSuccess(List<ItemModel> items) {
                if (getActivity() != null && isAdded()) {
                    if (items.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyStateTextView.setVisibility(View.VISIBLE);
                        emptyStateTextView.setText(getString(R.string.no_items_found_in_category, category));
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyStateTextView.setVisibility(View.GONE);

                        adapter = new ListingAdapter(requireContext(), items, CategoryListingFragment.this::navigateToItemDetail);
                        recyclerView.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), "Error loading items: " + error, Toast.LENGTH_SHORT).show();

                    // Show empty state on error
                    recyclerView.setVisibility(View.GONE);
                    emptyStateTextView.setVisibility(View.VISIBLE);
                    emptyStateTextView.setText("Failed to load items. Please try again.");
                }
            }
        });
    }

    private void navigateToItemDetail(ItemModel item) {
        // Navigate to item detail
        Bundle args = new Bundle();
        args.putString("itemId", item.getId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_categoryListingFragment_to_itemDetailFragment, args);
    }

    private void showFilterDialog() {
        // Create and show the filter bottom sheet
        FilterBottomSheetFragment filterSheet = new FilterBottomSheetFragment();

        filterSheet.setFilterAppliedListener((keyword, filterCategory, minPrice, maxPrice,
                                             condition, distance, sortBy) -> {
            // Apply the filters to the category listings using Firestore search
            applyFilters(keyword, filterCategory, minPrice, maxPrice, condition, sortBy);
        });

        filterSheet.show(getChildFragmentManager(), "FilterBottomSheet");
    }

    private void applyFilters(String keyword, String filterCategory, double minPrice,
                             double maxPrice, String condition, String sortBy) {
        // Use the current category if no specific category filter is applied
        String searchCategory = (filterCategory != null && !filterCategory.equals("All Categories"))
                               ? filterCategory : category;

        firebaseService.searchItemsWithFilters(keyword, searchCategory, minPrice, maxPrice,
                                             condition, sortBy, new FirebaseService.ItemsCallback() {
            @Override
            public void onSuccess(List<ItemModel> items) {
                if (getActivity() != null && isAdded()) {
                    if (items.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyStateTextView.setVisibility(View.VISIBLE);
                        emptyStateTextView.setText("No items found with the applied filters.");
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyStateTextView.setVisibility(View.GONE);

                        adapter = new ListingAdapter(requireContext(), items, CategoryListingFragment.this::navigateToItemDetail);
                        recyclerView.setAdapter(adapter);
                    }

                    Toast.makeText(requireContext(),
                            "Filters applied: " + items.size() + " items found",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), "Error applying filters: " + error, Toast.LENGTH_SHORT).show();
                    // Reload original category items on filter error
                    loadListings();
                }
            }
        });
    }

    private void setupFilterButton() {
        fabFilter.setOnClickListener(v -> {
            showFilterDialog();
        });
    }
}
