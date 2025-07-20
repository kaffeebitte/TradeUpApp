package com.example.tradeupapp.shared.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tradeupapp.R;
import com.example.tradeupapp.models.ChatModel;
import com.example.tradeupapp.utils.AESUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
    private final List<ChatModel> chatList;
    private final OnChatClickListener listener;
    private java.util.Map<String, String> userNameMap = new java.util.HashMap<>();
    private String currentUserId; // Add currentUserId variable

    public interface OnChatClickListener {
        void onChatClick(ChatModel chat);
    }

    public ChatListAdapter(List<ChatModel> chatList, OnChatClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
    }

    public void setUserNameMap(java.util.Map<String, String> map) {
        this.userNameMap = map != null ? map : new java.util.HashMap<>();
        notifyDataSetChanged();
    }

    // Add method to set currentUserId
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_conversation, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatModel chat = chatList.get(position);
        // Get the other user's ID from participants (not current user)
        String otherUserId = null;
        if (chat.getParticipants() != null) {
            for (String id : chat.getParticipants()) {
                if (!id.equals(currentUserId)) {
                    otherUserId = id;
                    break;
                }
            }
        }
        String displayName = userNameMap.get(otherUserId);
        if (displayName != null && !displayName.isEmpty()) {
            holder.username.setText(displayName);
        } else if (otherUserId != null && !otherUserId.isEmpty()) {
            // Fetch display name from Firestore if not in userNameMap
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(otherUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String fetchedName = documentSnapshot.getString("displayName");
                    if (fetchedName != null && !fetchedName.isEmpty()) {
                        holder.username.setText(fetchedName);
                    } else {
                        holder.username.setText("");
                    }
                })
                .addOnFailureListener(e -> holder.username.setText(""));
        } else {
            holder.username.setText("");
        }
        android.util.Log.d("ChatListAdapter", "tv_chat_username: otherUserId=" + otherUserId + ", displayName=" + displayName);
        String lastMessage = chat.getLastMessage();
        String lastMessageType = null;
        // Try to detect if lastMessage is a listing type (simple heuristic)
        if (lastMessage != null && lastMessage.startsWith("I'm interested in this product:")) {
            lastMessageType = "listing";
        }
        // Decrypt lastMessage if not listing and not empty
        if (lastMessage != null && !lastMessage.isEmpty() && (lastMessageType == null || !"listing".equals(lastMessageType))) {
            String aesKey = getAESKeyForChat(holder.itemView.getContext(), chat.getId());
            if (aesKey != null) {
                try {
                    lastMessage = AESUtils.decrypt(lastMessage, aesKey);
                } catch (Exception e) {
                    lastMessage = "[Unable to decrypt]";
                }
            }
        }
        if (chat.getLastMessageSenderId() != null && chat.getLastMessageSenderId().equals(currentUserId) && lastMessage != null && !lastMessage.isEmpty()) {
            lastMessage = "You: " + lastMessage;
        }
        holder.lastMessage.setText(lastMessage);
        // Format time as short (e.g. 10:30 AM, Yesterday, Mon)
        java.text.SimpleDateFormat timeFormat;
        java.util.Date lastDate = chat.getLastUpdated() != null ? chat.getLastUpdated().toDate() : null;
        if (lastDate != null) {
            long now = System.currentTimeMillis();
            long diff = now - lastDate.getTime();
            if (diff < 24 * 60 * 60 * 1000) {
                // Today: show hour:minute AM/PM
                timeFormat = new java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault());
            } else if (diff < 48 * 60 * 60 * 1000) {
                holder.time.setText("Yesterday");
                timeFormat = null;
            } else {
                timeFormat = new java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault());
            }
            if (timeFormat != null) {
                holder.time.setText(timeFormat.format(lastDate));
            }
        } else {
            holder.time.setText("");
        }
        holder.unreadIndicator.setVisibility(View.GONE);
        // Load avatar if available
        String avatarUrl = null;
        if (userNameMap instanceof java.util.Map) {
            Object avatarObj = userNameMap.get(otherUserId + "_avatar");
            if (avatarObj instanceof String) avatarUrl = (String) avatarObj;
        }
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(holder.avatar.getContext())
                .load(avatarUrl)
                .placeholder(R.drawable.ic_person_24)
                .error(R.drawable.ic_person_24)
                .into(holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.ic_person_24);
        }
        holder.itemView.setOnClickListener(v -> listener.onChatClick(chat));
        // Highlight card if last message is unread and sent by other user
        boolean isUnread = false;
        int unreadCount = 0;
        if (chat.getUnreadCount() != null) {
            Integer unread = chat.getUnreadCount().get(currentUserId);
            if (unread != null && unread > 0) {
                isUnread = true;
                unreadCount = unread;
            }
        }
        if (isUnread) {
            // WhatsApp-like: bold last message, show badge, use prominent color
            holder.lastMessage.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.md_theme_onSurface));
            holder.lastMessage.setTypeface(null, android.graphics.Typeface.BOLD);
            holder.unreadIndicator.setVisibility(View.VISIBLE);
            holder.unreadCount.setText(String.valueOf(unreadCount));
            // Change card background color for unread message
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.unread_notification_background));
        } else {
            holder.lastMessage.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.md_theme_onSurfaceVariant));
            holder.lastMessage.setTypeface(null, android.graphics.Typeface.NORMAL);
            holder.unreadIndicator.setVisibility(View.GONE);
            // Reset card background color for read message
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.cardBackground));
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView avatar;
        TextView username, lastMessage, time;
        MaterialCardView unreadIndicator;
        TextView unreadCount;
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.iv_chat_avatar);
            username = itemView.findViewById(R.id.tv_chat_username);
            lastMessage = itemView.findViewById(R.id.tv_chat_last_message);
            time = itemView.findViewById(R.id.tv_chat_time);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
            unreadCount = itemView.findViewById(R.id.tv_unread_count);
        }
    }

    // Helper to get AES key for a chat from SharedPreferences
    private String getAESKeyForChat(Context context, String chatId) {
        SharedPreferences prefs = context.getSharedPreferences("chat_keys", Context.MODE_PRIVATE);
        String key = prefs.getString("chat_key_" + chatId, null);
        if (key == null) {
            try {
                key = AESUtils.generateKey();
                prefs.edit().putString("chat_key_" + chatId, key).apply();
            } catch (Exception e) {
                key = null;
            }
        }
        return key;
    }
}
