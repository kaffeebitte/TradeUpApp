package com.example.tradeupapp.features.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.tradeupapp.R;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.core.services.ItemRepository;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class ItemCardAdapter extends RecyclerView.Adapter<ItemCardAdapter.ItemViewHolder> {

    private final List<ListingModel> listings;
    private final OnItemClickListener listener;
    private final OnSaveClickListener saveListener;
    private final ItemRepository itemRepository;

    public interface OnItemClickListener {
        void onItemClick(ListingModel listing);
    }

    public interface OnSaveClickListener {
        void onSaveClick(ListingModel listing, boolean isSaved);
    }

    public ItemCardAdapter(OnItemClickListener listener, ItemRepository itemRepository) {
        this.listings = new ArrayList<>();
        this.listener = listener;
        this.saveListener = null;
        this.itemRepository = itemRepository;
    }

    public ItemCardAdapter(OnItemClickListener listener, OnSaveClickListener saveListener, ItemRepository itemRepository) {
        this.listings = new ArrayList<>();
        this.listener = listener;
        this.saveListener = saveListener;
        this.itemRepository = itemRepository;
    }

    public void setItems(List<ListingModel> newListings) {
        this.listings.clear();
        this.listings.addAll(newListings);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_listing, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ListingModel listing = listings.get(position);
        holder.bind(listing, listener, saveListener, itemRepository);
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    // Single ViewHolder for item_listing.xml
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView imageView;
        private final TextView titleView;
        private final TextView priceView;
        private final TextView locationView;
        private final TextView statusView;
        private final TextView timeView;
        private final TextView viewCountView;
        private final MaterialCardView saveButton;
        private final ImageView saveIcon;
        private final Chip chipCondition;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_item_image);
            titleView = itemView.findViewById(R.id.tv_title);
            priceView = itemView.findViewById(R.id.tv_price);
            locationView = itemView.findViewById(R.id.tv_location);
            statusView = itemView.findViewById(R.id.tv_status);
            timeView = itemView.findViewById(R.id.tv_time);
            viewCountView = itemView.findViewById(R.id.tv_view_count);
            saveButton = itemView.findViewById(R.id.btn_save);
            saveIcon = itemView.findViewById(R.id.iv_save);
            chipCondition = itemView.findViewById(R.id.chip_condition);
        }

        public void bind(ListingModel listing, OnItemClickListener listener, OnSaveClickListener saveListener, ItemRepository itemRepository) {
            // Listing info
            priceView.setText(String.format("₫%,.0f", listing.getPrice()));
            if (statusView != null) {
                statusView.setText(listing.getTransactionStatus());
                statusView.setVisibility(listing.getTransactionStatus() != null && !listing.getTransactionStatus().isEmpty() ? View.VISIBLE : View.GONE);
            }
            if (timeView != null) {
                timeView.setText(formatTimeAgo(listing.getCreatedAtTimestamp().toDate()));
            }
            if (viewCountView != null) {
                viewCountView.setText(String.valueOf(listing.getViewCount()));
            }
            // Fetch ItemModel asynchronously
            itemRepository.getItemById(listing.getItemId(), new ItemRepository.ItemCallback() {
                @Override
                public void onItemLoaded(ItemModel item) {
                    if (item != null) {
                        titleView.setText(item.getTitle());
                        if (locationView != null) {
                            String locationText = "";
                            if (item.getLocation() != null) {
                                Object addressObj = item.getLocation().get("address");
                                if (addressObj != null) {
                                    locationText = addressObj.toString();
                                } else {
                                    Double lat = item.getLocationLatitude();
                                    Double lng = item.getLocationLongitude();
                                    if (lat != null && lng != null) {
                                        locationText = String.format("%.5f, %.5f", lat, lng);
                                    }
                                }
                            }
                            locationView.setText(locationText);
                        }
                        if (chipCondition != null && item.getCondition() != null) chipCondition.setText(item.getCondition());
                        // Load image if available
                        if (item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) {
                            Glide.with(imageView.getContext())
                                    .load(item.getPhotoUris().get(0))
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .centerCrop()
                                    .placeholder(R.drawable.placeholder_image)
                                    .error(R.drawable.error_image)
                                    .into(imageView);
                        } else {
                            imageView.setImageResource(R.drawable.placeholder_image);
                        }
                        // Do NOT override statusView here, always show transactionStatus from ListingModel only
                    }
                }
            });

            if (saveButton != null && saveListener != null) {
                saveButton.setOnClickListener(v -> {
                    // Toggle save state (actual save state should be managed by the app)
                    saveListener.onSaveClick(listing, true);
                });
            }

            // Set item click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(listing);
                }
            });
        }
    }

    // Helper method to format time ago string
    private static String formatTimeAgo(java.util.Date date) {
        if (date == null) return "Recently";

        long timeDiff = System.currentTimeMillis() - date.getTime();
        long daysDiff = timeDiff / (1000 * 60 * 60 * 24);

        if (daysDiff == 0) return "Today";
        else if (daysDiff == 1) return "Yesterday";
        else if (daysDiff < 7) return daysDiff + " days ago";
        else if (daysDiff < 30) return (daysDiff / 7) + " weeks ago";
        else if (daysDiff < 365) return (daysDiff / 30) + " months ago";
        else return (daysDiff / 365) + " years ago";
    }

    // Static factory methods for easy instantiation
    public static ItemCardAdapter createVerticalAdapter(OnItemClickListener listener, ItemRepository itemRepository) {
        return new ItemCardAdapter(listener, itemRepository);
    }

    public static ItemCardAdapter createVerticalAdapter(OnItemClickListener listener, OnSaveClickListener saveListener, ItemRepository itemRepository) {
        return new ItemCardAdapter(listener, saveListener, itemRepository);
    }
}
