package com.example.myplantcare.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myplantcare.fragments.MyPlantFragment;
import com.example.myplantcare.fragments.PlantInfoFragment;

public class ViewPagerPlantAdapter extends FragmentStateAdapter {

    public ViewPagerPlantAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    public ViewPagerPlantAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new MyPlantFragment();
            case 1:
                return new PlantInfoFragment();
            default:
                return new MyPlantFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
