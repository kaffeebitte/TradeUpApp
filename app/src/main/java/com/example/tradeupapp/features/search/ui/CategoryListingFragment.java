package com.example.tradeupapp.features.search.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.example.tradeupapp.models.ListingModel;

import java.util.ArrayList;
import java.util.List;

public class CategoryListingFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView headerText;
    private ListingAdapter listingAdapter;
    private String categoryId;
    private String categoryName;
    private Toolbar toolbar;
    private FirebaseService firebaseService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId", null);
            categoryName = getArguments().getString("categoryName", "Category");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sử dụng layout fragment_listing.xml chung cho tất cả các màn listing
        return inflater.inflate(R.layout.fragment_listing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_listings);
        toolbar = view.findViewById(R.id.toolbar);
        headerText = view.findViewById(R.id.header_text);

        // Initialize Firebase service
        firebaseService = FirebaseService.getInstance();

        setupToolbar();
        loadListings();
    }

    private void setupToolbar() {
        if (toolbar != null && categoryName != null) {
            toolbar.setTitle(categoryName);
        }
        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    private void loadListings() {
        if (categoryName == null) {
            showEmptyState();
            return;
        }
        firebaseService.getListingsByCategory(categoryName, new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(List<ListingModel> listings) {
                if (listings.isEmpty()) {
                    showEmptyState();
                    return;
                }
                listingAdapter = new ListingAdapter(requireContext(), listings, listing -> navigateToItemDetail(listing.getId()));
                recyclerView.setAdapter(listingAdapter);
                headerText.setText(categoryName);
                headerText.setVisibility(View.VISIBLE);
            }
            @Override
            public void onError(String error) {
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        if (headerText != null) headerText.setText("No items found in this category");
        if (recyclerView != null) recyclerView.setAdapter(null);
    }

    private void navigateToItemDetail(String listingId) {
        Bundle args = new Bundle();
        args.putString("listingId", listingId);
        Navigation.findNavController(requireView()).navigate(R.id.action_categoryListingFragment_to_itemDetailFragment, args);
    }
}
