package com.example.tradeupapp.features.profile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tradeupapp.R;
import com.example.tradeupapp.features.auth.ui.AuthActivity;
import com.example.tradeupapp.features.auth.utils.LogoutManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private ShapeableImageView profileImageView;
    private TextView usernameTextView;
    private TextView userEmailTextView;
    private TextView userBioTextView;
    private TextView userRatingTextView;
    private MaterialButton editProfileButton;
    private MaterialButton accountSettingsButton;
    private MaterialButton myListingsButton;
    private MaterialButton adminDashboardButton;
    private Button logoutButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private LogoutManager logoutManager;

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

        // Initialize logout manager
        logoutManager = LogoutManager.getInstance();
        logoutManager.initializeGoogleSignIn(requireContext());

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
        profileImageView = view.findViewById(R.id.iv_avatar);
        usernameTextView = view.findViewById(R.id.tv_name);
        userEmailTextView = view.findViewById(R.id.tv_email);
        userBioTextView = view.findViewById(R.id.tv_bio);
        userRatingTextView = view.findViewById(R.id.tv_rating);
        editProfileButton = view.findViewById(R.id.btn_edit_profile);
        accountSettingsButton = view.findViewById(R.id.btn_account_settings);
        myListingsButton = view.findViewById(R.id.btn_my_listings);
        adminDashboardButton = view.findViewById(R.id.btn_admin_dashboard);
        logoutButton = view.findViewById(R.id.btn_logout);
    }

    private void setupUserProfile() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // Display actual user information
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            if (displayName != null && !displayName.isEmpty()) {
                usernameTextView.setText(displayName);
            } else {
                usernameTextView.setText("Người dùng");
            }

            if (email != null) {
                userEmailTextView.setText(email);
            }

            // Show email verification status
            if (!currentUser.isEmailVerified() && !isGoogleUser(currentUser)) {
                userBioTextView.setText("⚠️ Email chưa được xác minh. Vui lòng kiểm tra hộp thư của bạn.");
                userBioTextView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                userBioTextView.setText("Tài khoản đã được xác minh ✓");
                userBioTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }

            // TODO: Load user rating from Firestore
            userRatingTextView.setText("4.9 (150)");

            // TODO: Load user avatar
            // Glide.with(this).load(userAvatarUrl).into(profileImageView);
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
        editProfileButton.setOnClickListener(v -> {
            // TODO: Navigate to edit profile when navigation action is added
            Toast.makeText(requireContext(), "Chỉnh sửa hồ sơ", Toast.LENGTH_SHORT).show();
        });

        accountSettingsButton.setOnClickListener(v -> {
            // TODO: Navigate to account settings when navigation action is added
            Toast.makeText(requireContext(), "Cài đặt tài khoản", Toast.LENGTH_SHORT).show();
        });

        myListingsButton.setOnClickListener(v -> {
            // Navigate to user's listings
            Toast.makeText(requireContext(), "Danh sách của tôi", Toast.LENGTH_SHORT).show();
        });

        adminDashboardButton.setOnClickListener(v -> {
            // Navigate to admin dashboard
            Toast.makeText(requireContext(), "Bảng điều khiển quản trị", Toast.LENGTH_SHORT).show();
        });

        logoutButton.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        logoutManager.performLogout(requireContext(), new LogoutManager.LogoutCallback() {
            @Override
            public void onLogoutComplete() {
                Toast.makeText(requireContext(), "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();

                // Return to login screen
                Intent intent = new Intent(requireContext(), AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}
