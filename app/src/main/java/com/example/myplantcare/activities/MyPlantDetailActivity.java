package com.example.myplantcare.activities;

import android.Manifest;
import android.app.Activity; // Import Activity
import android.content.Intent; // Import Intent
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.myplantcare.R;
import com.example.myplantcare.data.repositories.MyPlantRepository;
import com.example.myplantcare.data.repositories.MyPlantRepositoryImpl;
import com.example.myplantcare.fragments.AddScheduleDialogFragment;
import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.utils.DateUtils;
import com.example.myplantcare.utils.FirestoreCallback;
import com.example.myplantcare.viewmodels.ImageViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
    private MyPlantRepository myPlantRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_plant_detail);

        myPlantRepository = new MyPlantRepositoryImpl();

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
        registerActivityResults();

        btnBack.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnChangeImage.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
        btnAddSchedule.setOnClickListener(this);

        getAndDisplayPlantData();
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
                      reloadDataFromDb(currentUserId, myPlantId);
                  } else {
                      Log.e(TAG, "Cannot refresh data: userId or plantId is null.");
                  }
              }
              else {
                  Log.d(TAG, "Result code is not OK (" + result.getResultCode() + ") from update.");
              }
          }
        );
    }

    private void reloadDataFromDb(String userId, String myPlantId) {
        if(userId == null || myPlantId == null) {
            Log.e(TAG, "Cannot fetch plant data: userId or plantId is null.");
            return;
        }
        myPlantRepository.getMyPlantById(userId, myPlantId, new FirestoreCallback<MyPlantModel>() {
            @Override
            public void onSuccess(MyPlantModel plant) {
                if(plant!=null) {
                    Log.d(TAG, "Plant data fetched successfully. "+ plant);
                    myPlantName.setText(plant.getNickname()); // Hoặc key khác cho nickname
                    myPlantLocation.setText(plant.getLocation());
                    myPlantStatus.setText(plant.getStatus());
                    Glide.with(MyPlantDetailActivity.this).load(plant.getImage()).into(myPlantImg);
                    String formatCreatedDate = DateUtils.formatTimestamp(plant.getCreatedAt(), DateUtils.DATE_FORMAT_DISPLAY);
                    String formatUpdatedDate = DateUtils.formatTimestamp(plant.getUpdatedAt(), DateUtils.DATE_FORMAT_DISPLAY);
                    myPlantCreated.setText(formatCreatedDate);
                    myPlantUpdated.setText(formatUpdatedDate);

                    myPlantHeight.setText("--cm");
                    myPlantType.setText("N/A");
                }
                else {
                    Log.w(TAG, "MyPlant data not found for ID: " + myPlantId);
                    Toast.makeText(MyPlantDetailActivity.this, "Không tìm thấy thông tin cây", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng Activity nếu không tìm thấy cây
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching plant data", e);
                Toast.makeText(MyPlantDetailActivity.this, "Lỗi tải thông tin cây: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
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
            // TODO: Cần định dạng Timestamp/Date từ Extras sang String nếu chúng không phải String

            if(extras.getString("my_plant_created") == null) {
                myPlantCreated.setText("--/--/----");
            }
            else {
                myPlantCreated.setText(extras.getString("my_plant_created"));
            }
            if(extras.getString("my_plant_updated") == null) {
                myPlantUpdated.setText("--/--/----");
            }
            else {
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
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivBack) {
            finish();
        } else if (id == R.id.btn_delete_my_plant_detail) {
            // TODO: Xử lý sự kiện xóa cây
            // showDeleteConfirmationDialog(); // Ví dụ
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
                updatePlantImageInFirestore(currentUserId, myPlantId, imageUrl);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Image upload failed: " + e.getMessage(), e);
                Toast.makeText(MyPlantDetailActivity.this, "Lỗi tải ảnh lên: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                showLoading(false);
            }
        });
    }

    private void updatePlantImageInFirestore(String userId, String plantId, String imageUrl) {
        if (userId == null || plantId == null || imageUrl == null) {
            Log.e(TAG, "User ID, Plant ID, or Image URL is null. Cannot update Firestore.");
            Toast.makeText(this, "Lỗi hệ thống: Không thể cập nhật ảnh.", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }
        Log.d(TAG, "Updating plant image in Firestore for ID: " + plantId + " for user: " + userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("image", imageUrl);

        myPlantRepository.updateMyPlant(userId, plantId, updates, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Plant image updated in Firestore successfully.");
                Toast.makeText(MyPlantDetailActivity.this, "Cập nhật ảnh thành công!", Toast.LENGTH_SHORT).show();
                showLoading(false);
                // Cập nhật ảnh hiển thị trên UI ngay lập tức
                Glide.with(MyPlantDetailActivity.this).load(imageUrl).into(myPlantImg);

                // *** ĐẶT KẾT QUẢ TRẢ VỀ LÀ OK TRƯỚC KHI FINISH ***
                setResult(Activity.RESULT_OK); // Đặt kết quả thành công
                // TODO: Nếu bạn muốn truyền ID cây về, bạn có thể bỏ extra vào Intent:
                // Intent resultIntent = new Intent();
                // resultIntent.putExtra("updatedPlantId", plantId);
                // setResult(Activity.RESULT_OK, resultIntent);

                // finish(); // Activity sẽ tự đóng khi quay lại Fragment
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to update plant image in Firestore: " + e.getMessage(), e);
                Toast.makeText(MyPlantDetailActivity.this, "Lỗi cập nhật ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                showLoading(false);
            }
        });
    }


    private void showLoading(boolean isLoading) {
        btnChangeImage.setEnabled(!isLoading);
        // TODO: btnSave có thể không tồn tại, cần kiểm tra null hoặc thêm vào initContents
        // if (btnSave != null) {
        //    btnSave.setEnabled(!isLoading);
        // }
        // TODO: Thêm ProgressBar vào layout và điều khiển visibility ở đây
    }

    // TODO: Cần thêm các phương thức xử lý sự kiện click khác (Delete, Update, Add Schedule)

    // Thêm phương thức này nếu bạn cần xử lý khi Activity đóng (ví dụ: khi nhấn Back)
    // Nếu bạn dùng setResult(Activity.RESULT_OK) trong onSuccess và finish() sau đó,
    // MyPlantFragment sẽ nhận được kết quả OK. Nếu người dùng nhấn nút Back
    // trước khi update xong, kết quả mặc định là CANCELED, MyPlantFragment sẽ không refresh.
    // Điều này thường là hành vi mong muốn.

    // @Override
    // public void finish() {
    //     // Kiểm tra xem đã đặt kết quả chưa. Nếu chưa, đặt kết quả mặc định là CANCELED.
    //     // int resultCode = getCallingActivity() != null ? RESULT_CANCELED : RESULT_OK; // Logic phức tạp hơn
    //     // setResult(resultCode); // Có thể cần logic phức tạp hơn để đặt CANCELED đúng lúc
    //     super.finish();
    // }


}