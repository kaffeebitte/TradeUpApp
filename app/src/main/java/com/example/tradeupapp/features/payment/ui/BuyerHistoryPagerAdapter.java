package com.example.tradeupapp.features.payment.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.tradeupapp.features.home.ui.SavedItemsFragment;

public class BuyerHistoryPagerAdapter extends FragmentStateAdapter {

    private static final int TAB_COUNT = 3;
    private static final int SAVED_ITEMS_TAB = 0;
    private static final int OFFER_HISTORY_TAB = 1;
    private static final int PURCHASE_HISTORY_TAB = 2;

    public BuyerHistoryPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public BuyerHistoryPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case SAVED_ITEMS_TAB:
                return SavedItemsFragment.newInstance();
            case OFFER_HISTORY_TAB:
                return OfferHistoryFragment.newInstance();
            case PURCHASE_HISTORY_TAB:
                return PurchaseHistoryFragment.newInstance();
            default:
                throw new IllegalArgumentException("Invalid tab position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
