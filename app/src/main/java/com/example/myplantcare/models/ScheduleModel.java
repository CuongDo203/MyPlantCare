package com.example.myplantcare.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

public class ScheduleModel {
    @DocumentId
    private String id;
    private String taskId;
    private Integer frequency;
    private Timestamp startDate;
    private Timestamp time;
    @Exclude
    private String myPlantId;

    public ScheduleModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }
    @PropertyName("start_date")
    public Timestamp getStartDate() {
        return startDate;
    }
    @PropertyName("start_date")
    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }


    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }


    public String getTaskId() {
        return taskId;
    }

    @Override
    public String toString() {
        return "ScheduleModel{" +
                "id='" + id + '\'' +
                ", taskId='" + taskId + '\'' +
                ", frequency='" + frequency + '\'' +
                ", startDate=" + startDate +
                ", time='" + time + '\'' +
                '}';
    }
}
