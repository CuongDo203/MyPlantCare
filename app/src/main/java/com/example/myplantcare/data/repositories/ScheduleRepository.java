package com.example.myplantcare.data.repositories;

import com.example.myplantcare.data.responses.ScheduleWithMyPlantInfo;
import com.example.myplantcare.models.ScheduleModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.List;
import java.util.Map;

public interface ScheduleRepository {

    void getTodaySchedulesGroupedByTask(String userId, FirestoreCallback<Map<String, List<ScheduleWithMyPlantInfo>>> callback);
    void getSchedulesByMyPlantId(String userId, String myPlantId, FirestoreCallback<List<ScheduleModel>> callback);
    void deleteSchedule(String userId, String myPlantId, String scheduleId, FirestoreCallback<Void> callback);
}
