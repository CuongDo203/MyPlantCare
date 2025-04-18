package com.example.myplantcare.data.repositories;

import android.content.Context;
import android.util.Log;

import com.cloudinary.Cloudinary;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.utils.ObjectUtils;
import com.example.myplantcare.utils.FirestoreCallback;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ImageRepositoryImpl implements ImageRepository{

    private Cloudinary cloudinary;
    private String cloudName = "djugptpce";

    public ImageRepositoryImpl(Context context) {
        initCloudinary(context);
    }

    private void initCloudinary(Context context) {
//        cloudinary = new Cloudinary(ObjectUtils.asMap(
//                "cloud_name", "djugptpce",
//                "api_key", "949566295821873",
//                "api_secret", "iK5cmT1V3lWXIsvSTjrq0TMxOcg"
//        ));

            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", cloudName);
            config.put("api_key", "949566295821873");
            config.put("api_secret", "iK5cmT1V3lWXIsvSTjrq0TMxOcg");
            MediaManager.init(context, config);

    }

    public void uploadImage(File imageFile, FirestoreCallback<String> callback) {

        String requestId = MediaManager.get()
                .upload(imageFile.getPath()) // Sử dụng đường dẫn file
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
                        Log.d("ImageRepository", "Upload progress: " + requestId + " " + (int)(progress * 100) + "%");
                        // Tùy chọn: thông báo tiến trình upload
                        // callback.onProgress(progress); // Nếu callback của bạn có onProgress
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Log.d("ImageRepository", "Upload successful: " + requestId);
                        String url = (String) resultData.get("secure_url");
                        if (url != null) {
                            callback.onSuccess(url); // Gọi onSuccess của callback với URL
                        } else {
                            callback.onError(new Exception("Upload successful but secure_url not found in result."));
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e("ImageRepository", "Upload error: " + requestId + " - " + error.getDescription());
                        callback.onError(new Exception(error.getDescription())); // Gọi onError của callback
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.w("ImageRepository", "Upload rescheduled: " + requestId + " - " + error.getDescription());
                        // Tùy chọn: xử lý khi upload bị reschedule
                    }
                })
                .dispatch(); // Bắt đầu quá trình upload

        Log.d("ImageRepository", "Upload dispatched with request ID: " + requestId);
    }

}
