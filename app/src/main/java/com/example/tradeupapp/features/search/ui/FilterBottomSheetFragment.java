package com.example.tradeupapp.features.search.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.tradeupapp.R;
import com.example.tradeupapp.features.search.viewmodel.FilterViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.RangeSlider;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    private EditText etMinPrice, etMaxPrice;
    private Spinner spinnerCategory, spinnerCondition;
    private MaterialButton btnApplyFilters, btnUseGps;
    private FilterAppliedListener listener;
    private RangeSlider rangeSliderDistance;
    private TextView tvDistanceValue, tvGpsLocation;

    private Double selectedLat = null;
    private Double selectedLng = null;

    private static final int REQUEST_CODE_PICK_LOCATION = 1001;

    private FilterViewModel filterViewModel;

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
        etMinPrice = view.findViewById(R.id.et_min_price);
        etMaxPrice = view.findViewById(R.id.et_max_price);
        tvGpsLocation = view.findViewById(R.id.tv_gps_location);
        btnUseGps = view.findViewById(R.id.btn_use_gps);
        // Use RangeSlider instead of Slider
        rangeSliderDistance = view.findViewById(R.id.slider_distance);
        tvDistanceValue = view.findViewById(R.id.tv_distance_value);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerCondition = view.findViewById(R.id.spinner_condition);
        btnApplyFilters = view.findViewById(R.id.btn_apply_filters);

        // Lấy ViewModel scoped theo SearchFragment
        filterViewModel = new ViewModelProvider(requireParentFragment()).get(FilterViewModel.class);

        // Observe ViewModel để cập nhật UI khi có thay đổi
        filterViewModel.getMinPrice().observe(getViewLifecycleOwner(), min -> {
            if (min != null) etMinPrice.setText(min == 0.0 ? "" : String.valueOf(min));
        });
        filterViewModel.getMaxPrice().observe(getViewLifecycleOwner(), max -> {
            if (max != null && max != Double.MAX_VALUE) etMaxPrice.setText(String.valueOf(max));
            else etMaxPrice.setText("");
        });
        filterViewModel.getCategory().observe(getViewLifecycleOwner(), cat -> {
            if (cat != null) setSpinnerSelection(spinnerCategory, cat);
        });
        filterViewModel.getCondition().observe(getViewLifecycleOwner(), cond -> {
            if (cond != null) setSpinnerSelection(spinnerCondition, cond);
        });
        filterViewModel.getDistance().observe(getViewLifecycleOwner(), dist -> {
            if (dist != null) {
                // Set only the lower thumb, keep upper thumb as is
                java.util.List<Float> current = rangeSliderDistance.getValues();
                float upper = (current != null && current.size() == 2) ? current.get(1) : 100f;
                rangeSliderDistance.setValues(dist.floatValue(), upper);
                tvDistanceValue.setText(((int) dist.floatValue()) + " - " + ((int) upper) + " km");
            }
        });
        filterViewModel.getAddress().observe(getViewLifecycleOwner(), addr -> {
            if (addr != null && !addr.isEmpty()) tvGpsLocation.setText(addr);
        });
        filterViewModel.getSelectedLat().observe(getViewLifecycleOwner(), lat -> selectedLat = lat);
        filterViewModel.getSelectedLng().observe(getViewLifecycleOwner(), lng -> selectedLng = lng);

        // Setup dropdowns
        setupDropdowns();
        setupDistanceSlider();
        setupClickListener();

        btnUseGps.setOnClickListener(v -> {
            androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.mapPickerFragment);
        });
        // Lắng nghe kết quả chọn vị trí từ mapPickerFragment
        androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.getCurrentBackStackEntry().getSavedStateHandle().getLiveData("location_data").observe(getViewLifecycleOwner(), result -> {
            if (result != null && result instanceof Bundle) {
                Bundle locationData = (Bundle) result;
                double latitude = locationData.getDouble("latitude");
                double longitude = locationData.getDouble("longitude");
                String address = locationData.getString("address");
                filterViewModel.setSelectedLat(latitude);
                filterViewModel.setSelectedLng(longitude);
                filterViewModel.setAddress(address != null ? address : "");
                // Clear result để tránh nhận lại nhiều lần
                navController.getCurrentBackStackEntry().getSavedStateHandle().set("location_data", null);
            }
        });
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter == null) return;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void openMapPickerFragment() {
        androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        // Sử dụng navigate(R.id.mapPickerFragment) nếu dùng fragment id trực tiếp
        navController.navigate(R.id.mapPickerFragment);
    }

    private void setDefaultLocationFromDevice() {
        if (getActivity() == null) return;
        androidx.core.content.ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION);
        android.location.LocationManager locationManager = (android.location.LocationManager) getActivity().getSystemService(android.content.Context.LOCATION_SERVICE);
        try {
            android.location.Location lastKnown = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
            if (lastKnown != null) {
                selectedLat = lastKnown.getLatitude();
                selectedLng = lastKnown.getLongitude();
                setAddressToTextView(selectedLat, selectedLng);
            } else {
                tvGpsLocation.setText("No GPS location selected");
            }
        } catch (SecurityException e) {
            tvGpsLocation.setText("Location permission required");
        }
    }

    private void setupDropdowns() {
        // Setup Category spinner
        String[] categories = {"All Categories", "Electronics", "Clothing", "Home & Garden",
                              "Toys", "Books", "Sports", "Automotive", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Setup Condition spinner
        String[] conditions = {"Any Condition", "New", "Like New", "Good", "Fair", "Poor"};
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                conditions);
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCondition.setAdapter(conditionAdapter);

        // Remove Sort By spinner setup
    }

    // New method to setup distance slider
    private void setupDistanceSlider() {
        rangeSliderDistance.setValueFrom(5f);
        rangeSliderDistance.setValueTo(100f);
        rangeSliderDistance.setStepSize(1f);
        if (rangeSliderDistance.getValues() == null || rangeSliderDistance.getValues().size() != 2) {
            rangeSliderDistance.setValues(5f, 100f);
        }
        java.util.List<Float> values = rangeSliderDistance.getValues();
        tvDistanceValue.setText(((int) values.get(0).floatValue()) + " - " + ((int) values.get(1).floatValue()) + " km");
        rangeSliderDistance.addOnChangeListener((slider, value, fromUser) -> {
            java.util.List<Float> vals = rangeSliderDistance.getValues();
            tvDistanceValue.setText(((int) vals.get(0).floatValue()) + " - " + ((int) vals.get(1).floatValue()) + " km");
        });
    }

    // Request GPS location (requires location permission in activity)
    private void requestGpsLocation() {
        if (getActivity() == null) return;
        androidx.core.content.ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION);
        android.location.LocationManager locationManager = (android.location.LocationManager) getActivity().getSystemService(android.content.Context.LOCATION_SERVICE);
        try {
            android.location.Location lastKnown = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
            if (lastKnown != null) {
                selectedLat = lastKnown.getLatitude();
                selectedLng = lastKnown.getLongitude();
                setAddressToTextView(selectedLat, selectedLng);
            } else {
                tvGpsLocation.setText("Unable to get GPS location");
            }
        } catch (SecurityException e) {
            tvGpsLocation.setText("Location permission required");
        }
    }

    private void setAddressToTextView(Double lat, Double lng) {
        if (lat == null || lng == null) {
            tvGpsLocation.setText("No GPS location selected");
            return;
        }
        new Thread(() -> {
            try {
                android.location.Geocoder geocoder = new android.location.Geocoder(getContext(), java.util.Locale.getDefault());
                java.util.List<android.location.Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                String address = null;
                if (addresses != null && !addresses.isEmpty()) {
                    address = addresses.get(0).getAddressLine(0);
                }
                String display = (address != null) ? address : (lat + ", " + lng);
                final String finalDisplay = display;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> tvGpsLocation.setText(finalDisplay));
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> tvGpsLocation.setText(lat + ", " + lng));
                }
            }
        }).start();
    }

    private void setupClickListener() {
        btnApplyFilters.setOnClickListener(v -> {
            if (listener != null) {
                String category = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "All Categories";
                double minPrice = 0.0;
                double maxPrice = Double.MAX_VALUE;
                try {
                    if (etMinPrice.getText() != null && !etMinPrice.getText().toString().isEmpty()) {
                        minPrice = Double.parseDouble(etMinPrice.getText().toString());
                    }
                    if (etMaxPrice.getText() != null && !etMaxPrice.getText().toString().isEmpty()) {
                        maxPrice = Double.parseDouble(etMaxPrice.getText().toString());
                    }
                } catch (NumberFormatException e) {}
                String condition = spinnerCondition.getSelectedItem() != null ? spinnerCondition.getSelectedItem().toString() : "Any Condition";
                // Get min and max distance from RangeSlider
                java.util.List<Float> distanceRange = rangeSliderDistance.getValues();
                int minDistance = distanceRange.size() > 0 ? distanceRange.get(0).intValue() : 5;
                int maxDistance = distanceRange.size() > 1 ? distanceRange.get(1).intValue() : 100;
                Double lat = selectedLat, lng = selectedLng;
                // Lưu lại filter vào ViewModel
                filterViewModel.setCategory(category);
                filterViewModel.setCondition(condition);
                filterViewModel.setMinPrice(minPrice);
                filterViewModel.setMaxPrice(maxPrice);
                filterViewModel.setDistance(minDistance); // Optionally store both min/max in ViewModel if needed
                filterViewModel.setSelectedLat(lat);
                filterViewModel.setSelectedLng(lng);
                filterViewModel.setAddress(tvGpsLocation.getText().toString());
                Bundle bundle = new Bundle();
                bundle.putString("category", category);
                bundle.putDouble("minPrice", minPrice);
                bundle.putDouble("maxPrice", maxPrice);
                bundle.putString("condition", condition);
                bundle.putInt("minDistance", minDistance);
                bundle.putInt("maxDistance", maxDistance);
                if (lat != null && lng != null) {
                    bundle.putDouble("latitude", lat);
                    bundle.putDouble("longitude", lng);
                }
                getParentFragmentManager().setFragmentResult("filters_applied", bundle);
                dismiss();
            }
        });
    }
}
