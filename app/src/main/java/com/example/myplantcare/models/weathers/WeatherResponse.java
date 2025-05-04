package com.example.myplantcare.models.weathers;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResponse {

    @SerializedName("weather")
    private List<WeatherDescription> weather;

    @SerializedName("main")
    private MainInfo main;

    @SerializedName("wind")
    private WindInfo wind;

    @SerializedName("name")
    private String cityName;
    public WeatherResponse(List<WeatherDescription> weather, MainInfo main, WindInfo wind, String cityName) {
        this.weather = weather;
        this.main = main;
        this.wind = wind;
        this.cityName = cityName;
    }

    public List<WeatherDescription> getWeather() {
        return weather;
    }

    public MainInfo getMain() {
        return main;
    }

    public WindInfo getWind() {
        return wind;
    }

    public String getCityName() {
        return cityName;
    }
}
