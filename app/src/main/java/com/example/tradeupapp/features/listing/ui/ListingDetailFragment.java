package com.example.tradeupapp.features.listing.ui;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.tradeupapp.R;
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.core.services.CartService;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.models.User;
import com.example.tradeupapp.models.CartItem;
import com.example.tradeupapp.models.OfferModel;
import com.example.tradeupapp.shared.adapters.ImageSliderAdapter;
import com.example.tradeupapp.shared.adapters.OfferAdapter;
import com.example.tradeupapp.shared.adapters.ReviewAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class ListingDetailFragment extends Fragment {
    private ListingModel listing;
    private ItemModel item;
    private Toolbar toolbar;
    private TextView tvItemTitle, tvItemPrice, tvDescription, tvCondition, tvItemLocation;
    private ViewPager2 itemImagesViewPager;
    private com.google.android.material.tabs.TabLayout imageIndicator;
    private com.google.android.material.button.MaterialButton btnBuyNow, btnMakeOffer, btnMessage, btnAnalyticsOffers, btnUpdateListing;
    private FirebaseService firebaseService;
    private CartService cartService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private View sellerContainer;
    private ImageView ivSellerAvatar, ivStar;
    private TextView tvSellerName, tvSellerRating;
    private TextView tvViewCount, tvSaveCount, tvShareCount;
    private TextView tvItemStatus;
    private com.google.android.material.chip.ChipGroup tagGroup;
    private RecyclerView rvOffers;
    private OfferAdapter offerAdapter;
    private List<OfferModel> offerList = new ArrayList<>();
    private Map<String, ListingModel> listingMap = new HashMap<>();
    private Map<String, ItemModel> itemMap = new HashMap<>();
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private List<com.example.tradeupapp.models.ReviewModel> reviewList = new ArrayList<>();
    private TextView tvNoReviews;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseService = FirebaseService.getInstance();
        cartService = CartService.getInstance();
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
        View view = inflater.inflate(R.layout.fragment_item_detail, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        itemImagesViewPager.setAdapter(new ImageSliderAdapter(requireContext(), new ArrayList<>()));
        new com.google.android.material.tabs.TabLayoutMediator(imageIndicator, itemImagesViewPager, (tab, position) -> {}).attach();
        setupToolbar();
        rvOffers.setLayoutManager(new LinearLayoutManager(getContext()));
        offerAdapter = new OfferAdapter(getContext(), offerList, listingMap, itemMap, new OfferAdapter.OnOfferActionListener() {
            @Override
            public void onViewDetail(ListingModel listing) {
                // Implement view detail logic
            }
            @Override
            public void onAccept(OfferModel offer, ListingModel listing) {
                // Implement accept logic
            }
            @Override
            public void onReject(OfferModel offer, ListingModel listing) {
                // Implement reject logic
            }
            @Override
            public void onCounter(OfferModel offer, ListingModel listing) {
                // No counter-offer logic for buyers in this screen. (Required by interface)
            }
            @Override
            public void onMakeOffer(OfferModel offer, ListingModel listing) {
                showMakeOfferDialog();
            }
        });
        rvOffers.setAdapter(offerAdapter);
        loadOffersForListing();
        // --- Review section setup ---
        rvReviews = view.findViewById(R.id.rv_reviews); // Add this RecyclerView to your fragment_item_detail.xml
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        reviewAdapter = new ReviewAdapter(getContext(), reviewList);
        rvReviews.setAdapter(reviewAdapter);
        // Load reviewer user info for displaying displayName in reviews
        loadReviewerUsersForReviews();
    }

    private void loadReviewerUsersForReviews() {
        // Collect all unique reviewerIds from reviewList
        java.util.Set<String> reviewerIds = new java.util.HashSet<>();
        for (com.example.tradeupapp.models.ReviewModel review : reviewList) {
            if (review.getReviewerId() != null) {
                reviewerIds.add(review.getReviewerId());
            }
        }
        if (reviewerIds.isEmpty()) {
            reviewAdapter.setUserMap(new java.util.HashMap<>());
            return;
        }
        firebaseService.getUsersByIds(new java.util.ArrayList<>(reviewerIds), new FirebaseService.UsersByIdsCallback() {
            @Override
            public void onSuccess(java.util.Map<String, com.example.tradeupapp.models.User> userMap) {
                reviewAdapter.setUserMap(userMap);
            }
            @Override
            public void onError(String error) {
                reviewAdapter.setUserMap(new java.util.HashMap<>());
            }
        });
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
            new com.google.android.material.tabs.TabLayoutMediator(imageIndicator, itemImagesViewPager, (tab, position) -> {}).attach();
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
            new com.google.android.material.tabs.TabLayoutMediator(imageIndicator, itemImagesViewPager, (tab, position) -> {}).attach();
        }
        // Seller info
        if (listing.getSellerId() != null) {
            firebaseService.getReviewsByUserId(listing.getSellerId(), new FirebaseService.ReviewsCallback() {
                @Override
                public void onSuccess(List<com.example.tradeupapp.models.ReviewModel> reviews) {
                    double totalRating = 0;
                    int reviewCount = reviews.size();
                    for (com.example.tradeupapp.models.ReviewModel review : reviews) {
                        totalRating += review.getRating();
                    }
                    double avgRating = reviewCount > 0 ? totalRating / reviewCount : 0.0;
                    firebaseService.getUserById(listing.getSellerId(), new FirebaseService.UserCallback() {
                        @Override
                        public void onSuccess(User user) {
                            if (getActivity() == null || !isAdded()) return;
                            tvSellerName.setText(user.getDisplayName());
                            String ratingText = reviewCount > 0 ? String.format("%.1f (%d reviews)", avgRating, reviewCount) : "No rating";
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
                            // Handle error if needed
                        }
                    });
                }
                @Override
                public void onError(String error) {
                    // Handle error if needed
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
        // Show/hide Buy Now and Make Offer buttons based on listing availability and allowOffers
        boolean allowOffers = listing != null && listing.getAllowOffers();
        String transactionStatus = listing != null ? listing.getTransactionStatus() : null;
        boolean isAvailable = "available".equalsIgnoreCase(transactionStatus);
        if (btnBuyNow != null) {
            btnBuyNow.setEnabled(isAvailable);
            btnBuyNow.setVisibility(isAvailable ? View.VISIBLE : View.GONE);
        }
        if (btnMakeOffer != null) {
            // Chỉ hiện nút nếu available và allowOffers đều true, ngược lại ẩn
            if (isAvailable && allowOffers && !isCurrentUserSeller()) {
                btnMakeOffer.setVisibility(View.VISIBLE);
                btnMakeOffer.setEnabled(true);
            } else {
                btnMakeOffer.setVisibility(View.GONE);
            }
        }
        // Always show chat button
        if (btnMessage != null) {
            btnMessage.setVisibility(View.VISIBLE);
        }
        // Hide Make Offer button if not allowed or user is seller
        if (btnMakeOffer != null) {
            if (!allowOffers || isCurrentUserSeller()) {
                btnMakeOffer.setVisibility(View.GONE);
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
                    com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(requireContext());
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
        // Load reviews for this listing after UI is updated
        loadReviewsForListing();
    }

    // Utility methods below, outside of updateUI()
    private boolean isListingAvailableForPurchase(ListingModel listing) {
        try {
            java.lang.reflect.Method isSoldMethod = listing.getClass().getMethod("isSold");
            java.lang.reflect.Method isReservedMethod = listing.getClass().getMethod("isReserved");
            boolean isSold = (boolean) isSoldMethod.invoke(listing);
            boolean isReserved = (boolean) isReservedMethod.invoke(listing);
            return !isSold && !isReserved;
        } catch (Exception e) {
            return true;
        }
    }
    private boolean isCurrentUserSeller() {
        String currentUserId = firebaseService.getCurrentUserId();
        return currentUserId != null && listing != null && currentUserId.equals(listing.getSellerId());
    }
    private String formatPrice(double price) {
        return String.format(java.util.Locale.getDefault(), "%,.0f đ", price);
    }
    private void showError() {
        android.widget.Toast.makeText(getActivity(), "Error loading listing details", android.widget.Toast.LENGTH_SHORT).show();
        if (getView() != null) getView().setVisibility(android.view.View.GONE);
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
        btnAnalyticsOffers = view.findViewById(R.id.btn_analytics_offers);
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
        rvOffers = view.findViewById(R.id.rv_offers);
        rvReviews = view.findViewById(R.id.rv_reviews);
        tvNoReviews = view.findViewById(R.id.tv_no_reviews);
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
            if (btnAnalyticsOffers != null) btnAnalyticsOffers.setVisibility(View.VISIBLE);
            if (btnUpdateListing != null) btnUpdateListing.setVisibility(View.VISIBLE);
            if (btnMakeOffer != null) btnMakeOffer.setVisibility(View.GONE);
            if (btnMessage != null) btnMessage.setVisibility(View.GONE);
            if (btnBuyNow != null) btnBuyNow.setVisibility(View.GONE);
        } else {
            // Show buyer buttons, hide owner buttons
            if (btnAnalyticsOffers != null) btnAnalyticsOffers.setVisibility(View.GONE);
            if (btnUpdateListing != null) btnUpdateListing.setVisibility(View.GONE);
            if (btnMakeOffer != null) btnMakeOffer.setVisibility(View.VISIBLE);
            if (btnMessage != null) btnMessage.setVisibility(View.VISIBLE);
            if (btnBuyNow != null) btnBuyNow.setVisibility(View.VISIBLE);
        }
    }

    private void setupButtonClickListeners() {
        if (btnBuyNow != null) {
            btnBuyNow.setOnClickListener(v -> {
                if (listing == null) {
                    Toast.makeText(requireContext(), "Listing not loaded", Toast.LENGTH_SHORT).show();
                    return;
                }
                String userId = firebaseService.getCurrentUserId();
                if (userId == null) {
                    Toast.makeText(requireContext(), "You must be logged in to buy", Toast.LENGTH_SHORT).show();
                    return;
                }
                CartItem cartItem = new CartItem(listing.getId());
                cartService.addToCart(userId, cartItem, new CartService.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(requireContext(), "Added to cart!", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(requireContext(), "Failed to add to cart: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
        if (btnMakeOffer != null) {
            btnMakeOffer.setOnClickListener(v -> showMakeOfferDialog());
        }
        if (sellerContainer != null && listing != null && listing.getSellerId() != null) {
            sellerContainer.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("userId", listing.getSellerId());
                androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(requireView());
                navController.navigate(R.id.action_itemDetailFragment_to_publicProfileFragment, args);
            });
        }
        if (btnAnalyticsOffers != null) {
            btnAnalyticsOffers.setOnClickListener(v -> {
                showAnalyticsOffersDialog();
            });
        }
        if (btnUpdateListing != null) {
            btnUpdateListing.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Update Listing clicked", Toast.LENGTH_SHORT).show();
                // Pass ListingModel and ItemModel directly, like ManageListingsFragment
                Bundle args = new Bundle();
                if (listing != null) {
                    args.putSerializable("listing", listing);
                }
                if (item != null) {
                    args.putParcelable("item", item);
                }
                androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(requireView());
                navController.navigate(R.id.action_itemDetailFragment_to_updateItemFragment, args);
            });
        }
        if (btnMessage != null && listing != null) {
            btnMessage.setOnClickListener(v -> {
                String currentUserId = firebaseService.getCurrentUserId();
                String sellerId = listing.getSellerId();
                String itemId = listing.getItemId();
                // Add debug logging for parameters
                android.util.Log.d("ListingDetailFragment", "btnMessage clicked: currentUserId=" + currentUserId + ", sellerId=" + sellerId + ", itemId=" + itemId);
                if (currentUserId == null || currentUserId.isEmpty() || sellerId == null || sellerId.isEmpty() || itemId == null || itemId.isEmpty()) {
                    Toast.makeText(requireContext(), "Missing user or item info", Toast.LENGTH_SHORT).show();
                    android.util.Log.e("ListingDetailFragment", "Missing user or item info: currentUserId=" + currentUserId + ", sellerId=" + sellerId + ", itemId=" + itemId);
                    return;
                }
                // Use FirebaseService helper to get or create chat
                firebaseService.getOrCreateChat(currentUserId, sellerId, itemId, new FirebaseService.ChatIdCallback() {
                    @Override
                    public void onSuccess(String chatId) {
                        Bundle args = new Bundle();
                        args.putString("chatId", chatId);
                        args.putString("otherUserId", sellerId);
                        args.putString("listingId", listing.getId());
                        args.putString("itemId", itemId);
                        args.putString("itemName", item != null ? item.getTitle() : "");
                        args.putString("itemImageUrl", (item != null && item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) ? item.getPhotoUris().get(0) : null);
                        args.putString("itemPrice", listing.getPrice() > 0 ? formatPrice(listing.getPrice()) : null);
                        // Optionally pass seller display name if available
                        // You may want to fetch seller name here if needed
                        androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(requireView());
                        navController.navigate(R.id.action_itemDetailFragment_to_chatFragment, args);
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(requireContext(), "Failed to get or create chat: " + error, Toast.LENGTH_SHORT).show();
                        android.util.Log.e("ListingDetailFragment", "Failed to get or create chat: " + error + " | Params: currentUserId=" + currentUserId + ", sellerId=" + sellerId + ", itemId=" + itemId);
                    }
                });
            });
        }
    }

    private void showMakeOfferDialog() {
        if (listing == null || item == null) {
            Toast.makeText(requireContext(), "Listing not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        // Check if listing allows offers (giá thương lượng)
        boolean allowOffers = false;
        double price = 0;
        try {
            java.lang.reflect.Method getAllowOffersMethod = listing.getClass().getMethod("getAllowOffers");
            Object result = getAllowOffersMethod.invoke(listing);
            if (result instanceof Boolean) {
                allowOffers = (Boolean) result;
            } else if (result != null) {
                allowOffers = Boolean.parseBoolean(result.toString());
            }
        } catch (Exception e) {
            // Fallback to field access
            try {
                java.lang.reflect.Field allowOffersField = listing.getClass().getField("allowOffers");
                Object fieldValue = allowOffersField.get(listing);
                if (fieldValue instanceof Boolean) {
                    allowOffers = (Boolean) fieldValue;
                } else if (fieldValue != null) {
                    allowOffers = Boolean.parseBoolean(fieldValue.toString());
                }
            } catch (Exception ignored) {}
        }
        try {
            java.lang.reflect.Method getPriceMethod = listing.getClass().getMethod("getPrice");
            price = (double) getPriceMethod.invoke(listing);
        } catch (Exception e) {
            try {
                java.lang.reflect.Field priceField = listing.getClass().getField("price");
                price = priceField.getDouble(listing);
            } catch (Exception ignored) {}
        }
        if (!allowOffers || price <= 0) {
            Toast.makeText(requireContext(), "This listing does not accept offers.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isCurrentUserSeller()) {
            Toast.makeText(requireContext(), "You cannot make an offer on your own listing", Toast.LENGTH_SHORT).show();
            return;
        }
        String buyerId = firebaseService.getCurrentUserId();
        if (buyerId == null) {
            Toast.makeText(requireContext(), "You must be logged in to make an offer", Toast.LENGTH_SHORT).show();
            return;
        }
        // Prevent duplicate pending offers
        firebaseService.getOffersByBuyerId(buyerId, new FirebaseService.OffersCallback() {
            @Override
            public void onSuccess(List<OfferModel> offers) {
                for (OfferModel offer : offers) {
                    if (offer.getListingId().equals(listing.getId()) && "pending".equalsIgnoreCase(offer.getStatus())) {
                        Toast.makeText(requireContext(), "You already have a pending offer for this listing", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                // Show dialog if no pending offer
                showMakeOfferDialogInternal();
            }
            @Override
            public void onError(String error) {
                showMakeOfferDialogInternal();
            }
        });
    }

    private void showMakeOfferDialogInternal() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_make_offer, null);
        EditText etAmount = dialogView.findViewById(R.id.et_offer_amount);
        EditText etMessage = dialogView.findViewById(R.id.et_offer_message);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();
        dialogView.findViewById(R.id.btn_cancel_offer).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_submit_offer).setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            String message = etMessage.getText().toString().trim();
            if (amountStr.isEmpty()) {
                etAmount.setError("Enter offer amount");
                return;
            }
            double offerAmount;
            try {
                offerAmount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                etAmount.setError("Invalid amount");
                return;
            }
            double minOffer = 0.5 * listing.getPrice();
            double maxOffer = listing.getPrice();
            if (offerAmount < minOffer) {
                etAmount.setError("Offer must be at least 50% of the product price");
                return;
            }
            if (offerAmount > maxOffer) {
                etAmount.setError("Offer cannot exceed the product price");
                return;
            }
            if (offerAmount <= 0) {
                etAmount.setError("Amount must be positive");
                return;
            }
            String buyerId = firebaseService.getCurrentUserId();
            java.util.Date now = new java.util.Date();
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(now);
            cal.add(java.util.Calendar.DATE, 2); // expires in 2 days
            java.util.Date expiresAt = cal.getTime();
            OfferModel offer = new OfferModel();
            offer.setListingId(listing.getId());
            offer.setSellerId(listing.getSellerId());
            offer.setBuyerId(buyerId);
            offer.setOfferAmount(offerAmount);
            offer.setMessage(message);
            offer.setStatus(OfferModel.Status.PENDING.getValue());
            offer.setCreatedAt(now);
            offer.setExpiresAt(expiresAt);
            offer.setRespondedAt(null);
            offer.setCounterOffer(null);
            firebaseService.createOffer(offer, new FirebaseService.SimpleCallback() {
                @Override
                public void onSuccess() {
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Offer sent!", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onError(String error) {
                    Toast.makeText(requireContext(), "Failed to send offer: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show();
    }

    private void showOffersDialog() {
        if (listing == null) {
            Toast.makeText(requireContext(), "Listing not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_offers_list, null);
        RecyclerView rvOffers = dialogView.findViewById(R.id.rv_offers);
        rvOffers.setLayoutManager(new LinearLayoutManager(requireContext()));
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();
        dialogView.findViewById(R.id.btn_close_offers).setOnClickListener(v -> dialog.dismiss());
        // Prepare maps like mystore
        Map<String, ListingModel> offerListingMap = new HashMap<>();
        Map<String, ItemModel> offerItemMap = new HashMap<>();
        offerListingMap.put(listing.getId(), listing);
        if (item != null) offerItemMap.put(item.getId(), item);
        // Load offers for this listing
        firebaseService.getOffersByListingIds(java.util.Collections.singletonList(listing.getId()), new FirebaseService.OffersCallback() {
            @Override
            public void onSuccess(List<OfferModel> offers) {
                OfferAdapter adapter = new OfferAdapter(requireContext(), offers, offerListingMap, offerItemMap, new OfferAdapter.OnOfferActionListener() {
                    @Override
                    public void onViewDetail(ListingModel listing) {
                        // Xem chi tiết sản phẩm (optional: navigate to detail)
                    }
                    @Override
                    public void onAccept(OfferModel offer, ListingModel listing) {
                        offer.setStatus(OfferModel.Status.ACCEPTED.getValue());
                        offer.setRespondedAt(new java.util.Date());
                        firebaseService.updateOffer(offer, new FirebaseService.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(requireContext(), "Offer accepted!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                            @Override
                            public void onError(String error) {
                                Toast.makeText(requireContext(), "Failed: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onReject(OfferModel offer, ListingModel listing) {
                        offer.setStatus(OfferModel.Status.DECLINED.getValue());
                        offer.setRespondedAt(new java.util.Date());
                        firebaseService.updateOffer(offer, new FirebaseService.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(requireContext(), "Offer rejected!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                            @Override
                            public void onError(String error) {
                                Toast.makeText(requireContext(), "Failed: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onCounter(OfferModel offer, ListingModel listing) {
                        // No counter-offer logic for buyers in this screen. (Required by interface)
                    }
                    @Override
                    public void onMakeOffer(OfferModel offer, ListingModel listing) {
                        // No action needed in this dialog
                    }
                });
                rvOffers.setAdapter(adapter);
            }
            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Failed to load offers: " + error, Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    private void showCounterOfferDialog(OfferModel offer, AlertDialog parentDialog) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_make_offer, null);
        EditText etAmount = dialogView.findViewById(R.id.et_offer_amount);
        EditText etMessage = dialogView.findViewById(R.id.et_offer_message);
        etAmount.setText(offer.getOfferAmount() + "");
        etMessage.setText(offer.getMessage() != null ? offer.getMessage() : "");
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();
        dialogView.findViewById(R.id.btn_cancel_offer).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_submit_offer).setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            String message = etMessage.getText().toString().trim();
            double counterAmount;
            try {
                counterAmount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                etAmount.setError("Invalid amount");
                return;
            }
            if (counterAmount <= 0) {
                etAmount.setError("Amount must be positive");
                return;
            }
            offer.setStatus(OfferModel.Status.COUNTER_OFFERED.getValue());
            offer.setCounterOffer(counterAmount);
            offer.setRespondedAt(new java.util.Date());
            offer.setMessage(message);
            firebaseService.updateOffer(offer, new FirebaseService.SimpleCallback() {
                @Override
                public void onSuccess() {
                    dialog.dismiss();
                    parentDialog.dismiss();
                    Toast.makeText(requireContext(), "Counter offer sent!", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onError(String error) {
                    Toast.makeText(requireContext(), "Failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show();
    }

    private void loadOffersForListing() {
        if (listing == null) return;
        // Clear old data
        offerList.clear();
        listingMap.clear();
        itemMap.clear();
        // Add current listing and item to map
        listingMap.put(listing.getId(), listing);
        if (item != null) itemMap.put(item.getId(), item);
        // Load offers for this listing
        firebaseService.getOffersByListingIds(java.util.Collections.singletonList(listing.getId()), new FirebaseService.OffersCallback() {
            @Override
            public void onSuccess(List<OfferModel> offers) {
                offerList.clear();
                offerList.addAll(offers);
                offerAdapter.notifyDataSetChanged();
            }
            @Override
            public void onError(String error) {
                offerList.clear();
                offerAdapter.notifyDataSetChanged();
            }
        });
    }

    private void loadReviewsForListing() {
        if (listing == null || listing.getId() == null) return;
        android.util.Log.d("ListingDetailFragment", "Fetching reviews for listingId: " + listing.getId());
        firebaseService.getReviewsByListingId(listing.getId(), new FirebaseService.ReviewsCallback() {
            @Override
            public void onSuccess(List<com.example.tradeupapp.models.ReviewModel> reviews) {
                android.util.Log.d("ListingDetailFragment", "Reviews fetched: " + reviews.size());
                reviewList.clear();
                // Only add verified reviews
                for (com.example.tradeupapp.models.ReviewModel review : reviews) {
                    if (review.isVerified()) {
                        reviewList.add(review);
                    }
                }
                mainHandler.post(() -> {
                    reviewAdapter.notifyDataSetChanged();
                    if (tvNoReviews != null) {
                        tvNoReviews.setVisibility(reviewList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    // After loading reviews, load user info for reviewer display names
                    loadReviewerUsersForReviews();
                });
            }
            @Override
            public void onError(String error) {
                android.util.Log.e("ListingDetailFragment", "Error fetching reviews: " + error);
                mainHandler.post(() -> {
                    if (tvNoReviews != null) {
                        tvNoReviews.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }


    private void showAnalyticsOffersDialog() {
        if (listing == null || item == null) {
            Toast.makeText(requireContext(), "Listing not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_listing_analytics_offers, null);
        TextView tvViews = dialogView.findViewById(R.id.tv_analytics_views);
        TextView tvSaves = dialogView.findViewById(R.id.tv_analytics_saves);
        TextView tvShares = dialogView.findViewById(R.id.tv_analytics_shares);
        TextView tvChats = dialogView.findViewById(R.id.tv_analytics_chats);
        TextView tvOffers = dialogView.findViewById(R.id.tv_analytics_offers);
        RecyclerView rvOffers = dialogView.findViewById(R.id.rv_offers);
        rvOffers.setLayoutManager(new LinearLayoutManager(requireContext()));
        AlertDialog analyticsOffersDialog = new AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create();
        dialogView.findViewById(R.id.btn_close_analytics_offers).setOnClickListener(v -> analyticsOffersDialog.dismiss());
        analyticsOffersDialog.show();
        // Analytics data
        if (listing.getInteractions() != null && listing.getInteractions().getAggregate() != null) {
            tvViews.setText(String.valueOf(listing.getInteractions().getAggregate().getTotalViews()));
            tvSaves.setText(String.valueOf(listing.getInteractions().getAggregate().getTotalSaves()));
            tvShares.setText(String.valueOf(listing.getInteractions().getAggregate().getTotalShares()));
        } else {
            tvViews.setText("0");
            tvSaves.setText("0");
            tvShares.setText("0");
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("chats")
            .whereEqualTo("relatedItemId", item.getId())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                tvChats.setText(String.valueOf(querySnapshot.size()));
            })
            .addOnFailureListener(e -> {
                tvChats.setText("0");
            });
        db.collection("offers")
            .whereEqualTo("listingId", listing.getId())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                tvOffers.setText(String.valueOf(querySnapshot.size()));
            })
            .addOnFailureListener(e -> {
                tvOffers.setText("0");
            });
        // Offers list
        Map<String, ListingModel> offerListingMap = new HashMap<>();
        Map<String, ItemModel> offerItemMap = new HashMap<>();
        offerListingMap.put(listing.getId(), listing);
        if (item != null) offerItemMap.put(item.getId(), item);
        db.collection("offers")
            .whereEqualTo("listingId", listing.getId())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<OfferModel> offers = new ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                    OfferModel offer = doc.toObject(OfferModel.class);
                    offers.add(offer);
                }
                OfferAdapter adapter = new OfferAdapter(requireContext(), offers, offerListingMap, offerItemMap, new OfferAdapter.OnOfferActionListener() {
                    @Override
                    public void onViewDetail(ListingModel listing) {}
                    @Override
                    public void onAccept(OfferModel offer, ListingModel listing) {
                        offer.setStatus(OfferModel.Status.ACCEPTED.getValue());
                        offer.setRespondedAt(new java.util.Date());
                        firebaseService.updateOffer(offer, new FirebaseService.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(requireContext(), "Offer accepted!", Toast.LENGTH_SHORT).show();
                                analyticsOffersDialog.dismiss();
                            }
                            @Override
                            public void onError(String error) {
                                Toast.makeText(requireContext(), "Failed: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onReject(OfferModel offer, ListingModel listing) {
                        offer.setStatus(OfferModel.Status.DECLINED.getValue());
                        offer.setRespondedAt(new java.util.Date());
                        firebaseService.updateOffer(offer, new FirebaseService.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(requireContext(), "Offer rejected!", Toast.LENGTH_SHORT).show();
                                analyticsOffersDialog.dismiss();
                            }
                            @Override
                            public void onError(String error) {
                                Toast.makeText(requireContext(), "Failed: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onCounter(OfferModel offer, ListingModel listing) {}
                    @Override
                    public void onMakeOffer(OfferModel offer, ListingModel listing) {
                        // No action needed in analytics dialog
                    }
                });
                rvOffers.setAdapter(adapter);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to load offers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
