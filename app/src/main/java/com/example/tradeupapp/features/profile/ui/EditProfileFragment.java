package com.example.tradeupapp.features.profile.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tradeupapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileFragment extends Fragment {

    private ShapeableImageView avatarImageView;
    private TextView changeAvatarText;
    private TextInputEditText nameEditText;
    private TextInputEditText bioEditText;
    private TextInputEditText contactEditText;
    private MaterialButton saveButton;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the image picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        avatarImageView.setImageURI(selectedImageUri);
                    }
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadUserData();
        setupListeners();
    }

    private void initViews(View view) {
        avatarImageView = view.findViewById(R.id.iv_avatar);
        changeAvatarText = view.findViewById(R.id.tv_change_avatar);
        nameEditText = view.findViewById(R.id.et_name);
        bioEditText = view.findViewById(R.id.et_bio);
        contactEditText = view.findViewById(R.id.et_contact);
        saveButton = view.findViewById(R.id.btn_save);
    }

    private void loadUserData() {
        // TODO: Load user data from database or shared preferences
        // For now, we'll use some placeholder data
        nameEditText.setText("Nguyễn Văn A");
        bioEditText.setText("I love trading electronics and books!");
        contactEditText.setText("0123456789");

        // TODO: Load user avatar
        // Glide.with(this).load(userAvatarUrl).into(avatarImageView);
    }

    private void setupListeners() {
        // Handle change avatar click
        changeAvatarText.setOnClickListener(v -> {
            openImagePicker();
        });

        // Handle save button click
        saveButton.setOnClickListener(v -> {
            saveUserProfile();
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void saveUserProfile() {
        // Get values from edit texts
        String name = nameEditText.getText().toString().trim();
        String bio = bioEditText.getText().toString().trim();
        String contact = contactEditText.getText().toString().trim();

        // Validate input
        if (name.isEmpty()) {
            nameEditText.setError("Name cannot be empty");
            return;
        }

        // TODO: Save user data to database or shared preferences

        // Show success message
        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();

        // Navigate back
        Navigation.findNavController(requireView()).navigateUp();
    }
}
