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

public class PlantTipsFragment extends Fragment {

    private TextView tipsChamSoc;
    private String plantInstruction = null;

    public PlantTipsFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plantInstruction = getArguments().getString("instruction"); // Lấy String với key "instruction"
            Log.d("PlantTipsFragment", "Received instruction: " + plantInstruction); // Log để kiểm tra
        } else {
            Log.w("PlantTipsFragment", "Arguments bundle is null in onCreate.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_tips, container, false);
        tipsChamSoc = view.findViewById(R.id.tips_cham_soc);
        if (plantInstruction != null && !plantInstruction.isEmpty()) {
            tipsChamSoc.setText(plantInstruction);
        } else {
            tipsChamSoc.setText("Không có hướng dẫn chăm sóc cụ thể."); // Hiển thị text mặc định
            Log.w("PlantTipsFragment", "Instruction is null or empty, showing default text.");
        }
        return view;
    }
}