package com.example.tradeupapp.features.payment.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.core.services.CartService;
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.features.cart.ui.CartDisplayItem;
import com.example.tradeupapp.features.cart.ui.CartListAdapter;
import com.example.tradeupapp.models.CartItem;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutFragment extends Fragment {

    private FirebaseFirestore db;
    // UI Components
    private TextView tvItemPriceSummary;
    private TextView tvServiceFee;
    private TextView tvTotalAmount;
    private MaterialButton btnCancel;
    private MaterialButton btnProceedPayment;
    private View progressOverlay;
    private RecyclerView recyclerViewCart;
    private TextView tvEmptyCart;
    private com.google.android.material.textfield.TextInputEditText etAddressInput;
    private com.google.android.material.textfield.TextInputEditText etPhoneInput;

    // Services
    private CartService cartService;
    private FirebaseService firebaseService;
    private String userId;

    // Constants
    private static final double SERVICE_FEE = 30000; // 30,000 VND

    // Adapter
    private CartListAdapter cartListAdapter;
    private List<CartDisplayItem> cartDisplayItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        initViews(view);
        setupViews(view);
        cartService = CartService.getInstance();
        firebaseService = FirebaseService.getInstance();
        userId = com.example.tradeupapp.core.session.UserSession.getInstance().getId();
        // Always auto-fill address and phone
        com.example.tradeupapp.models.User user = com.example.tradeupapp.core.session.UserSession.getInstance().getCurrentUser();
        android.util.Log.d("CheckoutAutoFill", "UserSession.getCurrentUser(): " + user);
        com.google.firebase.firestore.GeoPoint geo = null;
        if (user != null) {
            try {
                geo = user.getLocation();
                android.util.Log.d("CheckoutAutoFill", "GeoPoint from getLocation(): " + geo);
                if (geo == null) {
                    Double lat = user.getLocationLatitude();
                    Double lng = user.getLocationLongitude();
                    android.util.Log.d("CheckoutAutoFill", "Fallback lat: " + lat + ", lng: " + lng);
                    if (lat != null && lng != null) {
                        geo = new com.google.firebase.firestore.GeoPoint(lat, lng);
                        android.util.Log.d("CheckoutAutoFill", "Created GeoPoint from lat/lng: " + geo);
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("CheckoutAutoFill", "Error getting location from user: " + e.getMessage());
            }
            autofillAddressFromLocation(geo);
            String phone = user.getPhoneNumber();
            android.util.Log.d("CheckoutAutoFill", "Phone: " + phone);
            if (phone != null) {
                etPhoneInput.setText(phone);
            } else {
                etPhoneInput.setText("");
            }
        } else {
            etAddressInput.setText("");
            etPhoneInput.setText("");
            android.util.Log.d("CheckoutAutoFill", "User is null, set both fields empty");
        }
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));
        cartListAdapter = new CartListAdapter(new ArrayList<>(), null); // null disables remove button
        recyclerViewCart.setAdapter(cartListAdapter);
        loadCartProducts();
    }

    private void initViews(View view) {
        tvItemPriceSummary = view.findViewById(R.id.tv_item_price_summary);
        tvServiceFee = view.findViewById(R.id.tv_service_fee);
        tvTotalAmount = view.findViewById(R.id.tv_total_amount);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnProceedPayment = view.findViewById(R.id.btn_proceed_payment);
        progressOverlay = view.findViewById(R.id.progress_overlay);
        recyclerViewCart = view.findViewById(R.id.recycler_view_checkout_cart);
        tvEmptyCart = view.findViewById(R.id.tv_empty_cart);
        etAddressInput = view.findViewById(R.id.et_address);
        etPhoneInput = view.findViewById(R.id.et_phone);
    }

    private void setupViews(View view) {
        btnCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        btnProceedPayment.setOnClickListener(v -> {
            if (cartDisplayItems.isEmpty()) {
                Toast.makeText(requireContext(), "Your cart is empty.", Toast.LENGTH_SHORT).show();
            } else {
                processPayment();
            }
        });
    }

    private void processPayment() {
        showLoading(true);
        String address = ((TextView) requireView().findViewById(R.id.et_address)).getText().toString().trim();
        String phone = ((TextView) requireView().findViewById(R.id.et_phone)).getText().toString().trim();
        RadioGroup rgPayment = requireView().findViewById(R.id.rg_payment_method);
        int selectedPaymentId = rgPayment.getCheckedRadioButtonId();
        String paymentMethod = null;
        if (selectedPaymentId != -1) {
            paymentMethod = ((TextView) requireView().findViewById(selectedPaymentId)).getText().toString();
        }
        if (address.isEmpty() || phone.isEmpty() || paymentMethod == null) {
            showLoading(false);
            Toast.makeText(requireContext(), "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }
        double totalAmount = 0;
        List<String> listingIds = new ArrayList<>();
        for (CartDisplayItem cartItem : cartDisplayItems) {
            totalAmount += cartItem.getListing().getPrice();
            listingIds.add(cartItem.getListing().getId());
        }
        totalAmount += SERVICE_FEE;
        final int[] completed = {0};
        final boolean[] hasError = {false};
        for (CartDisplayItem cartItem : cartDisplayItems) {
            double price = cartItem.getListing().getPrice();
            double transactionFee = SERVICE_FEE / cartDisplayItems.size();
            double sellerEarnings = price - transactionFee;
            java.util.Map<String, Object> order = new java.util.HashMap<>();
            order.put("listingId", cartItem.getListing().getId());
            order.put("sellerId", cartItem.getListing().getSellerId());
            order.put("buyerId", userId);
            order.put("amount", price);
            order.put("status", "completed");
            order.put("paymentMethod", paymentMethod);
            order.put("shippingMethod", "pickup"); // default
            order.put("createdAt", com.google.firebase.Timestamp.now());
            order.put("completedAt", com.google.firebase.Timestamp.now());
            order.put("offerId", null);
            order.put("transactionFee", transactionFee);
            order.put("sellerEarnings", sellerEarnings);
            order.put("address", address);
            order.put("phone", phone);
            db.collection("transactions").add(order)
                .addOnSuccessListener(documentReference -> {
                    db.collection("listings").document(cartItem.getListing().getId())
                        .update("transactionStatus", "sold")
                        .addOnSuccessListener(aVoid -> {
                            completed[0]++;
                            if (completed[0] == cartDisplayItems.size() && !hasError[0]) {
                                cartService.clearCart(userId, new CartService.SimpleCallback() {
                                    @Override
                                    public void onSuccess() {
                                        showLoading(false);
                                        Toast.makeText(requireContext(), "Order placed successfully!", Toast.LENGTH_LONG).show();
                                        Navigation.findNavController(requireView()).navigate(R.id.nav_recommendations);
                                    }
                                    @Override
                                    public void onError(String err) {
                                        showLoading(false);
                                        Toast.makeText(requireContext(), "Order placed, but failed to clear cart.", Toast.LENGTH_LONG).show();
                                        Navigation.findNavController(requireView()).navigate(R.id.nav_recommendations);
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(e -> {
                            hasError[0] = true;
                            showLoading(false);
                            Toast.makeText(requireContext(), "Order placed, but failed to update product status.", Toast.LENGTH_LONG).show();
                        });
                })
                .addOnFailureListener(e -> {
                    hasError[0] = true;
                    showLoading(false);
                    Toast.makeText(requireContext(), "Failed to place order. Please try again.", Toast.LENGTH_LONG).show();
                });
        }
    }

    private void showLoading(boolean show) {
        if (progressOverlay != null) {
            progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnProceedPayment.setEnabled(!show);
        btnCancel.setEnabled(!show);
    }

    private void loadCartProducts() {
        if (userId == null) return;
        cartService.getCart(userId, new CartService.CartCallback() {
            @Override
            public void onSuccess(List<CartItem> items) {
                List<String> listingIds = new ArrayList<>();
                for (CartItem item : items) listingIds.add(item.getListingId());
                if (listingIds.isEmpty()) {
                    cartDisplayItems.clear();
                    cartListAdapter.setCartItems(new ArrayList<>());
                    tvEmptyCart.setVisibility(View.VISIBLE);
                    recyclerViewCart.setVisibility(View.GONE);
                    updateSummary();
                    return;
                }
                List<ListingModel> allCartListings = new ArrayList<>();
                List<String> allItemIds = new ArrayList<>();
                List<List<String>> batches = new ArrayList<>();
                for (int i = 0; i < listingIds.size(); i += 10) {
                    batches.add(listingIds.subList(i, Math.min(i + 10, listingIds.size())));
                }
                final int[] batchesProcessed = {0};
                for (List<String> batch : batches) {
                    db.collection("listings")
                        .whereIn("id", batch)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                ListingModel l = doc.toObject(ListingModel.class);
                                if (l != null) {
                                    allCartListings.add(l);
                                    allItemIds.add(l.getItemId());
                                }
                            }
                            batchesProcessed[0]++;
                            if (batchesProcessed[0] == batches.size()) {
                                if (allCartListings.isEmpty()) {
                                    cartDisplayItems.clear();
                                    cartListAdapter.setCartItems(new ArrayList<>());
                                    tvEmptyCart.setVisibility(View.VISIBLE);
                                    recyclerViewCart.setVisibility(View.GONE);
                                    updateSummary();
                                    return;
                                }
                                firebaseService.getItemsByIds(allItemIds, new FirebaseService.ItemsByIdsCallback() {
                                    @Override
                                    public void onSuccess(java.util.Map<String, ItemModel> itemMap) {
                                        cartDisplayItems.clear();
                                        for (ListingModel listing : allCartListings) {
                                            ItemModel item = itemMap.get(listing.getItemId());
                                            if (item != null) {
                                                cartDisplayItems.add(new CartDisplayItem(listing, item));
                                            }
                                        }
                                        cartListAdapter.setCartItems(cartDisplayItems);
                                        tvEmptyCart.setVisibility(cartDisplayItems.isEmpty() ? View.VISIBLE : View.GONE);
                                        recyclerViewCart.setVisibility(cartDisplayItems.isEmpty() ? View.GONE : View.VISIBLE);
                                        updateSummary();
                                    }
                                    @Override
                                    public void onError(String error) {
                                        cartDisplayItems.clear();
                                        cartListAdapter.setCartItems(new ArrayList<>());
                                        tvEmptyCart.setVisibility(View.VISIBLE);
                                        recyclerViewCart.setVisibility(View.GONE);
                                        updateSummary();
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(e -> {
                            batchesProcessed[0]++;
                            if (batchesProcessed[0] == batches.size()) {
                                cartDisplayItems.clear();
                                cartListAdapter.setCartItems(new ArrayList<>());
                                tvEmptyCart.setVisibility(View.VISIBLE);
                                recyclerViewCart.setVisibility(View.GONE);
                                updateSummary();
                            }
                        });
                }
            }
            @Override
            public void onError(String error) {
                cartDisplayItems.clear();
                cartListAdapter.setCartItems(new ArrayList<>());
                tvEmptyCart.setVisibility(View.VISIBLE);
                recyclerViewCart.setVisibility(View.GONE);
                updateSummary();
            }
        });
    }

    private void updateSummary() {
        double total = 0;
        for (CartDisplayItem item : cartDisplayItems) {
            total += item.getListing().getPrice();
        }
        java.text.NumberFormat currencyFormat = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        String totalStr = String.format("%s đ", currencyFormat.format(total));
        String feeStr = String.format("%s đ", currencyFormat.format(SERVICE_FEE));
        String grandTotalStr = String.format("%s đ", currencyFormat.format(total + SERVICE_FEE));
        tvItemPriceSummary.setText(totalStr);
        tvServiceFee.setText(feeStr);
        tvTotalAmount.setText(grandTotalStr);
    }

    private void autofillAddressFromLocation(com.google.firebase.firestore.GeoPoint geo) {
        if (geo == null) {
            etAddressInput.setText("");
            return;
        }
        android.location.Geocoder geocoder = new android.location.Geocoder(requireContext(), java.util.Locale.getDefault());
        new Thread(() -> {
            try {
                java.util.List<android.location.Address> addresses = geocoder.getFromLocation(geo.getLatitude(), geo.getLongitude(), 1);
                String addressStr = null;
                if (addresses != null && !addresses.isEmpty()) {
                    android.location.Address address = addresses.get(0);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        sb.append(address.getAddressLine(i));
                        if (i != address.getMaxAddressLineIndex()) sb.append(", ");
                    }
                    addressStr = sb.toString();
                }
                if (addressStr == null || addressStr.isEmpty()) {
                    addressStr = String.format(java.util.Locale.US, "%.6f, %.6f", geo.getLatitude(), geo.getLongitude());
                }
                final String finalAddressStr = addressStr;
                requireActivity().runOnUiThread(() -> etAddressInput.setText(finalAddressStr));
            } catch (Exception e) {
                String fallback = String.format(java.util.Locale.US, "%.6f, %.6f", geo.getLatitude(), geo.getLongitude());
                requireActivity().runOnUiThread(() -> etAddressInput.setText(fallback));
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (btnProceedPayment != null) {
            btnProceedPayment.removeCallbacks(null);
        }
    }
}
