package com.example.tradeupapp.features.history.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.tradeupapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.viewpager2.widget.ViewPager2;

public class PurchaseOfferHistoryFragment extends Fragment {
    public PurchaseOfferHistoryFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_purchase_offer_history, container, false);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager_purchase_offer_history);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout_purchase_offer_history);
        PurchaseOfferHistoryPagerAdapter adapter = new PurchaseOfferHistoryPagerAdapter(this);
        viewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Purchase History");
            } else {
                tab.setText("Offer History");
            }
        }).attach();
        return view;
    }
}
