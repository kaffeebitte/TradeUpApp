package com.example.tradeupapp.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for handling Cloudinary media uploads.
 */
public class CloudinaryManager {
    private static final String TAG = "CloudinaryManager";
    private static boolean isInitialized = false;
    private static CloudinaryManager instance;
    private Context context;

    private CloudinaryManager(Context context) {
        this.context = context.getApplicationContext();
        init(this.context);
    }

    /**
     * Get singleton instance of CloudinaryManager
     */
    public static synchronized CloudinaryManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CloudinaryManager is not initialized, call initialize(Context) first");
        }
        return instance;
    }

    /**
     * Initialize the CloudinaryManager singleton instance
     */
    public static synchronized void initialize(Context context) {
        if (instance == null) {
            instance = new CloudinaryManager(context);
        }
    }

    /**
     * Initialize Cloudinary with configuration
     * Call this method in Application class
     */
    public static void init(Context context) {
        if (!isInitialized) {
            try {
                Map<String, String> config = new HashMap<>();
                config.put("cloud_name", "dl4ad9k0c"); // Replace with your Cloudinary cloud name
                config.put("api_key", "664574146651255");       // Replace with your Cloudinary API key
                config.put("api_secret", "UtDmBDBd_yo2blPYVaOGIobYy9Y"); // Replace with your Cloudinary API secret
                config.put("secure", "true");
                MediaManager.init(context, config);
                isInitialized = true;
            } catch (Exception e) {
                Log.e(TAG, "Error initializing Cloudinary: " + e.getMessage());
            }
        }
    }

    /**
     * Upload an image to Cloudinary using the singleton instance
     */
    public void uploadImage(Uri imageUri, final ImageUploadCallback callback) {
        uploadImage(context, imageUri, "profile_images", new CloudinaryUploadCallback() {
            @Override
            public void onStart() {
                // No corresponding method in ImageUploadCallback
            }

            @Override
            public void onProgress(double progress) {
                // No corresponding method in ImageUploadCallback
            }

            @Override
            public void onSuccess(String url) {
                callback.onSuccess(url);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Upload an image to Cloudinary
     *
     * @param context Android context
     * @param imageUri URI of the image to upload
     * @param folder Cloudinary folder to upload to
     * @param callback Callback for upload result
     * @return requestId The ID of the upload request
     */
    public static String uploadImage(Context context, Uri imageUri, String folder, final CloudinaryUploadCallback callback) {
        if (!isInitialized) {
            init(context);
        }

        String uniqueFileName = folder + "/" + UUID.randomUUID().toString();

        return MediaManager.get().upload(imageUri)
                .option("folder", folder)
                .option("public_id", uniqueFileName)
                .option("resource_type", "image")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Upload started: " + requestId);
                        callback.onStart();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        Log.d(TAG, "Upload progress: " + requestId + " " + bytes + "/" + totalBytes);
                        double progress = (double) bytes / totalBytes;
                        callback.onProgress(progress);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Log.d(TAG, "Upload success: " + requestId);
                        String url = (String) resultData.get("secure_url");
                        callback.onSuccess(url);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Upload error: " + requestId + " " + error.getDescription());
                        callback.onError(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.d(TAG, "Upload rescheduled: " + requestId + " " + error.getDescription());
                        callback.onError("Upload rescheduled: " + error.getDescription());
                    }
                })
                .dispatch();
    }

    /**
     * Callback interface for Cloudinary uploads
     */
    public interface CloudinaryUploadCallback {
        void onStart();
        void onProgress(double progress);
        void onSuccess(String url);
        void onError(String errorMessage);
    }

    /**
     * Simplified callback interface for image uploads
     */
    public interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }
}
