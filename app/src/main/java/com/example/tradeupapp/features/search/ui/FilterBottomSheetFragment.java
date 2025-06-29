package com.example.tradeupapp.features.search.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tradeupapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.slider.Slider;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    private TextInputEditText etKeyword, etMinPrice, etMaxPrice;
    private AutoCompleteTextView actvCategory, actvCondition, actvSort;
    private MaterialButton btnApplyFilters;
    private FilterAppliedListener listener;

    // Added references for distance slider and TextView
    private Slider sliderDistance;
    private TextView tvDistanceValue;

    public interface FilterAppliedListener {
        void onFiltersApplied(String keyword, String category, double minPrice,
                             double maxPrice, String condition, int distance, String sortBy);
    }

    public void setFilterAppliedListener(FilterAppliedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_sheet_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        etKeyword = view.findViewById(R.id.et_keyword);
        etMinPrice = view.findViewById(R.id.et_min_price);
        etMaxPrice = view.findViewById(R.id.et_max_price);
        sliderDistance = view.findViewById(R.id.slider_distance);
        tvDistanceValue = view.findViewById(R.id.tv_distance_value);
        actvCategory = view.findViewById(R.id.actv_category);
        actvCondition = view.findViewById(R.id.actv_condition);
        actvSort = view.findViewById(R.id.actv_sort);
        btnApplyFilters = view.findViewById(R.id.btn_apply_filters);

        // Setup dropdowns
        setupDropdowns();

        // Setup distance slider
        setupDistanceSlider();

        // Setup button click listener
        setupClickListener();
    }

    private void setupDropdowns() {
        // Setup Category dropdown
        String[] categories = {"All Categories", "Electronics", "Clothing", "Home & Garden",
                              "Toys", "Books", "Sports", "Automotive", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories);
        actvCategory.setAdapter(categoryAdapter);

        // Setup Condition dropdown
        String[] conditions = {"Any Condition", "New", "Like New", "Good", "Fair", "Poor"};
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                conditions);
        actvCondition.setAdapter(conditionAdapter);

        // Setup Sort dropdown
        String[] sortOptions = {"Newest First", "Price: Low to High", "Price: High to Low",
                               "Most Relevant", "Distance: Nearest"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                sortOptions);
        actvSort.setAdapter(sortAdapter);
    }

    // New method to setup distance slider
    private void setupDistanceSlider() {
        sliderDistance.setValue(50); // Default 50km
        sliderDistance.setLabelFormatter(value -> value + " km");

        sliderDistance.addOnChangeListener((slider, value, fromUser) -> {
            // Update distance value TextView when slider is changed
            tvDistanceValue.setText(value + " km");
        });
    }

    private void setupClickListener() {
        btnApplyFilters.setOnClickListener(v -> {
            if (listener != null) {
                // Get entered values (with validation and defaults)
                String keyword = etKeyword.getText() != null ? etKeyword.getText().toString() : "";
                String category = actvCategory.getText() != null ? actvCategory.getText().toString() : "All Categories";

                // Parse prices with defaults
                double minPrice = 0.0;
                double maxPrice = Double.MAX_VALUE;
                try {
                    if (etMinPrice.getText() != null && !etMinPrice.getText().toString().isEmpty()) {
                        minPrice = Double.parseDouble(etMinPrice.getText().toString());
                    }
                    if (etMaxPrice.getText() != null && !etMaxPrice.getText().toString().isEmpty()) {
                        maxPrice = Double.parseDouble(etMaxPrice.getText().toString());
                    }
                } catch (NumberFormatException e) {
                    // If parsing fails, use defaults
                }

                // Get condition
                String condition = actvCondition.getText() != null ?
                                  actvCondition.getText().toString() : "Any Condition";

                // Get distance from slider
                int distance = (int) sliderDistance.getValue(); // Get the current value of the slider

                // Get sort option
                String sortBy = actvSort.getText() != null ?
                               actvSort.getText().toString() : "Newest First";

                // Pass filter values to listener
                listener.onFiltersApplied(keyword, category, minPrice, maxPrice, condition, distance, sortBy);

                // Dismiss the bottom sheet
                dismiss();
            }
        });
    }
}
