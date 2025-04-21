package com.example.myplantcare.viewmodels;

import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myplantcare.data.repositories.PlantRepository;
import com.example.myplantcare.data.repositories.PlantRepositoryImpl;
import com.example.myplantcare.data.repositories.SpeciesRepository;
import com.example.myplantcare.data.repositories.SpeciesRepositoryImpl;
import com.example.myplantcare.data.responses.PlantResponse;
import com.example.myplantcare.models.PlantModel;
import com.example.myplantcare.models.SpeciesModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlantInfoViewModel extends ViewModel {

    private final PlantRepository plantRepository;
    private final SpeciesRepository speciesRepository;
    private final MutableLiveData<List<PlantResponse>> _plants = new MutableLiveData<>();
    public LiveData<List<PlantResponse>> plants = _plants;
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    // LiveData cho thông báo lỗi (tùy chọn)
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;


    // Danh sách thông tin cây gốc (chưa được lọc)
    private List<PlantResponse> _originalPlantList = new ArrayList<>();

    // LiveData cho danh sách loại cây (species) để populate Spinner lọc
    private final MutableLiveData<List<SpeciesModel>> _speciesList = new MutableLiveData<>();
    public final LiveData<List<SpeciesModel>> getSpeciesList() {
        return _speciesList;
    }

    // Các tiêu chí lọc hiện tại
    private String currentSearchText = "";
    private String currentFilterSpeciesId = null;

    public PlantInfoViewModel() {
        plantRepository = new PlantRepositoryImpl();
        speciesRepository = new SpeciesRepositoryImpl();
        loadPlants();
        loadSpecies();
    }

    private void loadSpecies() {
        speciesRepository.getAllSpecies(new FirestoreCallback<List<SpeciesModel>>() {
            @Override
            public void onSuccess(List<SpeciesModel> result) {
                _speciesList.postValue(result != null ? result : new ArrayList<>());

                Log.d("PlantInfoViewModel", "Species list loaded. Size: " + (result != null ? result.size() : "null"));
            }

            @Override
            public void onError(Exception e) {
                _errorMessage.postValue("Lỗi tải danh sách loại cây: " + e.getMessage());
                _speciesList.postValue(new ArrayList<>()); // Đặt danh sách rỗng khi lỗi
                Log.e("PlantInfoViewModel", "Error loading species", e);
            }
        });
    }

    public void loadPlants() {
        _isLoading.setValue(true);
        plantRepository.getAllPlants(new FirestoreCallback<List<PlantResponse>>() {
            @Override
            public void onSuccess(List<PlantResponse> result) {
                _originalPlantList = result != null ? result : new ArrayList<>();
                Log.d("PlantInfoViewModel", "Original plant info list loaded. Size: " + _originalPlantList.size());
                applyFilter(currentSearchText, currentFilterSpeciesId); // Áp dụng filter hiện tại

                _isLoading.setValue(false);
//                _plants.setValue(result);
//                _isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                _isLoading.setValue(false); // Kết thúc loading
                _errorMessage.postValue("Lỗi tải thông tin cây: " + e.getMessage());
                Log.e("PlantInfoViewModel", "Error loading plant info", e);
                _originalPlantList = new ArrayList<>(); // Clear original list
                _plants.postValue(new ArrayList<>());
            }
        });
    }

    public void applyFilter(String searchText, String filterSpeciesId) {
        this.currentSearchText = searchText != null ? searchText.toLowerCase(Locale.getDefault()) : "";
        this.currentFilterSpeciesId = filterSpeciesId; // null hoặc Species ID

        List<PlantResponse> filteredList = new ArrayList<>();

        if (_originalPlantList != null) {
            // Lọc danh sách gốc
            for (PlantResponse plant : _originalPlantList) {
                boolean matchesSearch = true;
                boolean matchesType = true;

                // Lọc theo tên (case-insensitive)
                if (!TextUtils.isEmpty(this.currentSearchText)) {
                    // Giả định PlantResponse có phương thức getName() hoặc nickname
                    String plantName = plant.getPlantName() != null ? plant.getPlantName().toLowerCase(Locale.getDefault()) : ""; // Hoặc plant.getNickname()
                    matchesSearch = plantName.contains(this.currentSearchText);
                }

                // Lọc theo loại cây (Species ID)
                // currentFilterSpeciesId sẽ là null nếu chọn "Tất cả loại"
                if (!TextUtils.isEmpty(this.currentFilterSpeciesId)) {
                    // Giả định PlantResponse có phương thức getSpeciesId()
                    String plantSpeciesId = plant.getSpecies().getId() != null ? plant.getSpecies().getId() : "";
                    matchesType = this.currentFilterSpeciesId.equals(plantSpeciesId);
                }

                // Thêm vào danh sách lọc nếu khớp cả hai tiêu chí (hoặc không có tiêu chí)
                if (matchesSearch && matchesType) {
                    filteredList.add(plant);
                }
            }
        }

        _plants.setValue(filteredList);
        Log.d("PlantInfoViewModel", "Filter applied. Filtered list size: " + filteredList.size() +
                ", Search: '" + searchText + "', Species ID: '" + filterSpeciesId + "'");
    }

    public void clearErrorMessage() {
        _errorMessage.postValue(null);
    }
}
