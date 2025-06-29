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
import com.example.tradeupapp.features.search.adapter.CategoryAdapter;
import com.example.tradeupapp.models.CategoryModel;

import java.util.ArrayList;
import java.util.List;

public class AllCategoriesFragment extends Fragment {

    private RecyclerView categoriesRecyclerView;
    private NavController navController;
    private Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize navigation controller
        navController = Navigation.findNavController(view);

        // Initialize views
        initViews(view);

        // Set up toolbar navigation
        setupToolbar();

        // Load categories
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
        // Create the adapter with the category data
        CategoryAdapter categoryAdapter = new CategoryAdapter(getDummyCategories(), category -> {
            // Navigate to category listing
            Bundle args = new Bundle();
            args.putString("category", category.getName());
            navController.navigate(R.id.action_allCategoriesFragment_to_categoryListingFragment, args);
        });

        // Set the adapter to the recycler view
        categoriesRecyclerView.setAdapter(categoryAdapter);
    }

    private List<CategoryModel> getDummyCategories() {
        List<CategoryModel> categories = new ArrayList<>();

        // Add dummy categories
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

        // Create category models
        for (int i = 0; i < categoryNames.length; i++) {
            CategoryModel category = new CategoryModel();
            category.setId(String.valueOf(i + 1));
            category.setName(categoryNames[i]);

            // Use icon resource if within range, otherwise use default icon
            if (i < categoryIcons.length) {
                try {
                    // Check if the resource exists
                    getResources().getDrawable(categoryIcons[i], requireContext().getTheme());
                    category.setIconResourceId(categoryIcons[i]);
                } catch (Exception e) {
                    // Use a default icon if the resource doesn't exist
                    category.setIconResourceId(R.drawable.ic_category_default);
                }
            } else {
                category.setIconResourceId(R.drawable.ic_category_default);
            }

            categories.add(category);
        }

        return categories;
    }
}
