package com.example.tradeupapp.ui.profile;

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
import com.example.tradeupapp.models.ItemModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ManageListingsFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserListingAdapter adapter;
    private List<ItemModel> dummyList;
    private LinearLayout emptyStateView;
    private MaterialButton btnCreateListing;
    private ExtendedFloatingActionButton fabAddListing;
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

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize navigation controller
        navController = Navigation.findNavController(view);

        // Set up click listeners for adding new listings
        fabAddListing.setOnClickListener(v -> navigateToAddItem());
        btnCreateListing.setOnClickListener(v -> navigateToAddItem());

        // Load user listings (using dummy data for now)
        loadUserListings();
    }

    private void loadUserListings() {
        // In a real app, you would fetch this data from your backend or local database
        dummyList = new ArrayList<>();

        // For testing, uncomment this to simulate having listings
        dummyList.add(new ItemModel("MacBook Pro M2", "Powerful laptop with 16GB RAM", 1299.99, "Electronics", "Like New", "San Francisco, CA", new ArrayList<>()));
        dummyList.add(new ItemModel("PS5 Console", "PlayStation 5 with 2 controllers", 450.00, "Gaming", "Good", "Los Angeles, CA", new ArrayList<>()));

        // Set up the adapter
        adapter = new UserListingAdapter(requireContext(), dummyList, new UserListingAdapter.OnItemActionListener() {
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

    private void showDeleteConfirmationDialog(ItemModel item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Listing")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Remove the item from the list
                    dummyList.remove(item);
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
        if (dummyList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
            fabAddListing.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
            fabAddListing.setVisibility(View.VISIBLE);
        }
    }

    private void showFeedbackMessage(String message) {
        // In a real app, you might use a Snackbar or Toast
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
