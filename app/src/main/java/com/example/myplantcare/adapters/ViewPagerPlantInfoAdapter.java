package com.example.myplantcare.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myplantcare.fragments.PlantInfoDetailFragment;
import com.example.myplantcare.fragments.PlantTipsFragment;

public class ViewPagerPlantInfoAdapter extends FragmentStateAdapter {

    private final String plantDescription;
    private final String plantInstruction;
    public ViewPagerPlantInfoAdapter(@NonNull FragmentActivity fragmentActivity, String description, String instruction) {
        super(fragmentActivity);
        this.plantDescription = description;
        this.plantInstruction = instruction;
    }
    public ViewPagerPlantInfoAdapter(@NonNull Fragment fragment, String description, String instruction) {
        super(fragment);
        this.plantDescription = description;
        this.plantInstruction = instruction;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle bundle = new Bundle();
        switch (position) {
            case 0:
                PlantInfoDetailFragment detailFragment = new PlantInfoDetailFragment();
                // Đặt description vào arguments cho PlantInfoDetailFragment
                bundle.putString("description", plantDescription); // Dùng key "description"
                detailFragment.setArguments(bundle);
                return detailFragment;
            case 1:
                PlantTipsFragment tipsFragment = new PlantTipsFragment();
                // Đặt instruction vào arguments cho PlantTipsFragment
                bundle.putString("instruction", plantInstruction); // Dùng key "instruction"
                tipsFragment.setArguments(bundle);
                return tipsFragment;
            default:
                // Trả về fragment mặc định và vẫn truyền dữ liệu (nếu có)
                PlantInfoDetailFragment defaultFragment = new PlantInfoDetailFragment();
                bundle.putString("description", plantDescription); // Vẫn truyền description
                defaultFragment.setArguments(bundle);
                return defaultFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Số lượng tab
    }
}
