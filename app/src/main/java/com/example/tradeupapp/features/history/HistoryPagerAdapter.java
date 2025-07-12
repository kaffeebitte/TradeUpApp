package com.example.tradeupapp.features.history;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HistoryPagerAdapter extends FragmentStateAdapter {
    public HistoryPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ViewedHistoryFragment();
            case 1:
                return new SavedHistoryFragment();
            case 2:
                return new SharedHistoryFragment();
            default:
                return new ViewedHistoryFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

