package com.example.tradeupapp.features.listing.ui;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.shared.adapters.PhotoUploadAdapter;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.core.services.FirebaseService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UpdateItemFragment extends Fragment implements PhotoUploadAdapter.OnPhotoActionListener {
    private MaterialToolbar toolbar;
    private TextInputEditText etTitle;
    private TextInputEditText etPrice;
    private TextInputEditText etDescription;
    private TextInputEditText etLocation;
    private TextInputEditText etTagInput;
    private AutoCompleteTextView actvCategory;
    private AutoCompleteTextView actvCondition;
    private RecyclerView recyclerPhotos;
    private RecyclerView recyclerTags;
    private PhotoUploadAdapter photoAdapter;
    private TagAdapter tagAdapter;
    private MaterialButton btnContinue; // Use MaterialButton for update
    private MaterialSwitch switchAllowOffers;
    private MaterialSwitch switchAllowReturns;
    private MaterialSwitch switchFeatured;
    private ListingModel editingListing = null;
    private ItemModel editingItem = null;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_item, container, false);
        initViews(view);
        setupPhotoAdapter();
        setupDropdowns();
        setupClickListeners();
        setupMapPickerResultObserver();
        firestore = FirebaseFirestore.getInstance();
        setupTagAdapter(view);
        fetchTagsFromFirestore();
        // --- Handle arguments from ItemDetailFragment ---
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey("listingId")) {
                String listingId = args.getString("listingId");
                FirebaseService.getInstance().getAllListings(new FirebaseService.ListingsCallback() {
                    @Override
                    public void onSuccess(java.util.List<ListingModel> listings) {
                        for (ListingModel l : listings) {
                            if (l.getId() != null && l.getId().equals(listingId)) {
                                editingListing = l;
                                break;
                            }
                        }
                        if (editingListing != null && editingListing.getItemId() != null) {
                            FirebaseService.getInstance().getItemById(editingListing.getItemId(), new FirebaseService.ItemCallback() {
                                @Override
                                public void onSuccess(ItemModel item) {
                                    editingItem = item;
                                    prefillFormForUpdate();
                                }
                                @Override
                                public void onError(String error) {
                                    Toast.makeText(requireContext(), "Failed to load item", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(requireContext(), "Failed to load listing", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Fallback: support old argument passing
                if (args.containsKey("listing")) {
                    editingListing = (ListingModel) args.getSerializable("listing");
                }
                if (args.containsKey("item")) {
                    editingItem = args.getParcelable("item");
                }
                prefillFormForUpdate();
            }
        }
        // Hide Save button for update mode
        MaterialButton btnSave = view.findViewById(R.id.btn_save);
        if (btnSave != null) btnSave.setVisibility(View.GONE);
        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.add_item_toolbar);
        setupToolbar();
        etTitle = view.findViewById(R.id.et_title);
        etPrice = view.findViewById(R.id.et_price);
        etDescription = view.findViewById(R.id.et_description);
        etLocation = view.findViewById(R.id.et_location);
        etTagInput = view.findViewById(R.id.et_tag_input);
        recyclerPhotos = view.findViewById(R.id.recycler_photos);
        recyclerTags = view.findViewById(R.id.recycler_tags);
        actvCategory = view.findViewById(R.id.actv_category);
        actvCondition = view.findViewById(R.id.actv_condition);
        btnContinue = view.findViewById(R.id.btn_preview); // Use MaterialButton
        switchAllowOffers = view.findViewById(R.id.switch_allow_offers);
        switchAllowReturns = view.findViewById(R.id.switch_allow_returns);
        // chipGroupTags = view.findViewById(R.id.chip_group_tags); // REMOVED: No longer used, replaced by recyclerTags
        recyclerTags = view.findViewById(R.id.recycler_tags); // Use RecyclerView for tags
    }

    private void setupPhotoAdapter() {
        photoAdapter = new PhotoUploadAdapter(this);
        recyclerPhotos.setAdapter(photoAdapter);
    }

    private void setupTagAdapter(View view) {
        recyclerTags.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        tagAdapter = new TagAdapter(selectedTags -> {
            // Update etTagInput when tags are selected/deselected
            etTagInput.setText(TextUtils.join(", ", selectedTags));
        });
        recyclerTags.setAdapter(tagAdapter);
        // Listen for tag input changes to update chips in adapter (tách tag giống AddItemFragment)
        etTagInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                String tagsRaw = s.toString();
                List<String> tags = new ArrayList<>();
                for (String tag : tagsRaw.split(",")) {
                    String trimmed = tag.trim();
                    if (!trimmed.isEmpty()) tags.add(trimmed);
                }
                tagAdapter.setSelectedTags(tags);
            }
        });
    }

    private void setupDropdowns() {
        String[] categories = {"Electronics", "Clothing", "Home & Garden", "Toys", "Books", "Sports", "Automotive", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories);
        actvCategory.setAdapter(categoryAdapter);
        String[] conditions = {"New", "Like New", "Good", "Fair", "Poor"};
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                conditions);
        actvCondition.setAdapter(conditionAdapter);
    }

    private void setupClickListeners() {
        etLocation.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            Bundle args = new Bundle();
            args.putBoolean("isFullAddress", true);
            navController.navigate(R.id.action_updateItemFragment_to_mapPickerFragment, args);
        });
        btnContinue.setText("Update Listing"); // Use hardcoded string for now
        btnContinue.setOnClickListener(v -> {
            if (validateInput()) {
                updateListing();
            }
        });
        // TODO: Implement tilLocation.setEndIconOnClickListener if needed
        // TODO: Implement setupTextWatchers if needed
    }

    private void prefillFormForUpdate() {
        if (editingItem != null) {
            etTitle.setText(editingItem.getTitle());
            etDescription.setText(editingItem.getDescription());
            actvCategory.setText(editingItem.getCategory(), false);
            actvCondition.setText(editingItem.getCondition(), false);
            if (editingItem.getLocation() != null && editingItem.getLocation().get("address") != null) {
                etLocation.setText(editingItem.getLocation().get("address").toString());
            } else {
                etLocation.setText("");
            }
            if (editingItem.getPhotoUris() != null) {
                for (String uriStr : editingItem.getPhotoUris()) {
                    if (uriStr != null) photoAdapter.addPhoto(Uri.parse(uriStr));
                }
            }
            // Ghép các tag thành 1 string, nối bằng dấu phẩy
            if (editingItem.getTags() != null && !editingItem.getTags().isEmpty()) {
                String joinedTags = android.text.TextUtils.join(", ", editingItem.getTags());
                etTagInput.setText(joinedTags);
            } else {
                etTagInput.setText("");
            }
        }
        if (editingListing != null) {
            // Parse price from editingListing and set to etPrice (raw, no formatting)
            double rawPrice = editingListing.getPrice();
            etPrice.setText(String.valueOf((long) rawPrice));
            // Always set allowOffers and allowReturns with default if missing
            boolean allowOffers = editingListing.getAllowOffers();
            boolean allowReturns = editingListing.isAllowReturns();
            switchAllowOffers.setChecked(allowOffers);
            switchAllowReturns.setChecked(allowReturns);
            // switchFeatured.setChecked(editingListing.isFeatured()); // Uncomment if needed
        }
    }

    private void updateListing() {
        if (editingListing == null || editingItem == null) return;
        // Title
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        editingItem.setTitle(title);
        // Description
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        editingItem.setDescription(description);
        // Category
        String category = actvCategory.getText() != null ? actvCategory.getText().toString().trim() : "";
        editingItem.setCategory(category);
        // Condition
        String condition = actvCondition.getText() != null ? actvCondition.getText().toString().trim() : "";
        editingItem.setCondition(condition);
        // Location
        String address = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
        java.util.Map<String, Object> location = new java.util.HashMap<>();
        location.put("address", address);
        Object tag = etLocation.getTag();
        if (tag instanceof double[]) {
            double[] latLng = (double[]) tag;
            location.put("latitude", latLng[0]);
            location.put("longitude", latLng[1]);
        }
        editingItem.setLocation(location);
        // Photos
        if (photoAdapter != null) {
            java.util.List<Uri> uriList = photoAdapter.getPhotoUris();
            java.util.List<String> stringList = new java.util.ArrayList<>();
            for (Uri uri : uriList) {
                if (uri != null) stringList.add(uri.toString());
            }
            editingItem.setPhotoUris(stringList);
        }
        // Tags
        String tagsRaw = etTagInput.getText() != null ? etTagInput.getText().toString() : "";
        java.util.List<String> tags = new java.util.ArrayList<>();
        for (String tagStr : tagsRaw.split(",")) {
            String trimmed = tagStr.trim();
            if (!trimmed.isEmpty()) tags.add(trimmed);
        }
        editingItem.setTags(tags);
        editingListing.setTags(tags); // Ensure tags are also set on listing
        // Price
        double price = 0.0;
        try {
            String priceStr = etPrice.getText() != null ? etPrice.getText().toString().replace(",", "").trim() : "0";
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException ignored) {}
        editingListing.setPrice(price);
        // Allow Offers
        boolean allowOffers = switchAllowOffers != null && switchAllowOffers.isChecked();
        editingListing.setAllowOffers(allowOffers);
        // Allow Returns
        boolean allowReturns = switchAllowReturns != null && switchAllowReturns.isChecked();
        editingListing.setAllowReturns(allowReturns);
        // Save changes to Firestore
        FirebaseService.getInstance().updateListingAndItem(editingListing, editingItem, new FirebaseService.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Listing updated successfully!", Toast.LENGTH_SHORT).show();
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.nav_my_store); // Quay về trang MyStore sau khi update
            }
            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Update failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput() {
        boolean isValid = true;
        if (etTitle.getText() == null || etTitle.getText().toString().trim().isEmpty()) {
            etTitle.setError("Please enter a title");
            isValid = false;
        } else {
            etTitle.setError(null);
        }
        if (etPrice.getText() == null || etPrice.getText().toString().trim().isEmpty()) {
            etPrice.setError("Please enter a price");
            isValid = false;
        } else {
            etPrice.setError(null);
        }
        if (etDescription.getText() == null || etDescription.getText().toString().trim().isEmpty()) {
            etDescription.setError("Please enter a description");
            isValid = false;
        } else {
            etDescription.setError(null);
        }
        return isValid;
    }

    private void setupToolbar() {
        if (toolbar != null) {
            if ("add_item_toolbar".equals(toolbar.getTag())) {
                ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
                if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
                    ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
                }
                toolbar.setNavigationOnClickListener(v -> {
                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                    navController.popBackStack();
                });
                toolbar.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_save) {
                        if (validateInput()) {
                            updateListing();
                        }
                        return true;
                    }
                    return false;
                });
            }
        }
    }

    private void setupMapPickerResultObserver() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.getCurrentBackStackEntry().getSavedStateHandle().getLiveData("location_data").observe(
                getViewLifecycleOwner(), result -> {
                    if (result != null && result instanceof Bundle) {
                        Bundle locationData = (Bundle) result;
                        String address = locationData.getString("address");
                        if (address != null) {
                            etLocation.setText(address);
                        }
                        double latitude = locationData.getDouble("latitude");
                        double longitude = locationData.getDouble("longitude");
                        etLocation.setTag(new double[]{latitude, longitude});
                        navController.getCurrentBackStackEntry().getSavedStateHandle().set("location_data", null);
                    }
                });
    }

    /**
     * Fetch tags from Firestore and display in recyclerTags using TagAdapter.
     * This logic is separate from saving tags to Firestore.
     */
    private void fetchTagsFromFirestore() {
        firestore.collection("tags")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> tags = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String tag = doc.getString("name");
                        if (tag != null && !tag.isEmpty()) {
                            tags.add(tag);
                        }
                    }
                    tagAdapter.setTags(tags);
                    // Prefill selected tags if editing
                    if (editingItem != null && editingItem.getTags() != null) {
                        tagAdapter.setSelectedTags(editingItem.getTags());
                    }
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Prefill tags if editing (for update or draft)
        if (editingItem != null && editingItem.getTags() != null && !editingItem.getTags().isEmpty()) {
            etTagInput.setText(android.text.TextUtils.join(", ", editingItem.getTags()));
            tagAdapter.setSelectedTags(new ArrayList<>(editingItem.getTags()));
        }
        // Always update etTagInput when tagAdapter changes (ensure comma-separated string)
        tagAdapter.setOnTagsChangedListener(selectedTags -> {
            etTagInput.setText(android.text.TextUtils.join(", ", selectedTags));
        });
    }

    // PhotoUploadAdapter.OnPhotoActionListener methods
    @Override
    public void onAddPhotoClicked() {
        // TODO: Implement photo picker logic if needed
        Toast.makeText(requireContext(), "Photo picker not implemented in update screen.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPhotoRemoved(int position) {
        if (photoAdapter != null) {
            photoAdapter.removePhoto(position);
        }
    }
}
