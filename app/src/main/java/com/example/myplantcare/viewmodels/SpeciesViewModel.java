package com.example.myplantcare.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myplantcare.data.repositories.SpeciesRepository;
import com.example.myplantcare.data.repositories.SpeciesRepositoryImpl;
import com.example.myplantcare.models.SpeciesModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.List;

public class SpeciesViewModel extends ViewModel {

    private final SpeciesRepository speciesRepository = new SpeciesRepositoryImpl();
    private final MutableLiveData<List<SpeciesModel>> _species = new MutableLiveData<>();
    public final MutableLiveData<List<SpeciesModel>> species = _species;
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    public SpeciesViewModel() {
        getAllSpecies();
    }

    private void getAllSpecies() {
        _isLoading.setValue(true);
        speciesRepository.getAllSpecies(new FirestoreCallback<List<SpeciesModel>>() {
            @Override
            public void onSuccess(List<SpeciesModel> data) {
                _isLoading.postValue(false);
                _species.postValue(data);
            }

            @Override
            public void onError(Exception e) {
                _isLoading.postValue(false);
                _errorMessage.postValue("Lỗi khi tải danh sách loại cây: " + e.getMessage());
            }
        });
    }


}
