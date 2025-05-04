package com.example.myplantcare.data.repositories;

import androidx.lifecycle.LiveData;

import com.example.myplantcare.models.weathers.WeatherResponse;

public interface WeatherRepository {

    LiveData<WeatherResponse> getCurrentWeatherLiveData(String cityName, String apiKey, String units, String lang);
    LiveData<WeatherResponse> getCurrentWeatherLiveDataByCoordinates(double lat, double lon, String apiKey, String units, String lang);

}
