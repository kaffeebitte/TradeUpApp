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
    private ReviewProductsAdapter adapter;
    private List<ListingModel> listings;
    private Map<String, ItemModel> itemMap;
    private Map<String, User> userMap;
    private String revieweeRole; // "seller" or "buyer"

    private List<String> sellerIds;
    private List<String> listingIds;

    private List<String> bannedWords = new ArrayList<>();

    private void fetchBannedWords() {
        FirebaseService.getInstance().getBannedWords(new FirebaseService.BannedWordsCallback() {
            @Override
            public void onSuccess(List<String> words) {
                bannedWords = words;
            }
            @Override
            public void onError(String error) {
                bannedWords = new ArrayList<>(); // fallback to empty
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fetchBannedWords();
        if (getArguments() != null) {
            String[] listingArr = getArguments().getStringArray("listingIds");
            String[] sellerArr = getArguments().getStringArray("sellerIds");
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
        Button btnSubmit = view.findViewById(R.id.btn_submit_reviews);
        Button btnExit = view.findViewById(R.id.btn_exit_review);
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        fetchDataAndSetupAdapter();
        btnSubmit.setOnClickListener(v -> submitReviews());
        btnExit.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        return view;
    }

    private void fetchDataAndSetupAdapter() {
        if (listingIds == null || listingIds.isEmpty()) return;
        FirebaseService service = FirebaseService.getInstance();
        service.getListingsByIds(listingIds, new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(List<ListingModel> fetchedListings) {
                // Defensive: filter out nulls and only keep listings with itemId and sellerId
                List<ListingModel> validListings = new ArrayList<>();
                List<String> itemIds = new ArrayList<>();
                for (ListingModel l : fetchedListings) {
                    if (l != null && l.getItemId() != null && l.getSellerId() != null) {
                        validListings.add(l);
                        itemIds.add(l.getItemId());
                    }
                }
                listings = validListings;
                if (listings.isEmpty()) {
                    Toast.makeText(getContext(), "No products to review.", Toast.LENGTH_SHORT).show();
                    return;
                }
                service.getItemsByIds(itemIds, new FirebaseService.ItemsByIdsCallback() {
                    @Override
                    public void onSuccess(Map<String, ItemModel> fetchedItemMap) {
                        itemMap = fetchedItemMap != null ? fetchedItemMap : new HashMap<>();
                        service.getUsersByIds(sellerIds, new FirebaseService.UsersByIdsCallback() {
                            @Override
                            public void onSuccess(Map<String, User> fetchedUserMap) {
                                userMap = fetchedUserMap != null ? fetchedUserMap : new HashMap<>();
                                if (getContext() != null && rvProducts != null) {
                                    adapter = new ReviewProductsAdapter(getContext(), listings, itemMap, userMap, revieweeRole);
                                    rvProducts.setAdapter(adapter);
                                }
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

    private boolean containsAbusiveContent(String comment) {
        if (comment == null) return false;
        String lower = comment.toLowerCase();
        for (String word : bannedWords) {
            if (lower.contains(word)) return true;
        }
        return false;
    }

    private void submitReviews() {
        // Always get latest input from visible views
        Map<String, ReviewProductsAdapter.ReviewInput> inputs = adapter.getReviewInputs(rvProducts);
        String fromUserId = UserSession.getInstance().getId();
        List<ReviewModel> reviews = new ArrayList<>();
        for (ListingModel listing : listings) {
            ReviewProductsAdapter.ReviewInput input = inputs.get(listing.getId());
            if (input == null || input.rating < 1) {
                Toast.makeText(getContext(), "Please rate all products before submitting.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (containsAbusiveContent(input.comment)) {
                Toast.makeText(getContext(), "Your review contains inappropriate language. Please revise your comment.", Toast.LENGTH_LONG).show();
                return;
            }
            String revieweeId = listing.getSellerId();
            String txnId = null; // Not used, can be set if needed
            ReviewModel review = new ReviewModel(
                    txnId,
                    fromUserId,
                    revieweeId,
                    listing.getId(),
                    input.rating,
                    input.comment
            );
            review.setVerified(true); // Only allow verified reviews to be submitted
            reviews.add(review);
        }
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
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }
}
