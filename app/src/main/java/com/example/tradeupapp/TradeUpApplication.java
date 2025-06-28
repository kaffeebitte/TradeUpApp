package com.example.tradeupapp;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class TradeUpApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
    }
}
