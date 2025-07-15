package com.example.tradeupapp.features.review;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.core.session.UserSession;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.models.ReviewModel;
import com.example.tradeupapp.models.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewProductsFragment extends Fragment {
    private RecyclerView rvProducts;
    private Button btnSubmit;
    private ReviewProductsAdapter adapter;
    private List<ListingModel> listings;
    private Map<String, ItemModel> itemMap;
    private Map<String, User> userMap;
    private String transactionId;
    private String revieweeRole; // "seller" or "buyer"
    private String toUserId;

    private List<String> transactionIds;
    private List<String> sellerIds;
    private List<String> listingIds;
    private Map<String, String> listingIdToTransactionId;
    private Map<String, String> listingIdToSellerId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String[] txnArr = getArguments().getStringArray("transactionIds");
            String[] listingArr = getArguments().getStringArray("listingIds");
            String[] sellerArr = getArguments().getStringArray("sellerIds");
            transactionIds = txnArr != null ? Arrays.asList(txnArr) : new ArrayList<>();
            listingIds = listingArr != null ? Arrays.asList(listingArr) : new ArrayList<>();
            sellerIds = sellerArr != null ? Arrays.asList(sellerArr) : new ArrayList<>();
            revieweeRole = getArguments().getString("revieweeRole", "seller");
        }
        listings = new ArrayList<>();
        itemMap = new HashMap<>();
        userMap = new HashMap<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_products, container, false);
        rvProducts = view.findViewById(R.id.rv_review_products);
        btnSubmit = view.findViewById(R.id.btn_submit_reviews);
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        fetchDataAndSetupAdapter();
        btnSubmit.setOnClickListener(v -> submitReviews());
        return view;
    }

    private void fetchDataAndSetupAdapter() {
        if (listingIds == null || listingIds.isEmpty()) return;
        FirebaseService service = FirebaseService.getInstance();
        service.getListingsByIds(listingIds, new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(List<ListingModel> fetchedListings) {
                listings = fetchedListings;
                List<String> itemIds = new ArrayList<>();
                for (ListingModel l : listings) itemIds.add(l.getItemId());
                service.getItemsByIds(itemIds, new FirebaseService.ItemsByIdsCallback() {
                    @Override
                    public void onSuccess(Map<String, ItemModel> fetchedItemMap) {
                        itemMap = fetchedItemMap;
                        service.getUsersByIds(sellerIds, new FirebaseService.UsersByIdsCallback() {
                            @Override
                            public void onSuccess(Map<String, User> fetchedUserMap) {
                                userMap = fetchedUserMap;
                                adapter = new ReviewProductsAdapter(getContext(), listings, itemMap, userMap, revieweeRole);
                                rvProducts.setAdapter(adapter);
                            }
                            @Override
                            public void onError(String error) {
                                Toast.makeText(getContext(), "Failed to load sellers for review.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), "Failed to load items for review.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Failed to load listings for review.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitReviews() {
        // Always get latest input from visible views
        Map<String, ReviewProductsAdapter.ReviewInput> inputs = adapter.getReviewInputs(rvProducts);
        // Lấy user hiện tại từ UserSession
        String fromUserId = UserSession.getInstance().getId();
        List<ReviewModel> reviews = new ArrayList<>();
        for (ListingModel listing : listings) {
            ReviewProductsAdapter.ReviewInput input = inputs.get(listing.getId());
            if (input == null || input.rating < 1) {
                Toast.makeText(getContext(), "Please rate all products before submitting.", Toast.LENGTH_SHORT).show();
                return;
            }
            String revieweeId = null;
            String txnId = null;
            // Nếu reviewee là seller thì lấy từ listing, còn reviewer luôn là user hiện tại
            if (listingIdToSellerId != null && listingIdToSellerId.containsKey(listing.getId())) {
                revieweeId = listingIdToSellerId.get(listing.getId());
            } else if (toUserId != null) {
                revieweeId = toUserId;
            } else if (listing.getSellerId() != null) {
                revieweeId = listing.getSellerId();
            }
            if (listingIdToTransactionId != null && listingIdToTransactionId.containsKey(listing.getId())) {
                txnId = listingIdToTransactionId.get(listing.getId());
            } else if (transactionId != null) {
                txnId = transactionId;
            }
            ReviewModel review = new ReviewModel(
                    txnId,
                    fromUserId,
                    revieweeId,
                    listing.getId(),
                    input.rating,
                    input.comment // Lưu comment từ EditText
            );
            reviews.add(review);
        }
        // Save all reviews to Firestore
        FirebaseService service = FirebaseService.getInstance();
        for (ReviewModel review : reviews) {
            service.addReview(review, new FirebaseService.SimpleCallback() {
                @Override
                public void onSuccess() {
                    // Optionally show success
                }
                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Failed to submit review: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
        Toast.makeText(getContext(), "Thank you for your reviews!", Toast.LENGTH_SHORT).show();
        // Optionally close fragment or navigate away
        requireActivity().onBackPressed();
    }
}
