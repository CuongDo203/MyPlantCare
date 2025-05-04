package com.example.myplantcare.activities;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myplantcare.R;
import com.example.myplantcare.viewmodels.ProfileViewModel;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SideBarActivity extends AppCompatActivity {
    ImageView ivReturn;
    LinearLayout itemPlants;
    Button btnUpdateProfile;
    private ImageView avatar;
    private TextView name, birth;
    private String userId;
    private ProfileViewModel profileViewModel;
    private ActivityResultLauncher<Intent> updateProfileLauncher;

    private FirebaseAuth mAuth;
    public static final int RESULT_CODE_NAV_TO_PLANTS = 201;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        setContentView(R.layout.side_bar);
        profileViewModel = new ProfileViewModel(userId);
        // Nút return dùng lại id ivMenu
        ivReturn = findViewById(R.id.ivReturn);
        itemPlants = findViewById(R.id.itemPlants);
        btnUpdateProfile = findViewById(R.id.btnEdit);
        avatar = findViewById(R.id.ivAvatar);
        name = findViewById(R.id.tvName);
        birth = findViewById(R.id.tvBirth);

        updateProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (profileViewModel != null) {
                            profileViewModel.loadUserProfile(); // <<< Call reload method
                        }
                        Toast.makeText(this, "Thông tin cá nhân đã được làm mới.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        ivReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Đóng SideBarActivity và quay về MainActivity
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); // Optional: hiệu ứng chuyển cảnh
            }
        });

        btnUpdateProfile.setOnClickListener(v -> {
            Intent intent = new Intent(SideBarActivity.this, UpdateProfileActivity.class);
            updateProfileLauncher.launch(intent);
        });

        // xử lý đăng xuất
        LinearLayout btnlogout = findViewById(R.id.itemLogout);
        if(btnlogout != null)
        {
            btnlogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLogoutConfirmationDialog();
                }
            });
        }
        else {
            Toast.makeText(SideBarActivity.this, "Null", Toast.LENGTH_SHORT).show();
        }

        // xử lý đổi mật khẩu.
        LinearLayout btnChangePassword = findViewById(R.id.itemChangePassword);
        if(btnChangePassword != null)
        {
            btnChangePassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChangePasswordDialog();
                }
            });
        }
        else {
            Toast.makeText(SideBarActivity.this, "Null", Toast.LENGTH_SHORT).show();
        }

        itemPlants.setOnClickListener(v -> {
            Intent intent = new Intent();
            setResult(RESULT_CODE_NAV_TO_PLANTS, intent);

            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        observeViewModel();
    }

    private void observeViewModel() {
        profileViewModel.userProfile.observe(this, userProfile -> {
            if(userProfile != null) {
                name.setText(userProfile.getName());
                birth.setText(userProfile.getDob());
                Glide.with(this)
                        .load(userProfile.getAvatar())
                        .circleCrop()
                        .placeholder(R.drawable.ic_avatar)
                        .error(R.drawable.ic_photo_error)
                        .into(avatar);
            }
        });
    }

    public void logoutUser() {

        mAuth.signOut();  // Đăng xuất khỏi Firebase Authentication

        // Thông báo đăng xuất thành công
        Toast.makeText(SideBarActivity.this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();

        // Chuyển hướng về màn hình đăng nhập
        Intent intent = new Intent(SideBarActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void changePassword(String currentPassword, String newPassword) {
        // Lấy thông tin người dùng hiện tại
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Để thay đổi mật khẩu, bạn cần phải xác thực mật khẩu cũ
            // Đầu tiên, bạn phải tái đăng nhập người dùng bằng mật khẩu cũ

            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

            user.reauthenticate(credential)  // Tái đăng nhập người dùng bằng mật khẩu cũ
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Nếu tái đăng nhập thành công, tiến hành thay đổi mật khẩu
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            // Đổi mật khẩu thành công
                                            Toast.makeText(SideBarActivity.this, "Mật khẩu đã được thay đổi!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // Xử lý khi có lỗi trong quá trình thay đổi mật khẩu
                                            Toast.makeText(SideBarActivity.this, "Lỗi khi thay đổi mật khẩu.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // Xử lý khi tái đăng nhập không thành công
                            Toast.makeText(SideBarActivity.this, "Mật khẩu cũ không chính xác.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(SideBarActivity.this, "Người dùng chưa đăng nhập.", Toast.LENGTH_SHORT).show();
        }
    }
    private void showChangePasswordDialog() {
        // Tạo và xây dựng dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(SideBarActivity.this);

        // Inflater layout vào dialog
        View dialogView = getLayoutInflater().inflate(R.layout.change_password_dialog, null);
        builder.setView(dialogView);

        // Lấy các view trong dialog
        EditText etCurrentPassword = dialogView.findViewById(R.id.CurrentPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.NewPassword);
        Button btnChangePassword = dialogView.findViewById(R.id.btnChangePassword);
        AlertDialog dialog = builder.create();

        // Thiết lập hành động cho nút thay đổi mật khẩu
        btnChangePassword.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();

            if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(SideBarActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            } else {
                // Thực hiện thay đổi mật khẩu
                changePassword(currentPassword, newPassword);
                dialog.dismiss();
            }
        });

        // Hiển thị dialog
        dialog.show();
    }
    private void showLogoutConfirmationDialog() {
        // Tạo AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(SideBarActivity.this);

        // Inflater layout vào dialog
        View dialogView = getLayoutInflater().inflate(R.layout.logout_confirmation_dialog, null);
        builder.setView(dialogView);

        // Lấy các view trong dialog
        Button btnYes = dialogView.findViewById(R.id.btnYes);
        Button btnNo = dialogView.findViewById(R.id.btnNo);
        AlertDialog dialog = builder.create();

        // Hiển thị dialog
        dialog.show();

        // Khi nhấn "Yes", thực hiện đăng xuất
        btnYes.setOnClickListener(v -> {
            logoutUser();
            Toast.makeText(SideBarActivity.this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // Khi nhấn "No", đóng dialog
        btnNo.setOnClickListener(v -> {
            Toast.makeText(SideBarActivity.this, "Đăng xuất bị hủy", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

    }
}

