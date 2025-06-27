package com.example.tradeupapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;
import com.google.android.material.card.MaterialCardView;

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

        // TODO: Thiết lập Adapter cho các RecyclerView
        // categoriesRecycler.setAdapter(new CategoryAdapter(getCategoryList()));
        // featuredItemsRecycler.setAdapter(new ItemAdapter(getFeaturedItems()));
        // nearbyItemsRecycler.setAdapter(new ItemAdapter(getNearbyItems()));
    }
}
