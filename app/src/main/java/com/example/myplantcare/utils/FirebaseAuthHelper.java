package com.example.myplantcare.utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;

public class FirebaseAuthHelper {
    private FirebaseAuth mAuth;

    public FirebaseAuthHelper() {
        mAuth = FirebaseAuth.getInstance();
    }

    // Đăng ký người dùng mới
    public void registerUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    // Đăng nhập người dùng
    public void loginUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    // Đổi mật khẩu
    public void changePassword(String newPassword, OnCompleteListener<Void> listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(listener);
        }
    }

    // Quên mật khẩu
    public void resetPassword(String email, OnCompleteListener<Void> listener) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(listener);
    }

    // Kiểm tra trạng thái người dùng đã đăng nhập hay chưa
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
}
