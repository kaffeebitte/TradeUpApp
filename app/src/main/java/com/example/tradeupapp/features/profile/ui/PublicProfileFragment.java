package com.example.tradeupapp.features.profile.ui;

import android.app.AlertDialog;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.GeoPoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.Toast;

public class PublicProfileFragment extends Fragment {
    private String userId;
    private TextView tvName, tvLocation, tvRating, tvBio, tvMemberSince;
    private TextView tvEmail, tvPhone, tvListingsCount, tvTotalTransactions;
    private ImageView ivAvatar;
    private FirebaseService firebaseService;
    private RecyclerView rvUserListings;
    private UserListingsAdapter listingsAdapter;
    private List<ListingModel> userListings = new ArrayList<>();
    private Map<String, ItemModel> itemMap = new HashMap<>();
    private TextView reviewCountTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_public_profile, container, false);
        tvName = view.findViewById(R.id.tv_name);
        tvLocation = view.findViewById(R.id.tv_location);
        tvRating = view.findViewById(R.id.tv_rating);
        reviewCountTextView = view.findViewById(R.id.tv_review_count);
        tvBio = view.findViewById(R.id.tv_bio);
        tvEmail = view.findViewById(R.id.tv_email);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvListingsCount = view.findViewById(R.id.tv_listings_count);
        tvTotalTransactions = view.findViewById(R.id.tv_total_transactions);
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvMemberSince = view.findViewById(R.id.tv_member_since);
        rvUserListings = view.findViewById(R.id.rv_user_listings);
        firebaseService = FirebaseService.getInstance();
        listingsAdapter = new UserListingsAdapter(userListings);
        listingsAdapter.setOnItemClickListener(new UserListingsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ListingModel listing) {
                androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(requireView());
                Bundle args = new Bundle();
                args.putString("listingId", listing.getId());
                args.putString("itemId", listing.getItemId());
                navController.navigate(R.id.itemDetailFragment, args);
            }
        });
        rvUserListings.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(getContext(), 2));
        rvUserListings.setAdapter(listingsAdapter);
        if (getArguments() != null && getArguments().containsKey("userId")) {
            userId = getArguments().getString("userId");
            loadUserProfile(userId);
        }
        // Report Profile Button
        View btnReportProfile = view.findViewById(R.id.btn_report_profile);
        btnReportProfile.setOnClickListener(v -> showReportProfileDialog());
        return view;
    }

    private void loadUserProfile(String userId) {
        firebaseService.getUserById(userId, new FirebaseService.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (getActivity() == null || !isAdded()) return;
                tvName.setText(user.getDisplayName());
                tvBio.setText(user.getBio() != null ? user.getBio() : "");
                // Member since
                if (user.getCreatedAt() != null) {
                    java.util.Date createdDate = user.getCreatedAt().toDate();
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    calendar.setTime(createdDate);
                    int year = calendar.get(java.util.Calendar.YEAR);
                    tvMemberSince.setText("Member since " + year);
                    tvMemberSince.setVisibility(View.VISIBLE);
                } else {
                    tvMemberSince.setVisibility(View.GONE);
                }
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
                // Show average rating and review count
                loadAverageRatingAndReviewCount(userId);
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
                loadTotalTransactions(userId);
            }
            @Override
            public void onError(String error) {
                if (getActivity() == null || !isAdded()) return;
                tvName.setText("Unknown User");
                tvBio.setText("");
                tvLocation.setText("");
                tvRating.setText("");
                tvEmail.setText("-");
                tvPhone.setText("-");
                ivAvatar.setImageResource(R.drawable.ic_user_24);
            }
        });
    }

    // Only count transactions where user is the buyer
    private void loadTotalTransactions(String userId) {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("transactions")
            .whereEqualTo("buyerId", userId)
            .get()
            .addOnSuccessListener(buyerSnapshot -> {
                int totalTransactions = buyerSnapshot.size();
                if (tvTotalTransactions != null) {
                    tvTotalTransactions.setText(String.valueOf(totalTransactions));
                }
            });
    }

    // Add this method to load average rating and review count for public profile
    private void loadAverageRatingAndReviewCount(String userId) {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("reviews")
            .whereEqualTo("revieweeId", userId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                double totalRating = 0;
                int reviewCount = querySnapshot.size();
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Number rating = doc.getDouble("rating");
                    if (rating != null) {
                        totalRating += rating.doubleValue();
                    }
                }
                double avgRating = reviewCount > 0 ? totalRating / reviewCount : 0.0;
                if (tvRating != null) {
                    tvRating.setText(reviewCount > 0 ? String.format("%.1f", avgRating) : "No rating");
                }
                if (reviewCountTextView != null) {
                    reviewCountTextView.setText(reviewCount > 0 ? String.format("%d", reviewCount) : "No reviews");
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

    private void showReportProfileDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_report_profile, null);
        final android.widget.RadioGroup reasonRadioGroup = dialogView.findViewById(R.id.rg_report_reason);
        final android.widget.EditText descriptionEditText = dialogView.findViewById(R.id.et_report_description);
        reasonRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_other) {
                descriptionEditText.setVisibility(View.VISIBLE);
            } else {
                descriptionEditText.setVisibility(View.GONE);
            }
        });
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Report Profile")
            .setMessage("Select a reason for reporting this profile.")
            .setView(dialogView)
            .setPositiveButton("Report", (dialog, which) -> {
                int checkedId = reasonRadioGroup.getCheckedRadioButtonId();
                String reason = "";
                if (checkedId == R.id.rb_spam) {
                    reason = "Spam";
                } else if (checkedId == R.id.rb_harassment) {
                    reason = "Harassment";
                } else if (checkedId == R.id.rb_scam) {
                    reason = "Scam";
                } else if (checkedId == R.id.rb_inappropriate) {
                    reason = "Inappropriate Content";
                } else if (checkedId == R.id.rb_other) {
                    reason = "Other";
                } else {
                    reason = "Inappropriate profile";
                }
                String description = descriptionEditText.getText().toString().trim();
                if (reason.equals("Other") && description.isEmpty()) {
                    Toast.makeText(requireContext(), "Please describe the issue for 'Other' reason.", Toast.LENGTH_SHORT).show();
                    return;
                }
                submitProfileReport(reason, description);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void submitProfileReport(String reason, String description) {
        String reporterId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";
        Map<String, Object> report = new HashMap<>();
        report.put("type", "profile");
        report.put("reportedUserId", userId);
        report.put("reporterId", reporterId);
        report.put("reason", reason);
        report.put("description", description);
        report.put("timestamp", com.google.firebase.Timestamp.now());
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("reports")
            .add(report)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(requireContext(), "Profile reported. Thank you!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to report profile. Please try again.", Toast.LENGTH_SHORT).show();
            });
    }

    private boolean isValidUrl(String url) {
        return url != null && url.startsWith("http");
    }
}
