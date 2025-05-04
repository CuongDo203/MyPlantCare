//package com.example.myplantcare.models;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//
//public class Note {
//
//    private String id;
//    private String title;
//    private String content;
//    private LocalDate date;
//
//    private String myPlantId;
//
//    // Constructor nhận vào String ngày và chuyển thành LocalDate
//    public Note(String id, String title, String content, String dateString) {
//        this.id = id;
//        this.title = title;
//        this.content = content;
//        this.date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
//
//    }
//
//    // Constructor nhận vào LocalDate
//    public Note(String id, String title, String content, LocalDate date,String myPlantId) {
//        this.id = id;
//        this.title = title;
//        this.content = content;
//        this.date = date;
//        this.myPlantId = myPlantId;
//    }
//
//    // Getter và setter cho id
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    // Getter và setter cho title
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    // Getter và setter cho content
//    public String getContent() {
//        return content;
//    }
//
//    public void setContent(String content) {
//        this.content = content;
//    }
//
//    // Getter và setter cho date
//    public LocalDate getDate() {
//        return date;
//    }
//
//    public void setDate(LocalDate date) {
//        this.date = date;
//    }
//
//    public String getMyPlantId() {
//        return myPlantId;
//    }
//
//    public void setMyPlantId(String myPlantId) {
//        this.myPlantId = myPlantId;
//    }
//    // Phương thức trả về ngày dạng chuỗi
//    public String getFormattedDate() {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        return this.date.format(formatter);
//    }
//}
// DO NOT DELETE


package com.example.myplantcare.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Note {
    private String id;
    private String title;
    private List<DetailNote> content;  // ← thay đổi từ String thành List<DetailNote>
    private LocalDate date;
    private String myPlantId;

    // Constructor mặc định (Firestore cần)
    public Note() {
        this.content = new ArrayList<>();
    }

    // Constructor nhận vào ngày dạng String, content rỗng
    public Note(String id, String title, String dateString) {
        this.id = id;
        this.title = title;
        this.date = LocalDate.parse(dateString,
                DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        this.content = new ArrayList<>();
    }

    // Constructor đầy đủ với LocalDate và myPlantId
    public Note(String id,
                String title,
                List<DetailNote> content,
                LocalDate date,
                String myPlantId) {
        this.id = id;
        this.title = title;
        this.content = content != null ? content : new ArrayList<>();
        this.date = date;
        this.myPlantId = myPlantId;
    }

    // --- Getters & Setters ---

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public List<DetailNote> getContent() {
        return content;
    }
    public void setContent(List<DetailNote> content) {
        this.content = content;
    }

    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMyPlantId() {
        return myPlantId;
    }
    public void setMyPlantId(String myPlantId) {
        this.myPlantId = myPlantId;
    }

    // Trả về ngày dạng chuỗi
    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return this.date.format(formatter);
    }
}
