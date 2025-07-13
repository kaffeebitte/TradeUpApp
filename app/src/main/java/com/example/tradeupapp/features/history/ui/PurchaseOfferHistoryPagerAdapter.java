package com.example.tradeupapp.features.history.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.tradeupapp.features.history.ui.OfferHistoryFragment;
import com.example.tradeupapp.features.history.ui.PurchaseHistoryFragment;

public class PurchaseOfferHistoryPagerAdapter extends FragmentStateAdapter {
    public PurchaseOfferHistoryPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new OfferHistoryFragment();
        } else {
            return new PurchaseHistoryFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}

