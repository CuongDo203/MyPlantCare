package com.example.myplantcare.viewmodels;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myplantcare.data.repositories.UserRepository;
import com.example.myplantcare.data.repositories.UserRepositoryImpl;
import com.example.myplantcare.models.User;
import com.example.myplantcare.utils.FirestoreCallback;

public class ProfileViewModel extends ViewModel {
    private static final String TAG = "ProfileViewModel";
    private final UserRepository userRepository;
    private String userId;
    private MutableLiveData<User> _userProfile = new MutableLiveData<>();
    public LiveData<User> userProfile = _userProfile;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    public ProfileViewModel(String userId) {
        userRepository = new UserRepositoryImpl();
        this.userId = userId;
        loadUserProfile();
    }

    public void loadUserProfile() {
        if (userId == null) {
            _errorMessage.setValue("User not logged in.");
            return;
        }
        _isLoading.setValue(true);

        // Gọi Repository và cung cấp callback để xử lý kết quả
        userRepository.getUser(userId, new FirestoreCallback<User>() {
            @Override
            public void onSuccess(User user) {
                _isLoading.setValue(false);
                _userProfile.setValue(user);
                Log.d(TAG, "User profile loaded successfully");
                Log.d(TAG, "User: " + user.toString());
            }

            @Override
            public void onError(Exception e) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Failed to load user profile: " + e.getMessage());
                Log.e(TAG, "Error loading user profile", e);
            }
        });
    }
}
