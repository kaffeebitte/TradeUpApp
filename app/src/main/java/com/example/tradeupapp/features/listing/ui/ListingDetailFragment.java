package com.example.tradeupapp.features.listing.ui;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.tradeupapp.R;
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.models.User;
import com.example.tradeupapp.shared.adapters.ImageSliderAdapter;
import com.example.tradeupapp.shared.adapters.ListingAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class ListingDetailFragment extends Fragment {
    private ListingModel listing;
    private ItemModel item;
    private Toolbar toolbar;
    private TextView tvItemTitle, tvItemPrice, tvDescription, tvCondition, tvItemLocation;
    private ViewPager2 itemImagesViewPager;
    private TabLayout imageIndicator;
    private MaterialButton btnBuyNow, btnMakeOffer, btnMessage, btnViewOffers, btnUpdateListing;
    private FirebaseService firebaseService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private View sellerContainer;
    private ImageView ivSellerAvatar, ivStar;
    private TextView tvSellerName, tvSellerRating;
    private TextView tvViewCount, tvSaveCount, tvShareCount;
    private TextView tvItemStatus;
    private ChipGroup tagGroup;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseService = FirebaseService.getInstance();
        // Get listingId from arguments
        if (getArguments() != null && getArguments().containsKey("listingId")) {
            String listingId = getArguments().getString("listingId");
            loadListingAndItem(listingId);
        } else {
            showError();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        itemImagesViewPager.setAdapter(new ImageSliderAdapter(requireContext(), new ArrayList<>()));
        new TabLayoutMediator(imageIndicator, itemImagesViewPager, (tab, position) -> {}).attach();
        setupToolbar();
    }

    private void loadListingAndItem(String listingId) {
        firebaseService.getAllListings(new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(java.util.List<ListingModel> listings) {
                for (ListingModel l : listings) {
                    if (l.getId() != null && l.getId().equals(listingId)) {
                        listing = l;
                        break;
                    }
                }
                if (listing != null && listing.getItemId() != null) {
                    firebaseService.getItemById(listing.getItemId(), new FirebaseService.ItemCallback() {
                        @Override
                        public void onSuccess(ItemModel itemModel) {
                            item = itemModel;
                            mainHandler.post(() -> updateUI());
                        }
                        @Override
                        public void onError(String error) {
                            mainHandler.post(() -> showError());
                        }
                    });
                } else {
                    mainHandler.post(() -> showError());
                }
            }
            @Override
            public void onError(String error) {
                mainHandler.post(() -> showError());
            }
        });
    }

    private void updateUI() {
        if (item == null || listing == null) {
            showError();
            return;
        }
        tvItemTitle.setText(item.getTitle());
        String formattedPrice = formatPrice(listing.getPrice());
        tvItemPrice.setText(formattedPrice);
        if (tvDescription != null && item.getDescription() != null) {
            tvDescription.setText(item.getDescription());
        }
        if (tvCondition != null && item.getCondition() != null) {
            tvCondition.setText(item.getCondition());
        }
        if (tvItemLocation != null && item.getLocation() != null) {
            java.util.Map<String, Object> location = item.getLocation();
            if (location != null && location.get("address") != null) {
                tvItemLocation.setText(location.get("address").toString());
            } else {
                tvItemLocation.setText(""); // or a default value
            }
        }
        if (item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) {
            // Convert List<String> to List<Uri>
            List<Uri> uriList = new ArrayList<>();
            for (String uriStr : item.getPhotoUris()) {
                if (uriStr != null) {
                    uriList.add(Uri.parse(uriStr));
                }
            }
            ImageSliderAdapter imageAdapter = new ImageSliderAdapter(requireContext(), uriList) {
                @Override
                public void onBindViewHolder(@NonNull ImageSliderAdapter.ImageViewHolder holder, int position) {
                    ImageView imageView = holder.itemView.findViewById(R.id.image_view); // Ensure this ID matches your layout
                    Uri uri = uriList.get(position);
                    Glide.with(imageView.getContext())
                        .load(uri)
                        .placeholder(R.drawable.tradeuplogo)
                        .error(R.drawable.tradeuplogo)
                        .into(imageView);
                }
            };
            itemImagesViewPager.setAdapter(imageAdapter);
            new TabLayoutMediator(imageIndicator, itemImagesViewPager, (tab, position) -> {}).attach();
        } else {
            // Nếu không có ảnh, hiển thị ảnh mặc định
            List<Uri> defaultList = new ArrayList<>();
            defaultList.add(Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.drawable.ic_image_placeholder));
            ImageSliderAdapter imageAdapter = new ImageSliderAdapter(requireContext(), defaultList) {
                @Override
                public void onBindViewHolder(@NonNull ImageSliderAdapter.ImageViewHolder holder, int position) {
                    ImageView imageView = holder.itemView.findViewById(R.id.image_view); // Ensure this ID matches your layout
                    Glide.with(imageView.getContext())
                        .load(defaultList.get(position))
                        .placeholder(R.drawable.tradeuplogo)
                        .error(R.drawable.tradeuplogo)
                        .into(imageView);
                }
            };
            itemImagesViewPager.setAdapter(imageAdapter);
            new TabLayoutMediator(imageIndicator, itemImagesViewPager, (tab, position) -> {}).attach();
        }
        // Seller info
        if (listing.getSellerId() != null) {
            firebaseService.getUserById(listing.getSellerId(), new FirebaseService.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    if (getActivity() == null || !isAdded()) return;
                    tvSellerName.setText(user.getDisplayName());
                    String ratingText = String.format("%.1f (%d reviews)", user.getRating(), user.getTotalReviews());
                    tvSellerRating.setText(ratingText);
                    if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                        Glide.with(requireContext())
                            .load(user.getPhotoUrl())
                            .placeholder(R.drawable.tradeuplogo)
                            .error(R.drawable.tradeuplogo)
                            .into(ivSellerAvatar);
                    } else {
                        ivSellerAvatar.setImageResource(R.drawable.tradeuplogo);
                    }
                }
                @Override
                public void onError(String error) {
                    if (getActivity() == null || !isAdded()) return;
                    tvSellerName.setText("Unknown Seller");
                    tvSellerRating.setText("");
                    ivSellerAvatar.setImageResource(R.drawable.tradeuplogo);
                }
            });
        }
        // Show interaction counts from new structure
        if (listing.getInteractions() != null && listing.getInteractions().getAggregate() != null) {
            tvViewCount.setText(String.valueOf(listing.getInteractions().getAggregate().getTotalViews()));
            tvSaveCount.setText(String.valueOf(listing.getInteractions().getAggregate().getTotalSaves()));
            tvShareCount.setText(String.valueOf(listing.getInteractions().getAggregate().getTotalShares()));
        } else {
            tvViewCount.setText("0");
            tvSaveCount.setText("0");
            tvShareCount.setText("0");
        }
        // Set transaction status to tvItemStatus
        if (tvItemStatus != null) {
            String status = null;
            try {
                java.lang.reflect.Method getStatusMethod = listing.getClass().getMethod("getTransactionStatus");
                status = (String) getStatusMethod.invoke(listing);
            } catch (Exception e) {
                status = null;
            }
            if (status != null) {
                // Capitalize first letter
                String displayStatus = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
                tvItemStatus.setText(displayStatus);
                // Optionally, set color based on status
                int colorRes;
                switch (status.toLowerCase()) {
                    case "available":
                        colorRes = R.color.md_theme_onSuccess;
                        break;
                    case "pending":
                        colorRes = R.color.md_theme_onSecondary;
                        break;
                    case "sold":
                        colorRes = R.color.md_theme_onError;
                        break;
                    default:
                        colorRes = R.color.md_theme_onSurfaceVariant;
                        break;
                }
                tvItemStatus.setTextColor(getResources().getColor(colorRes, null));
            } else {
                tvItemStatus.setText("");
            }
        }
        // Show/hide Buy Now button based on listing availability
        if (btnBuyNow != null) {
            if (isListingAvailableForPurchase(listing)) {
                btnBuyNow.setVisibility(View.VISIBLE);
            } else {
                btnBuyNow.setVisibility(View.GONE);
            }
        }
        // Hide Make Offer button if not allowed
        if (btnMakeOffer != null) {
            boolean allowOffers = false;
            try {
                java.lang.reflect.Method getAllowOffersMethod = listing.getClass().getMethod("getAllowOffers");
                allowOffers = (boolean) getAllowOffersMethod.invoke(listing);
            } catch (Exception e) {
                // fallback: check field directly if public
                try {
                    java.lang.reflect.Field allowOffersField = listing.getClass().getField("allowOffers");
                    allowOffers = allowOffersField.getBoolean(listing);
                } catch (Exception ignored) {}
            }
            if (!allowOffers) {
                btnMakeOffer.setVisibility(View.GONE);
            } else {
                btnMakeOffer.setVisibility(View.VISIBLE);
            }
        }
        // Tags
        if (tagGroup != null) {
            tagGroup.removeAllViews();
            List<String> tags = null;
            if (listing != null && listing.getTags() != null) {
                tags = listing.getTags();
            }
            if (tags != null && !tags.isEmpty()) {
                for (String tag : tags) {
                    Chip chip = new Chip(requireContext());
                    chip.setText(tag);
                    chip.setChipBackgroundColorResource(R.color.md_theme_surfaceVariant);
                    chip.setTextColor(getResources().getColor(R.color.md_theme_onSurfaceVariant, null));
                    chip.setChipStrokeColorResource(R.color.md_theme_outline);
                    chip.setChipStrokeWidth(1f);
                    tagGroup.addView(chip);
                }
                tagGroup.setVisibility(View.VISIBLE);
            } else {
                tagGroup.setVisibility(View.GONE);
            }
        }
        // Show owner or buyer buttons
        showOwnerOrBuyerButtons();
        setupButtonClickListeners();
    }

    /**
     * Determines if the listing is available for purchase.
     * You can adjust the logic as needed (e.g., check for sold/reserved status).
     */
    private boolean isListingAvailableForPurchase(ListingModel listing) {
        // Example: assuming ListingModel has isSold() and isReserved() methods
        // If not, adjust this logic to fit your model
        try {
            java.lang.reflect.Method isSoldMethod = listing.getClass().getMethod("isSold");
            java.lang.reflect.Method isReservedMethod = listing.getClass().getMethod("isReserved");
            boolean isSold = (boolean) isSoldMethod.invoke(listing);
            boolean isReserved = (boolean) isReservedMethod.invoke(listing);
            return !isSold && !isReserved;
        } catch (Exception e) {
            // If methods do not exist, default to always available
            return true;
        }
    }

    private String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%,.0f đ", price);
    }

    private void showError() {
        Toast.makeText(getActivity(), "Error loading listing details", Toast.LENGTH_SHORT).show();
        if (getView() != null) getView().setVisibility(View.GONE);
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        tvItemTitle = view.findViewById(R.id.tv_item_title);
        tvItemPrice = view.findViewById(R.id.tv_item_price);
        tvDescription = view.findViewById(R.id.tv_description);
        tvCondition = view.findViewById(R.id.chip_condition);
        tvItemLocation = view.findViewById(R.id.tv_item_location);
        itemImagesViewPager = view.findViewById(R.id.item_images_viewpager);
        imageIndicator = view.findViewById(R.id.image_indicator);
        btnBuyNow = view.findViewById(R.id.btn_buy_now);
        btnMakeOffer = view.findViewById(R.id.btn_make_offer);
        btnMessage = view.findViewById(R.id.btn_message);
        btnViewOffers = view.findViewById(R.id.btn_view_offers);
        btnUpdateListing = view.findViewById(R.id.btn_update_listing);
        sellerContainer = view.findViewById(R.id.seller_container);
        ivSellerAvatar = view.findViewById(R.id.iv_seller_avatar);
        ivStar = view.findViewById(R.id.iv_star);
        tvSellerName = view.findViewById(R.id.tv_seller_name);
        tvSellerRating = view.findViewById(R.id.tv_seller_rating);
        tvViewCount = view.findViewById(R.id.tv_view_count);
        tvSaveCount = view.findViewById(R.id.tv_save_count);
        tvShareCount = view.findViewById(R.id.tv_share_count);
        tvItemStatus = view.findViewById(R.id.tv_item_status);
        tagGroup = view.findViewById(R.id.tag_group);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        }
    }

    private void showOwnerOrBuyerButtons() {
        if (listing == null || firebaseService == null) return;
        String currentUserId = firebaseService.getCurrentUserId();
        boolean isOwner = currentUserId != null && currentUserId.equals(listing.getSellerId());
        if (isOwner) {
            // Show owner buttons, hide buyer buttons
            if (btnViewOffers != null) btnViewOffers.setVisibility(View.VISIBLE);
            if (btnUpdateListing != null) btnUpdateListing.setVisibility(View.VISIBLE);
            if (btnMakeOffer != null) btnMakeOffer.setVisibility(View.GONE);
            if (btnMessage != null) btnMessage.setVisibility(View.GONE);
            if (btnBuyNow != null) btnBuyNow.setVisibility(View.GONE);
        } else {
            // Show buyer buttons, hide owner buttons
            if (btnViewOffers != null) btnViewOffers.setVisibility(View.GONE);
            if (btnUpdateListing != null) btnUpdateListing.setVisibility(View.GONE);
            if (btnMakeOffer != null) btnMakeOffer.setVisibility(View.VISIBLE);
            if (btnMessage != null) btnMessage.setVisibility(View.VISIBLE);
            // btnBuyNow visibility handled by availability
        }
    }

    private void setupButtonClickListeners() {
        if (btnBuyNow != null) {
            btnBuyNow.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Buy Now clicked", Toast.LENGTH_SHORT).show();
                // TODO: Implement actual buy logic here
            });
        }
        if (sellerContainer != null && listing != null && listing.getSellerId() != null) {
            sellerContainer.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("userId", listing.getSellerId());
                androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(requireView());
                navController.navigate(R.id.action_itemDetailFragment_to_publicProfileFragment, args);
            });
        }
        if (btnViewOffers != null) {
            btnViewOffers.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "View Offers clicked", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to offers list for this listing
            });
        }
        if (btnUpdateListing != null) {
            btnUpdateListing.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Update Listing clicked", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to update listing screen
            });
        }
    }
}
