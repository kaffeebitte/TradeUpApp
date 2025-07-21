package com.example.tradeupapp.features.profile.ui;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EditProfileFragment extends Fragment {

    private ShapeableImageView avatarImageView;
    private TextView changeAvatarText;
    private TextInputEditText nameEditText;
    private TextInputEditText emailEditText; // Add this line
    private TextInputEditText bioEditText;
    private TextInputEditText contactEditText;
    private TextInputEditText locationEditText;
    private MaterialButton saveButton;
    private TextInputLayout locationInputLayout;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;

    private static final int REQUEST_CODE_PICK_LOCATION = 2002;
    private Double selectedLatitude = null;
    private Double selectedLongitude = null;

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

        // Restore selected location if available
        if (savedInstanceState != null) {
            selectedLatitude = (Double) savedInstanceState.getSerializable("selectedLatitude");
            selectedLongitude = (Double) savedInstanceState.getSerializable("selectedLongitude");
            if (selectedLatitude != null && selectedLongitude != null) {
                setLocationFieldWithAddress(selectedLatitude, selectedLongitude);
            }
        }

        // Listen for map picker result via SavedStateHandle (Navigation Component)
        androidx.navigation.NavController navController = Navigation.findNavController(requireView());
        navController.getCurrentBackStackEntry().getSavedStateHandle().getLiveData("location_data").observe(
            getViewLifecycleOwner(), result -> {
                if (result != null && result instanceof Bundle) {
                    Bundle locationData = (Bundle) result;
                    double latitude = locationData.getDouble("latitude");
                    double longitude = locationData.getDouble("longitude");
                    String address = locationData.getString("address");
                    selectedLatitude = latitude;
                    selectedLongitude = longitude;
                    if (address != null && !address.isEmpty()) {
                        locationEditText.setText(address);
                    } else {
                        setLocationFieldWithAddress(latitude, longitude);
                    }
                    // Clear result to avoid reprocessing
                    navController.getCurrentBackStackEntry().getSavedStateHandle().set("location_data", null);
                }
            });
    }

    private void initViews(View view) {
        avatarImageView = view.findViewById(R.id.iv_avatar);
        changeAvatarText = view.findViewById(R.id.tv_change_avatar);
        nameEditText = view.findViewById(R.id.et_name);
        emailEditText = view.findViewById(R.id.et_email); // Add this line
        bioEditText = view.findViewById(R.id.et_bio);
        contactEditText = view.findViewById(R.id.et_contact);
        locationEditText = view.findViewById(R.id.et_location);
        saveButton = view.findViewById(R.id.btn_save);
        locationInputLayout = view.findViewById(R.id.til_location);
    }

    private void loadUserData() {
        // Get user data from arguments
        Bundle args = getArguments();
        if (args != null && args.containsKey("user")) {
            com.example.tradeupapp.models.User user = (com.example.tradeupapp.models.User) args.getSerializable("user");
            if (user != null) {
                // Set user data to UI elements
                nameEditText.setText(user.getDisplayName());
                if (emailEditText != null) {
                    emailEditText.setText(user.getEmail()); // Set email if available
                }
                bioEditText.setText(user.getBio());
                contactEditText.setText(user.getPhoneNumber());

                // Fix: Show location as string if available, otherwise empty
                if (user.getLocation() != null) {
                    double lat = user.getLocation().getLatitude();
                    double lng = user.getLocation().getLongitude();
                    selectedLatitude = lat;
                    selectedLongitude = lng;
                    setLocationFieldWithAddress(lat, lng);
                } else if (user.getLocationLatitude() != null && user.getLocationLongitude() != null) {
                    selectedLatitude = user.getLocationLatitude();
                    selectedLongitude = user.getLocationLongitude();
                    setLocationFieldWithAddress(selectedLatitude, selectedLongitude);
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
        changeAvatarText.setOnClickListener(v -> openImagePicker());

        // Only open map picker when clicking the end icon
        locationInputLayout.setEndIconOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("isFullAddress", true); // Always get full address for profile
            Navigation.findNavController(requireView()).navigate(R.id.action_editProfileFragment_to_mapPickerFragment, args);
        });

        // Do NOT open map picker when clicking the field itself, just allow keyboard input

        // Handle save button click
        saveButton.setOnClickListener(v -> saveUserProfile());
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

        // Use selectedLatitude/Longitude if available
        if (selectedLatitude != null && selectedLongitude != null) {
            com.google.firebase.firestore.GeoPoint geoPoint = new com.google.firebase.firestore.GeoPoint(selectedLatitude, selectedLongitude);
            updates.put("location", geoPoint);
        } else if (!locationText.isEmpty()) {
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

    // Helper: set location field with address from lat/lng or fallback to less detailed address
    private void setLocationFieldWithAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);
                if (addressLine != null && !addressLine.isEmpty()) {
                    locationEditText.setText(addressLine);
                } else if (address.getThoroughfare() != null && !address.getThoroughfare().isEmpty()) {
                    // Fallback to street name if addressLine is not available
                    locationEditText.setText(address.getThoroughfare());
                } else if (address.getLocality() != null && !address.getLocality().isEmpty()) {
                    // Fallback to district/city
                    locationEditText.setText(address.getLocality());
                } else {
                    // Fallback to lat/lng
                    locationEditText.setText(String.format(Locale.getDefault(), "%.5f, %.5f", latitude, longitude));
                }
            } else {
                locationEditText.setText(String.format(Locale.getDefault(), "%.5f, %.5f", latitude, longitude));
            }
        } catch (IOException e) {
            locationEditText.setText(String.format(Locale.getDefault(), "%.5f, %.5f", latitude, longitude));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedLatitude != null) outState.putSerializable("selectedLatitude", selectedLatitude);
        if (selectedLongitude != null) outState.putSerializable("selectedLongitude", selectedLongitude);
    }
}
