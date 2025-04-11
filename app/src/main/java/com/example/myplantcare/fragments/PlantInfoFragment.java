package com.example.myplantcare.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myplantcare.R;
import com.example.myplantcare.adapters.PlantInfoAdapter;
import com.example.myplantcare.models.PlantModel;
import com.example.myplantcare.viewmodels.PlantInfoViewModel;

import java.util.ArrayList;
import java.util.List;

public class PlantInfoFragment extends Fragment {

    private RecyclerView recyclerView;
    private PlantInfoAdapter plantInfoAdapter;
    private PlantInfoViewModel plantInfoViewModel;
    List<PlantModel> plantInfos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_info, container, false);
        initContents(view);
        setupViewModel();
        observeViewModel();
        return view;
    }

    private void setupViewModel() {
        plantInfoViewModel = new ViewModelProvider(this).get(PlantInfoViewModel.class);
    }

    private void observeViewModel() {
        plantInfoViewModel.plants.observe(getViewLifecycleOwner(), plants -> {
            plantInfos.clear();
            plantInfos.addAll(plants);
            Log.d("PlantInfo", "Số lượng cây: " + plantInfos.size());
            plantInfoAdapter.notifyDataSetChanged();
        });

        plantInfoViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            // Nếu muốn xử lý hiển thị loading indicator thì xử lý ở đây
            // Ví dụ: hiển thị ProgressBar
        });
    }

    private void initContents(View view) {
        plantInfos = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerViewPlantInfo);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        Log.d("PlantInfo", "Số lượng cây: " + plantInfos.size());
        plantInfoAdapter = new PlantInfoAdapter(plantInfos);
        recyclerView.setAdapter(plantInfoAdapter);
    }
}