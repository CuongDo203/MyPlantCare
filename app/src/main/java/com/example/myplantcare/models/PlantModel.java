package com.example.myplantcare.models;

public class PlantModel {

    private String name;
    private String type;
    private String location;
    private String description;
    private String image;

    private String idealLight;
    private String idealTemperature;
    private String idealMoisture;
    private String idealWater;

    public PlantModel() {
    }

    public PlantModel(String name, String type, String location, String description,
                      String image, String idealLight, String idealTemperature, String idealMoisture, String idealWater) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.description = description;
        this.image = image;
        this.idealLight = idealLight;
        this.idealTemperature = idealTemperature;
        this.idealMoisture = idealMoisture;
        this.idealWater = idealWater;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getIdealLight() {
        return idealLight;
    }

    public void setIdealLight(String idealLight) {
        this.idealLight = idealLight;
    }

    public String getIdealTemperature() {
        return idealTemperature;
    }

    public void setIdealTemperature(String idealTemperature) {
        this.idealTemperature = idealTemperature;
    }

    public String getIdealMoisture() {
        return idealMoisture;
    }

    public void setIdealMoisture(String idealMoisture) {
        this.idealMoisture = idealMoisture;
    }

    public String getIdealWater() {
        return idealWater;
    }

    public void setIdealWater(String idealWater) {
        this.idealWater = idealWater;
    }
}
