package com.example.myplantcare.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myplantcare.data.repositories.MyPlantRepository;
import com.example.myplantcare.data.repositories.MyPlantRepositoryImpl;
import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.Map;

public class UpdateMyPlantViewModel extends ViewModel {

    private static final String TAG = "UpdatePlantViewModel";
    private final MyPlantRepository myPlantRepository;
    private final String userId;
    private String myPlantId;

    // LiveData cho dữ liệu cây chính
    private final MutableLiveData<MyPlantModel> _plantDetails = new MutableLiveData<>();
    public final LiveData<MyPlantModel> plantDetails = _plantDetails;

    // LiveData cho record Growth gần nhất
//    private final MutableLiveData<GrowthModel> _latestGrowth = new MutableLiveData<>();
//    public final LiveData<GrowthModel> latestGrowth = _latestGrowth;

    // LiveData cho trạng thái loading
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    // LiveData cho thông báo thành công/lỗi khi lưu/thêm
    private final MutableLiveData<String> _saveResult = new MutableLiveData<>();
    public final LiveData<String> saveResult = _saveResult;

    public UpdateMyPlantViewModel(String userId, String myPlantId) {
        this.userId = userId;
        this.myPlantId = myPlantId;
        this.myPlantRepository = new MyPlantRepositoryImpl(); // Khởi tạo Repository
        loadPlantDetails();
    }

    public void loadPlantDetails() {
        if (userId == null || myPlantId == null) {
            Log.e(TAG, "Cannot load plant details: userId or myPlantId is null");
            _saveResult.postValue("Lỗi: Không có thông tin người dùng hoặc cây.");
            return;
        }
        _isLoading.setValue(true);
        myPlantRepository.getMyPlantById(userId, myPlantId, new FirestoreCallback<MyPlantModel>() {
            @Override
            public void onSuccess(MyPlantModel result) {
                _plantDetails.setValue(result); // Cập nhật LiveData
                _isLoading.setValue(false);
                if (result == null) {
                    _saveResult.postValue("Không tìm thấy thông tin cây.");
                }
                Log.d(TAG, "Plant details loaded: " + (result != null ? result.getNickname() : "null"));
            }

            @Override
            public void onError(Exception e) {
                _isLoading.setValue(false);
                _saveResult.postValue("Lỗi tải thông tin cây: " + e.getMessage());
                Log.e(TAG, "Error loading plant details", e);
            }
        });
    }

    // Cập nhật thông tin chung của cây (nickname, location, status)
    public void updatePlantDetails(Map<String, Object> updates) {
        if (userId == null || myPlantId == null || updates == null || updates.isEmpty()) {
            _saveResult.postValue("Lỗi: Thông tin cập nhật không hợp lệ.");
            Log.w(TAG, "Cannot update plant details: invalid input");
            return;
        }
        _isLoading.setValue(true);
        myPlantRepository.updateMyPlant(userId, myPlantId, updates, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _isLoading.setValue(false);
                _saveResult.postValue("Cập nhật thông tin chung thành công!");
                Log.d(TAG, "Plant general info updated successfully.");
                // Sau khi cập nhật thành công, có thể tải lại chi tiết cây để UI phản ánh
                // loadPlantDetails(); // Tùy chọn nếu UI không tự cập nhật từ LiveData plantDetails (ví dụ nếu chỉ cập nhật 1 phần)
            }

            @Override
            public void onError(Exception e) {
                _isLoading.setValue(false);
                _saveResult.postValue("Lỗi cập nhật thông tin chung: " + e.getMessage());
                Log.e(TAG, "Error updating plant general info", e);
            }
        });
    }
}
