package com.example.myplantcare;

import android.app.Application;
import android.util.Log;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyPlantApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Khởi tạo Cloudinary MediaManager
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "djugptpce");
        config.put("api_key", "949566295821873");
        config.put("api_secret", "iK5cmT1V3lWXIsvSTjrq0TMxOcg");   // Thay bằng API Secret của bạn
        // config.put("secure", "true"); // Tùy chọn: Đảm bảo dùng HTTPS

        try {
            MediaManager.init(this, config);
            Log.i("MyPlantApplication", "Cloudinary MediaManager Initialized");
        } catch (IllegalStateException e) {
            Log.w("MyPlantApplication", "MediaManager already initialized", e);
        } catch (Exception e) {
            Log.e("MyPlantApplication", "Error initializing MediaManager", e);
        }
    }

}
