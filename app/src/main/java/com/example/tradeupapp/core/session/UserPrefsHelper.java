package com.example.tradeupapp.core.session;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPrefsHelper {
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static UserPrefsHelper instance;
    private SharedPreferences prefs;

    private UserPrefsHelper(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized UserPrefsHelper getInstance(Context context) {
        if (instance == null) {
            instance = new UserPrefsHelper(context);
        }
        return instance;
    }

    public void saveUserId(String userId) {
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public void clearUserId() {
        prefs.edit().remove(KEY_USER_ID).apply();
    }
}

