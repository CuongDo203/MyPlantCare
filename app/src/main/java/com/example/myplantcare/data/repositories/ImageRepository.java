package com.example.myplantcare.data.repositories;

import com.example.myplantcare.utils.FirestoreCallback;

import java.io.File;

public interface ImageRepository {

    void uploadImage(File imageFile, FirestoreCallback<String> callback);

}
