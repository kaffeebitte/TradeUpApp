package com.example.tradeupapp.features.cart.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.models.CartItem;
import com.example.tradeupapp.core.session.UserSession;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {
    private RecyclerView recyclerView;
    private CartListAdapter adapter;
    private CartService cartService;
    private FirebaseService firebaseService;
    private String userId;
    private TextView tvTotal;
    private MaterialButton btnCheckout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view_cart);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartListAdapter(new ArrayList<>(), this::onRemoveClicked);
        recyclerView.setAdapter(adapter);
        tvTotal = view.findViewById(R.id.tv_cart_total);
        cartService = CartService.getInstance();
        firebaseService = FirebaseService.getInstance();
        userId = UserSession.getInstance().getId();
        btnCheckout = view.findViewById(R.id.btn_checkout);
        loadCart();
        btnCheckout.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_cartFragment_to_checkoutFragment);
        });
    }

    private void loadCart() {
        if (userId == null) return;
        cartService.getCart(userId, new CartService.CartCallback() {
            @Override
            public void onSuccess(List<CartItem> items) {
                List<String> listingIds = new ArrayList<>();
                for (CartItem item : items) listingIds.add(item.getListingId());
                if (listingIds.isEmpty()) {
                    adapter.setCartItems(new ArrayList<>());
                    if (tvTotal != null) tvTotal.setText("0 đ");
                    if (btnCheckout != null) btnCheckout.setEnabled(false);
                    return;
                }
                firebaseService.getAllListings(new FirebaseService.ListingsCallback() {
                    @Override
                    public void onSuccess(List<ListingModel> allListings) {
                        List<ListingModel> cartListings = new ArrayList<>();
                        for (ListingModel l : allListings) {
                            if (listingIds.contains(l.getId())) cartListings.add(l);
                        }
                        if (cartListings.isEmpty()) {
                            adapter.setCartItems(new ArrayList<>());
                            if (tvTotal != null) tvTotal.setText("0 đ");
                            if (btnCheckout != null) btnCheckout.setEnabled(false);
                            return;
                        }
                        // Fetch all related ItemModels
                        List<String> itemIds = new ArrayList<>();
                        for (ListingModel l : cartListings) itemIds.add(l.getItemId());
                        firebaseService.getItemsByIds(itemIds, new FirebaseService.ItemsByIdsCallback() {
                            @Override
                            public void onSuccess(java.util.Map<String, ItemModel> itemMap) {
                                List<CartDisplayItem> cartItemList = new ArrayList<>();
                                double total = 0;
                                for (ListingModel listing : cartListings) {
                                    ItemModel item = itemMap.get(listing.getItemId());
                                    if (item != null) {
                                        // Only add items with title and condition
                                        if (item.getTitle() != null && item.getCondition() != null) {
                                            cartItemList.add(new CartDisplayItem(listing, item));
                                            total += listing.getPrice();
                                        }
                                    }
                                }
                                adapter.setCartItems(cartItemList);
                                if (tvTotal != null) tvTotal.setText(String.format("%,.0f đ", total));
                                if (btnCheckout != null) btnCheckout.setEnabled(!cartItemList.isEmpty());
                            }
                            @Override
                            public void onError(String error) {
                                Toast.makeText(getContext(), "Failed to load items", Toast.LENGTH_SHORT).show();
                                if (tvTotal != null) tvTotal.setText("0 đ");
                                if (btnCheckout != null) btnCheckout.setEnabled(false);
                            }
                        });
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), "Failed to load listings", Toast.LENGTH_SHORT).show();
                        if (tvTotal != null) tvTotal.setText("0 đ");
                        if (btnCheckout != null) btnCheckout.setEnabled(false);
                    }
                });
            }
            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Failed to load cart", Toast.LENGTH_SHORT).show();
                if (tvTotal != null) tvTotal.setText("0 đ");
                if (btnCheckout != null) btnCheckout.setEnabled(false);
            }
        });
    }

    private void onRemoveClicked(ListingModel listing) {
        if (userId == null) return;
        cartService.removeFromCart(userId, listing.getId(), new CartService.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Removed from cart", Toast.LENGTH_SHORT).show();
                loadCart();
            }
            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Failed to remove: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
