package com.example.myplantcare.viewmodels;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myplantcare.data.repositories.ImageRepository;
import com.example.myplantcare.data.repositories.ImageRepositoryImpl;
import com.example.myplantcare.utils.FirestoreCallback;

import java.io.File;

public class ImageViewModel extends ViewModel {

    private final MutableLiveData<String> _imageUrl = new MutableLiveData<>();
    public MutableLiveData<String> imageUrl = _imageUrl;

    private ImageRepository imageRepository;

    public ImageViewModel(Context context) {
        imageRepository = new ImageRepositoryImpl();
    }

    public void uploadImage(Uri imageUri, FirestoreCallback<String> callback) {
        imageRepository.uploadImage(imageUri, new FirestoreCallback<String>() {
            @Override
            public void onSuccess(String result) {
                // _imageUrl.postValue(result); // Cập nhật LiveData nếu dùng
                // _isLoading.postValue(false); // Cập nhật trạng thái loading
                Log.d("ImageViewModel", "Upload completed successfully in ViewModel.");
                callback.onSuccess(result); // Truyền kết quả về Fragment
            }

            @Override
            public void onError(Exception e) {
                // _error.postValue(e.getMessage()); // Cập nhật LiveData nếu dùng
                // _isLoading.postValue(false); // Cập nhật trạng thái loading
                Log.e("ImageViewModel", "Upload failed in ViewModel: " + e.getMessage());
                callback.onError(e); // Truyền lỗi về Fragment
            }
        });
    }

}
