package com.example.myplantcare.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myplantcare.data.repositories.WeatherRepository;
import com.example.myplantcare.data.repositories.WeatherRepositoryImpl;
import com.example.myplantcare.models.weathers.WeatherResponse;

public class HomeViewModel extends ViewModel {
    private WeatherRepository weatherRepository;
    private MutableLiveData<WeatherResponse> _weatherData = new MutableLiveData<>();
    public LiveData<WeatherResponse> weatherData = _weatherData; // Fragment sẽ observe cái này
    private MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    public HomeViewModel() {
        this.weatherRepository = new WeatherRepositoryImpl();
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
}
