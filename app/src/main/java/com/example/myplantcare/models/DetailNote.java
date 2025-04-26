//package com.example.myplantcare.models;
//import android.net.Uri;
//
//public class DetailNote {
//    private Uri imageUri;
//    private String noteText;
//
//    public DetailNote() {
//        this.imageUri = null;
//        this.noteText = "";
//    }
//
//    public DetailNote(Uri parse, String s) {
//    }
//
//    public Uri getImageUri() {
//        return imageUri;
//    }
//
//    public void setImageUri(Uri imageUri) {
//        this.imageUri = imageUri;
//    }
//
//    public String getNoteText() {
//        return noteText;
//    }
//
//    public void setNoteText(String noteText) {
//        this.noteText = noteText;
//    }
//}

package com.example.myplantcare.models;

import android.net.Uri;

public class DetailNote {
    private Uri imageUri;     // Đường dẫn ảnh nếu có
    private String noteText;  // Nội dung ghi chú

    public DetailNote() {
        this.imageUri = null;
        this.noteText = "";
    }

    public DetailNote(Uri imageUri, String noteText) {
        this.imageUri = imageUri;
        this.noteText = noteText;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public boolean hasImage() {
        return imageUri != null;
    }
}

