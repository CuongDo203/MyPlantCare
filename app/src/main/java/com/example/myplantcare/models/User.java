package com.example.myplantcare.models;

public class User {
    private String name;
    private String id;
    private City city;
    private String email;
    private String phone;
    private String dob;
    private String role;
    public User() {
    }
    public User(String name, String id, City city, String email, String phone, String dob, String role) {
        this.name = name;
        this.id = id;
        this.city = city;
        this.email = email;
        this.phone = phone;
        this.dob = dob;
        this.role = role;
    }
    public User(String name, String id, String email) {
        this.name = name;
        this.id = id;
        this.email = email;
    }
}
