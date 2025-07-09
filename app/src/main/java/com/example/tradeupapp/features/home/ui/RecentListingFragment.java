package com.example.tradeupapp.features.home.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.shared.adapters.ListingAdapter;
import com.example.tradeupapp.models.ListingModel;

import java.util.ArrayList;
import java.util.List;

public class RecentListingFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView headerText;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize navigation controller
        navController = Navigation.findNavController(view);

        // Initialize views
        initViews(view);

        // Load recent listings
        loadRecentListings();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_listings);
        headerText = view.findViewById(R.id.header_text);
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);

        // Set up the recycler view with a grid layout
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        // Set header text
        if (headerText != null) {
            headerText.setText("Recently Added");
        }

        // Set up back navigation
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                // Navigate back when back button is clicked
                requireActivity().onBackPressed();
            });
        }
    }

    private void loadRecentListings() {
        // Dummy data for demonstration (replace with real data from Firestore in production)
        List<ListingModel> recentListings = getDummyRecentListings();
        ListingAdapter adapter = new ListingAdapter(
                requireContext(),
                recentListings,
                listing -> {
                    // Navigate to listing detail, pass listingId
                    Bundle args = new Bundle();
                    args.putString("listingId", listing.getId());
                    navController.navigate(R.id.action_recentListingFragment_to_itemDetailFragment, args);
                }
        );
        recyclerView.setAdapter(adapter);
    }

    private List<ListingModel> getDummyRecentListings() {
        List<ListingModel> listings = new ArrayList<>();
        // Example dummy listings (replace with real ListingModel data)
        for (int i = 0; i < 12; i++) {
            ListingModel listing = new ListingModel();
            listing.setId("listing_" + (i + 1));
            listing.setPrice(100000 + i * 50000);
            listing.setSellerId("seller_" + (i + 1));
            listing.setItemId("item_" + (i + 1));
            listings.add(listing);
        }
        return listings;
    }
}
