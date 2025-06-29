package com.example.tradeupapp.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.tradeupapp.R;

/**
 * Utility class for handling image loading throughout the app.
 * Provides consistent image loading behavior with proper caching and error handling.
 */
public class ImageLoadingUtils {

    private static final RequestOptions DEFAULT_OPTIONS = new RequestOptions()
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_error_24)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)
            .centerInside();

    /**
     * Loads an image into an ImageView with default options
     * @param context Context
     * @param imageUri Uri of the image to load
     * @param imageView ImageView to load the image into
     */
    public static void loadImage(Context context, Uri imageUri, ImageView imageView) {
        loadImage(context, imageUri, imageView, null, DEFAULT_OPTIONS);
    }

    /**
     * Loads an image into an ImageView with a callback for load completion
     * @param context Context
     * @param imageUri Uri of the image to load
     * @param imageView ImageView to load the image into
     * @param callback Callback for image load events
     */
    public static void loadImage(Context context, Uri imageUri, ImageView imageView,
                                ImageLoadCallback callback) {
        loadImage(context, imageUri, imageView, callback, DEFAULT_OPTIONS);
    }

    /**
     * Loads an image with custom options and a completion callback
     * @param context Context
     * @param imageUri Uri of the image to load
     * @param imageView ImageView to load the image into
     * @param callback Callback for image load events
     * @param options Custom RequestOptions for loading
     */
    public static void loadImage(Context context, Uri imageUri, ImageView imageView,
                                @Nullable ImageLoadCallback callback, RequestOptions options) {
        if (context == null || imageView == null) {
            if (callback != null) {
                callback.onLoadFailed();
            }
            return;
        }

        try {
            RequestBuilder<Drawable> requestBuilder = Glide.with(context)
                    .load(imageUri)
                    .apply(options);

            if (callback != null) {
                requestBuilder.listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                               Target<Drawable> target, boolean isFirstResource) {
                        callback.onLoadFailed();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                 Target<Drawable> target, DataSource dataSource,
                                                 boolean isFirstResource) {
                        callback.onLoadSuccessful();
                        return false;
                    }
                });
            }

            requestBuilder.into(imageView);
        } catch (Exception e) {
            if (callback != null) {
                callback.onLoadFailed();
            }
        }
    }

    /**
     * Preloads an image into memory cache for faster loading later
     * @param context Context
     * @param imageUri Uri of the image to preload
     */
    public static void preloadImage(Context context, Uri imageUri) {
        if (context == null || imageUri == null) {
            return;
        }

        Glide.with(context)
                .load(imageUri)
                .apply(DEFAULT_OPTIONS.clone().diskCacheStrategy(DiskCacheStrategy.DATA))
                .preload();
    }

    /**
     * Callback interface for image loading events
     */
    public interface ImageLoadCallback {
        void onLoadSuccessful();
        void onLoadFailed();
    }
}
