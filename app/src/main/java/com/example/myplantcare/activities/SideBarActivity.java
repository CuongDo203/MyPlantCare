package com.example.myplantcare.activities;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myplantcare.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// SideBarActivity.java
public class SideBarActivity extends AppCompatActivity {
    ImageView ivReturn;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.side_bar);
        ivReturn = findViewById(R.id.ivReturn);
        ivReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); // Optional: hiệu ứng chuyển cảnh
            }
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

