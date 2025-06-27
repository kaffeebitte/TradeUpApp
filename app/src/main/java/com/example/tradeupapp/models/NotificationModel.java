package com.example.tradeupapp.models;

public class NotificationModel {
    private String title;
    private String message;
    private String time;
    private boolean isRead;

    public NotificationModel(String title, String message, String time) {
        this.title = title;
        this.message = message;
        this.time = time;
        this.isRead = false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
