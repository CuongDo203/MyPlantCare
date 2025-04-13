package com.example.myplantcare.data.responses;

import com.example.myplantcare.models.IdealConditionModel;
import com.example.myplantcare.models.PlantModel;
import com.example.myplantcare.models.SpeciesModel;

public class PlantResponse {

    private PlantModel plant;
    private SpeciesModel species;
    private IdealConditionModel idealCondition;

    public PlantResponse(PlantModel plant, SpeciesModel species, IdealConditionModel idealCondition) {
        this.plant = plant;
        this.species = species;
        this.idealCondition = idealCondition;
    }

    public PlantModel getPlant() {
        return plant;
    }

    public SpeciesModel getSpecies() {
        return species;
    }

    public IdealConditionModel getIdealCondition() {
        return idealCondition;
    }

    public String getPlantName() {
        return plant != null ? plant.getName() : "N/A";
    }

    public String getSpeciesName() {
        return species != null ? species.getName() : "N/A";
    }

    public String getPlantImage() {
        return plant != null ? plant.getImage() : null;
    }

    public Double getIdealTemperatureMin() {
        return idealCondition != null ? idealCondition.getTemperatureMin() : 0;
    }

    public Double getIdealTemperatureMax() {
        return idealCondition != null ? idealCondition.getTemperatureMax() : 0;

    }

    public Double getIdealMoistureMin() {
        return idealCondition != null ? idealCondition.getMoistureMin() : 0;
    }

    public Double getIdealMoistureMax() {
        return idealCondition != null ? idealCondition.getMoistureMax() : 0;
    }

    public Double getIdealWaterMin() {
        return idealCondition != null ? idealCondition.getWaterMin() : 0;
    }

    public Double getIdealWaterMax() {
        return idealCondition != null ? idealCondition.getWaterMax() : 0;
    }

    public String getIdealLight() {
        return idealCondition != null ? idealCondition.getLight() : "N/A";
    }
}
