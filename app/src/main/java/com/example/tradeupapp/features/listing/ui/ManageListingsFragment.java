package com.example.tradeupapp.features.listing.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.shared.adapters.UserListingAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ManageListingsFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserListingAdapter adapter;
    private FloatingActionButton fabAddListing, fabFilterListings;
    private Button btnCreateListing;
    private NavController navController;
    private FirebaseService firebaseService;

    private List<ItemModel> allListings = new ArrayList<>();
    private List<ItemModel> filteredListings = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_listings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase service
        firebaseService = FirebaseService.getInstance();

        // Initialize views
        initViews(view);

        // Initialize NavController
        navController = Navigation.findNavController(view);

        // Set up RecyclerView
        setupRecyclerView();

        // Set up click listeners for adding new listings
        fabAddListing.setOnClickListener(v -> navigateToAddItem());
        btnCreateListing.setOnClickListener(v -> navigateToAddItem());

        // Set up filter button click listener (simplified for now)
        fabFilterListings.setOnClickListener(v -> showSimpleFilter());

        // Load user listings from Firebase
        loadUserListings();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_user_listings);
        fabAddListing = view.findViewById(R.id.fab_add_listing);
        fabFilterListings = view.findViewById(R.id.fab_filter_listings);
        btnCreateListing = view.findViewById(R.id.btn_create_listing);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void showSimpleFilter() {
        // Simple filter implementation - you can enhance this later
        Toast.makeText(getContext(), "Filter functionality - coming soon", Toast.LENGTH_SHORT).show();
    }

    private void loadUserListings() {
        String currentUserId = firebaseService.getCurrentUserId();
        if (currentUserId == null) {
            Toast.makeText(getActivity(), "Please log in to view your listings", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseService.getUserItems(currentUserId, new FirebaseService.ItemsCallback() {
            @Override
            public void onSuccess(List<ItemModel> items) {
                if (getActivity() != null && isAdded()) {
                    allListings = items;
                    filteredListings = new ArrayList<>(allListings);

                    // Set up the adapter if not already initialized
                    if (adapter == null) {
                        setupAdapter();
                    } else {
                        adapter.updateItems(filteredListings);
                    }

                    // Show/hide empty state
                    updateEmptyState();
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), "Error loading listings: " + error, Toast.LENGTH_SHORT).show();
                    // Show empty state
                    updateEmptyState();
                }
            }
        });
    }

    private void setupAdapter() {
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
                // Navigate to edit item - use the correct navigation action
                Bundle args = new Bundle();
                args.putParcelable("item", item);
                args.putBoolean("isEdit", true);
                navController.navigate(R.id.action_manageListingsFragment_to_nav_add, args);
            }

            @Override
            public void onDelete(ItemModel item) {
                // Show confirmation dialog and delete item
                showDeleteConfirmationDialog(item);
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void updateEmptyState() {
        // Show/hide empty state views based on whether we have listings
        // This would be implemented based on your layout
        if (filteredListings.isEmpty()) {
            // Show empty state
            btnCreateListing.setVisibility(View.VISIBLE);
        } else {
            // Hide empty state
            btnCreateListing.setVisibility(View.GONE);
        }
    }

    private void showDeleteConfirmationDialog(ItemModel item) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
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

    private void showFeedbackMessage(String message) {
        // In a real app, you might use a Snackbar or Toast
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}
