package com.example.tradeupapp;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.tradeupapp.shared.auth.UserManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNavigationView;
    private AppBarConfiguration appBarConfiguration;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize UserManager
        userManager = UserManager.getInstance(this);

        // Initialize views
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Handle window insets for edge-to-edge display
        View rootView = findViewById(android.R.id.content);
        rootView.setOnApplyWindowInsetsListener((v, insets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets);

            // Get the navigation bar height
            int navigationBarHeight = insetsCompat.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            // Apply bottom padding to bottom navigation to avoid overlap with navigation bar
            if (bottomNavigationView != null) {
                bottomNavigationView.setPadding(
                        bottomNavigationView.getPaddingLeft(),
                        bottomNavigationView.getPaddingTop(),
                        bottomNavigationView.getPaddingRight(),
                        navigationBarHeight
                );
            }

            return insets;
        });

        // Set up navigation controller
        findViewById(R.id.nav_host_fragment).post(() -> {
            navController = Navigation.findNavController(this, R.id.nav_host_fragment);

            // Initialize the AppBarConfiguration with your top-level destinations
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_recommendations, R.id.nav_search, R.id.nav_chat, R.id.nav_my_store, R.id.nav_profile)
                    .build();

            // Store the last selected item ID to track navigation state
            final int[] currentNavItem = {R.id.nav_recommendations}; // Default is recommendations

            // Remove default setup and implement custom bottom navigation behavior
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                // Always clear backstack to main destinations when clicking bottom nav
                if (itemId == R.id.nav_recommendations || itemId == R.id.nav_search ||
                        itemId == R.id.nav_chat || itemId == R.id.nav_my_store || itemId == R.id.nav_profile) {

                    // Navigate to the start destination of the graph to clear everything
                    navController.popBackStack(R.id.nav_recommendations, false);

                    // If we're not going to recommendations, then navigate to the selected destination
                    if (itemId != R.id.nav_recommendations) {
                        navController.navigate(itemId);
                    }

                    // Update the current selected item
                    currentNavItem[0] = itemId;
                    return true;
                }
                return false;
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }
}