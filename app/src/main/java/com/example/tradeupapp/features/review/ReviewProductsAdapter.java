package com.example.tradeupapp.features.review;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tradeupapp.R;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewProductsAdapter extends RecyclerView.Adapter<ReviewProductsAdapter.ViewHolder> {
    public static class ReviewInput {
        public float rating;
        public String comment;
        public ReviewInput(float rating, String comment) {
            this.rating = rating;
            this.comment = comment;
        }
    }

    private final List<ListingModel> listings;
    private final Map<String, ItemModel> itemMap;
    private final Map<String, User> userMap;
    private final Map<String, ReviewInput> reviewInputs = new HashMap<>();
    private final Context context;
    private final String revieweeRole; // "seller" or "buyer"

    public ReviewProductsAdapter(Context context, List<ListingModel> listings, Map<String, ItemModel> itemMap, Map<String, User> userMap, String revieweeRole) {
        this.context = context;
        this.listings = listings;
        this.itemMap = itemMap;
        this.userMap = userMap;
        this.revieweeRole = revieweeRole;
    }

    public Map<String, ReviewInput> getReviewInputs() {
        return reviewInputs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListingModel listing = listings.get(position);
        ItemModel item = itemMap.get(listing.getItemId());
        User reviewee = null;
        if (revieweeRole.equals("seller")) {
            reviewee = userMap.get(listing.getSellerId());
        } else if (revieweeRole.equals("buyer")) {
            // If you want to support reviewing buyers
        }
        // Product image
        if (item != null && item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) {
            Glide.with(context).load(item.getPhotoUris().get(0)).placeholder(R.drawable.ic_image_placeholder).into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
        }
        // Product title
        holder.tvProductTitle.setText(item != null ? item.getTitle() : "");
        // Reviewee name
        holder.tvRevieweeName.setText(reviewee != null ? ("To: " + reviewee.getDisplayName()) : "");
        // Restore previous input if any
        ReviewInput input = reviewInputs.get(listing.getId());
        if (input != null) {
            holder.ratingBar.setRating(input.rating);
            holder.etReviewComment.setText(input.comment);
        } else {
            holder.ratingBar.setRating(5f);
            holder.etReviewComment.setText("");
        }
        // Set yellow color for selected stars
        int yellow = ContextCompat.getColor(context, android.R.color.holo_orange_light);
        holder.ratingBar.setProgressTintList(ColorStateList.valueOf(yellow));
        holder.ratingBar.setSecondaryProgressTintList(ColorStateList.valueOf(yellow));
        holder.ratingBar.setThumbTintList(ColorStateList.valueOf(yellow));
        // Save input on change
        holder.ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            reviewInputs.put(listing.getId(), new ReviewInput(rating, holder.etReviewComment.getText().toString()));
        });
        holder.etReviewComment.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                float rating = holder.ratingBar.getRating();
                String comment = holder.etReviewComment.getText().toString();
                reviewInputs.put(listing.getId(), new ReviewInput(rating, comment));
            }
        });
        holder.btnReportReview.setVisibility(View.GONE);
    }

    // Always get latest input from visible views
    public Map<String, ReviewInput> getReviewInputs(RecyclerView recyclerView) {
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            ViewHolder holder = (ViewHolder) recyclerView.getChildViewHolder(child);
            float rating = holder.ratingBar.getRating();
            String comment = holder.etReviewComment.getText().toString();
            reviewInputs.put(listings.get(holder.getAdapterPosition()).getId(), new ReviewInput(rating, comment));
        }
        return reviewInputs;
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductTitle;
        TextView tvRevieweeName;
        RatingBar ratingBar;
        EditText etReviewComment;
        Button btnReportReview;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductTitle = itemView.findViewById(R.id.tv_product_title);
            tvRevieweeName = itemView.findViewById(R.id.tv_reviewee_name);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            etReviewComment = itemView.findViewById(R.id.et_review_comment);
            btnReportReview = itemView.findViewById(R.id.btn_report_review);
        }
    }
}
