package com.example.myplantcare.models;
public class MyNotification {
    private Notification notificationModel;
    private boolean isRead;
    private User user;


    public MyNotification() {
    }

    public MyNotification(Notification notificationModel, User user, boolean isRead) {
        this.notificationModel = notificationModel;
        this.user = user;
        this.isRead = isRead;
    }
    public Notification getNotificationModel() {
        return notificationModel;
    }

    public void setNotificationModel(Notification notificationModel) {
        this.notificationModel = notificationModel;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
