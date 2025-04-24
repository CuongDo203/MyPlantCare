package com.example.myplantcare.data.responses;

import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.models.ScheduleModel;
import com.example.myplantcare.models.TaskModel;
import com.google.firebase.Timestamp;

public class ScheduleWithMyPlantInfo {

    private ScheduleModel schedule;
    private MyPlantModel myPlant;

    private boolean completedOnDate;
    private String taskName;

    private transient boolean isCheckedForGroupAction = false; // transient means it's not saved to Firestore

    public ScheduleWithMyPlantInfo(ScheduleModel schedule, MyPlantModel myPlant) {
        this.schedule = schedule;
        this.myPlant = myPlant;
    }

    public ScheduleModel getSchedule() {
        return schedule;
    }

    public MyPlantModel getMyPlant() { // Đổi tên getter
        return myPlant;
    }

    // Các getter tiện ích (ví dụ)
    public String getMyPlantNickname() {
        return (myPlant != null) ? myPlant.getNickname() : "N/A";
    }

    public String getMyPlantLocation() {
        return (myPlant != null) ? myPlant.getLocation() : "N/A";
    }

    public String getMyPlantImageUrl() {
        return (myPlant != null) ? myPlant.getImage() : null;
    }

    public Timestamp getScheduleTime() {
        return (schedule != null) ? schedule.getTime() : null;
    }

    public boolean isCompletedOnDate() {
        return completedOnDate;
    }

    public boolean isCheckedForGroupAction() {
        return isCheckedForGroupAction;
    }

    public void setCheckedForGroupAction(boolean checkedForGroupAction) {
        isCheckedForGroupAction = checkedForGroupAction;
    }
    public void setCompletedOnDate(boolean completedOnDate) {
        this.completedOnDate = completedOnDate;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}
