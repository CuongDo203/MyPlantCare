package com.example.myplantcare.models;

public class PlantModel {

    private String name;
    private String speciesId;
    private String location;
    private String description;
    private String image;

    private String id;
    private String idealConditionId;

    public PlantModel() {
    }

    public PlantModel(String name, String speciesId, String location, String description,
                      String image) {
        this.name = name;
        this.speciesId = speciesId;
        this.location = location;
        this.description = description;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(String speciesId) {
        this.speciesId = speciesId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdealConditionId() {
        return idealConditionId;
    }

    public void setIdealConditionId(String idealConditionId) {
        this.idealConditionId = idealConditionId;
    }

    public String getType() {
        return speciesId;
    }

    public void setType(String type) {
        this.speciesId = type;
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

}
