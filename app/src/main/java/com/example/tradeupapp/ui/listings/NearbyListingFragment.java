package com.example.tradeupapp.ui.listings;

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
import com.example.tradeupapp.adapters.ListingAdapter;
import com.example.tradeupapp.models.ItemModel;

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

        // Load nearby items
        loadNearbyItems();
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

    private void loadNearbyItems() {
        // Get dummy nearby items
        List<ItemModel> nearbyItems = getDummyNearbyItems();

        // Create adapter with the items
        ListingAdapter adapter = new ListingAdapter(
                requireContext(),
                nearbyItems,
                item -> {
                    // Navigate to item detail
                    Bundle args = new Bundle();
                    args.putString("itemId", item.getId());
                    navController.navigate(R.id.action_nearbyListingFragment_to_itemDetailFragment, args);
                }
        );

        // Set adapter to recycler view
        recyclerView.setAdapter(adapter);
    }

    private List<ItemModel> getDummyNearbyItems() {
        List<ItemModel> items = new ArrayList<>();

        // Nearby items dummy data
        String[] titles = {
                "IKEA Desk", "Bookshelf", "Coffee Table",
                "Dining Set", "Office Chair", "Floor Lamp",
                "Outdoor Grill", "Patio Furniture", "Bicycle",
                "Lawn Mower", "Garden Tools Set", "Snowblower"
        };

        double[] prices = {
                99.99, 79.99, 149.99, 299.99, 129.99, 59.99,
                199.99, 349.99, 189.99, 249.99, 89.99, 399.99
        };

        String[] conditions = {
                "Good", "Used", "Like New", "Good", "Used", "Excellent",
                "Good", "Like New", "Used", "Good", "Used", "Like New"
        };

        String[] categories = {
                "Furniture", "Furniture", "Furniture", "Furniture", "Furniture", "Home",
                "Outdoor", "Outdoor", "Sports", "Outdoor", "Tools", "Outdoor"
        };

        // Create item models with the data
        for (int i = 0; i < titles.length; i++) {
            ItemModel item = new ItemModel();
            item.setId("nearby_" + (i + 1));
            item.setTitle(titles[i]);
            item.setPrice(prices[i]);
            item.setCondition(conditions[i]);
            item.setCategory(categories[i]);
            item.setStatus("Available");
            item.setViewCount(30 + (int)(Math.random() * 150));
            item.setInteractionCount(5 + (int)(Math.random() * 30));

            items.add(item);
        }

        return items;
    }
}
