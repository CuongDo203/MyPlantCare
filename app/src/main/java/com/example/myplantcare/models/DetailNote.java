//package com.example.myplantcare.models;
//
//import android.net.Uri;
//
//public class DetailNote {
//    private Uri imageUri;     // Đường dẫn ảnh nếu có
//    private String noteText;  // Nội dung ghi chú
//
//    public DetailNote() {
//        this.imageUri = null;
//        this.noteText = "";
//    }
//
//    public DetailNote(Uri imageUri, String noteText) {
//        this.imageUri = imageUri;
//        this.noteText = noteText;
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
//
//    public boolean hasImage() {
//        return imageUri != null;
//    }
//}
// DO NOT DELETE

package com.example.myplantcare.models;

public class DetailNote {
    private String text;      // Nội dung ghi chú (mỗi dòng)
    private String imageUrl;  // URL hoặc đường dẫn ảnh tương ứng

    // Constructor mặc định (Firestore cần)
    public DetailNote() {
        this.text = "";
        this.imageUrl = "";
    }

    // Constructor đầy đủ
    public DetailNote(String text, String imageUrl) {
        this.text = text;
        this.imageUrl = imageUrl;
    }

    // Getter / Setter cho text
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    // Getter / Setter cho imageUrl
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Kiểm tra có ảnh hay không
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.isEmpty();
    }
}
