package com.example.myplantcare.models;

public class DayModel {
    private String dayOfWeek;
    private int day;
    private boolean isToday;

    public DayModel(String dayOfWeek, int day, boolean isToday) {
        this.dayOfWeek = dayOfWeek;
        this.day = day;
        this.isToday = isToday;
    }

    public String getDayOfWeek() { return dayOfWeek; }
    public int getDay() { return day; }
    public boolean isToday() { return isToday; }
}
