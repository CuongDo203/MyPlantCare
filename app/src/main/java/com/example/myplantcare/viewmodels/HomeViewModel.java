package com.example.myplantcare.viewmodels;

import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myplantcare.data.repositories.ScheduleRepository;
import com.example.myplantcare.data.repositories.ScheduleRepositoryImpl;
import com.example.myplantcare.data.repositories.WeatherRepository;
import com.example.myplantcare.data.repositories.WeatherRepositoryImpl;
import com.example.myplantcare.data.responses.ScheduleWithMyPlantInfo;
import com.example.myplantcare.models.weathers.WeatherResponse;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class HomeViewModel extends ViewModel {
    private static final String TAG = "HomeViewModel";
    private WeatherRepository weatherRepository;
    private final ScheduleRepository scheduleRepository;
    private MutableLiveData<WeatherResponse> _weatherData = new MutableLiveData<>();
    public LiveData<WeatherResponse> weatherData = _weatherData;
    private MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<List<ScheduleWithMyPlantInfo>> _todayUncompletedTasks = new MutableLiveData<>();
    public final LiveData<List<ScheduleWithMyPlantInfo>> todayUncompletedTasks = _todayUncompletedTasks;

    private final MutableLiveData<Boolean> _isLoadingWeather = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoadingWeather = _isLoadingWeather;
    private final MutableLiveData<Boolean> _isLoadingTasks = new MutableLiveData<>(false); // Loading state for tasks
    public final LiveData<Boolean> isLoadingTasks = _isLoadingTasks;
    private MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<String> _operationResult = new MutableLiveData<>();
    public final LiveData<String> operationResult = _operationResult;
    private String userId;

    public HomeViewModel() {
        this.weatherRepository = new WeatherRepositoryImpl();
        this.scheduleRepository = new ScheduleRepositoryImpl();
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void fetchWeatherData(String cityName, String apiKey, String units, String lang) {
        _isLoading.setValue(true); // Bắt đầu loading

        weatherRepository.getCurrentWeatherLiveData(cityName, apiKey, units, lang).observeForever(response -> {
            _isLoading.setValue(false); // Kết thúc loading
            if (response != null) {
                _weatherData.setValue(response);
            } else {
                _errorMessage.setValue("Không thể tải dữ liệu thời tiết"); // Hoặc lấy lỗi chi tiết hơn
            }
            // Cần remove observer này khi ViewModel bị cleared để tránh memory leak nếu Repo là singleton

        });
    }

    public void markTaskCompleted(ScheduleWithMyPlantInfo task) {
        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "markTaskCompleted: userId is null or empty. Cannot mark task.");
            _operationResult.postValue("Lỗi: Không thể hoàn thành công việc do thiếu thông tin người dùng.");
            return;
        }
        if (task == null || task.getSchedule().getId() == null || task.getSchedule().getId().isEmpty() ||
                task.getMyPlant() == null || task.getMyPlant().getId() == null || task.getMyPlant().getId().isEmpty()) {
            Log.w(TAG, "markTaskCompleted: invalid task data. Cannot mark task.");
            _operationResult.postValue("Lỗi: Thông tin công việc không hợp lệ.");
            return;
        }

        // Get today's date normalized to 0 hours
        Calendar todayZeroTime = Calendar.getInstance();
        todayZeroTime.set(Calendar.HOUR_OF_DAY, 0);
        todayZeroTime.set(Calendar.MINUTE, 0);
        todayZeroTime.set(Calendar.SECOND, 0);
        todayZeroTime.set(Calendar.MILLISECOND, 0);

        Log.d(TAG, "Attempting to mark task completed: " + task.getSchedule().getId() + " for myPlant: "
                + task.getMyPlant().getId() + " on date: " + todayZeroTime.getTime());

        // Call the repository method to mark the task completed
        scheduleRepository.markScheduleCompleted(userId, task.getMyPlant().getId(), task.getSchedule().getId(), todayZeroTime, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "Task marked completed successfully: " + task.getSchedule().getId());
                _operationResult.postValue("Đã hoàn thành công việc!");
                fetchTodayUncompletedTasks(userId); // Reload today's tasks
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error marking task completed: " + task.getSchedule().getId(), e);
                _errorMessage.postValue("Lỗi khi hoàn thành công việc: " + e.getMessage());
            }
        });
    }
    public void fetchTodayUncompletedTasks(String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "fetchTodayUncompletedTasks: userId is null or empty.");
            _errorMessage.postValue("Lỗi: Không thể tải danh sách công việc do thiếu thông tin người dùng.");
            _isLoadingTasks.postValue(false); // Ensure loading is off
            return;
        }

        _isLoadingTasks.setValue(true); // Start task loading

        Calendar today = Calendar.getInstance(); // Get today's date

        // Use the repository method to get schedules for today, grouped and split
        scheduleRepository.getSchedulesForDateGroupedByTask(userId, today, new FirestoreCallback<Pair<Map<String, List<ScheduleWithMyPlantInfo>>, Map<String, List<ScheduleWithMyPlantInfo>>>>() {
            @Override
            public void onSuccess(Pair<Map<String, List<ScheduleWithMyPlantInfo>>, Map<String, List<ScheduleWithMyPlantInfo>>> resultPair) {
                _isLoadingTasks.postValue(false); // Stop task loading

                List<ScheduleWithMyPlantInfo> uncompletedTasks = new ArrayList<>();
                if (resultPair != null && resultPair.first != null) {
                    for (Map.Entry<String, List<ScheduleWithMyPlantInfo>> entry : resultPair.first.entrySet()) {
                        String taskName = entry.getKey();
                        List<ScheduleWithMyPlantInfo> tasksForThisName = entry.getValue();
                        if (tasksForThisName != null) {
                            for (ScheduleWithMyPlantInfo task : tasksForThisName) {
                                task.setTaskName(taskName); // <--- GÁN GIÁ TRỊ VÀO ĐÂY
                                uncompletedTasks.add(task);
                            }
                        }
                    }
                    Log.d(TAG, "Fetched " + uncompletedTasks.size() + " uncompleted tasks for today.");
                } else {
                    Log.d(TAG, "No uncompleted tasks found for today or resultPair/first is null.");
                }
                // Cập nhật LiveData với danh sách phẳng
                _todayUncompletedTasks.postValue(uncompletedTasks);
            }

            @Override
            public void onError(Exception e) {
                _isLoadingTasks.postValue(false); // Stop task loading
                _errorMessage.postValue("Lỗi tải danh sách công việc: " + e.getMessage());
                Log.e(TAG, "Error fetching today's uncompleted tasks", e);
                // Optionally clear the task list on error
                // _todayUncompletedTasks.postValue(new ArrayList<>());
            }
        });
    }

    public void clearErrorMessage() {
        _errorMessage.postValue(null);
    }
}
