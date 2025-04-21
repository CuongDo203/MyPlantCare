package com.example.myplantcare.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.myplantcare.R;
import com.example.myplantcare.adapters.MyPlantScheduleAdapter;
import com.example.myplantcare.data.responses.ScheduleDisplayItem;
import com.example.myplantcare.fragments.AddScheduleDialogFragment;
import com.example.myplantcare.utils.DateUtils;
import com.example.myplantcare.utils.FirestoreCallback;
import com.example.myplantcare.viewmodels.ImageViewModel;
import com.example.myplantcare.viewmodels.MyPlantDetailViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyPlantDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MyPlantDetailActivity";
    private TextView myPlantName, myPlantLocation, myPlantCreated, myPlantHeight,
            myPlantType, myPlantStatus, myPlantUpdated;
    private ImageView myPlantImg, btnDelete, btnUpdate, btnChangeImage, btnBack;
    private LinearLayout btnAddSchedule;

    private String myPlantId;
    private String currentUserId;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionRequestLauncher;
    private ActivityResultLauncher<Intent> updateActivityLauncher;
    private ImageViewModel imageViewModel;
    private RecyclerView recyclerViewSchedules;
    private MyPlantScheduleAdapter myPlantScheduleAdapter;
    private MyPlantDetailViewModel myPlantDetailViewModel;
    private LottieAnimationView lottieNoSchedule;

    private FrameLayout plantImageContainer; // Container around image and Lottie
    private LottieAnimationView lottieImageLoading;
    private ProgressBar progressBarSchedules; // Tham chiếu ProgressBar
    private LinearLayout emptySchedulesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_plant_detail);
        // Lấy User ID hiện tại
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Bạn cần đăng nhập", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity
            return;
        }

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            myPlantId = intent.getExtras().getString("id"); // Lấy ID từ Intent, key phải khớp
        }
        if (TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(myPlantId)) {
            Log.e(TAG, "User ID (" + currentUserId + ") or MyPlant ID (" + myPlantId + ") is null or empty. Cannot proceed.");
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin cây.", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity nếu thiếu ID quan trọng
            return; // Rời khỏi onCreate
        }
        myPlantDetailViewModel = new ViewModelProvider(this, new MyPlantDetailViewModel.Factory(currentUserId, myPlantId)).get(MyPlantDetailViewModel.class);
        imageViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(ImageViewModel.class)) {
                    return (T) new ImageViewModel(getApplicationContext());
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }).get(ImageViewModel.class);

        initContents();
        setupRecyclerViews();
        registerActivityResults();
        setupObservers();
        setupClickListeners();
        myPlantImg.setVisibility(View.GONE); // Hide ImageView initially
        lottieImageLoading.setVisibility(View.VISIBLE); // Show Lottie initially
        lottieImageLoading.playAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != null && myPlantId != null) {
            myPlantDetailViewModel.loadPlantDetails();
            myPlantDetailViewModel.loadPlantSchedulesAndTasks();
            Log.d(TAG, "onResume: Calling loadPlantDetails() and loadPlantSchedulesAndTasks() to refresh data.");
        } else {
            Log.w(TAG, "onResume: userId or myPlantId is null, cannot load data.");
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnChangeImage.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
        btnAddSchedule.setOnClickListener(this);
    }

    private void setupObservers() {
        myPlantDetailViewModel.plantDetails.observe(this, plant -> {
            if (!isFinishing() && !isDestroyed()) {
                if (plant != null) {
                    Log.d(TAG, "Plant details received from ViewModel. Updating UI.");
                    myPlantName.setText(plant.getNickname());
                    myPlantLocation.setText(plant.getLocation());
                    if (myPlantStatus != null) myPlantStatus.setText(plant.getStatus());
                    // TODO: Update other details (Height, Type, etc.) based on plant object

                    if (plant.getImage() != null && !plant.getImage().isEmpty()) {
                        Log.d(TAG, "Loading image with Glide: " + plant.getImage());
                        Glide.with(MyPlantDetailActivity.this)
                                .load(plant.getImage())
                                .listener(new RequestListener<Drawable>() { // Attach RequestListener
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        Log.e(TAG, "Glide image load failed", e);
                                        // Hide Lottie, show ImageView with a default placeholder on failure
                                        lottieImageLoading.setVisibility(View.GONE);
                                        lottieImageLoading.cancelAnimation();
                                        myPlantImg.setImageResource(R.drawable.plant_sample); // Set a default placeholder
                                        myPlantImg.setVisibility(View.VISIBLE); // Show ImageView
                                        return false; // Allow Glide's default behavior (e.g., showing error drawable)
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        Log.d(TAG, "Glide image loaded successfully.");
                                        // Hide Lottie, show ImageView with the loaded image
                                        lottieImageLoading.setVisibility(View.GONE);
                                        lottieImageLoading.cancelAnimation();
                                        myPlantImg.setVisibility(View.VISIBLE); // Show the ImageView
                                        return false; // Allow Glide to set the drawable
                                    }
                                })
                                .placeholder(R.drawable.plant_sample) // Optional: Placeholder while loading
                                .error(R.drawable.plant_sample) // Optional: Error image if load fails
                                .into(myPlantImg); // Load into the ImageView

                    } else {
                        // No image URL or empty URL - show default placeholder immediately
                        Log.d(TAG, "No image URL provided. Showing default placeholder.");
                        lottieImageLoading.setVisibility(View.GONE); // Hide Lottie
                        lottieImageLoading.cancelAnimation(); // Stop Lottie animation
                        myPlantImg.setImageResource(R.drawable.plant_sample); // Set a default placeholder
                        myPlantImg.setVisibility(View.VISIBLE); // Show placeholder ImageView
                    }

                    String formatCreatedDate = DateUtils.formatTimestamp(plant.getCreatedAt(), DateUtils.DATE_FORMAT_DISPLAY);
                    String formatUpdatedDate = DateUtils.formatTimestamp(plant.getUpdatedAt(), DateUtils.DATE_FORMAT_DISPLAY);
                    myPlantCreated.setText(formatCreatedDate);
                    myPlantUpdated.setText(formatUpdatedDate);
                }else {
                    Log.w(TAG, "Plant details LiveData is null or plant not found.");
                    Toast.makeText(this, "Không tìm thấy thông tin cây.", Toast.LENGTH_SHORT).show();
                    finish(); // Finish activity if plant details are null
                }
            }
            else {
                Log.d(TAG, "Ignoring plantDetails update, Activity is finishing or destroyed.");
            }
        });

        myPlantDetailViewModel.isLoadingDetails.observe(this, isLoading -> {
            if (!isFinishing() && !isDestroyed()) {
                Log.d(TAG, "isLoadingDetails updated: " + isLoading);
                if (isLoading) {
                    myPlantImg.setVisibility(View.GONE); // Hide ImageView
                    lottieImageLoading.setVisibility(View.VISIBLE); // Show Lottie
                    lottieImageLoading.playAnimation(); // Start animation
                    // TODO: Show a progress bar for the whole screen if needed
                } else {

                }
            } else {
                Log.d(TAG, "Ignoring isLoadingDetails update, Activity is finishing or destroyed.");
            }
        });

        myPlantDetailViewModel.schedules.observe(this, schedules -> {
            if (!isFinishing() && !isDestroyed()) {
                Log.d(TAG, "Schedules LiveData updated. Size: " + (schedules != null ? schedules.size() : "null"));

                // *** LOGIC HIỂN THỊ DỮ LIỆU/EMPTY STATE/LOADING ***
                if (schedules == null || schedules.isEmpty()) {
                    recyclerViewSchedules.setVisibility(View.GONE);
                    emptySchedulesView.setVisibility(View.VISIBLE); // Hiển thị layout rỗng
                    lottieNoSchedule.playAnimation(); // Bắt đầu animation
                } else {
                    // Có dữ liệu
                    emptySchedulesView.setVisibility(View.GONE); // Ẩn layout rỗng
                    recyclerViewSchedules.setVisibility(View.VISIBLE); // Hiển thị RecyclerView
                    myPlantScheduleAdapter.setSchedules(schedules); // Cập nhật Adapter

                }
                // Ẩn ProgressBar sau khi dữ liệu (rỗng hoặc không rỗng) đã được xử lý
                progressBarSchedules.setVisibility(View.GONE);

            } else {
                Log.d(TAG, "Ignoring schedules update, Activity is finishing.");
            }
        });
        myPlantDetailViewModel.isLoadingSchedules.observe(this, isLoading -> {
            Log.d(TAG, "isLoadingSchedules updated: " + isLoading);
        });
        myPlantDetailViewModel.operationResult.observe(this, result -> {
            if (result != null && !result.isEmpty()) {
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Operation result message: " + result);
                myPlantDetailViewModel.clearOperationResult();
            }
        });
        myPlantDetailViewModel.getPlantDeletionResult().observe(this, success -> { // Sử dụng this (Activity) làm LifecycleOwner
            if (success != null) {
                if (!isFinishing() && !isDestroyed()) { // Kiểm tra trạng thái Activity
                    if (success) {
                        Log.d(TAG, "Observed plant deletion success result. Finishing activity.");
                        setResult(Activity.RESULT_OK); // Đặt kết quả OK để Activity cha biết có sự thay đổi
                        finish(); // Đóng Activity sau khi xóa thành công
                    } else {
                        Log.w(TAG, "Observed plant deletion failure result.");
                    }
                    myPlantDetailViewModel.clearPlantDeletionResult(); // Xóa kết quả sau khi xử lý
                } else {
                    Log.d(TAG, "Ignoring plantDeletionResult, Activity is finishing or destroyed.");
                }
            }
        });

    }

    private void registerActivityResults() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            Log.d(TAG, "Image selected: " + selectedImageUri.toString());
                            uploadAndSaveNewImage(selectedImageUri);
                        } else {
                            Log.w(TAG, "Selected image Uri is null after picking.");
                            Toast.makeText(this, "Không thể lấy ảnh đã chọn.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "Image picking cancelled or failed.");
                    }
                });

        permissionRequestLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Storage permission granted. Opening image picker.");
                        openImagePicker();
                    } else {
                        Log.w(TAG, "Storage permission denied.");
                        Toast.makeText(this, "Cần quyền truy cập bộ nhớ để đổi ảnh", Toast.LENGTH_SHORT).show();
                    }
                });

        updateActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (currentUserId != null && myPlantId != null) {
                            Log.d(TAG, "Result code is OK from update. Refreshing plant data from DB via ViewModel.");
                            myPlantDetailViewModel.loadPlantDetails();
                            myPlantDetailViewModel.loadPlantSchedulesAndTasks();
                        } else {
                            Log.e(TAG, "Cannot refresh data: userId or plantId is null.");
                        }
                    } else {
                        Log.d(TAG, "Result code is not OK (" + result.getResultCode() + ") from update.");
                    }
                }
        );
    }

    private void initContents() {
        myPlantName = findViewById(R.id.my_plant_name_dt);
        myPlantLocation = findViewById(R.id.my_plant_location_dt);
        myPlantCreated = findViewById(R.id.my_plant_created_dt);
        myPlantHeight = findViewById(R.id.my_plant_height_dt);
        myPlantType = findViewById(R.id.my_plant_type_dt);
        myPlantStatus = findViewById(R.id.my_plant_status_dt);
        myPlantUpdated = findViewById(R.id.my_plant_updated_dt);
        myPlantImg = findViewById(R.id.my_plant_img_dt);
        btnDelete = findViewById(R.id.btn_delete_my_plant_detail);
        btnUpdate = findViewById(R.id.btn_update_my_plant_detail);
        btnChangeImage = findViewById(R.id.change_my_plant_image);
        btnBack = findViewById(R.id.ivBack);
        btnAddSchedule = findViewById(R.id.btn_them_lich_trinh);
        recyclerViewSchedules = findViewById(R.id.recycler_view_schedules);
        lottieNoSchedule = findViewById(R.id.lottie_no_schedule);
        progressBarSchedules = findViewById(R.id.progress_bar_schedules);
        emptySchedulesView = findViewById(R.id.empty_schedules_view);
        plantImageContainer = findViewById(R.id.plant_image_container);
        lottieImageLoading = findViewById(R.id.lottie_image_loading);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivBack) {
            finish();
        } else if (id == R.id.btn_delete_my_plant_detail) {
             showDeletePlantConfirmationDialog();
        } else if (id == R.id.change_my_plant_image) {
            checkPermissionsAndOpenPicker();
        } else if (id == R.id.btn_update_my_plant_detail) {
            Intent intent = new Intent(this, MyPlantUpdateActivity.class);
            intent.putExtra("id", myPlantId);
            intent.putExtra("userId", currentUserId);
            intent.putExtra("plantName", myPlantName.getText().toString());
            intent.putExtra("location", myPlantLocation.getText().toString());
            intent.putExtra("status", myPlantStatus.getText().toString());
            updateActivityLauncher.launch(intent);
        } else if (id == R.id.btn_them_lich_trinh) {

            Log.d(TAG, "Add Schedule button clicked.");
            if (currentUserId != null && myPlantId != null) {
                // Tạo instance của DialogFragment bằng newInstance() factory method
                AddScheduleDialogFragment addScheduleDialogFragment = AddScheduleDialogFragment.newInstance(myPlantId, currentUserId);
                addScheduleDialogFragment.setOnScheduleSavedListener(() -> {
                    Log.d(TAG, "AddScheduleDialogFragment reported schedule saved.");
                    if (myPlantDetailViewModel != null) {
                        myPlantDetailViewModel.loadPlantSchedulesAndTasks();
                        Log.d(TAG, "Triggering loadPlantSchedulesAndTasks() after dialog saved.");
                    } else {
                        Log.w(TAG, "ViewModel is null, cannot load schedules after dialog saved.");
                    }
                });
                addScheduleDialogFragment.show(getSupportFragmentManager(), "ADD_SCHEDULE_DIALOG");
            } else {
                Log.e(TAG, "Cannot open AddScheduleDialog: userId or plantId is null.");
                Toast.makeText(this, "Lỗi: Không có thông tin cây để thêm lịch trình.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // --- Logic đổi ảnh ---
    private void checkPermissionsAndOpenPicker() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission already granted. Opening image picker.");
            openImagePicker();
        } else {
            Log.d(TAG, "Permission not granted. Requesting permission: " + permission);
            permissionRequestLauncher.launch(permission);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadAndSaveNewImage(Uri imageUri) {
        if (currentUserId == null || myPlantId == null) {
            Log.e(TAG, "User ID or Plant ID is null. Cannot upload/save image.");
            Toast.makeText(this, "Lỗi hệ thống: Không có thông tin cây.", Toast.LENGTH_SHORT).show();
            return;
        }
        showLoading(true);
        imageViewModel.uploadImage(imageUri, new FirestoreCallback<String>() {
            @Override
            public void onSuccess(String imageUrl) {
                Log.d(TAG, "Image upload successful. New URL: " + imageUrl);
                myPlantDetailViewModel.updatePlantImage(imageUrl);
//                updatePlantImageInFirestore(currentUserId, myPlantId, imageUrl);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Image upload failed: " + e.getMessage(), e);
                Toast.makeText(MyPlantDetailActivity.this, "Lỗi tải ảnh lên: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                showLoading(false);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        btnChangeImage.setEnabled(!isLoading);
        if (isLoading) {
            myPlantImg.setVisibility(View.GONE);
            lottieImageLoading.setVisibility(View.VISIBLE);
            lottieImageLoading.playAnimation();
        } else {
        }
    }

    private void setupRecyclerViews() {
        // Setup RecyclerView cho Lịch trình
        myPlantScheduleAdapter = new MyPlantScheduleAdapter(new MyPlantScheduleAdapter.OnScheduleItemClickListener() {
            @Override
            public void onDeleteClick(ScheduleDisplayItem schedule) {
                showDeleteScheduleConfirmationDialog(schedule);
            }
            // TODO: Implement các click listener khác của ScheduleAdapter nếu có
        });
        recyclerViewSchedules.setLayoutManager(new LinearLayoutManager(this)); // LayoutManager (ví dụ: dọc)
        recyclerViewSchedules.setAdapter(myPlantScheduleAdapter);

    }

    private void showDeletePlantConfirmationDialog() {
         if (TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(myPlantId)) { // Sử dụng biến userId
             Log.w(TAG, "Cannot show delete plant dialog: userId or plantId is missing.");
             Toast.makeText(this, "Lỗi: Không có thông tin cây để xoá.", Toast.LENGTH_SHORT).show();
             return;
         }
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xoá cây")
                .setMessage("Bạn có chắc chắn muốn xoá cây này không? Thao tác này không thể hoàn tác.")
                .setPositiveButton("Xoá", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        myPlantDetailViewModel.deletePlant(); // <-- Gọi ViewModel
                    }
                })
                .setNegativeButton("Huỷ", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showDeleteScheduleConfirmationDialog(ScheduleDisplayItem scheduleToDelete) {
        // TODO: Triển khai AlertDialog xác nhận xóa lịch trình
        if (scheduleToDelete == null || TextUtils.isEmpty(scheduleToDelete.getScheduleId())) {
            Log.w(TAG, "Cannot show delete schedule dialog, invalid schedule.");
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xoá lịch trình")
                .setMessage("Bạn có chắc chắn muốn xoá lịch trình \"" + scheduleToDelete.getTaskName() + "\" không?") // Hiển thị tên task
                // TODO: Lấy tên task thân thiện thay vì Task ID
                .setPositiveButton("Xoá", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Gọi ViewModel để xóa lịch trình
                        if (!TextUtils.isEmpty(scheduleToDelete.getScheduleId())) {
                            myPlantDetailViewModel.deleteSchedule(scheduleToDelete.getScheduleId());
                        } else {
                            Log.w(TAG, "Cannot delete schedule, schedule ID is missing.");
                        }
                    }
                })
                .setNegativeButton("Huỷ", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lottieImageLoading != null) {
            lottieImageLoading.cancelAnimation();
        }
        if (lottieNoSchedule != null) {
            lottieNoSchedule.cancelAnimation();
        }
    }
}