package com.example.myplantcare.data.repositories;

import android.net.Uri;

import com.example.myplantcare.utils.FirestoreCallback;

import java.io.File;

public interface ImageRepository {

    void uploadImage(Uri imageUri, FirestoreCallback<String> callback);

}
