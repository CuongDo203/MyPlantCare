package com.example.myplantcare.data.repositories;

import com.example.myplantcare.models.TaskModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.List;

public interface TaskRepository {
    void getAllTasks(FirestoreCallback<List<TaskModel>> callback);
}
