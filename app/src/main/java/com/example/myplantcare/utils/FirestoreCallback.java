package com.example.myplantcare.utils;

/**
 * Interface callback chung để xử lý kết quả bất đồng bộ từ Firestore
 * thông qua các lớp Repository.
 */
public interface FirestoreCallback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}
