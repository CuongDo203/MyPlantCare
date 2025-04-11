package com.example.myplantcare.models;

import java.util.List;

public class ScheduleModel {
    private String taskName;
    private String taskType;
    private List<TaskModel> tasks;

    public ScheduleModel(String taskName, List<TaskModel> tasks, String taskType) {
        this.taskName = taskName;
        this.tasks = tasks;
        this.taskType = taskType;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public void setTasks(List<TaskModel> tasks) {
        this.tasks = tasks;
    }

    public String getTaskName() {
        return taskName;
    }

    public List<TaskModel> getTasks() {
        return tasks;
    }
}
