package com.example.myplantcare.data.repositories;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.Cloudinary;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.HashMap;
import java.util.Map;

public class ImageRepositoryImpl implements ImageRepository {

    private Cloudinary cloudinary;
    private String cloudName = "djugptpce";

    public ImageRepositoryImpl(Context context) {
//        initCloudinary(context);
    }

    private void initCloudinary(Context context) {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", "949566295821873");
        config.put("api_secret", "iK5cmT1V3lWXIsvSTjrq0TMxOcg");
        MediaManager.init(context, config);


    }

    public void uploadImage(Uri imageUri, FirestoreCallback<String> callback) {
        if (imageUri == null) {
            Log.e("ImageRepository", "Image Uri is null.");
            callback.onError(new IllegalArgumentException("Image Uri cannot be null"));
            return;
        }

        Log.d("ImageRepository", "Attempting to upload Uri: " + imageUri.toString());

        String requestId = MediaManager.get()
                // Thay đổi .upload(File.getPath()) thành .upload(Uri)
                .upload(imageUri) // Sử dụng trực tiếp Uri
                .option("resource_type", "image") // Chỉ định loại resource
                // .option("folder", "your_folder_name") // Tùy chọn: upload vào một folder cụ thể
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("ImageRepository", "Upload started: " + requestId);
                        // Tùy chọn: thông báo cho callback rằng quá trình upload bắt đầu
                        // callback.onStart(); // Nếu callback của bạn có onStart
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        double progress = (double) bytes / totalBytes;
                        Log.d("ImageRepository", "Upload progress: " + requestId + " " + (int) (progress * 100) + "%");
                        // Tùy chọn: thông báo tiến trình upload
                        // callback.onProgress(progress); // Nếu callback của bạn có onProgress
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Log.d("ImageRepository", "Upload successful: " + requestId);
                        String url = (String) resultData.get("secure_url");
                        if (url != null) {
                            Log.d("ImageRepository", "Upload URL: " + url);
                            callback.onSuccess(url); // Gọi onSuccess của callback với URL
                        } else {
                            Log.e("ImageRepository", "Upload successful but secure_url not found in result data.");
                            callback.onError(new Exception("Upload successful but secure_url not found in result."));
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        String errorMessage = error != null ? error.getDescription() : "Unknown error";
                        Log.e("ImageRepository", "Upload error: " + requestId + " - " + errorMessage);
                        callback.onError(new Exception(errorMessage)); // Gọi onError của callback
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        String errorMessage = error != null ? error.getDescription() : "Unknown error";
                        Log.w("ImageRepository", "Upload rescheduled: " + requestId + " - " + errorMessage);
                        // Tùy chọn: xử lý khi upload bị reschedule
                    }
                })
                .dispatch(); // Bắt đầu quá trình upload

        Log.d("ImageRepository", "Upload dispatched with request ID: " + requestId);
    }

}
