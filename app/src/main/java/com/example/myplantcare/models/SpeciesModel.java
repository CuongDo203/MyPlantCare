package com.example.myplantcare.models;

import com.google.firebase.firestore.DocumentId;

public class SpeciesModel {
    @DocumentId
    private String id;
    private String name;

    public SpeciesModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
