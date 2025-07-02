package com.example.tradeupapp;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.os.StrictMode;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.tradeupapp.utils.CloudinaryManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class TradeUpApplication extends Application {

    private static TradeUpApplication instance;

    public static TradeUpApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Initialize Cloudinary
        CloudinaryManager.initialize(this);

        // Configure Firestore for better performance
        configureFirestore();

        // Enable dark mode based on system settings
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // Enable StrictMode in debug builds to catch potential issues
        // Using application info instead of BuildConfig to avoid compilation issues
        boolean isDebuggable = (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        if (isDebuggable) {
            enableStrictMode();
        }
    }

    private void configureFirestore() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true) // Enable offline persistence
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED) // Set cache size
                .build();
        firestore.setFirestoreSettings(settings);
    }

    private void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build());
    }
}
