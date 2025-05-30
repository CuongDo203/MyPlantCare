package com.example.myplantcare.viewmodels;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myplantcare.data.repositories.ImageRepository;
import com.example.myplantcare.data.repositories.ImageRepositoryImpl;
import com.example.myplantcare.data.repositories.MyPlantRepository;
import com.example.myplantcare.data.repositories.MyPlantRepositoryImpl;
import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.net.URI;

public class AddPlantViewModel extends ViewModel {
    private static final String TAG = "AddPlantViewModel";
    private final ImageRepository imageRepository;
    private final MyPlantRepository myPlantRepository;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    // LiveData to indicate success after saving
    private final MutableLiveData<Boolean> _saveSuccess = new MutableLiveData<>(false);
    public final LiveData<Boolean> saveSuccess = _saveSuccess;

    public AddPlantViewModel() {
        imageRepository = new ImageRepositoryImpl();
        myPlantRepository = new MyPlantRepositoryImpl();

    }

    public void savePlant(String userId, MyPlantModel myPlant, Uri selectedImageURI) {
        if (userId == null || userId.isEmpty() || myPlant == null) {
            Log.w(TAG, "savePlant: Invalid input. userId or myPlantToSave is null/empty.");
            _errorMessage.setValue("Lỗi: Thiếu thông tin để lưu cây.");
            return;
        }
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _saveSuccess.setValue(false);

        FirestoreCallback<Void> saveToFirestoreCallback = new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Plant saved to Firestore successfully.");
                _isLoading.setValue(false); // Kết thúc loading
                _saveSuccess.setValue(true); // Báo hiệu lưu thành công
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error saving plant to Firestore: " + e.getMessage(), e);
                _isLoading.setValue(false); // Kết thúc loading
                _errorMessage.setValue("Lỗi khi lưu cây: " + e.getMessage()); // Cập nhật lỗi
                _saveSuccess.setValue(false); // Đảm bảo trạng thái thành công là false
            }
        };

        if(selectedImageURI != null) {
            imageRepository.uploadImage(selectedImageURI, new FirestoreCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    myPlant.setImage(result);
                    myPlantRepository.addMyPlant(userId, myPlant, saveToFirestoreCallback);
                }

                @Override
                public void onError(Exception e) {
                    _isLoading.setValue(false); // Kết thúc loading
                    _errorMessage.setValue("Lỗi upload ảnh: " + e.getMessage()); // Cập nhật lỗi
                    _saveSuccess.setValue(false);
                }
            });
        }
        else {
            Log.d(TAG, "No image selected, saving plant directly via MyPlantRepository.");
            // Image URL trong myPlantToSave đang là null, phù hợp
            myPlantRepository.addMyPlant(userId, myPlant, saveToFirestoreCallback);
        }
    }

    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }

    public void clearSaveSuccess() {
        _saveSuccess.setValue(false);
    }
}
