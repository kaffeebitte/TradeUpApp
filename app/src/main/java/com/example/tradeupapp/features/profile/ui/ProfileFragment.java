package com.example.tradeupapp.features.profile.ui;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tradeupapp.R;
import com.example.tradeupapp.features.auth.ui.AuthActivity;
import com.example.tradeupapp.features.auth.utils.LogoutManager;
import com.example.tradeupapp.models.User;
import com.example.tradeupapp.shared.auth.UserManager;
import com.example.tradeupapp.shared.auth.UserRole;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    // UI Components - Profile section
    private ShapeableImageView profileImageView;
    private TextView usernameTextView;
    private TextView userEmailTextView;
    private TextView userRatingTextView;
    private MaterialButton editProfileButton;
    private Button logoutButton;

    // Bio and contact UI components
    private TextView userBioTextView;
    private TextView userBioEmptyTextView;
    private TextView userLocationTextView;
    private TextView userPhoneTextView;
    private TextView userEmailContactTextView;
    private TextView userPreferredContactTextView;

    // Firebase and services
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private LogoutManager logoutManager;
    private UserManager userManager;

    private User currentUser; // Add field to store the User object

    // Activity result launcher for handling return from EditProfileActivity
    private final ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    // Get updated user data
                    User updatedUser = (User) result.getData().getSerializableExtra("updatedUser");
                    if (updatedUser != null) {
                        // Update local user object
                        currentUser = updatedUser;
                        // Update UI with the new data
                        updateUIWithUserData(currentUser);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize managers
        logoutManager = LogoutManager.getInstance();
        logoutManager.initializeGoogleSignIn(requireContext());
        userManager = UserManager.getInstance(requireContext());

        // Initialize views
        initViews(view);

        // Set up user profile data
        setupUserProfile();

        // Set up click listeners
        setupClickListeners();

        // Check if user is admin
        checkAdminStatus();
    }

    private void initViews(View view) {
        // Profile header section
        profileImageView = view.findViewById(R.id.iv_profile_avatar);
        usernameTextView = view.findViewById(R.id.tv_user_name);
        userEmailTextView = view.findViewById(R.id.tv_member_since);

        // Stats section
        TextView itemsCountTextView = view.findViewById(R.id.tv_items_count);
        TextView reviewsCountTextView = view.findViewById(R.id.tv_reviews_count);
        userRatingTextView = view.findViewById(R.id.tv_rating);

        // Bio and contact views
        userBioTextView = view.findViewById(R.id.tv_user_bio);
        userBioEmptyTextView = view.findViewById(R.id.tv_bio_empty);
        userLocationTextView = view.findViewById(R.id.tv_user_location);
        userPhoneTextView = view.findViewById(R.id.tv_user_phone);
        userEmailContactTextView = view.findViewById(R.id.tv_user_email);
        userPreferredContactTextView = view.findViewById(R.id.tv_preferred_contact);

        // Quick action cards - these are MaterialCardView, not MaterialButton
        MaterialCardView myListingsCard = view.findViewById(R.id.card_my_listings);
        MaterialCardView purchasesCard = view.findViewById(R.id.card_purchases);

        // Menu cards - these are MaterialCardView, not MaterialButton
        MaterialCardView editProfileCard = view.findViewById(R.id.card_edit_profile);
        MaterialCardView paymentMethodsCard = view.findViewById(R.id.card_payment_methods);
        MaterialCardView addressesCard = view.findViewById(R.id.card_addresses);
        MaterialCardView notificationsCard = view.findViewById(R.id.card_notifications);
        MaterialCardView privacyCard = view.findViewById(R.id.card_privacy);
        MaterialCardView helpCard = view.findViewById(R.id.card_help);

        // Sign out button - this is MaterialButton
        logoutButton = view.findViewById(R.id.btn_sign_out);

        // Setup click listeners for all cards
        setupNewClickListeners(myListingsCard, purchasesCard, editProfileCard,
                paymentMethodsCard, addressesCard, notificationsCard,
                privacyCard, helpCard);
    }

    private void setupNewClickListeners(MaterialCardView myListingsCard, MaterialCardView purchasesCard,
                                       MaterialCardView editProfileCard, MaterialCardView paymentMethodsCard,
                                       MaterialCardView addressesCard, MaterialCardView notificationsCard,
                                       MaterialCardView privacyCard, MaterialCardView helpCard) {

        // Quick actions
        myListingsCard.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_profile_to_myListingsFragment);
        });

        purchasesCard.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_profile_to_purchaseHistoryFragment);
        });

        // Account section
        editProfileCard.setOnClickListener(v -> {
            if (currentUser != null) {
                NavController navController = NavHostFragment.findNavController(this);
                Bundle args = new Bundle();
                args.putSerializable("user", currentUser);
                navController.navigate(R.id.action_nav_profile_to_editProfileFragment, args);
            } else {
                Toast.makeText(requireContext(), "Loading user data, please try again later", Toast.LENGTH_SHORT).show();
            }
        });

        paymentMethodsCard.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Payment methods coming soon", Toast.LENGTH_SHORT).show();
        });

        addressesCard.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Shipping addresses coming soon", Toast.LENGTH_SHORT).show();
        });

        // Settings section
        notificationsCard.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Notifications settings coming soon", Toast.LENGTH_SHORT).show();
        });

        privacyCard.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_profile_to_accountPrivacyFragment);
        });

        helpCard.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Help & Support coming soon", Toast.LENGTH_SHORT).show();
        });

        // Sign out
        logoutButton.setOnClickListener(v -> showLogoutDialog());
    }

    private void setupUserProfile() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            // Display basic authentication info first
            String displayName = firebaseUser.getDisplayName();
            String email = firebaseUser.getEmail();

            if (displayName != null && !displayName.isEmpty()) {
                usernameTextView.setText(displayName);
            } else {
                usernameTextView.setText("User");
            }

            if (email != null) {
                userEmailTextView.setText(email);
            }

            // Show loading state or basic verification status initially
            if (!firebaseUser.isEmailVerified() && !isGoogleUser(firebaseUser)) {
                userRatingTextView.setText("⚠️ Email not verified. Please check your inbox.");
            }

            // Fetch detailed user data from Firestore
            String uid = firebaseUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            // Convert document to User object
                            currentUser = document.toObject(User.class);
                            if (currentUser != null) {
                                // Update UI with complete user data
                                updateUIWithUserData(currentUser);

                                // Load role-specific data
                                UserRole currentRole = userManager.getUserRole();
                                if (currentRole == UserRole.BUYER) {
                                    loadUserStats();
                                } else {
                                    loadUserStats();
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Could not load profile information", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Handle not logged in state
            navigateToLogin();
        }
    }

    private boolean isGoogleUser(FirebaseUser user) {
        if (user.getProviderData() != null) {
            for (com.google.firebase.auth.UserInfo userInfo : user.getProviderData()) {
                if ("google.com".equals(userInfo.getProviderId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkAdminStatus() {
        // Admin functionality có thể implement sau
        // Layout mới không có adminDashboardButton
    }

    private void setupClickListeners() {
        // Chỉ giữ lại phần logout button click listener
        logoutButton.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadUserStats() {
        if (currentUser != null && auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();

            // Load basic stats to display in header
            TextView itemsCount = getView().findViewById(R.id.tv_items_count);
            TextView reviewsCount = getView().findViewById(R.id.tv_reviews_count);

            // Count user's active listings
            db.collection("items")
                .whereEqualTo("sellerId", uid)
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (itemsCount != null) {
                        itemsCount.setText(String.valueOf(querySnapshot.size()));
                    }
                });

            // Count reviews received
            db.collection("reviews")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (reviewsCount != null) {
                        reviewsCount.setText(String.valueOf(querySnapshot.size()));
                    }
                });
        }
    }

    private void navigateToLogin() {
        // Navigate to login screen
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        logoutManager.performLogout(requireContext(), new LogoutManager.LogoutCallback() {
            @Override
            public void onLogoutComplete() {
                Toast.makeText(requireContext(), "Successfully logged out", Toast.LENGTH_SHORT).show();

                // Return to login screen
                Intent intent = new Intent(requireContext(), AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    /**
     * Updates the UI with user data
     */
    private void updateUIWithUserData(User user) {
        if (user != null) {
            // Update display name
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                usernameTextView.setText(user.getDisplayName());
            }

            // Update member since info (sử dụng email field tạm thời)
            if (user.getEmail() != null) {
                userEmailTextView.setText("Member since " + user.getEmail());
            }

            // Update rating display
            String ratingText = String.format("%.1f", user.getRating());
            userRatingTextView.setText(ratingText);

            // Load user avatar
            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                try {
                    com.bumptech.glide.Glide.with(requireContext())
                            .load(user.getPhotoUrl())
                            .placeholder(R.drawable.ic_account_circle)
                            .into(profileImageView);
                } catch (Exception e) {
                    // Fallback if Glide fails
                    profileImageView.setImageResource(R.drawable.ic_account_circle);
                }
            }

            // Update bio and contact information
            if (user.getBio() != null && !user.getBio().isEmpty()) {
                userBioTextView.setText(user.getBio());
                userBioTextView.setVisibility(View.VISIBLE);
                userBioEmptyTextView.setVisibility(View.GONE);
            } else {
                userBioTextView.setVisibility(View.GONE);
                userBioEmptyTextView.setVisibility(View.VISIBLE);
            }

            // Xử lý thông tin vị trí (là GeoPoint trong Firestore)
            if (user.getLocation() != null) {
                Object location = user.getLocation();
                if (location instanceof com.google.firebase.firestore.GeoPoint) {
                    com.google.firebase.firestore.GeoPoint geoPoint = (com.google.firebase.firestore.GeoPoint) location;
                    double lat = geoPoint.getLatitude();
                    double lng = geoPoint.getLongitude();
                    // Try to get address string from lat/lng
                    String addressStr = getAddressFromLatLng(lat, lng);
                    if (addressStr != null && !addressStr.isEmpty()) {
                        userLocationTextView.setText(addressStr);
                    } else {
                        userLocationTextView.setText(String.format("%.6f, %.6f", lat, lng));
                    }
                    userLocationTextView.setVisibility(View.VISIBLE);
                } else if (location instanceof String) {
                    userLocationTextView.setText((String) location);
                    userLocationTextView.setVisibility(View.VISIBLE);
                } else {
                    userLocationTextView.setVisibility(View.GONE);
                }
            } else {
                userLocationTextView.setVisibility(View.GONE);
            }

            // Sử dụng phoneNumber thay vì phone
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                userPhoneTextView.setText(user.getPhoneNumber());
                userPhoneTextView.setVisibility(View.VISIBLE);
            } else {
                userPhoneTextView.setVisibility(View.GONE);
            }

            if (user.getEmail() != null) {
                userEmailContactTextView.setText(user.getEmail());
                userEmailContactTextView.setVisibility(View.VISIBLE);
            } else {
                userEmailContactTextView.setVisibility(View.GONE);
            }

            // Mặc định hiển thị "In-app messaging" cho phương thức liên hệ ưa thích
            // vì User chưa có trường preferredContactMethod
            userPreferredContactTextView.setText("In-app messaging");
            userPreferredContactTextView.setVisibility(View.VISIBLE);
        }
    }

    // Helper: Convert lat/lng to address string
    private String getAddressFromLatLng(double latitude, double longitude) {
        if (!isAdded() || getContext() == null) return null;
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            // Ignore and fallback
        }
        return null;
    }
}
