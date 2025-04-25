package com.example.myplantcare.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

    private void filterNotes(String query) {
        filteredNoteList.clear();
        String lowerCaseQuery = query.toLowerCase().trim();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Nếu không có từ khóa lọc, hiển thị tất cả
        if (TextUtils.isEmpty(lowerCaseQuery)) {
            List<NoteSectionItem> allNotes = categorizeNotes(originalNoteList);
            adapter.setNotes(allNotes);
            return;
        }

        for (Note note : originalNoteList) {
            String formattedDate = note.getDate().format(formatter);
            if (note.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                    note.getContent().toLowerCase().contains(lowerCaseQuery) ||
                    formattedDate.toLowerCase().contains(lowerCaseQuery)) {
                filteredNoteList.add(note);
            }
        }

        List<NoteSectionItem> filteredSections = categorizeNotes(filteredNoteList);
        adapter.setNotes(filteredSections);
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

                        if (title != null && summary != null && timestamp != null) {
                            LocalDate date = timestamp.toDate().toInstant()
                                    .atZone(ZoneId.systemDefault()).toLocalDate();
                            Note note = new Note(doc.getId(), title, summary, date);
                            noteList.add(note);
                        } else {
                            Log.w("NoteActivity", "Bỏ qua document thiếu dữ liệu: " + doc.getId());
                        }
                    }

                    originalNoteList.addAll(noteList);
                    filteredNoteList.addAll(noteList);

                    List<NoteSectionItem> categorizedNotes = categorizeNotes(noteList);
                    adapter.setNotes(categorizedNotes);
                })
                .addOnFailureListener(e -> {
                    Log.e("NoteActivity", "Lỗi khi tải dữ liệu từ Firestore: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private List<NoteSectionItem> categorizeNotes(List<Note> notes) {
        Map<String, List<Note>> groupedNotes = new HashMap<>();
        for (Note note : notes) {
            String section = getDateSection(note.getDate());
            groupedNotes.computeIfAbsent(section, k -> new ArrayList<>()).add(note);
        }

        List<String> sectionOrder = new ArrayList<>();
        sectionOrder.add("7 ngày trước");
        sectionOrder.add("30 ngày trước");

        // Nhóm theo năm giảm dần
        Set<String> remainingYears = new TreeSet<>((a, b) -> Integer.parseInt(b) - Integer.parseInt(a));
        for (String key : groupedNotes.keySet()) {
            if (!sectionOrder.contains(key)) {
                remainingYears.add(key);
            }
        }
        sectionOrder.addAll(remainingYears);

        List<NoteSectionItem> sectionItems = new ArrayList<>();
        for (String section : sectionOrder) {
            sectionItems.add(new NoteSectionItem(section));  // Header
            List<Note> noteGroup = groupedNotes.get(section);
            if (noteGroup != null) {
                // Sắp xếp ghi chú trong nhóm theo ngày giảm dần
                noteGroup.sort((n1, n2) -> n2.getDate().compareTo(n1.getDate()));
                for (Note note : noteGroup) {
                    sectionItems.add(new NoteSectionItem(note));  // Item
                }
            }
        }

        return sectionItems;
    }

    private String getDateSection(LocalDate date) {
        LocalDate today = LocalDate.now();
        long daysBetween = ChronoUnit.DAYS.between(date, today);

        if (daysBetween <= 7) {
            return "7 ngày trước";
        } else if (daysBetween <= 30) {
            return "30 ngày trước";
        } else {
            return String.valueOf(date.getYear());
        }
    }

}