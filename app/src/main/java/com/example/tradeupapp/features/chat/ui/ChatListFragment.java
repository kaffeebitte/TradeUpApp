package com.example.tradeupapp.features.chat.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.Navigation;

import com.example.tradeupapp.R;
import com.example.tradeupapp.models.ChatModel;
import com.example.tradeupapp.shared.adapters.ChatListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatListFragment extends Fragment implements ChatListAdapter.OnChatClickListener {
    private RecyclerView recyclerView;
    private ChatListAdapter adapter;
    private List<ChatModel> chatList;
    private FirebaseFirestore db;
    private String currentUserId;

    private Map<String, String> userNameMap = new HashMap<>();
    private Map<String, String> userAvatarMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        recyclerView = view.findViewById(R.id.chat_list_recycler_view);
        View emptyState = view.findViewById(R.id.empty_chat_list_state); // <-- get reference here
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        chatList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        adapter = new ChatListAdapter(chatList, this);
        adapter.setCurrentUserId(currentUserId);
        recyclerView.setAdapter(adapter);
        loadChats(emptyState);
        return view;
    }

    private void loadChats(View emptyState) {
        android.util.Log.d("ChatListFragment", "Loading chats for user: " + currentUserId);
        db.collection("chats")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    chatList.clear();
                    int foundCount = 0;
                    Set<String> userIdsToFetch = new java.util.HashSet<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        List<String> participants = (List<String>) doc.get("participants");
                        if (participants != null && participants.contains(currentUserId)) {
                            foundCount++;
                            android.util.Log.d("ChatListFragment", "Found chat: " + doc.getId() + " participants=" + participants);
                            ChatModel chat = doc.toObject(ChatModel.class);
                            chat.setId(doc.getId());
                            chatList.add(chat);
                            String otherUserId = chat.getOtherUserId(currentUserId);
                            if (otherUserId != null) userIdsToFetch.add(otherUserId);
                        }
                    }
                    android.util.Log.d("ChatListFragment", "Total chats found for user " + currentUserId + ": " + foundCount);
                    if (!userIdsToFetch.isEmpty()) {
                        db.collection("users").whereIn("id", new ArrayList<>(userIdsToFetch)).get()
                            .addOnSuccessListener(usersSnapshot -> {
                                userNameMap.clear();
                                for (QueryDocumentSnapshot userDoc : usersSnapshot) {
                                    String id = userDoc.getString("id");
                                    String name = userDoc.getString("displayName");
                                    String avatar = userDoc.getString("photoUrl");
                                    if (id != null && name != null) userNameMap.put(id, name);
                                    if (id != null && avatar != null) userNameMap.put(id + "_avatar", avatar);
                                }
                                adapter.setUserNameMap(userNameMap);
                                adapter.notifyDataSetChanged();
                                if (chatList.isEmpty()) {
                                    emptyState.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                } else {
                                    emptyState.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                }
                            });
                    } else {
                        adapter.setUserNameMap(userNameMap);
                        adapter.notifyDataSetChanged();
                        if (chatList.isEmpty()) {
                            emptyState.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyState.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                    // Sắp xếp chatList theo thời gian nhắn tin gần nhất (lastMessageTime hoặc createdAt)
                    chatList.sort((a, b) -> {
                        java.util.Date dateA = a.getLastUpdated() != null ? a.getLastUpdated().toDate() : null;
                        java.util.Date dateB = b.getLastUpdated() != null ? b.getLastUpdated().toDate() : null;
                        if (dateA == null && dateB == null) return 0;
                        if (dateA == null) return 1;
                        if (dateB == null) return -1;
                        return dateB.compareTo(dateA);
                    });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ChatListFragment", "Failed to load chats", e);
                    Toast.makeText(requireContext(), "Failed to load chats", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onChatClick(ChatModel chat) {
        Bundle args = new Bundle();
        args.putString("chatId", chat.getId());
        String otherUserId = chat.getOtherUserId(currentUserId);
        args.putString("otherUserId", otherUserId);
        String otherUserName = userNameMap.get(otherUserId);
        if (otherUserName != null) {
            args.putString("otherUserName", otherUserName);
        }
        Navigation.findNavController(requireView()).navigate(R.id.action_chatListFragment_to_chatDetailFragment, args);
    }
}
