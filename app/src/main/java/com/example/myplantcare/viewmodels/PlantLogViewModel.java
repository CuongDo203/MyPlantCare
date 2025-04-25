package com.example.myplantcare.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myplantcare.data.repositories.TaskLogRepository;
import com.example.myplantcare.data.repositories.TaskLogRepositoryImpl;
import com.example.myplantcare.models.TaskLogModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.List;

public class PlantLogViewModel extends ViewModel {
    private final TaskLogRepository taskLogRepository;
    private static final String TAG = "PlantLogViewModel";
    private final MutableLiveData<List<TaskLogModel>> _taskLogs = new MutableLiveData<>();
    public final LiveData<List<TaskLogModel>> taskLogs = _taskLogs;

    // LiveData to expose loading state to the UI
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false); // Initial state is not loading
    public final LiveData<Boolean> isLoading = _isLoading;

    // LiveData to expose error messages to the UI
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    public PlantLogViewModel() {
        this.taskLogRepository = new TaskLogRepositoryImpl();
    }

    public void loadPlantTaskLogs(String userId, String plantId) {
        // Validate input before calling the repository
        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "loadPlantTaskLogs: userId is null or empty.");
            _errorMessage.postValue("Lỗi: Không tìm thấy thông tin người dùng."); // Post error
            _isLoading.postValue(false); // Ensure loading is off
            return;
        }
        if (plantId == null || plantId.isEmpty()) {
            Log.w(TAG, "loadPlantTaskLogs: plantId is null or empty.");
            _errorMessage.postValue("Lỗi: Không tìm thấy thông tin cây."); // Post error
            _isLoading.postValue(false); // Ensure loading is off
            return;
        }


        _isLoading.setValue(true); // Set loading state to true before fetching

        taskLogRepository.loadTaskLog(userId, plantId, new FirestoreCallback<List<TaskLogModel>>() {
            @Override
            public void onSuccess(List<TaskLogModel> result) {
                _isLoading.postValue(false);
                _taskLogs.postValue(result);
                _errorMessage.postValue(null);
                Log.d(TAG, "Task logs loaded successfully via ViewModel. Count: " + (result != null ? result.size() : 0));
            }

            @Override
            public void onError(Exception e) {
                _isLoading.postValue(false);
                _errorMessage.postValue("Lỗi tải lịch sử công việc: " + e.getMessage()); // Post error message
                _taskLogs.postValue(null);
                Log.e(TAG, "Error loading task logs via ViewModel", e);
            }
        });
    }

    public void clearErrorMessage() {
        _errorMessage.postValue(null);
    }
}
