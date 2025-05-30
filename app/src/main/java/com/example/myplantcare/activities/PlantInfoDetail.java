// File: com.example.myplantcare.activities/PlantInfoDetail.java
package com.example.myplantcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.myplantcare.R;
import com.example.myplantcare.adapters.ViewPagerPlantInfoAdapter; // Import Adapter
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator; // Import TabLayoutMediator

import java.util.Locale;

public class PlantInfoDetail extends AppCompatActivity {

    private static final String TAG = "PlantInfoDetail"; // Thêm TAG

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ImageView btnBack;
    private TextView plantNameTextView, speciesNameTextView, lightInfoTextView, temperatureInfoTextView, moistureInfoTextView, waterInfoTextView; // Đổi tên biến để tránh trùng với String extra
    ImageView plantImageView; // Đổi tên biến

    // Biến để lưu dữ liệu từ Intent
    private String plantDescription;
    private String plantInstruction;
    private String currentPlantName; // Lưu tên cây nếu cần dùng sau này
    private String currentSpeciesName; // Lưu tên loài nếu cần dùng sau này
    private String currentPlantImage; // Lưu url ảnh nếu cần dùng sau này
    private String currentPlantId;
    private String currentIdealLight;
    private double currentIdealTemperatureMin, currentIdealTemperatureMax;
    private double currentIdealMoistureMin, currentIdealMoistureMax;
    private double currentIdealWaterMin, currentIdealWaterMax;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_info_detail);

        initViews(); // Đổi tên hàm tìm views
        getPlantData(); // Lấy dữ liệu từ Intent và lưu vào biến
        setupViewPagerWithTabs(); // Setup ViewPager và TabLayout sau khi có dữ liệu

        btnBack.setOnClickListener(v -> {
            finish(); // Quay lại màn hình trước
        });
    }

    // Đổi tên hàm từ initContents thành initViews cho rõ ràng
    private void initViews() {
        viewPager = findViewById(R.id.view_pager_plant);
        tabLayout = findViewById(R.id.tab_info);
        btnBack = findViewById(R.id.ivBack);
        plantImageView = findViewById(R.id.plant_image); // Sử dụng tên mới
        plantNameTextView = findViewById(R.id.plant_name); // Sử dụng tên mới
        speciesNameTextView = findViewById(R.id.species); // Sử dụng tên mới
        lightInfoTextView = findViewById(R.id.light_info); // Sử dụng tên mới
        temperatureInfoTextView = findViewById(R.id.temperature_info); // Sử dụng tên mới
        moistureInfoTextView = findViewById(R.id.moisture_info); // Sử dụng tên mới
        waterInfoTextView = findViewById(R.id.water_info); // Sử dụng tên mới

        // Adapter sẽ được khởi tạo sau khi có dữ liệu
        // viewPagerAdapter = new ViewPagerPlantInfoAdapter(this);
        // viewPager.setAdapter(viewPagerAdapter); // Chưa set adapter ở đây
    }

    private void getPlantData() {
        Intent intent = getIntent();
        if(intent != null) {
            // Lưu dữ liệu vào biến thành viên
            currentPlantName = intent.getStringExtra("plantName");
            currentSpeciesName = intent.getStringExtra("speciesName");
            currentPlantImage = intent.getStringExtra("plantImage");
            currentPlantId = intent.getStringExtra("plantId");
            currentIdealLight = intent.getStringExtra("idealLight");
            plantDescription = intent.getStringExtra("description"); // Lấy description
            plantInstruction = intent.getStringExtra("instruction"); // Lấy instruction
            currentIdealTemperatureMin = intent.getDoubleExtra("idealTemperatureMin", 0);
            currentIdealTemperatureMax = intent.getDoubleExtra("idealTemperatureMax", 0);
            currentIdealMoistureMin = intent.getDoubleExtra("idealMoistureMin", 0);
            currentIdealMoistureMax = intent.getDoubleExtra("idealMoistureMax", 0);
            currentIdealWaterMin = intent.getDoubleExtra("idealWaterMin", 0);
            currentIdealWaterMax = intent.getDoubleExtra("idealWaterMax", 0);

            Log.d(TAG, "Received Data:");
            Log.d(TAG, "Name: " + currentPlantName);
            Log.d(TAG, "Species: " + currentSpeciesName);
            Log.d(TAG, "Description: " + plantDescription); // Log để kiểm tra
            Log.d(TAG, "Instruction: " + plantInstruction); // Log để kiểm tra
            // Log các dữ liệu khác nếu cần

            // Hiển thị dữ liệu lên Views của Activity
            plantNameTextView.setText(currentPlantName);
            speciesNameTextView.setText(currentSpeciesName);
            lightInfoTextView.setText(currentIdealLight);
            temperatureInfoTextView.setText(String.format(Locale.getDefault(), "%.1f - %.1f °C", currentIdealTemperatureMin, currentIdealTemperatureMax)); // Sử dụng định dạng float và Locale
            moistureInfoTextView.setText(String.format(Locale.getDefault(), "%.0f - %.0f %%", currentIdealMoistureMin, currentIdealMoistureMax)); // Định dạng % không có thập phân
            waterInfoTextView.setText(String.format(Locale.getDefault(), "%.0f - %.0f ml", currentIdealWaterMin, currentIdealWaterMax)); // Định dạng ml không có thập phân

            Glide.with(this)
                    .load(currentPlantImage)
                    .placeholder(R.drawable.plant_sample)
                    .error(R.drawable.ic_photo_error)
                    .into(plantImageView); // Sử dụng tên mới
        } else {
            Log.w(TAG, "Intent is null, no plant data received.");
            // Xử lý trường hợp không nhận được dữ liệu (ví dụ: hiển thị thông báo lỗi và đóng Activity)
            Toast.makeText(this, "Không nhận được dữ liệu cây", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Hàm setup ViewPager và TabLayout sau khi đã có dữ liệu
    private void setupViewPagerWithTabs() {
        // Khởi tạo Adapter, truyền dữ liệu description và instruction vào đây
        ViewPagerPlantInfoAdapter viewPagerAdapter = new ViewPagerPlantInfoAdapter(this, plantDescription, plantInstruction);
        viewPager.setAdapter(viewPagerAdapter);

        // Liên kết ViewPager2 với TabLayout
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    // Đặt tên cho các tab
                    switch (position) {
                        case 0:
                            tab.setText("Chi tiết");
                            break;
                        case 1:
                            tab.setText("Mẹo");
                            break;
                    }
                }).attach();

    }

}