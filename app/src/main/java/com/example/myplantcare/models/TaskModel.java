package com.example.myplantcare.models;

public class TaskModel {

    private String details;
    private boolean isCompleted;

    public TaskModel(String details, boolean isCompleted) {
        this.details = details;
        this.isCompleted = isCompleted;
    }

    public String getDetails() {
        return details;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

}
