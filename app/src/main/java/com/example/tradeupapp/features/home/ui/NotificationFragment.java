package com.example.tradeupapp.features.home.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.shared.adapters.NotificationAdapter;
import com.example.tradeupapp.models.NotificationModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private FirebaseService firebaseService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.notifications_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseService = FirebaseService.getInstance();

        // Initialize adapter with empty list
        adapter = new NotificationAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Load notifications from Firebase
        loadNotifications();
    }

    private void loadNotifications() {
        firebaseService.getUserNotifications(new FirebaseService.NotificationsCallback() {
            @Override
            public void onSuccess(List<NotificationModel> notifications) {
                if (getActivity() != null && isAdded()) {
                    adapter.updateNotifications(notifications);
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), "Error loading notifications: " + error, Toast.LENGTH_SHORT).show();
                    // Show empty state or fallback data
                    loadFallbackNotifications();
                }
            }
        });
    }

    private void loadFallbackNotifications() {
        // Only use fallback if Firebase fails
        List<NotificationModel> fallbackList = new ArrayList<>();
        String currentUserId = firebaseService.getCurrentUserId();
        if (currentUserId == null) currentUserId = "anonymous_user";

        NotificationModel welcomeNotification = new NotificationModel(
                currentUserId,
                "Welcome",
                "Welcome to TradeUp! Start exploring items.",
                NotificationModel.Type.SYSTEM,
                null);

        fallbackList.add(welcomeNotification);
        adapter.updateNotifications(fallbackList);
    }
}
