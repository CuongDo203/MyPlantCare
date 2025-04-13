package com.example.myplantcare.data.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myplantcare.data.services.ApiClient;
import com.example.myplantcare.data.services.WeatherApiService;
import com.example.myplantcare.models.weathers.WeatherResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepositoryImpl implements WeatherRepository{

    private WeatherApiService apiService;
    public WeatherRepositoryImpl() {
        this.apiService = ApiClient.getClient().create(WeatherApiService.class);
    }

    @Override
    public LiveData<WeatherResponse> getCurrentWeatherLiveData(String cityName, String apiKey, String units, String lang) {
        MutableLiveData<WeatherResponse> data = new MutableLiveData<>();
        apiService.getCurrentWeather(cityName, apiKey, units, lang)
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if(response.isSuccessful()) {
                            data.setValue(response.body());
                        }
                        else {
                            data.setValue(null);
                            Log.e("Repo", "ERROR: "+ response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable throwable) {
                        data.setValue(null); // Hoặc tạo một lớp Result/Resource để chứa cả lỗi
                        Log.e("Repo", "API Failure: " + throwable.getMessage());
                    }
                });
        return data;
    }
}
