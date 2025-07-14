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
    private View emptyStateContainer;

    private FirebaseService firebaseService;
    private OfferHistoryAdapter offerHistoryAdapter;
    private final List<OfferModel> offerList = new ArrayList<>();
    private final Map<String, ListingModel> listingMap = new HashMap<>();
    private final Map<String, ItemModel> itemMap = new HashMap<>();

    public static final String ARG_MODE = "mode"; // "seller" or "buyer"

    public static OfferHistoryFragment newInstance(String mode) {
        OfferHistoryFragment fragment = new OfferHistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_offer_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view_offer_history);
        emptyStateTextView = view.findViewById(R.id.empty_state_text);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        firebaseService = FirebaseService.getInstance();
        offerHistoryAdapter = new OfferHistoryAdapter(requireContext(), offerList, listingMap, itemMap);
        recyclerView.setAdapter(offerHistoryAdapter);
        String mode = getArguments() != null ? getArguments().getString(ARG_MODE, "seller") : "seller";
        if ("buyer".equals(mode)) {
            loadOffersOfCurrentBuyer();
        } else {
            loadOffersOfCurrentSeller();
        }
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
                                offerHistoryAdapter.notifyDataSetChanged();
                                showContent();
                            }
                            @Override
                            public void onError(String error) {
                                offerHistoryAdapter.notifyDataSetChanged();
                                showContent();
                            }
                        });
                    }
                    @Override
                    public void onError(String error) {
                        offerHistoryAdapter.notifyDataSetChanged();
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

    private void loadOffersOfCurrentBuyer() {
        String currentUserId = UserSession.getInstance().getCurrentUser().getUserIdOrUid();
        if (currentUserId == null) {
            showEmptyState();
            return;
        }
        // Lấy tất cả offer của buyer này
        firebaseService.getOffersByBuyerId(currentUserId, new FirebaseService.OffersCallback() {
            @Override
            public void onSuccess(List<OfferModel> offers) {
                offerList.clear();
                offerList.addAll(offers);
                if (offers.isEmpty()) {
                    showEmptyState();
                    return;
                }
                // Lấy tất cả listingId từ offer
                List<String> listingIds = new ArrayList<>();
                for (OfferModel offer : offers) {
                    if (offer.getListingId() != null) listingIds.add(offer.getListingId());
                }
                if (listingIds.isEmpty()) {
                    offerHistoryAdapter.notifyDataSetChanged();
                    showContent();
                    return;
                }
                // Lấy thông tin listing
                firebaseService.getListingsByIds(listingIds, new FirebaseService.ListingsCallback() {
                    @Override
                    public void onSuccess(List<ListingModel> listings) {
                        listingMap.clear();
                        List<String> itemIds = new ArrayList<>();
                        for (ListingModel listing : listings) {
                            listingMap.put(listing.getId(), listing);
                            if (listing.getItemId() != null) itemIds.add(listing.getItemId());
                        }
                        if (itemIds.isEmpty()) {
                            offerHistoryAdapter.notifyDataSetChanged();
                            showContent();
                            return;
                        }
                        // Lấy thông tin item
                        firebaseService.getItemsByIds(itemIds, new FirebaseService.ItemsByIdsCallback() {
                            @Override
                            public void onSuccess(Map<String, ItemModel> items) {
                                itemMap.clear();
                                itemMap.putAll(items);
                                offerHistoryAdapter.notifyDataSetChanged();
                                showContent();
                            }
                            @Override
                            public void onError(String error) {
                                offerHistoryAdapter.notifyDataSetChanged();
                                showContent();
                            }
                        });
                    }
                    @Override
                    public void onError(String error) {
                        offerHistoryAdapter.notifyDataSetChanged();
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
        if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.VISIBLE);
        if (emptyStateTextView != null) emptyStateTextView.setText(R.string.no_offers);
    }

    private void showContent() {
        recyclerView.setVisibility(View.VISIBLE);
        if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.GONE);
    }
}
