package com.example.myplantcare.data.repositories;

import com.example.myplantcare.data.responses.PlantResponse;
import com.example.myplantcare.models.PlantModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.List;

public interface PlantRepository {
    void addPlant(PlantModel plant, FirestoreCallback<Void> callback);
    void getAllPlants(FirestoreCallback<List<PlantResponse>> callback);
}
