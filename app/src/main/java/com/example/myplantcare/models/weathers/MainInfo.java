package com.example.myplantcare.models.weathers;

import com.google.gson.annotations.SerializedName;

public class MainInfo {

    @SerializedName("temp")
    private Double temp;

    @SerializedName("humidity")
    private Integer humidity;

    @SerializedName("pressure")
    private Integer pressure;

    public Double getTemp() {
        return temp;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public Integer getPressure() {
        return pressure;
    }
}
