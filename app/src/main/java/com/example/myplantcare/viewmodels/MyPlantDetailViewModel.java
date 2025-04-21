// File: com.example.myplantcare.viewmodels/MyPlantDetailViewModel.java
package com.example.myplantcare.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData; // Import MediatorLiveData
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.Observer; // Import Observer
import androidx.lifecycle.ViewModelProvider;

import com.example.myplantcare.data.repositories.MyPlantRepository;
import com.example.myplantcare.data.repositories.MyPlantRepositoryImpl;
import com.example.myplantcare.data.repositories.ScheduleRepository;
import com.example.myplantcare.data.repositories.ScheduleRepositoryImpl;
import com.example.myplantcare.data.repositories.TaskRepository;
import com.example.myplantcare.data.repositories.TaskRepositoryImpl;
import com.example.myplantcare.data.responses.ScheduleDisplayItem;
import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.models.ScheduleModel;
import com.example.myplantcare.models.TaskModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyPlantDetailViewModel extends ViewModel {

    private static final String TAG = "MyPlantDetailViewModel";

    private final MyPlantRepository myPlantRepository;
    private final ScheduleRepository scheduleRepository;
    private final TaskRepository taskRepository;
    private final String userId;
    private final String myPlantId;

    // LiveData cho chi tiết cây chính
    private final MutableLiveData<MyPlantModel> _plantDetails = new MutableLiveData<>();
    public final LiveData<MyPlantModel> plantDetails = _plantDetails;

    // LiveData gốc từ repository cho danh sách lịch trình và tasks
    private final MutableLiveData<List<ScheduleModel>> _rawSchedules = new MutableLiveData<>();
    private final MutableLiveData<List<TaskModel>> _tasks = new MutableLiveData<>();

    // MediatorLiveData để kết hợp _rawSchedules và _tasks thành danh sách ScheduleDisplayItem
    private final MediatorLiveData<List<ScheduleDisplayItem>> _schedules = new MediatorLiveData<>();
    public final LiveData<List<ScheduleDisplayItem>> schedules = _schedules; // <-- Adapter sẽ observe LiveData này

    // LiveData cho trạng thái loading (có thể tách riêng cho detail, schedules, tasks)
    private final MutableLiveData<Boolean> _isLoadingDetails = new MutableLiveData<>();
    public final LiveData<Boolean> isLoadingDetails = _isLoadingDetails;

    private final MutableLiveData<Boolean> _isLoadingSchedules = new MutableLiveData<>();
    public final LiveData<Boolean> isLoadingSchedules = _isLoadingSchedules; // Loading cho cả schedules và tasks

    // LiveData cho kết quả các thao tác (xóa schedule, lỗi tải,...)
    private final MutableLiveData<String> _operationResult = new MutableLiveData<>();
    public final LiveData<String> operationResult = _operationResult;


    // Factory để truyền userId và myPlantId
    // (Giữ nguyên như trước)
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final String userId;
        private final String myPlantId;

        public Factory(String userId, String myPlantId) {
            this.userId = userId;
            this.myPlantId = myPlantId;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(MyPlantDetailViewModel.class)) {
                return (T) new MyPlantDetailViewModel(userId, myPlantId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    // Constructor
    public MyPlantDetailViewModel(String userId, String myPlantId) {
        this.userId = userId;
        this.myPlantId = myPlantId;
        this.myPlantRepository = new MyPlantRepositoryImpl();
        this.scheduleRepository = new ScheduleRepositoryImpl();
        this.taskRepository = new TaskRepositoryImpl();
        // Thêm nguồn cho MediatorLiveData: _rawSchedules và _tasks
        _schedules.addSource(_rawSchedules, schedulesList -> combineLatestData());
        _schedules.addSource(_tasks, tasksList -> combineLatestData());

        // Tải dữ liệu khi ViewModel được tạo
//        loadPlantDetails();
        loadPlantSchedulesAndTasks(); // <-- Gọi phương thức tải cả schedules và tasks
    }

    // Tải thông tin chi tiết cây chính (Giữ nguyên)
    public void loadPlantDetails() {
        // ... (code loadPlantDetails giữ nguyên) ...
        if (userId == null || myPlantId == null) {
            Log.e(TAG, "Cannot load plant details: userId or myPlantId is null");
            _plantDetails.setValue(null); // Đặt null để báo hiệu không tìm thấy
            return; // Không thể tải nếu thiếu ID
        }
        _isLoadingDetails.setValue(true);
        myPlantRepository.getMyPlantById(userId, myPlantId, new FirestoreCallback<MyPlantModel>() {
            @Override
            public void onSuccess(MyPlantModel result) {
                _plantDetails.setValue(result);
                _isLoadingDetails.setValue(false);
                Log.d(TAG, "Plant details loaded: " + (result != null ? result.getNickname() : "null"));
                if (result == null) {
                    _operationResult.postValue("Không tìm thấy thông tin cây.");
                }
            }

            @Override
            public void onError(Exception e) {
                _isLoadingDetails.setValue(false);
                _operationResult.postValue("Lỗi tải thông tin cây: " + e.getMessage());
                Log.e(TAG, "Error loading plant details", e);
                _plantDetails.setValue(null); // Đặt null khi lỗi
            }
        });
    }

    // Phương thức mới để tải cả lịch trình và tasks
    public void loadPlantSchedulesAndTasks() {
        if (userId == null || myPlantId == null) {
            Log.e(TAG, "Cannot load schedules and tasks: userId or myPlantId is null");
            // Đặt danh sách rỗng cho cả rawSchedules và tasks để combineLatestData xử lý empty state
            _rawSchedules.setValue(new ArrayList<>());
            _tasks.setValue(new ArrayList<>());
            return;
        }
        _isLoadingSchedules.setValue(true); // Bắt đầu loading cho cả schedules/tasks

        // Tải danh sách Schedules
        scheduleRepository.getSchedulesByMyPlantId(userId, myPlantId, new FirestoreCallback<List<ScheduleModel>>() {
            @Override
            public void onSuccess(List<ScheduleModel> result) {
                Log.d(TAG, "Raw schedules loaded. Size: " + (result != null ? result.size() : "null"));
                _rawSchedules.setValue(result); // Cập nhật LiveData raw schedules
                // Loading sẽ kết thúc sau khi cả schedules và tasks đều tải xong
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading raw schedules", e);
                _operationResult.postValue("Lỗi tải lịch trình: " + e.getMessage());
                _rawSchedules.setValue(new ArrayList<>()); // Đặt danh sách rỗng khi lỗi
                // Loading sẽ kết thúc sau khi cả schedules và tasks đều tải xong
            }
        });

        // Tải danh sách Tasks
        taskRepository.getAllTasks(new FirestoreCallback<List<TaskModel>>() {
            @Override
            public void onSuccess(List<TaskModel> result) {
                Log.d(TAG, "Tasks loaded. Size: " + (result != null ? result.size() : "null"));
                _tasks.setValue(result); // Cập nhật LiveData tasks
                // Loading sẽ kết thúc sau khi cả schedules và tasks đều tải xong
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading tasks", e);
                _operationResult.postValue("Lỗi tải thông tin công việc: " + e.getMessage());
                _tasks.setValue(new ArrayList<>()); // Đặt danh sách rỗng khi lỗi
                // Loading sẽ kết thúc sau khi cả schedules và tasks đều tải xong
            }
        });
    }

    // Phương thức kết hợp dữ liệu khi _rawSchedules HOẶC _tasks thay đổi
    private void combineLatestData() {
        List<ScheduleModel> rawSchedules = _rawSchedules.getValue();
        List<TaskModel> tasks = _tasks.getValue();

        // Chỉ thực hiện kết hợp khi cả hai nguồn dữ liệu đều đã được tải ít nhất một lần
        // (rawSchedules != null && tasks != null) có thể chưa đủ, vì giá trị ban đầu là null
        // Kiểm tra xem đã tải xong chưa (dựa vào isLoadingSchedules) HOẶC nếu cả 2 đều là rỗng (load lỗi/không có data)
        boolean schedulesLoaded = _isLoadingSchedules.getValue() != null && !_isLoadingSchedules.getValue(); // Đã tải xong Schedules?
        // Chúng ta cần một cờ loading riêng cho tasks hoặc kiểm tra giá trị của cả rawSchedules và tasks
        // Cách đơn giản hơn là chờ cả 2 LiveData đều có giá trị (có thể là null hoặc rỗng)

        if (rawSchedules != null && tasks != null) {
            Log.d(TAG, "Combining raw schedules (" + rawSchedules.size() + ") and tasks (" + tasks.size() + ").");
            List<ScheduleDisplayItem> displayItems = new ArrayList<>();
            Map<String, String> taskIdToNameMap = new HashMap<>();

            // Tạo map lookup Task ID -> Task Name
            for (TaskModel task : tasks) {
                if (task.getId() != null && task.getName() != null) {
                    taskIdToNameMap.put(task.getId(), task.getName());
                }
            }

            // Kết hợp dữ liệu schedules và tasks
            for (ScheduleModel schedule : rawSchedules) {
                String taskName = taskIdToNameMap.get(schedule.getTaskId());
                // Sử dụng Task ID làm tên nếu không tìm thấy tên hiển thị (hoặc "N/A")
                if (taskName == null) {
                    taskName = schedule.getTaskId() != null ? schedule.getTaskId() : "N/A";
                }

                ScheduleDisplayItem displayItem = new ScheduleDisplayItem(
                        schedule.getId(),
                        taskName,
                        schedule.getTaskId(),
                        schedule.getFrequency(),
                        schedule.getTime(),
                        schedule.getStartDate()
                );
                displayItems.add(displayItem);
            }

            _schedules.setValue(displayItems); // Cập nhật LiveData cho Adapter
            _isLoadingSchedules.setValue(false); // Kết thúc loading chỉ sau khi kết hợp xong
            Log.d(TAG, "Combined data updated. Display items size: " + displayItems.size());

        } else {
            // Dữ liệu chưa đủ để kết hợp (một trong hai danh sách vẫn là null)
            Log.d(TAG, "Data not ready for combining. rawSchedules=" + (rawSchedules != null) + ", tasks=" + (tasks != null));
            // Không làm gì, chờ LiveData còn lại cập nhật
            // Giữ isLoading = true nếu một trong hai nguồn vẫn đang tải
            if (_isLoadingSchedules.getValue() == null || _isLoadingSchedules.getValue() == false) {
                // Nếu không còn cờ loading chung, hãy kiểm tra cờ loading riêng cho từng nguồn
                // Hoặc chỉ set isLoading = false khi cả 2 nguồn đều đã cập nhật ít nhất 1 lần
                // Hiện tại, cờ loading chung được set FALSE ở cuối combineLatestData
            }
        }
    }


    // Phương thức để xóa lịch trình (Giữ nguyên logic, chỉ gọi loadPlantSchedulesAndTasks để tải lại)
    public void deleteSchedule(String scheduleId) {
        if (userId == null || myPlantId == null || scheduleId == null) {
            _operationResult.postValue("Lỗi: Không có thông tin để xóa lịch trình.");
            Log.w(TAG, "Cannot delete schedule: invalid input");
            return;
        }
        scheduleRepository.deleteSchedule(userId, myPlantId, scheduleId, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _operationResult.postValue("Đã xóa lịch trình thành công!");
                Log.d(TAG, "Schedule deleted successfully: " + scheduleId);
                // Sau khi xóa thành công, tải lại cả danh sách schedules và tasks để kết hợp lại
                loadPlantSchedulesAndTasks();
            }

            @Override
            public void onError(Exception e) {
                _operationResult.postValue("Lỗi xóa lịch trình: " + e.getMessage());
                Log.e(TAG, "Error deleting schedule: " + scheduleId, e);
            }
        });
    }

    // Phương thức thêm lịch trình (Giữ nguyên logic, gọi loadPlantSchedulesAndTasks để tải lại)
//    public void addSchedule(ScheduleModel schedule) {
//        if (userId == null || myPlantId == null || schedule == null) {
//            _operationResult.postValue("Lỗi: Dữ liệu lịch trình không hợp lệ.");
//            Log.w(TAG, "Cannot add schedule: invalid input");
//            return;
//        }
//        scheduleRepository.addSchedule(userId, myPlantId, schedule, new FirestoreCallback<Void>() {
//            @Override
//            public void onSuccess(Void result) {
//                _operationResult.postValue("Đã thêm lịch trình mới!");
//                Log.d(TAG, "Schedule added successfully.");
//                // Sau khi thêm thành công, tải lại cả danh sách schedules và tasks
//                loadPlantSchedulesAndTasks();
//            }
//
//            @Override
//            public void onError(Exception e) {
//                _operationResult.postValue("Lỗi thêm lịch trình: " + e.getMessage());
//                Log.e(TAG, "Error adding schedule", e);
//            }
//        });
//    }

    // Phương thức để xóa thông báo operationResult (Giữ nguyên)
    public void clearOperationResult() {
        _operationResult.postValue(null);
    }

}