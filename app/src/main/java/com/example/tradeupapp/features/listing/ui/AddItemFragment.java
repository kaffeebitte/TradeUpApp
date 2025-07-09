package com.example.tradeupapp.features.listing.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.shared.adapters.PhotoUploadAdapter;
import com.example.tradeupapp.models.ItemModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddItemFragment extends Fragment implements PhotoUploadAdapter.OnPhotoActionListener {

    private MaterialToolbar toolbar;
    private TextInputLayout tilTitle;
    private TextInputLayout tilPrice;
    private TextInputLayout tilDescription;
    private TextInputLayout tilCategory;
    private TextInputLayout tilCondition;
    private TextInputLayout tilLocation;
    private TextInputLayout tilTag;
    private TextInputEditText etTitle;
    private TextInputEditText etPrice;
    private TextInputEditText etDescription;
    private TextInputEditText etLocation;
    private TextInputEditText etTag;
    private AutoCompleteTextView actvCategory;
    private AutoCompleteTextView actvCondition;
    private RecyclerView recyclerPhotos;
    private PhotoUploadAdapter photoAdapter;
    private MaterialButton btnContinue;

    // Activity Result Launcher for picking images
    private ActivityResultLauncher<Intent> photoPickerLauncher;
    // Activity Result Launcher for location permission
    private ActivityResultLauncher<String> locationPermissionLauncher;

    private FusedLocationProviderClient fusedLocationClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_item, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the photo picker launcher
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            photoAdapter.addPhoto(selectedImageUri);
                        }
                    }
                }
        );

        // Initialize the location permission launcher
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permission is granted. Continue the action or workflow in your app.
                        getCurrentLocation();
                        Toast.makeText(requireContext(), "Location permission granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                    }
                });
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Setup photo adapter
        setupPhotoAdapter();

        // Setup category and condition dropdowns
        setupDropdowns();

        // Setup click listeners for buttons
        setupClickListeners();

        // Set up observer for map picker result - moved from onCreate()
        setupMapPickerResultObserver();
    }

    private void initViews(View view) {
        // Initialize toolbar using the tag we added
        toolbar = view.findViewById(R.id.add_item_toolbar);
        setupToolbar();

        tilTitle = view.findViewById(R.id.til_title);
        tilPrice = view.findViewById(R.id.til_price);
        tilDescription = view.findViewById(R.id.til_description);
        tilCategory = view.findViewById(R.id.til_category);
        tilCondition = view.findViewById(R.id.til_condition);
        tilLocation = view.findViewById(R.id.til_location);
        tilTag = view.findViewById(R.id.til_tag);

        etTitle = view.findViewById(R.id.et_title);
        etPrice = view.findViewById(R.id.et_price);
        etDescription = view.findViewById(R.id.et_description);
        etLocation = view.findViewById(R.id.et_location);
        etTag = view.findViewById(R.id.et_tag);

        actvCategory = view.findViewById(R.id.actv_category);
        actvCondition = view.findViewById(R.id.actv_condition);

        recyclerPhotos = view.findViewById(R.id.recycler_photos);
        // Use the correct button ID from the layout
        btnContinue = view.findViewById(R.id.btn_preview);
    }

    private void setupPhotoAdapter() {
        photoAdapter = new PhotoUploadAdapter(this);
        recyclerPhotos.setAdapter(photoAdapter);
    }

    private void setupDropdowns() {
        // Set up category dropdown
        String[] categories = {"Electronics", "Clothing", "Home & Garden", "Toys", "Books", "Sports", "Automotive", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories);
        actvCategory.setAdapter(categoryAdapter);

        // Set up condition dropdown
        String[] conditions = {"New", "Like New", "Good", "Fair", "Poor"};
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                conditions);
        actvCondition.setAdapter(conditionAdapter);
    }

    private void setupClickListeners() {
        // Set up click listener for the location field to open location selection
        etLocation.setOnClickListener(v -> {
            // Navigate to map picker fragment
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_nav_add_to_mapPickerFragment);
        });

        // Set up click listener for the continue button
        btnContinue.setOnClickListener(v -> {
            if (validateInput()) {
                showItemPreview();
            }
        });

        // Set up click listener for the location end icon (current location)
        tilLocation.setEndIconOnClickListener(v -> {
            requestLocationPermission();
        });

        // Set up text change listeners to enable/disable continue button
        setupTextWatchers();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            // Logic to handle location object
                            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    etLocation.setText(addresses.get(0).getAddressLine(0));
                                }
                            } catch (IOException e) {
                                Toast.makeText(requireContext(), "Error getting address", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Enable continue button if all required fields are filled
                boolean allFieldsFilled = !etTitle.getText().toString().trim().isEmpty() &&
                        !etPrice.getText().toString().trim().isEmpty() &&
                        !etDescription.getText().toString().trim().isEmpty();
                btnContinue.setEnabled(allFieldsFilled);
            }
        };

        etTitle.addTextChangedListener(textWatcher);
        etPrice.addTextChangedListener(textWatcher);
        etDescription.addTextChangedListener(textWatcher);
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Kiểm tra tên sản phẩm
        if (etTitle.getText().toString().trim().isEmpty()) {
            tilTitle.setError("Vui lòng nhập tên sản phẩm");
            isValid = false;
        } else {
            tilTitle.setError(null);
        }

        // Kiểm tra giá
        if (etPrice.getText().toString().trim().isEmpty()) {
            tilPrice.setError("Vui lòng nhập giá");
            isValid = false;
        } else {
            tilPrice.setError(null);
        }

        // Kiểm tra mô tả
        if (etDescription.getText().toString().trim().isEmpty()) {
            tilDescription.setError("Vui lòng nhập mô t��� sản phẩm");
            isValid = false;
        } else {
            tilDescription.setError(null);
        }

        return isValid;
    }

    private void showItemPreview() {
        try {
            // Create an ItemModel from the form data
            ItemModel item = new ItemModel();
            item.setTitle(etTitle.getText().toString().trim());
            item.setDescription(etDescription.getText().toString().trim());

            // Parse price (handle possible exceptions)
            String priceStr = etPrice.getText().toString().trim();
            double price = 0.0;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get selected category and condition
            item.setCategory(actvCategory.getText().toString());
            item.setCondition(actvCondition.getText().toString());

            // Set location
            item.setLocation(etLocation.getText().toString().trim());

            // Set photos (convert List<Uri> to List<String>)
            if (photoAdapter != null) {
                java.util.List<Uri> uriList = photoAdapter.getPhotoUris();
                java.util.List<String> stringList = new java.util.ArrayList<>();
                for (Uri uri : uriList) {
                    if (uri != null) stringList.add(uri.toString());
                }
                item.setPhotoUris(stringList);
            }

            // Navigate to preview fragment
            Bundle args = new Bundle();
            args.putParcelable("item", item);
            args.putDouble("price", price); // Pass price separately
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_nav_add_to_itemPreviewFragment, args);

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error creating preview: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupToolbar() {
        if (toolbar != null) {
            // Check if the toolbar has the tag we added
            if ("add_item_toolbar".equals(toolbar.getTag())) {
                // We can now use the tag to identify this specific toolbar

                // Set up the toolbar with AppCompatActivity
                ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
                ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);

                // Handle navigation icon click (back button)
                toolbar.setNavigationOnClickListener(v -> {
                    requireActivity().onBackPressed();
                });

                // Handle menu item clicks
                toolbar.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_save) {
                        if (validateInput()) {
                            showItemPreview();
                        }
                        return true;
                    }
                    return false;
                });
            }
        }
    }

    private void setupMapPickerResultObserver() {
        // Using the correct method to get NavController
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.getCurrentBackStackEntry().getSavedStateHandle().getLiveData("location_data").observe(
                getViewLifecycleOwner(), result -> {
                    if (result != null && result instanceof Bundle) {
                        Bundle locationData = (Bundle) result;
                        double latitude = locationData.getDouble("latitude");
                        double longitude = locationData.getDouble("longitude");
                        String address = locationData.getString("address");

                        // Update the location field with the selected address
                        etLocation.setText(address);

                        // Store the coordinates for later use when creating the item
                        etLocation.setTag(new double[]{latitude, longitude});

                        // Clear the result to avoid reprocessing on navigation
                        navController.getCurrentBackStackEntry().getSavedStateHandle().set("location_data", null);
                    }
                });
    }

    @Override
    public void onAddPhotoClicked() {
        // Launch the photo picker intent
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerLauncher.launch(intent);
    }

    @Override
    public void onPhotoRemoved(int position) {
        // Remove the photo from the adapter
        photoAdapter.removePhoto(position);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1001 && grantResults.length > 0 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
