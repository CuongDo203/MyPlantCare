package com.example.myplantcare.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myplantcare.R;

public class PlantInfoDetailFragment extends Fragment {

    private TextView motaCay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_info_detail, container, false);
        motaCay = view.findViewById(R.id.mota_cay);
        motaCay.setText("Thông tin chi tiết về cây");
        return view;
    }
}