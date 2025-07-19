package com.example.tradeupapp.shared.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tradeupapp.R;
import com.example.tradeupapp.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 0;
    private static final int VIEW_TYPE_RECEIVED = 1;
    private static final int VIEW_TYPE_LISTING = 2;

    private final Context context;
    private final List<ChatMessage> messages;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        String type = msg.getMessageType();
        if ("listing".equals(type)) {
            return VIEW_TYPE_LISTING;
        } else if (ChatMessage.TYPE_SENT.equals(type)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_RECEIVED) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_LISTING) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_listing, parent, false);
            return new ListingMessageViewHolder(view);
        } else {
            throw new IllegalArgumentException("Unknown viewType: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        int type = holder.getItemViewType();
        if (type == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (type == VIEW_TYPE_RECEIVED) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        } else if (type == VIEW_TYPE_LISTING) {
            ((ListingMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // Add a new message to the adapter
    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    // ViewHolder for sent messages
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final TextView messageTime;
        private final ImageView messageImage;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageTime = itemView.findViewById(R.id.message_time);
            messageImage = itemView.findViewById(R.id.message_image);
        }

        void bind(ChatMessage message) {
            if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
                messageImage.setVisibility(View.VISIBLE);
                // Set fixed size for image (e.g. 160x160dp)
                int sizePx = (int) (160 * itemView.getContext().getResources().getDisplayMetrics().density);
                messageImage.getLayoutParams().width = sizePx;
                messageImage.getLayoutParams().height = sizePx;
                messageImage.requestLayout();
                Glide.with(messageImage.getContext())
                        .load(message.getAttachments().get(0))
                        .centerCrop()
                        .into(messageImage);
            } else {
                messageImage.setVisibility(View.GONE);
                // Reset size if not image
                messageImage.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                messageImage.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                messageImage.requestLayout();
            }
            messageText.setText(message.getMessage());

            // Format the timestamp as a relative time string (e.g., "5 minutes ago")
            String timeAgo = DateUtils.getRelativeTimeSpanString(
                    message.getTimestamp() != null ? message.getTimestamp().toDate().getTime() : System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString();
            messageTime.setText(timeAgo);
        }
    }

    // ViewHolder for received messages
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final TextView messageTime;
        private final ImageView messageImage;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageTime = itemView.findViewById(R.id.message_time);
            messageImage = itemView.findViewById(R.id.message_image);
        }

        void bind(ChatMessage message) {
            if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
                messageImage.setVisibility(View.VISIBLE);
                int sizePx = (int) (160 * itemView.getContext().getResources().getDisplayMetrics().density);
                messageImage.getLayoutParams().width = sizePx;
                messageImage.getLayoutParams().height = sizePx;
                messageImage.requestLayout();
                Glide.with(messageImage.getContext())
                        .load(message.getAttachments().get(0))
                        .centerCrop()
                        .into(messageImage);
            } else {
                messageImage.setVisibility(View.GONE);
                messageImage.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                messageImage.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                messageImage.requestLayout();
            }
            messageText.setText(message.getMessage());

            // Format the timestamp as a relative time string (e.g., "5 minutes ago")
            String timeAgo = DateUtils.getRelativeTimeSpanString(
                    message.getTimestamp() != null ? message.getTimestamp().toDate().getTime() : System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString();
            messageTime.setText(timeAgo);
        }
    }

    // ViewHolder for listing messages
    static class ListingMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView listingTitle;
        private final TextView listingPrice;
        private final ImageView listingImage;

        ListingMessageViewHolder(View itemView) {
            super(itemView);
            listingTitle = itemView.findViewById(R.id.listing_title);
            listingPrice = itemView.findViewById(R.id.listing_price);
            listingImage = itemView.findViewById(R.id.listing_image);
        }

        void bind(ChatMessage message) {
            listingTitle.setText(message.getMessage());

            // Price: try to get from message.getAttachments().size() > 1 ? message.getAttachments().get(1) : null
            if (message.getAttachments() != null && message.getAttachments().size() > 1) {
                listingPrice.setText(message.getAttachments().get(1));
            } else if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
                // fallback: if only one attachment, treat as image
                listingPrice.setVisibility(View.GONE);
            } else {
                listingPrice.setVisibility(View.GONE);
            }

            // Image
            if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
                listingImage.setVisibility(View.VISIBLE);
                Glide.with(listingImage.getContext())
                        .load(message.getAttachments().get(0))
                        .centerCrop()
                        .into(listingImage);
            } else {
                listingImage.setImageResource(R.drawable.ic_image_placeholder);
            }
        }
    }
}
