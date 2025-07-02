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
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for purchase history - uses ItemModel (small spoon) for lightweight transaction records
 */
public class PurchaseHistoryAdapter extends RecyclerView.Adapter<PurchaseHistoryAdapter.ViewHolder> {

    private final List<ItemModel> purchasedItems;
    private final OnItemClickListener listener;
    private final Context context;

    public interface OnItemClickListener {
        void onItemClick(ItemModel item);
        void onReorderClick(ItemModel item);
        void onReviewClick(ItemModel item);
    }

    public PurchaseHistoryAdapter(Context context, List<ItemModel> purchasedItems, OnItemClickListener listener) {
        this.context = context;
        this.purchasedItems = purchasedItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_purchase_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemModel item = purchasedItems.get(position);

        // Basic item info
        holder.title.setText(item.getTitle());
        holder.price.setText(String.format("₫%,.0f", item.getPrice()));

        // Purchase date (using dateAdded as purchase date for purchased items)
        if (item.getDateAdded() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            holder.purchaseDate.setText("Purchased on " + dateFormat.format(item.getDateAdded()));
        }

        // Transaction status (for purchase history, this would be "Delivered", "Shipped", etc.)
        holder.status.setText(item.getStatus() != null ? item.getStatus() : "Completed");

        // Load image
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

        // Set click listeners
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.btnReorder.setOnClickListener(v -> listener.onReorderClick(item));
        holder.btnReview.setOnClickListener(v -> listener.onReviewClick(item));
    }

    @Override
    public int getItemCount() {
        return purchasedItems.size();
    }

    public void updateItems(List<ItemModel> newItems) {
        purchasedItems.clear();
        purchasedItems.addAll(newItems);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView image;
        TextView title, price, purchaseDate, status;
        TextView btnReorder, btnReview;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.iv_item_image);
            title = itemView.findViewById(R.id.tv_item_title);
            price = itemView.findViewById(R.id.tv_item_price);
            purchaseDate = itemView.findViewById(R.id.tv_purchase_date);
            status = itemView.findViewById(R.id.tv_status);
            btnReorder = itemView.findViewById(R.id.btn_reorder);
            btnReview = itemView.findViewById(R.id.btn_review);
        }
    }
}
