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
    private TextInputEditText locationEditText;
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
        setupToolbar(view);
        loadUserData();
        setupListeners();
    }

    private void initViews(View view) {
        avatarImageView = view.findViewById(R.id.iv_avatar);
        changeAvatarText = view.findViewById(R.id.tv_change_avatar);
        nameEditText = view.findViewById(R.id.et_name);
        bioEditText = view.findViewById(R.id.et_bio);
        contactEditText = view.findViewById(R.id.et_contact);
        locationEditText = view.findViewById(R.id.et_location);
        saveButton = view.findViewById(R.id.btn_save);
    }

    private void loadUserData() {
        // Get user data from arguments
        Bundle args = getArguments();
        if (args != null && args.containsKey("user")) {
            com.example.tradeupapp.models.User user = (com.example.tradeupapp.models.User) args.getSerializable("user");
            if (user != null) {
                // Set user data to UI elements
                nameEditText.setText(user.getDisplayName());
                bioEditText.setText(user.getBio());
                contactEditText.setText(user.getPhoneNumber());

                // Convert GeoPoint to String for location
                if (user.getLocation() != null) {
                    String locationStr = String.format("%.5f, %.5f",
                            user.getLocation().getLatitude(),
                            user.getLocation().getLongitude());
                    locationEditText.setText(locationStr);
                } else {
                    locationEditText.setText("");
                }

                // Load user avatar if available
                if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                    try {
                        com.bumptech.glide.Glide.with(this)
                            .load(user.getPhotoUrl())
                            .placeholder(R.drawable.ic_user_24)
                            .into(avatarImageView);
                    } catch (Exception e) {
                        // Fallback if Glide fails
                        avatarImageView.setImageResource(R.drawable.ic_user_24);
                    }
                }
            }
        }
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
        String locationText = locationEditText.getText().toString().trim();

        // Validate input
        if (name.isEmpty()) {
            nameEditText.setError("Name cannot be empty");
            return;
        }

        // Get the current user
        com.google.firebase.auth.FirebaseUser firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(requireContext(), "You need to be logged in to update your profile", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        // Get reference to the user document
        String uid = firebaseUser.getUid();
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        com.google.firebase.firestore.DocumentReference userRef = db.collection("users").document(uid);

        // Create a map with the updated fields
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("displayName", name);
        updates.put("bio", bio);
        updates.put("phoneNumber", contact);
        updates.put("updatedAt", new java.util.Date());

        // Parse location string to GeoPoint if possible
        if (!locationText.isEmpty()) {
            try {
                // Try to parse the location string (format: "latitude, longitude")
                String[] coordinates = locationText.split(",");
                if (coordinates.length == 2) {
                    double latitude = Double.parseDouble(coordinates[0].trim());
                    double longitude = Double.parseDouble(coordinates[1].trim());

                    // Create a GeoPoint object and add it to updates
                    com.google.firebase.firestore.GeoPoint geoPoint =
                            new com.google.firebase.firestore.GeoPoint(latitude, longitude);
                    updates.put("location", geoPoint);
                }
            } catch (NumberFormatException e) {
                // If parsing fails, don't update the location
                Toast.makeText(requireContext(), "Invalid location format. Location not updated.",
                        Toast.LENGTH_SHORT).show();
            }
        }

        // If user selected a new image, upload it first
        if (selectedImageUri != null) {
            uploadImageToCloudinary(selectedImageUri, uid, updates, userRef);
        } else {
            // Otherwise just update the text fields
            updateUserProfile(userRef, updates);
        }
    }

    private void uploadImageToCloudinary(Uri imageUri, String userId, java.util.Map<String, Object> updates, com.google.firebase.firestore.DocumentReference userRef) {
        // Get instance of CloudinaryManager
        com.example.tradeupapp.utils.CloudinaryManager cloudinaryManager = com.example.tradeupapp.utils.CloudinaryManager.getInstance();

        // Upload image to Cloudinary
        cloudinaryManager.uploadImage(imageUri, new com.example.tradeupapp.utils.CloudinaryManager.ImageUploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                // Add the Cloudinary URL to the updates
                updates.put("photoUrl", imageUrl);

                // Update Firestore document with all data including the image URL
                updateUserProfile(userRef, updates);
            }

            @Override
            public void onError(String errorMessage) {
                // Handle upload error
                requireActivity().runOnUiThread(() -> {
                    saveButton.setEnabled(true);
                    saveButton.setText("Save Changes");
                    Toast.makeText(requireContext(), "Failed to upload image: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateUserProfile(com.google.firebase.firestore.DocumentReference userRef, java.util.Map<String, Object> updates) {
        userRef.update(updates)
            .addOnSuccessListener(aVoid -> {
                // Update successful
                saveButton.setEnabled(true);
                saveButton.setText("Save Changes");

                // Show success message
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();

                // Navigate back
                Navigation.findNavController(requireView()).navigateUp();
            })
            .addOnFailureListener(e -> {
                // Handle any errors
                saveButton.setEnabled(true);
                saveButton.setText("Save Changes");
                Toast.makeText(requireContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void setupToolbar(View view) {
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            // Navigate back when the back button is clicked
            Navigation.findNavController(requireView()).navigateUp();
        });
    }
}
