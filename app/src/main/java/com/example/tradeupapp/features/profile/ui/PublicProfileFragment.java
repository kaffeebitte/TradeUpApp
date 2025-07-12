package com.example.tradeupapp.features.profile.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tradeupapp.models.User;
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.R;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.features.listing.adapter.UserListingsAdapter;
import com.google.firebase.firestore.GeoPoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicProfileFragment extends Fragment {
    private String userId;
    private TextView tvName, tvLocation, tvRating, tvReviewsCount, tvBio;
    private TextView tvEmail, tvPhone, tvListingsCount;
    private ImageView ivAvatar;
    private FirebaseService firebaseService;
    private RecyclerView rvUserListings;
    private UserListingsAdapter listingsAdapter;
    private List<ListingModel> userListings = new ArrayList<>();
    private Map<String, ItemModel> itemMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_public_profile, container, false);
        tvName = view.findViewById(R.id.tv_name);
        tvLocation = view.findViewById(R.id.tv_location);
        tvRating = view.findViewById(R.id.tv_rating);
        tvReviewsCount = view.findViewById(R.id.tv_reviews_count);
        tvBio = view.findViewById(R.id.tv_bio);
        tvEmail = view.findViewById(R.id.tv_email);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvListingsCount = view.findViewById(R.id.tv_listings_count);
        ivAvatar = view.findViewById(R.id.iv_avatar);
        rvUserListings = view.findViewById(R.id.rv_user_listings);
        firebaseService = FirebaseService.getInstance();
        listingsAdapter = new UserListingsAdapter(userListings);
        rvUserListings.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvUserListings.setAdapter(listingsAdapter);
        if (getArguments() != null && getArguments().containsKey("userId")) {
            userId = getArguments().getString("userId");
            loadUserProfile(userId);
        }
        return view;
    }

    private void loadUserProfile(String userId) {
        firebaseService.getUserById(userId, new FirebaseService.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (getActivity() == null || !isAdded()) return;
                tvName.setText(user.getDisplayName());
                tvBio.setText(user.getBio() != null ? user.getBio() : "");
                // Convert GeoPoint to string for display
                GeoPoint geoPoint = null;
                try {
                    geoPoint = (GeoPoint) user.getLocation();
                } catch (Exception ignored) {}
                if (geoPoint != null) {
                    String locationStr = String.format("%.5f, %.5f", geoPoint.getLatitude(), geoPoint.getLongitude());
                    tvLocation.setText(locationStr);
                } else if (user.getLocation() != null) {
                    tvLocation.setText(user.getLocation().toString());
                } else {
                    tvLocation.setText("");
                }
                tvRating.setText(String.format("%.1f", user.getRating()));
                tvReviewsCount.setText(String.format("(%d)", user.getTotalReviews()));
                tvEmail.setText(user.getEmail() != null ? user.getEmail() : "-");
                tvPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "-");
                if (isValidUrl(user.getPhotoUrl())) {
                    Glide.with(requireContext())
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.ic_user_24)
                        .error(R.drawable.ic_user_24)
                        .into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_user_24);
                }
                loadUserListings(userId);
            }
            @Override
            public void onError(String error) {
                if (getActivity() == null || !isAdded()) return;
                tvName.setText("Unknown User");
                tvBio.setText("");
                tvLocation.setText("");
                tvRating.setText("");
                tvReviewsCount.setText("");
                tvEmail.setText("-");
                tvPhone.setText("-");
                ivAvatar.setImageResource(R.drawable.ic_user_24);
            }
        });
    }

    private void loadUserListings(String sellerId) {
        firebaseService.getListingsBySellerId(sellerId, new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(List<ListingModel> listings) {
                userListings.clear();
                userListings.addAll(listings);
                tvListingsCount.setText(String.valueOf(listings.size()));
                if (listings.isEmpty()) {
                    listingsAdapter.setItemMap(new HashMap<>());
                    listingsAdapter.notifyDataSetChanged();
                    return;
                }
                // Fetch all related items by itemId
                List<String> itemIds = new ArrayList<>();
                for (ListingModel listing : listings) {
                    if (listing.getItemId() != null) itemIds.add(listing.getItemId());
                }
                firebaseService.getItemsByIds(itemIds, new FirebaseService.ItemsByIdsCallback() {
                    @Override
                    public void onSuccess(Map<String, ItemModel> itemMap) {
                        listingsAdapter.setItemMap(itemMap);
                        listingsAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onError(String error) {
                        listingsAdapter.setItemMap(new HashMap<>());
                        listingsAdapter.notifyDataSetChanged();
                    }
                });
            }
            @Override
            public void onError(String error) {
                listingsAdapter.setItemMap(new HashMap<>());
                listingsAdapter.notifyDataSetChanged();
            }
        });
    }

    private boolean isValidUrl(String url) {
        return url != null && url.startsWith("http");
    }
}
