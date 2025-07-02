package com.example.tradeupapp.features.profile.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.tradeupapp.R;
import com.example.tradeupapp.features.profile.viewmodel.ProfileViewModel;
import com.example.tradeupapp.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.GeoPoint;

import java.util.Locale;

/**
 * Activity for editing user profile information in the TradeUpApp.
 * Uses MVVM architecture with a ProfileViewModel to handle business logic and data operations.
 * Allows editing of profile picture, display name, bio, phone number, and location.
 */
public class EditProfileActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1001;

    // UI Components
    private MaterialToolbar toolbar;
    private ShapeableImageView profileImageView;
    private TextInputEditText displayNameEditText;
    private TextInputEditText bioEditText;
    private TextInputEditText phoneNumberEditText;
    private TextInputEditText locationEditText;
    private TextInputEditText emailEditText;
    private Button changePhotoButton;
    private Button saveButton;
    private LinearProgressIndicator progressIndicator;

    // ViewModel
    private ProfileViewModel viewModel;

    // State variables
    private Uri selectedImageUri = null;
    private boolean imageChanged = false;
    private GeoPoint selectedLocation = null;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        Glide.with(this)
                                .load(selectedImageUri)
                                .placeholder(R.drawable.ic_user_24)
                                .into(profileImageView);
                        imageChanged = true;
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_edit_profile);

        // Set up ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Initialize views
        initViews();

        // Set up toolbar
        setupToolbar();

        // Observe ViewModel LiveData
        setupObservers();

        // Get current user data from intent
        if (getIntent().hasExtra("user")) {
            User user = (User) getIntent().getSerializableExtra("user");
            viewModel.setCurrentUser(user);
        } else {
            Toast.makeText(this, R.string.error_loading_user, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set up click listeners
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        profileImageView = findViewById(R.id.iv_avatar);  // Changed from iv_profile_picture
        displayNameEditText = findViewById(R.id.et_name);  // Changed from et_display_name
        bioEditText = findViewById(R.id.et_bio);
        phoneNumberEditText = findViewById(R.id.et_contact);  // Changed from et_phone_number
        locationEditText = findViewById(R.id.et_location);
        emailEditText = findViewById(R.id.et_email);
        changePhotoButton = findViewById(R.id.tv_change_avatar);  // Changed from btn_change_photo
        saveButton = findViewById(R.id.btn_save);  // Changed from btn_save_profile
        progressIndicator = findViewById(R.id.progress_indicator);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.edit_profile_title);
        }

        // Use a compatibility approach for back navigation
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupObservers() {
        // Observe current user
        viewModel.getCurrentUser().observe(this, this::populateUserData);

        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
            saveButton.setEnabled(!isLoading);
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                viewModel.clearErrorMessage();
            }
        });

        // Observe operation success - fix the redundant null check
        viewModel.getOperationSuccess().observe(this, operationSuccess -> {
            if (operationSuccess == ProfileViewModel.OperationSuccessType.PROFILE_UPDATED) {
                Toast.makeText(this, R.string.profile_update_success, Toast.LENGTH_SHORT).show();
                // Return updated user data to calling activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("updatedUser", viewModel.getCurrentUser().getValue());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
            viewModel.clearOperationSuccess();
        });
    }

    private void populateUserData(User user) {
        if (user != null) {
            // Set display name
            if (user.getDisplayName() != null) {
                displayNameEditText.setText(user.getDisplayName());
            }

            // Set bio
            if (user.getBio() != null) {
                bioEditText.setText(user.getBio());
            }

            // Set phone number
            if (user.getPhoneNumber() != null) {
                phoneNumberEditText.setText(user.getPhoneNumber());
            }

            // Set email (readonly)
            emailEditText.setText(user.getEmail());

            // Set location
            if (user.getLocation() != null) {
                selectedLocation = user.getLocation();
                locationEditText.setText(String.format(Locale.getDefault(),
                        "%.5f, %.5f",
                        selectedLocation.getLatitude(),
                        selectedLocation.getLongitude()));
            }

            // Load profile image
            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.ic_user_24)
                        .into(profileImageView);
            }
        }
    }

    private void setupClickListeners() {
        changePhotoButton.setOnClickListener(v -> checkPermissionAndPickImage());

        locationEditText.setOnClickListener(v -> showLocationNotImplementedDialog());

        saveButton.setOnClickListener(v -> validateAndSaveProfile());
    }

    private void showLocationNotImplementedDialog() {
        // In a real app, this would launch a location picker
        // For now, we'll show a dialog explaining this functionality is not implemented
        new AlertDialog.Builder(this)
                .setTitle(R.string.location_not_implemented_title)
                .setMessage(R.string.location_not_implemented_message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void checkPermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            openImagePicker();
        }
    }

    private void openImagePicker() {
        // Fix the warning about setting type after URI by using setDataAndType instead
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        pickImageLauncher.launch(intent);
    }

    private void validateAndSaveProfile() {
        // Add null checks to prevent NullPointerException
        CharSequence displayNameText = displayNameEditText.getText();
        CharSequence bioText = bioEditText.getText();
        CharSequence phoneNumberText = phoneNumberEditText.getText();

        String displayName = displayNameText != null ? displayNameText.toString().trim() : "";
        String bio = bioText != null ? bioText.toString().trim() : "";
        String phoneNumber = phoneNumberText != null ? phoneNumberText.toString().trim() : "";

        // Basic validation
        if (TextUtils.isEmpty(displayName)) {
            displayNameEditText.setError(getString(R.string.error_empty_display_name));
            return;
        }

        // If image was changed, upload it and save profile
        if (imageChanged && selectedImageUri != null) {
            viewModel.updateProfileWithImage(displayName, bio, phoneNumber, selectedImageUri, selectedLocation);
        } else {
            // Otherwise just update the profile data
            viewModel.updateProfile(displayName, bio, phoneNumber, selectedLocation);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // This would handle the location picker result in a real implementation
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
