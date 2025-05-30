package com.example.myplantcare.models;

import java.util.Date;

public class Notification {

    private String title;
    private String count;
    private String content;
    private String created_At;

    private String type;

    private ScheduleModel scheduleModel = null;

    public Notification(String title, String content, String created_At, String type, ScheduleModel scheduleModel)
    {
        this.title = title;
        this.content = content;
        this.created_At = created_At;
        this.type = type;
        this.scheduleModel = scheduleModel;
    }

    public Notification(String message,String time,String count) {
        content = message;
        created_At = time;
        this.count = count;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCreated_At() {
        return created_At;
    }
    public String getType() {
        return type;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public void setCreated_At(String created_At) {
        this.created_At = created_At;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getCount() {
        return count;
    }
    public void setCount(String count) {
        this.count = count;
    }

}
