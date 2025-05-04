package com.example.myplantcare.models;

import com.google.firebase.firestore.DocumentId;

public class User {
    private String name;
    @DocumentId
    private String id;
    private City city;
    private String email;
    private String phone;
    private String dob;
    private String role;
    private String avatar;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", city=" + city +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", dob='" + dob + '\'' +
                ", role='" + role + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }
}
