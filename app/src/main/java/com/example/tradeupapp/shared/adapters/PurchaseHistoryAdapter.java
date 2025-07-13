package com.example.tradeupapp.shared.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.TransactionModel;
import com.example.tradeupapp.core.services.ItemRepository;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for purchase history - uses TransactionModel for lightweight transaction records
 */
public class PurchaseHistoryAdapter extends RecyclerView.Adapter<PurchaseHistoryAdapter.ViewHolder> {

    private final List<TransactionModel> transactions;
    private final OnItemClickListener listener;
    private final Context context;
    private final ItemRepository itemRepository;
    private final java.util.Map<String, com.example.tradeupapp.models.ListingModel> listingMap;
    private final java.util.Map<String, com.example.tradeupapp.models.ItemModel> itemMap;

    public interface OnItemClickListener {
        void onItemClick(String listingId);
        void onReorderClick(String listingId);
        void onReviewClick(String listingId);
    }

    public PurchaseHistoryAdapter(Context context, List<TransactionModel> transactions, OnItemClickListener listener, ItemRepository itemRepository,
                                  java.util.Map<String, com.example.tradeupapp.models.ListingModel> listingMap,
                                  java.util.Map<String, com.example.tradeupapp.models.ItemModel> itemMap) {
        this.context = context;
        this.transactions = transactions;
        this.listener = listener;
        this.itemRepository = itemRepository;
        this.listingMap = listingMap;
        this.itemMap = itemMap;
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
        TransactionModel transaction = transactions.get(position);
        holder.price.setText(String.format(java.util.Locale.getDefault(), "₫%,.0f", transaction.getAmount()));
        // Purchase date (use completedAt if available, else hide)
        if (transaction.getCompletedAt() != null) {
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault());
            holder.purchaseDate.setText("Purchased on " + dateFormat.format(transaction.getCompletedAt().toDate()));
            holder.purchaseDate.setVisibility(View.VISIBLE);
        } else {
            holder.purchaseDate.setVisibility(View.GONE);
        }
        // Transaction status
        holder.status.setText(transaction.getStatus() != null ? transaction.getStatus() : "COMPLETED");
        // Lấy thông tin item thực tế
        String listingId = transaction.getListingId();
        String itemTitle = "Unknown Item";
        String imageUrl = null;
        if (listingId != null && listingMap.containsKey(listingId)) {
            com.example.tradeupapp.models.ListingModel listing = listingMap.get(listingId);
            if (listing != null && listing.getItemId() != null && itemMap.containsKey(listing.getItemId())) {
                com.example.tradeupapp.models.ItemModel item = itemMap.get(listing.getItemId());
                if (item != null) {
                    if (item.getTitle() != null) itemTitle = item.getTitle();
                    if (item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) {
                        imageUrl = item.getPhotoUris().get(0);
                    }
                }
            }
        }
        holder.title.setText(itemTitle);
        if (imageUrl != null) {
            // Sử dụng Glide hoặc Picasso nếu có, ở đây dùng Glide
            try {
                com.bumptech.glide.Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .into(holder.image);
            } catch (Exception e) {
                holder.image.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            holder.image.setImageResource(R.drawable.ic_image_placeholder);
        }
        // Set click listeners
        holder.itemView.setOnClickListener(v -> listener.onItemClick(transaction.getListingId()));
        holder.btnReorder.setOnClickListener(v -> listener.onReorderClick(transaction.getListingId()));
        holder.btnReview.setOnClickListener(v -> listener.onReviewClick(transaction.getListingId()));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateTransactions(List<TransactionModel> newTransactions) {
        transactions.clear();
        transactions.addAll(newTransactions);
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
