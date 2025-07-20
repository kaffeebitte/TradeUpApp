package com.example.tradeupapp.features.listing.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.example.tradeupapp.R;
import java.util.ArrayList;
import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {
    private List<String> tags = new ArrayList<>();
    private List<String> selectedTags = new ArrayList<>();
    private final OnTagSelectedListener listener;
    private OnTagSelectedListener onTagsChangedListener;

    public interface OnTagSelectedListener {
        void onTagSelectionChanged(List<String> selectedTags);
    }

    public TagAdapter(OnTagSelectedListener listener) {
        this.listener = listener;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSelectedTags(List<String> selectedTags) {
        this.selectedTags = selectedTags != null ? new ArrayList<>(selectedTags) : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<String> getSelectedTags() {
        return new ArrayList<>(selectedTags);
    }

    public void setOnTagsChangedListener(OnTagSelectedListener listener) {
        this.onTagsChangedListener = listener;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_chip, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        String tag = tags.get(position);
        holder.chip.setText(tag);
        holder.chip.setChecked(selectedTags.contains(tag));
        holder.chip.setCloseIconVisible(true);
        holder.chip.setOnCloseIconClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                String removedTag = tags.get(pos);
                tags.remove(pos);
                selectedTags.remove(removedTag);
                notifyItemRemoved(pos);
                if (listener != null) listener.onTagSelectionChanged(getSelectedTags());
                if (onTagsChangedListener != null) onTagsChangedListener.onTagSelectionChanged(getSelectedTags());
            }
        });
        holder.chip.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                if (!selectedTags.contains(tag)) selectedTags.add(tag);
            } else {
                selectedTags.remove(tag);
            }
            if (listener != null) listener.onTagSelectionChanged(getSelectedTags());
            if (onTagsChangedListener != null) onTagsChangedListener.onTagSelectionChanged(getSelectedTags());
        });
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    static class TagViewHolder extends RecyclerView.ViewHolder {
        Chip chip;
        TagViewHolder(@NonNull View itemView) {
            super(itemView);
            chip = itemView.findViewById(R.id.chip_tag);
        }
    }
}
