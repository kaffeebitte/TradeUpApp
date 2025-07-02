package com.example.tradeupapp.features.search.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.shared.adapters.CategoryAdapter;
import com.example.tradeupapp.models.CategoryModel;

import java.util.ArrayList;
import java.util.List;

public class AllCategoriesFragment extends Fragment {

    private RecyclerView categoriesRecyclerView;
    private NavController navController;
    private Toolbar toolbar;
    private FirebaseService firebaseService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_categories, container, false);
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

        // Set up toolbar navigation
        setupToolbar();

        // Load categories from Firestore
        loadCategories();
    }

    private void initViews(View view) {
        categoriesRecyclerView = view.findViewById(R.id.recycler_all_categories);
        toolbar = view.findViewById(R.id.toolbar);

        // Set up recycler view with grid layout
        categoriesRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> {
            // Navigate back when navigation icon is clicked
            navController.navigateUp();
        });
    }

    private void loadCategories() {
        // Load categories from Firestore
        firebaseService.getAllCategories(new FirebaseService.CategoriesCallback() {
            @Override
            public void onSuccess(List<CategoryModel> categories) {
                if (getActivity() != null && isAdded()) {
                    // Set default icon resource IDs for categories that don't have iconUrl
                    for (CategoryModel category : categories) {
                        if (category.getIconUrl() == null || category.getIconUrl().isEmpty()) {
                            setDefaultIconForCategory(category);
                        }
                    }

                    // Create the adapter with the category data
                    CategoryAdapter categoryAdapter = new CategoryAdapter(categories, category -> {
                        // Navigate to category listing
                        Bundle args = new Bundle();
                        args.putString("category", category.getName());
                        navController.navigate(R.id.action_allCategoriesFragment_to_categoryListingFragment, args);
                    });

                    // Set the adapter to the recycler view
                    categoriesRecyclerView.setAdapter(categoryAdapter);
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), "Error loading categories: " + error, Toast.LENGTH_SHORT).show();

                    // Fallback to static categories if Firestore fails
                    loadFallbackCategories();
                }
            }
        });
    }

    private void setDefaultIconForCategory(CategoryModel category) {
        // Set default icons based on category name
        String name = category.getName().toLowerCase();
        if (name.contains("electronics")) {
            category.setIconResourceId(R.drawable.ic_category_electronics);
        } else if (name.contains("clothing") || name.contains("fashion")) {
            category.setIconResourceId(R.drawable.ic_category_clothing);
        } else if (name.contains("furniture") || name.contains("home")) {
            category.setIconResourceId(R.drawable.ic_category_furniture);
        } else if (name.contains("books")) {
            category.setIconResourceId(R.drawable.ic_category_books);
        } else if (name.contains("sports")) {
            category.setIconResourceId(R.drawable.ic_category_sports);
        } else if (name.contains("toys")) {
            category.setIconResourceId(R.drawable.ic_category_toys);
        } else {
            category.setIconResourceId(R.drawable.ic_category_default);
        }
    }

    private void loadFallbackCategories() {
        // Fallback categories if Firestore fails
        List<CategoryModel> categories = new ArrayList<>();

        String[] categoryNames = {
                "Electronics", "Clothing", "Furniture", "Books",
                "Sports", "Home", "Toys", "Beauty", "Jewelry",
                "Automotive", "Tools", "Pet Supplies", "Baby",
                "Garden", "Office", "Music", "Collectibles", "Art"
        };

        int[] categoryIcons = {
                R.drawable.ic_category_electronics,
                R.drawable.ic_category_clothing,
                R.drawable.ic_category_furniture,
                R.drawable.ic_category_books,
                R.drawable.ic_category_sports,
                R.drawable.ic_category_home,
                R.drawable.ic_category_toys,
                R.drawable.ic_category_default,
                R.drawable.ic_category_default,
                R.drawable.ic_category_default,
                R.drawable.ic_category_default,
                R.drawable.ic_category_default,
                R.drawable.ic_category_default,
                R.drawable.ic_category_default,
                R.drawable.ic_category_default,
                R.drawable.ic_category_default,
                R.drawable.ic_category_default,
                R.drawable.ic_category_default
        };

        for (int i = 0; i < categoryNames.length; i++) {
            CategoryModel category = new CategoryModel(categoryNames[i], null);
            category.setId(String.valueOf(i + 1));
            category.setIconResourceId(categoryIcons[i]);
            categories.add(category);
        }

        CategoryAdapter categoryAdapter = new CategoryAdapter(categories, category -> {
            Bundle args = new Bundle();
            args.putString("category", category.getName());
            navController.navigate(R.id.action_allCategoriesFragment_to_categoryListingFragment, args);
        });

        categoriesRecyclerView.setAdapter(categoryAdapter);
    }
}
