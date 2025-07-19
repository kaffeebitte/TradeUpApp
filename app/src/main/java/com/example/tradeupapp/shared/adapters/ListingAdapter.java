package com.example.tradeupapp.shared.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tradeupapp.R;
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;

import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {

    private final List<ListingModel> listingList;
    private final OnItemClickListener listener;
    private final Context context;
    private final FirebaseService firebaseService;

    public interface OnItemClickListener {
        void onItemClick(ListingModel listing);
    }

    public ListingAdapter(Context context, List<ListingModel> listingList, OnItemClickListener listener) {
        this.context = context;
        this.listingList = listingList;
        this.listener = listener;
        this.firebaseService = FirebaseService.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListingModel listing = listingList.get(position);
        holder.price.setText(String.format("₫%,.0f", listing.getPrice()));
        holder.title.setText("Loading...");
        holder.image.setImageResource(R.drawable.ic_image_placeholder);
        holder.status.setText(listing.getTransactionStatus());
        holder.status.setVisibility(listing.getTransactionStatus() != null && !listing.getTransactionStatus().isEmpty() ? View.VISIBLE : View.GONE);
        // Show interaction counts from new structure
        if (listing.getInteractions() != null && listing.getInteractions().getAggregate() != null) {
            holder.viewCount.setText(String.valueOf(listing.getInteractions().getAggregate().getTotalViews()));
        } else {
            holder.viewCount.setText("0");
        }
        // Calculate days since createdAt for tv_time
        java.util.Date createdDate = null;
        if (listing.getCreatedAt() instanceof com.google.firebase.Timestamp) {
            createdDate = ((com.google.firebase.Timestamp) listing.getCreatedAt()).toDate();
        } else if (listing.getCreatedAt() instanceof java.util.Date) {
            createdDate = (java.util.Date) listing.getCreatedAt();
        } else if (listing.getCreatedAt() instanceof String) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US);
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                createdDate = sdf.parse((String) listing.getCreatedAt());
            } catch (Exception ignored) {}
        }
        if (createdDate != null) {
            long diffMillis = System.currentTimeMillis() - createdDate.getTime();
            long days = diffMillis / (1000 * 60 * 60 * 24);
            holder.time.setText(days == 0 ? "Today" : days + " days ago");
        } else {
            holder.time.setText("Recently");
        }
        // Fetch ItemModel for details (title, image, etc.)
        firebaseService.getItemById(listing.getItemId(), new FirebaseService.ItemCallback() {
            @Override
            public void onSuccess(ItemModel item) {
                if (item != null) {
                    holder.title.setText(item.getTitle());
                    if (item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) {
                        Glide.with(context)
                                .load(item.getPhotoUris().get(0))
                                .placeholder(R.drawable.ic_image_placeholder)
                                .into(holder.image);
                    } else {
                        holder.image.setImageResource(R.drawable.ic_image_placeholder);
                    }
                    // Fix: Show location address if available, else show lat/lng or empty
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
                    holder.location.setText(locationText);
                    if (holder.chipCondition != null && item.getCondition() != null)
                        holder.chipCondition.setText(item.getCondition());
                } else {
                    holder.title.setText("No title");
                }
            }
            @Override
            public void onError(String error) {
                holder.title.setText("No title");
            }
        });
        holder.itemView.setOnClickListener(v -> {
            String userId = firebaseService.getCurrentUserId();
            if (userId != null) {
                // Only increment totalViews if user is not the seller
                if (!userId.equals(listing.getSellerId())) {
                    // Increment totalViews in Firestore
                    firebaseService.incrementListingTotalViews(listing.getId());
                }
                firebaseService.logListingViewedInteraction(listing.getId(), userId);
            }
            listener.onItemClick(listing);
        });
        holder.btnSave.setOnClickListener(v -> {
            String userId = firebaseService.getCurrentUserId();
            if (userId != null) {
                firebaseService.logListingSavedInteraction(listing.getId(), userId);
            }
        });
        holder.btnShare.setOnClickListener(v -> {
            String userId = firebaseService.getCurrentUserId();
            if (userId != null) {
                firebaseService.logListingSharedInteraction(listing.getId(), userId);
            }
            // Optional: share intent (native Android share)
            /*
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, holder.title.getText());
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this listing: ...");
            context.startActivity(Intent.createChooser(shareIntent, "Share via"));
            */
        });
    }

    @Override
    public int getItemCount() {
        return listingList.size();
    }

    /**
     * Updates the adapter's dataset with a new list of items.
     * @param newListings The new list of items to display
     */
    public void updateListings(List<ListingModel> newListings) {
        listingList.clear();
        listingList.addAll(newListings);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, price, location, status, viewCount, time;
        com.google.android.material.chip.Chip chipCondition;
        View btnSave, btnShare;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.iv_item_image);
            title = itemView.findViewById(R.id.tv_title);
            price = itemView.findViewById(R.id.tv_price);
            location = itemView.findViewById(R.id.tv_location);
            status = itemView.findViewById(R.id.tv_status);
            viewCount = itemView.findViewById(R.id.tv_view_count);
            time = itemView.findViewById(R.id.tv_time);
            chipCondition = itemView.findViewById(R.id.chip_condition);
            btnSave = itemView.findViewById(R.id.btn_save);
            btnShare = itemView.findViewById(R.id.btn_share);
        }
    }
}
