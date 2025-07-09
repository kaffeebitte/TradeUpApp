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

public class NearbyListingFragment extends Fragment {

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

        // Load nearby listings
        loadNearbyListings();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_listings);
        headerText = view.findViewById(R.id.header_text);
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);

        // Set up the recycler view with a grid layout
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        // Set header text
        if (headerText != null) {
            headerText.setText("Items Near You");
        }

        // Set up back navigation
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                // Navigate back when back button is clicked
                requireActivity().onBackPressed();
            });
        }
    }

    private void loadNearbyListings() {
        // Dummy data for demonstration (replace with real data from Firestore in production)
        List<ListingModel> nearbyListings = getDummyNearbyListings();
        ListingAdapter adapter = new ListingAdapter(
                requireContext(),
                nearbyListings,
                listing -> {
                    Bundle args = new Bundle();
                    args.putString("listingId", listing.getId());
                    navController.navigate(R.id.action_nearbyListingFragment_to_itemDetailFragment, args);
                }
        );
        recyclerView.setAdapter(adapter);
    }

    private List<ListingModel> getDummyNearbyListings() {
        List<ListingModel> listings = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            ListingModel listing = new ListingModel();
            listing.setId("nearby_listing_" + (i + 1));
            listing.setPrice(150000 + i * 40000);
            listing.setSellerId("seller_" + (i + 1));
            listing.setItemId("item_" + (i + 1));
            listings.add(listing);
        }
        return listings;
    }
}
