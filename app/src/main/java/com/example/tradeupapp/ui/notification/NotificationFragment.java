package com.example.tradeupapp.ui.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.adapters.NotificationAdapter;
import com.example.tradeupapp.models.NotificationModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;

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

        List<NotificationModel> dummyList = new ArrayList<>();
        dummyList.add(new NotificationModel("Trade Request", "Alex wants to trade your item", "Just now"));
        dummyList.add(new NotificationModel("Message", "You received a new message from Lily", "1h ago"));
        dummyList.add(new NotificationModel("Reminder", "Don't forget to respond to trade offer", "Yesterday"));

        adapter = new NotificationAdapter(dummyList);
        recyclerView.setAdapter(adapter);
    }
}
