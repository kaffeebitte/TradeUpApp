package com.example.tradeupapp.features.search.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tradeupapp.R;
import com.example.tradeupapp.core.services.FirebaseService;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;

public class SearchHistoryDialogFragment extends BottomSheetDialogFragment {
    private RecyclerView recyclerView;
    private SearchHistoryAdapter adapter;
    private TextView emptyText;
    private List<String> searchHistory = new ArrayList<>();
    private FirebaseService firebaseService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_history, container, false);
        recyclerView = view.findViewById(R.id.recycler_search_history);
        emptyText = view.findViewById(R.id.tv_empty_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchHistoryAdapter(searchHistory, new SearchHistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String query) {
                if (getTargetFragment() instanceof SearchFragment) {
                    ((SearchFragment) getTargetFragment()).fillSearchQuery(query);
                }
                dismiss();
            }
            @Override
            public void onDeleteClick(String query) {
                deleteQueryFromHistory(query);
            }
        });
        recyclerView.setAdapter(adapter);
        firebaseService = FirebaseService.getInstance();
        loadSearchHistory();
        return view;
    }

    private void loadSearchHistory() {
        String userId = firebaseService.getCurrentUserId();
        if (userId == null) return;
        firebaseService.getUserSearchHistory(userId, new FirebaseService.SearchHistoryCallback() {
            @Override
            public void onSuccess(List<String> history) {
                searchHistory.clear();
                searchHistory.addAll(history);
                adapter.notifyDataSetChanged();
                emptyText.setVisibility(history.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onFailure(Exception e) {
                emptyText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void deleteQueryFromHistory(String query) {
        String userId = firebaseService.getCurrentUserId();
        if (userId == null) return;
        firebaseService.deleteUserSearchQuery(userId, query, new FirebaseService.SimpleCallback() {
            @Override
            public void onSuccess() {
                loadSearchHistory();
            }
            @Override
            public void onError(String error) {
                // Optionally show error
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}

