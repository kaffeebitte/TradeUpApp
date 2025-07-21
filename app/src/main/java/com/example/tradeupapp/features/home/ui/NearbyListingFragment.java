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
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.shared.adapters.ListingAdapter;
import com.example.tradeupapp.utils.ListingFilterUtil;

import java.util.ArrayList;
import java.util.List;

public class NearbyListingFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView headerText;
    private NavController navController;
    private FirebaseService firebaseService;
    private List<ListingModel> allListings = new ArrayList<>();
    private List<ItemModel> allItems = new ArrayList<>();
    private List<String> selectedCategories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        initViews(view);
        firebaseService = FirebaseService.getInstance();
        if (getArguments() != null && getArguments().containsKey("selectedCategories")) {
            selectedCategories = getArguments().getStringArrayList("selectedCategories");
        } else {
            selectedCategories.add("All Categories");
        }
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
        firebaseService.getAllListings(new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(List<ListingModel> listings) {
                allListings = new ArrayList<>(listings);
                firebaseService.getAllItems(new FirebaseService.ItemsCallback() {
                    @Override
                    public void onSuccess(List<ItemModel> items) {
                        allItems = new ArrayList<>(items);
                        // Refactored: use ListingFilterUtil for category filtering
                        List<ListingModel> filtered = ListingFilterUtil.filterListingsByCategoriesForList(
                                allListings, selectedCategories, allItems
                        );
                        ListingAdapter adapter = new ListingAdapter(
                                requireContext(),
                                filtered,
                                listing -> {
                                    Bundle args = new Bundle();
                                    args.putString("listingId", listing.getId());
                                    navController.navigate(R.id.action_nearbyListingFragment_to_itemDetailFragment, args);
                                }
                        );
                        recyclerView.setAdapter(adapter);
                    }
                    @Override
                    public void onError(String error) {
                        // handle error
                    }
                });
            }
            @Override
            public void onError(String error) {
                // handle error
            }
        });
    }
}
