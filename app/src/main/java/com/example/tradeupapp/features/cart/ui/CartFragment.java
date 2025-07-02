package com.example.tradeupapp.features.cart.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Fragment for displaying the user's shopping cart
 */
public class CartFragment extends Fragment {

    private RecyclerView recyclerViewCart;
    private MaterialButton btnCheckout;
    private MaterialToolbar toolbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Setup toolbar
        setupToolbar();

        // Setup recycler view
        setupRecyclerView();

        // Setup checkout button
        setupCheckoutButton();
    }

    private void initViews(View view) {
        recyclerViewCart = view.findViewById(R.id.recycler_view_cart);
        btnCheckout = view.findViewById(R.id.btn_checkout);
        toolbar = view.findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        toolbar.setTitle(R.string.cart);
        toolbar.setNavigationOnClickListener(v -> {
            // Navigate back
            requireActivity().onBackPressed();
        });
    }

    private void setupRecyclerView() {
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        // TODO: Set adapter with cart items
        // CartAdapter adapter = new CartAdapter(getCartItems());
        // recyclerViewCart.setAdapter(adapter);
    }

    private void setupCheckoutButton() {
        btnCheckout.setOnClickListener(v -> {
            // TODO: Navigate to checkout
            // Navigation.findNavController(v).navigate(R.id.action_cartFragment_to_checkoutFragment);
        });
    }
}
