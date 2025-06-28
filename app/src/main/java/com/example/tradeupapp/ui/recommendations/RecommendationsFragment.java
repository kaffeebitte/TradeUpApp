package com.example.tradeupapp.ui.recommendations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.adapters.CategoryAdapter;
import com.example.tradeupapp.adapters.ListingAdapter;
import com.example.tradeupapp.models.CategoryModel;
import com.example.tradeupapp.models.ItemModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class RecommendationsFragment extends Fragment {

    private RecyclerView categoriesRecyclerView;
    private RecyclerView personalizedRecyclerView;
    private RecyclerView nearbyRecyclerView;
    private RecyclerView recentRecyclerView;

    private TextView seeAllCategories;
    private TextView seeAllRecommended;
    private TextView seeAllNearby;
    private TextView seeAllRecent;

    private MaterialButton filterButton;

    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recommendations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize navigation controller
        navController = Navigation.findNavController(view);

        // Initialize views
        initViews(view);

        // Set up click listeners
        setupClickListeners();

        // Load dummy data
        loadDummyData();
    }

    private void initViews(View view) {
        // RecyclerViews
        categoriesRecyclerView = view.findViewById(R.id.recycler_categories);
        personalizedRecyclerView = view.findViewById(R.id.recycler_personalized);
        nearbyRecyclerView = view.findViewById(R.id.recycler_nearby);
        recentRecyclerView = view.findViewById(R.id.recycler_recent);

        // "See All" TextView buttons
        seeAllCategories = view.findViewById(R.id.tv_see_all_categories);
        seeAllRecommended = view.findViewById(R.id.tv_see_all_recommended);
        seeAllNearby = view.findViewById(R.id.tv_see_all_nearby);
        seeAllRecent = view.findViewById(R.id.tv_see_all_recent);

        // Filter button
        filterButton = view.findViewById(R.id.btn_filter);
    }

    private void setupClickListeners() {
        // "See All" buttons to navigate to respective listing pages
        seeAllCategories.setOnClickListener(v -> {
            // Navigate to all categories page
            navController.navigate(R.id.action_nav_recommendations_to_allCategoriesFragment);
        });

        seeAllRecommended.setOnClickListener(v -> {
            // Navigate to all recommended items
            navController.navigate(R.id.action_nav_recommendations_to_recommendedListingFragment);
        });

        seeAllNearby.setOnClickListener(v -> {
            // Navigate to all nearby items
            navController.navigate(R.id.action_nav_recommendations_to_nearbyListingFragment);
        });

        seeAllRecent.setOnClickListener(v -> {
            // Navigate to all recent items
            navController.navigate(R.id.action_nav_recommendations_to_recentListingFragment);
        });

        // Filter button click listener
        filterButton.setOnClickListener(v -> {
            showFilterDialog();
        });
    }

    private void showFilterDialog() {
        // Show filter dialog/bottom sheet
        Toast.makeText(requireContext(), "Opening filter options", Toast.LENGTH_SHORT).show();
        // In a real implementation, you would show a bottom sheet or dialog with filter options
        // Example: FilterBottomSheetFragment filterSheet = new FilterBottomSheetFragment();
        // filterSheet.show(getChildFragmentManager(), "FilterBottomSheet");
    }

    private void loadDummyData() {
        // Load category data
        CategoryAdapter categoryAdapter = new CategoryAdapter(getDummyCategories(), category -> {
            // Navigate to category listing
            navigateToCategoryListing(category);
        });
        categoriesRecyclerView.setAdapter(categoryAdapter);

        // Load personalized recommendations
        ListingAdapter personalizedAdapter = new ListingAdapter(
                requireContext(),
                getDummyItems("Recommended"),
                this::navigateToItemDetail
        );
        personalizedRecyclerView.setAdapter(personalizedAdapter);

        // Load nearby items
        ListingAdapter nearbyAdapter = new ListingAdapter(
                requireContext(),
                getDummyItems("Nearby"),
                this::navigateToItemDetail
        );
        nearbyRecyclerView.setAdapter(nearbyAdapter);

        // Load recent items
        ListingAdapter recentAdapter = new ListingAdapter(
                requireContext(),
                getDummyItems("Recent"),
                this::navigateToItemDetail
        );
        recentRecyclerView.setAdapter(recentAdapter);
    }

    private void navigateToCategoryListing(CategoryModel category) {
        // Bundle để truyền dữ liệu category vào fragment tiếp theo
        Bundle args = new Bundle();
        args.putString("category", category.getName());

        // Sử dụng NavController để chuyển hướng đến màn hình danh sách sản phẩm theo danh mục
        navController.navigate(R.id.action_nav_recommendations_to_categoryListingFragment, args);
    }

    private void navigateToItemDetail(ItemModel item) {
        // Bundle để truyền ID của sản phẩm vào fragment chi tiết
        Bundle args = new Bundle();
        args.putString("itemId", item.getId());

        // Sử dụng NavController để chuyển hướng đến màn hình chi tiết sản phẩm
        navController.navigate(R.id.action_nav_recommendations_to_itemDetailFragment, args);
    }

    private List<CategoryModel> getDummyCategories() {
        List<CategoryModel> categories = new ArrayList<>();

        // Add dummy categories
        String[] categoryNames = {"Electronics", "Clothing", "Furniture", "Books", "Sports", "Home", "Toys"};
        int[] categoryIcons = {
                R.drawable.ic_category_electronics,
                R.drawable.ic_category_clothing,
                R.drawable.ic_category_furniture,
                R.drawable.ic_category_books,
                R.drawable.ic_category_sports,
                R.drawable.ic_category_home,
                R.drawable.ic_category_toys
        };

        // Create category models
        for (int i = 0; i < categoryNames.length; i++) {
            CategoryModel category = new CategoryModel();
            category.setId(String.valueOf(i + 1));
            category.setName(categoryNames[i]);

            // Use icon resource if it exists, otherwise use a default icon
            try {
                // Check if the resource exists
                getResources().getDrawable(categoryIcons[i], requireContext().getTheme());
                category.setIconResourceId(categoryIcons[i]);
            } catch (Exception e) {
                // Use a default icon if the resource doesn't exist
                category.setIconResourceId(R.drawable.ic_category_default);
            }

            categories.add(category);
        }

        return categories;
    }

    private List<ItemModel> getDummyItems(String type) {
        List<ItemModel> items = new ArrayList<>();

        // Create different items based on the type (recommended, nearby, recent)
        String[] titles;
        double[] prices;
        String[] conditions;
        String[] categories;

        if ("Recommended".equals(type)) {
            titles = new String[]{
                    "MacBook Pro M1", "Sony PlayStation 5", "Nike Air Jordan",
                    "iPhone 13 Pro", "Samsung QLED TV", "Dyson V11 Vacuum"
            };
            prices = new double[]{1299.99, 499.99, 159.99, 999.99, 1199.99, 599.99};
            conditions = new String[]{"Excellent", "Like New", "Good", "Like New", "New", "Good"};
            categories = new String[]{"Electronics", "Gaming", "Clothing", "Electronics", "Electronics", "Home"};
        } else if ("Nearby".equals(type)) {
            titles = new String[]{
                    "IKEA Desk", "Bookshelf", "Coffee Table",
                    "Dining Set", "Office Chair", "Floor Lamp"
            };
            prices = new double[]{99.99, 79.99, 149.99, 299.99, 129.99, 59.99};
            conditions = new String[]{"Good", "Used", "Like New", "Good", "Used", "Excellent"};
            categories = new String[]{"Furniture", "Furniture", "Furniture", "Furniture", "Furniture", "Home"};
        } else { // Recent
            titles = new String[]{
                    "Nintendo Switch", "Bluetooth Speaker", "Air Fryer",
                    "Yoga Mat", "Mountain Bike", "Tennis Racket"
            };
            prices = new double[]{279.99, 69.99, 89.99, 29.99, 349.99, 59.99};
            conditions = new String[]{"New", "Like New", "New", "Good", "Used", "Excellent"};
            categories = new String[]{"Gaming", "Electronics", "Home", "Sports", "Sports", "Sports"};
        }

        // Create item models
        for (int i = 0; i < titles.length; i++) {
            ItemModel item = new ItemModel();
            item.setId(type.toLowerCase() + "_" + (i + 1));
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

