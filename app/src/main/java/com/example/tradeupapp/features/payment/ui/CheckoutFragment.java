package com.example.tradeupapp.features.payment.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tradeupapp.R;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;

public class CheckoutFragment extends Fragment {

    private ItemModel item;
    private boolean isBuyNow;
    private ListingModel listing; // Store the loaded listing
    private FirebaseFirestore db;

    // UI Components
    private ImageView ivItemImage;
    private TextView tvItemTitle;
    private TextView tvItemPrice;
    private TextView tvItemCondition;
    private TextView tvItemPriceSummary;
    private TextView tvServiceFee;
    private TextView tvTotalAmount;
    private MaterialButton btnCancel;
    private MaterialButton btnProceedPayment;
    private View progressOverlay;

    // Constants
    private static final double SERVICE_FEE = 2.99;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get arguments from navigation
        if (getArguments() != null) {
            item = (ItemModel) getArguments().getSerializable("item");
            isBuyNow = getArguments().getBoolean("isBuyNow", false);
        }

        db = FirebaseFirestore.getInstance();
        initViews(view);
        setupViews(view);
        fetchListingAndDisplay();
    }

    private void initViews(View view) {
        ivItemImage = view.findViewById(R.id.iv_item_image);
        tvItemTitle = view.findViewById(R.id.tv_item_title);
        tvItemPrice = view.findViewById(R.id.tv_item_price);
        tvItemCondition = view.findViewById(R.id.tv_item_condition);
        tvItemPriceSummary = view.findViewById(R.id.tv_item_price_summary);
        tvServiceFee = view.findViewById(R.id.tv_service_fee);
        tvTotalAmount = view.findViewById(R.id.tv_total_amount);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnProceedPayment = view.findViewById(R.id.btn_proceed_payment);
        progressOverlay = view.findViewById(R.id.progress_overlay);
    }

    private void setupViews(View view) {
        // Setup click listeners
        btnCancel.setOnClickListener(v -> {
            // Navigate back to previous screen
            Navigation.findNavController(v).navigateUp();
        });

        btnProceedPayment.setOnClickListener(v -> {
            if (item != null) {
                processPayment();
            } else {
                Toast.makeText(requireContext(), "Lỗi: Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchListingAndDisplay() {
        if (item == null) {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("listings").whereEqualTo("itemId", item.getId()).limit(1).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    ListingModel loadedListing = queryDocumentSnapshots.getDocuments().get(0).toObject(ListingModel.class);
                    if (loadedListing != null) {
                        listing = loadedListing;
                        displayItemDataWithListing();
                        return;
                    }
                }
                Toast.makeText(requireContext(), "Không tìm thấy thông tin giá sản phẩm.", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> Toast.makeText(requireContext(), "Lỗi khi tải giá sản phẩm.", Toast.LENGTH_SHORT).show());
    }

    private void displayItemDataWithListing() {
        if (item == null || listing == null) return;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvItemTitle.setText(item.getTitle());
        tvItemPrice.setText(currencyFormat.format(listing.getPrice()));
        tvItemCondition.setText("Tình trạng: " + item.getCondition());
        tvItemPriceSummary.setText(currencyFormat.format(listing.getPrice()));
        tvServiceFee.setText(currencyFormat.format(SERVICE_FEE));
        double totalAmount = listing.getPrice() + SERVICE_FEE;
        tvTotalAmount.setText(currencyFormat.format(totalAmount));
        String buttonText = isBuyNow ? "Mua ngay" : "Tiến hành thanh toán";
        btnProceedPayment.setText(buttonText);
        // TODO: Load item image using Glide when image URLs are available
    }

    private void processPayment() {
        // Show loading
        showLoading(true);

        // Simulate payment processing
        btnProceedPayment.postDelayed(() -> {
            showLoading(false);

            // TODO: Implement actual payment processing here
            // This could include:
            // - Payment gateway integration (Stripe, PayPal, etc.)
            // - Order creation in Firestore
            // - Notification to seller
            // - Email confirmation to buyer

            String successMessage = isBuyNow ?
                "Mua hàng thành công! Đơn hàng của bạn đang được xử lý." :
                "Thanh toán thành công! Đơn hàng của bạn đang được xử lý.";

            Toast.makeText(requireContext(), successMessage, Toast.LENGTH_LONG).show();

            // Navigate back to home or order confirmation screen
            Navigation.findNavController(requireView()).navigate(R.id.nav_recommendations);

        }, 2000); // Simulate 2 second processing time
    }

    private void showLoading(boolean show) {
        if (progressOverlay != null) {
            progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnProceedPayment.setEnabled(!show);
        btnCancel.setEnabled(!show);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove any pending callbacks to prevent memory leaks
        if (btnProceedPayment != null) {
            btnProceedPayment.removeCallbacks(null);
        }
    }
}
