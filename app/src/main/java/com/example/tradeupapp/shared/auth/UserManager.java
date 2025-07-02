package com.example.tradeupapp.shared.auth;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Singleton class to manage user roles and related preferences
 */
public class UserManager {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_ROLE = "user_role";

    private static UserManager instance;
    private final SharedPreferences preferences;

    private UserManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context);
        }
        return instance;
    }

    /**
     * Set the user's role
     * @param role The role to set
     */
    public void setUserRole(UserRole role) {
        preferences.edit().putString(KEY_USER_ROLE, role.name()).apply();
    }

    /**
     * Get the current user's role
     * @return The user's role (defaults to BUYER if not set)
     */
    public UserRole getUserRole() {
        String roleName = preferences.getString(KEY_USER_ROLE, UserRole.BUYER.name());
        try {
            return UserRole.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            // Default to BUYER if there's an issue
            return UserRole.BUYER;
        }
    }

    /**
     * Check if the current user is a buyer
     * @return true if the user is a buyer
     */
    public boolean isBuyer() {
        return getUserRole() == UserRole.BUYER;
    }

    /**
     * Check if the current user is a seller
     * @return true if the user is a seller
     */
    public boolean isSeller() {
        return getUserRole() == UserRole.SELLER;
    }

    /**
     * Toggle between buyer and seller roles
     */
    public void toggleUserRole() {
        if (isBuyer()) {
            setUserRole(UserRole.SELLER);
        } else {
            setUserRole(UserRole.BUYER);
        }
    }
}
