package com.example.tradeupapp.adapters;

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
import com.example.tradeupapp.models.ItemModel;

import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {

    private final List<ItemModel> itemList;
    private final OnItemClickListener listener;
    private final Context context;

    public interface OnItemClickListener {
        void onItemClick(ItemModel item);
    }

    public ListingAdapter(Context context, List<ItemModel> itemList, OnItemClickListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_listing_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemModel item = itemList.get(position);

        // Set title and price
        holder.title.setText(item.getTitle());
        holder.price.setText("$" + item.getPrice());

        // Load image with Glide for better image handling and caching
        if (item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) {
            Uri firstImageUri = item.getPhotoUris().get(0);
            if (firstImageUri != null) {
                Glide.with(context)
                    .load(firstImageUri)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_error_24)
                    .centerCrop()
                    .into(holder.image);
            } else {
                holder.image.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            holder.image.setImageResource(R.drawable.ic_image_placeholder);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, price;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.itemImage);
            title = itemView.findViewById(R.id.itemTitle);
            price = itemView.findViewById(R.id.itemPrice);
        }
    }
}
