package com.example.myplantcare.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class MyPlantModel {
    @DocumentId
    private String id; // Document ID từ Firestore
    private String nickname;
    private String location;
    private double progress; // Tiến độ phát triển (ví dụ: 0.0 - 1.0)
    private String plantId; // Tham chiếu đến Plant template ID
    private String userId; // Tham chiếu đến User ID

    @ServerTimestamp
    private Date createdAt; // Tự động lấy timestamp từ server khi tạo
    @ServerTimestamp
    private Date updatedAt; // Tự động lấy timestamp từ server khi cập nhật

    // Constructor rỗng cần thiết cho Firestore
    public MyPlantModel() {}

    public MyPlantModel(String nickname, String location, double progress) {
        this.nickname = nickname;
        this.location = location;
        this.progress = progress;
    }

    // Getters and Setters cho tất cả các trường...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    // ... các getters/setters khác
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public String getPlantId() { return plantId; }
    public void setPlantId(String plantId) { this.plantId = plantId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getProgress() { return progress; }
    public void setProgress(double progress) { this.progress = progress; }
}
