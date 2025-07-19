package com.example.tradeupapp.features.listing.ui;

import android.net.Uri;
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
import androidx.viewpager2.widget.ViewPager2;

import com.example.tradeupapp.R;
import com.example.tradeupapp.shared.adapters.ImagePhotoAdapter;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Locale;

public class ItemPreviewFragment extends Fragment {

    private static final String ARG_ITEM = "item";

    private ItemModel itemModel; // Item fields only
    private double listingPrice; // Listing field
    private boolean allowOffers = true; // Listing field
    private boolean allowReturns = false; // Listing field
    private ViewPager2 photosViewPager;
    private TextView tvTitle;
    private TextView tvPrice;
    private TextView tvDescription;
    private TextView tvCategory;
    private TextView tvCondition;
    private TextView tvLocation;
    private MaterialButton btnEdit;
    private MaterialButton btnPublish;
    private ImagePhotoAdapter photoAdapter;
    private ChipGroup chipGroupTags;
    private TextView tvAllowOffers;
    private TextView tvAllowReturns;
    private TextView tvFeatured;
    private View loadingOverlayPreview;

    public static ItemPreviewFragment newInstance(ItemModel item) {
        ItemPreviewFragment fragment = new ItemPreviewFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itemModel = getArguments().getParcelable(ARG_ITEM);
            listingPrice = getArguments().getDouble("price", 0.0);
            allowOffers = getArguments().getBoolean("allowOffers", true);
            allowReturns = getArguments().getBoolean("allowReturns", false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        populateItemDetails();
        setupListeners();
    }

    private void initViews(View view) {
        photosViewPager = view.findViewById(R.id.viewpager_photos);
        tvTitle = view.findViewById(R.id.tv_item_title);
        tvPrice = view.findViewById(R.id.tv_item_price);
        tvDescription = view.findViewById(R.id.tv_item_description);
        tvCategory = view.findViewById(R.id.tv_item_category);
        tvCondition = view.findViewById(R.id.tv_item_condition);
        tvLocation = view.findViewById(R.id.tv_item_location);
        btnEdit = view.findViewById(R.id.btn_edit);
        btnPublish = view.findViewById(R.id.btn_publish);
        chipGroupTags = view.findViewById(R.id.tag_group_preview);
        tvAllowOffers = view.findViewById(R.id.tv_allow_offers);
        tvAllowReturns = view.findViewById(R.id.tv_allow_returns);
        tvFeatured = view.findViewById(R.id.tv_featured);
        loadingOverlayPreview = view.findViewById(R.id.loading_overlay_preview);
    }

    private void populateItemDetails() {
        if (itemModel == null) {
            Toast.makeText(requireContext(), "Error loading item details", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }
        // Use photoUris from arguments if available
        java.util.List<Uri> uriList = new java.util.ArrayList<>();
        Bundle previewArgs = getArguments();
        if (previewArgs != null && previewArgs.containsKey("photoUris")) {
            java.util.ArrayList<String> photoUris = previewArgs.getStringArrayList("photoUris");
            if (photoUris != null) {
                for (String uriStr : photoUris) {
                    if (uriStr != null) uriList.add(Uri.parse(uriStr));
                }
            }
        } else if (itemModel.getPhotoUris() != null) {
            for (String uriStr : itemModel.getPhotoUris()) {
                if (uriStr != null) uriList.add(Uri.parse(uriStr));
            }
        }
        photoAdapter = new ImagePhotoAdapter(requireContext(), uriList);
        photosViewPager.setAdapter(photoAdapter);
        tvTitle.setText(itemModel.getTitle());
        // Show price from separate variable
        if (listingPrice > 0) {
            tvPrice.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", listingPrice));
        } else {
            tvPrice.setText("");
        }
        tvDescription.setText(itemModel.getDescription());
        tvCategory.setText(itemModel.getCategory());
        tvCondition.setText(itemModel.getCondition());
        // Show address if available, else lat/lng, else empty
        String locationText = "";
        if (itemModel.getLocation() != null) {
            Object addressObj = itemModel.getLocation().get("address");
            Object latObj = itemModel.getLocation().get("_latitude");
            Object lngObj = itemModel.getLocation().get("_longitude");
            if (addressObj != null) {
                locationText = addressObj.toString();
                if (latObj != null && lngObj != null) {
                    locationText += String.format(Locale.getDefault(), "\n(%.5f, %.5f)", ((Number)latObj).doubleValue(), ((Number)lngObj).doubleValue());
                }
            } else if (latObj != null && lngObj != null) {
                locationText = String.format(Locale.getDefault(), "%.5f, %.5f", ((Number)latObj).doubleValue(), ((Number)lngObj).doubleValue());
            }
        }
        tvLocation.setText(locationText);
        // Clear previous tags
        chipGroupTags.removeAllViews();
        // Show tags as Chips
        if (itemModel.getTags() != null && !itemModel.getTags().isEmpty()) {
            for (String tag : itemModel.getTags()) {
                Chip chip = new Chip(requireContext());
                chip.setText(tag);
                chip.setCloseIconVisible(true); // Use setCloseIconVisible instead of deprecated setCloseIconEnabled
                chip.setOnCloseIconClickListener(v -> chipGroupTags.removeView(chip));
                chipGroupTags.addView(chip);
            }
        } else {
            Chip chip = new Chip(requireContext());
            chip.setText("-");
            chip.setEnabled(false);
            chipGroupTags.addView(chip);
        }
        // Show item behavior (these should be added to ItemModel and set in AddItemFragment)
        boolean allowOffersVal = allowOffers;
        boolean allowReturnsVal = allowReturns;
        tvAllowOffers.setText(allowOffersVal ? "Allow Offers from Buyers" : "Offers Not Allowed");
        tvAllowReturns.setText(allowReturnsVal ? "Allow Returns" : "No Returns");
        tvFeatured.setVisibility(View.GONE);
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> {
            Bundle previewArgs = getArguments();
            Bundle args = new Bundle();
            if (previewArgs != null && previewArgs.containsKey("photoUris")) {
                args.putStringArrayList("photoUris", previewArgs.getStringArrayList("photoUris"));
            } else if (itemModel != null && itemModel.getPhotoUris() != null) {
                args.putStringArrayList("photoUris", new java.util.ArrayList<>(itemModel.getPhotoUris()));
            }
            if (itemModel != null) {
                args.putParcelable("item", itemModel);
            }
            // Pass listing fields back to AddItemFragment
            args.putDouble("price", listingPrice);
            args.putBoolean("allowOffers", allowOffers);
            args.putBoolean("allowReturns", allowReturns);
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_itemPreviewFragment_to_nav_add, args);
        });

        btnPublish.setOnClickListener(v -> {
            showLoadingOverlayPreview();
            java.util.List<Uri> photoUris = new java.util.ArrayList<>();
            Bundle previewArgs = getArguments();
            if (previewArgs != null && previewArgs.containsKey("photoUris")) {
                java.util.ArrayList<String> photoUriStrings = previewArgs.getStringArrayList("photoUris");
                if (photoUriStrings != null) {
                    for (String uriStr : photoUriStrings) {
                        if (uriStr != null) photoUris.add(Uri.parse(uriStr));
                    }
                }
            } else if (itemModel.getPhotoUris() != null) {
                for (String uriStr : itemModel.getPhotoUris()) {
                    if (uriStr != null) photoUris.add(Uri.parse(uriStr));
                }
            }
            if (photoUris.isEmpty()) {
                saveItemAndListing(new java.util.ArrayList<>());
            } else {
                uploadImagesToCloudinary(photoUris, urls -> {
                    saveItemAndListing(urls);
                });
            }
        });
    }

