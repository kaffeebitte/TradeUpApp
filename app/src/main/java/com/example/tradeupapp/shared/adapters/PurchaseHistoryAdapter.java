package com.example.tradeupapp.shared.adapters;

import android.content.Context;
import android.util.Log;
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
import com.example.tradeupapp.core.services.FirebaseService;
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
    private final FirebaseService firebaseService;

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
        this.firebaseService = FirebaseService.getInstance();
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
        // Purchase date (prefer completedAt from transaction)
        if (transaction.getCompletedAt() != null) {
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault());
            holder.purchaseDate.setText("Purchased on " + dateFormat.format(transaction.getCompletedAt().toDate()));
            holder.purchaseDate.setVisibility(View.VISIBLE);
        } else {
            holder.purchaseDate.setVisibility(View.GONE);
        }
        // Transaction status
        holder.status.setText(transaction.getStatus() != null ? transaction.getStatus() : "COMPLETED");
        // Product info: title, image
        String listingId = transaction.getListingId();
        holder.title.setText("Loading...");
        holder.image.setImageResource(R.drawable.ic_image_placeholder);
        if (listingId != null) {
            // Always fetch listing from Firestore if not in map
            if (listingMap.containsKey(listingId)) {
                com.example.tradeupapp.models.ListingModel listing = listingMap.get(listingId);
                if (listing != null && listing.getItemId() != null) {
                    fetchAndBindItem(listing.getItemId(), holder);
                } else {
                    holder.title.setText("No title");
                    holder.image.setImageResource(R.drawable.ic_image_placeholder);
                }
            } else {
                // Fetch listing from Firestore
                firebaseService.getListingById(listingId, new com.example.tradeupapp.core.services.ListingCallback() {
                    @Override
                    public void onSuccess(com.example.tradeupapp.models.ListingModel listing) {
                        if (listing != null && listing.getItemId() != null) {
                            fetchAndBindItem(listing.getItemId(), holder);
                        } else {
                            holder.title.setText("No title");
                            holder.image.setImageResource(R.drawable.ic_image_placeholder);
                        }
                    }
                    @Override
                    public void onError(String error) {
                        holder.title.setText("No title");
                        holder.image.setImageResource(R.drawable.ic_image_placeholder);
                    }
                });
            }
        } else {
            holder.title.setText("Unknown Item");
            holder.image.setImageResource(R.drawable.ic_image_placeholder);
        }
        // Địa chỉ giao hàng từ transaction
        String deliveryAddress = transaction.getAddress();
        if (deliveryAddress != null && !deliveryAddress.isEmpty()) {
            holder.location.setText(deliveryAddress);
            holder.location.setVisibility(View.VISIBLE);
        } else {
            holder.location.setVisibility(View.GONE);
        }
        // Set click listeners
        holder.itemView.setOnClickListener(v -> listener.onItemClick(transaction.getListingId()));
        holder.btnViewDetail.setOnClickListener(v -> listener.onItemClick(transaction.getListingId()));
        holder.btnReview.setOnClickListener(v -> listener.onReviewClick(transaction.getListingId()));
        // Disable review button if review exists
        firebaseService.getReviewsByListingId(listingId, new FirebaseService.ReviewsCallback() {
            @Override
            public void onSuccess(java.util.List<com.example.tradeupapp.models.ReviewModel> reviews) {
                String currentUserId = firebaseService.getCurrentUserId();
                boolean hasReview = false;
                for (com.example.tradeupapp.models.ReviewModel review : reviews) {
                    if (review.getReviewerId() != null && review.getReviewerId().equals(currentUserId)) {
                        hasReview = true;
                        break;
                    }
                }
                holder.btnReview.setEnabled(!hasReview);
                if (hasReview) {
                    holder.btnReview.setText("Reviewed");
                } else {
                    holder.btnReview.setText("Write Review");
                }
            }
            @Override
            public void onError(String error) {
                holder.btnReview.setEnabled(true);
                holder.btnReview.setText("Write Review");
            }
        });
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

    // Helper method to fetch item and bind to holder
    private void fetchAndBindItem(String itemId, ViewHolder holder) {
        firebaseService.getItemById(itemId, new FirebaseService.ItemCallback() {
            @Override
            public void onSuccess(com.example.tradeupapp.models.ItemModel item) {
                if (item != null) {
                    holder.title.setText(item.getTitle() != null ? item.getTitle() : "No title");
                    if (item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) {
                        com.bumptech.glide.Glide.with(context)
                            .load(item.getPhotoUris().get(0))
                            .placeholder(R.drawable.ic_image_placeholder)
                            .into(holder.image);
                    } else {
                        holder.image.setImageResource(R.drawable.ic_image_placeholder);
                    }
                } else {
                    holder.title.setText("No title");
                    holder.image.setImageResource(R.drawable.ic_image_placeholder);
                }
            }
            @Override
            public void onError(String error) {
                holder.title.setText("No title");
                holder.image.setImageResource(R.drawable.ic_image_placeholder);
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView image;
        TextView title, price, purchaseDate, status, location;
        TextView btnViewDetail, btnReview;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.iv_item_image);
            title = itemView.findViewById(R.id.tv_item_title);
            price = itemView.findViewById(R.id.tv_item_price);
            purchaseDate = itemView.findViewById(R.id.tv_purchase_date);
            status = itemView.findViewById(R.id.tv_status);
            btnViewDetail = itemView.findViewById(R.id.btn_view_detail);
            btnReview = itemView.findViewById(R.id.btn_review);
            location = itemView.findViewById(R.id.tv_item_location);
        }
    }
}
