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
        if (item != null) {
            holder.tvProductName.setText(item.getTitle() != null ? item.getTitle() : "Unknown");
            holder.tvProductPrice.setText(listing != null && listing.getPrice() > 0 ? ("Original Price: " + numberFormat.format(listing.getPrice()) + "₫") : "Original Price: N/A");
            // Load image if available
            if (item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) {
                holder.ivProductImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                    .load(item.getPhotoUris().get(0))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(holder.ivProductImage);
            } else {
                holder.ivProductImage.setVisibility(View.GONE);
            }
        } else {
            holder.tvProductName.setText("Unknown");
            holder.tvProductPrice.setText("Original Price: N/A");
            holder.ivProductImage.setVisibility(View.GONE);
        }
        holder.tvOfferPrice.setText("Offer Price: " + numberFormat.format(offer.getOfferAmount()) + "₫");
        holder.tvOfferMessage.setText(offer.getMessage() != null ? offer.getMessage() : "");
        // Handle withdrawn status
        if (offer.isWithdrawn()) {
            holder.tvOfferPrice.setText("Offer Withdrawn");
            holder.tvOfferPrice.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.tvOfferMessage.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.btnAccept.setEnabled(false);
            holder.btnReject.setEnabled(false);
        } else {
            holder.tvOfferPrice.setTextColor(context.getResources().getColor(R.color.md_theme_secondary));
            holder.tvOfferMessage.setTextColor(context.getResources().getColor(R.color.md_theme_onSurfaceVariant));
            holder.btnAccept.setEnabled(true);
            holder.btnReject.setEnabled(true);
        }
        holder.btnViewDetail.setOnClickListener(v -> {
            if (listing != null && actionListener != null) actionListener.onViewDetail(listing);
        });
        holder.btnAccept.setOnClickListener(v -> {
            if (listing != null && actionListener != null) actionListener.onAccept(offer, listing);
        });
        holder.btnReject.setOnClickListener(v -> {
            if (listing != null && actionListener != null) actionListener.onReject(offer, listing);
        });
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    public static class OfferViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, tvOfferPrice, tvOfferMessage;
        Button btnViewDetail, btnAccept, btnReject;
        ImageView ivProductImage;
        public OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_offer_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_offer_product_price);
            tvOfferPrice = itemView.findViewById(R.id.tv_offer_price);
            tvOfferMessage = itemView.findViewById(R.id.tv_offer_message);
            btnViewDetail = itemView.findViewById(R.id.btn_offer_view_detail);
            btnAccept = itemView.findViewById(R.id.btn_offer_accept);
            btnReject = itemView.findViewById(R.id.btn_offer_reject);
            ivProductImage = itemView.findViewById(R.id.iv_offer_image);
        }
    }
}
