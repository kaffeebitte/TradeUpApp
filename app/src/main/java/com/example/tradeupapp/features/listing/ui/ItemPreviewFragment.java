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

public class ItemPreviewFragment extends Fragment {

    private static final String ARG_ITEM = "item";

    private ItemModel item;
    private ListingModel listing; // Add this if you want to show price
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
    private TextView tvTags;
    private TextView tvAllowOffers;
    private TextView tvAllowReturns;
    private TextView tvFeatured;

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
            item = getArguments().getParcelable(ARG_ITEM);
            // Optionally get listing if passed as argument
            if (getArguments().containsKey("listing")) {
                listing = (ListingModel) getArguments().getSerializable("listing");
            }
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
        tvTags = view.findViewById(R.id.tv_item_tags);
        tvAllowOffers = view.findViewById(R.id.tv_allow_offers);
        tvAllowReturns = view.findViewById(R.id.tv_allow_returns);
        tvFeatured = view.findViewById(R.id.tv_featured);
    }

    private void populateItemDetails() {
        if (item == null) {
            Toast.makeText(requireContext(), "Error loading item details", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }
        // Convert List<String> to List<Uri> for the photo adapter
        java.util.List<Uri> uriList = new java.util.ArrayList<>();
        if (item.getPhotoUris() != null) {
            for (String uriStr : item.getPhotoUris()) {
                if (uriStr != null) uriList.add(Uri.parse(uriStr));
            }
        }
        photoAdapter = new ImagePhotoAdapter(requireContext(), uriList);
        photosViewPager.setAdapter(photoAdapter);
        tvTitle.setText(item.getTitle());
        // Show price if listing is available, otherwise hide or show placeholder
        if (listing != null) {
            tvPrice.setText(String.format("%,.0f VNĐ", listing.getPrice()));
        } else {
            tvPrice.setText(""); // or hide the view
        }
        tvDescription.setText(item.getDescription());
        tvCategory.setText(item.getCategory());
        tvCondition.setText(item.getCondition());
        // Show address if available, else lat/lng, else empty
        String locationText = "";
        if (item.getLocation() != null) {
            Object addressObj = item.getLocation().get("address");
            if (addressObj != null) {
                locationText = addressObj.toString();
            } else {
                Double lat = item.getLocationLatitude();
                Double lng = item.getLocationLongitude();
                if (lat != null && lng != null) {
                    locationText = String.format("%.5f, %.5f", lat, lng);
                }
            }
        }
        tvLocation.setText(locationText);
        // Show tags (comma separated)
        if (item.getKeyFeatures() != null && !item.getKeyFeatures().isEmpty()) {
            tvTags.setText(android.text.TextUtils.join(", ", item.getKeyFeatures()));
        } else {
            tvTags.setText("-");
        }
        // Show item behavior (these should be added to ItemModel and set in AddItemFragment)
        boolean allowOffers = false;
        boolean allowReturns = false;
        boolean featured = false;
        Bundle args = getArguments();
        if (args != null) {
            allowOffers = args.getBoolean("allow_offers", false);
            allowReturns = args.getBoolean("allow_returns", false);
            featured = args.getBoolean("featured", false);
        }
        tvAllowOffers.setText(allowOffers ? "Allow Offers from Buyers" : "Offers Not Allowed");
        tvAllowReturns.setText(allowReturns ? "Allow Returns" : "No Returns");
        tvFeatured.setText(featured ? "Featured Listing" : "Not Featured");
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> {
            // Go back to edit the item
            requireActivity().onBackPressed();
        });

        btnPublish.setOnClickListener(v -> {
            // TODO: Implement API call to publish the item
            Toast.makeText(requireContext(), "Item published successfully!", Toast.LENGTH_SHORT).show();

            // Navigate back to home screen
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.nav_recommendations);
        });
    }
}
