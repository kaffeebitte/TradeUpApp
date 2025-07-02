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
import com.example.tradeupapp.models.ItemModel;

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

        // Load recent items
        loadRecentItems();
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

    private void loadRecentItems() {
        // Get dummy recent items
        List<ItemModel> recentItems = getDummyRecentItems();

        // Create adapter with the items
        ListingAdapter adapter = new ListingAdapter(
                requireContext(),
                recentItems,
                item -> {
                    // Navigate to item detail
                    Bundle args = new Bundle();
                    args.putString("itemId", item.getId());
                    navController.navigate(R.id.action_recentListingFragment_to_itemDetailFragment, args);
                }
        );

        // Set adapter to recycler view
        recyclerView.setAdapter(adapter);
    }

    private List<ItemModel> getDummyRecentItems() {
        List<ItemModel> items = new ArrayList<>();

        // Recent items dummy data
        String[] titles = {
                "Nintendo Switch", "Bluetooth Speaker", "Air Fryer",
                "Yoga Mat", "Mountain Bike", "Tennis Racket",
                "Electric Skateboard", "Basketball Hoop", "Camping Tent",
                "Smart Watch", "Wireless Earbuds", "Digital Camera"
        };

        double[] prices = {
                279.99, 69.99, 89.99, 29.99, 349.99, 59.99,
                399.99, 149.99, 199.99, 249.99, 129.99, 349.99
        };

        String[] conditions = {
                "New", "Like New", "New", "Good", "Used", "Excellent",
                "Like New", "Good", "Used", "New", "Like New", "Good"
        };

        String[] categories = {
                "Gaming", "Electronics", "Home", "Sports", "Sports", "Sports",
                "Sports", "Sports", "Outdoor", "Electronics", "Electronics", "Electronics"
        };

        // Create item models with the data
        for (int i = 0; i < titles.length; i++) {
            ItemModel item = new ItemModel();
            item.setId("recent_" + (i + 1));
            item.setTitle(titles[i]);
            item.setPrice(prices[i]);
            item.setCondition(conditions[i]);
            item.setCategory(categories[i]);
            item.setStatus("Available");
            item.setViewCount(10 + (int)(Math.random() * 50));
            item.setInteractionCount(1 + (int)(Math.random() * 15));

            items.add(item);
        }

        return items;
    }
}
