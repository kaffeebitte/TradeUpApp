package com.example.tradeupapp.features.history.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.LinearLayout;
import com.example.tradeupapp.R;
import com.example.tradeupapp.models.TransactionModel;
import com.example.tradeupapp.shared.adapters.PurchaseHistoryAdapter;
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.core.services.ItemRepository;
import java.util.ArrayList;
import java.util.List;

public class PurchaseHistoryFragment extends Fragment implements PurchaseHistoryAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private LinearLayout emptyStateContainer;
    private PurchaseHistoryAdapter adapter;
    private ItemRepository itemRepository;
    private final java.util.Map<String, com.example.tradeupapp.models.ListingModel> listingMap = new java.util.HashMap<>();
    private final java.util.Map<String, com.example.tradeupapp.models.ItemModel> itemMap = new java.util.HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchase_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view_purchase_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemRepository = new ItemRepository();
        adapter = new PurchaseHistoryAdapter(requireContext(), new ArrayList<>(), this, itemRepository, listingMap, itemMap);
        recyclerView.setAdapter(adapter);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        loadPurchaseHistory();
    }

    private void loadPurchaseHistory() {
        String userId = FirebaseService.getInstance().getCurrentUserId();
        if (userId != null) {
            FirebaseService.getInstance().getUserTransactions(userId, new FirebaseService.TransactionsCallback() {
                @Override
                public void onSuccess(List<TransactionModel> transactions) {
                    // Filter for purchases: status is "completed"
                    List<TransactionModel> purchaseList = new ArrayList<>();
                    for (TransactionModel txn : transactions) {
                        if (txn.getStatus() != null && txn.getStatus().equalsIgnoreCase("completed")) {
                            purchaseList.add(txn);
                        }
                    }
                    showPurchaseHistory(purchaseList);
                }
                @Override
                public void onError(String error) {
                    recyclerView.setVisibility(View.GONE);
                    emptyStateContainer.setVisibility(View.VISIBLE);
                }
            });
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showPurchaseHistory(List<TransactionModel> purchases) {
        if (purchases == null || purchases.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
            adapter.updateTransactions(purchases);
        }
    }

    @Override
    public void onItemClick(String listingId) {
        itemRepository.getListingById(listingId, listing -> {
            // Navigation logic if needed
        });
    }

    @Override
    public void onReorderClick(String listingId) {
        // Implement reorder logic if needed
    }

    @Override
    public void onReviewClick(String listingId) {
        // Implement review logic if needed
    }
}
