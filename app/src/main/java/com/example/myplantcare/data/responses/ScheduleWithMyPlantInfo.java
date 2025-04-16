package com.example.myplantcare.data.responses;

import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.models.ScheduleModel;
import com.example.myplantcare.models.TaskModel;
import com.google.firebase.Timestamp;

public class ScheduleWithMyPlantInfo {

    private ScheduleModel schedule;
    private MyPlantModel myPlant;

    private TaskModel task;

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
}
