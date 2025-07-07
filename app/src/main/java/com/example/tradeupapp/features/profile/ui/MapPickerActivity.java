package com.example.tradeupapp.features.profile.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Minimal stub for MapPickerActivity.
 * Replace this with a real map UI (e.g. Google Maps) to let users pick a location.
 */
public class MapPickerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // For now, just return a fixed location (e.g. Hanoi)
        Intent result = new Intent();
        result.putExtra("latitude", 21.0285);
        result.putExtra("longitude", 105.8542);
        setResult(Activity.RESULT_OK, result);
        finish();
    }
}

