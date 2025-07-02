package com.example.tradeupapp.shared.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.models.CategoryModel;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<CategoryModel> categories;
    private final CategoryClickListener listener;

    public interface CategoryClickListener {
        void onCategoryClick(CategoryModel category);
    }

    public CategoryAdapter(List<CategoryModel> categories, CategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryModel category = categories.get(position);

        // Set category name
        holder.categoryName.setText(category.getName());

        // Set category icon
        // Option 1: If you have an iconResourceId field in your model
        if (category.getIconResourceId() != 0) {
            holder.categoryIcon.setImageResource(category.getIconResourceId());
        }
        // Option 2: If you only have a URL, use Glide or another image loading library
        else if (category.getIconUrl() != null && !category.getIconUrl().isEmpty()) {
            // Use Glide to load the image from URL
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                .load(category.getIconUrl())
                .placeholder(com.example.tradeupapp.R.drawable.ic_category_default)
                .error(com.example.tradeupapp.R.drawable.ic_category_default)
                .into(holder.categoryIcon);
        } else {
            // Set a default icon if no icon is available
            holder.categoryIcon.setImageResource(com.example.tradeupapp.R.drawable.ic_category_default);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryIcon;
        TextView categoryName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIcon = itemView.findViewById(R.id.category_icon);
            categoryName = itemView.findViewById(R.id.category_name);
        }
    }
}
