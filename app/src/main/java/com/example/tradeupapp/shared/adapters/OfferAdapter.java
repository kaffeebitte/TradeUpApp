package com.example.tradeupapp.shared.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {
    private final Context context;
    private final List<OfferModel> offers;
    private final Map<String, ListingModel> listingMap;
    private Map<String, ItemModel> itemMap;
    private final OnOfferActionListener actionListener;

    public interface OnOfferActionListener {
        void onViewDetail(ListingModel listing);
        void onAccept(OfferModel offer, ListingModel listing);
        void onReject(OfferModel offer, ListingModel listing);
        void onCounter(OfferModel offer, ListingModel listing);
        void onMakeOffer(OfferModel offer, ListingModel listing); // New callback
    }

    public OfferAdapter(Context context, List<OfferModel> offers, Map<String, ListingModel> listingMap, Map<String, ItemModel> itemMap, OnOfferActionListener actionListener) {
        this.context = context;
        this.offers = offers;
        this.listingMap = listingMap;
        this.itemMap = itemMap;
        this.actionListener = actionListener;
    }

    public void setItemMap(Map<String, ItemModel> itemMap) {
        this.itemMap = itemMap;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_offer, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
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
        // Counter-offer formatting (if status is counter_offered)
        if ("counter_offered".equalsIgnoreCase(offer.getStatus())) {
            holder.tvOfferPrice.setText("Counter Offer: " + numberFormat.format(offer.getOfferAmount()) + "₫");
        }
        // Message
        holder.tvOfferMessage.setText(offer.getMessage() != null ? offer.getMessage() : "");
        // Status chip
        if (holder.chipOfferStatus != null) {
            holder.chipOfferStatus.setText(offer.getStatus());
            int chipColorRes = R.color.md_theme_secondaryContainer;
            if ("pending".equalsIgnoreCase(offer.getStatus())) {
                chipColorRes = R.color.md_theme_secondaryContainer;
            } else if ("accepted".equalsIgnoreCase(offer.getStatus())) {
                chipColorRes = R.color.md_theme_primaryContainer;
            } else if ("declined".equalsIgnoreCase(offer.getStatus())) {
                chipColorRes = R.color.md_theme_errorContainer;
            } else if ("counter_offered".equalsIgnoreCase(offer.getStatus())) {
                chipColorRes = R.color.md_theme_tertiaryContainer;
            }
            holder.chipOfferStatus.setChipBackgroundColorResource(chipColorRes);
        }
        // Action buttons
        boolean isPending = "pending".equalsIgnoreCase(offer.getStatus());
        holder.btnAccept.setVisibility(isPending ? View.VISIBLE : View.GONE);
        holder.btnReject.setVisibility(isPending ? View.VISIBLE : View.GONE);
        holder.btnViewDetail.setVisibility(View.VISIBLE);
        holder.btnCounter.setVisibility(isPending ? View.VISIBLE : View.GONE);
        holder.btnAccept.setOnClickListener(v -> {
            if (actionListener != null && listing != null) actionListener.onAccept(offer, listing);
        });
        holder.btnReject.setOnClickListener(v -> {
            if (actionListener != null && listing != null) actionListener.onReject(offer, listing);
        });
        holder.btnViewDetail.setOnClickListener(v -> {
            if (actionListener != null && listing != null) actionListener.onViewDetail(listing);
        });
        holder.btnCounter.setOnClickListener(v -> {
            if (actionListener != null && listing != null) actionListener.onCounter(offer, listing);
        });
        // Counter offer action
        holder.itemView.setOnLongClickListener(v -> {
            if (actionListener != null && listing != null) {
                actionListener.onCounter(offer, listing);
                return true;
            }
            return false;
        });
        // Offer actions container visibility
        holder.offerActionsContainer.setVisibility(isPending ? View.VISIBLE : View.GONE);
        // Trigger onMakeOffer callback on item click
        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null && listing != null) actionListener.onMakeOffer(offer, listing);
        });
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    public static class OfferViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, tvOfferPrice, tvOfferMessage;
        Button btnViewDetail, btnAccept, btnReject, btnCounter;
        ImageView ivProductImage;
        com.google.android.material.chip.Chip chipOfferStatus;
        View offerActionsContainer;
        public OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_offer_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_offer_product_price);
            tvOfferPrice = itemView.findViewById(R.id.tv_offer_price);
            tvOfferMessage = itemView.findViewById(R.id.tv_offer_message);
            btnViewDetail = itemView.findViewById(R.id.btn_offer_view_detail);
            btnAccept = itemView.findViewById(R.id.btn_offer_accept);
            btnReject = itemView.findViewById(R.id.btn_offer_reject);
            btnCounter = itemView.findViewById(R.id.btn_offer_counter);
            ivProductImage = itemView.findViewById(R.id.iv_offer_image);
            chipOfferStatus = itemView.findViewById(R.id.chip_offer_status);
            offerActionsContainer = itemView.findViewById(R.id.offer_actions_container);
        }
    }
}
