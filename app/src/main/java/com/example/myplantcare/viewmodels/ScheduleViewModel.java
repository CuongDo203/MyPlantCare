package com.example.myplantcare.viewmodels;

import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myplantcare.data.repositories.ScheduleRepository;
import com.example.myplantcare.data.repositories.ScheduleRepositoryImpl;
import com.example.myplantcare.data.responses.ScheduleWithMyPlantInfo;
import com.example.myplantcare.models.ScheduleModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleViewModel extends ViewModel {
    private static final String TAG = "ScheduleViewModel";
    private String userId;
    private final ScheduleRepository scheduleRepository;
    private final MutableLiveData<Map<String, List<ScheduleWithMyPlantInfo>>> _todaySchedules = new MutableLiveData<>();
    public MutableLiveData<Map<String, List<ScheduleWithMyPlantInfo>>> todaySchedules = _todaySchedules;
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    // LiveData cho lịch trình CHƯA hoàn thành (nhóm theo Task)
    private final MutableLiveData<Map<String, List<ScheduleWithMyPlantInfo>>> _uncompletedSchedules = new MutableLiveData<>();
    public final LiveData<Map<String, List<ScheduleWithMyPlantInfo>>> uncompletedSchedules = _uncompletedSchedules;

    // LiveData cho lịch trình ĐÃ hoàn thành (nhóm theo Task)
    private final MutableLiveData<Map<String, List<ScheduleWithMyPlantInfo>>> _completedSchedules = new MutableLiveData<>();
    public final LiveData<Map<String, List<ScheduleWithMyPlantInfo>>> completedSchedules = _completedSchedules;

    // LiveData cho trạng thái loading khi đánh dấu/hủy hoàn thành
    private final MutableLiveData<Boolean> _isMarkingUnmarking = new MutableLiveData<>(false);
    public final LiveData<Boolean> isMarkingUnmarking = _isMarkingUnmarking;

    // LiveData cho kết quả các thao tác (mark/unmark) - Dùng để hiển thị Toast
    private final MutableLiveData<String> _operationResult = new MutableLiveData<>();
    public final LiveData<String> operationResult = _operationResult;

    // Lưu trữ ngày hiện tại mà ViewModel đang tải lịch trình cho
    private Calendar currentLoadedDate;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;
    public ScheduleViewModel(String userId) {
        this.userId = userId;
        scheduleRepository = new ScheduleRepositoryImpl();
//        fetchTodaySchedules(userId);
    }

    public void loadSchedulesForDate(Calendar date) {
        // Kiểm tra userId và date có hợp lệ không
        if (userId == null || userId.isEmpty() || date == null) {
            Log.w(TAG, "loadSchedulesForDate: invalid input. userId or date is null/empty.");
            _errorMessage.postValue("Lỗi: Không thể tải lịch trình do thiếu thông tin người dùng hoặc ngày.");
            _isLoading.postValue(false); // Đảm bảo tắt loading state
            return;
        }

        // Lưu trữ ngày đang được tải
        currentLoadedDate = (Calendar) date.clone();

        _isLoading.setValue(true); // Bật trạng thái loading

        // Gọi Repository để lấy lịch trình cho ngày cụ thể
        scheduleRepository.getSchedulesForDateGroupedByTask(userId, date, new FirestoreCallback<Pair<Map<String, List<ScheduleWithMyPlantInfo>>, Map<String, List<ScheduleWithMyPlantInfo>>>>() {
            @Override
            public void onSuccess(Pair<Map<String, List<ScheduleWithMyPlantInfo>>, Map<String, List<ScheduleWithMyPlantInfo>>> resultPair) {
                _isLoading.postValue(false); // Tắt trạng thái loading

                if (resultPair != null) {
                    // Cập nhật LiveData cho lịch trình chưa hoàn thành và đã hoàn thành
                    _uncompletedSchedules.postValue(resultPair.first != null ? resultPair.first : new HashMap<>());
                    _completedSchedules.postValue(resultPair.second != null ? resultPair.second : new HashMap<>());
                    Log.d(TAG, "Schedules loaded successfully for date: " + date.getTime());
                } else {
                    // Trường hợp resultPair là null (không mong đợi nếu repository thành công)
                    Log.w(TAG, "Repository returned null Pair for schedules.");
                    _uncompletedSchedules.postValue(new HashMap<>()); // Trả về map rỗng
                    _completedSchedules.postValue(new HashMap<>()); // Trả về map rỗng
                }
            }

            @Override
            public void onError(Exception e) {
                _isLoading.postValue(false); // Tắt trạng thái loading
                // Cập nhật LiveData lỗi
                _errorMessage.postValue("Lỗi khi tải lịch trình: " + e.getMessage());
                Log.e(TAG, "Error loading schedules for date: " + date.getTime(), e);
            }
        });
    }

    public void markScheduleCompleted(ScheduleWithMyPlantInfo scheduleItem) {
        // Kiểm tra tính hợp lệ của scheduleItem và userId, cũng như ngày đã load
        if (userId == null || userId.isEmpty() || scheduleItem == null || scheduleItem.getSchedule().getId() == null || scheduleItem.getSchedule().getId().isEmpty() || scheduleItem.getMyPlant() == null || scheduleItem.getMyPlant().getId() == null || scheduleItem.getMyPlant().getId().isEmpty() || currentLoadedDate == null) {
            Log.w(TAG, "markScheduleCompleted: invalid input. userId, scheduleItem, or currentLoadedDate is null/empty.");
            _operationResult.postValue("Lỗi: Không đủ thông tin để hoàn thành công việc.");
            return;
        }

        _isMarkingUnmarking.setValue(true); // Bật trạng thái loading cho thao tác

        // Gọi Repository để thêm task log
        scheduleRepository.markScheduleCompleted(userId, scheduleItem.getMyPlant().getId(), scheduleItem.getSchedule().getId(),scheduleItem.getTaskName(), currentLoadedDate, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _isMarkingUnmarking.postValue(false); // Tắt loading
                _operationResult.postValue("Đã hoàn thành công việc!"); // Thông báo thành công
                Log.d(TAG, "Schedule marked completed successfully. Schedule ID: " + scheduleItem.getSchedule().getId());

                // Tải lại lịch trình cho ngày hiện tại để cập nhật UI
                loadSchedulesForDate(currentLoadedDate);
            }

            @Override
            public void onError(Exception e) {
                _isMarkingUnmarking.postValue(false); // Tắt loading
                _operationResult.postValue("Lỗi khi hoàn thành công việc: " + e.getMessage()); // Thông báo lỗi
                Log.e(TAG, "Error marking schedule completed. Schedule ID: " + scheduleItem.getSchedule().getId(), e);
            }
        });
    }

    // --- Phương thức hoàn tác lịch trình đã hoàn thành ---
    public void unmarkScheduleCompleted(ScheduleWithMyPlantInfo scheduleItem) {
        // Kiểm tra tính hợp lệ tương tự markScheduleCompleted
        if (userId == null || userId.isEmpty() || scheduleItem == null || scheduleItem.getSchedule().getId() == null || scheduleItem.getSchedule().getId().isEmpty() || scheduleItem.getMyPlant() == null || scheduleItem.getMyPlant().getId() == null || scheduleItem.getMyPlant().getId().isEmpty() || currentLoadedDate == null) {
            Log.w(TAG, "unmarkScheduleCompleted: invalid input. userId, scheduleItem, or currentLoadedDate is null/empty.");
            _operationResult.postValue("Lỗi: Không đủ thông tin để hoàn tác công việc.");
            return;
        }

        _isMarkingUnmarking.setValue(true); // Bật trạng thái loading cho thao tác

        // Gọi Repository để xóa task log
        scheduleRepository.unmarkScheduleCompleted(userId, scheduleItem.getMyPlant().getId(), scheduleItem.getSchedule().getId(), currentLoadedDate, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _isMarkingUnmarking.postValue(false); // Tắt loading
                _operationResult.postValue("Đã hoàn tác công việc!"); // Thông báo thành công
                Log.d(TAG, "Schedule unmarked completed successfully. Schedule ID: " + scheduleItem.getSchedule().getId());

                // Tải lại lịch trình cho ngày hiện tại để cập nhật UI
                loadSchedulesForDate(currentLoadedDate);
            }

            @Override
            public void onError(Exception e) {
                _isMarkingUnmarking.postValue(false); // Tắt loading
                _operationResult.postValue("Lỗi khi hoàn tác công việc: " + e.getMessage()); // Thông báo lỗi
                Log.e(TAG, "Error unmarking schedule completed. Schedule ID: " + scheduleItem.getSchedule().getId(), e);
            }
        });
    }
    public void fetchTodaySchedules(String userId) {
        _isLoading.setValue(true);
        scheduleRepository.getTodaySchedulesGroupedByTask(userId, new FirestoreCallback<Map<String, List<ScheduleWithMyPlantInfo>>>() {
            @Override
            public void onSuccess(Map<String, List<ScheduleWithMyPlantInfo>> data) {
                _isLoading.postValue(false);
                _todaySchedules.postValue(data != null ? data : new HashMap<>());
            }

            @Override
            public void onError(Exception e) {
                _isLoading.postValue(false);
                _errorMessage.postValue("Lỗi khi tải lịch trình: " + e.getMessage());
            }
        });
    }
}
