// File: com.example.myplantcare.viewmodels/MyPlantDetailViewModel.java
package com.example.myplantcare.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.Observer;
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

    // LiveData cho kết quả các thao tác (xóa schedule, lỗi tải, cập nhật ảnh, xóa cây)
    private final MutableLiveData<String> _operationResult = new MutableLiveData<>();
    public final LiveData<String> operationResult = _operationResult;

    // LiveData cho kết quả cập nhật ảnh
    private final MutableLiveData<Boolean> _imageUpdateResult = new MutableLiveData<>();
    public final LiveData<Boolean> getImageUpdateResult() {
        return _imageUpdateResult;
    }

    // LiveData cho kết quả xóa cây
    private final MutableLiveData<Boolean> _plantDeletionResult = new MutableLiveData<>();
    public final LiveData<Boolean> getPlantDeletionResult() {
        return _plantDeletionResult;
    }


    // Factory để truyền userId và myPlantId
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
        this.scheduleRepository = new ScheduleRepositoryImpl(); // Khởi tạo ScheduleRepository
        this.taskRepository = new TaskRepositoryImpl(); // Khởi tạo TaskRepository

        // Thêm nguồn cho MediatorLiveData: _rawSchedules và _tasks
        _schedules.addSource(_rawSchedules, schedulesList -> combineLatestData());
        _schedules.addSource(_tasks, tasksList -> combineLatestData());

        // Tải dữ liệu ban đầu khi ViewModel được tạo
        loadPlantDetails(); // Tải chi tiết cây
        loadPlantSchedulesAndTasks(); // Tải cả schedules và tasks
    }

    // Tải thông tin chi tiết cây chính
    public void loadPlantDetails() {
        if (userId == null || myPlantId == null) {
            Log.e(TAG, "Cannot load plant details: userId or myPlantId is null");
            _plantDetails.setValue(null); // Đặt null để báo hiệu không tìm thấy
            _isLoadingDetails.setValue(false); // Dừng loading
            return; // Không thể tải nếu thiếu ID
        }
        _isLoadingDetails.setValue(true); // Bắt đầu loading chi tiết
        myPlantRepository.getMyPlantById(userId, myPlantId, new FirestoreCallback<MyPlantModel>() {
            @Override
            public void onSuccess(MyPlantModel result) {
                _plantDetails.setValue(result);
                _isLoadingDetails.setValue(false); // Kết thúc loading chi tiết
                Log.d(TAG, "Plant details loaded: " + (result != null ? result.getNickname() : "null"));
                if (result == null) {
                    _operationResult.postValue("Không tìm thấy thông tin cây.");
                }
            }

            @Override
            public void onError(Exception e) {
                _isLoadingDetails.setValue(false); // Kết thúc loading chi tiết
                _operationResult.postValue("Lỗi tải thông tin cây: " + e.getMessage());
                Log.e(TAG, "Error loading plant details", e);
                _plantDetails.setValue(null); // Đặt null khi lỗi
            }
        });
    }

    // Phương thức để tải cả lịch trình và tasks
    public void loadPlantSchedulesAndTasks() {
        if (userId == null || myPlantId == null) {
            Log.e(TAG, "Cannot load schedules and tasks: userId or myPlantId is null");
            // Đặt danh sách rỗng cho cả rawSchedules và tasks để combineLatestData xử lý empty state
            _rawSchedules.setValue(new ArrayList<>());
            _tasks.setValue(new ArrayList<>());
            _isLoadingSchedules.setValue(false); // Dừng loading
            return;
        }
        _isLoadingSchedules.setValue(true); // Bắt đầu loading cho cả schedules/tasks

        // Tải danh sách Schedules
        scheduleRepository.getSchedulesByMyPlantId(userId, myPlantId, new FirestoreCallback<List<ScheduleModel>>() {
            @Override
            public void onSuccess(List<ScheduleModel> result) {
                Log.d(TAG, "Raw schedules loaded. Size: " + (result != null ? result.size() : "null"));
                _rawSchedules.setValue(result); // Cập nhật LiveData raw schedules
                // Loading sẽ kết thúc sau khi cả schedules và tasks đều tải xong (trong combineLatestData)
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading raw schedules", e);
                _operationResult.postValue("Lỗi tải lịch trình: " + e.getMessage());
                _rawSchedules.setValue(new ArrayList<>()); // Đặt danh sách rỗng khi lỗi
                // Loading sẽ kết thúc sau khi cả schedules và tasks đều tải xong (trong combineLatestData)
            }
        });

        // Tải danh sách Tasks
        taskRepository.getAllTasks(new FirestoreCallback<List<TaskModel>>() {
            @Override
            public void onSuccess(List<TaskModel> result) {
                Log.d(TAG, "Tasks loaded. Size: " + (result != null ? result.size() : "null"));
                _tasks.setValue(result); // Cập nhật LiveData tasks
                // Loading sẽ kết thúc sau khi cả schedules và tasks đều tải xong (trong combineLatestData)
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading tasks", e);
                _operationResult.postValue("Lỗi tải thông tin công việc: " + e.getMessage());
                _tasks.setValue(new ArrayList<>()); // Đặt danh sách rỗng khi lỗi
                // Loading sẽ kết thúc sau khi cả schedules và tasks đều tải xong (trong combineLatestData)
            }
        });
    }

    // Phương thức kết hợp dữ liệu khi _rawSchedules HOẶC _tasks thay đổi
    private void combineLatestData() {
        List<ScheduleModel> rawSchedules = _rawSchedules.getValue();
        List<TaskModel> tasks = _tasks.getValue();

        // Chỉ thực hiện kết hợp khi cả hai nguồn dữ liệu đều đã được tải ít nhất một lần
        // (rawSchedules != null && tasks != null)
        // Cờ _isLoadingSchedules sẽ được set false ở cuối phương thức này sau khi kết hợp
        // Điều này đảm bảo Observer trong Activity chỉ cập nhật UI sau khi dữ liệu sẵn sàng kết hợp.
        if (rawSchedules != null && tasks != null) {
            Log.d(TAG, "Combining raw schedules (" + rawSchedules.size() + ") and tasks (" + tasks.size() + ").");
            List<ScheduleDisplayItem> displayItems = new ArrayList<>();
            Map<String, String> taskIdToNameMap = new HashMap<>();

            // Tạo map lookup Task ID -> Task Name
            if(tasks != null) { // Kiểm tra null lần nữa cho an toàn
                for (TaskModel task : tasks) {
                    if (task.getId() != null && task.getName() != null) {
                        taskIdToNameMap.put(task.getId(), task.getName());
                    }
                }
            }


            // Kết hợp dữ liệu schedules và tasks
            if(rawSchedules != null) { // Kiểm tra null lần nữa cho an toàn
                for (ScheduleModel schedule : rawSchedules) {
                    // Bỏ qua schedule nếu taskId là null hoặc rỗng
                    if (schedule.getTaskId() == null || schedule.getTaskId().isEmpty()) {
                        Log.w(TAG, "Skipping schedule with null or empty taskId: " + schedule.getId());
                        continue;
                    }
                    String taskName = taskIdToNameMap.get(schedule.getTaskId());
                    // Sử dụng Task ID làm tên nếu không tìm thấy tên hiển thị (hoặc "N/A")
                    if (taskName == null) {
                        taskName = "Unknown Task (" + schedule.getTaskId() + ")"; // Tên placeholder nếu không tìm thấy task
                        Log.w(TAG, "Task name not found for taskId: " + schedule.getTaskId());
                    }

                    // Đảm bảo ScheduleModel có getter cho ID
                    if (schedule.getId() != null) {
                        ScheduleDisplayItem displayItem = new ScheduleDisplayItem(
                                schedule.getId(), // Schedule Document ID
                                taskName,       // Tên Task (hiển thị)
                                schedule.getTaskId(), // Task Document ID
                                schedule.getFrequency(), // Tần suất
                                schedule.getTime(),     // Giờ
                                schedule.getStartDate() // Ngày bắt đầu (Timestamp)
                                // TODO: Thêm các trường khác cần hiển thị (ví dụ: interval cho custom frequency)
                        );
                        displayItems.add(displayItem);
                    } else {
                        Log.w(TAG, "Skipping schedule with null ID.");
                    }

                }
            }


            _schedules.setValue(displayItems); // Cập nhật LiveData cho Adapter
            _isLoadingSchedules.setValue(false); // Kết thúc loading chỉ sau khi kết hợp xong
            Log.d(TAG, "Combined data updated. Display items size: " + displayItems.size());

        } else {
            // Dữ liệu chưa đủ để kết hợp (một trong hai danh sách vẫn là null)
            // Giữ trạng thái loading nếu một trong hai nguồn vẫn đang tải
            // Nếu cả 2 nguồn đều null khi tải xong (ví dụ: load lỗi), _schedules sẽ không bao giờ được set
            // Xử lý trường hợp lỗi tải trong onError của từng repository
            Log.d(TAG, "Data not ready for combining. rawSchedules=" + (rawSchedules != null) + ", tasks=" + (tasks != null));
            // Không set _isLoadingSchedules = false ở đây, chờ đến khi cả 2 nguồn đều cập nhật
            // (combineLatestData sẽ được gọi lại)
        }
    }


    // Phương thức xóa lịch trình
    public void deleteSchedule(String scheduleId) {
        if (userId == null || myPlantId == null || scheduleId == null || scheduleId.isEmpty()) {
            _operationResult.postValue("Lỗi: Không có thông tin để xóa lịch trình.");
            Log.w(TAG, "Cannot delete schedule: invalid input");
            return;
        }
        // Có thể set một cờ loading riêng cho thao tác xóa schedule nếu cần
        // _isLoadingSchedules.setValue(true); // Hoặc một cờ loading riêng

        scheduleRepository.deleteSchedule(userId, myPlantId, scheduleId, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _operationResult.postValue("Đã xóa lịch trình thành công!");
                Log.d(TAG, "Schedule deleted successfully: " + scheduleId);
                // Sau khi xóa thành công, tải lại cả danh sách schedules và tasks để cập nhật UI
                loadPlantSchedulesAndTasks(); // <-- Tải lại để refresh danh sách

            }

            @Override
            public void onError(Exception e) {
                _operationResult.postValue("Lỗi xóa lịch trình: " + e.getMessage());
                Log.e(TAG, "Error deleting schedule: " + scheduleId, e);
                // _isLoadingSchedules.setValue(false); // Dừng loading nếu có cờ riêng
            }
        });
    }

    // Phương thức thêm lịch trình (Di chuyển từ Fragment)
    public void addSchedule(ScheduleModel schedule) {
        if (userId == null || myPlantId == null || schedule == null) {
            _operationResult.postValue("Lỗi: Dữ liệu lịch trình không hợp lệ.");
            Log.w(TAG, "Cannot add schedule: invalid input");
            return;
        }
        scheduleRepository.addSchedule(userId, myPlantId, schedule, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _operationResult.postValue("Đã thêm lịch trình mới!");
                Log.d(TAG, "Schedule added successfully.");
                // Sau khi thêm thành công, tải lại cả danh sách schedules và tasks để cập nhật UI
                loadPlantSchedulesAndTasks(); // <-- Tải lại để refresh danh sách
            }

            @Override
            public void onError(Exception e) {
                _operationResult.postValue("Lỗi thêm lịch trình: " + e.getMessage());
                Log.e(TAG, "Error adding schedule", e);
                // _isLoadingSchedules.setValue(false); // Dừng loading nếu có cờ riêng
            }
        });
    }

    // Phương thức cập nhật ảnh cây (Di chuyển từ Activity)
    public void updatePlantImage(String imageUrl) {
        if (userId == null || myPlantId == null || imageUrl == null || imageUrl.isEmpty()) {
            _operationResult.postValue("Lỗi: Thiếu thông tin để cập nhật ảnh.");
            _imageUpdateResult.postValue(false); // Báo cáo thất bại cụ thể
            Log.w(TAG, "Cannot update plant image: invalid input.");
            return;
        }
        // Có thể set cờ loading riêng cho thao tác cập nhật ảnh
        // _isLoadingDetails.setValue(true); // Hoặc một cờ loading riêng

        Map<String, Object> updates = new HashMap<>();
        updates.put("image", imageUrl); // Giả định "image" là field name trong Firestore

        myPlantRepository.updateMyPlant(userId, myPlantId, updates, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Plant image updated in Firestore successfully.");
                _operationResult.postValue("Cập nhật ảnh thành công!");
                _imageUpdateResult.postValue(true); // Báo cáo thành công cụ thể
                // Sau khi cập nhật, tải lại chi tiết cây để LiveData plantDetails được cập nhật URL ảnh mới
                loadPlantDetails(); // <-- Tải lại để refresh chi tiết

            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to update plant image in Firestore", e);
                _operationResult.postValue("Lỗi cập nhật ảnh: " + e.getMessage());
                _imageUpdateResult.postValue(false); // Báo cáo thất bại cụ thể
                // _isLoadingDetails.setValue(false); // Dừng loading nếu có cờ riêng
            }
        });
    }

    // Phương thức xóa cây chính (Di chuyển từ Activity)
    public void deletePlant() {
        if (userId == null || myPlantId == null) {
            _operationResult.postValue("Lỗi: Thiếu thông tin để xóa cây.");
            _plantDeletionResult.postValue(false); // Báo cáo thất bại cụ thể
            Log.w(TAG, "Cannot delete plant: userId or myPlantId is null.");
            return;
        }
        // Có thể set cờ loading riêng cho thao tác xóa cây
        // _isLoadingDetails.setValue(true); // Hoặc một cờ loading riêng

        myPlantRepository.deleteMyPlant(userId, myPlantId, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Plant deleted successfully.");
                _operationResult.postValue("Đã xóa cây.");
                _plantDeletionResult.postValue(true); // Báo cáo thành công cụ thể (để Activity finish)
                // Activity sẽ lắng nghe LiveData này và finish.

            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to delete plant", e);
                _operationResult.postValue("Lỗi xóa cây: " + e.getMessage());
                _plantDeletionResult.postValue(false); // Báo cáo thất bại cụ thể
                // _isLoadingDetails.setValue(false); // Dừng loading nếu có cờ riêng
            }
        });
    }

    // Phương thức để xóa thông báo operationResult (Giữ nguyên)
    public void clearOperationResult() {
        _operationResult.postValue(null);
    }

    // Phương thức để xóa kết quả cập nhật ảnh (để tránh lặp lại event)
    public void clearImageUpdateResult() {
        _imageUpdateResult.postValue(null);
    }

    // Phương thức để xóa kết quả xóa cây (để tránh lặp lại event)
    public void clearPlantDeletionResult() {
        _plantDeletionResult.postValue(null);
    }

}