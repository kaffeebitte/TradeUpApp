package com.example.tradeupapp.shared.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import com.example.tradeupapp.R;
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class UserListingAdapter extends RecyclerView.Adapter<UserListingAdapter.ViewHolder> {

    private final List<ListingModel> listingList;
    private final FirebaseService firebaseService = FirebaseService.getInstance();
    private final OnItemActionListener listener;
    private final Context context;

    public interface OnItemActionListener {
        void onView(ListingModel listing, ItemModel item);
        void onEdit(ListingModel listing, ItemModel item);
        void onDelete(ListingModel listing, ItemModel item);
    }

    public UserListingAdapter(Context context, List<ListingModel> listingList, OnItemActionListener listener) {
        this.listingList = listingList;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListingModel listing = listingList.get(position);
        firebaseService.getItemById(listing.getItemId(), new FirebaseService.ItemCallback() {
            @Override
            public void onSuccess(ItemModel item) {
                holder.title.setText(item != null ? item.getTitle() : "");
                // Load first image (placeholder if null)
                if (item != null && item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) {
                    Uri firstImageUri = Uri.parse(item.getPhotoUris().get(0));
                    Glide.with(context)
                        .load(firstImageUri)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .centerCrop()
                        .into(holder.image);
                } else {
                    holder.image.setImageResource(R.drawable.ic_image_placeholder);
                }
                holder.itemView.setOnClickListener(v -> listener.onView(listing, item));
                holder.btnEdit.setOnClickListener(v -> listener.onEdit(listing, item));
                holder.btnDelete.setOnClickListener(v -> listener.onDelete(listing, item));
            }
            @Override
            public void onError(String error) {
                holder.title.setText("");
                holder.image.setImageResource(R.drawable.ic_image_placeholder);
            }
        });
        holder.price.setText(String.format("₫%,.0f", listing.getPrice()));
        String statusText = listing.getTransactionStatus() != null ? listing.getTransactionStatus() : "Available";
        holder.status.setText(statusText);
        // Show interaction counts from new structure, remove dots before numbers
        int viewsCount = (listing.getInteractions() != null && listing.getInteractions().getAggregate() != null) ?
                listing.getInteractions().getAggregate().getTotalViews() : 0;
        int savesCount = (listing.getInteractions() != null && listing.getInteractions().getAggregate() != null) ?
                listing.getInteractions().getAggregate().getTotalSaves() : 0;
        int sharesCount = (listing.getInteractions() != null && listing.getInteractions().getAggregate() != null) ?
                listing.getInteractions().getAggregate().getTotalShares() : 0;
        holder.views.setText(String.valueOf(viewsCount));
        holder.saves.setText(String.valueOf(savesCount));
        holder.shares.setText(String.valueOf(sharesCount));
        // Set chip background and text color based on status
        switch (statusText) {
            case "available":
            case "Available":
                holder.status.setChipBackgroundColorResource(R.color.md_theme_primaryContainer);
                holder.status.setTextColor(context.getColor(R.color.md_theme_primary));
                holder.status.setText("Available");
                break;
            case "pending":
            case "Paused":
                holder.status.setChipBackgroundColorResource(R.color.md_theme_secondaryContainer);
                holder.status.setTextColor(context.getColor(R.color.md_theme_secondary));
                holder.status.setText("Paused");
                break;
            case "sold":
            case "Sold":
                holder.status.setChipBackgroundColorResource(R.color.md_theme_errorContainer);
                holder.status.setTextColor(context.getColor(R.color.md_theme_error));
                holder.status.setText("Sold");
                // Hide edit and delete buttons if sold
                holder.btnEdit.setVisibility(View.GONE);
                holder.btnDelete.setVisibility(View.GONE);
                break;
            default:
                holder.status.setChipBackgroundColorResource(R.color.md_theme_surfaceVariant);
                holder.status.setTextColor(context.getColor(R.color.black));
                holder.status.setText("Available");
                break;
        }
        // Show edit/delete if not sold
        if (!statusText.equalsIgnoreCase("sold")) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return listingList.size();
    }

    /**
     * Updates the adapter's dataset with a new list of items.
     * @param newItems The new list of items to display
     */
    public void updateItems(List<ListingModel> newItems) {
        listingList.clear();
        listingList.addAll(newItems);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView image;
        TextView title, price, views, saves, shares;
        Chip status;
        MaterialButton btnEdit, btnDelete;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.iv_item_image);
            title = itemView.findViewById(R.id.tv_item_title);
            price = itemView.findViewById(R.id.tv_item_price);
            views = itemView.findViewById(R.id.tv_views);
            saves = itemView.findViewById(R.id.tv_saves);
            shares = itemView.findViewById(R.id.tv_shares);
            status = itemView.findViewById(R.id.chip_item_status);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
