package com.example.tradeupapp.shared.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tradeupapp.R;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ReviewModel;

import java.util.List;
import java.util.Map;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private final List<ReviewModel> reviewList;
    private final Context context;
    private Map<String, com.example.tradeupapp.models.ListingModel> listingMap;
    private Map<String, ItemModel> itemMap;
    private Map<String, com.example.tradeupapp.models.User> userMap;

    public ReviewAdapter(Context context, List<ReviewModel> reviewList) {
        this(context, reviewList, null, null, null);
    }

    public ReviewAdapter(Context context, List<ReviewModel> reviewList, Map<String, com.example.tradeupapp.models.ListingModel> listingMap, Map<String, ItemModel> itemMap, Map<String, com.example.tradeupapp.models.User> userMap) {
        this.context = context;
        this.reviewList = reviewList;
        this.listingMap = listingMap;
        this.itemMap = itemMap;
        this.userMap = userMap;
    }

    public void setListingMap(Map<String, com.example.tradeupapp.models.ListingModel> listingMap) {
        this.listingMap = listingMap;
        notifyDataSetChanged();
    }
    public void setItemMap(Map<String, ItemModel> itemMap) {
        this.itemMap = itemMap;
        notifyDataSetChanged();
    }
    public void setUserMap(Map<String, com.example.tradeupapp.models.User> userMap) {
        this.userMap = userMap;
        if (userMap != null) {
            for (Map.Entry<String, com.example.tradeupapp.models.User> entry : userMap.entrySet()) {
                android.util.Log.d("ReviewAdapter", "userMap key: " + entry.getKey() + ", displayName: " + entry.getValue().getDisplayName());
            }
        } else {
            android.util.Log.d("ReviewAdapter", "userMap is null");
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review_product, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewModel review = reviewList.get(position);
        // Get product title and image from listingId -> ListingModel -> itemId -> ItemModel
        String listingId = review.getListingId();
        ItemModel item = null;
        if (listingMap != null && listingMap.containsKey(listingId)) {
            com.example.tradeupapp.models.ListingModel listing = listingMap.get(listingId);
            if (listing != null && itemMap != null && listing.getItemId() != null) {
                item = itemMap.get(listing.getItemId());
            }
        }
        if (item != null) {
            holder.tvProductTitle.setText(item.getTitle());
            if (item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) {
                Glide.with(context)
                        .load(item.getPhotoUris().get(0))
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .into(holder.ivProductImage);
            } else {
                holder.ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            holder.tvProductTitle.setText("");
            holder.ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
        }
        // Hide product title if null or empty (for detail page)
        if (holder.tvProductTitle != null) {
            if (holder.tvProductTitle.getText() == null || holder.tvProductTitle.getText().toString().trim().isEmpty()) {
                holder.tvProductTitle.setVisibility(View.GONE);
            } else {
                holder.tvProductTitle.setVisibility(View.VISIBLE);
            }
        }
        // Show reviewer's displayName if available, fallback to id
        String reviewerId = review.getReviewerId();
        String reviewerDisplayName = reviewerId;
        if (userMap != null && userMap.containsKey(reviewerId)) {
            String displayName = userMap.get(reviewerId).getDisplayName();
            android.util.Log.d("ReviewAdapter", "reviewerId: " + reviewerId + ", displayName: " + displayName);
            if (displayName != null && !displayName.isEmpty()) {
                reviewerDisplayName = displayName;
            }
        } else {
            android.util.Log.d("ReviewAdapter", "reviewerId: " + reviewerId + " not found in userMap");
        }
        holder.tvRevieweeName.setText(reviewerDisplayName);
        holder.ratingBar.setRating(review.getRating());
        holder.ratingBar.setIsIndicator(true); // Only display, no interaction
        // Set star color based on rating
        int color;
        if (review.getRating() >= 4.5f) {
            color = context.getResources().getColor(R.color.gold, null); // 5 sao vàng đậm
        } else if (review.getRating() >= 4.0f) {
            color = context.getResources().getColor(R.color.md_theme_secondary, null); // 4 sao vàng nhạt
        } else if (review.getRating() >= 3.0f) {
            color = context.getResources().getColor(R.color.giants_orange, null); // 3 sao cam
        } else {
            color = context.getResources().getColor(R.color.md_theme_error, null); // 1-2 sao đỏ
        }
        holder.ratingBar.getProgressDrawable().setTint(color);
        holder.ratingBar.setFocusable(false);
        holder.ratingBar.setClickable(false);
        holder.ratingBar.setEnabled(false);
        holder.tvComment.setText(review.getComment());
        holder.tvComment.setEnabled(false);
        holder.tvComment.setFocusable(false);
        holder.tvComment.setCursorVisible(false);
        holder.tvComment.setKeyListener(null);
        holder.btnReportReview.setVisibility(View.VISIBLE);
        holder.btnReportReview.setOnClickListener(v -> {
            // Show a simple dialog for now
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setTitle("Report Review")
                .setMessage("Do you want to report this review?")
                .setPositiveButton("Report", (dialog, which) -> {
                    // TODO: Implement report logic (e.g., call a callback or service)
                    android.widget.Toast.makeText(context, "Review reported!", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductTitle, tvRevieweeName, tvComment;
        RatingBar ratingBar;
        Button btnReportReview;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductTitle = itemView.findViewById(R.id.tv_product_title);
            tvRevieweeName = itemView.findViewById(R.id.tv_reviewee_name);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvComment = itemView.findViewById(R.id.et_review_comment);
            btnReportReview = itemView.findViewById(R.id.btn_report_review);
        }
    }
}
