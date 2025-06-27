package com.example.tradeupapp.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;

public class ChatFragment extends Fragment {

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
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
    }

    private void setupRecyclerView() {
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // TODO: Thiết lập adapter cho RecyclerView
        // chatRecyclerView.setAdapter(new ChatAdapter(getMessageList()));
    }

    private void setupSendMessageListener() {
        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageEditText.setText("");
            }
        });
    }

    private void sendMessage(String message) {
        // TODO: Xử lý logic gửi tin nhắn
        // 1. Thêm tin nhắn vào danh sách
        // 2. Cập nhật RecyclerView
        // 3. Cuộn RecyclerView đến vị trí mới nhất
    }
}
