package com.example.myplantcare.data.services;

import com.example.myplantcare.models.weathers.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    @GET("weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("q") String cityName,
            @Query("appid") String apiKey,
            @Query("units") String units,
            @Query("lang") String language);

}
