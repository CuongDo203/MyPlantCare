package com.example.myplantcare.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myplantcare.R;
import com.example.myplantcare.viewmodels.UpdateMyPlantViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class MyPlantUpdateActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MyPlantUpdateActivity";

    private TextInputEditText editTextNickname, editTextLocation, editTextStatus;
    private TextInputEditText editTextHeight, editTextFlowers, editTextLeaves, editTextFruits;
    private TextView textViewGrowthDate;
    private Button buttonSaveGeneralInfo, buttonAddGrowthRecord;
    private ImageView btnBack;
    private ProgressBar progressBar;

    private String currentUserId;
    private String currentMyPlantId;
    private UpdateMyPlantViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_plant_update);

        Intent intent = getIntent();
        if(intent != null && intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            currentMyPlantId = extras.getString("id");
            currentUserId = extras.getString("userId");
        }
        if (currentUserId == null || currentMyPlantId == null) {
            Log.e(TAG, "User ID or MyPlant ID is missing. Cannot load data.");
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin cây.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initContents();
        setUpListeners();
    }

    private void initContents() {
        editTextNickname = findViewById(R.id.edit_text_nickname);
        editTextLocation = findViewById(R.id.edit_text_location);
        editTextStatus = findViewById(R.id.edit_text_status); // Nếu có trường status trong layout

        editTextHeight = findViewById(R.id.edit_text_height);
        editTextFlowers = findViewById(R.id.edit_text_flowers);
        editTextLeaves = findViewById(R.id.edit_text_leaves);
        editTextFruits = findViewById(R.id.edit_text_fruits);

        textViewGrowthDate = findViewById(R.id.text_view_growth_date);
        buttonSaveGeneralInfo = findViewById(R.id.button_save_general_info);
        buttonAddGrowthRecord = findViewById(R.id.button_add_growth_record);
        btnBack = findViewById(R.id.ivBack);
        progressBar = findViewById(R.id.progress_bar_update);
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new UpdateMyPlantViewModel(currentUserId, currentMyPlantId);
            }
        }).get(UpdateMyPlantViewModel.class);

    }

    private void setUpListeners() {
        btnBack.setOnClickListener(this);
        buttonSaveGeneralInfo.setOnClickListener(this);
        buttonAddGrowthRecord.setOnClickListener(this);

        viewModel.plantDetails.observe(this, plant -> {
            if(plant != null) {
                Log.d(TAG, "Plant details received. Populating UI.");
                editTextNickname.setText(plant.getNickname());
                editTextLocation.setText(plant.getLocation());
                editTextStatus.setText(plant.getStatus());
            }
            else {
                Log.w(TAG, "Plant details LiveData is null.");
            }
        });

        viewModel.isLoading.observe(this, isLoading -> {
            Log.d(TAG, "isLoading updated: " + isLoading);
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            // Tắt/Bật các nút hoặc trường nhập liệu khi đang tải
            buttonSaveGeneralInfo.setEnabled(!isLoading);
            buttonAddGrowthRecord.setEnabled(!isLoading);
        });

        viewModel.saveResult.observe(this, result -> {
            if(result != null && !result.isEmpty()) {
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Save result message: " + result);
                if ("Cập nhật thông tin chung thành công!".equals(result)) {
                    setResult(Activity.RESULT_OK);
                    finish();
                }

            }
        });
    }

    private void saveGeneralInfo() {
        String nickname = editTextNickname.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String status = editTextStatus.getText().toString().trim();

        if (nickname.isEmpty()) {
            editTextNickname.setError("Tên gọi không được để trống");
            editTextNickname.requestFocus();
            return;
        }
        if (location.isEmpty()) {
            editTextLocation.setError("Vị trí không được để trống");
            editTextLocation.requestFocus();
            return;
        }
        if (status.isEmpty()) {
            editTextStatus.setError("Trạng thái không được để trống");
            editTextStatus.requestFocus();
            return;
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("nickname", nickname);
        updates.put("location", location);
        updates.put("status", status);
        viewModel.updatePlantDetails(updates);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.ivBack) {
            finish();
        } else if (v.getId() == R.id.button_save_general_info) {
            saveGeneralInfo();
        }
    }
}