package com.example.tradeupapp.features.listing.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.snackbar.Snackbar;

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
    private TextInputEditText etTagInput;
    private AutoCompleteTextView actvCategory;
    private AutoCompleteTextView actvCondition;
    private RecyclerView recyclerPhotos;
    private PhotoUploadAdapter photoAdapter;
    private MaterialButton btnContinue;
    private MaterialButton btnSave;
    private MaterialSwitch switchAllowOffers;
    private MaterialSwitch switchAllowReturns;
    private MaterialSwitch switchFeatured;
    private RecyclerView recyclerTags; // Use RecyclerView for tags
    private View loadingOverlay;

    // Activity Result Launcher for picking images
    private ActivityResultLauncher<Intent> photoPickerLauncher;
    // Activity Result Launcher for location permission
    private ActivityResultLauncher<String> locationPermissionLauncher;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private Double selectedLatitude = null;
    private Double selectedLongitude = null;
    private java.util.ArrayList<String> photoUrisState = new java.util.ArrayList<>();

    private ItemModel itemModel; // Item fields only
    private double listingPrice; // Listing field
    private boolean allowOffers = true; // Listing field
    private boolean allowReturns = false; // Listing field

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_item, container, false);
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        // Tự động lấy location khi vào màn hình
        autofillLocation();
        // etLocation cho nhập liệu tự do, chỉ mở map picker khi bấm icon định vị
        tilLocation.setEndIconOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("isFullAddress", true);
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                .navigate(R.id.action_nav_add_to_mapPickerFragment, args);
        });
        // Không mở map picker khi click vào etLocation, chỉ cho nhập liệu
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the photo picker launcher
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        if (result.getData().getClipData() != null) {
                            // Multiple images selected
                            int count = result.getData().getClipData().getItemCount();
                            int currentCount = photoAdapter.getItemCount();
                            for (int i = 0; i < count; i++) {
                                if (currentCount >= 10) {
                                    Toast.makeText(requireContext(), "You can upload up to 10 images only.", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                                String type = requireContext().getContentResolver().getType(imageUri);
                                if (type != null && (type.equals("image/jpeg") || type.equals("image/png"))) {
                                    photoAdapter.addPhoto(imageUri);
                                    currentCount++;
                                } else {
                                    Toast.makeText(requireContext(), "Only JPEG and PNG images are supported.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            // Single image selected
                            Uri selectedImageUri = result.getData().getData();
                            if (selectedImageUri != null) {
                                if (photoAdapter.getItemCount() >= 10) {
                                    Toast.makeText(requireContext(), "You can upload up to 10 images only.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                String type = requireContext().getContentResolver().getType(selectedImageUri);
                                if (type != null && (type.equals("image/jpeg") || type.equals("image/png"))) {
                                    photoAdapter.addPhoto(selectedImageUri);
                                } else {
                                    Toast.makeText(requireContext(), "Only JPEG and PNG images are supported.", Toast.LENGTH_SHORT).show();
                                }
                            }
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
        etTagInput = view.findViewById(R.id.et_tag_input);
        recyclerPhotos = view.findViewById(R.id.recycler_photos);
        actvCategory = view.findViewById(R.id.actv_category);
        actvCondition = view.findViewById(R.id.actv_condition);
        btnContinue = view.findViewById(R.id.btn_preview);
        btnSave = view.findViewById(R.id.btn_save);
        switchAllowOffers = view.findViewById(R.id.switch_allow_offers);
        switchAllowReturns = view.findViewById(R.id.switch_allow_returns);
        recyclerTags = view.findViewById(R.id.recycler_tags);
        loadingOverlay = view.findViewById(R.id.loading_overlay);
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

        // Set up click listener for the save button
        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                showLoadingOverlay();
                ItemModel item = new ItemModel();
                item.setTitle(etTitle != null && etTitle.getText() != null ? etTitle.getText().toString().trim() : "");
                item.setDescription(etDescription != null && etDescription.getText() != null ? etDescription.getText().toString().trim() : "");
                item.setCategory(actvCategory != null ? actvCategory.getText().toString() : "");
                item.setCondition(actvCondition != null ? actvCondition.getText().toString() : "");
                java.util.Map<String, Object> locationMap = new java.util.HashMap<>();
                if (selectedLatitude != null && selectedLongitude != null) {
                    locationMap.put("_latitude", selectedLatitude);
                    locationMap.put("_longitude", selectedLongitude);
                    locationMap.put("address", etLocation != null && etLocation.getText() != null ? etLocation.getText().toString().trim() : "");
                } else {
                    locationMap.put("address", etLocation != null && etLocation.getText() != null ? etLocation.getText().toString().trim() : "");
                }
                item.setLocation(locationMap);
                // Tags
                String tagsRaw = etTagInput != null && etTagInput.getText() != null ? etTagInput.getText().toString() : "";
                java.util.List<String> tags = new java.util.ArrayList<>();
                for (String tag : tagsRaw.split(",")) {
                    String trimmed = tag.trim();
                    if (!trimmed.isEmpty()) tags.add(trimmed);
                }
                item.setTags(tags);
                // Photos
                java.util.List<Uri> uriList = photoAdapter.getPhotoUris();
                if (uriList.isEmpty()) {
                    saveAndPublishItemWithListing(item, new java.util.ArrayList<>());
                } else {
                    uploadImagesToCloudinary(uriList, photoUrls -> {
                        item.setPhotoUris(photoUrls);
                        saveAndPublishItemWithListing(item, photoUrls);
                    });
                }
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
            ItemModel item = new ItemModel();
            item.setTitle(etTitle.getText().toString().trim());
            item.setDescription(etDescription.getText().toString().trim());
            item.setCategory(actvCategory.getText().toString());
            item.setCondition(actvCondition.getText().toString());
            java.util.Map<String, Object> locationMap = new java.util.HashMap<>();
            if (selectedLatitude != null && selectedLongitude != null) {
                locationMap.put("_latitude", selectedLatitude);
                locationMap.put("_longitude", selectedLongitude);
                locationMap.put("address", etLocation.getText().toString().trim());
            } else {
                locationMap.put("address", etLocation.getText().toString().trim());
            }
            item.setLocation(locationMap);
            // Tags
            String tagsRaw = etTagInput.getText() != null ? etTagInput.getText().toString() : "";
            java.util.List<String> tags = new java.util.ArrayList<>();
            for (String tag : tagsRaw.split(",")) {
                String trimmed = tag.trim();
                if (!trimmed.isEmpty()) tags.add(trimmed);
            }
            item.setTags(tags);
            // Photos
            if (photoAdapter != null) {
                java.util.List<Uri> uriList = photoAdapter.getPhotoUris();
                java.util.List<String> photoUrls = new java.util.ArrayList<>();
                for (Uri uri : uriList) {
                    photoUrls.add(uri.toString());
                }
                item.setPhotoUris(photoUrls);
                photoUrisState = new java.util.ArrayList<>(photoUrls); // Save for restore
            }
            // Listing fields
            String priceStr = etPrice.getText().toString().trim();
            double price = 0.0;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean allowOffersVal = switchAllowOffers != null ? switchAllowOffers.isChecked() : true;
            boolean allowReturnsVal = switchAllowReturns != null ? switchAllowReturns.isChecked() : false;
            Bundle args = new Bundle();
            args.putParcelable("item", item);
            args.putDouble("price", price);
            args.putStringArrayList("tags", new java.util.ArrayList<>(tags));
            args.putStringArrayList("photoUris", photoUrisState); // Pass photoUris to preview
            args.putBoolean("allowOffers", allowOffersVal);
            args.putBoolean("allowReturns", allowReturnsVal);
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_nav_add_to_itemPreviewFragment, args);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error creating preview: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void continueToPreview(ItemModel item, double price) {
        // Get tags from etTagInput
        String tagsRaw = etTagInput.getText() != null ? etTagInput.getText().toString() : "";
        java.util.List<String> tags = new java.util.ArrayList<>();
        for (String tag : tagsRaw.split(",")) {
            String trimmed = tag.trim();
            if (!trimmed.isEmpty()) tags.add(trimmed);
        }
        item.setTags(tags);
        Bundle args = new Bundle();
        args.putParcelable("item", item);
        args.putDouble("price", price);
        args.putStringArrayList("tags", new java.util.ArrayList<>(tags));
        boolean allowOffers = switchAllowOffers != null ? switchAllowOffers.isChecked() : true;
        boolean allowReturns = switchAllowReturns != null ? switchAllowReturns.isChecked() : false;
        args.putBoolean("allowOffers", allowOffers);
        args.putBoolean("allowReturns", allowReturns);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_nav_add_to_itemPreviewFragment, args);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            if ("add_item_toolbar".equals(toolbar.getTag())) {
                ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
                ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
                toolbar.setNavigationOnClickListener(v -> {
                    requireActivity().onBackPressed();
                });
                toolbar.setOnMenuItemClickListener(itemMenu -> {
                    int itemId = itemMenu.getItemId();
                    if (itemId == R.id.action_save) {
                        if (validateInput()) {
                            saveAndPublishItemWithListing();
                        }
                        return true;
                    }
                    return false;
                });
            }
        }
    }

    private void showLoadingOverlay() {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
    }

    private void hideLoadingOverlay() {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
    }

    private void saveAndPublishItemWithListing() {
        ItemModel item = new ItemModel();
        item.setTitle(etTitle.getText().toString().trim());
        item.setDescription(etDescription.getText().toString().trim());
        item.setCategory(actvCategory.getText().toString());
        item.setCondition(actvCondition.getText().toString());
        java.util.Map<String, Object> locationMap = new java.util.HashMap<>();
        if (selectedLatitude != null && selectedLongitude != null) {
            locationMap.put("_latitude", selectedLatitude);
            locationMap.put("_longitude", selectedLongitude);
            locationMap.put("address", etLocation.getText().toString().trim());
        } else {
            locationMap.put("address", etLocation.getText().toString().trim());
        }
        item.setLocation(locationMap);
        // Tags
        String tagsRaw = etTagInput.getText() != null ? etTagInput.getText().toString() : "";
        java.util.List<String> tags = new java.util.ArrayList<>();
        for (String tag : tagsRaw.split(",")) {
            String trimmed = tag.trim();
            if (!trimmed.isEmpty()) tags.add(trimmed);
        }
        item.setTags(tags);
        // Photos
        if (photoAdapter != null) {
            java.util.List<Uri> uriList = photoAdapter.getPhotoUris();
            if (uriList.isEmpty()) {
                saveItemToFirestore(item, new java.util.ArrayList<>());
                return;
            }
            uploadImagesToCloudinary(uriList, (photoUrls) -> {
                item.setPhotoUris(photoUrls);
                saveItemToFirestore(item, photoUrls);
            });
        } else {
            saveItemToFirestore(item, new java.util.ArrayList<>());
        }
    }

    private void uploadImagesToCloudinary(java.util.List<Uri> uriList, java.util.function.Consumer<java.util.List<String>> callback) {
        java.util.List<String> photoUrls = new java.util.ArrayList<>();
        if (uriList.isEmpty()) {
            callback.accept(photoUrls);
            return;
        }

        Context context = requireContext().getApplicationContext();
        final int total = uriList.size();
        final int[] completed = {0};
        for (Uri uri : uriList) {
            com.example.tradeupapp.utils.CloudinaryManager.uploadImage(context, uri, "trade_items", new com.example.tradeupapp.utils.CloudinaryManager.CloudinaryUploadCallback() {
                @Override
                public void onStart() {}
                @Override
                public void onProgress(double progress) {}
                @Override
                public void onSuccess(String url) {
                    photoUrls.add(url);
                    completed[0]++;
                    if (completed[0] == total) {
                        callback.accept(photoUrls);
                    }
                }
                @Override
                public void onError(String errorMessage) {
                    completed[0]++;
                    if (completed[0] == total) {
                        callback.accept(photoUrls);
                    }
                }
            });
        }
    }

    private void saveItemToFirestore(ItemModel item, java.util.List<String> photoUrls) {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("items")
            .add(item)
            .addOnSuccessListener(documentReference -> {
                String itemId = documentReference.getId();
                db.collection("items").document(itemId).update("id", itemId, "photoUris", photoUrls)
                    .addOnSuccessListener(aVoid -> {
                        // Create and initialize ListingModel here
                        double price = 0.0;
                        try {
                            price = Double.parseDouble(etPrice != null && etPrice.getText() != null ? etPrice.getText().toString().trim() : "0");
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        boolean allowOffersVal = switchAllowOffers == null || switchAllowOffers.isChecked();
                        boolean allowReturnsVal = switchAllowReturns != null && switchAllowReturns.isChecked();
                        String sellerId = com.example.tradeupapp.core.services.FirebaseService.getInstance().getCurrentUserId();
                        com.example.tradeupapp.models.ListingModel listing = new com.example.tradeupapp.models.ListingModel();
                        listing.setItemId(itemId);
                        listing.setId(null);
                        listing.setPrice(price);
                        listing.setSellerId(sellerId);
                        listing.setActive(true);
                        listing.setCreatedAt(com.google.firebase.Timestamp.now());
                        listing.setUpdatedAt(com.google.firebase.Timestamp.now());
                        listing.setTransactionStatus("available");
                        listing.setViewCount(0);
                        listing.setTags(item.getTags());
                        listing.setDistanceRadius(10);
                        listing.setAllowOffers(allowOffersVal);
                        listing.setAllowReturns(allowReturnsVal);
                        db.collection("listings")
                            .add(listing)
                            .addOnSuccessListener(listingRef -> {
                                String listingId = listingRef.getId();
                                listing.setId(listingId);
                                db.collection("listings").document(listingId)
                                    .update("id", listingId)
                                    .addOnSuccessListener(aVoid2 -> {
                                        Toast.makeText(requireContext(), "Listing saved successfully!", Toast.LENGTH_SHORT).show();
                                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                                        navController.navigate(R.id.nav_my_store); // Quay về trang MyStore
                                    })
                                    .addOnFailureListener(e -> {
                                        hideLoadingOverlay();
                                        Toast.makeText(requireContext(), "Failed to update listing id: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                            })
                            .addOnFailureListener(e -> {
                                hideLoadingOverlay();
                                Toast.makeText(requireContext(), "Failed to save listing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    })
                    .addOnFailureListener(e -> {
                        hideLoadingOverlay();
                        Toast.makeText(requireContext(), "Failed to update item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                hideLoadingOverlay();
                Toast.makeText(requireContext(), "Failed to save item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void setLocationFieldWithAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);
                if (addressLine != null && !addressLine.isEmpty()) {
                    etLocation.setText(addressLine);
                } else if (address.getThoroughfare() != null && !address.getThoroughfare().isEmpty()) {
                    etLocation.setText(address.getThoroughfare());
                } else if (address.getLocality() != null && !address.getLocality().isEmpty()) {
                    etLocation.setText(address.getLocality());
                } else {
                    etLocation.setText(String.format(Locale.getDefault(), "%.5f, %.5f", latitude, longitude));
                }
            } else {
                etLocation.setText(String.format(Locale.getDefault(), "%.5f, %.5f", latitude, longitude));
            }
        } catch (IOException e) {
            etLocation.setText(String.format(Locale.getDefault(), "%.5f, %.5f", latitude, longitude));
        }
    }

    private void setupMapPickerResultObserver() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.getCurrentBackStackEntry().getSavedStateHandle().getLiveData("location_data").observe(
            getViewLifecycleOwner(), result -> {
                if (result != null && result instanceof Bundle) {
                    Bundle locationData = (Bundle) result;
                    double latitude = locationData.getDouble("latitude");
                    double longitude = locationData.getDouble("longitude");
                    String address = locationData.getString("address");
                    selectedLatitude = latitude;
                    selectedLongitude = longitude;
                    if (address != null && !address.isEmpty()) {
                        etLocation.setText(address);
                    } else {
                        setLocationFieldWithAddress(latitude, longitude);
                    }
                    navController.getCurrentBackStackEntry().getSavedStateHandle().set("location_data", null);
                }
            });
    }

    @Override
    public void onAddPhotoClicked() {
        // Launch the photo picker intent for multiple images
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        photoPickerLauncher.launch(intent);
    }

    @Override
    public void onPhotoRemoved(int position) {
        // Remove the photo from the adapter
        photoAdapter.removePhoto(position);
    }

    private void autofillLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(requireView(), "Location permission not granted", Snackbar.LENGTH_SHORT).show();
                return;
            }
            // Chỉ gán location nếu selectedLatitude và selectedLongitude đều null (mới khởi tạo)
            if (selectedLatitude == null && selectedLongitude == null) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    fillLocationField(location);
                    selectedLatitude = location.getLatitude();
                    selectedLongitude = location.getLongitude();
                }).addOnFailureListener(e -> {
                    Snackbar.make(requireView(), "Failed to get location", Snackbar.LENGTH_SHORT).show();
                });
            }
        } catch (SecurityException e) {
            Snackbar.make(requireView(), "Location permission denied (SecurityException)", Snackbar.LENGTH_SHORT).show();
        }
    }

    // Helper: set location field with address from lat/lng or fallback to less detailed address
    private void fillLocationField(Location location) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);
                if (addressLine != null && !addressLine.isEmpty()) {
                    etLocation.setText(addressLine);
                } else if (address.getThoroughfare() != null && !address.getThoroughfare().isEmpty()) {
                    // Fallback to street name if addressLine is not available
                    etLocation.setText(address.getThoroughfare());
                } else if (address.getLocality() != null && !address.getLocality().isEmpty()) {
                    // Fallback to district/city
                    etLocation.setText(address.getLocality());
                } else {
                    // Fallback to lat/lng
                    etLocation.setText(location.getLatitude() + ", " + location.getLongitude());
                }
            } else {
                etLocation.setText(location.getLatitude() + ", " + location.getLongitude());
            }
        } catch (IOException e) {
            etLocation.setText(location.getLatitude() + ", " + location.getLongitude());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Restore photoUris and all fields if returning from preview
        if (getArguments() != null) {
            // Restore photoUris
            if (getArguments().containsKey("photoUris")) {
                java.util.ArrayList<String> photoUris = getArguments().getStringArrayList("photoUris");
                if (photoUris != null && photoAdapter != null) {
                    java.util.List<Uri> currentUris = new java.util.ArrayList<>(photoAdapter.getPhotoUris());
                    for (int i = currentUris.size() - 1; i >= 0; i--) {
                        photoAdapter.removePhoto(i);
                    }
                    for (String uriStr : photoUris) {
                        if (uriStr != null && !uriStr.isEmpty()) {
                            photoAdapter.addPhoto(Uri.parse(uriStr));
                        }
                    }
                }
            }
            // Restore item fields
            if (getArguments().containsKey("item")) {
                itemModel = getArguments().getParcelable("item");
                if (itemModel != null) {
                    etTitle.setText(itemModel.getTitle());
                    etDescription.setText(itemModel.getDescription());
                    actvCategory.setText(itemModel.getCategory(), false);
                    actvCondition.setText(itemModel.getCondition(), false);
                    if (itemModel.getLocation() != null) {
                        Object addressObj = itemModel.getLocation().get("address");
                        Object latObj = itemModel.getLocation().get("_latitude");
                        Object lngObj = itemModel.getLocation().get("_longitude");
                        // Always restore all three if present
                        if (addressObj != null) {
                            etLocation.setText(addressObj.toString());
                        }
                        if (latObj instanceof Double) {
                            selectedLatitude = (Double) latObj;
                        } else {
                            selectedLatitude = null;
                        }
                        if (lngObj instanceof Double) {
                            selectedLongitude = (Double) lngObj;
                        } else {
                            selectedLongitude = null;
                        }
                    }
                    if (itemModel.getTags() != null && !itemModel.getTags().isEmpty()) {
                        etTagInput.setText(android.text.TextUtils.join(", ", itemModel.getTags()));
                    }
                }
            }
            // Restore listing fields
            if (getArguments().containsKey("price")) {
                listingPrice = getArguments().getDouble("price", 0.0);
                if (listingPrice > 0) {
                    etPrice.setText(String.format(java.util.Locale.US, "%.0f", listingPrice));
                }
            }
            if (getArguments().containsKey("allowOffers")) {
                allowOffers = getArguments().getBoolean("allowOffers", true);
                switchAllowOffers.setChecked(allowOffers);
            }
            if (getArguments().containsKey("allowReturns")) {
                allowReturns = getArguments().getBoolean("allowReturns", false);
                switchAllowReturns.setChecked(allowReturns);
            }
            // Disable autofill for location field when returning from preview
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                etLocation.setImportantForAutofill(android.view.View.IMPORTANT_FOR_AUTOFILL_NO);
            }
        } else {
            // Only autofill location if not returning from preview
            autofillLocation();
            // Enable autofill for location field
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                etLocation.setImportantForAutofill(android.view.View.IMPORTANT_FOR_AUTOFILL_YES);
            }
        }
        // Listen for tag input changes to update chips
        etTagInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                String tagsRaw = s.toString();
                java.util.List<String> tags = new java.util.ArrayList<>();
                for (String tag : tagsRaw.split(",")) {
                    String trimmed = tag.trim();
                    if (!trimmed.isEmpty()) tags.add(trimmed);
                }
            }
        });
    }

    private void saveItemAndListing() {
        ItemModel item = new ItemModel();
        item.setTitle(etTitle.getText().toString().trim());
        item.setDescription(etDescription.getText().toString().trim());
        item.setCategory(actvCategory.getText().toString());
        item.setCondition(actvCondition.getText().toString());
        java.util.Map<String, Object> locationMap = new java.util.HashMap<>();
        if (selectedLatitude != null && selectedLongitude != null) {
            locationMap.put("_latitude", selectedLatitude);
            locationMap.put("_longitude", selectedLongitude);
            locationMap.put("address", etLocation.getText().toString().trim());
        } else {
            locationMap.put("address", etLocation.getText().toString().trim());
        }
        item.setLocation(locationMap);
        // Tags
        String tagsRaw = etTagInput.getText() != null ? etTagInput.getText().toString() : "";
        java.util.List<String> tags = new java.util.ArrayList<>();
        for (String tag : tagsRaw.split(",")) {
            String trimmed = tag.trim();
            if (!trimmed.isEmpty()) tags.add(trimmed);
        }
        item.setTags(tags);
        // Photos
        java.util.List<Uri> uriList = photoAdapter.getPhotoUris();
        if (uriList.isEmpty()) {
            saveItemToFirestoreAndListing(item, new java.util.ArrayList<>());
        } else {
            uploadImagesToCloudinary(uriList, photoUrls -> {
                item.setPhotoUris(photoUrls);
                saveItemToFirestoreAndListing(item, photoUrls);
            });
        }
    }

    private void saveItemToFirestoreAndListing(ItemModel item, java.util.List<String> photoUrls) {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("items")
            .add(item)
            .addOnSuccessListener(documentReference -> {
                String itemId = documentReference.getId();
                item.setId(itemId);
                db.collection("items").document(itemId).update("id", itemId, "photoUris", photoUrls)
                    .addOnSuccessListener(aVoid -> {
                        String priceStr = etPrice != null && etPrice.getText() != null ? etPrice.getText().toString().trim() : "0";
                        double price;
                        try {
                            price = Double.parseDouble(priceStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        boolean allowOffersVal = switchAllowOffers == null || switchAllowOffers.isChecked();
                        boolean allowReturnsVal = switchAllowReturns != null && switchAllowReturns.isChecked();
                        String sellerId = com.example.tradeupapp.core.services.FirebaseService.getInstance().getCurrentUserId();
                        com.example.tradeupapp.models.ListingModel listing = new com.example.tradeupapp.models.ListingModel();
                        listing.setItemId(itemId);
                        listing.setId(null);
                        listing.setPrice(price);
                        listing.setSellerId(sellerId);
                        listing.setActive(true);
                        listing.setCreatedAt(com.google.firebase.Timestamp.now());
                        listing.setUpdatedAt(com.google.firebase.Timestamp.now());
                        listing.setTransactionStatus("available");
                        listing.setViewCount(0);
                        listing.setTags(item.getTags());
                        listing.setDistanceRadius(10);
                        listing.setAllowOffers(allowOffersVal);
                        listing.setAllowReturns(allowReturnsVal);
                        db.collection("listings")
                            .add(listing)
                            .addOnSuccessListener(listingRef -> {
                                String listingId = listingRef.getId();
                                listing.setId(listingId);
                                db.collection("listings").document(listingId)
                                    .update("id", listingId)
                                    .addOnSuccessListener(aVoid2 -> {
                                        Toast.makeText(requireContext(), "Listing saved successfully!", Toast.LENGTH_SHORT).show();
                                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                                        navController.navigate(R.id.nav_my_store); // Quay về trang MyStore
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to update listing id: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to save listing: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to update item: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            })
            .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to save item: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveAndPublishItemWithListing(ItemModel item, java.util.List<String> photoUrls) {
        if (item == null) item = new ItemModel();
        // Null-safe getText usage
        String title = etTitle != null && etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String description = etDescription != null && etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String category = actvCategory != null && actvCategory.getText() != null ? actvCategory.getText().toString() : "";
        String condition = actvCondition != null && actvCondition.getText() != null ? actvCondition.getText().toString() : "";
        item.setTitle(title);
        item.setDescription(description);
        item.setCategory(category);
        item.setCondition(condition);
        java.util.Map<String, Object> locationMap = new java.util.HashMap<>();
        if (selectedLatitude != null && selectedLongitude != null) {
            locationMap.put("_latitude", selectedLatitude);
            locationMap.put("_longitude", selectedLongitude);
            locationMap.put("address", etLocation != null && etLocation.getText() != null ? etLocation.getText().toString().trim() : "");
        } else {
            locationMap.put("address", etLocation != null && etLocation.getText() != null ? etLocation.getText().toString().trim() : "");
        }
        item.setLocation(locationMap);
        // Tags
        String tagsRaw = etTagInput != null && etTagInput.getText() != null ? etTagInput.getText().toString() : "";
        java.util.List<String> tags = new java.util.ArrayList<>();
        for (String tag : tagsRaw.split(",")) {
            String trimmed = tag.trim();
            if (!trimmed.isEmpty()) tags.add(trimmed);
        }
        item.setTags(tags);
        item.setPhotoUris(photoUrls);
        // Listing fields
        String priceStr = etPrice != null && etPrice.getText() != null ? etPrice.getText().toString().trim() : "0";
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean allowOffersVal = switchAllowOffers == null || switchAllowOffers.isChecked();
        boolean allowReturnsVal = switchAllowReturns != null && switchAllowReturns.isChecked();
        String sellerId = com.example.tradeupapp.core.services.FirebaseService.getInstance().getCurrentUserId();
        com.example.tradeupapp.models.ListingModel listing = new com.example.tradeupapp.models.ListingModel();
        listing.setItemId(item.getId());
        listing.setId(null);
        listing.setPrice(price);
        listing.setSellerId(sellerId);
        listing.setActive(true);
        listing.setCreatedAt(com.google.firebase.Timestamp.now());
        listing.setUpdatedAt(com.google.firebase.Timestamp.now());
        listing.setTransactionStatus("available");
        listing.setViewCount(0);
        listing.setTags(item.getTags());
        listing.setDistanceRadius(10);
        listing.setAllowOffers(allowOffersVal);
        listing.setAllowReturns(allowReturnsVal);
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("items")
            .add(item)
            .addOnSuccessListener(documentReference -> {
                String itemId = documentReference.getId();
                db.collection("items").document(itemId).update("id", itemId, "photoUris", photoUrls)
                    .addOnSuccessListener(aVoid -> {
                        listing.setItemId(itemId);
                        db.collection("listings")
                            .add(listing)
                            .addOnSuccessListener(listingRef -> {
                                String listingId = listingRef.getId();
                                listing.setId(listingId);
                                db.collection("listings").document(listingId)
                                    .update("id", listingId)
                                    .addOnSuccessListener(aVoid2 -> {
                                        hideLoadingOverlay();
                                        Toast.makeText(requireContext(), "Listing saved successfully!", Toast.LENGTH_SHORT).show();
                                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                                        navController.navigate(R.id.nav_my_store); // Quay về trang MyStore
                                    })
                                    .addOnFailureListener(e -> {
                                        hideLoadingOverlay();
                                        Toast.makeText(requireContext(), "Failed to update listing id: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                            })
                            .addOnFailureListener(e -> {
                                hideLoadingOverlay();
                                Toast.makeText(requireContext(), "Failed to save listing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    })
                    .addOnFailureListener(e -> {
                        hideLoadingOverlay();
                        Toast.makeText(requireContext(), "Failed to update item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                hideLoadingOverlay();
                Toast.makeText(requireContext(), "Failed to save item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
