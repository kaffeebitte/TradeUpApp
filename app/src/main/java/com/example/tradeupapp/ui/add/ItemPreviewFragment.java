package com.example.tradeupapp.ui.add;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tradeupapp.R;
import com.example.tradeupapp.models.ItemModel;
import com.google.android.material.button.MaterialButton;

public class ItemPreviewFragment extends Fragment {

    private static final String ARG_ITEM = "item";

    private ItemModel item;
    private ViewPager2 photosViewPager;
    private TextView tvTitle;
    private TextView tvPrice;
    private TextView tvDescription;
    private TextView tvCategory;
    private TextView tvCondition;
    private TextView tvLocation;
    private MaterialButton btnEdit;
    private MaterialButton btnPublish;
    private ImagePhotoAdapter photoAdapter;

    public static ItemPreviewFragment newInstance(ItemModel item) {
        ItemPreviewFragment fragment = new ItemPreviewFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            item = getArguments().getParcelable(ARG_ITEM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        populateItemDetails();
        setupListeners();
    }

    private void initViews(View view) {
        photosViewPager = view.findViewById(R.id.viewpager_photos);
        tvTitle = view.findViewById(R.id.tv_item_title);
        tvPrice = view.findViewById(R.id.tv_item_price);
        tvDescription = view.findViewById(R.id.tv_item_description);
        tvCategory = view.findViewById(R.id.tv_item_category);
        tvCondition = view.findViewById(R.id.tv_item_condition);
        tvLocation = view.findViewById(R.id.tv_item_location);
        btnEdit = view.findViewById(R.id.btn_edit);
        btnPublish = view.findViewById(R.id.btn_publish);
    }

    private void populateItemDetails() {
        if (item == null) {
            Toast.makeText(requireContext(), "Error loading item details", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }

        // Set up the photo adapter
        photoAdapter = new ImagePhotoAdapter(requireContext(), item.getPhotoUris());
        photosViewPager.setAdapter(photoAdapter);

        // Set up item details
        tvTitle.setText(item.getTitle());
        tvPrice.setText(String.format("$%.2f", item.getPrice()));
        tvDescription.setText(item.getDescription());
        tvCategory.setText(item.getCategory());
        tvCondition.setText(item.getCondition());
        tvLocation.setText(item.getLocation());
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> {
            // Go back to edit the item
            requireActivity().onBackPressed();
        });

        btnPublish.setOnClickListener(v -> {
            // TODO: Implement API call to publish the item
            Toast.makeText(requireContext(), "Item published successfully!", Toast.LENGTH_SHORT).show();

            // Navigate back to home screen
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.nav_home);
        });
    }
}
