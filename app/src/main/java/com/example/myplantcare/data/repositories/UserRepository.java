package com.example.myplantcare.data.repositories;

import com.example.myplantcare.models.User;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.Map;

public interface UserRepository {
    void getUser(String userId, FirestoreCallback<User> callback);
    void updateUser(String userId, Map<String, Object> updates, FirestoreCallback<Void> callback);

}
