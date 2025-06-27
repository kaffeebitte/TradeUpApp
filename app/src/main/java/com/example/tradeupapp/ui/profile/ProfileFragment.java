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

public class ProfileFragment extends Fragment {

    private ShapeableImageView profileImageView;
    private TextView usernameTextView;
    private TextView userEmailTextView;
    private TextView userBioTextView;
    private TextView userRatingTextView;
    private MaterialButton editProfileButton;
    private MaterialButton accountSettingsButton;
    private MaterialButton myListingsButton;
    private Button logoutButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo các view
        initViews(view);

        // Thiết lập dữ liệu người dùng
        setupUserProfile();

        // Thiết lập sự kiện cho các nút
        setupClickListeners();
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
        logoutButton = view.findViewById(R.id.btn_logout);
    }

    private void setupUserProfile() {
        // TODO: Lấy thông tin người dùng từ database hoặc shared preferences
        usernameTextView.setText("Nguyễn Văn A");
        userEmailTextView.setText("nguyenvana@gmail.com");
        userBioTextView.setText("I love trading electronics and books!");
        userRatingTextView.setText("4.9 (150)");

        // TODO: Tải ảnh đại diện của người dùng
        // Glide.with(this).load(userAvatarUrl).into(profileImageView);
    }

    private void setupClickListeners() {
        // Xử lý sự kiện khi click vào nút Edit Profile
        editProfileButton.setOnClickListener(v -> {
            // Navigate to edit profile screen
            Navigation.findNavController(requireView()).navigate(R.id.action_nav_profile_to_editProfileFragment);
        });

        // Xử lý sự kiện khi click vào nút Account Settings
        accountSettingsButton.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_nav_profile_to_accountSettingsFragment);
        });

        // Xử lý sự kiện khi click vào nút My Listings
        myListingsButton.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_nav_profile_to_manageListingsFragment);
        });

        // Xử lý sự kiện khi click vào nút Logout
        logoutButton.setOnClickListener(v -> {
            // TODO: Xử lý đăng xuất
            // Hiển thị thông báo đăng xuất thành công
            Toast.makeText(requireContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show();

            // Quay về màn hình đăng nhập
            Intent intent = new Intent(requireContext(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
