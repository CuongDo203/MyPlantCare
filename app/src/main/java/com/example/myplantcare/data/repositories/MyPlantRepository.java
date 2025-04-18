package com.example.myplantcare.data.repositories;

import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.List;

public interface MyPlantRepository {

    // Lấy tất cả cây của một user
    void getAllMyPlants(String userId, FirestoreCallback<List<MyPlantModel>> callback);
    // Hoặc trả về LiveData<List<MyPlant>> getAllMyPlants(String userId);

    // Lấy chi tiết một cây
    void getMyPlantById(String myPlantId, FirestoreCallback<MyPlantModel> callback);

    // Thêm cây mới
    void addMyPlant(String userId, MyPlantModel myPlant, FirestoreCallback<Void> callback);

    // Cập nhật cây
    void updateMyPlant(MyPlantModel myPlant, FirestoreCallback<Void> callback);

    // Xóa cây
    void deleteMyPlant(String userId, String myPlantId, FirestoreCallback<Void> callback);

    // Các hàm cho Growth, Note, CustomInstruction...
//    void getNotesForPlant(String myPlantId, FirestoreCallback<List<Note>> callback);
//    void addNote(Note note, FirestoreCallback<Void> callback);
}
