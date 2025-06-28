package com.example.tradeupapp.ui.profile;

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
import com.example.tradeupapp.ui.auth.AuthActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
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
        // TODO: Get user information from database or shared preferences
        usernameTextView.setText("Nguyễn Văn A");
        userEmailTextView.setText("nguyenvana@gmail.com");
        userBioTextView.setText("I love trading electronics and books!");
        userRatingTextView.setText("4.9 (150)");

        // TODO: Load user avatar
        // Glide.with(this).load(userAvatarUrl).into(profileImageView);
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
        // Handle click on Edit Profile button
        editProfileButton.setOnClickListener(v -> {
            // Navigate to edit profile screen
            Navigation.findNavController(requireView()).navigate(R.id.action_nav_profile_to_editProfileFragment);
        });

        // Handle click on Account Settings button
        accountSettingsButton.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_nav_profile_to_accountSettingsFragment);
        });

        // Handle click on My Listings button
        myListingsButton.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_nav_profile_to_manageListingsFragment);
        });

        // Handle click on Admin Dashboard button
        adminDashboardButton.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_nav_profile_to_adminDashboardFragment);
        });

        // Handle click on Logout button
        logoutButton.setOnClickListener(v -> {
            // TODO: Handle logout
            // Show logout success message
            Toast.makeText(requireContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show();

            // Return to login screen
            Intent intent = new Intent(requireContext(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
