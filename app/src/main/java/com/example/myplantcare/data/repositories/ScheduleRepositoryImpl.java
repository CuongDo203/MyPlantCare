package com.example.myplantcare.data.repositories;

import android.util.Log;
import android.util.Pair;

import com.example.myplantcare.data.responses.ScheduleWithMyPlantInfo;
import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.models.ScheduleModel;
import com.example.myplantcare.models.TaskLogModel;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ScheduleRepositoryImpl implements ScheduleRepository {
    private static final String TAG = "ScheduleRepositoryImpl";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection(Constants.USERS_COLLECTION);
    private CollectionReference schedulesRef = db.collection(Constants.SCHEDULES_COLLECTION);
    private CollectionReference tasksRef = db.collection(Constants.TASKS_COLLECTION);
    private CollectionReference myPlantsRef;

    @Override
    public void addSchedule(String userId, String myPlantId, ScheduleModel schedule, FirestoreCallback<Void> callback) {
        // Kiểm tra các tham số đầu vào
        if (userId == null || userId.isEmpty() || myPlantId == null || myPlantId.isEmpty() || schedule == null) {
            Log.e("ScheduleRepository", "addSchedule failed: userId, myPlantId, or schedule is null/empty.");
            if (callback != null) {
                callback.onError(new IllegalArgumentException("User ID, Plant ID, or Schedule object is invalid."));
            }
            return;
        }

        // Lấy tham chiếu đến subcollection 'schedules' dưới cây của người dùng
        CollectionReference schedulesCollectionRef = userRef
                .document(userId)
                .collection(Constants.MY_PLANTS_COLLECTION) // Constants.MY_PLANTS_COLLECTION = "my_plants"
                .document(myPlantId)
                .collection(Constants.SCHEDULES_COLLECTION); // Constants.SCHEDULES_COLLECTION = "schedules"

        // Thêm đối tượng schedule vào collection. Firestore sẽ tự tạo ID.
        schedulesCollectionRef.add(schedule)
                .addOnSuccessListener(documentReference -> {
                    // documentReference chứa tham chiếu đến document mới được tạo
                    Log.d("ScheduleRepository", "Schedule added with ID: " + documentReference.getId() + " for plant: " + myPlantId);
                    if (callback != null) {
                        callback.onSuccess(null); // Thông báo thành công
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ScheduleRepository", "Error adding schedule for plant: " + myPlantId, e);
                    if (callback != null) {
                        callback.onError(e); // Thông báo thất bại kèm Exception
                    }
                });
    }

    @Override
    public void updateSchedule(String userId, String myPlantId, ScheduleModel schedule, FirestoreCallback<Void> callback) {
        if (userId == null || userId.isEmpty() || myPlantId == null || myPlantId.isEmpty() || schedule == null || schedule.getId() == null || schedule.getId().isEmpty()) {
            Log.e(TAG, "updateSchedule failed: userId, myPlantId, schedule, or schedule ID is null/empty.");
            if (callback != null) {
                callback.onError(new IllegalArgumentException("User ID, Plant ID, Schedule object, or Schedule ID is invalid."));
            }
            return;
        }
        DocumentReference scheduleRef = userRef
                .document(userId)
                .collection(Constants.MY_PLANTS_COLLECTION)
                .document(myPlantId)
                .collection(Constants.SCHEDULES_COLLECTION)
                .document(schedule.getId());
        scheduleRef.set(schedule) // Ghi đè toàn bộ document
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Schedule updated successfully. Schedule ID: " + schedule.getId() + " for plant: " + myPlantId);
                    if (callback != null) {
                        callback.onSuccess(null); // Thông báo thành công
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating schedule. Schedule ID: " + schedule.getId() + " for plant: " + myPlantId, e);
                    if (callback != null) {
                        callback.onError(e); // Thông báo thất bại
                    }
                });
    }

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
//                                    swmpi.setCompletedOnDate(isCompleted);
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
    public void getSchedulesForDateGroupedByTask(String userId, Calendar targetDate, FirestoreCallback<Pair<Map<String, List<ScheduleWithMyPlantInfo>>, Map<String, List<ScheduleWithMyPlantInfo>>>> callback) {
        if (userId == null || userId.isEmpty() || targetDate == null) {
            Log.w(TAG, "getSchedulesForDateGroupedByTask: invalid input. userId or targetDate is null/empty.");
            callback.onError(new IllegalArgumentException("User ID and targetDate must not be null/empty."));
            return;
        }

        // Chuẩn hóa ngày mục tiêu về 0 giờ
        Calendar targetDateZeroTime = (Calendar) targetDate.clone();
        targetDateZeroTime.set(Calendar.HOUR_OF_DAY, 0);
        targetDateZeroTime.set(Calendar.MINUTE, 0);
        targetDateZeroTime.set(Calendar.SECOND, 0);
        targetDateZeroTime.set(Calendar.MILLISECOND, 0);
        Log.d(TAG, "Fetching schedules for date (zero time): " + targetDateZeroTime.getTime());

        // Tham chiếu đến subcollection 'my_plants' của người dùng cụ thể
        CollectionReference myPlantsRef = userRef.document(userId).collection(Constants.MY_PLANTS_COLLECTION);

        // --- BƯỚC 1: Lấy tất cả các cây ('my_plants') của người dùng ---
        myPlantsRef.get().addOnSuccessListener(plantSnapshots -> {
            Log.d(TAG, "Fetched myPlants snapshot. Size: " + plantSnapshots.size());

            if (plantSnapshots.isEmpty()) {
                Log.d(TAG, "No myPlants found for user. Returning empty schedules.");
                callback.onSuccess(new Pair<>(new HashMap<>(), new HashMap<>()));
                return;
            }

            // Lưu trữ các MyPlantModel trong một map để tra cứu nhanh bằng ID
            Map<String, MyPlantModel> myPlantMap = new HashMap<>();
            for (DocumentSnapshot myPlantDoc : plantSnapshots.getDocuments()) {
                String myPlantId = myPlantDoc.getId();
                try {
                    MyPlantModel myPlant = myPlantDoc.toObject(MyPlantModel.class);
                    if (myPlant != null) {
                        myPlant.setId(myPlantId);
                        myPlantMap.put(myPlantId, myPlant);
                    } else {
                        Log.w(TAG, "Failed to convert myPlant document to MyPlantModel: " + myPlantId);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error converting myPlant document to MyPlantModel: " + myPlantId, e);
                }
            }

            // --- BƯỚC 2: Đối với mỗi cây, lấy lịch trình của nó và kiểm tra trạng thái hoàn thành ---
            // Danh sách các Task. Mỗi Task trong danh sách này sẽ xử lý schedules và task logs cho MỘT cây
            List<Task<List<ScheduleCompletionTuple>>> allPlantSchedulesAndCompletionTasks = new ArrayList<>();

            // Lặp qua từng document cây tìm thấy
            for (DocumentSnapshot myPlantDoc : plantSnapshots.getDocuments()) {
                String myPlantId = myPlantDoc.getId();
                MyPlantModel currentMyPlant = myPlantMap.get(myPlantId); // Lấy MyPlantModel tương ứng từ map
                if (currentMyPlant == null) {
                    Log.w(TAG, "MyPlantModel not found in map for ID: " + myPlantId + ", skipping.");
                    continue;
                }
                // Tham chiếu đến subcollection 'schedules' và 'task_logs' của cây hiện tại
                CollectionReference currentSchedulesRef = myPlantDoc.getReference().collection(Constants.SCHEDULES_COLLECTION);
                CollectionReference currentTaskLogsRef = myPlantDoc.getReference().collection(Constants.TASK_LOGS_COLLECTION);
                // Task để lấy tất cả schedules cho CÂY NÀY
                Task<QuerySnapshot> fetchSchedulesTask = currentSchedulesRef.get();
                // Tạo một Task tiếp theo (continueWithTask) để xử lý kết quả fetch schedules
                // Task này sẽ trả về một danh sách các ScheduleCompletionTuple cho cây hiện tại
                Task<List<ScheduleCompletionTuple>> plantSchedulesAndCompletionTask = fetchSchedulesTask.continueWithTask(task -> {
                    List<ScheduleCompletionTuple> tuples = new ArrayList<>();
                    if (!task.isSuccessful() || task.getResult() == null) {
                        Log.e(TAG, "Error fetching schedules for myPlant " + myPlantId, task.getException());
                        return Tasks.forResult(tuples); // Trả về danh sách rỗng nếu lỗi hoặc không có kết quả
                    }
                    // Lấy danh sách ScheduleModel từ kết quả fetch
                    List<ScheduleModel> schedulesForPlant = new ArrayList<>();
                    for (QueryDocumentSnapshot scheduleDoc : task.getResult()) {
                        try {
                            ScheduleModel schedule = scheduleDoc.toObject(ScheduleModel.class);
                            schedule.setId(scheduleDoc.getId()); // Gán ID document Firestore
                            schedulesForPlant.add(schedule);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing schedule doc: " + scheduleDoc.getId() + " for myPlant " + myPlantId, e);
                        }
                    }
                    Log.d(TAG, "Found " + schedulesForPlant.size() + " schedules for myPlant " + myPlantId);

                    List<Task<Void>> completionCheckTasks = new ArrayList<>();
                    // Lặp qua từng schedule của cây hiện tại
                    for (ScheduleModel schedule : schedulesForPlant) {
                        // Kiểm tra tính hợp lệ cơ bản của schedule
                        if (schedule.getTaskId() == null || schedule.getTaskId().isEmpty() || schedule.getId() == null || schedule.getId().isEmpty()) {
                            Log.w(TAG, "Skipping schedule with invalid taskId or ID: " + schedule.getId());
                            continue;
                        }

                        // Kiểm tra xem lịch trình này có đến hạn vào ngày mục tiêu không
                        boolean isDue = isDueOnDate(schedule, targetDateZeroTime);

                        // Chỉ xử lý các lịch trình đến hạn
                        if (isDue) {
                            // Log.d(TAG, "Schedule is due. Checking task logs for completion.");
                            // --- Truy vấn subcollection 'task_logs' để kiểm tra hoàn thành cho schedule và ngày cụ thể ---
                            Task<QuerySnapshot> fetchTaskLogTask = currentTaskLogsRef
                                    .whereEqualTo("scheduleId", schedule.getId()) // Lọc theo ID của lịch trình
                                    .whereEqualTo("date", new Timestamp(targetDateZeroTime.getTime())) // Lọc theo ngày mục tiêu (đã chuẩn hóa)
                                    .whereEqualTo("status", true) // Lọc theo trạng thái 'true'
                                    .limit(1) // Chỉ cần tìm thấy một log 'done' là đủ
                                    .get();

                            // Tạo một Task tiếp theo (continueWith) để xử lý kết quả truy vấn task log
                            Task<Void> processLogResultTask = fetchTaskLogTask.continueWith(logTask -> {
                                boolean isCompleted = false; // Mặc định là chưa hoàn thành
                                // Nếu truy vấn task log thành công và có kết quả (tìm thấy log hoàn thành)
                                if (logTask.isSuccessful() && logTask.getResult() != null) {
                                    isCompleted = !logTask.getResult().isEmpty(); // isCompleted = true nếu có ít nhất 1 document
                                } else if (!logTask.isSuccessful()) {
                                    Log.e(TAG, "Error fetching task logs for schedule " + schedule.getId(), logTask.getException());
                                    // Nếu lỗi, coi như chưa hoàn thành (isCompleted vẫn là false)
                                }

                                // Tạo một tuple chứa schedule, myPlant (của cây hiện tại), và trạng thái hoàn thành
                                tuples.add(new ScheduleCompletionTuple(schedule, currentMyPlant, isCompleted));
                                return null; // continueWith<Void> cần trả về null
                            });
                            completionCheckTasks.add(processLogResultTask); // Thêm Task xử lý log vào danh sách
                        }
                    }
                    // Đợi tất cả các Task kiểm tra hoàn thành cho các schedule của CÂY NÀY hoàn tất
                    // Sau đó, trả về danh sách các tuple đã thu thập được cho CÂY NÀY
                    return Tasks.whenAll(completionCheckTasks).continueWith(completionTask -> tuples);
                });
                // Thêm Task xử lý cho CÂY NÀY vào danh sách chung cho TẤT CẢ CÁC CÂY
                allPlantSchedulesAndCompletionTasks.add(plantSchedulesAndCompletionTask);
            }

            // --- BƯỚC 3: Đợi tất cả các Task xử lý từng cây hoàn tất và gom kết quả ---
            // Tasks.whenAllSuccess đảm bảo tất cả các Task trong danh sách đã hoàn thành thành công
            Tasks.whenAllSuccess(allPlantSchedulesAndCompletionTasks).addOnSuccessListener(resultsList -> {
                Log.d(TAG, "All plant schedules and completion checks completed. Processing results.");

                // Gom tất cả các tuple từ danh sách kết quả của từng cây vào một danh sách phẳng
                List<ScheduleCompletionTuple> allDueScheduleTuples = new ArrayList<>();
                for (Object plantTuplesObject : resultsList) {
                    // Kiểm tra xem phần tử có phải là List không
                    if (plantTuplesObject instanceof List) {
                        // Ép kiểu phần tử về List<ScheduleCompletionTuple>
                        // Sử dụng @SuppressWarnings để bỏ qua cảnh báo unchecked cast
                        @SuppressWarnings("unchecked")
                        List<ScheduleCompletionTuple> plantTuples = (List<ScheduleCompletionTuple>) plantTuplesObject;
                        // Thêm tất cả các tuple từ danh sách này vào danh sách phẳng chung
                        if (plantTuples != null) {
                            allDueScheduleTuples.addAll(plantTuples);
                        }
                    } else {
                        // Trường hợp ngoại lệ nếu kết quả không phải List (không mong đợi)
                        Log.w(TAG, "Encountered unexpected object type in resultsList: " + plantTuplesObject);
                    }
                }
                Log.d(TAG, "Collected " + allDueScheduleTuples.size() + " due schedule tuples across all plants.");
                Collections.sort(allDueScheduleTuples, new Comparator<ScheduleCompletionTuple>() {
                    @Override
                    public int compare(ScheduleCompletionTuple tuple1, ScheduleCompletionTuple tuple2) {
                        Timestamp time1 = tuple1.schedule != null ? tuple1.schedule.getTime() : null;
                        Timestamp time2 = tuple2.schedule != null ? tuple2.schedule.getTime() : null;

                        // Xử lý trường hợp null: đặt các lịch trình không có thời gian ở cuối danh sách
                        if (time1 == null && time2 == null) return 0; // Cả hai đều null -> coi như bằng nhau
                        if (time1 == null) return 1; // time1 null, time2 không null -> time1 đứng sau time2
                        if (time2 == null) return -1; // time2 null, time1 không null -> time1 đứng trước time2

                        // So sánh Timestamp: compareTo trả về <0 nếu time1 < time2, 0 nếu bằng, >0 nếu time1 > time2
                        return time1.compareTo(time2); // Sắp xếp tăng dần (sớm đến muộn)
                    }
                });
                Log.d(TAG, "Sorted due schedule tuples by time.");
                // --- BƯỚC 4: Thu thập tất cả các Task ID duy nhất từ các tuples đến hạn ---
                List<String> uniqueTaskIds = new ArrayList<>();
                for (ScheduleCompletionTuple tuple : allDueScheduleTuples) {
                    // Chỉ thêm vào nếu schedule và taskId hợp lệ và chưa có trong danh sách unique
                    if (tuple.schedule != null && tuple.schedule.getTaskId() != null && !tuple.schedule.getTaskId().isEmpty() && !uniqueTaskIds.contains(tuple.schedule.getTaskId())) {
                        uniqueTaskIds.add(tuple.schedule.getTaskId());
                    }
                }
                Log.d(TAG, "Fetching names for unique Task IDs: " + uniqueTaskIds.size());
                // --- BƯỚC 5: Lấy tên Task cho các Task ID duy nhất ---
                List<Task<DocumentSnapshot>> taskNameFetchTasks = new ArrayList<>(); // Danh sách các Task để fetch Task document
                Map<String, String> taskIdToNameMap = new HashMap<>(); // Map để lưu Task ID -> Task Name

                if (!uniqueTaskIds.isEmpty()) {
                    for (String taskId : uniqueTaskIds) {
                        // Tạo một Task để lấy document của Task cụ thể
                        Task<DocumentSnapshot> taskFetch = tasksRef.document(taskId).get();
                        // Thêm listener để xử lý kết quả fetch Task document
                        taskFetch.addOnSuccessListener(taskDoc -> {
                            if (taskDoc.exists()) {
                                String taskName = taskDoc.getString("name"); // Lấy tên Task (field 'name')
                                if (taskName != null && !taskName.isEmpty()) {
                                    taskIdToNameMap.put(taskId, taskName); // Lưu vào map nếu tên hợp lệ
                                    Log.d(TAG, "Fetched task name '" + taskName + "' for ID: " + taskId);
                                } else {
                                    Log.w(TAG, "Task name is null or empty for ID: " + taskId);
                                }
                            } else {
                                Log.w(TAG, "Task document not found for ID: " + taskId);
                            }
                        });
                        taskNameFetchTasks.add(taskFetch); // Thêm Task fetch Task document vào danh sách chờ
                    }
                }
                // --- Đợi tất cả các Task lấy tên Task hoàn tất và gom nhóm lịch trình ---
                Tasks.whenAll(taskNameFetchTasks).addOnSuccessListener(v -> {
                    Log.d(TAG, "All task name fetch tasks completed. Grouping schedules by task name and completion status.");
                    // Khởi tạo hai map cuối cùng: một cho lịch trình chưa hoàn thành, một cho đã hoàn thành
                    Map<String, List<ScheduleWithMyPlantInfo>> finalUncompletedMap = new HashMap<>();
                    Map<String, List<ScheduleWithMyPlantInfo>> finalCompletedMap = new HashMap<>();
                    // Gom nhóm các tuple đã thu thập được sử dụng tên Task và phân loại theo trạng thái hoàn thành
                    for (ScheduleCompletionTuple tuple : allDueScheduleTuples) {
                        // Kiểm tra tính hợp lệ của tuple trước khi xử lý
                        if (tuple != null && tuple.schedule != null && tuple.schedule.getTaskId() != null && !tuple.schedule.getTaskId().isEmpty() && tuple.myPlant != null) {
                            String taskName = taskIdToNameMap.get(tuple.schedule.getTaskId());
                            if (taskName == null || taskName.isEmpty()) {
                                taskName = "Unknown Task (" + tuple.schedule.getTaskId() + ")";
                                Log.w(TAG, "Task name not found in map for ID: " + tuple.schedule.getTaskId() + ". Using fallback name: " + taskName);
                            }
                            // Tạo đối tượng wrapper cuối cùng
                            ScheduleWithMyPlantInfo swmpi = new ScheduleWithMyPlantInfo(tuple.schedule, tuple.myPlant);

                            swmpi.setTaskName(taskName);
                            swmpi.setCompletedOnDate(tuple.isCompleted);
                            // Thêm đối tượng vào map tương ứng dựa trên trạng thái hoàn thành
                            if (tuple.isCompleted) {
                                finalCompletedMap.computeIfAbsent(taskName, k -> new ArrayList<>()).add(swmpi);
                            } else {
                                finalUncompletedMap.computeIfAbsent(taskName, k -> new ArrayList<>()).add(swmpi);
                            }
                        } else {
                            Log.w(TAG, "Skipping invalid tuple during final grouping.");
                        }
                    }
                    Log.d(TAG, "Final grouping completed. Uncompleted groups: " + finalUncompletedMap.size() + ", Completed groups: " + finalCompletedMap.size());
                    // --- BƯỚC 7: Trả về kết quả cuối cùng qua callback ---
                    callback.onSuccess(new Pair<>(finalUncompletedMap, finalCompletedMap));
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error waiting for task name fetch tasks", e);
                    callback.onError(e);
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error collecting all schedule completion tuples", e);
                callback.onError(e);
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching myPlants", e);
            callback.onError(e);
        });
    }

    @Override
    public void unmarkScheduleCompleted(String userId, String myPlantId, String scheduleId, Calendar date, FirestoreCallback<Void> callback) {
        if (userId == null || userId.isEmpty() || myPlantId == null || myPlantId.isEmpty() || scheduleId == null || scheduleId.isEmpty() || date == null) {
            Log.w(TAG, "unmarkScheduleCompleted: invalid input.");
            if (callback != null) {
                callback.onError(new IllegalArgumentException("userId, myPlantId, scheduleId, and date must not be null/empty."));
            }
            return;
        }

        // Chuẩn hóa ngày về 0 giờ để tìm task log
        Calendar dateZeroTime = (Calendar) date.clone();
        dateZeroTime.set(Calendar.HOUR_OF_DAY, 0);
        dateZeroTime.set(Calendar.MINUTE, 0);
        dateZeroTime.set(Calendar.SECOND, 0);
        dateZeroTime.set(Calendar.MILLISECOND, 0);

        // Lấy tham chiếu đến subcollection 'task_logs' dưới cây cụ thể
        CollectionReference taskLogsCollectionRef = userRef
                .document(userId)
                .collection(Constants.MY_PLANTS_COLLECTION)
                .document(myPlantId)
                .collection(Constants.TASK_LOGS_COLLECTION);

        // Truy vấn task log cho scheduleId và ngày cụ thể (đã chuẩn hóa)
        // Có thể có nhiều log cho cùng schedule và ngày nếu cho phép hoàn thành nhiều lần
        // Logic này sẽ xóa TẤT CẢ các log matching. Nếu chỉ muốn xóa 1, cần logic phức tạp hơn.
        taskLogsCollectionRef
                .whereEqualTo("scheduleId", scheduleId)
                .whereEqualTo("date", new Timestamp(dateZeroTime.getTime()))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Danh sách các Task xóa
                    List<Task<Void>> deleteTasks = new ArrayList<>();
                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " task logs to delete for schedule " + scheduleId + " on date " + dateZeroTime.getTime());

                    // Tạo Task xóa cho mỗi document log tìm thấy
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        deleteTasks.add(doc.getReference().delete());
                    }
                    // Đợi tất cả các Task xóa hoàn tất
                    Tasks.whenAll(deleteTasks)
                            .addOnSuccessListener(v -> {
                                Log.d(TAG, "All matching task logs deleted successfully.");
                                if (callback != null) {
                                    callback.onSuccess(null); // Báo cáo thành công
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting one or more matching task logs.", e);
                                if (callback != null) {
                                    callback.onError(e); // Báo cáo lỗi
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying task logs for deletion for schedule: " + scheduleId + " on date: " + dateZeroTime.getTime(), e);
                    if (callback != null) {
                        callback.onError(e); // Báo cáo lỗi truy vấn
                    }
                });
    }

    @Override
    public void markScheduleCompleted(String userId, String myPlantId, String scheduleId,String taskName, Calendar date, FirestoreCallback<Void> callback) {
        // Kiểm tra tham số đầu vào
        if (userId == null || userId.isEmpty() || myPlantId == null || myPlantId.isEmpty() || scheduleId == null || scheduleId.isEmpty() || date == null) {
            Log.w(TAG, "markScheduleCompleted: invalid input.");
            if (callback != null) {
                callback.onError(new IllegalArgumentException("userId, myPlantId, scheduleId, and date must not be null/empty."));
            }
            return;
        }

        // Chuẩn hóa ngày về 0 giờ để lưu trong task log (đảm bảo khớp khi truy vấn sau này)
        Calendar dateZeroTime = (Calendar) date.clone();
        dateZeroTime.set(Calendar.HOUR_OF_DAY, 0);
        dateZeroTime.set(Calendar.MINUTE, 0);
        dateZeroTime.set(Calendar.SECOND, 0);
        dateZeroTime.set(Calendar.MILLISECOND, 0);

        // Tạo đối tượng TaskLogModel
        TaskLogModel taskLog = new TaskLogModel(scheduleId, taskName, new Timestamp(dateZeroTime.getTime()), true, "");

        // Lấy tham chiếu đến subcollection 'task_logs' dưới cây cụ thể của người dùng
        CollectionReference taskLogsCollectionRef = userRef
                .document(userId)
                .collection(Constants.MY_PLANTS_COLLECTION)
                .document(myPlantId)
                .collection(Constants.TASK_LOGS_COLLECTION); // "task_logs"

        // Thêm document log mới. Firestore sẽ tự tạo ID.
        taskLogsCollectionRef.add(taskLog)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Task log added successfully for schedule: " + scheduleId + " on date: " + dateZeroTime.getTime());
                    if (callback != null) {
                        callback.onSuccess(null); // Báo cáo thành công
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding task log for schedule: " + scheduleId + " on date: " + dateZeroTime.getTime(), e);
                    if (callback != null) {
                        callback.onError(e); // Báo cáo lỗi
                    }
                });
    }

    // Helper class to hold schedule, myPlant, and completion status
    private static class ScheduleCompletionTuple {
        ScheduleModel schedule;
        MyPlantModel myPlant;
        boolean isCompleted;

        ScheduleCompletionTuple(ScheduleModel schedule, MyPlantModel myPlant, boolean isCompleted) {
            this.schedule = schedule;
            this.myPlant = myPlant;
            this.isCompleted = isCompleted;
        }
    }
    private boolean isDueOnDate(ScheduleModel schedule, Calendar targetDateZeroTime) {
        if (schedule == null || schedule.getStartDate() == null || schedule.getFrequency() == null || schedule.getFrequency() <= 0) {
            Log.w(TAG, "isDueOnDate: invalid schedule data.");
            return false;
        }

        Date start = schedule.getStartDate().toDate();

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(start);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        if (targetDateZeroTime.before(startCal)) {
            return false;
        }

        long diffMillis = targetDateZeroTime.getTimeInMillis() - startCal.getTimeInMillis();
        int daysBetween = (int) TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS);

        return daysBetween % schedule.getFrequency() == 0;
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
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
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
