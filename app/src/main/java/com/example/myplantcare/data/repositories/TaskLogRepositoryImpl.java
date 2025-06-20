package com.example.myplantcare.data.repositories;

import android.net.Uri;
import android.util.Log;

import com.example.myplantcare.models.TaskLogModel;
import com.example.myplantcare.utils.Constants;
import com.example.myplantcare.utils.FirestoreCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TaskLogRepositoryImpl implements TaskLogRepository{
    private static final String TAG = "TaskLogRepositoryImpl";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    public void loadTaskLog(String userId, String myPlantId, FirestoreCallback<List<TaskLogModel>> callback) {
        if (userId == null || userId.isEmpty() || myPlantId == null || myPlantId.isEmpty()) {
            Log.w(TAG, "Cannot load task logs: userId or plantId is null/empty.");
            return;
        }

        CollectionReference taskLogsRef = db.collection(Constants.USERS_COLLECTION);
        taskLogsRef.document(userId)
                .collection(Constants.MY_PLANTS_COLLECTION)
                .document(myPlantId)
                .collection(Constants.TASK_LOGS_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TaskLogModel> taskLogList = new ArrayList<>();
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                TaskLogModel taskLog = document.toObject(TaskLogModel.class);
                                taskLog.setId(document.getId());
                                taskLogList.add(taskLog);
                                Log.d(TAG, "Loaded log document: " + document.getId() + " -> TaskName: " + taskLog.getTaskName() + ", Date: " + taskLog.getDate());

                            } catch (Exception e) {
                                Log.e(TAG, "Error converting document to TaskLogModel: " + document.getId(), e);
                            }
                        }
                        Log.d(TAG, "Successfully loaded " + taskLogList.size() + " task logs for plantId: " + myPlantId);
                        callback.onSuccess(taskLogList);

                    } else {
                        // Không có document nào được tìm thấy
                        Log.d(TAG, "No task logs found for plantId: " + myPlantId);
                        // Trả về danh sách rỗng thông qua callback onSuccess
                        callback.onSuccess(new ArrayList<>()); // Trả về một danh sách rỗng
                    }
                }).addOnFailureListener(e -> {
                    // --- Xử lý khi truy vấn thất bại ---
                    Log.e(TAG, "Error loading task logs for plantId: " + myPlantId, e);
                    // Trả về lỗi thông qua callback onError
                    callback.onError(e);
                });;
    }

    @Override
    public void uploadTaskLogImage(String userId, String myPlantId, String taskLogId, String imageUri, FirestoreCallback<Void> callback) {
        if (userId == null || userId.isEmpty() || myPlantId == null || myPlantId.isEmpty() || taskLogId == null || taskLogId.isEmpty() || imageUri == null) {
            Log.w(TAG, "uploadTaskLogImageAndUrl: invalid input.");
            if (callback != null) {
                callback.onError(new IllegalArgumentException("Required parameters must not be null/empty."));
            }
            return;
        }
        if (callback == null) {
            Log.w(TAG, "uploadTaskLogImageAndUrl: callback is null. Cannot return result.");
            return;
        }

        DocumentReference taskLogRef = db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .collection(Constants.MY_PLANTS_COLLECTION)
                .document(myPlantId)
                .collection(Constants.TASK_LOGS_COLLECTION)
                .document(taskLogId);
        taskLogRef.update("userPhotoUrl", imageUri.toString())
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Task log image uploaded successfully for task log ID: " + taskLogId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error uploading task log image for task log ID: " + taskLogId, e);
                    callback.onError(e);
                });
    }
}
