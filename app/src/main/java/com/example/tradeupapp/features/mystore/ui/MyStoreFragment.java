package com.example.tradeupapp.features.mystore.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tradeupapp.R;
import com.example.tradeupapp.core.services.FirebaseService;
import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;
import com.example.tradeupapp.shared.adapters.UserListingAdapter;
import com.example.tradeupapp.shared.adapters.OfferAdapter;
import com.example.tradeupapp.models.OfferModel;
import com.example.tradeupapp.core.session.UserSession;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyStoreFragment extends Fragment {
    private RecyclerView rvStoreListings;
    private UserListingAdapter listingAdapter;
    private List<ListingModel> userListings = new ArrayList<>();
    private FirebaseService firebaseService;
    private TabLayout tabLayoutListingStatus;
    private String currentStatusFilter = "available";
    private List<ListingModel> allUserListings = new ArrayList<>();
    private RecyclerView rvStoreOffers;
    private OfferAdapter offerAdapter;
    private List<OfferModel> userOffers = new ArrayList<>();
    private Map<String, ListingModel> offerListingMap = new HashMap<>();
    private Map<String, ItemModel> offerItemMap = new HashMap<>();
    private TextView tvOfferEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_store, container, false);
        Button btnAddItem = view.findViewById(R.id.btn_add_item);
        btnAddItem.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.nav_add);
        });
        // Setup TabLayout for listing status
        tabLayoutListingStatus = view.findViewById(R.id.tab_layout_listing_status);
        tabLayoutListingStatus.addTab(tabLayoutListingStatus.newTab().setText("Available"));
        tabLayoutListingStatus.addTab(tabLayoutListingStatus.newTab().setText("Pending"));
        tabLayoutListingStatus.addTab(tabLayoutListingStatus.newTab().setText("Sold"));
        tabLayoutListingStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentStatusFilter = "available";
                        break;
                    case 1:
                        currentStatusFilter = "pending";
                        break;
                    case 2:
                        currentStatusFilter = "sold";
                        break;
                }
                filterListingsByStatus();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        // Setup RecyclerView for listings preview
        rvStoreListings = view.findViewById(R.id.rv_store_listings);
        // Change to vertical layout
        rvStoreListings.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        firebaseService = FirebaseService.getInstance();
        listingAdapter = new UserListingAdapter(getContext(), userListings, new UserListingAdapter.OnItemActionListener() {
            @Override
            public void onView(ListingModel listing, ItemModel item) {
                // Chỉ truyền id sang detail, dùng đúng key "listingId"
                Bundle args = new Bundle();
                args.putString("listingId", listing.getId());
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.itemDetailFragment, args);
            }
            @Override
            public void onEdit(ListingModel listing, ItemModel item) {
                // Navigate to update screen (dedicated update fragment)
                Bundle args = new Bundle();
                args.putSerializable("listing", listing);
                args.putParcelable("item", item);
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.updateItemFragment, args);
            }
            @Override
            public void onDelete(ListingModel listing, ItemModel item) {
                // Delete listing and item from Firebase, prevent delete if in transaction
                firebaseService.deleteListingWithItem(listing.getId(),
                    MyStoreFragment.this::loadUserListings,
                    errorMsg -> {
                        if (getContext() != null) {
                            android.widget.Toast.makeText(getContext(), errorMsg, android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }
                );
            }
        });
        rvStoreListings.setAdapter(listingAdapter);

        // Setup Offers RecyclerView
        rvStoreOffers = view.findViewById(R.id.rv_store_offers);
        rvStoreOffers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        offerAdapter = new OfferAdapter(getContext(), userOffers, offerListingMap, offerItemMap, new OfferAdapter.OnOfferActionListener() {
            @Override
            public void onViewDetail(ListingModel listing) {
                // Xem chi tiết sản phẩm
                Bundle args = new Bundle();
                args.putString("listingId", listing.getId());
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.itemDetailFragment, args);
            }
            @Override
            public void onAccept(OfferModel offer, ListingModel listing) {
                // TODO: Xử lý đồng ý offer (ví dụ cập nhật trạng thái giao dịch, gửi thông báo, ...)
                android.widget.Toast.makeText(getContext(), "Đã đồng ý đề nghị giá!", android.widget.Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onReject(OfferModel offer, ListingModel listing) {
                // TODO: Xử lý từ chối offer (ví dụ cập nhật tr��ng thái offer, gửi thông báo, ...)
                android.widget.Toast.makeText(getContext(), "Đã từ chối đề nghị giá!", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        rvStoreOffers.setAdapter(offerAdapter);
        tvOfferEmptyState = view.findViewById(R.id.tv_offer_empty_state);

        loadUserListings();
        return view;
    }
    private void filterListingsByStatus() {
        List<ListingModel> filtered = new ArrayList<>();
        for (ListingModel listing : allUserListings) {
            String status = listing.getTransactionStatus();
            if (currentStatusFilter.equals("available") && (status == null || status.equalsIgnoreCase("available"))) {
                filtered.add(listing);
            } else if (currentStatusFilter.equals("pending") && status != null && status.equalsIgnoreCase("pending")) {
                filtered.add(listing);
            } else if (currentStatusFilter.equals("sold") && status != null && status.equalsIgnoreCase("sold")) {
                filtered.add(listing);
            }
        }
        userListings.clear();
        userListings.addAll(filtered);
        if (listingAdapter != null) listingAdapter.notifyDataSetChanged();
        updateStoreStats();
    }
    private void loadUserListings() {
        String currentUserId = null;
        if (UserSession.getInstance().getCurrentUser() != null) {
            currentUserId = UserSession.getInstance().getCurrentUser().getUserIdOrUid();
        }
        if (currentUserId == null) {
            showOfferEmptyState("Bạn cần đăng nhập để xem cửa hàng của mình.");
            return;
        }
        firebaseService.getListingsBySellerId(currentUserId, new FirebaseService.ListingsCallback() {
            @Override
            public void onSuccess(List<ListingModel> listings) {
                allUserListings.clear();
                allUserListings.addAll(listings);
                filterListingsByStatus();
                // Lấy danh sách itemId từ các listing
                List<String> itemIds = new ArrayList<>();
                for (ListingModel listing : listings) {
                    if (listing.getItemId() != null) itemIds.add(listing.getItemId());
                }
                // Lấy thông tin item
                firebaseService.getItemsByIds(itemIds, new FirebaseService.ItemsByIdsCallback() {
                    @Override
                    public void onSuccess(Map<String, ItemModel> itemMap) {
                        offerItemMap.clear();
                        offerItemMap.putAll(itemMap);
                        // Sau khi đã có allUserListings và offerItemMap, load offer
                        loadUserOffers();
                    }
                    @Override
                    public void onError(String error) {
                        loadUserOffers();
                    }
                });
            }
            @Override
            public void onError(String error) {
                showOfferEmptyState("Không thể tải danh sách sản phẩm: " + error);
                loadUserOffers();
            }
        });
    }

    private void loadUserOffers() {
        String currentUserId = null;
        if (UserSession.getInstance().getCurrentUser() != null) {
            currentUserId = UserSession.getInstance().getCurrentUser().getUserIdOrUid();
        }
        if (currentUserId == null) {
            showOfferEmptyState("Bạn cần đăng nhập để xem đề nghị giá.");
            return;
        }
        // Lấy tất cả listingId của user hiện tại
        List<String> listingIds = new ArrayList<>();
        for (ListingModel listing : allUserListings) {
            if (listing.getId() != null) listingIds.add(listing.getId());
        }
        if (listingIds.isEmpty()) {
            userOffers.clear();
            offerListingMap.clear();
            if (offerAdapter != null) offerAdapter.notifyDataSetChanged();
            showOfferEmptyState("Chưa có đề nghị giá nào cho sản phẩm của bạn.");
            return;
        }
        firebaseService.getOffersByListingIds(listingIds, new FirebaseService.OffersCallback() {
            @Override
            public void onSuccess(List<OfferModel> offers) {
                userOffers.clear();
                userOffers.addAll(offers);
                // Map listingId -> ListingModel
                offerListingMap.clear();
                for (ListingModel listing : allUserListings) {
                    offerListingMap.put(listing.getId(), listing);
                }
                if (offerAdapter != null) offerAdapter.notifyDataSetChanged();
                if (offers.isEmpty()) {
                    showOfferEmptyState("Chưa có đề nghị giá nào cho sản phẩm của bạn.");
                } else {
                    showOfferContent();
                }
            }
            @Override
            public void onError(String error) {
                userOffers.clear();
                offerListingMap.clear();
                if (offerAdapter != null) offerAdapter.notifyDataSetChanged();
                showOfferEmptyState("Không thể tải đề nghị giá: " + error);
            }
        });
    }

    private void showOfferEmptyState(String message) {
        if (tvOfferEmptyState != null) {
            tvOfferEmptyState.setVisibility(View.VISIBLE);
            tvOfferEmptyState.setText(message);
        }
        if (rvStoreOffers != null) rvStoreOffers.setVisibility(View.GONE);
    }
    private void showOfferContent() {
        if (tvOfferEmptyState != null) tvOfferEmptyState.setVisibility(View.GONE);
        if (rvStoreOffers != null) rvStoreOffers.setVisibility(View.VISIBLE);
    }

    private void updateStoreStats() {
        int activeCount = 0;
        int soldCount = 0;
        int totalViews = 0;
        int totalSaves = 0;
        for (ListingModel listing : userListings) {
            String status = listing.getTransactionStatus();
            if (status != null && status.equalsIgnoreCase("available")) {
                activeCount++;
            } else if (status != null && status.equalsIgnoreCase("sold")) {
                soldCount++;
            }
            if (listing.getInteractions() != null && listing.getInteractions().getAggregate() != null) {
                totalViews += listing.getInteractions().getAggregate().getTotalViews();
                totalSaves += listing.getInteractions().getAggregate().getTotalSaves();
            } else {
                totalViews += listing.getViewCount();
            }
        }
        View view = getView();
        if (view != null) {
            TextView tvStatActive = view.findViewById(R.id.tv_stat_active);
            TextView tvStatSold = view.findViewById(R.id.tv_stat_sold);
            TextView tvStatViews = view.findViewById(R.id.tv_stat_views);
            TextView tvStatSaves = view.findViewById(R.id.tv_stat_saves);
            // Removed tvStatShares, not in layout
            tvStatActive.setText(String.valueOf(activeCount));
            tvStatSold.setText(String.valueOf(soldCount));
            tvStatViews.setText(String.valueOf(totalViews));
            if (tvStatSaves != null) tvStatSaves.setText(String.valueOf(totalSaves));
        }
    }
}
