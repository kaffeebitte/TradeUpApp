package com.example.tradeupapp.ui.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.models.NotificationModel;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationModel> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationModel notification, int position);
    }

    public NotificationAdapter(List<NotificationModel> notifications) {
        this.notifications = notifications;
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notification = notifications.get(position);
        holder.titleTextView.setText(notification.getTitle());
        holder.messageTextView.setText(notification.getMessage());
        holder.timeTextView.setText(notification.getTime());

        // Change background color based on read status
        if (notification.isRead()) {
            holder.itemView.setBackgroundResource(R.color.read_notification_background);
        } else {
            holder.itemView.setBackgroundResource(R.color.unread_notification_background);
        }

        holder.itemView.setOnClickListener(v -> {
            notification.setRead(true);
            notifyItemChanged(position);
            if (listener != null) {
                listener.onNotificationClick(notification, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void updateNotifications(List<NotificationModel> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView messageTextView;
        TextView timeTextView;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_notification_title);
            messageTextView = itemView.findViewById(R.id.tv_notification_message);
            timeTextView = itemView.findViewById(R.id.tv_notification_time);
        }
    }
}
