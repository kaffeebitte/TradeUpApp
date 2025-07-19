package com.example.tradeupapp.features.payment.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.example.tradeupapp.models.TransactionModel;
import com.example.tradeupapp.core.session.UserSession;
import com.example.tradeupapp.shared.adapters.PurchaseHistoryAdapter;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class PurchaseHistoryFragment extends Fragment implements PurchaseHistoryAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private LinearLayout emptyStateContainer;
    private PurchaseHistoryAdapter adapter;
    private FirebaseService firebaseService;
    private com.example.tradeupapp.core.services.ItemRepository itemRepository;
    private final List<TransactionModel> transactionList = new ArrayList<>();
    private final java.util.Map<String, ListingModel> listingMap = new java.util.HashMap<>();
    private final java.util.Map<String, com.example.tradeupapp.models.ItemModel> itemMap = new java.util.HashMap<>();

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
        recyclerView = view.findViewById(R.id.recycler_view_purchase_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PurchaseHistoryAdapter(requireContext(), transactionList, this, new com.example.tradeupapp.core.services.ItemRepository(), listingMap, itemMap);
        recyclerView.setAdapter(adapter);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        firebaseService = FirebaseService.getInstance();
        itemRepository = new com.example.tradeupapp.core.services.ItemRepository();
        loadPurchaseTransactions();
    }

    private void loadPurchaseTransactions() {
        String currentUserId = com.example.tradeupapp.core.session.UserSession.getInstance().getCurrentUser().getUserIdOrUid();
        if (currentUserId == null) {
            showPurchaseTransactions(new ArrayList<>());
            return;
        }
        firebaseService.getTransactionsByUserId(currentUserId, new FirebaseService.TransactionsCallback() {
            @Override
            public void onSuccess(List<TransactionModel> transactions) {
                transactionList.clear();
                transactionList.addAll(transactions);
                if (transactions.isEmpty()) {
                    showPurchaseTransactions(transactions);
                    return;
                }
                // Lấy danh sách listingId
                List<String> listingIds = new ArrayList<>();
                for (TransactionModel txn : transactions) {
                    if (txn.getListingId() != null) listingIds.add(txn.getListingId());
                }
                firebaseService.getListingsByIds(listingIds, new FirebaseService.ListingsCallback() {
                    @Override
                    public void onSuccess(List<ListingModel> listings) {
                        listingMap.clear();
                        List<String> itemIds = new ArrayList<>();
                        for (ListingModel listing : listings) {
                            listingMap.put(listing.getId(), listing);
                            if (listing.getItemId() != null) itemIds.add(listing.getItemId());
                        }
                        firebaseService.getItemsByIds(itemIds, new FirebaseService.ItemsByIdsCallback() {
                            @Override
                            public void onSuccess(java.util.Map<String, com.example.tradeupapp.models.ItemModel> items) {
                                itemMap.clear();
                                itemMap.putAll(items);
                                adapter.notifyDataSetChanged();
                                showPurchaseTransactions(transactions);
                            }
                            @Override
                            public void onError(String error) {
                                adapter.notifyDataSetChanged();
                                showPurchaseTransactions(transactions);
                            }
                        });
                    }
                    @Override
                    public void onError(String error) {
                        adapter.notifyDataSetChanged();
                        showPurchaseTransactions(transactions);
                    }
                });
            }
            @Override
            public void onError(String error) {
                showPurchaseTransactions(new ArrayList<>());
            }
        });
    }

    private void showPurchaseTransactions(List<TransactionModel> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
            adapter.updateTransactions(transactions);
        }
    }

    // PurchaseHistoryAdapter.OnItemClickListener implementation
    @Override
    public void onItemClick(String listingId) {
        // Fetch ListingModel to get itemId, then navigate
        itemRepository.getListingById(listingId, listing -> {
            if (listing != null && listing.getItemId() != null) {
                Bundle args = new Bundle();
                args.putString("itemId", listing.getItemId());
                Navigation.findNavController(requireView()).navigate(R.id.action_purchaseHistoryFragment_to_itemDetailFragment, args);
            } else {
                Toast.makeText(getContext(), "Listing not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReorderClick(String listingId) {
        itemRepository.getListingById(listingId, listing -> {
            if (listing != null && listing.getItemId() != null) {
                Bundle args = new Bundle();
                args.putString("itemId", listing.getItemId());
                Toast.makeText(getContext(), "Redirecting to original listing...", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigate(R.id.action_purchaseHistoryFragment_to_itemDetailFragment, args);
            } else {
                Toast.makeText(getContext(), "Listing not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReviewClick(String listingId) {
        Toast.makeText(getContext(), "Review feature not implemented yet.", Toast.LENGTH_SHORT).show();
    }
}
