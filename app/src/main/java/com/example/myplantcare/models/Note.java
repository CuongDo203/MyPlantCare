package com.example.myplantcare.models;

import android.os.Build;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
// Trong class Note.java
import java.time.LocalDate;

public class Note {
    private String title;
    private String content;
    private LocalDate date;
    private String date2;

    // Constructor hiện tại của bạn (có thể có hoặc không)
    public Note(String title, String content, String dateString) {
        this.title = title;
        this.content = content;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }

    // Constructor mới nhận LocalDate
    public Note(String title, String content, LocalDate date) {
        this.title = title;
        this.content = content;
        this.date = date;
    }

    // Getter cho date
    public LocalDate getDate() {
        return date;
    }

    // Setter cho date (nếu cần)
    public void setDate(LocalDate date) {
        this.date = date;
    }

    // Getter và setter cho title và content (đã có hoặc thêm vào)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
