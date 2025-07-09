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
                } else {
                    holder.title.setText("No title");
                }
            }
            @Override
            public void onError(String error) {
                holder.title.setText("No title");
            }
        });
        holder.itemView.setOnClickListener(v -> listener.onItemClick(listing));
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
        TextView title, price;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.iv_item_image);
            title = itemView.findViewById(R.id.tv_title);
            price = itemView.findViewById(R.id.tv_price);
        }
    }
}
