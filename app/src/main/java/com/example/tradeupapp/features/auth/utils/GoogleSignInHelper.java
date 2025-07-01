package com.example.tradeupapp.features.auth.utils;

import android.app.Activity;
import android.content.Intent;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleSignInHelper {

    public static final int RC_SIGN_IN = 9001;

    public interface GoogleSignInCallback {
        void onGoogleSignInSuccess();
        void onGoogleSignInFailure(String errorMessage);
    }

    /**
     * Configure Google Sign-In options
     * @param webClientId The default web client ID from Firebase
     * @return GoogleSignInOptions configured for Firebase
     */
    public static GoogleSignInOptions getGoogleSignInOptions(String webClientId) {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();
    }

    /**
     * Get Google Sign-In client
     * @param activity The activity context
     * @param webClientId The default web client ID from Firebase
     * @return Configured GoogleSignInClient
     */
    public static GoogleSignInClient getGoogleSignInClient(Activity activity, String webClientId) {
        GoogleSignInOptions gso = getGoogleSignInOptions(webClientId);
        return GoogleSignIn.getClient(activity, gso);
    }

    /**
     * Start Google Sign-In intent
     * @param fragment The fragment that will handle the result
     * @param googleSignInClient The configured GoogleSignInClient
     */
    public static void signIn(Fragment fragment, GoogleSignInClient googleSignInClient) {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        fragment.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Handle Google Sign-In result and authenticate with Firebase
     * @param data The intent data from onActivityResult
     * @param callback Callback for success/failure
     */
    public static void handleSignInResult(Intent data, GoogleSignInCallback callback) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken(), callback);
        } catch (ApiException e) {
            callback.onGoogleSignInFailure("Đăng nhập Google thất bại: " + e.getMessage());
        }
    }

    /**
     * Authenticate with Firebase using Google credentials
     * @param idToken The ID token from Google Sign-In
     * @param callback Callback for success/failure
     */
    private static void firebaseAuthWithGoogle(String idToken, GoogleSignInCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onGoogleSignInSuccess();
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() :
                                "Xác thực Firebase thất bại";
                        callback.onGoogleSignInFailure(errorMessage);
                    }
                });
    }

    /**
     * Sign out from Google Sign-In
     * @param googleSignInClient The GoogleSignInClient to sign out from
     */
    public static void signOut(GoogleSignInClient googleSignInClient) {
        googleSignInClient.signOut();
        FirebaseAuth.getInstance().signOut();
    }
}
