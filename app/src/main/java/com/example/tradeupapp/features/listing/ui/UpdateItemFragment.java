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
    private MaterialButton btnContinue;
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
        if (getArguments() != null) {
            if (getArguments().containsKey("listing")) {
                editingListing = (ListingModel) getArguments().getSerializable("listing");
            }
            if (getArguments().containsKey("item")) {
                editingItem = getArguments().getParcelable("item");
            }
            prefillFormForUpdate();
        }
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
        btnContinue = view.findViewById(R.id.btn_preview);
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
            navController.navigate(R.id.action_updateItemFragment_to_mapPickerFragment);
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
            // Set tags to tag input field as comma separated
            if (editingItem.getTags() != null && !editingItem.getTags().isEmpty()) {
                etTagInput.setText(android.text.TextUtils.join(", ", editingItem.getTags()));
            }
        }
        if (editingListing != null) {
            // Format price with grouping and no scientific notation
            java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
            nf.setGroupingUsed(true);
            nf.setMaximumFractionDigits(0);
            String formattedPrice = nf.format(editingListing.getPrice());
            etPrice.setText(formattedPrice);
            // Always set allowOffers and allowReturns with default if missing
            boolean allowOffers = editingListing.isAllowOffers();
            boolean allowReturns = editingListing.isAllowReturns();
            switchAllowOffers.setChecked(allowOffers);
            switchAllowReturns.setChecked(allowReturns);
            // switchFeatured.setChecked(editingListing.isFeatured()); // Uncomment if needed
        }
    }

    private void updateListing() {
        if (editingListing == null || editingItem == null) return;
        editingItem.setTitle(etTitle.getText() != null ? etTitle.getText().toString().trim() : "");
        editingItem.setDescription(etDescription.getText() != null ? etDescription.getText().toString().trim() : "");
        editingItem.setCategory(actvCategory.getText().toString());
        editingItem.setCondition(actvCondition.getText().toString());
        // Set location as a map with address string
        String address = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
        java.util.Map<String, Object> location = new java.util.HashMap<>();
        location.put("address", address);
        editingItem.setLocation(location);
        if (photoAdapter != null) {
            java.util.List<Uri> uriList = photoAdapter.getPhotoUris();
            java.util.List<String> stringList = new java.util.ArrayList<>();
            for (Uri uri : uriList) {
                if (uri != null) stringList.add(uri.toString());
            }
            editingItem.setPhotoUris(stringList);
        }
        String tagsRaw = etTagInput.getText() != null ? etTagInput.getText().toString() : "";
        java.util.List<String> tags = new java.util.ArrayList<>();
        for (String tag : tagsRaw.split(",")) {
            if (!tag.trim().isEmpty()) tags.add(tag.trim());
        }
        // TODO: Uncomment if setTags exists in ItemModel
        // editingItem.setTags(tags);
        double price = 0.0;
        try {
            price = Double.parseDouble(etPrice.getText() != null ? etPrice.getText().toString().trim() : "0");
        } catch (NumberFormatException ignored) {}
        editingListing.setPrice(price);
        // Always set allowOffers and allowReturns with default if missing
        boolean allowOffers = switchAllowOffers != null ? switchAllowOffers.isChecked() : true;
        boolean allowReturns = switchAllowReturns != null ? switchAllowReturns.isChecked() : false;
        editingListing.setAllowOffers(allowOffers);
        editingListing.setAllowReturns(allowReturns);
        // editingListing.setFeatured(switchFeatured.isChecked()); // Uncomment if needed

        // Save changes to Firestore
        FirebaseService.getInstance().updateListingAndItem(editingListing, editingItem, new FirebaseService.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Listing updated successfully!", Toast.LENGTH_SHORT).show();
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.popBackStack();
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
                        double latitude = locationData.getDouble("latitude");
                        double longitude = locationData.getDouble("longitude");
                        String address = locationData.getString("address");
                        etLocation.setText(address);
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
            etTagInput.setText(TextUtils.join(", ", editingItem.getTags()));
            tagAdapter.setSelectedTags(new ArrayList<>(editingItem.getTags()));
        }
        // Listen for tag input changes to update chips in adapter
        etTagInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                // Only update chips if user is typing, not when chips are checked
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
