package com.example.tradeupapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.example.tradeupapp.adapters.ListingAdapter;
import com.example.tradeupapp.models.ItemModel;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView categoriesRecycler;
    private RecyclerView featuredItemsRecycler;
    private RecyclerView nearbyItemsRecycler;
    private MaterialCardView searchCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo các view
        initViews(view);

        // Thiết lập sự kiện cho search card
        setupClickListeners();

        // Thiết lập dữ liệu mẫu cho RecyclerView
        setupRecyclerViews();
    }

    private void initViews(View view) {
        categoriesRecycler = view.findViewById(R.id.categories_recycler);
        featuredItemsRecycler = view.findViewById(R.id.featured_items_recycler);
        nearbyItemsRecycler = view.findViewById(R.id.nearby_items_recycler);
        searchCard = view.findViewById(R.id.search_card);
    }

    private void setupClickListeners() {
        // Chuyển hướng đến màn hình tìm kiếm khi click vào search card
        searchCard.setOnClickListener(v -> {
            // Sử dụng Navigation Component để điều hướng
            // Navigation.findNavController(requireView()).navigate(R.id.action_nav_home_to_nav_search);
        });

        // Xử lý sự kiện khi click vào "See All"
        requireView().findViewById(R.id.see_all_featured).setOnClickListener(v -> {
            // TODO: Chuyển đến màn hình danh sách sản phẩm nổi bật
        });

        requireView().findViewById(R.id.see_all_nearby).setOnClickListener(v -> {
            // TODO: Chuyển đến màn hình danh sách sản phẩm gần đây
        });
    }

    private void setupRecyclerViews() {
        // Thiết lập LayoutManager cho RecyclerView
        categoriesRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        featuredItemsRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        nearbyItemsRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // Implement ListingAdapter for featured items
        List<ItemModel> featuredItems = getDummyItems("Featured");
        ListingAdapter featuredAdapter = new ListingAdapter(
                requireContext(),
                featuredItems,
                item -> navigateToItemDetail(item)
        );
        featuredItemsRecycler.setAdapter(featuredAdapter);

        // Implement ListingAdapter for nearby items
        List<ItemModel> nearbyItems = getDummyItems("Nearby");
        ListingAdapter nearbyAdapter = new ListingAdapter(
                requireContext(),
                nearbyItems,
                item -> navigateToItemDetail(item)
        );
        nearbyItemsRecycler.setAdapter(nearbyAdapter);

        // TODO: Implement CategoryAdapter for categories
        // categoriesRecycler.setAdapter(new CategoryAdapter(getCategoryList()));
    }

    private void navigateToItemDetail(ItemModel item) {
        // Navigate to item detail page
        Toast.makeText(requireContext(), "Selected: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        // TODO: Use proper navigation when item detail fragment is available
        // Bundle args = new Bundle();
        // args.putParcelable("selected_item", item);
        // Navigation.findNavController(requireView()).navigate(R.id.action_nav_home_to_nav_item_detail, args);
    }

    // Helper method to create dummy items for testing
    private List<ItemModel> getDummyItems(String prefix) {
        List<ItemModel> items = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ItemModel item = new ItemModel();
            item.setTitle(prefix + " Item " + i);
            item.setPrice(50.0 * i);
            item.setStatus("Available");
            item.setViewCount(10 * i);
            item.setInteractionCount(i);
            items.add(item);
        }
        return items;
    }
}
