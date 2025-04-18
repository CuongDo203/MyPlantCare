package com.example.myplantcare.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class MyPlantModel {
    @DocumentId
    private String id; // Document ID từ Firestore
    private String nickname;
    private String location;
    private double progress; // Tiến độ phát triển (ví dụ: 0.0 - 1.0)
    private String plantId; // Tham chiếu đến Plant template ID
    @Exclude
    private String userId; // Tham chiếu đến User ID

    private String speciesId;
    private String image;

    @ServerTimestamp
    private Timestamp createdAt; // Tự động lấy timestamp từ server khi tạo
    @ServerTimestamp
    private Timestamp updatedAt; // Tự động lấy timestamp từ server khi cập nhật

    // Constructor rỗng cần thiết cho Firestore
    public MyPlantModel() {}

    public MyPlantModel(String nickname, String location, String speciesId, String image) {
        this.nickname = nickname;
        this.location = location;
        this.speciesId = speciesId;
        this.image = image;
    }

    // Getters and Setters cho tất cả các trường...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    // ... các getters/setters khác

    @PropertyName("created_at")
    public Timestamp getCreatedAt() { return createdAt; }
    @PropertyName("created_at")
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    @PropertyName("updated_at")
    public Timestamp getUpdatedAt() { return updatedAt; }
    @PropertyName("updated_at")
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public String getPlantId() { return plantId; }
    public void setPlantId(String plantId) { this.plantId = plantId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getProgress() { return progress; }
    public void setProgress(double progress) { this.progress = progress; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}
