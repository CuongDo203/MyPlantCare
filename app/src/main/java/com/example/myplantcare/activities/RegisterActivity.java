package com.example.myplantcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnRegister;
    private EditText etEmail, etPassword, etConfirmPassword;
    private CheckBox cbAgree;
    TextView txtLoginNow;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private boolean isAgreeChecked = false;
    private ImageView ivTogglePassword, ivToggleConfirmPassword;

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
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnRegister) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
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

//    private void togglePasswordVisibility(boolean isPassword) {
//        if (isPassword) {
//            if (isPasswordVisible) {
//                // Ẩn mật khẩu
//                etPassword.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
//                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//                ivTogglePassword.setImageResource(R.drawable.splash_eye); // Icon mắt đóng
//
//            } else {
//                // Hiển thị mật khẩu
//                etPassword.setTransformationMethod(null);
//                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//                ivTogglePassword.setImageResource(R.drawable.mdi_eye);
//
//            }
//            // Giữ con trỏ ở cuối văn bản
//            etPassword.setSelection(etPassword.getText().length());
//            // Đảo trạng thái hiển thị mật khẩu
//            isPasswordVisible = !isPasswordVisible;
//        }
//        else {
//            if (isConfirmPasswordVisible) {
//                // Ẩn mật khẩu
//                etConfirmPassword.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
//                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//                ivToggleConfirmPassword.setImageResource(R.drawable.splash_eye); //
//            } else {
//                // Hiển thị mật khẩu
//                etConfirmPassword.setTransformationMethod(null);
//                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//                ivToggleConfirmPassword.setImageResource(R.drawable.mdi_eye);
//            }
//            // Giữ con trỏ ở cuối văn bản
//            etConfirmPassword.setSelection(etConfirmPassword.getText().length());
//            // Đảo trạng thái hiển thị mật khẩu
//            isConfirmPasswordVisible = !isConfirmPasswordVisible;
//        }
//
//    }

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

}