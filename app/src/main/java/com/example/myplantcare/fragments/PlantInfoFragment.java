package com.example.myplantcare.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myplantcare.R;
import com.example.myplantcare.adapters.PlantInfoAdapter;
import com.example.myplantcare.models.PlantModel;

import java.util.ArrayList;
import java.util.List;

public class PlantInfoFragment extends Fragment {

    private RecyclerView recyclerView;
    private PlantInfoAdapter plantInfoAdapter;
    List<PlantModel> plantInfos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_info, container, false);
        initContents(view);
        plantInfos = new ArrayList<>();
        plantInfos.add(new PlantModel("Cây xương rồng", "Cây trong nhà", "Trong nhà", "", "",
                "30-35", "25-30 C", "50%","200-300ml"));
        plantInfos.add(new PlantModel("Cây sen đá", "Cây trong nhà", "Trong nhà", "", "",
                "35-38", "25-30 C", "70%","300-400ml"));
        plantInfoAdapter = new PlantInfoAdapter(plantInfos);
        recyclerView.setAdapter(plantInfoAdapter);
        return view;
    }

    private void initContents(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewPlantInfo);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }
}