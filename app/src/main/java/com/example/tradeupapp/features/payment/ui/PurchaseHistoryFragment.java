package com.example.tradeupapp.features.payment.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.shared.adapters.PurchaseHistoryAdapter;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class PurchaseHistoryFragment extends Fragment implements PurchaseHistoryAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private MaterialTextView emptyStateTextView;
    private PurchaseHistoryAdapter adapter;
    private FirebaseService firebaseService;

    public static PurchaseHistoryFragment newInstance() {
        return new PurchaseHistoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchase_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase service
        firebaseService = FirebaseService.getInstance();

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view);
        emptyStateTextView = view.findViewById(R.id.empty_state_text);

        // Setup RecyclerView
        setupRecyclerView();

        // Load purchase history data
        loadPurchaseHistory();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize adapter with empty list
        adapter = new PurchaseHistoryAdapter(requireContext(), new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }

    private void loadPurchaseHistory() {
        String currentUserId = firebaseService.getCurrentUserId();
        if (currentUserId == null) {
            Toast.makeText(getActivity(), "Please log in to view your purchase history", Toast.LENGTH_SHORT).show();
            showEmptyState();
            return;
        }

        // Load items that this user has purchased (items with status like "Purchased", "Delivered", etc.)
        firebaseService.getUserPurchasedItems(currentUserId, new FirebaseService.ItemsCallback() {
            @Override
            public void onSuccess(List<ItemModel> purchasedItems) {
                if (getActivity() != null && isAdded()) {
                    if (purchasedItems.isEmpty()) {
                        showEmptyState();
                    } else {
                        showPurchaseHistory(purchasedItems);
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), "Error loading purchase history: " + error, Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }
        });
    }

    private void showPurchaseHistory(List<ItemModel> purchasedItems) {
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateTextView.setVisibility(View.GONE);
        adapter.updateItems(purchasedItems);
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.VISIBLE);
        emptyStateTextView.setText(R.string.no_purchases);
    }

    // PurchaseHistoryAdapter.OnItemClickListener implementation
    @Override
    public void onItemClick(ItemModel item) {
        // Navigate to item details to view the purchased item
        Bundle args = new Bundle();
        args.putParcelable("item", item);
        Navigation.findNavController(requireView()).navigate(R.id.action_purchaseHistoryFragment_to_itemDetailFragment, args);
    }

    @Override
    public void onReorderClick(ItemModel item) {
        // Handle reorder functionality
        if (item.getListingId() != null) {
            // Navigate to the original listing to reorder
            Bundle args = new Bundle();
            args.putString("listingId", item.getListingId());
            Toast.makeText(getContext(), "Redirecting to original listing...", Toast.LENGTH_SHORT).show();
            // Navigation.findNavController(requireView()).navigate(R.id.action_purchaseHistoryFragment_to_listingDetailFragment, args);
        } else {
            Toast.makeText(getContext(), "Original listing is no longer available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReviewClick(ItemModel item) {
        // Navigate to review/rating screen
        Bundle args = new Bundle();
        args.putParcelable("item", item);
        args.putString("transactionId", item.getTransactionId());
        Toast.makeText(getContext(), "Opening review form...", Toast.LENGTH_SHORT).show();
        // Navigation.findNavController(requireView()).navigate(R.id.action_purchaseHistoryFragment_to_reviewFragment, args);
    }
}
