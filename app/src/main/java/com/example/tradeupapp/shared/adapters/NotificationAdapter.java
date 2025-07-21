package com.example.tradeupapp.shared.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.models.NotificationModel;
import com.google.firebase.firestore.FirebaseFirestore;

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
        holder.messageTextView.setText(notification.getBody());

        // Format timestamp to a readable string
        String formattedTime = "Just now";
        if (notification.getCreatedAt() != null) {
            formattedTime = android.text.format.DateUtils.getRelativeTimeSpanString(
                    notification.getCreatedAt().toDate().getTime(),
                    System.currentTimeMillis(),
                    android.text.format.DateUtils.MINUTE_IN_MILLIS
            ).toString();
        }
        holder.timeTextView.setText(formattedTime);

        // Set icon based on notification type
        int iconRes = R.drawable.ic_notification_24;
        String type = notification.getType();
        if (type != null) {
            switch (type) {
                case "chat":
                case "chat_message":
                    iconRes = R.drawable.ic_chat_24;
                    break;
                case "offer":
                case "offer_received":
                case "offer_accepted":
                case "offer_declined":
                case "counter_offer":
                    iconRes = R.drawable.ic_offer_24;
                    break;
                case "listing_update":
                case "item_sold":
                case "price_drop":
                case "low_stock":
                    iconRes = R.drawable.ic_listing_24;
                    break;
                case "review":
                case "review_received":
                    iconRes = R.drawable.ic_review_24;
                    break;
                case "system":
                case "verification_approved":
                    iconRes = R.drawable.ic_system_24;
                    break;
                default:
                    iconRes = R.drawable.ic_notification_24;
            }
        }
        holder.iconImageView.setImageResource(iconRes);

        // Change background color based on read status
        if (notification.isRead()) {
            holder.itemView.setBackgroundResource(R.color.read_notification_background);
        } else {
            holder.itemView.setBackgroundResource(R.color.unread_notification_background);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!notification.isRead()) {
                // Update Firestore: mark as read
                String notifId = notification.getId();
                if (notifId != null) {
                    FirebaseFirestore.getInstance().collection("notifications")
                            .document(notifId)
                            .update("read", true);
                }
                notification.markAsRead();
                notifyItemChanged(position);
            }
            // Removed navigation callback to prevent moving to another page
            // if (listener != null) {
            //     listener.onNotificationClick(notification, position);
            // }
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
        ImageView iconImageView;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_notification_title);
            messageTextView = itemView.findViewById(R.id.tv_notification_message);
            timeTextView = itemView.findViewById(R.id.tv_notification_time);
            iconImageView = itemView.findViewById(R.id.iv_notification_icon);
        }
    }
}
