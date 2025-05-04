package com.example.myplantcare.viewmodels;

import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myplantcare.data.repositories.MyPlantRepository;
import com.example.myplantcare.data.repositories.MyPlantRepositoryImpl;
import com.example.myplantcare.data.repositories.SpeciesRepository;
import com.example.myplantcare.data.repositories.SpeciesRepositoryImpl;
import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.models.SpeciesModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MyPlantListViewModel extends ViewModel {
    private final MyPlantRepository myPlantRepository;
    private final SpeciesRepository speciesRepository;
    private final String currentUserId; // Cần cách lấy ID người dùng hiện tại

    private final MutableLiveData<List<MyPlantModel>> _myPlants = new MutableLiveData<>();
    public final LiveData<List<MyPlantModel>> myPlants = _myPlants;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;
    private List<MyPlantModel> _originalPlantList = new ArrayList<>();
    // Các tiêu chí lọc hiện tại
    private String currentSearchText = "";
    private String currentFilterType = null;

    private final MutableLiveData<List<SpeciesModel>> _speciesList = new MutableLiveData<>();
    public final LiveData<List<SpeciesModel>> getSpeciesList() {
        return _speciesList;
    }

    public MyPlantListViewModel(String currentUserId) {
        myPlantRepository  = new MyPlantRepositoryImpl();
        speciesRepository = new SpeciesRepositoryImpl();
        this.currentUserId = currentUserId;
        loadMyPlants();
        loadSpecies();
    }

    public void loadMyPlants() {
        if (currentUserId == null) {
            _errorMessage.postValue("Không có User ID để tải danh sách cây.");
            _myPlants.postValue(new ArrayList<>()); // Clear list
            _originalPlantList = new ArrayList<>();
            return;
        }
        _isLoading.setValue(true);
        myPlantRepository.getAllMyPlants(currentUserId, new FirestoreCallback<List<MyPlantModel>>() {
            @Override
            public void onSuccess(List<MyPlantModel> result) {
                // Lưu danh sách gốc
                _originalPlantList = result != null ? result : new ArrayList<>();
                Log.d("MyPlantListViewModel", "Original plant list loaded. Size: " + _originalPlantList.size());
                // Áp dụng bộ lọc hiện tại lên danh sách gốc
                applyFilter(currentSearchText, currentFilterType); // Áp dụng filter hiện tại
                _isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                _errorMessage.postValue("Lỗi tải danh sách cây: " + e.getMessage());
                _isLoading.setValue(false);
                _originalPlantList = new ArrayList<>(); // Clear original list
                _myPlants.postValue(new ArrayList<>()); // Clear filtered list
                Log.e("MyPlantListViewModel", "Error loading user plants", e);
            }
        });
    }

    public void loadSpecies() {
        speciesRepository.getAllSpecies(new FirestoreCallback<List<SpeciesModel>>() {
            @Override
            public void onSuccess(List<SpeciesModel> result) {
                _speciesList.postValue(result != null ? result : new ArrayList<>());
                Log.d("MyPlantListViewModel", "Species list loaded. Size: " + (result != null ? result.size() : "null"));
            }

            @Override
            public void onError(Exception e) {
                _errorMessage.postValue("Lỗi tải danh sách loại cây: " + e.getMessage());
                _speciesList.postValue(new ArrayList<>());
                Log.e("MyPlantListViewModel", "Error loading species", e);
            }
        });
    }


    public void applyFilter(String searchText, String filterSpeciesId) {
        this.currentSearchText = searchText != null ? searchText.toLowerCase(Locale.getDefault()) : "";
        this.currentFilterType = filterSpeciesId;

        List<MyPlantModel> filteredList = new ArrayList<>();

        if (_originalPlantList != null) {
            // Lọc danh sách gốc
            for (MyPlantModel plant : _originalPlantList) {
                boolean matchesSearch = true;
                boolean matchesType = true;

                // Lọc theo tên (case-insensitive)
                if (!TextUtils.isEmpty(this.currentSearchText)) {
                    String plantName = plant.getNickname() != null ? plant.getNickname().toLowerCase(Locale.getDefault()) : "";
                    matchesSearch = plantName.contains(this.currentSearchText);
                }
                Log.d("MyPlantListViewModel", "After filter applied " + filteredList.size());
                // Lọc theo loại cây (Species ID)
                // currentFilterSpeciesId sẽ là null nếu chọn "Tất cả loại"
                if (!TextUtils.isEmpty(this.currentFilterType)) {
                    String plantSpeciesId = plant.getSpeciesId() != null ? plant.getSpeciesId() : "";
                    matchesType = this.currentFilterType.equals(plantSpeciesId);
                }
                if (matchesSearch && matchesType) {
                    filteredList.add(plant);
                }
            }
        }

        // Cập nhật LiveData myPlants với danh sách đã lọc
        _myPlants.setValue(filteredList);
        Log.d("MyPlantListViewModel", "Filter applied. Filtered list size: " + filteredList.size() +
                ", Search: '" + searchText + "', Species ID: " + filterSpeciesId);
    }

    public void deleteMyPlant(String plantId) {
        if (currentUserId == null || plantId == null || plantId.isEmpty()) {
            Log.e("MyPlantListViewModel", "Cannot delete plant: currentUserId is null or plantId is invalid");
            _errorMessage.postValue("Lỗi: Không đủ thông tin để xóa cây.");
            return;
        }

        myPlantRepository.deleteMyPlant(currentUserId, plantId, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d("MyPlantListViewModel", "Plant deleted successfully: " + plantId);
                _errorMessage.postValue("Đã xóa cây thành công!"); // Báo cáo thành công
                loadMyPlants();
            }

            @Override
            public void onError(Exception e) {
                Log.e("MyPlantListViewModel", "Error deleting plant: " + plantId, e);
                _errorMessage.postValue("Lỗi xóa cây: " + e.getMessage());
            }
        });
    }

    // Phương thức để xóa lỗi sau khi đã hiển thị (tùy chọn)
    public void clearErrorMessage() {
        _errorMessage.postValue(null);
    }
}
