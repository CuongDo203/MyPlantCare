package com.example.myplantcare.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

public class PlantModel {
    @DocumentId
    private String id;
    private String name;
    private String speciesId;
    private String location;
    private String description;
    private String image;
    private String instruction;

    private String idealConditionId;

    public PlantModel() {
    }

    public PlantModel(String name, String speciesId, String location, String description, String instruction,
                      String image) {
        this.name = name;
        this.speciesId = speciesId;
        this.location = location;
        this.description = description;
        this.image = image;
        this.instruction = instruction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @PropertyName("speciesId")
    public String getSpeciesId() {
        return speciesId;
    }

    @PropertyName("speciesId")
    public void setSpeciesId(String speciesId) {
        this.speciesId = speciesId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("idealConditionId")
    public String getIdealConditionId() {
        return idealConditionId;
    }
    @PropertyName("idealConditionId")
    public void setIdealConditionId(String idealConditionId) {
        this.idealConditionId = idealConditionId;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
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
