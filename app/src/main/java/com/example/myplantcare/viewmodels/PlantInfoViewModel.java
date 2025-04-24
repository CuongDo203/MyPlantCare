package com.example.myplantcare.viewmodels;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myplantcare.data.repositories.PlantRepository;
import com.example.myplantcare.data.repositories.PlantRepositoryImpl;
import com.example.myplantcare.data.responses.PlantResponse;
import com.example.myplantcare.models.PlantModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.List;
public class PlantInfoViewModel extends ViewModel {

    private final PlantRepository plantRepository = new PlantRepositoryImpl(); // Khởi tạo trực tiếp
    private final MutableLiveData<List<PlantResponse>> _plants = new MutableLiveData<>();
    public LiveData<List<PlantResponse>> plants = _plants;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    public PlantInfoViewModel() {
        loadPlants();
    }

    public void loadPlants() {
        _isLoading.setValue(true);
        plantRepository.getAllPlants(new FirestoreCallback<List<PlantResponse>>() {
            @Override
            public void onSuccess(List<PlantResponse> result) {
                _plants.setValue(result);
                _isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                _isLoading.setValue(false);
                // handle error nếu muốn
            }
        });
    }
}
