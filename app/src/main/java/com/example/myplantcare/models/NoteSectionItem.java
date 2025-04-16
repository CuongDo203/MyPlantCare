package com.example.myplantcare.models;

public class NoteSectionItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NOTE = 1;

    private int type;
    private String headerTitle;
    private Note note;

    // Constructors
    public NoteSectionItem(String headerTitle) {
        this.type = TYPE_HEADER;
        this.headerTitle = headerTitle;
    }

    public NoteSectionItem(Note note) {
        this.type = TYPE_NOTE;
        this.note = note;
    }

    // Getters
    public int getType() {
        return type;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public Note getNote() {
        return note;
    }
}

