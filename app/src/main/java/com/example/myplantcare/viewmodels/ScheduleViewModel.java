package com.example.myplantcare.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myplantcare.data.repositories.ScheduleRepository;
import com.example.myplantcare.data.repositories.ScheduleRepositoryImpl;
import com.example.myplantcare.data.responses.ScheduleWithMyPlantInfo;
import com.example.myplantcare.models.ScheduleModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleViewModel extends ViewModel {

    private final ScheduleRepository scheduleRepository = new ScheduleRepositoryImpl();
    private final MutableLiveData<Map<String, List<ScheduleWithMyPlantInfo>>> _todaySchedules = new MutableLiveData<>();
    public MutableLiveData<Map<String, List<ScheduleWithMyPlantInfo>>> todaySchedules = _todaySchedules;
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;
    public ScheduleViewModel(String userId) {
        fetchTodaySchedules(userId);
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
