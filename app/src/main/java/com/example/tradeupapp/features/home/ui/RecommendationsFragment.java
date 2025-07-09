package com.example.tradeupapp.features.home.ui;

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
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.shared.adapters.CategoryAdapter;
import com.example.tradeupapp.shared.adapters.ListingAdapter;
import com.example.tradeupapp.models.CategoryModel;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;

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

    private NavController navController;
    private FirebaseService firebaseService;

    // Adapters
    private ListingAdapter personalizedAdapter;
    private ListingAdapter nearbyAdapter;
    private ListingAdapter recentAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase service
        firebaseService = FirebaseService.getInstance();

        // Initialize navigation controller
        navController = Navigation.findNavController(view);

        // Initialize views
        initViews(view);

        // Set up click listeners
        setupClickListeners();

        // Load real data from Firestore
        loadDataFromFirestore();
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
    }

    private void loadDataFromFirestore() {
        // Load categories (can be static or from Firestore)
        loadCategories();

        // Load recommended items (all available items for now, can be enhanced with ML)
        loadRecommendedItems();

        // Load nearby items (within user's location radius)
        loadNearbyItems();

        // Load recent items (recently added items)
        loadRecentItems();
    }

    private void loadCategories() {
        // Create static categories or load from Firestore
        CategoryAdapter categoryAdapter = new CategoryAdapter(getStaticCategories(), category -> {
            navigateToCategoryListing(category);
        });
        categoriesRecyclerView.setAdapter(categoryAdapter);
    }

    private void loadRecommendedItems() {
        firebaseService.getAllListings(new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(List<ListingModel> listings) {
                if (getActivity() != null && isAdded()) {
                    List<ListingModel> recommendedListings = listings.size() > 5 ? listings.subList(0, 5) : listings;
                    personalizedAdapter = new ListingAdapter(
                            requireContext(),
                            recommendedListings,
                            listing -> navigateToItemDetail(listing.getId())
                    );
                    personalizedRecyclerView.setAdapter(personalizedAdapter);
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), "Error loading recommendations: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadNearbyItems() {
        firebaseService.getAllListings(new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(List<ListingModel> listings) {
                if (getActivity() != null && isAdded()) {
                    int start = Math.min(5, listings.size());
                    int end = Math.min(10, listings.size());
                    List<ListingModel> nearbyListings = listings.size() > start ? listings.subList(start, end) : new ArrayList<>();
                    nearbyAdapter = new ListingAdapter(
                            requireContext(),
                            nearbyListings,
                            listing -> navigateToItemDetail(listing.getId())
                    );
                    nearbyRecyclerView.setAdapter(nearbyAdapter);
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), "Error loading nearby items: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadRecentItems() {
        firebaseService.getAllListings(new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(List<ListingModel> listings) {
                if (getActivity() != null && isAdded()) {
                    List<ListingModel> recentListings = listings.size() > 3 ? listings.subList(0, 3) : listings;
                    recentAdapter = new ListingAdapter(
                            requireContext(),
                            recentListings,
                            listing -> navigateToItemDetail(listing.getId())
                    );
                    recentRecyclerView.setAdapter(recentAdapter);
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), "Error loading recent items: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private List<CategoryModel> getStaticCategories() {
        List<CategoryModel> categories = new ArrayList<>();

        CategoryModel electronics = new CategoryModel("Electronics", null);
        electronics.setId("1");
        electronics.setIconResourceId(R.drawable.ic_category_electronics);
        categories.add(electronics);

        CategoryModel clothing = new CategoryModel("Clothing", null);
        clothing.setId("2");
        clothing.setIconResourceId(R.drawable.ic_category_clothing);
        categories.add(clothing);

        CategoryModel books = new CategoryModel("Books", null);
        books.setId("3");
        books.setIconResourceId(R.drawable.ic_category_books);
        categories.add(books);

        CategoryModel furniture = new CategoryModel("Furniture", null);
        furniture.setId("4");
        furniture.setIconResourceId(R.drawable.ic_category_furniture);
        categories.add(furniture);

        CategoryModel sports = new CategoryModel("Sports", null);
        sports.setId("5");
        sports.setIconResourceId(R.drawable.ic_category_sports);
        categories.add(sports);

        return categories;
    }

    private void navigateToCategoryListing(CategoryModel category) {
        // Bundle để truyền dữ liệu category vào fragment tiếp theo
        Bundle args = new Bundle();
        args.putString("category", category.getName());

        // Sử dụng NavController để chuyển hướng đến màn hình danh sách sản phẩm theo danh mục
        navController.navigate(R.id.action_nav_recommendations_to_categoryListingFragment, args);
    }

    private void navigateToItemDetail(String listingId) {
        Bundle args = new Bundle();
        args.putString("listingId", listingId);
        navController.navigate(R.id.action_nav_recommendations_to_itemDetailFragment, args);
    }
}
