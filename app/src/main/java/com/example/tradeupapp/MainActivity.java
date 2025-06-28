package com.example.tradeupapp;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNavigationView;
    private MaterialToolbar toolbar;
    private FloatingActionButton fabAddItem;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddItem = findViewById(R.id.fab_add_item);

        // Set up the toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Set up navigation controller
        // Wait until the view is laid out before attempting to find the NavController
        findViewById(R.id.nav_host_fragment).post(() -> {
            navController = Navigation.findNavController(this, R.id.nav_host_fragment);

            // Initialize the AppBarConfiguration with your top-level destinations
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_recommendations, R.id.nav_search, R.id.nav_chat, R.id.nav_profile)
                    .build();

            // Store the last selected item ID to track navigation state
            final int[] currentNavItem = {R.id.nav_recommendations}; // Default is recommendations

            // Remove default setup and implement custom bottom navigation behavior
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                // Always clear backstack to main destinations when clicking bottom nav
                if (itemId == R.id.nav_recommendations || itemId == R.id.nav_search ||
                        itemId == R.id.nav_chat || itemId == R.id.nav_profile) {

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

            // Set FAB click listener to navigate to add item fragment
            fabAddItem.setOnClickListener(view -> {
                navController.navigate(R.id.nav_add);
            });

            // Handle notification button click to navigate to notificationFragment
            com.google.android.material.button.MaterialButton notificationButton = findViewById(R.id.notifications_btn);
            notificationButton.setOnClickListener(v -> {
                // Navigate to notification with clearBackStack option
                navController.navigate(R.id.notificationFragment);

                // Deselect any selected item in bottom navigation
                bottomNavigationView.getMenu().findItem(currentNavItem[0]).setChecked(false);
            });

            // Configure destination changes to hide/show FAB
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                // Hide FAB on Add Item fragment to avoid duplication
                if (destination.getId() == R.id.nav_add) {
                    fabAddItem.hide();
                } else {
                    fabAddItem.show();
                }
            });
        });

        // Handle edge-to-edge display
        View mainLayout = findViewById(R.id.app_bar_layout);
        mainLayout.setOnApplyWindowInsetsListener((v, insets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets);

            // Apply top padding to avoid status bar overlap
            int statusBarHeight = insetsCompat.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, statusBarHeight, 0, 0);

            // Apply bottom padding for navigation bar
            bottomNavigationView.setPadding(0, 0, 0,
                    insetsCompat.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom);

            return insets;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}