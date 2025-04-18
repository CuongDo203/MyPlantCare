package com.example.myplantcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myplantcare.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnLogin;
    private EditText etUsername, etPassword;
    private TextView txtRegisterNow;
    private ImageView ivTogglePassword;
    private boolean isPasswordVisible = false;

    private TextView btnForgetPassword;
    // Firebase Authentication instance
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        initContents();
        btnLogin.setOnClickListener(this);
        txtRegisterNow.setOnClickListener(this);
        ivTogglePassword.setOnClickListener(this);
        btnForgetPassword.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
    }

    private void initContents() {
        btnLogin = findViewById(R.id.btnLogin);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPasswordLogin);
        txtRegisterNow = findViewById(R.id.register_now);
        ivTogglePassword = findViewById(R.id.ivTogglePasswordLogin);
        btnForgetPassword = findViewById(R.id.btnForgotPassword);
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Ẩn mật khẩu
            etPassword.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivTogglePassword.setImageResource(R.drawable.splash_eye); // Icon mắt đóng
        } else {
            // Hiển thị mật khẩu
            etPassword.setTransformationMethod(null);
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivTogglePassword.setImageResource(R.drawable.mdi_eye); // Icon mắt mở
        }

        // Giữ con trỏ ở cuối văn bản
        etPassword.setSelection(etPassword.getText().length());

        // Đảo trạng thái hiển thị mật khẩu
        isPasswordVisible = !isPasswordVisible;
    }



    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnLogin) {
            loginUser();  // Gọi hàm đăng nhập
        }
        else if(v.getId() == R.id.register_now){
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
        else if (v.getId() == R.id.ivTogglePasswordLogin) {

            togglePasswordVisibility();
        }
        else if(v.getId() == R.id.btnForgotPassword)
        {
            showForgotPasswordDialog();
        }
    }

    // hiển thị dialog quên mật khẩu
    private void showForgotPasswordDialog() {
        // Tạo AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Thiết lập layout của dialog
        final View dialogView = getLayoutInflater().inflate(R.layout.forget_password, null);  // Tạo view từ layout

        builder.setView(dialogView)
                .setCancelable(true) // Cho phép đóng dialog khi chạm ra ngoài
                .setTitle("Quên mật khẩu");

        // Lấy các thành phần trong dialog từ dialogView
        final EditText etEmail = dialogView.findViewById(R.id.etEmail);  // Lấy ID từ dialogView
        Button btnResetPassword = dialogView.findViewById(R.id.btnResetPassword);  // Lấy ID từ dialogView

        // Tạo đối tượng AlertDialog
        final AlertDialog dialog = builder.create();

        // Khi nhấn nút "Xác nhận"
        btnResetPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Vui lòng nhập email của bạn", Toast.LENGTH_SHORT).show();
            } else {
                // Gửi yêu cầu khôi phục mật khẩu
                resetPassword(email);

                // Đóng dialog sau khi nhấn xác nhận
                dialog.dismiss();  // Đóng dialog
            }
        });

        // Hiển thị dialog
        dialog.show();
    }


    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Vui lòng nhập tên người dùng và mật khẩu", Toast.LENGTH_SHORT).show();
        } else {
            // Firebase login process
            mAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Đăng nhập thành công
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);

                            finish();  // Kết thúc LoginActivity
                        } else {
                            // Thông báo lỗi đăng nhập
                            Toast.makeText(LoginActivity.this, "Sai tên người dùng hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    // Hàm reset mật khẩu
    private void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Email khôi phục mật khẩu đã được gửi", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Lỗi: Không thể gửi email khôi phục mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}