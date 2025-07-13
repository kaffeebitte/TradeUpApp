package com.example.tradeupapp.features.history.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.models.TransactionModel;
import com.example.tradeupapp.shared.adapters.PurchaseHistoryAdapter;
import com.example.tradeupapp.core.services.ItemRepository;

import java.util.ArrayList;
import java.util.List;

public class OfferHistoryFragment extends Fragment implements PurchaseHistoryAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private LinearLayout emptyStateContainer;
    private PurchaseHistoryAdapter adapter;
    private ItemRepository itemRepository;
    private final java.util.Map<String, com.example.tradeupapp.models.ListingModel> listingMap = new java.util.HashMap<>();
    private final java.util.Map<String, com.example.tradeupapp.models.ItemModel> itemMap = new java.util.HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_offer_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view_offer_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemRepository = new ItemRepository();
        adapter = new PurchaseHistoryAdapter(requireContext(), new ArrayList<>(), this, itemRepository, listingMap, itemMap);
        recyclerView.setAdapter(adapter);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        loadOfferHistory();
    }

    private void loadOfferHistory() {
        // Dummy data for UI testing
        List<TransactionModel> dummyList = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            TransactionModel t = new TransactionModel();
            t.setId("offer_dummy_" + i);
            t.setListingId("listing_offer_" + i);
            t.setBuyerId("user_dummy");
            t.setSellerId("seller_dummy");
            t.setAmount(500000 * i);
            t.setStatus(i % 2 == 0 ? "pending" : "accepted");
            t.setPaymentMethod("cash");
            t.setCreatedAt(null);
            t.setCompletedAt(null);
            dummyList.add(t);
        }
        showOfferHistory(dummyList);
    }

    private void showOfferHistory(List<TransactionModel> offers) {
        if (offers == null || offers.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
            adapter.updateTransactions(offers);
        }
    }

    @Override
    public void onItemClick(String listingId) {
        itemRepository.getListingById(listingId, listing -> {
            if (listing != null && listing.getItemId() != null) {
                Bundle args = new Bundle();
                args.putString("itemId", listing.getItemId());
                Toast.makeText(getContext(), "Go to item detail", Toast.LENGTH_SHORT).show();
                // Navigation logic here if needed
            } else {
                Toast.makeText(getContext(), "Listing not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReorderClick(String listingId) {
        Toast.makeText(getContext(), "Reorder feature not implemented yet.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReviewClick(String listingId) {
        Toast.makeText(getContext(), "Review feature not implemented yet.", Toast.LENGTH_SHORT).show();
    }
}
