package com.example.myplantcare.data.repositories;

import android.util.Log;

import com.example.myplantcare.models.User;
import com.example.myplantcare.utils.Constants;
import com.example.myplantcare.utils.FirestoreCallback;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class UserRepositoryImpl implements UserRepository{

    private static final String TAG = "UserRepository";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void getUser(String userId, FirestoreCallback<User> callback) {
        if(userId == null) {
            callback.onError(new IllegalArgumentException("userId must not be null"));
            return;
        }
        db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            callback.onSuccess(user);
                        } else {
                            callback.onError(new Exception("Failed to parse user data"));
                        }
                    } else {
                        callback.onError(new Exception("User document not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user: " + e.getMessage());
                    callback.onError(e);
                });
    }

    @Override
    public void updateUser(String userId, Map<String, Object> updates, FirestoreCallback<Void> callback) {
        if(userId == null) {
            callback.onError(new IllegalArgumentException("userId must not be null"));
            return;
        }
        db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "User updated successfully for ID: " + userId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user for ID: "+ userId, e);
                    callback.onError(e);
                });
    }
}
