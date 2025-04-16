package com.example.myplantcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.myplantcare.R;
import com.example.myplantcare.adapters.ViewPagerPlantInfoAdapter;
import com.google.android.material.tabs.TabLayout;

public class PlantInfoDetail extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ViewPagerPlantInfoAdapter viewPagerAdapter;
    private ImageView btnBack;
    private TextView plantName, speciesName, lightInfo, temperatureInfo, moistureInfo, waterInfo;
    ImageView plantImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_info_detail);

        initContents();
        getPlantData();
        btnBack.setOnClickListener(v -> {
            finish();
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (tabLayout.getTabCount() > position && tabLayout.getTabAt(position) != null) {
                    tabLayout.getTabAt(position).select();
                }
            }
        });
    }

    private void getPlantData() {
        Intent intent = getIntent();
        if(intent != null) {
            String plantName = intent.getStringExtra("plantName");
            String speciesName = intent.getStringExtra("speciesName");
            String plantImage = intent.getStringExtra("plantImage");
            String plantId = intent.getStringExtra("plantId");
            String idealLight = intent.getStringExtra("idealLight");
            double idealTemperatureMin = intent.getDoubleExtra("idealTemperatureMin", 0);
            double idealTemperatureMax = intent.getDoubleExtra("idealTemperatureMax", 0);
            double idealMoistureMin = intent.getDoubleExtra("idealMoistureMin", 0);
            double idealMoistureMax = intent.getDoubleExtra("idealMoistureMax", 0);
            double idealWaterMin = intent.getDoubleExtra("idealWaterMin", 0);
            double idealWaterMax = intent.getDoubleExtra("idealWaterMax", 0);

            this.plantName.setText(plantName);
            this.speciesName.setText(speciesName);
            this.lightInfo.setText(idealLight);
            this.temperatureInfo.setText(String.format("%s - %s C", String.valueOf(idealTemperatureMin), String.valueOf(idealTemperatureMax)));
            this.moistureInfo.setText(String.format("%s - %s %%", String.valueOf(idealMoistureMin), String.valueOf(idealMoistureMax)));
            this.waterInfo.setText(String.format("%s - %s ml", String.valueOf(idealWaterMin), String.valueOf(idealWaterMax)));
            Glide.with(this)
                    .load(plantImage)
                    .placeholder(R.drawable.plant_sample)
                    .error(R.drawable.ic_photo_error)
                    .into(this.plantImage);
        }
    }

    private void initContents() {
        viewPager = findViewById(R.id.view_pager_plant);
        tabLayout = findViewById(R.id.tab_info);
        btnBack = findViewById(R.id.ivBack);
        plantImage = findViewById(R.id.plant_image);
        plantName = findViewById(R.id.plant_name);
        speciesName = findViewById(R.id.species);
        lightInfo = findViewById(R.id.light_info);
        temperatureInfo = findViewById(R.id.temperature_info);
        moistureInfo = findViewById(R.id.moisture_info);
        waterInfo = findViewById(R.id.water_info);

        viewPagerAdapter = new ViewPagerPlantInfoAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
    }
}