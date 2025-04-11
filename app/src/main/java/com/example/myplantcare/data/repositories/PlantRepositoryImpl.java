package com.example.myplantcare.data.repositories;

import android.util.Log;

import com.example.myplantcare.models.PlantModel;
import com.example.myplantcare.utils.FirestoreCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PlantRepositoryImpl implements PlantRepository{

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    public void addPlant(PlantModel plant, FirestoreCallback<Void> callback) {
        CollectionReference plantsRef = db.collection("plants");

        // Nếu bạn muốn để Firestore tự sinh ID
        plantsRef.add(plant)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess(null); // Trả về null vì Void
                })
                .addOnFailureListener(callback::onError);
    }

    @Override
    public void getAllPlants(FirestoreCallback<List<PlantModel>> callback) {
        db.collection("plants")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<PlantModel> plants = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        PlantModel plant = doc.toObject(PlantModel.class);
                        plant.setId(doc.getId()); // nếu cần
                        plants.add(plant);
                    }
                    callback.onSuccess(plants);
                })
                .addOnFailureListener(callback::onError);

    }
}
