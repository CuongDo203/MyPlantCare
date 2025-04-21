package com.example.myplantcare.data.repositories;

import android.util.Log;

import com.example.myplantcare.models.TaskModel;
import com.example.myplantcare.utils.Constants;
import com.example.myplantcare.utils.FirestoreCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TaskRepositoryImpl implements TaskRepository{
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference tasksCollection = db.collection(Constants.TASKS_COLLECTION);
    @Override
    public void getAllTasks(FirestoreCallback<List<TaskModel>> callback) {
        tasksCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<TaskModel> tasks = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                TaskModel task = doc.toObject(TaskModel.class);
                task.setId(doc.getId()); // Set ID document nếu ID task là document ID
                tasks.add(task);
            }
            Log.d("TaskRepositoryImpl", "Fetched " + tasks.size() + " tasks.");
            callback.onSuccess(tasks);
        }).addOnFailureListener(e -> {
            Log.e("TaskRepositoryImpl", "Error fetching tasks", e);
            callback.onError(e);
        });
    }
}
