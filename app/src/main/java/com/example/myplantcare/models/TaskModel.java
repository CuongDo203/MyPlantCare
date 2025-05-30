package com.example.myplantcare.models;

import com.google.firebase.firestore.DocumentId;

public class TaskModel {
    @DocumentId
    private String id;

    private String name;

    private String description;

    public TaskModel() {}

    public TaskModel(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
