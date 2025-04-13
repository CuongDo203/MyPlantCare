package com.example.myplantcare.models;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;

public class TaskLogModel {

    @DocumentId
    private String id;
    private String scheduleId;
    private Date date;
    private String taskName;
    private boolean status;
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

    public TaskLogModel(Date date, String taskName, boolean status, String userPhotoUrl) {
        this.date = date;
        this.taskName = taskName;
        this.status = status;
        this.userPhotoUrl = userPhotoUrl;
    }

    public TaskLogModel(){}

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
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
