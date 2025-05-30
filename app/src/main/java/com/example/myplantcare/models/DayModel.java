package com.example.myplantcare.models;

import java.util.Calendar;

public class DayModel {
    private String dayOfWeek;
    private int dayOfMonth;
    private boolean isToday;
    private boolean isSelected;
    private Calendar calendar;

    public DayModel(String dayOfWeek, int dayOfMonth, boolean isToday, boolean isSelected, Calendar calendar) {
        this.dayOfWeek = dayOfWeek;
        this.dayOfMonth = dayOfMonth;
        this.isToday = isToday;
        this.isSelected = isSelected;
        this.calendar = calendar;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public boolean isToday() {
        return isToday;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
