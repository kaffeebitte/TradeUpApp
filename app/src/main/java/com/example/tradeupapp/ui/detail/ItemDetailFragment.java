package com.example.tradeupapp.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tradeupapp.R;

public class ItemDetailFragment extends Fragment {

    private String itemId;
    private Toolbar toolbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get item ID from arguments
        if (getArguments() != null) {
            itemId = getArguments().getString("itemId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Setup toolbar with back navigation
        setupToolbar();

        // Load item data
        loadItemData();
    }

    private void initViews(View view) {
        // Initialize toolbar
        toolbar = view.findViewById(R.id.toolbar);

        // TODO: Initialize your item detail views here
        // For now, we'll just display a temporary message
        TextView tempMessage = view.findViewById(R.id.temp_message);
        if (tempMessage != null) {
            tempMessage.setText("Item Detail for ID: " + itemId);
        }
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_back_24); // Use existing back icon
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle back navigation
                    requireActivity().onBackPressed();
                }
            });
        }
    }

    private void loadItemData() {
        // In a real app, you would load item data from your repository
        // For now, show a toast with the item ID
        Toast.makeText(requireContext(), "Loading item with ID: " + itemId, Toast.LENGTH_SHORT).show();
    }
}
