package com.example.myplantcare.data.repositories;

import android.util.Log;

import com.example.myplantcare.data.responses.PlantResponse;
import com.example.myplantcare.models.IdealConditionModel;
import com.example.myplantcare.models.PlantModel;
import com.example.myplantcare.models.SpeciesModel;
import com.example.myplantcare.utils.FirestoreCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlantRepositoryImpl implements PlantRepository{

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference plantsRef = db.collection("plants");
    private final CollectionReference speciesRef = db.collection("species");
    private final CollectionReference conditionsRef = db.collection("ideal_conditions");
    @Override
    public void addPlant(PlantModel plant, FirestoreCallback<Void> callback) {


        // Nếu bạn muốn để Firestore tự sinh ID
        plantsRef.add(plant)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess(null); // Trả về null vì Void
                })
                .addOnFailureListener(callback::onError);
    }

    @Override
    public void getAllPlants(FirestoreCallback<List<PlantResponse>> callback) {
        plantsRef.get().addOnSuccessListener(plantsSnapshot -> {
            if (plantsSnapshot.isEmpty()) {
                Log.d("PlantRepository", "No plants found.");
                callback.onSuccess(new ArrayList<>()); // Trả về list rỗng
                return;
            }
            List<PlantModel> plantModels = new ArrayList<>();
            Set<String> speciesIds = new HashSet<>();
            Set<String> idealConditionIds = new HashSet<>();

            for(QueryDocumentSnapshot doc : plantsSnapshot) {
                PlantModel plant = doc.toObject(PlantModel.class);
                plant.setId(doc.getId());
                plantModels.add(plant);
                if(plant.getSpeciesId() != null && !plant.getSpeciesId().isEmpty()) {
                    speciesIds.add(plant.getSpeciesId());
                }
                if(plant.getIdealConditionId() != null && !plant.getIdealConditionId().isEmpty()) {
                    idealConditionIds.add(plant.getIdealConditionId());
                }
            }

            if (speciesIds.isEmpty() && idealConditionIds.isEmpty()) {
                List<PlantResponse> displayDataList = new ArrayList<>();
                for(PlantModel pm : plantModels) {
                    displayDataList.add(new PlantResponse(pm, null, null));
                }
                callback.onSuccess(displayDataList);
                return;
            }

            Task<QuerySnapshot> speciesTask = speciesIds.isEmpty() ?
                    Tasks.forResult(null)
                    : speciesRef.whereIn(FieldPath.documentId(), new ArrayList<>(speciesIds)).get();

            Task<QuerySnapshot> conditionsTask = idealConditionIds.isEmpty() ?
                    Tasks.forResult(null)
                    : conditionsRef.whereIn(FieldPath.documentId(), new ArrayList<>(idealConditionIds)).get();

            Tasks.whenAllSuccess(speciesTask, conditionsTask).addOnSuccessListener(results  -> {
                Map<String, SpeciesModel> speciesMap = new HashMap<>();
                Map<String, IdealConditionModel> conditionsMap = new HashMap<>();

                // Xử lý kết quả species
                QuerySnapshot speciesResult = (QuerySnapshot) results.get(0);
                if(speciesResult != null) {
                    for(QueryDocumentSnapshot doc : speciesResult) {
                        speciesMap.put(doc.getId(), doc.toObject(SpeciesModel.class));
                    }
                }

                // Xử lý kết quả conditions (results.get(1))
                QuerySnapshot conditionsResult = (QuerySnapshot) results.get(1);
                if (conditionsResult != null) {
                    for (QueryDocumentSnapshot doc : conditionsResult) {
                        conditionsMap.put(doc.getId(), doc.toObject(IdealConditionModel.class));
                    }
                }

                // Kết hợp dữ liệu
                List<PlantResponse> finalPlantData = new ArrayList<>();
                for (PlantModel plant : plantModels) {
                    SpeciesModel species = speciesMap.get(plant.getSpeciesId());
                    IdealConditionModel condition = conditionsMap.get(plant.getIdealConditionId());
                    finalPlantData.add(new PlantResponse(plant, species, condition));
                }
                callback.onSuccess(finalPlantData);
            })
            .addOnFailureListener(e -> {
                Log.e("PlantRepository", "Error fetching species or conditions", e);
                callback.onError(e);
            });
        })
        .addOnFailureListener(e -> {
            Log.e("PlantRepository", "Error fetching plants", e);
            callback.onError(e);
        });
    }
}
