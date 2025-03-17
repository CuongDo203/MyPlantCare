package com.example.myplantcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myplantcare.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnLogin;
    private EditText etUsername, etPassword;
    private TextView txtRegisterNow;
    private ImageView ivTogglePassword;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        initContents();
        btnLogin.setOnClickListener(this);
        txtRegisterNow.setOnClickListener(this);
        ivTogglePassword.setOnClickListener(this);
    }

    private void initContents() {
        btnLogin = findViewById(R.id.btnLogin);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPasswordLogin);
        txtRegisterNow = findViewById(R.id.register_now);
        ivTogglePassword = findViewById(R.id.ivTogglePasswordLogin);
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
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.register_now){
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
        else if (v.getId() == R.id.ivTogglePasswordLogin) {

            togglePasswordVisibility();

        }
    }
}