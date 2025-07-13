package com.example.tradeupapp.core.session;

import com.example.tradeupapp.models.User;

public class UserSession {
    private static UserSession instance;
    private User currentUser;

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void clear() {
        currentUser = null;
    }

    public String getUserIdOrUid() {
        if (currentUser == null) return null;
        return currentUser.getUserIdOrUid();
    }

    public String getId() {
        if (currentUser == null) return null;
        return currentUser.getUserIdOrUid();
    }
}
