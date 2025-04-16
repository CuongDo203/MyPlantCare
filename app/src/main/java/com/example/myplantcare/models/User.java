package com.example.myplantcare.models;

public class User {
    private String name;
    private String id;
    private City city;

    public User() {
    }

    public User(String name, String id, City city) {
        this.name = name;
        this.id = id;
        this.city = city;
    }
}
