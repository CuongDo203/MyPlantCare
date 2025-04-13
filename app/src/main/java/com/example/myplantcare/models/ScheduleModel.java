package com.example.myplantcare.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

import java.sql.Time;
import java.util.Date;
import java.util.List;

public class ScheduleModel {
    @DocumentId
    private String id;
    private String taskId;
    private String frequency;
    private Date startDate;
    private TaskModel task;
    private String time;

    public ScheduleModel(String taskName) {
        this.taskId = taskName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
    @PropertyName("start_date")
    public Date getStartDate() {
        return startDate;
    }
    @PropertyName("start_date")
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    @PropertyName("taskId")
    public TaskModel getTask() {
        return task;
    }
    @PropertyName("taskId")
    public void setTask(TaskModel task) {
        this.task = task;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }


    public String getTaskId() {
        return taskId;
    }

}
