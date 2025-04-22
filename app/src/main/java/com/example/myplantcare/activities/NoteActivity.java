//package com.example.myplantcare.activities;
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.util.Log;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.myplantcare.R;
//import com.example.myplantcare.adapters.NoteAdapter;
//import com.example.myplantcare.fragments.SelectPlantDialog;
//import com.example.myplantcare.models.Note;
//import com.example.myplantcare.models.NoteSectionItem;
//import com.google.firebase.Timestamp;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.QueryDocumentSnapshot;
//
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.time.temporal.ChronoUnit;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//
//public class NoteActivity extends AppCompatActivity {
//
//    FirebaseFirestore db;
//
//    private List<Note> originalNoteList = new ArrayList<>();
//    private List<Note> filteredNoteList = new ArrayList<>();
//    private RecyclerView recyclerView;
//    private NoteAdapter adapter;
//    private final List<Note> noteList = new ArrayList<>();
//
//    private TextView toolbarTitle;
//    private ImageButton toolbarBackButton;
//    private EditText etSearchNote;
//
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_note); // layout mới không phụ thuộc MainActivity
////
////
////        db = FirebaseFirestore.getInstance();
////        loadNotesFromFirestore();
////
////
////        etSearchNote = findViewById(R.id.etSearchNote);
////        etSearchNote.addTextChangedListener(new TextWatcher() {
////            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
////            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
////                filterNotes(s.toString());
////            }
////            @Override public void afterTextChanged(Editable s) {}
////        });
////
////        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.insider_toolbar);
////        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
////        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);
////        toolbarTitle.setText("Ghi chú");
////        toolbarBackButton.setOnClickListener(v -> finish());
////
////        recyclerView = findViewById(R.id.recyclerViewNotes);
////        recyclerView.setLayoutManager(new LinearLayoutManager(this));
////
////        adapter = new NoteAdapter(this, filteredNoteList);
////        recyclerView.setAdapter(adapter);
////
////        adapter.setOnItemClickListener(note -> {
////            Intent intent = new Intent(NoteActivity.this, DetailNoteActivity.class);
//////            intent.putExtra("noteTitle", note.getTitle());
//////            intent.putExtra("noteContent", note.getContent());
//////            intent.putExtra("noteDate", note.getDate().toString());
////            intent.putExtra("noteId", note.getId());
////            startActivity(intent);
////        });
//
////        Button btnCreateNote = findViewById(R.id.btnCreateNote);
////        btnCreateNote.setOnClickListener(v -> {
////            showSelectPlantDialog();
//////            Intent intent = new Intent(NoteActivity.this, DetailNoteActivity.class);
//////            intent.putExtra("noteTitle", "");
//////            intent.putExtra("noteContent", "");
//////            intent.putExtra("noteDate", LocalDate.now().toString());
//////            startActivity(intent);
////        });
////    }
//
//
//protected void onCreate(Bundle savedInstanceState) {
//    super.onCreate(savedInstanceState);
//    setContentView(R.layout.activity_note);
//
//    db = FirebaseFirestore.getInstance();
//    loadNotesFromFirestore();
//
//    etSearchNote = findViewById(R.id.etSearchNote);
//    etSearchNote.addTextChangedListener(new TextWatcher() {
//        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
//            filterNotes(s.toString());
//        }
//        @Override public void afterTextChanged(Editable s) {}
//    });
//
//    androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.insider_toolbar);
//    toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
//    toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);
//    toolbarTitle.setText("Ghi chú");
//    toolbarBackButton.setOnClickListener(v -> finish());
//
//    recyclerView = findViewById(R.id.recyclerViewNotes);
//    recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//    adapter = new NoteAdapter(this, filteredNoteList);
//    recyclerView.setAdapter(adapter);
//
//    adapter.setOnItemClickListener(note -> {
//        Intent intent = new Intent(NoteActivity.this, DetailNoteActivity.class);
//        intent.putExtra("noteId", note.getId());
//        startActivity(intent);
//    });
//
//    Button btnCreateNote = findViewById(R.id.btnCreateNote);
//    btnCreateNote.setOnClickListener(v -> {
//        showSelectPlantDialog();
//    });
//}
//
//
//    private void showSelectPlantDialog() {
//        SelectPlantDialog selectPlantDialog = new SelectPlantDialog();
//        selectPlantDialog.show(getSupportFragmentManager(), "SelectPlantDialog");
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    private void filterNotes(String query) {
//        filteredNoteList.clear();
//        String lowerCaseQuery = query.toLowerCase().trim();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//
//        for (Note note : originalNoteList) {
//            String formattedDate = note.getDate().format(formatter);
//            if (note.getTitle().toLowerCase().contains(lowerCaseQuery) ||
//                    note.getContent().toLowerCase().contains(lowerCaseQuery) ||
//                    formattedDate.toLowerCase().contains(lowerCaseQuery)) {
//                filteredNoteList.add(note);
//            }
//        }
//        adapter.notifyDataSetChanged();
//    }
//
//
//    @SuppressLint("NotifyDataSetChanged")
//    private void loadNotesFromFirestore() {
//        db.collection("notes")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    Log.d("NoteActivity", "Tải dữ liệu thành công. Số lượng: " + queryDocumentSnapshots.size());
//
//                    noteList.clear();
//                    originalNoteList.clear();
//                    filteredNoteList.clear();
//
//                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
//                        String title = doc.getString("title");
//                        String summary = doc.getString("summary");
//                        Timestamp timestamp = doc.getTimestamp("lastUpdated");
//                        String dateStr = null;
//
//                        // Nếu có timestamp, chuyển thành định dạng ngày
//                        if (timestamp != null) {
//                            Date date = timestamp.toDate();
//                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//                            dateStr = sdf.format(date);
//                        }
//
//                        Log.d("NoteActivity", "Document: " + doc.getId() + ", title=" + title + ", summary=" + summary + ", date=" + dateStr);
//
//                        // Kiểm tra nếu các trường không rỗng
//                        if (title != null && summary != null && dateStr != null) {
//                            // Sử dụng DateTimeFormatter để phân tích chuỗi ngày
//                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Định dạng ngày cho LocalDate
//                            LocalDate date = LocalDate.parse(dateStr, formatter); // Phân tích ngày
//                            Note note = new Note(doc.getId(), title, summary, date); // Chuyển vào đối tượng Note
//                            noteList.add(note);
//                        } else {
//                            Log.w("NoteActivity", "Bỏ qua document thiếu dữ liệu: " + doc.getId());
//                        }
//                    }
//
//                    // Cập nhật danh sách ghi chú
//                    originalNoteList.addAll(noteList);
//                    filteredNoteList.addAll(noteList);
//                    adapter.notifyDataSetChanged();
//
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("NoteActivity", "Lỗi khi tải dữ liệu từ Firestore: " + e.getMessage(), e);
//                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }
////
////    @SuppressLint("NotifyDataSetChanged")
////    private void loadNotesFromFirestore() {
////        db.collection("notes")
////                .get()
////                .addOnSuccessListener(queryDocumentSnapshots -> {
////                    Log.d("NoteActivity", "Tải dữ liệu thành công. Số lượng: " + queryDocumentSnapshots.size());
////
////                    noteList.clear();
////                    originalNoteList.clear();
////                    filteredNoteList.clear();
////
////                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
////                        String title = doc.getString("title");
////                        String summary = doc.getString("summary");
////                        Timestamp timestamp = doc.getTimestamp("lastUpdated");
////                        String dateStr = null;
////
////                        if (timestamp != null) {
////                            Date date = timestamp.toDate();
////                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
////                            dateStr = sdf.format(date);
////                        }
////
////                        Log.d("NoteActivity", "Document: " + doc.getId() + ", title=" + title + ", summary=" + summary + ", date=" + dateStr);
////
////                        if (title != null && summary != null && dateStr != null) {
////                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
////                            LocalDate date = LocalDate.parse(dateStr, formatter);
////                            Note note = new Note(doc.getId(), title, summary, date);
////                            noteList.add(note);
////                        } else {
////                            Log.w("NoteActivity", "Bỏ qua document thiếu dữ liệu: " + doc.getId());
////                        }
////                    }
////
////                    // Phân loại ghi chú và cập nhật adapter
////                    List<NoteSectionItem> categorizedNotes = categorizeNotes(noteList);
////                    adapter.setNotes(categorizedNotes);
////
////                })
////                .addOnFailureListener(e -> {
////                    Log.e("NoteActivity", "Lỗi khi tải dữ liệu từ Firestore: " + e.getMessage(), e);
////                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
////                });
////    }
////
////
////    private List<NoteSectionItem> categorizeNotes(List<Note> noteList) {
////        Map<String, List<Note>> groupedNotes = new HashMap<>();
////
////        // Phân loại ghi chú theo ngày
////        for (Note note : noteList) {
////            String section = getDateSection(note.getDate()); // Phân loại theo ngày hoặc năm
////            if (!groupedNotes.containsKey(section)) {
////                groupedNotes.put(section, new ArrayList<>());
////            }
////            groupedNotes.get(section).add(note);
////        }
////
////        // Tạo danh sách NoteSectionItem từ các nhóm ghi chú
////        List<NoteSectionItem> noteSectionItems = new ArrayList<>();
////
////        for (String section : groupedNotes.keySet()) {
////            // Thêm header (tiêu đề nhóm)
////            noteSectionItems.add(new NoteSectionItem(section));
////
////            // Thêm ghi chú trong nhóm
////            for (Note note : groupedNotes.get(section)) {
////                noteSectionItems.add(new NoteSectionItem(note));
////            }
////        }
////
////        return noteSectionItems;
////    }
////
////    private String getDateSection(LocalDate date) {
////        LocalDate today = LocalDate.now();
////        long daysBetween = ChronoUnit.DAYS.between(date, today);
////
////        if (daysBetween <= 7) {
////            return "7 ngày trước";
////        } else if (daysBetween <= 30) {
////            return "30 ngày trước";
////        } else if (date.getYear() == today.getYear()) {
////            return String.valueOf(date.getYear()); // Nhóm theo năm nếu trong năm hiện tại
////        } else {
////            return date.getYear() + ""; // Nhóm theo năm nếu không phải trong năm hiện tại
////        }
////    }
//}



package com.example.myplantcare.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantcare.R;
import com.example.myplantcare.adapters.NoteAdapter;
import com.example.myplantcare.fragments.SelectPlantDialog;
import com.example.myplantcare.models.Note;
import com.example.myplantcare.models.NoteSectionItem;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class NoteActivity extends AppCompatActivity {

    FirebaseFirestore db;

    private List<Note> originalNoteList = new ArrayList<>();
    private List<Note> filteredNoteList = new ArrayList<>();
    private final List<Note> noteList = new ArrayList<>();

    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private TextView toolbarTitle;
    private ImageButton toolbarBackButton;
    private EditText etSearchNote;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        db = FirebaseFirestore.getInstance();
        loadNotesFromFirestore();

        etSearchNote = findViewById(R.id.etSearchNote);
        etSearchNote.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.insider_toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);
        toolbarTitle.setText("Ghi chú");
        toolbarBackButton.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(note -> {
            Intent intent = new Intent(NoteActivity.this, DetailNoteActivity.class);
            intent.putExtra("noteId", note.getId());
            startActivity(intent);
        });

        Button btnCreateNote = findViewById(R.id.btnCreateNote);
        btnCreateNote.setOnClickListener(v -> showSelectPlantDialog());
    }

    private void showSelectPlantDialog() {
        SelectPlantDialog selectPlantDialog = new SelectPlantDialog();
        selectPlantDialog.show(getSupportFragmentManager(), "SelectPlantDialog");
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterNotes(String query) {
        filteredNoteList.clear();
        String lowerCaseQuery = query.toLowerCase().trim();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Note note : originalNoteList) {
            String formattedDate = note.getDate().format(formatter);
            if (note.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                    note.getContent().toLowerCase().contains(lowerCaseQuery) ||
                    formattedDate.toLowerCase().contains(lowerCaseQuery)) {
                filteredNoteList.add(note);
            }
        }

        List<NoteSectionItem> filteredNoteSectionList = categorizeNotes(filteredNoteList);
        adapter.setNotes(filteredNoteSectionList); // ✅ cập nhật giao diện phân nhóm
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadNotesFromFirestore() {
        db.collection("notes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("NoteActivity", "Tải dữ liệu thành công. Số lượng: " + queryDocumentSnapshots.size());

                    noteList.clear();
                    originalNoteList.clear();
                    filteredNoteList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String title = doc.getString("title");
                        String summary = doc.getString("summary");
                        Timestamp timestamp = doc.getTimestamp("lastUpdated");
                        String dateStr = null;

                        if (timestamp != null) {
                            Date date = timestamp.toDate();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            dateStr = sdf.format(date);
                        }

                        if (title != null && summary != null && dateStr != null) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            LocalDate date = LocalDate.parse(dateStr, formatter);
                            Note note = new Note(doc.getId(), title, summary, date);
                            noteList.add(note);
                        } else {
                            Log.w("NoteActivity", "Bỏ qua document thiếu dữ liệu: " + doc.getId());
                        }
                    }

                    originalNoteList.addAll(noteList);
                    filteredNoteList.addAll(noteList);

                    List<NoteSectionItem> categorizedNotes = categorizeNotes(noteList);
                    adapter.setNotes(categorizedNotes); // ✅ hiển thị dữ liệu ban đầu theo nhóm
                })
                .addOnFailureListener(e -> {
                    Log.e("NoteActivity", "Lỗi khi tải dữ liệu từ Firestore: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private List<NoteSectionItem> categorizeNotes(List<Note> noteList) {
        Map<String, List<Note>> groupedNotes = new HashMap<>();

        for (Note note : noteList) {
            String section = getDateSection(note.getDate());
            groupedNotes.computeIfAbsent(section, k -> new ArrayList<>()).add(note);
        }

        List<NoteSectionItem> noteSectionItems = new ArrayList<>();
        for (String section : groupedNotes.keySet()) {
            noteSectionItems.add(new NoteSectionItem(section)); // header
            for (Note note : Objects.requireNonNull(groupedNotes.get(section))) {
                noteSectionItems.add(new NoteSectionItem(note)); // item
            }
        }
        return noteSectionItems;
    }

    private String getDateSection(LocalDate date) {
        LocalDate today = LocalDate.now();
        long daysBetween = ChronoUnit.DAYS.between(date, today);

        if (daysBetween <= 7) {
            return "7 ngày trước";
        } else if (daysBetween <= 30) {
            return "30 ngày trước";
        } else if (date.getYear() == today.getYear()) {
            return String.valueOf(date.getYear());
        } else {
            return String.valueOf(date.getYear());
        }
    }
}
