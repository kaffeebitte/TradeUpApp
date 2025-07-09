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
    private MaterialButton btnBuyNow, btnMakeOffer, btnMessage;
    private FirebaseService firebaseService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private View sellerContainer;
    private ImageView ivSellerAvatar, ivStar;
    private TextView tvSellerName, tvSellerRating;

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
            tvItemLocation.setText(item.getLocation());
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
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
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
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
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
                            .placeholder(R.drawable.ic_person_24)
                            .error(R.drawable.ic_person_24)
                            .into(ivSellerAvatar);
                    } else {
                        ivSellerAvatar.setImageResource(R.drawable.ic_person_24);
                    }
                }
                @Override
                public void onError(String error) {
                    if (getActivity() == null || !isAdded()) return;
                    tvSellerName.setText("Unknown Seller");
                    tvSellerRating.setText("");
                    ivSellerAvatar.setImageResource(R.drawable.ic_person_24);
                }
            });
        }
        setupButtonClickListeners();
    }

    private String formatPrice(double price) {
        try {
            Currency currency = Currency.getInstance(Locale.getDefault());
            return String.format(Locale.getDefault(), "%s%.2f", currency.getSymbol(), price);
        } catch (Exception e) {
            return String.format(Locale.getDefault(), "$%.2f", price);
        }
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
        sellerContainer = view.findViewById(R.id.seller_container);
        ivSellerAvatar = view.findViewById(R.id.iv_seller_avatar);
        ivStar = view.findViewById(R.id.iv_star);
        tvSellerName = view.findViewById(R.id.tv_seller_name);
        tvSellerRating = view.findViewById(R.id.tv_seller_rating);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        }
    }

    private void setupButtonClickListeners() {
        // ...existing code for button click listeners...
        if (sellerContainer != null && listing != null && listing.getSellerId() != null) {
            sellerContainer.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("userId", listing.getSellerId());
                androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(requireView());
                navController.navigate(R.id.action_itemDetailFragment_to_publicProfileFragment, args);
            });
        }
    }
}
