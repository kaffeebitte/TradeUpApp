package com.example.tradeupapp.features.auth.utils;

import android.content.Context;
import android.content.Intent;
import com.example.tradeupapp.MainActivity;
import com.example.tradeupapp.features.auth.ui.AuthActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Session Manager for handling authentication state and navigation
 */
public class SessionManager {

    private static SessionManager instance;
    private FirebaseAuth mAuth;

    private SessionManager() {
        mAuth = FirebaseAuth.getInstance();
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Check authentication state and navigate to appropriate screen
     * @param context Application context
     */
    public void checkAuthAndNavigate(Context context) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User is logged in, navigate to main app
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } else {
            // User is not logged in, navigate to auth
            Intent intent = new Intent(context, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
    }

    /**
     * Get current authentication state
     * @return true if user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return mAuth.getCurrentUser() != null;
    }

    /**
     * Get current user
     * @return FirebaseUser if authenticated, null otherwise
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Check if current user needs email verification
     * @return true if email verification is required, false otherwise
     */
    public boolean requiresEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return false;

        // Google users don't need email verification
        if (user.getProviderData() != null) {
            for (com.google.firebase.auth.UserInfo userInfo : user.getProviderData()) {
                if ("google.com".equals(userInfo.getProviderId())) {
                    return false;
                }
            }
        }

        return !user.isEmailVerified();
    }
}
