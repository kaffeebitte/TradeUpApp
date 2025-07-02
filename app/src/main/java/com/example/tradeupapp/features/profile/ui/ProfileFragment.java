package com.example.tradeupapp.features.profile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    // UI Components - Profile section
    private ShapeableImageView profileImageView;
    private TextView usernameTextView;
    private TextView userEmailTextView;
    private TextView userBioTextView;
    private TextView userRatingTextView;
    private MaterialButton editProfileButton;
    private MaterialButton accountSettingsButton;
    private MaterialButton adminDashboardButton;
    private Button logoutButton;

    // Role switcher
    private TabLayout roleSwitcherTabLayout;

    // Mode containers
    private LinearLayout buyerModeLayout;
    private LinearLayout sellerModeLayout;

    // Buyer mode UI components
    private TextView savedItemsCountTextView;
    private TextView offersCountTextView;
    private TextView purchasesCountTextView;
    private MaterialButton savedItemsButton;
    private MaterialButton purchaseHistoryButton;
    private MaterialButton offerHistoryButton;

    // Seller mode UI components
    private TextView activeListingsCountTextView;
    private TextView offersReceivedCountTextView;
    private TextView salesCountTextView;
    private MaterialButton addListingButton;
    private MaterialButton myListingsButton;
    private MaterialButton salesHistoryButton;

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

        // Set up role switcher
        setupRoleSwitcher();

        // Set up user profile data
        setupUserProfile();

        // Set up click listeners
        setupClickListeners();

        // Check if user is admin
        checkAdminStatus();
    }

    private void initViews(View view) {
        // Profile section
        profileImageView = view.findViewById(R.id.iv_avatar);
        usernameTextView = view.findViewById(R.id.tv_name);
        userEmailTextView = view.findViewById(R.id.tv_email);
        userBioTextView = view.findViewById(R.id.tv_bio);
        userRatingTextView = view.findViewById(R.id.tv_rating);
        editProfileButton = view.findViewById(R.id.btn_edit_profile);
        accountSettingsButton = view.findViewById(R.id.btn_account_settings);
        adminDashboardButton = view.findViewById(R.id.btn_admin_dashboard);
        logoutButton = view.findViewById(R.id.btn_logout);

        // Role switcher
        roleSwitcherTabLayout = view.findViewById(R.id.tab_role_switcher);

        // Mode containers
        buyerModeLayout = view.findViewById(R.id.layout_buyer_mode);
        sellerModeLayout = view.findViewById(R.id.layout_seller_mode);

        // Buyer mode UI components
        savedItemsCountTextView = view.findViewById(R.id.tv_saved_items_count);
        offersCountTextView = view.findViewById(R.id.tv_offers_count);
        purchasesCountTextView = view.findViewById(R.id.tv_purchases_count);
        savedItemsButton = view.findViewById(R.id.btn_saved_items);
        purchaseHistoryButton = view.findViewById(R.id.btn_purchase_history);
        offerHistoryButton = view.findViewById(R.id.btn_offer_history);

        // Seller mode UI components
        activeListingsCountTextView = view.findViewById(R.id.tv_active_listings_count);
        offersReceivedCountTextView = view.findViewById(R.id.tv_offers_received_count);
        salesCountTextView = view.findViewById(R.id.tv_sales_count);
        addListingButton = view.findViewById(R.id.btn_add_listing);
        myListingsButton = view.findViewById(R.id.btn_my_listings);
        salesHistoryButton = view.findViewById(R.id.btn_sales_history);
    }

    private void setupRoleSwitcher() {
        // Initialize tab selection based on current user role
        UserRole currentRole = userManager.getUserRole();
        int initialTab = currentRole == UserRole.SELLER ? 1 : 0;

        // Select the appropriate tab
        TabLayout.Tab tab = roleSwitcherTabLayout.getTabAt(initialTab);
        if (tab != null) {
            tab.select();
        }

        // Initially show the correct mode layout
        updateUIForRole(currentRole);

        // Set up tab selection listener
        roleSwitcherTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                UserRole newRole = tab.getPosition() == 0 ? UserRole.BUYER : UserRole.SELLER;

                // Update user role in UserManager
                userManager.setUserRole(newRole);

                // Update UI to reflect the new role
                updateUIForRole(newRole);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });
    }

    private void updateUIForRole(UserRole role) {
        if (role == UserRole.BUYER) {
            buyerModeLayout.setVisibility(View.VISIBLE);
            sellerModeLayout.setVisibility(View.GONE);

            // Load buyer-specific data
            loadBuyerData();
        } else {
            buyerModeLayout.setVisibility(View.GONE);
            sellerModeLayout.setVisibility(View.VISIBLE);

            // Load seller-specific data
            loadSellerData();
        }
    }

    private void loadBuyerData() {
        // In a real app, these values would be loaded from a database

        if (currentUser != null) {
            // Load data from user object or make additional queries
            String uid = auth.getCurrentUser().getUid();

            // Example: Count saved items
            db.collection("savedItems")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Update UI directly with the result
                    savedItemsCountTextView.setText(String.valueOf(querySnapshot.size()));
                });

            // Example: Count offers
            db.collection("offers")
                .whereEqualTo("buyerId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Update UI directly with the result
                    offersCountTextView.setText(String.valueOf(querySnapshot.size()));
                });

            // Example: Count purchases
            db.collection("transactions")
                .whereEqualTo("buyerId", uid)
                .whereEqualTo("status", "completed")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Update UI directly with the result
                    purchasesCountTextView.setText(String.valueOf(querySnapshot.size()));
                });
        }
    }

    private void loadSellerData() {
        if (currentUser != null) {
            // Load data from user object or make additional queries
            String uid = auth.getCurrentUser().getUid();

            // Example: Count active listings
            db.collection("items")
                .whereEqualTo("sellerId", uid)
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Update UI directly with the result
                    activeListingsCountTextView.setText(String.valueOf(querySnapshot.size()));
                });

            // Example: Count offers received
            db.collection("offers")
                .whereEqualTo("sellerId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Update UI directly with the result
                    offersReceivedCountTextView.setText(String.valueOf(querySnapshot.size()));
                });

            // Example: Count sales
            db.collection("transactions")
                .whereEqualTo("sellerId", uid)
                .whereEqualTo("status", "completed")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Update UI directly with the result
                    salesCountTextView.setText(String.valueOf(querySnapshot.size()));
                });

            // Update rating text
            String ratingText = String.format("%.1f (%d)", currentUser.getRating(), currentUser.getTotalReviews());
            userRatingTextView.setText(ratingText);
        }
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
                userBioTextView.setText("⚠️ Email not verified. Please check your inbox.");
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
                                loadBuyerData();
                            } else {
                                loadSellerData();
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
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                            if (isAdmin != null && isAdmin) {
                                adminDashboardButton.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }
    }

    private void setupClickListeners() {
        // Profile and account section buttons
        editProfileButton.setOnClickListener(v -> {
            if (currentUser != null) {
                // Navigate to edit profile fragment
                NavController navController = NavHostFragment.findNavController(this);
                Bundle args = new Bundle();
                args.putSerializable("user", currentUser);
                navController.navigate(R.id.action_nav_profile_to_editProfileFragment, args);
            } else {
                Toast.makeText(requireContext(), "Loading user data, please try again later", Toast.LENGTH_SHORT).show();
            }
        });

        accountSettingsButton.setOnClickListener(v -> {
            // Navigate to account settings
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_profile_to_accountSettingsFragment);
        });

        adminDashboardButton.setOnClickListener(v -> {
            // Navigate to admin dashboard
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_profile_to_adminDashboardFragment);
        });

        logoutButton.setOnClickListener(v -> showLogoutDialog());

        // Buyer mode buttons
        savedItemsButton.setOnClickListener(v -> {
            // Navigate to saved items
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_profile_to_savedItemsFragment);
        });

        purchaseHistoryButton.setOnClickListener(v -> {
            // Navigate to purchase history
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_profile_to_purchaseHistoryFragment);
        });

        offerHistoryButton.setOnClickListener(v -> {
            // Navigate to offers made
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_profile_to_offerHistoryFragment);
        });

        // Seller mode buttons
        addListingButton.setOnClickListener(v -> {
            // Navigate to add new listing
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_profile_to_addItemFragment);
        });

        myListingsButton.setOnClickListener(v -> {
            // Navigate to my listings
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_profile_to_myListingsFragment);
        });

        salesHistoryButton.setOnClickListener(v -> {
            // Navigate to sales history
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_profile_to_salesHistoryFragment);
        });
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

            // Update email
            userEmailTextView.setText(user.getEmail());

            // Update bio if it exists
            if (user.getBio() != null && !user.getBio().isEmpty()) {
                userBioTextView.setText(user.getBio());
            } else {
                FirebaseUser firebaseUser = auth.getCurrentUser();
                if (firebaseUser != null && (firebaseUser.isEmailVerified() || isGoogleUser(firebaseUser))) {
                    userBioTextView.setText("Account verified ✓");
                }
            }

            // Update rating display
            String ratingText = String.format("%.1f (%d)", user.getRating(), user.getTotalReviews());
            userRatingTextView.setText(ratingText);

            // Load user avatar
            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                try {
                    com.bumptech.glide.Glide.with(requireContext())
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.ic_user_24)
                        .into(profileImageView);
                } catch (Exception e) {
                    // Fallback if Glide fails
                    profileImageView.setImageResource(R.drawable.ic_user_24);
                }
            }
        }
    }
}
