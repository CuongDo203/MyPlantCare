package com.example.myplantcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myplantcare.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnRegister;
    private EditText etEmail, etPassword, etConfirmPassword;
    private CheckBox cbAgree;
    TextView txtLoginNow;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private boolean isAgreeChecked = false;
    private ImageView ivTogglePassword, ivToggleConfirmPassword;


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        initContents();
        btnRegister.setOnClickListener(this);
        txtLoginNow.setOnClickListener(this);
    }

    private void initContents() {
        btnRegister = findViewById(R.id.btnRegister);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        cbAgree = findViewById(R.id.cbAgree);
        txtLoginNow = findViewById(R.id.login_now);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        ivToggleConfirmPassword = findViewById(R.id.ivToggleConfirmPassword);
        ivTogglePassword.setOnClickListener(this);
        ivToggleConfirmPassword.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnRegister) {
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
            registerUser();
        }
        else if(v.getId() == R.id.login_now) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        else if (v.getId() == R.id.ivTogglePassword) {
            togglePasswordVisibility(true);
        }
        else if (v.getId() == R.id.ivToggleConfirmPassword) {
            togglePasswordVisibility(false);
        }
    }

    private void togglePasswordVisibility(boolean isPassword) {
        if (isPassword) {
            if (isPasswordVisible) {
                // Ẩn mật khẩu
                etPassword.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
                ivTogglePassword.setImageResource(R.drawable.splash_eye);
            } else {
                // Hiển thị mật khẩu
                etPassword.setTransformationMethod(null);
                ivTogglePassword.setImageResource(R.drawable.mdi_eye);
            }
            etPassword.setSelection(etPassword.getText().length());
            isPasswordVisible = !isPasswordVisible;
        } else {
            if (isConfirmPasswordVisible) {
                etConfirmPassword.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
                ivToggleConfirmPassword.setImageResource(R.drawable.splash_eye);
            } else {
                etConfirmPassword.setTransformationMethod(null);
                ivToggleConfirmPassword.setImageResource(R.drawable.mdi_eye);
            }
            etConfirmPassword.setSelection(etConfirmPassword.getText().length());
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
        }
    }
    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Vui lòng nhập email hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Vui lòng xác nhận mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbAgree.isChecked()) {
            Toast.makeText(this, "Bạn phải đồng ý với điều khoản sử dụng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đăng ký user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công. Hãy đăng nhập để hoàn tất.", Toast.LENGTH_SHORT).show();

                        // Chuyển sang LoginActivity kèm email & password
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra("email", email);
                        intent.putExtra("password", password);
                        intent.putExtra("isNewUser", true);  // flag để biết cần lưu
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}