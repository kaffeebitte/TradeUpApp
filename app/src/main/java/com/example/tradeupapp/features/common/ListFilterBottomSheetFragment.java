package com.example.tradeupapp.features.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tradeupapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ListFilterBottomSheetFragment extends BottomSheetDialogFragment {

    private ChipGroup chipGroupStatus;
    private ChipGroup chipGroupDate;
    private TextInputEditText etMinPrice, etMaxPrice;
    private RadioGroup radioGroupSort;
    private MaterialButton btnApplyFilters, btnResetFilters;
    private FilterAppliedListener listener;

    public interface FilterAppliedListener {
        void onFiltersApplied(List<String> statusList, String dateRange,
                             double minPrice, double maxPrice, String sortBy);
    }

    public void setFilterAppliedListener(FilterAppliedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_list_filter_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        chipGroupStatus = view.findViewById(R.id.chip_group_status);
        chipGroupDate = view.findViewById(R.id.chip_group_date);
        etMinPrice = view.findViewById(R.id.et_min_price);
        etMaxPrice = view.findViewById(R.id.et_max_price);
        radioGroupSort = view.findViewById(R.id.radio_group_sort);
        btnApplyFilters = view.findViewById(R.id.btn_apply_filters);
        btnResetFilters = view.findViewById(R.id.btn_reset_filters);

        // Setup button click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Apply filters button
        btnApplyFilters.setOnClickListener(v -> {
            if (listener != null) {
                // Get selected status filters
                List<String> selectedStatuses = getSelectedStatuses();

                // Get selected date range
                String dateRange = getSelectedDateRange();

                // Get price range
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
                    // Use default values if parsing fails
                }

                // Get sort option
                String sortBy = getSelectedSortOption();

                // Pass filter values to listener
                listener.onFiltersApplied(selectedStatuses, dateRange, minPrice, maxPrice, sortBy);

                // Dismiss the bottom sheet
                dismiss();
            }
        });

        // Reset filters button
        btnResetFilters.setOnClickListener(v -> {
            // Clear status selections
            for (int i = 0; i < chipGroupStatus.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupStatus.getChildAt(i);
                chip.setChecked(false);
            }

            // Clear date range selections
            for (int i = 0; i < chipGroupDate.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupDate.getChildAt(i);
                chip.setChecked(false);
            }

            // Clear price range
            etMinPrice.setText("");
            etMaxPrice.setText("");

            // Reset sort options to default (usually first option)
            if (radioGroupSort.getChildCount() > 0) {
                ((RadioButton) radioGroupSort.getChildAt(0)).setChecked(true);
            }
        });
    }

    private List<String> getSelectedStatuses() {
        List<String> selectedStatuses = new ArrayList<>();
        for (int i = 0; i < chipGroupStatus.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupStatus.getChildAt(i);
            if (chip.isChecked()) {
                selectedStatuses.add(chip.getText().toString());
            }
        }
        return selectedStatuses;
    }

    private String getSelectedDateRange() {
        for (int i = 0; i < chipGroupDate.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupDate.getChildAt(i);
            if (chip.isChecked()) {
                return chip.getText().toString();
            }
        }
        return ""; // No date range selected
    }

    private String getSelectedSortOption() {
        int selectedId = radioGroupSort.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton radioButton = getView().findViewById(selectedId);
            return radioButton.getText().toString();
        }
        return "Newest"; // Default sort option
    }
}
