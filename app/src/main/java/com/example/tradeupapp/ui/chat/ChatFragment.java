package com.example.tradeupapp.ui.chat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.adapters.ChatAdapter;
import com.example.tradeupapp.models.ChatMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ChatFragment extends Fragment {

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ImageButton attachmentButton;
    private Toolbar toolbar;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private Uri selectedImageUri;

    // Activity Result Launcher for selecting images from gallery
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Toast.makeText(requireContext(), "Ảnh đã được đính kèm", Toast.LENGTH_SHORT).show();
                }
            });

    // Receiver ID - in a real app, this would come from the chat conversation
    private final String currentUserId = "current_user";
    private final String otherUserId = "other_user";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Enable options menu for block/report
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
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Chat with Seller");
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo các view
        initViews(view);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập sự kiện gửi tin nhắn
        setupSendMessageListener();
    }

    private void initViews(View view) {
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view);
        messageEditText = view.findViewById(R.id.message_edit_text);
        sendButton = view.findViewById(R.id.send_button);
        attachmentButton = view.findViewById(R.id.attachment_button);

        // Setup attachment button click listener
        attachmentButton.setOnClickListener(v -> openGallery());
    }

    private void setupRecyclerView() {
        // Initialize message list and adapter
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(requireContext(), messageList);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true); // Messages appear from bottom
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        // Load some dummy messages for demonstration
        loadDummyMessages();
    }

    private void loadDummyMessages() {
        // Add some sample messages for demonstration
        messageList.add(new ChatMessage(otherUserId, currentUserId,
                "Xin chào, sản phẩm này còn không?", null, ChatMessage.TYPE_RECEIVED));

        messageList.add(new ChatMessage(currentUserId, otherUserId,
                "Vâng, còn ạ. Bạn có thể đến xem trực tiếp.", null, ChatMessage.TYPE_SENT));

        messageList.add(new ChatMessage(otherUserId, currentUserId,
                "Bạn có thể gửi thêm vài tấm ảnh không?", null, ChatMessage.TYPE_RECEIVED));

        // Update the adapter
        chatAdapter.notifyDataSetChanged();

        // Scroll to the bottom
        chatRecyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void setupSendMessageListener() {
        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (!message.isEmpty() || selectedImageUri != null) {
                sendMessage(message);
                messageEditText.setText("");
                selectedImageUri = null; // Clear the selected image after sending
            }
        });
    }

    private void sendMessage(String message) {
        // Create a new message
        ChatMessage chatMessage = new ChatMessage(
                currentUserId,
                otherUserId,
                message,
                selectedImageUri != null ? selectedImageUri.toString() : null,
                ChatMessage.TYPE_SENT
        );

        // Add the message to the list
        messageList.add(chatMessage);

        // Update the RecyclerView
        chatAdapter.notifyItemInserted(messageList.size() - 1);

        // Scroll to the bottom
        chatRecyclerView.scrollToPosition(messageList.size() - 1);

        // Simulate receiving a response after a delay
        simulateResponse();
    }

    private void simulateResponse() {
        // Simulate a delay before receiving a response
        chatRecyclerView.postDelayed(() -> {
            // Create a new response message
            ChatMessage responseMessage = new ChatMessage(
                    otherUserId,
                    currentUserId,
                    "Đã nhận được tin nhắn của bạn!",
                    null,
                    ChatMessage.TYPE_RECEIVED
            );

            // Add the message to the list
            messageList.add(responseMessage);

            // Update the RecyclerView
            chatAdapter.notifyItemInserted(messageList.size() - 1);

            // Scroll to the bottom
            chatRecyclerView.scrollToPosition(messageList.size() - 1);
        }, 1000); // 1 second delay
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_block_user) {
            // TODO: Handle block user action
            Toast.makeText(requireContext(), "Người dùng đã bị chặn", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_report) {
            // TODO: Handle report user action
            Toast.makeText(requireContext(), "Đã báo cáo người dùng", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
