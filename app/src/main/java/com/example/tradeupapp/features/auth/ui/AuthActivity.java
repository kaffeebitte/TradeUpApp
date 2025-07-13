package com.example.tradeupapp.features.auth.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tradeupapp.R;
import com.example.tradeupapp.core.session.UserPrefsHelper;
import com.example.tradeupapp.core.session.UserSession;
import com.example.tradeupapp.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Auto-login: Check if userId is saved in SharedPreferences
        String userId = UserPrefsHelper.getInstance(this).getUserId();
        if (userId != null) {
            // Fetch user info from Firestore and set to UserSession
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User userModel = documentSnapshot.toObject(User.class);
                        if (userModel != null) {
                            UserSession.getInstance().setCurrentUser(userModel);
                            // Auto-navigate to MainActivity if user is valid
                            startActivity(new android.content.Intent(this, com.example.tradeupapp.MainActivity.class));
                            finish();
                        }
                    }
                });
        }

        // Thiết lập Navigation Controller
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.auth_nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            // Always navigate to login fragment if this is a fresh launch (after sign out)
            if (isTaskRoot() && savedInstanceState == null) {
                navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
