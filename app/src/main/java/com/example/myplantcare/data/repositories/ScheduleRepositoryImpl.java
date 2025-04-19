package com.example.myplantcare.data.repositories;

import android.util.Log;

import com.example.myplantcare.data.responses.ScheduleWithMyPlantInfo;
import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.models.ScheduleModel;
import com.example.myplantcare.utils.Constants;
import com.example.myplantcare.utils.FirestoreCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleRepositoryImpl implements ScheduleRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection(Constants.USERS_COLLECTION);
    private CollectionReference schedulesRef = db.collection(Constants.SCHEDULES_COLLECTION);
    private CollectionReference tasksRef = db.collection(Constants.TASKS_COLLECTION);
    private CollectionReference myPlantsRef;

    @Override
    public void getTodaySchedulesGroupedByTask(String userId, FirestoreCallback<Map<String, List<ScheduleWithMyPlantInfo>>> callback) { // Cập nhật kiểu Callback
        CollectionReference myPlantsRef = db.collection(Constants.USERS_COLLECTION).document(userId).collection(Constants.MY_PLANTS_COLLECTION);

        // Map trung gian sử dụng kiểu Wrapper mới
        Map<String, List<ScheduleWithMyPlantInfo>> groupedScheduleMyPlantInfo = new HashMap<>();

        Log.d("ScheduleRepository", "Fetching myPlants for user: " + userId);

        myPlantsRef.get().addOnSuccessListener(plantSnapshots -> {
            Log.d("ScheduleRepository", "Fetched myPlants snapshot. Is empty: " + plantSnapshots.isEmpty());
            if (plantSnapshots.isEmpty()) {
                Log.d("ScheduleRepository", "No myPlants found for user.");
                callback.onSuccess(new HashMap<>());
                return;
            }

            List<Task<Void>> allProcessingTasks = new ArrayList<>();
            Log.d("ScheduleRepository", "Found " + plantSnapshots.size() + " myPlants.");

            for (DocumentSnapshot myPlantDoc : plantSnapshots.getDocuments()) { // Đổi tên biến cho rõ ràng
                String myPlantId = myPlantDoc.getId();
                MyPlantModel currentMyPlant; // Sử dụng MyPlantModel
                try {
                    // Chuyển đổi sang MyPlantModel
                    currentMyPlant = myPlantDoc.toObject(MyPlantModel.class);
                    if (currentMyPlant == null) {
                        Log.e("ScheduleRepository", "Failed to convert document to MyPlantModel for myPlantId: " + myPlantId);
                        continue;
                    }
                    currentMyPlant.setId(myPlantId); // Gán ID
                    Log.d("ScheduleRepository", "Processing myPlant: " + myPlantId + " - Nickname: " + currentMyPlant.getNickname());
                } catch (Exception e) {
                    Log.e("ScheduleRepository", "Error converting document to MyPlantModel for myPlantId: " + myPlantId, e);
                    continue;
                }

                CollectionReference currentSchedulesRef = myPlantDoc.getReference().collection(Constants.SCHEDULES_COLLECTION);

                Task<Void> processingTask = currentSchedulesRef.get().continueWith(task -> {
                    Log.d("ScheduleRepository", "Fetched schedules for myPlant " + myPlantId + ". Task successful: " + task.isSuccessful());
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d("ScheduleRepository", "Found " + task.getResult().size() + " schedules for myPlant " + myPlantId);
                        for (QueryDocumentSnapshot scheduleDoc : task.getResult()) {
                            try {
                                ScheduleModel schedule = scheduleDoc.toObject(ScheduleModel.class);
                                schedule.setId(scheduleDoc.getId());
                                // Không cần set plantId vào ScheduleModel nữa

                                Log.d("ScheduleRepository", "Processing schedule: " + schedule.getId() + " for myPlant " + myPlantId);

                                boolean isDueToday = shouldDoToday(schedule);
                                Log.d("ScheduleRepository", "Schedule " + schedule.getId() + " - shouldDoToday: " + isDueToday);

                                if (isDueToday) {
                                    // Tạo đối tượng kết hợp mới
                                    ScheduleWithMyPlantInfo swmpi = new ScheduleWithMyPlantInfo(schedule, currentMyPlant);
                                    // Thêm vào map trung gian
                                    groupedScheduleMyPlantInfo.computeIfAbsent(schedule.getTaskId(), k -> new ArrayList<>()).add(swmpi);
                                    Log.d("ScheduleRepository", "Added schedule " + schedule.getId() + " with myPlant " + myPlantId + " to task group " + schedule.getTaskId());
                                }
                            } catch (Exception e) {
                                Log.e("ScheduleRepository", "Error processing schedule doc: " + scheduleDoc.getId() + " for myPlant " + myPlantId, e);
                            }
                        }
                    } else if (!task.isSuccessful()) {
                        Log.e("ScheduleRepository", "Error fetching schedules for myPlant " + myPlantId, task.getException());
                    }
                    return null;
                });
                allProcessingTasks.add(processingTask);
            }

            Log.d("ScheduleRepository", "Waiting for all schedule processing tasks (" + allProcessingTasks.size() + ")");
            Tasks.whenAll(allProcessingTasks).addOnSuccessListener(__ -> {
                Log.d("ScheduleRepository", "All schedule processing tasks completed. Size of groupedScheduleMyPlantInfo: " + groupedScheduleMyPlantInfo.size());

                // --- Lấy tên Task ---
                Map<String, List<ScheduleWithMyPlantInfo>> finalResult = new HashMap<>(); // Sử dụng kiểu Wrapper mới
                List<Task<Void>> taskNameTasks = new ArrayList<>();

                if (groupedScheduleMyPlantInfo.isEmpty()) {
                    Log.d("ScheduleRepository", "groupedScheduleMyPlantInfo is empty, returning empty result.");
                    callback.onSuccess(finalResult);
                    return;
                }

                CollectionReference tasksRef = db.collection(Constants.TASKS_COLLECTION);

                for (String taskId : groupedScheduleMyPlantInfo.keySet()) {
                    Log.d("ScheduleRepository", "Fetching task name for taskId: " + taskId);
                    Task<Void> taskNameTask = tasksRef.document(taskId).get()
                            .continueWith(taskDoc -> {
                                Log.d("ScheduleRepository", "Fetched task name for " + taskId + ". Task successful: " + taskDoc.isSuccessful());
                                if (taskDoc.isSuccessful()) {
                                    DocumentSnapshot doc = taskDoc.getResult();
                                    if (doc != null && doc.exists()) {
                                        String taskName = doc.getString("name");
                                        if (taskName != null && !taskName.isEmpty()) {
                                            Log.d("ScheduleRepository", "Task name found: '" + taskName + "' for ID: " + taskId);
                                            List<ScheduleWithMyPlantInfo> infoList = groupedScheduleMyPlantInfo.get(taskId); // Lấy list kiểu mới
                                            if (infoList != null) {
                                                finalResult.put(taskName, infoList); // Thêm vào kết quả cuối
                                            }
                                        } else {
                                            Log.w("ScheduleRepository", "Task name is null or empty for ID: " + taskId);
                                            // finalResult.put("Không rõ ("+taskId+")", groupedScheduleMyPlantInfo.get(taskId));
                                        }
                                    } else {
                                        Log.w("ScheduleRepository", "Task document not found or null for ID: " + taskId);
                                        // finalResult.put("Không rõ ("+taskId+")", groupedScheduleMyPlantInfo.get(taskId));
                                    }
                                } else {
                                    Log.e("ScheduleRepository", "Failed to fetch task document for ID: " + taskId, taskDoc.getException());
                                    // finalResult.put("Lỗi Task ("+taskId+")", groupedScheduleMyPlantInfo.get(taskId));
                                }
                                return null;
                            });
                    Log.d("ScheduleRepository", "TaskNameTask created for: " + taskId);
                    taskNameTasks.add(taskNameTask);
                }

                Log.d("ScheduleRepository", "Waiting for all task name fetch tasks (" + taskNameTasks.size() + ")");
                Tasks.whenAll(taskNameTasks).addOnSuccessListener(v -> {
                    Log.d("ScheduleRepository", "All task name fetch tasks completed. Final result size: " + finalResult.size());
                    callback.onSuccess(finalResult); // Trả về kết quả cuối cùng kiểu mới
                }).addOnFailureListener(e -> {
                    Log.e("ScheduleRepository", "Error fetching one or more task names", e);
                    callback.onError(e);
                });

            }).addOnFailureListener(e -> {
                Log.e("ScheduleRepository", "Error waiting for all schedule processing tasks", e);
                callback.onError(e);
            });

        }).addOnFailureListener(e -> {
            Log.e("ScheduleRepository", "Error fetching myPlants", e);
            callback.onError(e);
        });
    }

    @Override
    public void getSchedulesByMyPlantId(String userId, String myPlantId, FirestoreCallback<List<ScheduleModel>> callback) {
        if (userId == null || myPlantId == null) {
            callback.onError(new Exception("Invalid parameters"));
        } else {
            CollectionReference schedulesRef = db.collection(Constants.USERS_COLLECTION)
                    .document(userId)
                    .collection(Constants.MY_PLANTS_COLLECTION)
                    .document(myPlantId)
                    .collection(Constants.SCHEDULES_COLLECTION);

            schedulesRef.orderBy("start_date", Query.Direction.ASCENDING)
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                        List<ScheduleModel> schedules = new ArrayList<>();
                        for(QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            ScheduleModel schedule = doc.toObject(ScheduleModel.class);
                            schedule.setId(doc.getId());
                            schedules.add(schedule);
                        }
                        Log.d("ScheduleRepository", "Fetched " + schedules.size() + " schedules for plant ID: " + myPlantId);
                        callback.onSuccess(schedules);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ScheduleRepository", "Error fetching schedules for plant ID: " + myPlantId, e);
                        callback.onError(e);
                    });
        }
    }

    @Override
    public void deleteSchedule(String userId, String myPlantId, String scheduleId, FirestoreCallback<Void> callback) {
        if (userId == null || myPlantId == null || scheduleId == null) {
            Log.w("ScheduleRepository", "deleteSchedule: userId, myPlantId, or scheduleId is null");
            callback.onError(new IllegalArgumentException("userId, myPlantId, and scheduleId must not be null"));
            return;
        }
        DocumentReference scheduleRef = db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .collection(Constants.MY_PLANTS_COLLECTION)
                .document(myPlantId)
                .collection(Constants.SCHEDULES_COLLECTION)
                .document(scheduleId);

        scheduleRef.delete()
                .addOnSuccessListener(unused -> {
                    Log.d("ScheduleRepository", "Schedule deleted successfully for ID: " + scheduleId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("ScheduleRepository", "Error deleting schedule for ID: " + scheduleId, e);
                    callback.onError(e);
                });
    }
    private boolean shouldDoToday(ScheduleModel schedule) {
        if (schedule.getStartDate() == null) return false;
        Date start = schedule.getStartDate().toDate();
        Date now = Timestamp.now().toDate();
        int daysBetween = daysBetweenIgnoreTime(start, now);
        if (daysBetween < 0) return false;
        Integer freq = schedule.getFrequency();
        return daysBetween % freq == 0;
    }

    private int daysBetweenIgnoreTime(Date startDate, Date endDate) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MILLISECOND, 0);

        long diffMillis = endCal.getTimeInMillis() - startCal.getTimeInMillis();
        return (int) (diffMillis / (1000 * 60 * 60 * 24));
    }
}
