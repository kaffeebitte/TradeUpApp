package com.example.tradeupapp.features.history.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.tradeupapp.features.payment.ui.OfferHistoryFragment;

public class PurchaseOfferHistoryPagerAdapter extends FragmentStateAdapter {
    public PurchaseOfferHistoryPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new PurchaseHistoryFragment();
        } else {
            return OfferHistoryFragment.newInstance("buyer");
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
