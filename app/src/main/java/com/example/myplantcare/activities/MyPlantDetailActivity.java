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

import java.util.HashMap;
import java.util.Map;

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
    //    private MyPlantRepository myPlantRepository;
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

//        myPlantRepository = new MyPlantRepositoryImpl();

        // Lấy User ID hiện tại
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser != null) {
//            currentUserId = currentUser.getUid();
//        } else {
//            // Xử lý trường hợp người dùng chưa đăng nhập (không thể xem chi tiết cây của họ)
//            Toast.makeText(this, "Bạn cần đăng nhập", Toast.LENGTH_SHORT).show();
//            finish(); // Đóng Activity
//            return;
//        }
        currentUserId = "eoWltJXzlBtC8U8QZx9G"; // Xóa hardcode này khi có Auth thật

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
//        getAndDisplayPlantData();
        myPlantImg.setVisibility(View.GONE); // Hide ImageView initially
        lottieImageLoading.setVisibility(View.VISIBLE); // Show Lottie initially
        lottieImageLoading.playAnimation();
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


                    // --- Load image using Glide with RequestListener ---
                    if (plant.getImage() != null && !plant.getImage().isEmpty()) {
                        Log.d(TAG, "Loading image with Glide: " + plant.getImage());

                        // Ensure Lottie is showing while Glide loads (redundant with isLoadingDetails but safe)
                        // myPlantImg.setVisibility(View.GONE);
                        // lottieImageLoading.setVisibility(View.VISIBLE);
                        // lottieImageLoading.playAnimation();

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
                    // Handle case plant not found (e.g., show error message, finish activity)
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
                    // When loading details from DB finishes, the plantDetails observer
                    // will be triggered. Image loading visibility will be handled there by Glide Listener.
                    // Avoid hiding Lottie here immediately, let the Glide listener do it.
                    // lottieImageLoading.setVisibility(View.GONE);
                    // lottieImageLoading.cancelAnimation();
                    // TODO: Hide the progress bar
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
//                      reloadDataFromDb(currentUserId, myPlantId);
                        } else {
                            Log.e(TAG, "Cannot refresh data: userId or plantId is null.");
                        }
                    } else {
                        Log.d(TAG, "Result code is not OK (" + result.getResultCode() + ") from update.");
                    }
                }
        );
    }

    private void getAndDisplayPlantData() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            myPlantId = extras.getString("id"); // Lấy ID cây với key "id"

            if (myPlantId == null || currentUserId == null) {
                Log.e(TAG, "myPlantId or currentUserId is null.");
                Toast.makeText(this, "Lỗi dữ liệu cây", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            Log.d(TAG, "Loading details for myPlantId: " + myPlantId + " userId: " + currentUserId);
            // Hiển thị dữ liệu từ Intent (cần đảm bảo các key này đúng)
            myPlantName.setText(extras.getString("plantName")); // Hoặc key khác cho nickname
            myPlantLocation.setText(extras.getString("location"));
            myPlantStatus.setText(extras.getString("status"));
            Glide.with(this).load(extras.getString("image")).into(myPlantImg);

            if (extras.getString("my_plant_created") == null) {
                myPlantCreated.setText("--/--/----");
            } else {
                myPlantCreated.setText(extras.getString("my_plant_created"));
            }
            if (extras.getString("my_plant_updated") == null) {
                myPlantUpdated.setText("--/--/----");
            } else {
                myPlantUpdated.setText(extras.getString("my_plant_updated"));
            }
            //cap nhat sau, hien tai dang fixed cung
            myPlantHeight.setText("--cm");
            myPlantType.setText("N/A");
        } else {
            Log.e(TAG, "Intent or extras is null. Cannot load plant data.");
            Toast.makeText(this, "Không nhận được dữ liệu cây", Toast.LENGTH_SHORT).show();
            finish();
        }
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
                AddScheduleDialogFragment addScheduleDialogFragment = new AddScheduleDialogFragment();
                addScheduleDialogFragment.show(getSupportFragmentManager(), "ADD_SCHEDULE_DIALOG"); // Sử dụng getSupportFragmentManager()
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
//            myPlantImg.setVisibility(View.VISIBLE);
//            lottieImageLoading.setVisibility(View.GONE);
//            lottieImageLoading.cancelAnimation();
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
                        // Gọi ViewModel để xóa cây
                        myPlantDetailViewModel.deletePlant(); // <-- Gọi ViewModel
                        // Kết quả (thành công/thất bại và kết thúc Activity) được xử lý bởi observer plantDeletionResult
                        // showLoading(true); // Hiển thị indicator loading cho quá trình xóa cây nếu có
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
        // Không cần remove observers nếu dùng getViewLifecycleOwner() hoặc 'this' (Activity)
        // Cleanup khác nếu có (ví dụ: dừng animation Lottie khi Activity bị hủy)
        if (lottieImageLoading != null) {
            lottieImageLoading.cancelAnimation();
        }
        if (lottieNoSchedule != null) {
            lottieNoSchedule.cancelAnimation();
        }
    }
}