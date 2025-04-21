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

import com.airbnb.lottie.LottieAnimationView;
import com.example.myplantcare.R;
import com.example.myplantcare.adapters.PlantInfoAdapter;
import com.example.myplantcare.data.responses.PlantResponse;
import com.example.myplantcare.models.PlantModel;
import com.example.myplantcare.viewmodels.PlantInfoViewModel;

import java.util.ArrayList;
import java.util.List;

public class PlantInfoFragment extends Fragment {

    private RecyclerView recyclerView;
    private PlantInfoAdapter plantInfoAdapter;
    private PlantInfoViewModel plantInfoViewModel;
    List<PlantResponse> plantInfos;
    private LottieAnimationView lottieLoadingAnimation;

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
            if(isLoading) {
                recyclerView.setVisibility(View.GONE);
                lottieLoadingAnimation.setVisibility(View.VISIBLE);
                lottieLoadingAnimation.playAnimation();
                Log.d("PlantInfo", "Loading...");
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                lottieLoadingAnimation.setVisibility(View.GONE);
                lottieLoadingAnimation.cancelAnimation();
            }
        });
    }

    private void initContents(View view) {
        plantInfos = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerViewPlantInfo);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        plantInfoAdapter = new PlantInfoAdapter(plantInfos);
        recyclerView.setAdapter(plantInfoAdapter);
        lottieLoadingAnimation = view.findViewById(R.id.lottie_loading_animation);
    }
}