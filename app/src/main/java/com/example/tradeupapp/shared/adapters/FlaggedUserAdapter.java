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
import com.example.tradeupapp.models.User;

import java.util.List;

public class FlaggedUserAdapter extends RecyclerView.Adapter<FlaggedUserAdapter.FlaggedUserViewHolder> {
    private final Context context;
    private List<User> userList;

    public FlaggedUserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FlaggedUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_flagged_user, parent, false);
        return new FlaggedUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlaggedUserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvUserName.setText(user.getDisplayName());
        holder.tvUserEmail.setText(user.getEmail());
        holder.tvUserStatus.setText("Status: " + user.getStatus());
        holder.tvWarningCount.setText("Warnings: " + user.getWarningCount());
        // Optionally load avatar if available
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    static class FlaggedUserViewHolder extends RecyclerView.ViewHolder {
        ImageView imgUserAvatar;
        TextView tvUserName, tvUserEmail, tvUserStatus, tvWarningCount;
        public FlaggedUserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUserAvatar = itemView.findViewById(R.id.img_user_avatar);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserEmail = itemView.findViewById(R.id.tv_user_email);
            tvUserStatus = itemView.findViewById(R.id.tv_user_status);
            tvWarningCount = itemView.findViewById(R.id.tv_warning_count);
        }
    }
}

