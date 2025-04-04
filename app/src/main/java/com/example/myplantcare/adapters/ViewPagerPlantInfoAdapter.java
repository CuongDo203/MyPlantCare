package com.example.myplantcare.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myplantcare.fragments.PlantInfoDetailFragment;
import com.example.myplantcare.fragments.PlantTipsFragment;

public class ViewPagerPlantInfoAdapter extends FragmentStateAdapter {
    public ViewPagerPlantInfoAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    public ViewPagerPlantInfoAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PlantInfoDetailFragment();
            case 1:
                return new PlantTipsFragment();
            default:
                return new PlantInfoDetailFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Số lượng tab
    }
}
