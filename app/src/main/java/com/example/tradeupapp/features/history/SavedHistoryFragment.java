package com.example.tradeupapp.features.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tradeupapp.R;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.shared.adapters.ListingAdapter;
import java.util.ArrayList;
import java.util.List;

public class SavedHistoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private ListingAdapter adapter;
    private List<ListingModel> savedListings = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_tab_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_history_tab);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns
        adapter = new ListingAdapter(getContext(), savedListings, listing -> {});
        recyclerView.setAdapter(adapter);
        // Load saved listings from backend
        String userId = com.example.tradeupapp.core.services.FirebaseService.getInstance().getCurrentUserId();
        if (userId != null) {
            com.example.tradeupapp.core.services.FirebaseService.getInstance().getUserHistoryListings(userId, "savedAt", new com.example.tradeupapp.core.services.FirebaseService.ListingsCallback() {
                @Override
                public void onSuccess(List<com.example.tradeupapp.models.ListingModel> listings) {
                    savedListings.clear();
                    savedListings.addAll(listings);
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
