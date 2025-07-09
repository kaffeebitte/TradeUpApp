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
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.shared.adapters.PurchaseHistoryAdapter;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class PurchaseHistoryFragment extends Fragment implements PurchaseHistoryAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private MaterialTextView emptyStateTextView;
    private PurchaseHistoryAdapter adapter;
    private FirebaseService firebaseService;
    private com.example.tradeupapp.core.services.ItemRepository itemRepository;

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
        // Initialize ItemRepository
        itemRepository = new com.example.tradeupapp.core.services.ItemRepository();
        // Initialize adapter with empty list and pass itemRepository
        adapter = new PurchaseHistoryAdapter(requireContext(), new ArrayList<>(), this, itemRepository);
        recyclerView.setAdapter(adapter);
    }

    private void loadPurchaseHistory() {
        String currentUserId = firebaseService.getCurrentUserId();
        if (currentUserId == null) {
            Toast.makeText(getActivity(), "Please log in to view your purchase history", Toast.LENGTH_SHORT).show();
            showEmptyState();
            return;
        }
        // TODO: Replace with getUserPurchasedListings when available
        firebaseService.getUserPurchasedListings(currentUserId, new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(List<ListingModel> purchasedListings) {
                if (getActivity() != null && isAdded()) {
                    if (purchasedListings.isEmpty()) {
                        showEmptyState();
                    } else {
                        showPurchaseHistory(purchasedListings);
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

    private void showPurchaseHistory(List<ListingModel> purchasedListings) {
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateTextView.setVisibility(View.GONE);
        adapter.updateItems(purchasedListings);
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.VISIBLE);
        emptyStateTextView.setText(R.string.no_purchases);
    }

    // PurchaseHistoryAdapter.OnItemClickListener implementation
    @Override
    public void onItemClick(ListingModel listing) {
        // Handle item click (navigate to item detail)
        Bundle args = new Bundle();
        args.putString("itemId", listing.getItemId());
        Navigation.findNavController(requireView()).navigate(R.id.action_purchaseHistoryFragment_to_itemDetailFragment, args);
    }

    @Override
    public void onReorderClick(ListingModel listing) {
        // Handle reorder click (navigate to item detail or show a message)
        Bundle args = new Bundle();
        args.putString("itemId", listing.getItemId());
        Toast.makeText(getContext(), "Redirecting to original listing...", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigate(R.id.action_purchaseHistoryFragment_to_itemDetailFragment, args);
    }

    @Override
    public void onReviewClick(ListingModel listing) {
        // Handle review click (show a message or navigate to review screen)
        Toast.makeText(getContext(), "Review feature not implemented yet.", Toast.LENGTH_SHORT).show();
    }
}
