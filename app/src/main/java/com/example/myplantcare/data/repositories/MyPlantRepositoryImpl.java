package com.example.myplantcare.data.repositories;

import android.util.Log;

import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.utils.Constants;
import com.example.myplantcare.utils.FirestoreCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyPlantRepositoryImpl implements MyPlantRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public MyPlantRepositoryImpl() {
    }

    @Override
    public void getAllMyPlants(String userId, FirestoreCallback<List<MyPlantModel>> callback) {
        CollectionReference myPLantsRef = db.collection(Constants.USERS_COLLECTION)
                .document(userId).collection(Constants.MY_PLANTS_COLLECTION);
        myPLantsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<MyPlantModel> myPlants = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                MyPlantModel myPlant = doc.toObject(MyPlantModel.class);
                myPlant.setId(doc.getId());
                myPlants.add(myPlant);
            }

            callback.onSuccess(myPlants);
        }).addOnFailureListener(callback::onError);

    }

    @Override
    public void getMyPlantById(String userId, String myPlantId, FirestoreCallback<MyPlantModel> callback) {
        if (userId == null || myPlantId == null) {
            Log.w("MyPlantRepository", "getMyPlantById: userId or myPlantId is null");
            callback.onError(new IllegalArgumentException("userId and myPlantId must not be null"));
            return;
        }
        DocumentReference docRef = db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .collection(Constants.MY_PLANTS_COLLECTION)
                .document(myPlantId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                MyPlantModel myPlant = documentSnapshot.toObject(MyPlantModel.class);
                if (myPlant != null) {
                    myPlant.setId(documentSnapshot.getId()); // Set ID
                    Log.d("MyPlantRepository", "Fetched plant details for ID: " + myPlantId);
                    callback.onSuccess(myPlant);
                } else {
                    Log.e("MyPlantRepository", "Failed to convert document to MyPlantModel for ID: " + myPlantId);
                    callback.onError(new Exception("Failed to parse plant data"));
                }
            } else {
                Log.w("MyPlantRepository", "Plant document not found for ID: " + myPlantId);
                callback.onSuccess(null); // Trả về null nếu không tìm thấy document
            }
        }).addOnFailureListener(e -> {
            Log.e("MyPlantRepository", "Error fetching plant details for ID: " + myPlantId, e);
            callback.onError(e);
        });
    }

    @Override
    public void addMyPlant(String userId, MyPlantModel myPlant, FirestoreCallback<Void> callback) {
        DocumentReference docRef = db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .collection(Constants.MY_PLANTS_COLLECTION)
                .document(); // Tạo ID tự động

        myPlant.setId(docRef.getId()); // Set ID cho model nếu bạn dùng

        docRef.set(myPlant)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    @Override
    public void updateMyPlant(String userId, String myPlantId, Map<String, Object> updates, FirestoreCallback<Void> callback) {
        if (userId == null || myPlantId == null || updates == null || updates.isEmpty()) {
            Log.w("MyPlantRepository", "updateMyPlant: userId, myPlantId, or updates is null/empty");
            callback.onError(new IllegalArgumentException("userId, myPlantId, and updates must not be null/empty"));
            return;
        }

        DocumentReference docRef = db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .collection(Constants.MY_PLANTS_COLLECTION)
                .document(myPlantId);

        updates.put("updated_at", FieldValue.serverTimestamp());

        docRef.update(updates)
                .addOnSuccessListener(unused -> {
                    Log.d("MyPlantRepository", "Plant updated successfully for ID: " + myPlantId + " for user: " + userId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("MyPlantRepository", "Error updating plant for ID: " + myPlantId + " for user: " + userId, e);
                    callback.onError(e);
                });
    }

    @Override
    public void deleteMyPlant(String userId, String myPlantId, FirestoreCallback<Void> callback) {
        DocumentReference docRef = db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .collection(Constants.MY_PLANTS_COLLECTION)
                .document(myPlantId);

        docRef.delete()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }
}
