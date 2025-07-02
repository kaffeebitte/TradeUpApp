package com.example.tradeupapp.shared.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import com.example.tradeupapp.R;
import com.example.tradeupapp.models.ItemModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class UserListingAdapter extends RecyclerView.Adapter<UserListingAdapter.ViewHolder> {

    private final List<ItemModel> itemList;
    private final OnItemActionListener listener;
    private final Context context;

    public interface OnItemActionListener {
        void onView(ItemModel item);
        void onEdit(ItemModel item);
        void onDelete(ItemModel item);
    }

    public UserListingAdapter(Context context, List<ItemModel> itemList, OnItemActionListener listener) {
        this.itemList = itemList;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemModel item = itemList.get(position);
        holder.title.setText(item.getTitle());
        holder.price.setText(String.format("₫%,.0f", item.getPrice()));

        // Set status using the correct field
        String statusText = item.getStatus() != null ? item.getStatus() : "Available";
        holder.status.setText(statusText);

        // Set views and interactions counts
        holder.views.setText(item.getViewCount() + " views");
        holder.chats.setText(" • " + (item.getInteractionCount() / 2) + " chats");
        holder.offers.setText(" • " + (item.getInteractionCount() / 2) + " offers");

        // Set chip background and text color based on status
        switch (statusText) {
            case "Available":
                holder.status.setChipBackgroundColorResource(R.color.md_theme_primaryContainer);
                holder.status.setTextColor(context.getColor(R.color.md_theme_primary));
                break;
            case "Paused":
                holder.status.setChipBackgroundColorResource(R.color.md_theme_secondaryContainer);
                holder.status.setTextColor(context.getColor(R.color.md_theme_secondary));
                break;
            case "Sold":
                holder.status.setChipBackgroundColorResource(R.color.md_theme_errorContainer);
                holder.status.setTextColor(context.getColor(R.color.md_theme_error));
                break;
            default:
                holder.status.setChipBackgroundColorResource(R.color.md_theme_surfaceVariant);
                holder.status.setTextColor(context.getColor(R.color.black));
                break;
        }

        // Load first image (placeholder if null)
        if (item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) {
            Uri firstImageUri = item.getPhotoUris().get(0);
            if (firstImageUri != null) {
                Glide.with(context)
                    .load(firstImageUri)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .centerCrop()
                    .into(holder.image);
            } else {
                holder.image.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            holder.image.setImageResource(R.drawable.ic_image_placeholder);
        }

        holder.itemView.setOnClickListener(v -> listener.onView(item));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    /**
     * Updates the adapter's dataset with a new list of items.
     * @param newItems The new list of items to display
     */
    public void updateItems(List<ItemModel> newItems) {
        itemList.clear();
        itemList.addAll(newItems);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView image;
        TextView title, price, views, chats, offers;
        Chip status;
        MaterialButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.iv_item_image);
            title = itemView.findViewById(R.id.tv_item_title);
            price = itemView.findViewById(R.id.tv_item_price);
            views = itemView.findViewById(R.id.tv_views);
            chats = itemView.findViewById(R.id.tv_chats);
            offers = itemView.findViewById(R.id.tv_offers);
            status = itemView.findViewById(R.id.chip_item_status);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
