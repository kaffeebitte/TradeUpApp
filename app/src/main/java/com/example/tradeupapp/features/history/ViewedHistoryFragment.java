package com.example.tradeupapp.features.history;

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
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.shared.adapters.ListingAdapter;
import java.util.ArrayList;
import java.util.List;

public class ViewedHistoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private ListingAdapter adapter;
    private List<ListingModel> viewedListings = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_tab_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_history_tab);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ListingAdapter(getContext(), viewedListings, listing -> {});
        recyclerView.setAdapter(adapter);
        // Load viewed listings from backend
        String userId = com.example.tradeupapp.core.services.FirebaseService.getInstance().getCurrentUserId();
        if (userId != null) {
            com.example.tradeupapp.core.services.FirebaseService.getInstance().getUserHistoryListings(userId, "viewedAt", new com.example.tradeupapp.core.services.FirebaseService.ListingsCallback() {
                @Override
                public void onSuccess(List<com.example.tradeupapp.models.ListingModel> listings) {
                    viewedListings.clear();
                    viewedListings.addAll(listings);
                    adapter.notifyDataSetChanged();
                }
                @Override
                public void onError(String error) {
                    // Handle error if needed
                }
            });
        }
        return view;
    }
}
