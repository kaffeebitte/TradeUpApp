package com.example.tradeupapp.features.listing.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;

import java.util.ArrayList;
import java.util.List;

public class PhotoUploadAdapter extends RecyclerView.Adapter<PhotoUploadAdapter.PhotoViewHolder> {

    private static final int MAX_PHOTOS = 10;
    private List<Uri> photoUris;
    private OnPhotoActionListener listener;

    public interface OnPhotoActionListener {
        void onAddPhotoClicked();
        void onPhotoRemoved(int position);
    }

    public PhotoUploadAdapter(OnPhotoActionListener listener) {
        this.photoUris = new ArrayList<>();
        this.listener = listener;
        // Add empty placeholder to show the "add photo" button
        if (photoUris.isEmpty()) {
            photoUris.add(null);
        }
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_upload, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Uri photoUri = photoUris.get(position);

        if (photoUri == null) {
            // This is an empty slot (add photo button)
            holder.ivPhoto.setVisibility(View.GONE);
            holder.ivAddPhoto.setVisibility(View.VISIBLE);
            holder.btnRemovePhoto.setVisibility(View.GONE);

            holder.ivAddPhoto.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddPhotoClicked();
                }
            });
        } else {
            // This is a photo
            holder.ivPhoto.setVisibility(View.VISIBLE);
            holder.ivAddPhoto.setVisibility(View.GONE);
            holder.btnRemovePhoto.setVisibility(View.VISIBLE);

            // Load image
            holder.ivPhoto.setImageURI(photoUri);

            // Set up remove button
            final int photoPosition = position;
            holder.btnRemovePhoto.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPhotoRemoved(photoPosition);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return photoUris.size();
    }

    /**
     * Add a new photo URI to the adapter
     * @param uri The URI of the photo to add
     * @return true if photo was added, false if limit reached
     */
    public boolean addPhoto(Uri uri) {
        // Validate input and check photo limit
        if (uri == null || getPhotoCount() >= MAX_PHOTOS) {
            return false;
        }

        // Reorganize the list: remove add button, add photo, then add button at end
        removeAddButton();
        photoUris.add(uri);
        addAddButtonIfNeeded();

        notifyDataSetChanged();
        return true;
    }

    /**
     * Remove a photo at the specified position
     * @param position Position of the photo to remove
     */
    public void removePhoto(int position) {
        // Only remove valid photo items (not null/add button)
        if (position >= 0 && position < photoUris.size() && photoUris.get(position) != null) {
            photoUris.remove(position);
            addAddButtonIfNeeded();
            notifyDataSetChanged();
        }
    }

    /**
     * Removes the add button (null item) from the list if it exists
     */
    private void removeAddButton() {
        // Use Iterator to avoid ConcurrentModificationException
        photoUris.removeIf(uri -> uri == null);
    }

    /**
     * Adds the add button (null) at the end of the list if needed
     */
    private void addAddButtonIfNeeded() {
        // Add the button if we have room for more photos and no button exists
        if (getPhotoCount() < MAX_PHOTOS && !hasAddButton()) {
            photoUris.add(null);
        }
    }

    /**
     * Checks if the list already has an add button
     * @return true if add button exists
     */
    private boolean hasAddButton() {
        return photoUris.contains(null);
    }

    /**
     * Get the number of actual photos (excluding the add button)
     */
    public int getPhotoCount() {
        int count = 0;
        for (Uri uri : photoUris) {
            if (uri != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get all photo URIs (excluding null/add button)
     */
    public List<Uri> getPhotoUris() {
        List<Uri> result = new ArrayList<>();
        for (Uri uri : photoUris) {
            if (uri != null) {
                result.add(uri);
            }
        }
        return result;
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        ImageView ivAddPhoto;
        ImageButton btnRemovePhoto;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            ivAddPhoto = itemView.findViewById(R.id.iv_add_photo);
            btnRemovePhoto = itemView.findViewById(R.id.btn_remove_photo);
        }
    }
}
