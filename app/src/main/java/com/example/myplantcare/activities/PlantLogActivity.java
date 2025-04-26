package com.example.myplantcare.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myplantcare.R;
import com.example.myplantcare.adapters.TaskLogAdapter;
import com.example.myplantcare.models.TaskLogModel;
import com.example.myplantcare.viewmodels.PlantLogViewModel;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlantLogActivity extends AppCompatActivity implements View.OnClickListener,
        TaskLogAdapter.OnTaskLogImageClickListener{
    private static final String TAG = "PlantLogActivity";
    private RecyclerView recyclerView;
    private TaskLogAdapter taskLogAdapter;
    private List<TaskLogModel> taskLogList;
    private ImageView plantImage, btnBack;
    private TextView plantName;
    private String plantId, userId;
    private PlantLogViewModel plantLogViewModel;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private String currentTaskLogIdForImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_log);
        initContents();
        loadPlantInfo();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            Log.d(TAG, "Current user ID: " + userId);
        } else {
            Log.w(TAG, "User is not logged in. Cannot trigger ViewModel load.");
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử công việc.", Toast.LENGTH_SHORT).show();
        }
        plantLogViewModel = new PlantLogViewModel(this);
        plantLogViewModel.loadPlantTaskLogs(userId, plantId);
        observeViewModel();
        btnBack.setOnClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskLogAdapter = new TaskLogAdapter(this, taskLogList, this::onTaskLogImageClick);
        recyclerView.setAdapter(taskLogAdapter);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null && currentTaskLogIdForImage != null) {
                            Log.d(TAG, "Image selected: " + selectedImageUri.toString() + " for task log ID: " + currentTaskLogIdForImage);
                            plantLogViewModel.uploadTaskLogImage(userId, plantId, currentTaskLogIdForImage, selectedImageUri);
                            currentTaskLogIdForImage = null;
                        } else {
                            Log.w(TAG, "Selected image Uri is null or currentTaskLogIdForImage is null.");
                            Toast.makeText(this, "Không thể xử lý ảnh đã chọn.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "Image selection cancelled or failed.");
                        currentTaskLogIdForImage = null;
                    }
                });
    }

    @Override
    public void onTaskLogImageClick(String taskLogId) {
        Log.d(TAG, "Task log image clicked for ID: " + taskLogId);
        currentTaskLogIdForImage = taskLogId;
        openImagePicker();
    }

    // --- Phương thức mở trình chọn ảnh ---
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*"); // Chỉ cho phép chọn các loại file ảnh
        imagePickerLauncher.launch(intent); // Sử dụng launcher đã đăng ký
        Log.d(TAG, "Image picker launched.");
    }
    private void observeViewModel() {
        plantLogViewModel.taskLogs.observe(this, taskLogs -> {
            Log.d(TAG, "Task logs observed in Activity. Count: " + (taskLogs != null ? taskLogs.size() : 0));
            if (taskLogs != null) {
                taskLogAdapter = new TaskLogAdapter(this, taskLogs, this::onTaskLogImageClick);
                recyclerView.setAdapter(taskLogAdapter);
            } else {
                Log.d(TAG, "Observed null task logs.");
                taskLogAdapter = new TaskLogAdapter(this, new ArrayList<>(), this::onTaskLogImageClick);
                recyclerView.setAdapter(taskLogAdapter);
            }
        });

        plantLogViewModel.isLoading.observe(this, isLoading -> {
            //Hieu ung loading....
        });

        plantLogViewModel.errorMessage.observe(this, errorMessage -> {
            //neu co loi
        });
    }

    private void loadPlantInfo() {
        //Lay tu Intent
        Intent intent = getIntent();
        if(intent != null) {
            if(intent.hasExtra("id")) {
                plantId = intent.getStringExtra("id");
            }
            if(intent.hasExtra("plantName")) {
                plantName.setText(intent.getStringExtra("plantName"));
            }
            else {
                plantName.setText("Không xác định");
            }
            if(intent.hasExtra("image")) {
                String imageUrl = intent.getStringExtra("image");
//                plantImage.setImageResource(Integer.parseInt(imageUrl));
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.plant_sample)
                        .error(R.drawable.ic_photo_error)
                        .centerCrop()
                        .into(plantImage);
            }
            else {
                plantName.setText("Lỗi tải dữ liệu");
                plantImage.setImageResource(R.drawable.ic_photo_error);
            }
        }
    }

    private void initContents() {
        recyclerView = findViewById(R.id.recycler_view_log);
        plantImage = findViewById(R.id.image_view_log_plant);
        plantName = findViewById(R.id.text_view_log_plant_name);
        btnBack = findViewById(R.id.ivBack);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.ivBack) {
            finish();
        }
    }
}