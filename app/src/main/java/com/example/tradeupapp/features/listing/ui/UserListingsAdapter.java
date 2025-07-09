package com.example.tradeupapp.features.listing.ui;

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
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;
import java.util.List;
import java.util.Map;

public class UserListingsAdapter extends RecyclerView.Adapter<UserListingsAdapter.ListingViewHolder> {
    private List<ListingModel> listings;
    private Map<String, ItemModel> itemMap = null;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ListingModel listing);
    }

    public UserListingsAdapter(List<ListingModel> listings) {
        this.listings = listings;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItemMap(Map<String, ItemModel> itemMap) {
        this.itemMap = itemMap;
    }

    @NonNull
    @Override
    public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listing, parent, false);
        return new ListingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingViewHolder holder, int position) {
        ListingModel listing = listings.get(position);
        ItemModel item = (itemMap != null && listing.getItemId() != null) ? itemMap.get(listing.getItemId()) : null;
        holder.bind(listing, item);
    }

    @Override
    public int getItemCount() {
        return listings != null ? listings.size() : 0;
    }

    class ListingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvPrice, tvLocation, tvViewCount, tvTime;
        TextView tvStatus;
        com.google.android.material.chip.Chip chipCondition;
        public ListingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_item_image);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvViewCount = itemView.findViewById(R.id.tv_view_count);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
            chipCondition = itemView.findViewById(R.id.chip_condition);
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(listings.get(getAdapterPosition()));
                }
            });
        }
        public void bind(ListingModel listing, ItemModel item) {
            // Listing info
            tvPrice.setText(String.format("₫%,.0f", listing.getPrice()));
            tvViewCount.setText(String.valueOf(listing.getViewCount()));
            tvStatus.setText(listing.getTransactionStatus());
            // Item info
            if (item != null) {
                tvTitle.setText(item.getTitle());
                tvLocation.setText(item.getLocation());
                chipCondition.setText(item.getCondition());
                if (item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) {
                    Glide.with(itemView.getContext())
                        .load(item.getPhotoUris().get(0))
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .into(ivImage);
                } else {
                    ivImage.setImageResource(R.drawable.ic_image_placeholder);
                }
            } else {
                tvTitle.setText("");
                tvLocation.setText("");
                chipCondition.setText("");
                ivImage.setImageResource(R.drawable.ic_image_placeholder);
            }
            // Time (for demo, just show 'Recently')
            tvTime.setText("Recently");
        }
    }
}
