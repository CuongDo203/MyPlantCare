package com.example.myplantcare.viewmodels;

import android.content.Context;
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
        imageRepository = new ImageRepositoryImpl(context);
    }

    public void uploadImage(File imageFile) {
        imageRepository.uploadImage(imageFile, new FirestoreCallback<String>() {
            @Override
            public void onSuccess(String result) {
                _imageUrl.postValue(result);
            }
            @Override
            public void onError(Exception e) {
                Log.e("Cloudinary", "Lỗi upload ảnh: " + e.getMessage());
            }
        });
    }

}
