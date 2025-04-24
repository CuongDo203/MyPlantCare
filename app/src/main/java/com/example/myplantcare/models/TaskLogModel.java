package com.example.myplantcare.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class TaskLogModel {
    @DocumentId
    private String id;
    private String scheduleId;
    private Timestamp date;
    private String taskName;
    private boolean status;
    @ServerTimestamp
    private Timestamp createdAt;
    @Exclude
    private String userPhotoUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public TaskLogModel(String scheduleId, Timestamp date, boolean status) {
        this.date = date;
        this.scheduleId = scheduleId;
        this.status = status;
    }

    public TaskLogModel(){}

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }

    public void setUserPhotoUrl(String userPhotoUrl) {
        this.userPhotoUrl = userPhotoUrl;
    }
}
