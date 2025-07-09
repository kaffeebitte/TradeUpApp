package com.example.tradeupapp.features.home.adapter;

import android.net.Uri;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Define view types for different card layouts - using only 2 types now
    public static final int VIEW_TYPE_VERTICAL = 0;  // Full featured listing (vertical)
    public static final int VIEW_TYPE_HORIZONTAL = 1;  // Horizontal scrolling card

    private final List<ListingModel> listings;
    private final int viewType;
    private final OnItemClickListener listener;
    private final OnFavoriteClickListener favoriteListener;
    private final ItemRepository itemRepository;

    public interface OnItemClickListener {
        void onItemClick(ListingModel listing);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(ListingModel listing, boolean isFavorite);
    }

    public ItemCardAdapter(int viewType, OnItemClickListener listener, ItemRepository itemRepository) {
        this.listings = new ArrayList<>();
        this.viewType = viewType;
        this.listener = listener;
        this.favoriteListener = null;
        this.itemRepository = itemRepository;
    }

    public ItemCardAdapter(int viewType, OnItemClickListener listener, OnFavoriteClickListener favoriteListener, ItemRepository itemRepository) {
        this.listings = new ArrayList<>();
        this.viewType = viewType;
        this.listener = listener;
        this.favoriteListener = favoriteListener;
        this.itemRepository = itemRepository;
    }

    public void setItems(List<ListingModel> newListings) {
        this.listings.clear();
        this.listings.addAll(newListings);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (this.viewType == VIEW_TYPE_HORIZONTAL) {
            View horizontalView = inflater.inflate(R.layout.item_card_horizontal, parent, false);
            return new HorizontalViewHolder(horizontalView);
        } else {
            View verticalView = inflater.inflate(R.layout.item_listing, parent, false);
            return new VerticalViewHolder(verticalView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListingModel listing = listings.get(position);

        if (holder instanceof HorizontalViewHolder) {
            ((HorizontalViewHolder) holder).bind(listing, listener, itemRepository);
        } else if (holder instanceof VerticalViewHolder) {
            ((VerticalViewHolder) holder).bind(listing, listener, favoriteListener, itemRepository);
        }
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    // ViewHolder for item_listing.xml (vertical layout)
    static class VerticalViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView imageView;
        private final TextView titleView;
        private final TextView priceView;
        private final TextView locationView;
        private final TextView statusView;
        private final TextView timeView;
        private final TextView viewCountView;
        private final MaterialCardView favoriteButton;
        private final ImageView favoriteIcon;

        public VerticalViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_item_image);
            titleView = itemView.findViewById(R.id.tv_title);
            priceView = itemView.findViewById(R.id.tv_price);
            locationView = itemView.findViewById(R.id.tv_location);
            statusView = itemView.findViewById(R.id.tv_status);
            timeView = itemView.findViewById(R.id.tv_time);
            viewCountView = itemView.findViewById(R.id.tv_view_count);
            favoriteButton = itemView.findViewById(R.id.btn_favorite);
            favoriteIcon = itemView.findViewById(R.id.iv_favorite);
        }

        public void bind(ListingModel listing, OnItemClickListener listener, OnFavoriteClickListener favoriteListener, ItemRepository itemRepository) {
            // Set placeholders first
            titleView.setText("Loading...");
            priceView.setText(String.valueOf(listing.getPrice()));

            // Fetch ItemModel asynchronously
            itemRepository.getItemById(listing.getItemId(), new ItemRepository.ItemCallback() {
                @Override
                public void onItemLoaded(ItemModel item) {
                    if (item != null) {
                        titleView.setText(item.getTitle());
                        if (locationView != null) locationView.setText(item.getLocation());

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
                    }
                }
            });

            if (statusView != null) {
                statusView.setText(listing.getTransactionStatus());
                // Set visibility of status based on availability
                statusView.setVisibility(listing.getTransactionStatus() != null && !listing.getTransactionStatus().isEmpty() ?
                        View.VISIBLE : View.GONE);
            }

            if (timeView != null) {
                // Set time ago text (using a utility method)
                timeView.setText(formatTimeAgo(listing.getCreatedAt() != null ? listing.getCreatedAt().toDate() : null));
            }

            if (viewCountView != null) {
                // Set view count
                viewCountView.setText(String.valueOf(listing.getViewCount()));
            }

            // Set favorite button click listener if available
            if (favoriteButton != null && favoriteListener != null) {
                favoriteButton.setOnClickListener(v -> {
                    // Toggle favorite state (actual favorite state should be managed by the app)
                    favoriteListener.onFavoriteClick(listing, true);
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

    // ViewHolder for item_card_horizontal.xml (horizontal scrolling)
    static class HorizontalViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView titleView;
        private final TextView priceView;
        private final TextView locationView;
        private final TextView conditionView;

        public HorizontalViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_item_image);
            titleView = itemView.findViewById(R.id.tv_item_title);
            priceView = itemView.findViewById(R.id.tv_item_price);
            locationView = itemView.findViewById(R.id.tv_item_location);
            conditionView = itemView.findViewById(R.id.tv_item_condition);
        }

        public void bind(ListingModel listing, OnItemClickListener listener, ItemRepository itemRepository) {
            titleView.setText("Loading...");
            priceView.setText(String.valueOf(listing.getPrice()));

            itemRepository.getItemById(listing.getItemId(), new ItemRepository.ItemCallback() {
                @Override
                public void onItemLoaded(ItemModel item) {
                    if (item != null) {
                        titleView.setText(item.getTitle());
                        locationView.setText(item.getLocation());
                        conditionView.setText(item.getCondition());

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
                    }
                }
            });

            // Set click listener
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
        return new ItemCardAdapter(VIEW_TYPE_VERTICAL, listener, itemRepository);
    }

    public static ItemCardAdapter createVerticalAdapter(OnItemClickListener listener, OnFavoriteClickListener favoriteListener, ItemRepository itemRepository) {
        return new ItemCardAdapter(VIEW_TYPE_VERTICAL, listener, favoriteListener, itemRepository);
    }

    public static ItemCardAdapter createHorizontalAdapter(OnItemClickListener listener, ItemRepository itemRepository) {
        return new ItemCardAdapter(VIEW_TYPE_HORIZONTAL, listener, itemRepository);
    }
}
