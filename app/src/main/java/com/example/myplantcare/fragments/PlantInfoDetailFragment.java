package com.example.myplantcare.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myplantcare.R;

public class PlantInfoDetailFragment extends Fragment {

    private TextView motaCay;
    private String plantDescription = null;

    public PlantInfoDetailFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy arguments được truyền sang ngay trong onCreate
        if (getArguments() != null) {
            plantDescription = getArguments().getString("description");
            Log.d("PlantDetailFragment", "Received description: " + plantDescription);
        } else {
            Log.w("PlantDetailFragment", "Arguments bundle is null in onCreate.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_info_detail, container, false);
        motaCay = view.findViewById(R.id.mota_cay);
        // Sử dụng biến plantDescription đã lấy được
        if (plantDescription != null && !plantDescription.isEmpty()) {
            motaCay.setText(plantDescription);
        } else {
            motaCay.setText("Không có mô tả chi tiết."); // Hiển thị text mặc định nếu không có description
            Log.w("PlantDetailFragment", "Description is null or empty, showing default text.");
        }

        return view;
    }
}