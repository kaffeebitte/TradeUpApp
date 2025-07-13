package com.example.tradeupapp.features.payment.ui;

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
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.core.session.UserSession;
import com.example.tradeupapp.models.OfferModel;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.shared.adapters.OfferAdapter;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OfferHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private MaterialTextView emptyStateTextView;

    private FirebaseService firebaseService;
    private OfferAdapter offerAdapter;
    private final List<OfferModel> offerList = new ArrayList<>();
    private final Map<String, ListingModel> listingMap = new HashMap<>();
    private final Map<String, ItemModel> itemMap = new HashMap<>();

    public static OfferHistoryFragment newInstance() {
        return new OfferHistoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_offer_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view);
        emptyStateTextView = view.findViewById(R.id.empty_state_text);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        firebaseService = FirebaseService.getInstance();
        offerAdapter = new OfferAdapter(requireContext(), offerList, listingMap, itemMap, null);
        recyclerView.setAdapter(offerAdapter);

        loadOffersOfCurrentSeller();
    }

    private void loadOffersOfCurrentSeller() {
        String currentUserId = UserSession.getInstance().getCurrentUser().getUserIdOrUid();
        if (currentUserId == null) {
            showEmptyState();
            return;
        }
        firebaseService.getOffersBySellerId(currentUserId, new FirebaseService.OffersCallback() {
            @Override
            public void onSuccess(List<OfferModel> offers) {
                offerList.clear();
                offerList.addAll(offers);
                if (offers.isEmpty()) {
                    showEmptyState();
                    return;
                }
                // Lấy danh sách listingId và itemId từ các offer
                List<String> listingIds = new ArrayList<>();
                List<String> itemIds = new ArrayList<>();
                for (OfferModel offer : offers) {
                    if (offer.getListingId() != null) listingIds.add(offer.getListingId());
                }
                // Lấy thông tin listing
                firebaseService.getListingsByIds(listingIds, new FirebaseService.ListingsCallback() {
                    @Override
                    public void onSuccess(List<ListingModel> listings) {
                        listingMap.clear();
                        for (ListingModel listing : listings) {
                            listingMap.put(listing.getId(), listing);
                            if (listing.getItemId() != null) itemIds.add(listing.getItemId());
                        }
                        // Lấy thông tin item
                        firebaseService.getItemsByIds(itemIds, new FirebaseService.ItemsByIdsCallback() {
                            @Override
                            public void onSuccess(Map<String, ItemModel> items) {
                                itemMap.clear();
                                itemMap.putAll(items);
                                offerAdapter.notifyDataSetChanged();
                                showContent();
                            }
                            @Override
                            public void onError(String error) {
                                offerAdapter.notifyDataSetChanged();
                                showContent();
                            }
                        });
                    }
                    @Override
                    public void onError(String error) {
                        offerAdapter.notifyDataSetChanged();
                        showContent();
                    }
                });
            }
            @Override
            public void onError(String error) {
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.VISIBLE);
        emptyStateTextView.setText(R.string.no_offers);
    }

    private void showContent() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateTextView.setVisibility(View.GONE);
    }
}
