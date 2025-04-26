package com.example.myplantcare.viewmodels;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myplantcare.data.repositories.ImageRepository;
import com.example.myplantcare.data.repositories.ImageRepositoryImpl;
import com.example.myplantcare.data.repositories.TaskLogRepository;
import com.example.myplantcare.data.repositories.TaskLogRepositoryImpl;
import com.example.myplantcare.models.TaskLogModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.List;

public class PlantLogViewModel extends ViewModel {
    private final TaskLogRepository taskLogRepository;
    private final ImageRepository imageRepository;
    private static final String TAG = "PlantLogViewModel";
    private final MutableLiveData<List<TaskLogModel>> _taskLogs = new MutableLiveData<>();
    public final LiveData<List<TaskLogModel>> taskLogs = _taskLogs;

    // LiveData to expose loading state to the UI
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false); // Initial state is not loading
    public final LiveData<Boolean> isLoading = _isLoading;

    // LiveData to expose error messages to the UI
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<Boolean> _isUploadingImage = new MutableLiveData<>(false);
    public final LiveData<Boolean> isUploadingImage = _isUploadingImage;

    // LiveData cho lỗi upload ảnh (tùy chọn)
    private final MutableLiveData<String> _imageUploadError = new MutableLiveData<>();
    public final LiveData<String> imageUploadError = _imageUploadError;

    public PlantLogViewModel(Context context) {
        this.taskLogRepository = new TaskLogRepositoryImpl();
        this.imageRepository = new ImageRepositoryImpl(context);
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

    public void uploadTaskLogImage(String userId, String plantId, String taskLogId, Uri imageUri) {
        Log.d(TAG, "Attempting to upload image for task log ID: " + taskLogId);

        // Kiểm tra tham số
        if (taskLogId == null || taskLogId.isEmpty() || imageUri == null) {
            Log.w(TAG, "uploadTaskLogImage: invalid input. taskLogId or imageUri is null/empty.");
            _errorMessage.setValue("Lỗi: Thiếu thông tin ảnh hoặc mục lịch sử.");
            return;
        }

        _isUploadingImage.setValue(true);
        _imageUploadError.setValue(null);
        _errorMessage.setValue(null);

        imageRepository.uploadImage(imageUri, new FirestoreCallback<String>() {
            @Override
            public void onSuccess(String imageUrl) {
                Log.d(TAG, "Image uploaded to Cloudinary successfully. URL: " + imageUrl);
                taskLogRepository.uploadTaskLogImage(userId, plantId, taskLogId, imageUrl,
                        new FirestoreCallback<Void>() { // Truyền URL dưới dạng Uri
                            @Override
                            public void onSuccess(Void data) {
                                Log.d(TAG, "Task log document updated with image URL successfully.");
                                _isUploadingImage.setValue(false); // Kết thúc upload loading

                                loadPlantTaskLogs(userId, plantId);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Error updating task log document with image URL.", e);
                                _imageUploadError.setValue("Lỗi cập nhật Firestore: " + e.getMessage());
                                _errorMessage.setValue("Lỗi cập nhật Firestore: " + e.getMessage());
                                _isUploadingImage.setValue(false);
                            }
                        });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error uploading image to Cloudinary.", e);
                _imageUploadError.setValue("Lỗi tải ảnh lên Cloudinary: " + e.getMessage()); // Lỗi upload Cloudinary
                _errorMessage.setValue("Lỗi tải ảnh lên Cloudinary: " + e.getMessage()); // Lỗi chung
                _isUploadingImage.setValue(false); // Kết thúc upload loading
            }
        });
    }

    public void clearErrorMessage() {
        _errorMessage.postValue(null);
    }

    public void clearImageUploadError() {
        _imageUploadError.setValue(null);
    }
}
