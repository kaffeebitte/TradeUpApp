package com.example.tradeupapp.features.cart.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tradeupapp.R;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;

import java.util.List;
import java.util.Locale;

public class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.CartViewHolder> {
    public interface OnRemoveClickListener {
        void onRemove(ListingModel listing);
    }
    private List<CartDisplayItem> cartItems;
    private final OnRemoveClickListener removeClickListener;

    public CartListAdapter(List<CartDisplayItem> cartItems, OnRemoveClickListener removeClickListener) {
        this.cartItems = cartItems;
        this.removeClickListener = removeClickListener;
    }

    public void setCartItems(List<CartDisplayItem> cartItems) {
        this.cartItems = cartItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartDisplayItem cartItem = cartItems.get(position);
        ListingModel listing = cartItem.getListing();
        ItemModel item = cartItem.getItem();
        holder.tvTitle.setText(item.getTitle());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", listing.getPrice()));
        holder.tvCondition.setText(item.getCondition());
        if (item.getPhotoUris() != null && !item.getPhotoUris().isEmpty()) {
            Glide.with(holder.ivImage.getContext())
                .load(item.getPhotoUris().get(0))
                .placeholder(R.drawable.tradeuplogo)
                .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_image_placeholder);
        }
        if (removeClickListener != null) {
            holder.btnRemove.setVisibility(View.VISIBLE);
            holder.btnRemove.setOnClickListener(v -> removeClickListener.onRemove(listing));
        } else {
            holder.btnRemove.setVisibility(View.GONE);
            holder.btnRemove.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvPrice, tvCondition;
        View btnRemove;
        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.item_image);
            tvTitle = itemView.findViewById(R.id.item_title);
            tvPrice = itemView.findViewById(R.id.item_price);
            tvCondition = itemView.findViewById(R.id.item_condition);
            btnRemove = itemView.findViewById(R.id.remove_item);
        }
    }
}
