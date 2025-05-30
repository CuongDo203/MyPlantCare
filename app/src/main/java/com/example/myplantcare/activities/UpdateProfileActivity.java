package com.example.myplantcare.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.myplantcare.R;
import com.example.myplantcare.viewmodels.UpdateProfileViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class UpdateProfileActivity extends AppCompatActivity {

    private static final String TAG = "UpdateProfileActivity";
    private UpdateProfileViewModel viewModel;
    private String userId;
    private ImageView ivBack, avatar;
    private TextInputEditText edtName, edtPhone, edtDob;
    private MaterialButton btnSave;

    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        initContents();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(Class<T> modelClass) {
                return (T) new UpdateProfileViewModel(userId);
            }
        }).get(UpdateProfileViewModel.class);
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        viewModel.onImageSelected(selectedImageUri); // Gửi URI cho ViewModel
                    }
                });
        observeViewModel();
        setUpListeners();
    }

    private void observeViewModel() {
        viewModel.isLoading.observe(this, isLoading -> {
            if (isLoading) {
                btnSave.setEnabled(false);
            } else {
                btnSave.setEnabled(true);
            }
            btnSave.setText(isLoading ? "Đang cập nhật..." : "Lưu thông tin");
        });

        viewModel.userProfile.observe(this, user -> {
            Log.d(TAG, "observeViewModel: user = " + user);
            if (user != null) {
                edtName.setText(user.getName());
                edtPhone.setText(user.getPhone());
                edtDob.setText(user.getDob());
            }
        });

        viewModel.errorMessage.observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            }
        });

        viewModel.updateSuccess.observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        viewModel.openDatePickerEvent.observe(this, aVoid -> {
            showDatePickerDialog();
        });

        viewModel.previewImageUri.observe(this, uri -> {
            Log.d(TAG, "observeViewModel: previewImageUri changed, uri = " + uri);
            if (uri != null) {
                Glide.with(this)
                        .load(uri)
                        .circleCrop()
                        .placeholder(R.drawable.ic_avatar)
                        .error(R.drawable.ic_photo_error)
                        .into(avatar);
            } else {
                Glide.with(this)
                        .load(R.drawable.ic_avatar) // Tải ảnh mặc định
                        .circleCrop() // Bo tròn ảnh mặc định
                        .into(avatar);
            }
        });

        viewModel.navigateBackEvent.observe(this, aVoid -> {
            finish(); // Đóng Activity
//            viewModel.navigateBackEvent.setValue(null);
        });


        // ---- Xử lý sự kiện từ người dùng (gửi đến ViewModel) ----

        avatar.setOnClickListener(v -> viewModel.onAvatarClicked());

        edtDob.setOnClickListener(v -> viewModel.onDobClicked());

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String dob = edtDob.getText().toString().trim();

            // Gọi phương thức updateProfile của ViewModel
            viewModel.updateProfile(name, phone, dob /*, city*/);
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageLauncher.launch(Intent.createChooser(intent, "Chọn ảnh đại diện"));
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, yearSelected, monthOfYear, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + yearSelected;
                    edtDob.setText(selectedDate);
//                     viewModel.onDateSelected(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }
    private void setUpListeners() {
        ivBack.setOnClickListener(v -> viewModel.onBackClicked());
        btnSave.setOnClickListener(v -> {
            viewModel.updateProfile(edtName.getText().toString(), edtPhone.getText().toString(), edtDob.getText().toString());
        });
        avatar.setOnClickListener(v -> openImageChooser());
        edtDob.setOnClickListener(v -> viewModel.onDobClicked());
    }

    private void initContents() {
        ivBack = findViewById(R.id.ivBack);
        avatar = findViewById(R.id.imageViewAvatar);
        edtName = findViewById(R.id.editTextName);
        edtPhone = findViewById(R.id.editTextPhone);
        edtDob = findViewById(R.id.editTextDob);
        btnSave = findViewById(R.id.buttonSave);
    }
}