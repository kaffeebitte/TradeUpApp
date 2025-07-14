package com.example.tradeupapp.features.payment.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tradeupapp.R;
import com.example.tradeupapp.models.OfferModel;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.models.ItemModel;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OfferHistoryAdapter extends RecyclerView.Adapter<OfferHistoryAdapter.OfferHistoryViewHolder> {
    private final Context context;
    private final List<OfferModel> offers;
    private final Map<String, ListingModel> listingMap;
    private final Map<String, ItemModel> itemMap;

    public OfferHistoryAdapter(Context context, List<OfferModel> offers, Map<String, ListingModel> listingMap, Map<String, ItemModel> itemMap) {
        this.context = context;
        this.offers = offers;
        this.listingMap = listingMap;
        this.itemMap = itemMap;
    }

    @NonNull
    @Override
    public OfferHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_offer_history, parent, false);
        return new OfferHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferHistoryViewHolder holder, int position) {
        OfferModel offer = offers.get(position);
        ListingModel listing = listingMap.get(offer.getListingId());
        ItemModel item = null;
        if (listing != null && itemMap != null) {
            item = itemMap.get(listing.getItemId());
        }
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        // Product name
        holder.tvProductName.setText(item != null && item.getTitle() != null ? item.getTitle() : "Unknown");
        // Product price
        holder.tvProductPrice.setText(listing != null && listing.getPrice() > 0 ? ("Original Price: " + numberFormat.format(listing.getPrice()) + "₫") : "Original Price: N/A");
        // Product image
        if (item != null && item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) {
            holder.ivProductImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                .load(item.getPhotoUris().get(0))
                .placeholder(R.drawable.ic_image_placeholder)
                .into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setVisibility(View.GONE);
        }
        // Offer price
        holder.tvOfferPrice.setText("Offer Price: " + numberFormat.format(offer.getOfferAmount()) + "₫");
        // Message
        holder.tvOfferMessage.setText(offer.getMessage() != null ? offer.getMessage() : "");
        // Status
        String status = offer.getStatus();
        holder.tvStatus.setText("Status: " + status);
        int statusColor = context.getResources().getColor(R.color.md_theme_onSurfaceVariant);
        if ("pending".equalsIgnoreCase(status)) {
            statusColor = context.getResources().getColor(R.color.md_theme_secondary);
        } else if ("accepted".equalsIgnoreCase(status)) {
            statusColor = context.getResources().getColor(R.color.md_theme_primary);
        } else if ("declined".equalsIgnoreCase(status)) {
            statusColor = context.getResources().getColor(android.R.color.holo_red_dark);
        } else if ("counter_offered".equalsIgnoreCase(status)) {
            statusColor = context.getResources().getColor(R.color.md_theme_tertiary);
        }
        holder.tvStatus.setTextColor(statusColor);
        // Created at
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        holder.tvCreatedAt.setText("Created: " + (offer.getCreatedAt() != null ? sdf.format(offer.getCreatedAt()) : ""));
        // Counter offer
        if (offer.getCounterOffer() != null) {
            holder.tvCounterOffer.setVisibility(View.VISIBLE);
            holder.tvCounterOffer.setText("Counter Offer: " + numberFormat.format(offer.getCounterOffer()) + "₫");
        } else {
            holder.tvCounterOffer.setVisibility(View.GONE);
        }
        // Responded at
        if (offer.getRespondedAt() != null) {
            holder.tvRespondedAt.setVisibility(View.VISIBLE);
            holder.tvRespondedAt.setText("Responded: " + sdf.format(offer.getRespondedAt()));
        } else {
            holder.tvRespondedAt.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    static class OfferHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, tvOfferPrice, tvOfferMessage, tvStatus, tvCreatedAt, tvCounterOffer, tvRespondedAt;
        ImageView ivProductImage;
        OfferHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_offer_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_offer_product_price);
            tvOfferPrice = itemView.findViewById(R.id.tv_offer_price);
            tvOfferMessage = itemView.findViewById(R.id.tv_offer_message);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvCreatedAt = itemView.findViewById(R.id.tv_created_at);
            tvCounterOffer = itemView.findViewById(R.id.tv_counter_offer);
            tvRespondedAt = itemView.findViewById(R.id.tv_responded_at);
            ivProductImage = itemView.findViewById(R.id.iv_offer_image);
        }
    }
}

