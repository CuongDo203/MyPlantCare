package com.example.myplantcare.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myplantcare.R;
import com.example.myplantcare.adapters.MyPlantAdapter;
import com.example.myplantcare.models.MyPlantModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class MyPlantFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyPlantAdapter adapter;
    private List<MyPlantModel> myPlantList;
    private FloatingActionButton fabAddMyPlant;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_plant, container, false);

        fabAddMyPlant = view.findViewById(R.id.fab_add_my_plant);
        recyclerView = view.findViewById(R.id.recycler_my_plant);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        fabAddMyPlant.setOnClickListener(v -> showAddPlantDialog());


        myPlantList = new ArrayList<>();
        adapter = new MyPlantAdapter(myPlantList);
        recyclerView.setAdapter(adapter);

        myPlantList.add(new MyPlantModel("Xương rồng", "Phòng khách", 70));
        myPlantList.add(new MyPlantModel("Sen đá", "Ban công", 45));
        adapter.notifyDataSetChanged();

        return view;
    }

    private void showAddPlantDialog() {
        AddPlantDialogFragment dialogFragment = new AddPlantDialogFragment();
        dialogFragment.show(getChildFragmentManager(), "AddPlantDialogTag");
    }
}