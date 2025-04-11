package com.example.myplantcare.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myplantcare.data.repositories.MyPlantRepository;
import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.List;

public class MyPlantListViewModel extends ViewModel {
    private final MyPlantRepository myPlantRepository;
    private final String currentUserId; // Cần cách lấy ID người dùng hiện tại

    private final MutableLiveData<List<MyPlantModel>> _myPlants = new MutableLiveData<>();
    public final LiveData<List<MyPlantModel>> myPlants = _myPlants;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;


    // Constructor (nên được inject qua Factory hoặc Hilt)
    public MyPlantListViewModel(MyPlantRepository repository, String userId) {
        this.myPlantRepository = repository;
        this.currentUserId = userId;
        // Gọi loadMyPlants ngay khi ViewModel được tạo và có userId
        loadMyPlants();
    }

    public void loadMyPlants() {
        _isLoading.setValue(true);
        myPlantRepository.getAllMyPlants(currentUserId, new FirestoreCallback<List<MyPlantModel>>() {
            @Override
            public void onSuccess(List<MyPlantModel > result) {
                _myPlants.setValue(result);
                _isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                _isLoading.setValue(false);
            }
        });
    }
}
