package com.example.tradeupapp.utils

import android.content.Context
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.util.*

/**
 * Helper class for Cloudinary media management
 * Used for uploading and managing images/videos in TradeUpApp
 */
object CloudinaryHelper {

    private var isInitialized = false
    private var currentCloudName: String? = null

    /**
     * Initialize Cloudinary with your credentials
     * Call this in your Application class or MainActivity
     */
    fun initCloudinary(context: Context, cloudName: String, apiKey: String, apiSecret: String) {
        if (!isInitialized) {
            val config = mapOf(
                "cloud_name" to cloudName,
                "api_key" to apiKey,
                "api_secret" to apiSecret
            )
            MediaManager.init(context, config)
            isInitialized = true
            currentCloudName = cloudName
        }
    }

    /**
     * Upload an image to Cloudinary
     * @param filePath Local file path of the image
     * @param folder Cloudinary folder to upload to (e.g., "trade_items", "user_profiles")
     * @param callback Callback for upload result
     */
    fun uploadImage(
        filePath: String,
        folder: String = "trade_items",
        callback: (String?, String?) -> Unit
    ) {
        if (!isInitialized) {
            callback(null, "Cloudinary not initialized")
            return
        }

        val requestId = MediaManager.get().upload(filePath)
            .option("folder", folder)
            .option("resource_type", "image")
            .option("quality", "auto:good")
            .option("fetch_format", "auto")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    // Upload started
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Upload progress
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as? String
                    // The public_id can be extracted from the secure_url if needed or returned separately
                    callback(secureUrl, null)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    callback(null, error.description)
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    // Upload rescheduled
                }
            })
            .dispatch()
    }

    /**
     * Generate optimized image URL for display
     * @param publicId The public ID returned from upload
     * @param width Desired width (optional)
     * @param height Desired height (optional)
     * @return Optimized image URL
     */
    fun getOptimizedImageUrl(
        publicId: String,
        width: Int? = null,
        height: Int? = null
    ): String {
        val cloudName = getCloudName()
        if (cloudName == "your_cloud_name") {
            // Optionally, handle the case where cloud name is not yet set or use a default
            // Log an error or return a placeholder URL
        }
        val baseUrl = "https://res.cloudinary.com/$cloudName/image/upload/"
        val transformations = mutableListOf<String>()

        // Add quality and format optimizations
        transformations.add("q_auto")
        transformations.add("f_auto")

        // Add dimensions if provided
        width?.let { w ->
            height?.let { h ->
                // Using c_limit to avoid upscaling and maintain aspect ratio
                // c_fill will crop if aspect ratio is different
                transformations.add("w_$w,h_$h,c_limit")
            } ?: transformations.add("w_$w")
        }

        val transformation = if (transformations.isNotEmpty()) {
            transformations.joinToString(",") + "/"
        } else ""

        // Ensure publicId does not contain the file extension if transformations are applied
        return "$baseUrl$transformation${extractPublicIdFromUrl(publicId)}"
    }

    /**
     * Delete an image from Cloudinary
     * @param publicId The public ID of the image to delete
     * @param callback Callback for deletion result
     */
    fun deleteImage(publicId: String, callback: (Boolean, String?) -> Unit) {
        if (!isInitialized) {
            callback(false, "Cloudinary not initialized")
            return
        }

        // Deletion from the client-side is generally not recommended for security reasons
        // as it requires your API secret to be exposed.
        // This functionality is often handled by a backend server.
        // If you must do it client-side (e.g., for development or specific use cases),
        // ensure you understand the security implications.

        // For client-side deletion (less secure, requires signed uploads or specific setup):
        // MediaManager.get().destroy(publicId, object : UploadCallback { ... })
        // However, this typically requires signed uploads or other configurations.

        // Placeholder for server-side deletion logic
        // You would typically make an API call to your backend server here,
        // which then securely calls the Cloudinary Admin API.
        println("Deletion of $publicId requested. Implement server-side deletion for security.")
        callback(false, "Deletion is a server-side operation. This is a placeholder.")
    }

    /**
     * Extracts the public ID from a Cloudinary URL.
     * Handles cases where the URL might already be transformed.
     */
    private fun extractPublicIdFromUrl(url: String): String {
        // Example URL: https://res.cloudinary.com/demo/image/upload/v1572253805/sample.jpg
        // Example URL with transformation: https://res.cloudinary.com/demo/image/upload/w_200,h_150,c_fill/v1572253805/sample.jpg
        // We need to get "sample" (without .jpg if transformations are applied, or "v1572253805/sample")
        val parts = url.split("/")
        // The public ID part is usually after /upload/ or /upload/transformations/
        val uploadIndex = parts.indexOf("upload")
        return parts.subList(uploadIndex + 1, parts.size).joinToString("/").substringBeforeLast(".")
    }

    private fun getCloudName(): String {
        return currentCloudName ?: "your_cloud_name" // Fallback if not initialized
    }
}
