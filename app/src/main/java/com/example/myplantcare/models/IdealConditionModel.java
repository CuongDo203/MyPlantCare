package com.example.myplantcare.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

public class IdealConditionModel {
    @DocumentId
    private String id;

    private Double temperatureMin;
    private Double temperatureMax;

    private Double moistureMin;

    private Double moistureMax;
    private Double waterMin;
    private Double waterMax;
    private String light;

    public String getId() {
        return id;
    }
    @PropertyName("temperature_min")
    public Double getTemperatureMin() {
        return temperatureMin;
    }
    @PropertyName("temperature_max")
    public Double getTemperatureMax() {
        return temperatureMax;
    }
    @PropertyName("moisture_min")
    public Double getMoistureMin() {
        return moistureMin;
    }
    @PropertyName("moisture_max")
    public Double getMoistureMax() {
        return moistureMax;
    }
    @PropertyName("water_min")
    public Double getWaterMin() {
        return waterMin;
    }
    @PropertyName("water_max")
    public Double getWaterMax() {
        return waterMax;
    }
    @PropertyName("light")
    public String getLight() {
        return light;
    }

    public void setId(String id) {
        this.id = id;
    }
    @PropertyName("temperature_min")
    public void setTemperatureMin(Double temperatureMin) {
        this.temperatureMin = temperatureMin;
    }

    @PropertyName("temperature_max")
    public void setTemperatureMax(Double temperatureMax) {
        this.temperatureMax = temperatureMax;
    }

    @PropertyName("moisture_min")
    public void setMoistureMin(Double moistureMin) {
        this.moistureMin = moistureMin;
    }

    @PropertyName("moisture_max")
    public void setMoistureMax(Double moistureMax) {
        this.moistureMax = moistureMax;
    }

    @PropertyName("water_min")
    public void setWaterMin(Double waterMin) {
        this.waterMin = waterMin;
    }
    @PropertyName("water_max")
    public void setWaterMax(Double waterMax) {
        this.waterMax = waterMax;
    }

    @PropertyName("light")
    public void setLight(String light) {
        this.light = light;
    }

    @Override
    public String toString() {
        return "IdealConditionModel{" +
                "id='" + id + '\'' +
                ", temperatureMin=" + temperatureMin +
                ", temperatureMax=" + temperatureMax +
                ", moistureMin=" + moistureMin +
                ", moistureMax=" + moistureMax +
                ", waterMin=" + waterMin +
                ", waterMax=" + waterMax +
                ", light='" + light + '\'' +
                '}';
    }
}
