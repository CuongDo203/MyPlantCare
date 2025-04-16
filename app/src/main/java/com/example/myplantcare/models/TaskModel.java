package com.example.myplantcare.models;

import com.google.firebase.firestore.DocumentId;

public class TaskModel {
    @DocumentId
    private String id;

    private String name;

    private boolean description;

    public TaskModel() {}

    public TaskModel(String name, boolean description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public boolean isDescription() {
        return description;
    }

}
