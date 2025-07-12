package com.example.tradeupapp.features.search.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;

import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(String query);
        void onDeleteClick(String query);
    }

    private final List<String> searchHistory;
    private final OnItemClickListener listener;

    public SearchHistoryAdapter(List<String> searchHistory, OnItemClickListener listener) {
        this.searchHistory = searchHistory;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String query = searchHistory.get(position);
        holder.queryText.setText(query);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(query));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(query));
    }

    @Override
    public int getItemCount() {
        return searchHistory.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView queryText;
        ImageView btnDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            queryText = itemView.findViewById(R.id.tv_search_query);
            btnDelete = itemView.findViewById(R.id.btn_delete_history);
        }
    }
}

