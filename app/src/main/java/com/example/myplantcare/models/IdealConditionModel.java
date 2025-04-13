package com.example.myplantcare.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

public class IdealConditionModel {
    @DocumentId
    private String id;
    @PropertyName("temperature_min")
    private Double temperatureMin;
    @PropertyName("temperature_max")
    private Double temperatureMax;
    @PropertyName("moisture_min")
    private Double moistureMin;
    @PropertyName("moisture_max")
    private Double moistureMax;
    @PropertyName("water_min")
    private Double waterMin;
    @PropertyName("water_max")
    private Double waterMax;
    @PropertyName("light")
    private String light;

    public String getId() {
        return id;
    }

    public Double getTemperatureMin() {
        return temperatureMin;
    }

    public Double getTemperatureMax() {
        return temperatureMax;
    }

    public Double getMoistureMin() {
        return moistureMin;
    }

    public Double getMoistureMax() {
        return moistureMax;
    }

    public Double getWaterMin() {
        return waterMin;
    }

    public Double getWaterMax() {
        return waterMax;
    }

    public String getLight() {
        return light;
    }
}
