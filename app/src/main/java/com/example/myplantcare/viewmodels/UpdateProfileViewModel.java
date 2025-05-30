package com.example.myplantcare.viewmodels;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myplantcare.data.repositories.ImageRepository;
import com.example.myplantcare.data.repositories.ImageRepositoryImpl;
import com.example.myplantcare.data.repositories.UserRepository;
import com.example.myplantcare.data.repositories.UserRepositoryImpl;
import com.example.myplantcare.models.User;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.HashMap;
import java.util.Map;

public class UpdateProfileViewModel extends ViewModel{
    private static final String TAG = "UpdateProfileViewModel";
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private String userId;
    private final MutableLiveData<User> _userProfile = new MutableLiveData<>();
    public final LiveData<User> userProfile = _userProfile;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<Boolean> _updateSuccess = new MutableLiveData<>();
    public final LiveData<Boolean> updateSuccess = _updateSuccess;

    // LiveData để báo cho View mở Date Picker
    private final MutableLiveData<Void> _openDatePickerEvent = new MutableLiveData<>();
    public final LiveData<Void> openDatePickerEvent = _openDatePickerEvent;

    // LiveData để báo cho View mở Image Picker
    private final MutableLiveData<Void> _openImagePickerEvent = new MutableLiveData<>();
    public final LiveData<Void> openImagePickerEvent = _openImagePickerEvent;

    // LiveData để lưu tạm URI ảnh mới được chọn trong ViewModel
    private Uri _selectedImageUri;
    // LiveData để thông báo cho View cập nhật ImageView
    private final MutableLiveData<Uri> _previewImageUri = new MutableLiveData<>();
    public final LiveData<Uri> previewImageUri = _previewImageUri;

    // LiveData để báo cho View quay lại
    private final MutableLiveData<Void> _navigateBackEvent = new MutableLiveData<>();
    public final LiveData<Void> navigateBackEvent = _navigateBackEvent;

    public UpdateProfileViewModel(String userId) {
        userRepository = new UserRepositoryImpl();
        imageRepository = new ImageRepositoryImpl();
        this.userId = userId;
        loadUserProfile();
    }

    private void loadUserProfile() {
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
                // Hiển thị avatar hiện tại ngay sau khi load user
                if (user != null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    _previewImageUri.setValue(Uri.parse(user.getAvatar()));
                }
            }

            @Override
            public void onError(Exception e) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Failed to load user profile: " + e.getMessage());
                Log.e(TAG, "Error loading user profile", e);
            }
        });
    }

    public void updateProfile(String name, String phone, String dob) {
        if (userId == null) {
            _errorMessage.setValue("User not logged in.");
            return;
        }
        // Thực hiện validation cơ bản
        if (name.isEmpty()) {
            _errorMessage.setValue("Tên không được để trống.");
            return;
        }
        // Thêm validation cho phone, dob nếu cần
        _isLoading.setValue(true);

        // Tạo Map chứa các trường cần cập nhật (trừ avatar)
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("dob", dob);

        if (_selectedImageUri != null) {
            // Có ảnh mới được chọn, tải ảnh lên Cloudinary trước
            imageRepository.uploadImage(_selectedImageUri, new FirestoreCallback<String>() {
                @Override
                public void onSuccess(String cloudinaryUrl) {
                    // Sau khi tải ảnh lên Cloudinary thành công, thêm URL vào map cập nhật
                    updates.put("avatar", cloudinaryUrl);
                    // Cập nhật thông tin user vào Firestore
                    performFirestoreUpdate(updates);
                }

                @Override
                public void onError(Exception e) {
                    // Xử lý lỗi khi tải ảnh lên Cloudinary
                    _isLoading.setValue(false);
                    _errorMessage.setValue("Failed to upload avatar: " + e.getMessage());
                    Log.e(TAG, "Error uploading avatar to Cloudinary", e);
                }
            });
        } else {
            performFirestoreUpdate(updates);
        }
    }

    private void performFirestoreUpdate(Map<String, Object> updates) {
        userRepository.updateUser(userId, updates, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _isLoading.setValue(false);
                _updateSuccess.setValue(true); // Báo hiệu cập nhật thành công
                // Reset LiveData sau khi sử dụng (để tránh trigger lại khi xoay màn hình)
                _updateSuccess.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Failed to update profile: " + e.getMessage());
                Log.e(TAG, "Error updating profile in Firestore", e);
            }
        });
    }

    public void onDobClicked() {
        _openDatePickerEvent.setValue(null); // Kích hoạt sự kiện mở DatePicker
    }

    public void onDateSelected(String date) {
        // Xử lý ngày được chọn nếu cần, ví dụ: lưu tạm vào biến state trong ViewModel
    }

    public void onAvatarClicked() {
        _openImagePickerEvent.setValue(null); // Kích hoạt sự kiện mở Image Picker
    }

    public void onImageSelected(Uri imageUri) {
        _selectedImageUri = imageUri; // Lưu URI ảnh mới
        _previewImageUri.setValue(imageUri); // Cập nhật ImageView ngay lập tức để preview
    }

    public void onBackClicked() {
        _navigateBackEvent.setValue(null); // Kích hoạt sự kiện quay lại
    }


}
