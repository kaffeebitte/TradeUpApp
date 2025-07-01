package com.example.tradeupapp.features.auth.utils;

import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.Fragment;

import com.example.tradeupapp.features.auth.ui.AuthActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Utility class for handling user logout functionality
 * Supports both Firebase Auth and Google Sign-In logout
 */
public class LogoutManager {

    private static LogoutManager instance;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private LogoutManager() {
        mAuth = FirebaseAuth.getInstance();
    }

    public static LogoutManager getInstance() {
        if (instance == null) {
            instance = new LogoutManager();
        }
        return instance;
    }

    /**
     * Initialize Google Sign-In client for logout
     * @param context Application context
     */
    public void initializeGoogleSignIn(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("966394665760-d73tf9klj68snprd3dukqmgbbo08hiun.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    /**
     * Perform complete logout - signs out from both Firebase and Google
     * @param context Context for navigation
     * @param callback Callback for logout completion
     */
    public void performLogout(Context context, LogoutCallback callback) {
        // Sign out from Firebase
        mAuth.signOut();

        // Sign out from Google if available
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(task -> {
                        callback.onLogoutComplete();
                        navigateToAuth(context);
                    });
        } else {
            callback.onLogoutComplete();
            navigateToAuth(context);
        }
    }

    /**
     * Quick logout without Google Sign-In client
     * @param context Context for navigation
     */
    public void performQuickLogout(Context context) {
        mAuth.signOut();
        navigateToAuth(context);
    }

    /**
     * Navigate to authentication screen after logout
     * @param context Context for navigation
     */
    private void navigateToAuth(Context context) {
        Intent intent = new Intent(context, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    /**
     * Check if user is currently logged in
     * @return true if user is logged in, false otherwise
     */
    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    /**
     * Get current user's email
     * @return User's email if logged in, null otherwise
     */
    public String getCurrentUserEmail() {
        return mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : null;
    }

    /**
     * Get current user's display name
     * @return User's display name if available, null otherwise
     */
    public String getCurrentUserDisplayName() {
        return mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getDisplayName() : null;
    }

    /**
     * Check if current user's email is verified
     * @return true if email is verified, false otherwise
     */
    public boolean isEmailVerified() {
        return mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified();
    }

    /**
     * Callback interface for logout operations
     */
    public interface LogoutCallback {
        void onLogoutComplete();
    }
}
