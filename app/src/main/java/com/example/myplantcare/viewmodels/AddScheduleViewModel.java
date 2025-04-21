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
import com.example.myplantcare.data.repositories.TaskRepository;
import com.example.myplantcare.data.repositories.TaskRepositoryImpl;
import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.models.ScheduleModel;
import com.example.myplantcare.models.TaskModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.List;

public class AddScheduleViewModel extends ViewModel {
    private static final String TAG = "AddScheduleViewModel";
    private final MyPlantRepository myPlantRepository;
    private final TaskRepository taskRepository;
    private final ScheduleRepository scheduleRepository;
    private final String userId;
    private final String myPlantId;
    private final MutableLiveData<List<MyPlantModel>> _userPlants = new MutableLiveData<>();
    public final LiveData<List<MyPlantModel>> userPlants = _userPlants;

    // LiveData cho danh sách tất cả tasks
    private final MutableLiveData<List<TaskModel>> _allTasks = new MutableLiveData<>();
    public final LiveData<List<TaskModel>> allTasks = _allTasks;

    // LiveData cho kết quả lưu lịch trình
    private final MutableLiveData<Boolean> _saveResult = new MutableLiveData<>();
    public final LiveData<Boolean> saveResult = _saveResult;

    // LiveData cho thông báo lỗi
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    // LiveData cho trạng thái loading (tùy chọn)
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final String userId;
        private final String myPlantId;

        public Factory(String userId, String myPlantId) {
            this.userId = userId;
            this.myPlantId = myPlantId;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AddScheduleViewModel.class)) {
                return (T) new AddScheduleViewModel(userId, myPlantId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    public AddScheduleViewModel(String userId, String myPlantId) {
        this.userId = userId;
        this.myPlantId = myPlantId;
        myPlantRepository = new MyPlantRepositoryImpl();
        taskRepository = new TaskRepositoryImpl();
        scheduleRepository = new ScheduleRepositoryImpl();
        loadUserPlants();
        loadAllTasks();
    }

    private void loadAllTasks() {
        _isLoading.setValue(true); // TODO: Cần quản lý cờ loading chi tiết hơn
        taskRepository.getAllTasks(new FirestoreCallback<List<TaskModel>>() {
            @Override
            public void onSuccess(List<TaskModel> result) {
                _allTasks.setValue(result);
                _isLoading.setValue(false); // TODO: Cần quản lý cờ loading chi tiết hơn
                Log.d(TAG, "All tasks loaded. Size: " + (result != null ? result.size() : "null"));
            }

            @Override
            public void onError(Exception e) {
                _errorMessage.postValue("Lỗi tải danh sách công việc: " + e.getMessage());
                _isLoading.setValue(false);
                Log.e(TAG, "Error loading all tasks", e);
            }
        });
    }

    private void loadUserPlants() {
        if (userId == null) {
            _errorMessage.postValue("Không có User ID để tải danh sách cây.");
            return;
        }
        _isLoading.setValue(true);
        myPlantRepository.getAllMyPlants(userId, new FirestoreCallback<List<MyPlantModel>>() {
            @Override
            public void onSuccess(List<MyPlantModel> result) {
                _userPlants.setValue(result);
                _isLoading.setValue(false);
                Log.d("AddScheduleViewModel", "User plants loaded. Size: " + (result != null ? result.size() : "null"));
            }

            @Override
            public void onError(Exception e) {
                _errorMessage.postValue("Lỗi tải danh sách cây: " + e.getMessage());
                _isLoading.setValue(false);
                Log.e("AddScheduleViewModel", "Error loading user plants", e);
            }
        });
    }

    public void addSchedule(String myPlantId, ScheduleModel scheduleModel) {
        if(userId == null || myPlantId == null || scheduleModel == null) {
            _errorMessage.postValue("Lỗi: Thiếu thông tin để lưu lịch trình.");
            return;
        }
        _isLoading.setValue(true); // TODO: Cần quản lý cờ loading chi tiết hơn
        scheduleRepository.addSchedule(userId, myPlantId, scheduleModel, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _saveResult.postValue(true); // Thông báo lưu thành công
                _isLoading.setValue(false);
                Log.d("AddScheduleViewModel", "Schedule added successfully for plant ID: " + myPlantId);
            }

            @Override
            public void onError(Exception e) {
                _errorMessage.postValue("Lỗi lưu lịch trình: " + e.getMessage());
                _saveResult.postValue(false); // Thông báo lưu thất bại
                _isLoading.setValue(false);
                Log.e("AddScheduleViewModel", "Error adding schedule for plant ID: " + myPlantId, e);
            }
        });
    }

    public void clearSaveResult() {
        _saveResult.postValue(null);
    }

    public void clearErrorMessage() {
        _errorMessage.postValue(null);
    }
}
