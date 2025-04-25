package com.example.myplantcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class PlantLogActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "PlantLogActivity";
    private RecyclerView recyclerView;
    private TaskLogAdapter taskLogAdapter;
    private List<TaskLogModel> taskLogList;
    private ImageView plantImage, btnBack;
    private TextView plantName;
    private String plantId, userId;
    private PlantLogViewModel plantLogViewModel;

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
        plantLogViewModel = new PlantLogViewModel();
        plantLogViewModel.loadPlantTaskLogs(userId, plantId);
        observeViewModel();
        btnBack.setOnClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskLogAdapter = new TaskLogAdapter(this, taskLogList);
        recyclerView.setAdapter(taskLogAdapter);
    }

    private void observeViewModel() {
        plantLogViewModel.taskLogs.observe(this, taskLogs -> {
            Log.d(TAG, "Task logs observed in Activity. Count: " + (taskLogs != null ? taskLogs.size() : 0));
            if (taskLogs != null) {
                taskLogAdapter = new TaskLogAdapter(this, taskLogs);
                recyclerView.setAdapter(taskLogAdapter);

            } else {
                Log.d(TAG, "Observed null task logs.");
                taskLogAdapter = new TaskLogAdapter(this, new ArrayList<>());
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