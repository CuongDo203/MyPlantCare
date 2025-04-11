package com.example.myplantcare.utils;

/**
 * Interface callback chung để xử lý kết quả bất đồng bộ từ Firestore
 * thông qua các lớp Repository.
 *
 * @param <T> Kiểu dữ liệu mong đợi khi thành công. Sử dụng Void nếu không có dữ liệu trả về.
 */
public interface FirestoreCallback<T> {

    /**
     * Được gọi khi thao tác Firestore thành công.
     * @param result Kết quả trả về (có thể là null nếu T là Void).
     */
    void onSuccess(T result);

    /**
     * Được gọi khi thao tác Firestore xảy ra lỗi.
     * @param e Exception xảy ra.
     */
    void onError(Exception e);
}
