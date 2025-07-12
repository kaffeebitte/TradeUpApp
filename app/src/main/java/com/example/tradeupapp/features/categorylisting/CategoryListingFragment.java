package com.example.tradeupapp.features.categorylisting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.shared.adapters.ListingAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class CategoryListingFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView headerText;
    private MaterialToolbar toolbar;
    private ListingAdapter listingAdapter;
    private FirebaseService firebaseService;
    private String categoryName;
    private String categoryId;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_listings);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        headerText = view.findViewById(R.id.header_text);
        toolbar = view.findViewById(R.id.toolbar);
        navController = Navigation.findNavController(view);
        firebaseService = FirebaseService.getInstance();

        if (getArguments() != null) {
            categoryName = getArguments().getString("categoryName", "");
            categoryId = getArguments().getString("categoryId", "");
        }
        if (categoryName != null && !categoryName.isEmpty()) {
            headerText.setText(categoryName);
            toolbar.setTitle(categoryName);
        } else {
            headerText.setText("Category Listings");
            toolbar.setTitle("Category Listings");
        }
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        loadListings();
    }

    private void loadListings() {
        if (categoryName == null || categoryName.isEmpty()) {
            Toast.makeText(getContext(), "No category selected", Toast.LENGTH_SHORT).show();
            return;
        }
        // 1. Lấy tất cả items thuộc category này
        firebaseService.getItemsByCategory(categoryName, new FirebaseService.ItemsCallback() {
            @Override
            public void onSuccess(List<com.example.tradeupapp.models.ItemModel> items) {
                if (items == null || items.isEmpty()) {
                    android.util.Log.d("CategoryListingFragment", "No items found for category: " + categoryName);
                    recyclerView.setAdapter(null);
                    headerText.setText("No items found in this category");
                    return;
                }
                // 2. Lấy tất cả listings
                firebaseService.getAllListings(new FirebaseService.ListingsCallback() {
                    @Override
                    public void onSuccess(List<ListingModel> listings) {
                        // 3. Join listings với items qua itemId
                        java.util.Set<String> itemIds = new java.util.HashSet<>();
                        for (com.example.tradeupapp.models.ItemModel item : items) {
                            itemIds.add(item.getId());
                        }
                        List<ListingModel> filtered = new java.util.ArrayList<>();
                        for (ListingModel listing : listings) {
                            if (itemIds.contains(listing.getItemId())) {
                                filtered.add(listing);
                            }
                        }
                        android.util.Log.d("CategoryListingFragment", "Filtered listings count: " + filtered.size());
                        listingAdapter = new ListingAdapter(requireContext(), filtered, listing -> navigateToItemDetail(listing.getId()));
                        recyclerView.setAdapter(listingAdapter);
                        if (filtered.isEmpty()) {
                            headerText.setText("No listings found in this category");
                        }
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), "Error loading listings: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error loading items: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToItemDetail(String listingId) {
        Bundle args = new Bundle();
        args.putString("listingId", listingId);
        navController.navigate(R.id.action_categoryListingFragment_to_itemDetailFragment, args);
    }
}
