package com.example.myplantcare.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantcare.R;
import com.example.myplantcare.adapters.NoteAdapter;
import com.example.myplantcare.fragments.SelectPlantDialog;
import com.example.myplantcare.models.Note;
import com.example.myplantcare.models.NoteSectionItem;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    private TextView emptyTextView;

    private int totalPlantCount = 0;
    private int loadedPlantCount = 0;

    private ListenerRegistration notesListener;

    private ProgressBar progressBar;

    private String userId;
    private String myPlantId;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        emptyTextView = findViewById(R.id.emptyTextView);

        db = FirebaseFirestore.getInstance();

        progressBar = findViewById(R.id.progressBar);

        // 👉 Lấy userId và myPlantsId từ Intent
        userId = getIntent().getStringExtra("userId");
        Log.d("NoteActivity", "userId nhận được: " + userId);
        myPlantId = getIntent().getStringExtra("Id");
        Log.d("NoteActivity", "myPlantsId nhận được: " + myPlantId);

        if (myPlantId == null) {
            startListeningToNotes();
        } else {
            startListeningToNotes(); // Cả hai đều dùng startListeningToNotes
        }



        etSearchNote = findViewById(R.id.etSearchNote);
        etSearchNote.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        etSearchNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                filterNotes(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
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

    private void startListeningToNotes() {
        if (userId == null) {
            Log.e("NoteActivity", "User ID null, không thể tải ghi chú.");
            return;
        }

        noteList.clear(); // Xoá cũ để load mới
        totalPlantCount = 0;
        loadedPlantCount = 0;

        if (myPlantId != null) {
            // 👉 Nếu có myPlantId cụ thể
            notesListener = db.collection("users")
                    .document(userId)
                    .collection("my_plants")
                    .document(myPlantId)
                    .collection("notes")
                    .addSnapshotListener((snapshot, e) -> {
                        if (e != null) {
                            Log.e("NoteActivity", "Lỗi realtime load notes: " + e.getMessage(), e);
                            return;
                        }
                        handleRealtimeNoteData(snapshot, false);
                    });
        } else {
            db.collection("users")
                    .document(userId)
                    .collection("my_plants")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        totalPlantCount = queryDocumentSnapshots.size();
                        Log.d("NoteActivity", "Loaded " + totalPlantCount + " plants from Firestore");
                        if (totalPlantCount == 0) {
                            onAllNotesLoaded();
                            return;
                        }
                        for (QueryDocumentSnapshot plantDoc : queryDocumentSnapshots) {
                            Log.d("NoteActivity", "Plant ID: " + plantDoc.getId());
                            final boolean[] firstSnapshotReceived = {false}; // Khởi tạo lại mỗi lần xử lý cây

                            db.collection("users")
                                    .document(userId)
                                    .collection("my_plants")
                                    .document(plantDoc.getId())
                                    .collection("notes")
                                    .addSnapshotListener((snapshot, e) -> {
                                        if (e != null) {
                                            Log.e("NoteActivity", "Lỗi realtime load notes for plant " + plantDoc.getId() + ": " + e.getMessage(), e);
                                            return;
                                        }
                                        if (snapshot != null) {
                                            Log.d("NoteActivity", "Loaded notes for plant " + plantDoc.getId() + ": " + snapshot.size());
                                            handleRealtimeNoteData(snapshot, false);

                                            // Chỉ lần đầu tiên nhận snapshot mới tăng loadedPlantCount
                                            if (!firstSnapshotReceived[0]) {
                                                firstSnapshotReceived[0] = true;
                                                loadedPlantCount++;
                                                if (loadedPlantCount == totalPlantCount) {
                                                    onAllNotesLoaded();
                                                }
                                            }
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("NoteActivity", "Lỗi khi tải danh sách my_plants: " + e.getMessage(), e);
                        Toast.makeText(this, "Lỗi tải cây: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        }
    }

    private void handleRealtimeNoteData(QuerySnapshot snapshots, boolean fromInitialLoad) {
        if (snapshots == null) return;

        boolean dataChanged = false; // 👈 Cờ kiểm tra có thay đổi không

        Log.d("NoteActivity", "Received snapshot with " + snapshots.size() + " documents");

        for (DocumentChange dc : snapshots.getDocumentChanges()) {
            DocumentSnapshot doc = dc.getDocument();
            String id = doc.getId();
            String title = doc.getString("title");
            String summary = doc.getString("summary");
            Timestamp timestamp = doc.getTimestamp("lastUpdated");

            if (title == null || summary == null || timestamp == null) {
                Log.w("NoteActivity", "Ghi chú thiếu dữ liệu, bỏ qua: " + id);
                continue;
            }

            LocalDate date = timestamp.toDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            Note note = new Note(id, title, summary, date);

            switch (dc.getType()) {
                case ADDED:
                    noteList.add(note);
                    dataChanged = true;
                    Log.d("NoteActivity", "Added note: " + title);
                    break;
                case MODIFIED:
                    for (int i = 0; i < noteList.size(); i++) {
                        if (noteList.get(i).getId().equals(id)) {
                            noteList.set(i, note);
                            dataChanged = true;
                            Log.d("NoteActivity", "Modified note: " + title);
                            break;
                        }
                    }
                    break;
                case REMOVED:
                    if (noteList.removeIf(n -> n.getId().equals(id))) {
                        dataChanged = true;
                        Log.d("NoteActivity", "Removed note: " + title);
                    }
                    break;
            }
        }

        if (dataChanged) {
            Log.d("NoteActivity", "Data changed, updating RecyclerView.");
            // Notify RecyclerView to refresh
            adapter.notifyDataSetChanged();
        }
    }




    private void onAllNotesLoaded() {
        hideLoading();

        originalNoteList.clear();
        originalNoteList.addAll(noteList);

        List<NoteSectionItem> allNotes = categorizeNotes(originalNoteList);
        adapter.setNotes(allNotes);

        emptyTextView.setVisibility(noteList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private List<NoteSectionItem> categorizeNotes(List<Note> notes) {
        Map<String, List<Note>> groupedNotes = new HashMap<>();
        for (Note note : notes) {
            String section = getDateSection(note.getDate());
            groupedNotes.computeIfAbsent(section, k -> new ArrayList<>()).add(note);
        }

        List<String> sectionOrder = new ArrayList<>();
        sectionOrder.add("7 ngày trước");
        sectionOrder.add("30 ngày trước");

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

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startListeningToNotes();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (notesListener != null) {
            notesListener.remove();
            notesListener = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (notesListener != null) {
            notesListener.remove();
            notesListener = null;
        }
        super.onDestroy();
    }

}
