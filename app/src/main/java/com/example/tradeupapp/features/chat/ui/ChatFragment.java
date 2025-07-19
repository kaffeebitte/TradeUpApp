package com.example.tradeupapp.features.chat.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.shared.adapters.ChatAdapter;
import com.example.tradeupapp.models.ChatMessage;
import com.example.tradeupapp.models.ChatModel;
import com.example.tradeupapp.utils.AESUtils;
import com.example.tradeupapp.utils.CloudinaryManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

public class ChatFragment extends Fragment {

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ImageButton attachmentButton;
    private View imagePreviewContainer;
    private ImageButton removeImageButton;
    private ImageView imagePreview;
    private Toolbar toolbar;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private Uri selectedImageUri;
    private View productCardContainer;
    private ImageView productCardImage;
    private TextView productCardTitle;
    private TextView productCardPrice;
    private ImageButton productCardClose;
    private String itemImageUrl;
    private String itemPrice;
    private String otherUserName;

    // Activity Result Launcher for selecting images from gallery
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    // Show the selected image in the preview
                    imagePreview.setImageURI(selectedImageUri);
                    imagePreviewContainer.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), "Image attached", Toast.LENGTH_SHORT).show();
                }
            });

    private FirebaseFirestore db;
    private String chatId;
    private String currentUserId;
    private String otherUserId;
    private String listingId;
    private String itemId;
    private String itemName;
    private CollectionReference messagesRef;

    private String chatAESKey; // AES key for this chat

    private String getAESKeyForChat() {
        SharedPreferences prefs = requireContext().getSharedPreferences("chat_keys", android.content.Context.MODE_PRIVATE);
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        chatId = getArguments() != null ? getArguments().getString("chatId", "") : "";
        otherUserId = getArguments() != null ? getArguments().getString("otherUserId", "") : "";
        listingId = getArguments() != null ? getArguments().getString("listingId", null) : null;
        itemId = getArguments() != null ? getArguments().getString("itemId", null) : null;
        itemName = getArguments() != null ? getArguments().getString("itemName", null) : null;
        itemImageUrl = getArguments() != null ? getArguments().getString("itemImageUrl", null) : null;
        itemPrice = getArguments() != null ? getArguments().getString("itemPrice", null) : null;
        otherUserName = getArguments() != null ? getArguments().getString("otherUserName", null) : null;
        if (chatId == null || chatId.isEmpty()) {
            // Show error or handle new chat creation here
            Toast.makeText(getContext(), "Invalid chat. Please try again.", Toast.LENGTH_SHORT).show();
            messagesRef = null;
        } else {
            messagesRef = db.collection("chats").document(chatId).collection("messages");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Setup toolbar
        toolbar = view.findViewById(R.id.chat_toolbar);
        if (toolbar != null) {
            ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24); // Ensure back icon is set
            // Set chat title to other user's name if available
            if (otherUserName != null && !otherUserName.isEmpty()) {
                toolbar.setTitle(otherUserName);
            } else {
                toolbar.setTitle("Chat");
            }
            toolbar.setNavigationOnClickListener(v -> {
                // Navigate back to chat list
                androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.popBackStack(R.id.chatListFragment, false);
            });
        }

        return view;
    }

    private LinearLayout blockNoticeContainer; // Use XML block notice container
    private TextView blockNoticeTextView; // Use XML block notice text
    private boolean isBlocked = false;
    private TextView chatTitleStatusTextView; // Add status text next to chat title
    private ConstraintLayout messageInputLayout;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize views
        initViews(view);
        // Get message input layout
        messageInputLayout = view.findViewById(R.id.message_input_layout);
        // Find chat title status view (add to toolbar if not exists)
        chatTitleStatusTextView = new TextView(requireContext());
        chatTitleStatusTextView.setText("");
        chatTitleStatusTextView.setTextColor(getResources().getColor(R.color.md_theme_onError));
        chatTitleStatusTextView.setTextSize(14);
        chatTitleStatusTextView.setPadding(16, 0, 0, 0);
        if (toolbar != null && chatTitleStatusTextView.getParent() == null) {
            toolbar.addView(chatTitleStatusTextView);
        }
        // Remove block notice container
        blockNoticeContainer = view.findViewById(R.id.block_notice_container);
        if (blockNoticeContainer != null) blockNoticeContainer.setVisibility(View.GONE);
        // Check block status
        checkBlockStatus();
        // If product info is available, auto-fill message input
        if (itemName != null && messageEditText != null && messageEditText.getText().toString().isEmpty()) {
            messageEditText.setText("I'm interested in this product: " + itemName);
            messageEditText.setSelection(messageEditText.getText().length());
        }
        // Setup RecyclerView
        setupRecyclerView();
        // Setup send message event
        setupSendMessageListener();
        // Ensure empty state is hidden initially
        View emptyState = view.findViewById(R.id.empty_state);
        emptyState.setVisibility(View.GONE);
        // Setup product card views
        productCardContainer = view.findViewById(R.id.product_card_container);
        productCardImage = view.findViewById(R.id.product_card_image);
        productCardTitle = view.findViewById(R.id.product_card_title);
        productCardPrice = view.findViewById(R.id.product_card_price);
        productCardClose = view.findViewById(R.id.product_card_close);
        // Show product card if info is available
        if (itemName != null) {
            productCardContainer.setVisibility(View.VISIBLE);
            productCardTitle.setText(itemName);
            if (itemPrice != null) {
                productCardPrice.setText(itemPrice);
            } else {
                productCardPrice.setVisibility(View.GONE);
            }
            if (itemImageUrl != null && !itemImageUrl.isEmpty()) {
                // Use Glide or similar to load image
                com.bumptech.glide.Glide.with(requireContext())
                        .load(itemImageUrl)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .into(productCardImage);
            } else {
                productCardImage.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            productCardContainer.setVisibility(View.GONE);
        }
        // Dismiss product card
        productCardClose.setOnClickListener(v -> productCardContainer.setVisibility(View.GONE));
        chatAESKey = getAESKeyForChat();
    }

    private void initViews(View view) {
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view);
        messageEditText = view.findViewById(R.id.message_edit_text);
        sendButton = view.findViewById(R.id.send_button);
        attachmentButton = view.findViewById(R.id.attachment_button);
        imagePreviewContainer = view.findViewById(R.id.image_preview_container);
        removeImageButton = view.findViewById(R.id.remove_image_button);
        imagePreview = view.findViewById(R.id.image_preview);

        // Setup attachment button click listener
        attachmentButton.setOnClickListener(v -> openGallery());

        // Setup remove image button click listener
        removeImageButton.setOnClickListener(v -> {
            selectedImageUri = null;
            imagePreview.setImageURI(null);
            imagePreviewContainer.setVisibility(View.GONE);
        });
    }

    private void checkBlockStatus() {
        if (chatId == null || chatId.isEmpty()) return;
        db.collection("chats").document(chatId).get().addOnSuccessListener(doc -> {
            List<String> blockedBy = (List<String>) doc.get("blockedBy");
            if (blockedBy != null && !blockedBy.isEmpty()) {
                if (blockedBy.contains(currentUserId)) {
                    isBlocked = true;
                    chatTitleStatusTextView.setText("(You blocked)");
                } else if (blockedBy.contains(otherUserId)) {
                    isBlocked = true;
                    chatTitleStatusTextView.setText("(Blocked)");
                } else {
                    isBlocked = false;
                    chatTitleStatusTextView.setText("");
                }
            } else {
                isBlocked = false;
                chatTitleStatusTextView.setText("");
            }
            messageEditText.setEnabled(!isBlocked);
            sendButton.setEnabled(!isBlocked);
            attachmentButton.setEnabled(!isBlocked);
            if (messageInputLayout != null) {
                messageInputLayout.setAlpha(isBlocked ? 0.5f : 1f);
                messageInputLayout.setEnabled(!isBlocked);
            }
        });
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(requireContext(), messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);
        if (messagesRef != null) {
            listenForMessages();
        } else {
            // Show empty state if no valid chat
            View emptyState = requireView().findViewById(R.id.empty_state);
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
            chatRecyclerView.setVisibility(View.GONE);
        }
    }

    private void listenForMessages() {
        if (messagesRef == null) return;
        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(requireContext(), "Failed to load messages", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    messageList.clear();
                    View rootView = getView();
                    View emptyState = null;
                    if (rootView != null) {
                        emptyState = rootView.findViewById(R.id.empty_state);
                    }
                    if (value != null && !value.isEmpty()) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ChatMessage msg = doc.toObject(ChatMessage.class);
                            if (msg != null) {
                                // Decrypt message text if not listing and not image
                                if (!"listing".equals(msg.getMessageType()) && !"image".equals(msg.getMessageType()) && msg.getMessage() != null && chatAESKey != null) {
                                    try {
                                        String decrypted = AESUtils.decrypt(msg.getMessage(), chatAESKey);
                                        msg.setMessage(decrypted);
                                    } catch (Exception e) {
                                        msg.setMessage("[Unable to decrypt]");
                                    }
                                }
                                // Decrypt image URLs in attachments for image messages
                                if ("image".equals(msg.getMessageType()) && msg.getAttachments() != null && !msg.getAttachments().isEmpty() && chatAESKey != null) {
                                    List<String> decryptedAttachments = new ArrayList<>();
                                    for (String url : msg.getAttachments()) {
                                        if (url != null && !url.equals("[Encryption failed]")) {
                                            try {
                                                decryptedAttachments.add(AESUtils.decrypt(url, chatAESKey));
                                            } catch (Exception e) {
                                                decryptedAttachments.add("[Unable to decrypt]");
                                            }
                                        }
                                    }
                                    msg.setAttachments(decryptedAttachments);
                                }
                                // Decrypt attachments for other types (if needed)
                                if (!"image".equals(msg.getMessageType()) && msg.getAttachments() != null && !msg.getAttachments().isEmpty() && chatAESKey != null) {
                                    List<String> decryptedAttachments = new ArrayList<>();
                                    for (String url : msg.getAttachments()) {
                                        if (url != null && !url.equals("[Encryption failed]")) {
                                            try {
                                                decryptedAttachments.add(AESUtils.decrypt(url, chatAESKey));
                                            } catch (Exception e) {
                                                decryptedAttachments.add("[Unable to decrypt]");
                                            }
                                        }
                                    }
                                    msg.setAttachments(decryptedAttachments);
                                }
                                if ("listing".equals(msg.getMessageType())) {
                                    // Do not change type
                                } else {
                                    msg.setMessageType(msg.getSenderId().equals(currentUserId) ? ChatMessage.TYPE_SENT : ChatMessage.TYPE_RECEIVED);
                                }
                                messageList.add(msg);
                            }
                        }
                        chatAdapter.notifyDataSetChanged();
                        chatRecyclerView.scrollToPosition(messageList.size() - 1);
                        markMessagesAsRead();
                        // Hide empty state if there are messages
                        if (emptyState != null) emptyState.setVisibility(View.GONE);
                        chatRecyclerView.setVisibility(View.VISIBLE);
                    } else {
                        // Show empty state if no messages
                        if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                        chatRecyclerView.setVisibility(View.GONE);
                    }
                });
    }

    private void setupSendMessageListener() {
        sendButton.setOnClickListener(v -> {
            if (isBlocked) {
                Toast.makeText(requireContext(), "You cannot send messages in this chat.", Toast.LENGTH_SHORT).show();
                return;
            }
            String message = messageEditText.getText().toString().trim();
            boolean hasListingMessage = false;
            for (ChatMessage msg : messageList) {
                if ("listing".equals(msg.getMessageType())) {
                    hasListingMessage = true;
                    break;
                }
            }
            // If this chat is about a listing and no listing message exists, send listing message first
            if (listingId != null && !hasListingMessage) {
                List<String> listingAttachments = new ArrayList<>();
                if (itemImageUrl != null && !itemImageUrl.isEmpty()) listingAttachments.add(itemImageUrl);
                if (itemPrice != null && !itemPrice.isEmpty()) listingAttachments.add(itemPrice);
                String listingTitle = itemName != null ? itemName : "Listing";
                sendListingMessageToFirestore(listingTitle, listingAttachments);
            }
            // Send normal message
            if (!message.isEmpty() || selectedImageUri != null) {
                if (selectedImageUri != null) {
                    uploadImageAndSendMessage(message, selectedImageUri);
                } else {
                    sendMessageToFirestore(message, null);
                }
                messageEditText.setText("");
                selectedImageUri = null;
                imagePreviewContainer.setVisibility(View.GONE);
            }
        });
    }

    // Send a listing message to Firestore
    private void sendListingMessageToFirestore(String listingTitle, List<String> attachments) {
        if (messagesRef == null) return;
        String messageId = "msg_" + java.util.UUID.randomUUID().toString().replace("-", "");
        // Encrypt all image URLs in attachments
        List<String> encryptedAttachments = new ArrayList<>();
        if (attachments != null) {
            for (String url : attachments) {
                if (url != null && chatAESKey != null) {
                    try {
                        encryptedAttachments.add(AESUtils.encrypt(url, chatAESKey));
                    } catch (Exception e) {
                        encryptedAttachments.add("[Encryption failed]");
                    }
                } else {
                    encryptedAttachments.add(url);
                }
            }
        }
        ChatMessage chatMessage = new ChatMessage(chatId, currentUserId, listingTitle, "listing", encryptedAttachments);
        chatMessage.setId(messageId);
        chatMessage.setTimestamp(Timestamp.now());
        chatMessage.setRead(false);
        messagesRef.document(messageId).set(chatMessage)
            .addOnSuccessListener(aVoid -> {
                updateChatLastMessage(listingTitle, encryptedAttachments, "listing");
                saveMessageToTopLevelCollection(chatMessage);
            });
    }

    private void uploadImageAndSendMessage(String message, Uri imageUri) {
        CloudinaryManager.getInstance().uploadImage(requireContext(), imageUri, "chat_images", new CloudinaryManager.CloudinaryUploadCallback() {
            @Override
            public void onStart() {
                // Optionally show progress indicator
            }
            @Override
            public void onProgress(double progress) {
                // Optionally update progress UI (e.g., progress bar)
            }
            @Override
            public void onSuccess(String imageUrl) {
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    sendMessageToFirestore(message, imageUrl);
                } else {
                    Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessageToFirestore(String message, String imageUrl) {
        if (messagesRef == null) {
            Toast.makeText(requireContext(), "Cannot send message: messagesRef is null", Toast.LENGTH_SHORT).show();
            android.util.Log.e("ChatFragment", "sendMessageToFirestore: messagesRef is null. chatId=" + chatId);
            return;
        }
        android.util.Log.d("ChatFragment", "sendMessageToFirestore: chatId=" + chatId + ", messagesRef path=" + messagesRef.getPath());
        final List<String> attachments = new ArrayList<>();
        final String messageType;
        String encryptedMessage = message;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Encrypt image URL before saving
            if (chatAESKey != null) {
                try {
                    String encryptedUrl = AESUtils.encrypt(imageUrl, chatAESKey);
                    if (encryptedUrl != null && !encryptedUrl.isEmpty()) {
                        attachments.add(encryptedUrl);
                    }
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Image encryption failed", Toast.LENGTH_SHORT).show();
                    return; // Do not send message if encryption fails
                }
            } else {
                Toast.makeText(requireContext(), "Encryption key missing", Toast.LENGTH_SHORT).show();
                return;
            }
            messageType = "image";
        } else {
            messageType = "text";
        }
        // Encrypt message text if not listing and key exists
        if (!"listing".equals(messageType) && chatAESKey != null && message != null && !message.isEmpty()) {
            try {
                encryptedMessage = AESUtils.encrypt(message, chatAESKey);
            } catch (Exception e) {
                encryptedMessage = "[Encryption failed]";
            }
        }
        // Generate custom message ID (e.g., msg_UUID)
        String messageId = "msg_" + java.util.UUID.randomUUID().toString().replace("-", "");
        ChatMessage chatMessage = new ChatMessage(chatId, currentUserId, encryptedMessage, messageType, attachments);
        chatMessage.setId(messageId);
        chatMessage.setTimestamp(Timestamp.now());
        chatMessage.setRead(false); // receiver will mark as read
        // Save with custom ID to match sample data
        messagesRef.document(messageId).set(chatMessage)
            .addOnSuccessListener(aVoid -> {
                android.util.Log.d("ChatFragment", "Message sent successfully to Firestore. chatId=" + chatId + ", docId=" + messageId);
                updateChatLastMessage(message, attachments, messageType);
                // Save to top-level chat_messages collection
                saveMessageToTopLevelCollection(chatMessage);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
                android.util.Log.e("ChatFragment", "Failed to send message: " + e.getMessage() + ", chatId=" + chatId);
            });
    }

    // Save message to top-level chat_messages collection
    private void saveMessageToTopLevelCollection(ChatMessage chatMessage) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("chat_messages")
            .document(chatMessage.getId())
            .set(chatMessage)
            .addOnSuccessListener(aVoid -> {
                android.util.Log.d("ChatFragment", "Message also saved to chat_messages collection. id=" + chatMessage.getId());
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("ChatFragment", "Failed to save message to chat_messages: " + e.getMessage());
            });
    }

    private void updateChatLastMessage(String message, List<String> attachments, String messageType) {
        DocumentReference chatRef = db.collection("chats").document(chatId);
        String lastMsg;
        if ("image".equals(messageType)) {
            lastMsg = "[Image] " + message;
        } else {
            lastMsg = message;
        }
        // Encrypt lastMsg if not listing and key exists
        if (!"listing".equals(messageType) && chatAESKey != null && lastMsg != null && !lastMsg.isEmpty()) {
            try {
                lastMsg = AESUtils.encrypt(lastMsg, chatAESKey);
            } catch (Exception e) {
                lastMsg = "[Encryption failed]";
            }
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", lastMsg);
        updates.put("lastMessageTime", Timestamp.now());
        updates.put("lastMessageSenderId", currentUserId);
        // Increment unreadCount for the other user
        if (otherUserId != null && !otherUserId.isEmpty()) {
            updates.put("unreadCount." + otherUserId, FieldValue.increment(1));
        }
        chatRef.set(updates, SetOptions.merge());
    }

    private void markMessagesAsRead() {
        if (messagesRef == null) return;
        for (ChatMessage msg : messageList) {
            if (!msg.isRead() && !msg.getSenderId().equals(currentUserId)) {
                messagesRef.document(msg.getId()).update("isRead", true);
                // Decrement unreadCount for current user in chat
                DocumentReference chatRef = db.collection("chats").document(chatId);
                chatRef.update("unreadCount." + currentUserId, FieldValue.increment(-1));
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat, menu);
        // Show/hide block/unblock menu items based on block status
        MenuItem blockItem = menu.findItem(R.id.action_block_user);
        MenuItem unblockItem = menu.findItem(R.id.action_unblock_user);
        if (isBlocked && chatTitleStatusTextView.getText().toString().equals("(You blocked)")) {
            if (blockItem != null) blockItem.setVisible(false);
            if (unblockItem != null) unblockItem.setVisible(true);
        } else {
            if (blockItem != null) blockItem.setVisible(true);
            if (unblockItem != null) unblockItem.setVisible(false);
        }
        // Report is always visible
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_block_user) {
            // Show confirmation dialog before blocking
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Block User")
                .setMessage("Are you sure you want to block this user? You will not be able to send or receive messages in this chat.")
                .setPositiveButton("Block", (dialog, which) -> {
                    db.collection("chats").document(chatId)
                        .update("blockedBy", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId))
                        .addOnSuccessListener(aVoid -> {
                            isBlocked = true;
                            Toast.makeText(requireContext(), "User has been blocked", Toast.LENGTH_SHORT).show();
                            chatTitleStatusTextView.setText("(You blocked)");
                            requireActivity().invalidateOptionsMenu();
                            checkBlockStatus();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Failed to block user", Toast.LENGTH_SHORT).show();
                        });
                })
                .setNegativeButton("Cancel", null)
                .show();
            return true;
        } else if (itemId == R.id.action_unblock_user) {
            // Show confirmation dialog before unblocking
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Unblock User")
                .setMessage("Do you want to unblock this user?")
                .setPositiveButton("Unblock", (dialog, which) -> {
                    db.collection("chats").document(chatId)
                        .update("blockedBy", com.google.firebase.firestore.FieldValue.arrayRemove(currentUserId))
                        .addOnSuccessListener(aVoid -> {
                            isBlocked = false;
                            Toast.makeText(requireContext(), "User has been unblocked", Toast.LENGTH_SHORT).show();
                            chatTitleStatusTextView.setText("");
                            requireActivity().invalidateOptionsMenu();
                            checkBlockStatus();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Failed to unblock user", Toast.LENGTH_SHORT).show();
                        });
                })
                .setNegativeButton("Cancel", null)
                .show();
            return true;
        } else if (itemId == R.id.action_report) {
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_report_chat, null);
            final android.widget.RadioGroup reasonRadioGroup = dialogView.findViewById(R.id.rg_report_reason);
            final EditText descriptionEditText = dialogView.findViewById(R.id.et_report_description);
            reasonRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rb_other) {
                    descriptionEditText.setVisibility(View.VISIBLE);
                } else {
                    descriptionEditText.setVisibility(View.GONE);
                }
            });
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Report User")
                .setMessage("Select a reason for reporting this conversation.")
                .setView(dialogView)
                .setPositiveButton("Report", (dialog, which) -> {
                    int checkedId = reasonRadioGroup.getCheckedRadioButtonId();
                    String reason = "";
                    if (checkedId == R.id.rb_spam) {
                        reason = "Spam";
                    } else if (checkedId == R.id.rb_harassment) {
                        reason = "Harassment";
                    } else if (checkedId == R.id.rb_scam) {
                        reason = "Scam";
                    } else if (checkedId == R.id.rb_inappropriate) {
                        reason = "Inappropriate Content";
                    } else if (checkedId == R.id.rb_other) {
                        reason = "Other";
                    } else {
                        reason = "Inappropriate conversation";
                    }
                    String description = descriptionEditText.getText().toString().trim();
                    if (reason.equals("Other") && description.isEmpty()) {
                        Toast.makeText(requireContext(), "Please describe the issue for 'Other' reason.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String reportId = "report_" + java.util.UUID.randomUUID().toString().replace("-", "");
                    String reportType = checkedId == R.id.rb_scam ? "scam" : (checkedId == R.id.rb_harassment ? "harassment" : "inappropriate_conversation");
                    String status = "pending";
                    com.google.firebase.Timestamp createdAt = com.google.firebase.Timestamp.now();
                    java.util.Map<String, Object> reportData = new java.util.HashMap<>();
                    reportData.put("id", reportId);
                    reportData.put("reporterId", currentUserId);
                    reportData.put("reportedUserId", otherUserId);
                    reportData.put("reportedItemId", null);
                    reportData.put("chatId", chatId);
                    reportData.put("reportType", reportType);
                    reportData.put("reason", reason);
                    reportData.put("description", description);
                    reportData.put("status", status);
                    reportData.put("createdAt", createdAt);
                    reportData.put("resolvedAt", null);
                    reportData.put("adminNotes", null);
                    reportData.put("evidence", new java.util.ArrayList<String>());
                    db.collection("reports").document(reportId).set(reportData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(requireContext(), "User has been reported", Toast.LENGTH_SHORT).show();
                            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                .setTitle("Block User?")
                                .setMessage("Do you also want to block this user from further conversation?")
                                .setPositiveButton("Block", (blockDialog, blockWhich) -> {
                                    db.collection("chats").document(chatId)
                                        .update("blockedBy", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId))
                                        .addOnSuccessListener(bVoid -> {
                                            isBlocked = true;
                                            Toast.makeText(requireContext(), "User has been blocked", Toast.LENGTH_SHORT).show();
                                            chatTitleStatusTextView.setText("(You blocked)");
                                            requireActivity().invalidateOptionsMenu();
                                            checkBlockStatus();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(requireContext(), "Failed to block user", Toast.LENGTH_SHORT).show();
                                        });
                                })
                                .setNegativeButton("No", null)
                                .show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Failed to report user", Toast.LENGTH_SHORT).show();
                        });
                })
                .setNegativeButton("Cancel", null)
                .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
