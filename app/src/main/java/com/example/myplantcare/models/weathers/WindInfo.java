package com.example.myplantcare.models.weathers;

import com.google.gson.annotations.SerializedName;

public class WindInfo {

    @SerializedName("deg")
    private Integer deg;

    @SerializedName("speed")
    private Double speed;

    public Integer getDeg() {
        return deg;
    }

    public Double getSpeed() {
        return speed;
    }
}
