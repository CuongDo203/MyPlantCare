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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnLogin;
    private EditText etUsername, etPassword;
    private TextView txtRegisterNow;
    private ImageView ivTogglePassword;
    private boolean isPasswordVisible = false;

    private TextView btnForgetPassword;
    // Firebase Authentication instance
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
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
        db = FirebaseFirestore.getInstance();
        // Lấy intent được gửi sang
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");
        boolean isNewUser = intent.getBooleanExtra("isNewUser", false);
        // Kiểm tra xem có cần gọi hàm không
        if (isNewUser) {
            loginUser(email, password, isNewUser);
        }
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
            loginUser(etUsername.getText().toString(), etPassword.getText().toString(), false);  // Gọi hàm đăng nhập
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
    private void loginUser(String email, String password, boolean newUser) {
        String trimmedEmail = email.trim();
        String trimmedPassword = password.trim();

        if (trimmedEmail.isEmpty() || trimmedPassword.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Vui lòng nhập tên người dùng và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Dùng trimmedEmail trong các bước tiếp theo:
        mAuth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (newUser) {
                                String uid = user.getUid();
                                Map<String, Object> userInfo = new HashMap<>();
                                userInfo.put("name", "Người dùng");
                                userInfo.put("email", trimmedEmail);
                                userInfo.put("phone", "");
                                userInfo.put("dob", "");
                                userInfo.put("role", "");
                                userInfo.put("cityId", "");
                                userInfo.put("avatar", "");

                                db.collection("users").document(uid)
                                        .set(userInfo)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(LoginActivity.this, "Lưu thông tin người dùng thành công", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(LoginActivity.this, "Không thể lưu thông tin người dùng", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                // Tài khoản cũ → chuyển tiếp luôn
                                Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Sai tên người dùng hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                });
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