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

public class RecommendedListingFragment extends Fragment {

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

        // Load recommended items
        loadRecommendedItems();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_listings);
        headerText = view.findViewById(R.id.header_text);
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);

        // Set up the recycler view with a grid layout
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        // Set header text
        if (headerText != null) {
            headerText.setText("Recommended For You");
        }

        // Set up back navigation
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                // Navigate back when back button is clicked
                requireActivity().onBackPressed();
            });
        }
    }

    private void loadRecommendedItems() {
        // Get dummy recommended items
        List<ItemModel> recommendedItems = getDummyRecommendedItems();

        // Create adapter with the items
        ListingAdapter adapter = new ListingAdapter(
                requireContext(),
                recommendedItems,
                item -> {
                    // Navigate to item detail
                    Bundle args = new Bundle();
                    args.putString("itemId", item.getId());
                    navController.navigate(R.id.action_recommendedListingFragment_to_itemDetailFragment, args);
                }
        );

        // Set adapter to recycler view
        recyclerView.setAdapter(adapter);
    }

    private List<ItemModel> getDummyRecommendedItems() {
        List<ItemModel> items = new ArrayList<>();

        // Recommended items dummy data
        String[] titles = {
                "MacBook Pro M1", "Sony PlayStation 5", "Nike Air Jordan",
                "iPhone 13 Pro", "Samsung QLED TV", "Dyson V11 Vacuum",
                "Bose QuietComfort 45", "Kindle Paperwhite", "Nintendo Switch OLED",
                "Canon EOS R5", "GoPro HERO11", "Sonos Arc Soundbar"
        };

        double[] prices = {
                1299.99, 499.99, 159.99, 999.99, 1199.99, 599.99,
                329.99, 149.99, 349.99, 3899.99, 499.99, 899.99
        };

        String[] conditions = {
                "Excellent", "Like New", "Good", "Like New", "New", "Good",
                "Like New", "Good", "Excellent", "New", "Like New", "Excellent"
        };

        String[] categories = {
                "Electronics", "Gaming", "Clothing", "Electronics", "Electronics", "Home",
                "Electronics", "Electronics", "Gaming", "Electronics", "Electronics", "Electronics"
        };

        // Create item models with the data
        for (int i = 0; i < titles.length; i++) {
            ItemModel item = new ItemModel();
            item.setId("recommended_" + (i + 1));
            item.setTitle(titles[i]);
            item.setPrice(prices[i]);
            item.setCondition(conditions[i]);
            item.setCategory(categories[i]);
            item.setStatus("Available");
            item.setViewCount(50 + (int)(Math.random() * 200));
            item.setInteractionCount(10 + (int)(Math.random() * 40));

            items.add(item);
        }

        return items;
    }
}
