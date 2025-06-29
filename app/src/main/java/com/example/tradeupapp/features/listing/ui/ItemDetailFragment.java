package com.example.tradeupapp.features.listing.ui;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tradeupapp.R;
import com.example.tradeupapp.features.listing.adapter.ImageSliderAdapter;
import com.example.tradeupapp.features.listing.viewmodel.ItemDetailViewModel;
import com.example.tradeupapp.models.ItemModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

public class ItemDetailFragment extends Fragment {

    private ItemModel item;
    private Toolbar toolbar;
    private TextView itemTitle, itemPrice, itemDescription, itemCondition, itemLocation;
    private ViewPager2 imageViewPager;
    private TabLayout tabLayoutIndicators;
    private MaterialButton btnBuyNow, btnMakeOffer, btnMessage;
    private ItemDetailViewModel viewModel;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ItemDetailViewModel.class);

        // Get item from arguments
        if (getArguments() != null && getArguments().containsKey("item")) {
            item = getArguments().getParcelable("item");
            viewModel.setItem(item);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Set empty adapter immediately to avoid "No adapter attached" errors
        imageViewPager.setAdapter(new ImageSliderAdapter(requireContext(), new ArrayList<>()));
        new TabLayoutMediator(tabLayoutIndicators, imageViewPager, (tab, position) -> {}).attach();

        // Setup toolbar with back navigation
        setupToolbar();

        // Observe item data from ViewModel
        observeItemData();
    }

    private void observeItemData() {
        viewModel.getItemLiveData().observe(getViewLifecycleOwner(), itemModel -> {
            if (itemModel != null) {
                item = itemModel;
                updateUI();
            } else if (isUsingDummyData()) {
                setupDummyData();
                updateUI();
            } else {
                showError();
            }
        });
    }

    private void updateUI() {
        // Set item details in views
        itemTitle.setText(item.getTitle());

        // Format currency properly
        String formattedPrice = formatPrice(item.getPrice());
        itemPrice.setText(formattedPrice);

        if (itemDescription != null && item.getDescription() != null) {
            itemDescription.setText(item.getDescription());
        }

        if (itemCondition != null && item.getCondition() != null) {
            itemCondition.setText(getString(R.string.condition_format, item.getCondition()));
        }

        if (itemLocation != null && item.getLocation() != null) {
            itemLocation.setText(getString(R.string.location_format, item.getLocation()));
        }

        // Setup image slider if the item has photos
        if (item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) {
            ImageSliderAdapter imageAdapter = new ImageSliderAdapter(requireContext(), item.getPhotoUris());
            imageViewPager.setAdapter(imageAdapter);

            // Set up indicators for the image slider
            new TabLayoutMediator(tabLayoutIndicators, imageViewPager, (tab, position) -> {
                // No text for tabs, they are just indicators
            }).attach();
        }

        // Set up button click listeners
        setupButtonClickListeners();
    }

    private String formatPrice(double price) {
        try {
            Currency currency = Currency.getInstance(new Locale("vi", "VN"));
            return String.format("%s %,.0f", currency.getSymbol(), price);
        } catch (Exception e) {
            return String.format("₫%,.0f", price);
        }
    }

    private void initViews(View view) {
        // Initialize toolbar
        toolbar = view.findViewById(R.id.toolbar);

        // Initialize item detail views
        itemTitle = view.findViewById(R.id.item_title);
        itemPrice = view.findViewById(R.id.item_price);
        itemDescription = view.findViewById(R.id.item_description);
        itemCondition = view.findViewById(R.id.item_condition);
        itemLocation = view.findViewById(R.id.item_location);

        // Initialize image slider
        imageViewPager = view.findViewById(R.id.view_pager_images);
        tabLayoutIndicators = view.findViewById(R.id.tab_layout_indicators);

        // Initialize buttons
        btnBuyNow = view.findViewById(R.id.btn_buy_now);
        btnMakeOffer = view.findViewById(R.id.btn_make_offer);
        btnMessage = view.findViewById(R.id.btn_message);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            // Set title to show the item name in the toolbar when collapsed
            if (item != null) {
                toolbar.setTitle(item.getTitle());
            }

            toolbar.setNavigationOnClickListener(v -> {
                // Navigate back to previous screen
                safeNavigateBack();
            });
        }
    }

    private void safeNavigateBack() {
        mainHandler.post(() -> {
            if (isAdded()) {
                requireActivity().onBackPressed();
            }
        });
    }

    private void showError() {
        Toast.makeText(requireContext(), R.string.error_item_not_found, Toast.LENGTH_SHORT).show();
        // Navigate back safely after a short delay
        mainHandler.postDelayed(this::safeNavigateBack, 500);
    }

    private void setupButtonClickListeners() {
        // Buy Now button click listener
        btnBuyNow.setOnClickListener(v -> {
            // Create bundle with item information for checkout
            Bundle bundle = new Bundle();
            bundle.putParcelable("item", item);
            bundle.putBoolean("isBuyNow", true);

            // Navigate to checkout fragment
            safeNavigate(R.id.action_itemDetailFragment_to_checkoutFragment, bundle);
        });

        // Make Offer button click listener
        btnMakeOffer.setOnClickListener(v -> {
            // Create a bundle with the item information
            Bundle bundle = new Bundle();
            bundle.putParcelable("item", item);

            // Show the offer dialog
            MakeOfferDialogFragment offerDialog = new MakeOfferDialogFragment();
            offerDialog.setArguments(bundle);
            offerDialog.show(getChildFragmentManager(), "MakeOfferDialog");
        });

        // Message button click listener
        btnMessage.setOnClickListener(v -> {
            // Create bundle with seller and item information
            Bundle bundle = new Bundle();
            bundle.putString("receiverId", item.getUserId());
            bundle.putString("receiverName", "Item Owner"); // Temporary placeholder
            bundle.putParcelable("item", item);

            // Navigate to chat fragment
            safeNavigate(R.id.action_itemDetailFragment_to_chatFragment, bundle);
        });
    }

    private void safeNavigate(int actionId, Bundle args) {
        if (isAdded() && getView() != null) {
            Navigation.findNavController(requireView()).navigate(actionId, args);
        }
    }

    // Method to check if we should use dummy data
    private boolean isUsingDummyData() {
        return true; // For now, always use dummy data
    }

    // Setup dummy data for testing
    private void setupDummyData() {
        if (item == null) {
            item = new ItemModel();
            item.setId("dummy_item_123");
            item.setUserId("dummy_user_456");
            item.setTitle("Bàn học gỗ cao cấp");
            item.setDescription("Bàn học gỗ cao cấp, màu nâu, có ngăn kéo, phù hợp cho học sinh, sinh viên và người làm việc tại nhà.");
            item.setPrice(850000);
            item.setCategory("Đồ nội thất");
            item.setCondition("Còn mới 90%");
            item.setLocation("Quận 1, TP.HCM");
            item.setStatus("Available");

            // Add dummy image URIs - use existing drawable resources
            try {
                Uri dummyUri1 = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.drawable.ic_image_placeholder);
                item.addPhotoUri(dummyUri1);

                // Add a second image to demonstrate the slider
                Uri dummyUri2 = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.drawable.sample_item_image);
                item.addPhotoUri(dummyUri2);
            } catch (Exception e) {
                // Fallback in case resource doesn't exist
                // Log error but continue without images
                e.printStackTrace();
            }

            // Update the ViewModel with our dummy data
            viewModel.setItem(item);
        }

        // Always ensure ViewPager has an adapter, regardless of photo status
        if (isAdded()) {
            ImageSliderAdapter imageAdapter = new ImageSliderAdapter(
                    requireContext(),
                    item.getPhotoUris() != null ? item.getPhotoUris() : new ArrayList<>());
            imageViewPager.setAdapter(imageAdapter);

            // Always set up indicators for the image slider
            new TabLayoutMediator(tabLayoutIndicators, imageViewPager, (tab, position) -> {
                // No text for tabs, they are just indicators
            }).attach();
        }
    }
}
