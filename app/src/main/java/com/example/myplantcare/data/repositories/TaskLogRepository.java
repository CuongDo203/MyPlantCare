package com.example.myplantcare.data.repositories;

import com.example.myplantcare.models.TaskLogModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.List;

public interface TaskLogRepository {

    void loadTaskLog(String userId, String myPlantId, FirestoreCallback<List<TaskLogModel>> callback);

}
