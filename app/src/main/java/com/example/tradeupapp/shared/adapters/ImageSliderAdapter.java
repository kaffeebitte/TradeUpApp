package com.example.tradeupapp.shared.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.tradeupapp.R;

import java.util.List;

/**
 * Adapter for displaying images in a ViewPager2 with optimized loading
 */
public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder> {

    private final Context context;
    private final List<Uri> imageUris;
    private final RequestOptions glideOptions;

    public ImageSliderAdapter(Context context, List<Uri> imageUris) {
        this.context = context;
        this.imageUris = imageUris;

        // Pre-configure Glide options for better performance
        this.glideOptions = new RequestOptions()
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_error_24)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerInside();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_slider, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        if (position < 0 || position >= imageUris.size()) {
            return; // Safety check
        }

        Uri imageUri = imageUris.get(position);

        // Show loading indicator
        holder.progressBar.setVisibility(View.VISIBLE);

        // Load image using Glide with optimized settings
        Glide.with(context)
                .load(imageUri)
                .apply(glideOptions)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model,
                                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model,
                                                  com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                  com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUris != null ? imageUris.size() : 0;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public ProgressBar progressBar;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
}
