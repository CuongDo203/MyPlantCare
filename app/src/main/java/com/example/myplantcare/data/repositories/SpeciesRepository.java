package com.example.myplantcare.data.repositories;

import com.example.myplantcare.models.SpeciesModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.List;

public interface SpeciesRepository {

    List<SpeciesModel> getAllSpecies(FirestoreCallback<List<SpeciesModel>> callback);

}
