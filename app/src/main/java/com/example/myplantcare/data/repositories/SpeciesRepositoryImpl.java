package com.example.myplantcare.data.repositories;

import android.util.Log;

import com.example.myplantcare.models.SpeciesModel;
import com.example.myplantcare.utils.Constants;
import com.example.myplantcare.utils.FirestoreCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.List;

public class SpeciesRepositoryImpl implements SpeciesRepository{

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference speciesRef = db.collection(Constants.SPECIES_COLLECTION);

    @Override
    public List<SpeciesModel> getAllSpecies(FirestoreCallback<List<SpeciesModel>> callback) {
        speciesRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<SpeciesModel> species = queryDocumentSnapshots.toObjects(SpeciesModel.class);
            Log.d("SpeciesRepository", "Fetched species: " + species.size());
            callback.onSuccess(species);
        })
        .addOnFailureListener(callback::onError);
        return Collections.emptyList();
    }
}
