package com.example.myplantcare.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myplantcare.R;
import com.example.myplantcare.adapters.MyPlantAdapter;
import com.example.myplantcare.data.repositories.MyPlantRepositoryImpl;
import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.viewmodels.MyPlantListViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class MyPlantFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyPlantAdapter adapter;
    private List<MyPlantModel> myPlantList;
    private FloatingActionButton fabAddMyPlant;
    private MyPlantListViewModel myPlantListViewModel;
    private MyPlantRepositoryImpl repository = new MyPlantRepositoryImpl();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_plant, container, false);

        initContents(view);
        setupViewModel();
        observeViewModel();
        fabAddMyPlant.setOnClickListener(v -> showAddPlantDialog());

//        myPlantList.add(new MyPlantModel("Xương rồng", "Phòng khách", 70, String.valueOf(R.drawable.plant_sample)));
//        myPlantList.add(new MyPlantModel("Sen đá", "Ban công", 45, String.valueOf(R.drawable.plant_sample)));
//        adapter.notifyDataSetChanged();

        return view;
    }

    private void observeViewModel() {

        myPlantListViewModel.myPlants.observe(getViewLifecycleOwner(), myPlants -> {
            myPlantList.clear();
            myPlantList.addAll(myPlants);
            Log.d("MyPlant", "Số lượng cây: " + myPlants.size());
            adapter.notifyDataSetChanged();
        });
        myPlantListViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {

        });
    }

    private void setupViewModel() {
        String userId = "eoWltJXzlBtC8U8QZx9G";  //tam thoi de hard code
        myPlantListViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new MyPlantListViewModel(userId);
            }
        }).get(MyPlantListViewModel.class);
    }

    private void initContents(View view) {
        fabAddMyPlant = view.findViewById(R.id.fab_add_my_plant);
        recyclerView = view.findViewById(R.id.recycler_my_plant);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        myPlantList = new ArrayList<>();
        String userId = "eoWltJXzlBtC8U8QZx9G";  //tam thoi de hard code
        adapter = new MyPlantAdapter(myPlantList, userId, repository, () -> {
            // callback sau khi xoá, có thể dùng load lại hoặc log
            Log.d("MyPlantFragment", "Đã xoá cây và cập nhật giao diện");
        });
        recyclerView.setAdapter(adapter);
    }

    private void showAddPlantDialog() {
        AddPlantDialogFragment dialogFragment = new AddPlantDialogFragment();
        dialogFragment.setOnSavePlantListener(() -> {
            myPlantListViewModel.loadMyPlants();
        });
        dialogFragment.show(getChildFragmentManager(), "AddPlantDialogTag");
    }

}