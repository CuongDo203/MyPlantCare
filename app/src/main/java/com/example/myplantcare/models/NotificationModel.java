package com.example.myplantcare.models;

import java.util.Date;

public class NotificationModel {

    private String title;

    private String content;

    private Date created_At;

    private String type;

    private ScheduleModel scheduleModel = null;

    public NotificationModel(String title, String content, Date created_At, String type, ScheduleModel scheduleModel)
    {
        this.title = title;
        this.content = content;
        this.created_At = created_At;
        this.type = type;
        this.scheduleModel = scheduleModel;
    }

    public NotificationModel() {

    }
    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Date getCreated_At() {
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

    public void setCreated_At(Date created_At) {
        this.created_At = created_At;
    }

    public void setType(String type) {
        this.type = type;
    }


}
