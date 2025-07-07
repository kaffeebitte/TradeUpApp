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

    private final List<ItemModel> items;
    private final int viewType;
    private final OnItemClickListener listener;
    private final OnFavoriteClickListener favoriteListener;

    public interface OnItemClickListener {
        void onItemClick(ItemModel item);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(ItemModel item, boolean isFavorite);
    }

    public ItemCardAdapter(int viewType, OnItemClickListener listener) {
        this(viewType, listener, null);
    }

    public ItemCardAdapter(int viewType, OnItemClickListener listener, OnFavoriteClickListener favoriteListener) {
        this.items = new ArrayList<>();
        this.viewType = viewType;
        this.listener = listener;
        this.favoriteListener = favoriteListener;
    }

    public void setItems(List<ItemModel> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
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
        ItemModel item = items.get(position);

        if (holder instanceof HorizontalViewHolder) {
            ((HorizontalViewHolder) holder).bind(item, listener);
        } else if (holder instanceof VerticalViewHolder) {
            ((VerticalViewHolder) holder).bind(item, listener, favoriteListener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
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

        public void bind(ItemModel item, OnItemClickListener listener, OnFavoriteClickListener favoriteListener) {
            titleView.setText(item.getTitle());

            // Format price with currency
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            priceView.setText(formatter.format(item.getPrice()));

            if (locationView != null) {
                locationView.setText(item.getLocation());
            }

            if (statusView != null) {
                statusView.setText(item.getStatus());
                // Set visibility of status based on availability
                statusView.setVisibility(item.getStatus() != null && !item.getStatus().isEmpty() ?
                        View.VISIBLE : View.GONE);
            }

            if (timeView != null) {
                // Set time ago text (using a utility method)
                timeView.setText(formatTimeAgo(item.getDateAdded()));
            }

            if (viewCountView != null) {
                // Set view count
                viewCountView.setText(String.valueOf(item.getViewCount()));
            }

            // Load image if available
            if (item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) {
                Uri imageUri = item.getPhotoUris().get(0);
                Glide.with(imageView.getContext())
                        .load(imageUri)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(imageView);
            } else {
                // Set a default placeholder image
                imageView.setImageResource(R.drawable.placeholder_image);
            }

            // Set favorite button click listener if available
            if (favoriteButton != null && favoriteListener != null) {
                favoriteButton.setOnClickListener(v -> {
                    // Toggle favorite state (actual favorite state should be managed by the app)
                    favoriteListener.onFavoriteClick(item, true);
                });
            }

            // Set item click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
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

        public void bind(ItemModel item, OnItemClickListener listener) {
            titleView.setText(item.getTitle());

            // Format price with currency
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            priceView.setText(formatter.format(item.getPrice()));

            locationView.setText(item.getLocation());
            conditionView.setText(item.getCondition());

            // Load image if available
            if (item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) {
                Uri imageUri = item.getPhotoUris().get(0);
                Glide.with(imageView.getContext())
                        .load(imageUri)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(imageView);
            } else {
                // Set a default placeholder image
                imageView.setImageResource(R.drawable.placeholder_image);
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
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
    public static ItemCardAdapter createVerticalAdapter(OnItemClickListener listener) {
        return new ItemCardAdapter(VIEW_TYPE_VERTICAL, listener);
    }

    public static ItemCardAdapter createVerticalAdapter(OnItemClickListener listener, OnFavoriteClickListener favoriteListener) {
        return new ItemCardAdapter(VIEW_TYPE_VERTICAL, listener, favoriteListener);
    }

    public static ItemCardAdapter createHorizontalAdapter(OnItemClickListener listener) {
        return new ItemCardAdapter(VIEW_TYPE_HORIZONTAL, listener);
    }
}
