package com.example.myplantcare.models;

public class ScheduleModel {
    private String title;
    private String time;
    private boolean isCompleted;

    public ScheduleModel(String title, String time, boolean isCompleted) {
        this.title = title;
        this.time = time;
        this.isCompleted = isCompleted;
    }

    public String getTitle() { return title; }
    public String getTime() { return time; }
    public boolean isCompleted() { return isCompleted; }
}
