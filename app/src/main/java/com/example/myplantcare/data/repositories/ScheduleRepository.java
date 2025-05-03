package com.example.myplantcare.data.repositories;

import android.util.Pair;

import com.example.myplantcare.data.responses.ScheduleWithMyPlantInfo;
import com.example.myplantcare.models.ScheduleModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public interface ScheduleRepository {
    void addSchedule(String userId, String myPlantId, ScheduleModel schedule, FirestoreCallback<Void> callback);
    void getTodaySchedulesGroupedByTask(String userId, FirestoreCallback<Map<String, List<ScheduleWithMyPlantInfo>>> callback);

    void getSchedulesByMyPlantId(String userId, String myPlantId, FirestoreCallback<List<ScheduleModel>> callback);
    void deleteSchedule(String userId, String myPlantId, String scheduleId, FirestoreCallback<Void> callback);

    void getSchedulesForDateGroupedByTask(String userId, Calendar targetDate,
                                          FirestoreCallback<Pair<Map<String, List<ScheduleWithMyPlantInfo>>,
                                                  Map<String, List<ScheduleWithMyPlantInfo>>>> callback);

    void markScheduleCompleted(String userId, String myPlantId, String scheduleId,String taskName, Calendar date, FirestoreCallback<Void> callback);
    void unmarkScheduleCompleted(String userId, String myPlantId, String scheduleId, Calendar date, FirestoreCallback<Void> callback);
}
