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
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.core.services.ItemRepository;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for purchase history - uses ListingModel for lightweight transaction records
 */
public class PurchaseHistoryAdapter extends RecyclerView.Adapter<PurchaseHistoryAdapter.ViewHolder> {

    private final List<ListingModel> purchasedListings;
    private final OnItemClickListener listener;
    private final Context context;
    private final ItemRepository itemRepository;

    public interface OnItemClickListener {
        void onItemClick(ListingModel item);
        void onReorderClick(ListingModel item);
        void onReviewClick(ListingModel item);
    }

    public PurchaseHistoryAdapter(Context context, List<ListingModel> purchasedListings, OnItemClickListener listener, ItemRepository itemRepository) {
        this.context = context;
        this.purchasedListings = purchasedListings;
        this.listener = listener;
        this.itemRepository = itemRepository;
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
        ListingModel listing = purchasedListings.get(position);

        // Set placeholders first
        holder.title.setText("Loading...");
        holder.price.setText(String.format("₫%,.0f", listing.getPrice()));

        // Purchase date (using createdAt as purchase date for purchased listings)
        if (listing.getCreatedAt() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            holder.purchaseDate.setText("Purchased on " + dateFormat.format(listing.getCreatedAt().toDate()));
        }

        // Transaction status
        holder.status.setText(listing.getTransactionStatus() != null ? listing.getTransactionStatus() : "Completed");

        // Fetch ItemModel for title and image
        itemRepository.getItemById(listing.getItemId(), new ItemRepository.ItemCallback() {
            @Override
            public void onItemLoaded(ItemModel item) {
                if (item != null) {
                    holder.title.setText(item.getTitle());
                    // Load image if available
                    if (item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) {
                        String firstImageUriStr = item.getPhotoUris().get(0);
                        Glide.with(context)
                            .load(firstImageUriStr)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .centerCrop()
                            .into(holder.image);
                    } else {
                        holder.image.setImageResource(R.drawable.ic_image_placeholder);
                    }
                } else {
                    holder.title.setText("Unknown Item");
                    holder.image.setImageResource(R.drawable.ic_image_placeholder);
                }
            }
        });

        // Set click listeners
        holder.itemView.setOnClickListener(v -> listener.onItemClick(listing));
        holder.btnReorder.setOnClickListener(v -> listener.onReorderClick(listing));
        holder.btnReview.setOnClickListener(v -> listener.onReviewClick(listing));
    }

    @Override
    public int getItemCount() {
        return purchasedListings.size();
    }

    public void updateItems(List<ListingModel> newListings) {
        purchasedListings.clear();
        purchasedListings.addAll(newListings);
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
