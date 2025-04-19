package com.example.myplantcare.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myplantcare.data.repositories.MyPlantRepository;
import com.example.myplantcare.data.repositories.MyPlantRepositoryImpl;
import com.example.myplantcare.data.repositories.ScheduleRepository;
import com.example.myplantcare.data.repositories.ScheduleRepositoryImpl;
import com.example.myplantcare.models.ScheduleModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.ArrayList;
import java.util.List;

public class MyPlantDetailViewModel extends ViewModel {

    private static final String TAG = "MyPlantDetailViewModel";
    private final ScheduleRepository scheduleRepository;
    private String userId;
    private String myPlantId;

    private final MutableLiveData<List<ScheduleModel>> _schedules = new MutableLiveData<>();
    public final LiveData<List<ScheduleModel>> schedules = _schedules;

    private final MutableLiveData<Boolean> _isLoadingSchedules = new MutableLiveData<>();
    public final LiveData<Boolean> isLoadingSchedules = _isLoadingSchedules;

    private final MutableLiveData<String> _operationResult = new MutableLiveData<>();
    public final LiveData<String> operationResult = _operationResult;

    public static class Factory implements ViewModelProvider.Factory {
        private final String userId;
        private final String myPlantId;

        public Factory(String userId, String myPlantId) {
            this.userId = userId;
            this.myPlantId = myPlantId;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(MyPlantDetailViewModel.class)) {
                return (T) new MyPlantDetailViewModel(userId, myPlantId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    public MyPlantDetailViewModel(String userId, String myPlantId) {
        this.userId = userId;
        this.myPlantId = myPlantId;
        this.scheduleRepository = new ScheduleRepositoryImpl();
        loadMyPlantSchedule();
    }

    public void loadMyPlantSchedule() {
        _isLoadingSchedules.setValue(true);
        scheduleRepository.getSchedulesByMyPlantId(userId, myPlantId, new FirestoreCallback<List<ScheduleModel>>() {
            @Override
            public void onSuccess(List<ScheduleModel> result) {
                _schedules.setValue(result);
                _isLoadingSchedules.setValue(false);
                Log.d(TAG, "Plant schedules loaded. Size: " + (result != null ? result.size() : "null"));
            }

            @Override
            public void onError(Exception e) {
                _isLoadingSchedules.setValue(false);
                _operationResult.postValue("Lỗi tải danh sách lịch trình: " + e.getMessage());
                Log.e(TAG, "Error loading plant schedules", e);
//                _schedules.setValue(new ArrayList<>());
            }
        });
    }

    public void deleteSchedule(String scheduleId) {
        if (userId == null || myPlantId == null || scheduleId == null) {
            _operationResult.postValue("Lỗi: Không có thông tin để xóa lịch trình.");
            Log.w(TAG, "Cannot delete schedule: invalid input");
            return;
        }
        // Không cần set isLoading = true ở đây nếu không muốn block UI chính
        scheduleRepository.deleteSchedule(userId, myPlantId, scheduleId, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _operationResult.postValue("Đã xóa lịch trình thành công!");
                Log.d(TAG, "Schedule deleted successfully: " + scheduleId);
                // Sau khi xóa thành công, tải lại danh sách lịch trình để UI cập nhật
                loadMyPlantSchedule();
            }

            @Override
            public void onError(Exception e) {
                _operationResult.postValue("Lỗi xóa lịch trình: " + e.getMessage());
                Log.e(TAG, "Error deleting schedule: " + scheduleId, e);
            }
        });
    }

    public void clearOperationResult() {
        _operationResult.postValue(null);
    }
}
