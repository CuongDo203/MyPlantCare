package com.example.myplantcare.data.responses;

import com.google.firebase.Timestamp;

public class ScheduleDisplayItem {
    private String scheduleId; // ID của document schedule gốc
    private String taskName;      // Tên công việc đã được lookup từ TaskModel
    private String taskId;        // Giữ lại taskId gốc (có thể cần cho việc xóa hoặc tham chiếu)
    private Integer frequency;
    private Timestamp time;          // Giờ (String)
    private Timestamp startDate;

    public ScheduleDisplayItem(String scheduleId, String taskName, String taskId, Integer frequency, Timestamp time, Timestamp startDate) {
        this.scheduleId = scheduleId;
        this.taskName = taskName;
        this.taskId = taskId;
        this.frequency = frequency;
        this.time = time;
        this.startDate = startDate;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getTaskName() {
        return taskName;
    }
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    @Override
    public String toString() {
        return "ScheduleDisplayItem{" +
                "scheduleId='" + scheduleId + '\'' +
                ", taskName='" + taskName + '\'' +
                ", taskId='" + taskId + '\'' +
                ", frequency='" + frequency + '\'' +
                ", time='" + time + '\'' +
                ", startDate=" + startDate +
                '}';
    }
}
