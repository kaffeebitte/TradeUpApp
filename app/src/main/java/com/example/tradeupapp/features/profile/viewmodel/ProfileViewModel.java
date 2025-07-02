package com.example.tradeupapp.features.profile.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tradeupapp.R;
import com.example.tradeupapp.models.User;
import com.example.tradeupapp.utils.CloudinaryManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

/**
 * ViewModel for profile-related operations in the TradeUpApp.
 * Handles data operations and business logic for the EditProfileActivity.
 * Simplified to focus only on profile information editing.
 */
public class ProfileViewModel extends AndroidViewModel {

    // Operation success types - simplified to only include profile updates
    public enum OperationSuccessType {
        PROFILE_UPDATED
    }

    // Firebase components
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    // LiveData
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<OperationSuccessType> operationSuccess = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // Getters for LiveData
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<OperationSuccessType> getOperationSuccess() {
        return operationSuccess;
    }

    // Setters and state clearers
    public void setCurrentUser(User user) {
        currentUser.setValue(user);
    }

    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    public void clearOperationSuccess() {
        operationSuccess.setValue(null);
    }

    /**
     * Update user profile without changing profile image
     */
    public void updateProfile(String displayName, String bio, String phoneNumber, GeoPoint location) {
        if (auth.getCurrentUser() == null) {
            errorMessage.setValue(getApplication().getString(R.string.error_session_expired));
            return;
        }

        isLoading.setValue(true);

        // Update Firebase Auth display name
        updateAuthDisplayName(displayName);

        // Update Firestore user document
        Map<String, Object> updates = prepareProfileUpdates(displayName, bio, phoneNumber, null, location);
        updateFirestoreProfile(updates);
    }

    /**
     * Update user profile including profile image
     */
    public void updateProfileWithImage(String displayName, String bio, String phoneNumber,
                                       Uri imageUri, GeoPoint location) {
        if (auth.getCurrentUser() == null) {
            errorMessage.setValue(getApplication().getString(R.string.error_session_expired));
            return;
        }

        isLoading.setValue(true);

        // Upload image to Cloudinary
        CloudinaryManager.uploadImage(getApplication(), imageUri, "profile_images",
                new CloudinaryManager.CloudinaryUploadCallback() {
                    @Override
                    public void onStart() {
                        // Already set loading to true above
                    }

                    @Override
                    public void onProgress(double progress) {
                        // Can be used to update a progress indicator if needed
                    }

                    @Override
                    public void onSuccess(String imageUrl) {
                        // Update auth display name
                        updateAuthDisplayName(displayName);

                        // Update Firestore with image URL
                        Map<String, Object> updates = prepareProfileUpdates(
                                displayName, bio, phoneNumber, imageUrl, location);
                        updateFirestoreProfile(updates);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        errorMessage.setValue(errorMsg);
                    }
                });
    }

    // Helper methods

    private void updateAuthDisplayName(String displayName) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnFailureListener(e ->
                        errorMessage.setValue(getApplication().getString(R.string.error_update_display_name)));
    }

    private Map<String, Object> prepareProfileUpdates(String displayName, String bio,
                                                     String phoneNumber, String photoUrl,
                                                     GeoPoint location) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("displayName", displayName);
        updates.put("bio", bio);
        updates.put("phoneNumber", phoneNumber);

        if (photoUrl != null) {
            updates.put("photoUrl", photoUrl);
        }

        if (location != null) {
            updates.put("location", location);
        }

        return updates;
    }

    private void updateFirestoreProfile(Map<String, Object> updates) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String uid = currentUser.getUid();

        db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    isLoading.setValue(false);

                    // Update local user object with new values
                    User user = this.currentUser.getValue();
                    if (user != null) {
                        if (updates.containsKey("displayName")) {
                            user.setDisplayName((String) updates.get("displayName"));
                        }
                        if (updates.containsKey("bio")) {
                            user.setBio((String) updates.get("bio"));
                        }
                        if (updates.containsKey("phoneNumber")) {
                            user.setPhoneNumber((String) updates.get("phoneNumber"));
                        }
                        if (updates.containsKey("photoUrl")) {
                            user.setPhotoUrl((String) updates.get("photoUrl"));
                        }
                        if (updates.containsKey("location")) {
                            user.setLocation((GeoPoint) updates.get("location"));
                        }

                        this.currentUser.setValue(user);
                    }

                    operationSuccess.setValue(OperationSuccessType.PROFILE_UPDATED);
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(e.getMessage());
                });
    }
}