    private void uploadImagesToCloudinary(java.util.List<Uri> uriList, java.util.function.Consumer<java.util.List<String>> callback) {
        java.util.List<String> photoUrls = new java.util.ArrayList<>();
        if (uriList.isEmpty()) {
            callback.accept(photoUrls);
            return;
        }
        android.content.Context context = requireContext().getApplicationContext();
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

    private void saveItemAndListing(java.util.List<String> photoUrls) {
        com.example.tradeupapp.core.services.FirebaseService firebaseService = com.example.tradeupapp.core.services.FirebaseService.getInstance();
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        itemModel.setPhotoUris(photoUrls);
        db.collection("items")
            .add(itemModel)
            .addOnSuccessListener(documentReference -> {
                String itemId = documentReference.getId();
                itemModel.setId(itemId);
                db.collection("items").document(itemId).update("id", itemId, "photoUris", photoUrls)
                    .addOnSuccessListener(aVoid -> {
                        String sellerId = firebaseService.getCurrentUserId();
                        com.example.tradeupapp.models.ListingModel listing = new com.example.tradeupapp.models.ListingModel();
                        listing.setItemId(itemId);
                        listing.setId(null);
                        listing.setPrice(listingPrice);
                        listing.setSellerId(sellerId);
                        listing.setActive(true);
                        listing.setCreatedAt(com.google.firebase.Timestamp.now());
                        listing.setUpdatedAt(com.google.firebase.Timestamp.now());
                        listing.setTransactionStatus("available");
                        listing.setViewCount(0);
                        listing.setTags(itemModel.getTags());
                        listing.setDistanceRadius(10);
                        listing.setAllowOffers(allowOffers);
                        listing.setAllowReturns(allowReturns);
                        db.collection("listings")
                            .add(listing)
                            .addOnSuccessListener(listingRef -> {
                                String listingId = listingRef.getId();
                                listing.setId(listingId);
                                db.collection("listings").document(listingId)
                                    .update("id", listingId)
                                    .addOnSuccessListener(aVoid2 -> {
                                        hideLoadingOverlayPreview();
                                        Toast.makeText(requireContext(), "Item published successfully!", Toast.LENGTH_SHORT).show();
                                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                                        navController.navigate(R.id.nav_recommendations);
                                    })
                                    .addOnFailureListener(e -> {
                                        hideLoadingOverlayPreview();
                                        Toast.makeText(requireContext(), "Failed to update listing id: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                            })
                            .addOnFailureListener(e -> {
                                hideLoadingOverlayPreview();
                                Toast.makeText(requireContext(), "Failed to publish listing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    })
                    .addOnFailureListener(e -> {
                        hideLoadingOverlayPreview();
                        Toast.makeText(requireContext(), "Failed to update item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                hideLoadingOverlayPreview();
                Toast.makeText(requireContext(), "Failed to save item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void showLoadingOverlayPreview() {
        if (loadingOverlayPreview != null) loadingOverlayPreview.setVisibility(View.VISIBLE);
    }

    private void hideLoadingOverlayPreview() {
        if (loadingOverlayPreview != null) loadingOverlayPreview.setVisibility(View.GONE);
    }
}
