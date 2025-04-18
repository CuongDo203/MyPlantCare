package com.example.myplantcare.data.repositories;

import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.utils.FirestoreCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyPlantRepositoryImpl implements MyPlantRepository{

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    public void getAllMyPlants(String userId, FirestoreCallback<List<MyPlantModel>> callback) {
        CollectionReference myPLantsRef = db.collection("users")
                .document(userId).collection("my_plants");
        myPLantsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<MyPlantModel> myPlants = new ArrayList<>();
            for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                MyPlantModel myPlant = doc.toObject(MyPlantModel.class);
                myPlant.setId(doc.getId());
                myPlants.add(myPlant);
            }

            callback.onSuccess(myPlants);
        }).addOnFailureListener(callback::onError);

    }

    @Override
    public void getMyPlantById(String myPlantId, FirestoreCallback<MyPlantModel> callback) {

    }

    @Override
    public void addMyPlant(String userId, MyPlantModel myPlant, FirestoreCallback<Void> callback) {
        DocumentReference docRef = db.collection("users")
                .document(userId)
                .collection("my_plants")
                .document(); // Tạo ID tự động

        myPlant.setId(docRef.getId()); // Set ID cho model nếu bạn dùng

        docRef.set(myPlant)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    @Override
    public void updateMyPlant(MyPlantModel myPlant, FirestoreCallback<Void> callback) {

    }

    @Override
    public void deleteMyPlant(String userId, String myPlantId, FirestoreCallback<Void> callback) {
        DocumentReference docRef = db.collection("users")
                .document(userId)
                .collection("my_plants")
                .document(myPlantId);

        docRef.delete()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }
}
