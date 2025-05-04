package com.example.myplantcare.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantcare.R;
import com.example.myplantcare.adapters.MyPlantAdapterForSelectDialog;
import com.example.myplantcare.adapters.NoteAdapter;
import com.example.myplantcare.models.DetailNote;
import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.models.Note;
import com.example.myplantcare.models.NoteSectionItem;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
import java.util.UUID;

import java.text.Normalizer;
import java.util.concurrent.atomic.AtomicReference;


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
    private String treeName;

    private MyPlantModel selectedPlant;

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
        myPlantId = getIntent().getStringExtra("id");
        Log.d("NoteActivity", "myPlantId nhận được: " + myPlantId);
        treeName = getIntent().getStringExtra("treeName");

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
        if(myPlantId != null){
            toolbarTitle.setText("Ghi chú " + treeName);
        }else{
            toolbarTitle.setText("Toàn bộ ghi chú");
        }
        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);
//        toolbarTitle.setText("Ghi chú");
        toolbarBackButton.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(this, new ArrayList<>(), userId);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(note -> {
            Log.d("NoteActivity", "Clicked noteId=" + note.getId()
                    + " note.getMyPlantId()=" + note.getMyPlantId());
            Intent intent = new Intent(NoteActivity.this, DetailNoteActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("id", note.getMyPlantId());
            intent.putExtra("noteId", note.getId());
            startActivity(intent);
        });

        Button btnCreateNote = findViewById(R.id.btnCreateNote);
        btnCreateNote.setOnClickListener(v -> showSelectPlantDialog());
    }

    private void filterNotes(String query) {
        // 1. Chuẩn hoá query
        String q = stripAccents(query).toLowerCase().trim();

        // 2. Nếu query rỗng thì show tất cả
        if (q.isEmpty()) {
            adapter.setNotes(categorizeNotes(originalNoteList));
            return;
        }

        filteredNoteList.clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Note note : originalNoteList) {
            // 3. Normalize title và date
            String titleNorm = stripAccents(note.getTitle()).toLowerCase();
            String dateStr   = note.getDate().format(formatter);
            String dateNorm  = stripAccents(dateStr).toLowerCase();

            // 4. Build một chuỗi chứa tất cả text trong content
            StringBuilder sb = new StringBuilder();
            for (DetailNote item : note.getContent()) {
                sb.append(item.getText()).append(" ");
            }
            String contentAll = sb.toString().trim();
            String contentNorm = stripAccents(contentAll).toLowerCase();

            // 5. So sánh query với title, content hoặc date
            if (titleNorm.contains(q)
                    || contentNorm.contains(q)
                    || dateNorm.contains(q)) {
                filteredNoteList.add(note);
            }
        }

        // 6. Chuyển thành sections và cập nhật adapter
        List<NoteSectionItem> filteredSections = categorizeNotes(filteredNoteList);
        adapter.setNotes(filteredSections);
    }



    private void startListeningToNotes() {
        if (userId == null) {
            Log.e("NoteActivity", "User ID null, không thể tải ghi chú.");
            return;
        }

        showLoading();
        noteList.clear();
        totalPlantCount = 0;
        loadedPlantCount = 0;

        if (myPlantId != null) {
            // Chỉ cho 1 cây
            listenNotesForPlant(myPlantId);
        } else {
            // Lấy tất cả cây
            db.collection("users")
                    .document(userId)
                    .collection("my_plants")
                    .get()
                    .addOnSuccessListener(plantsSnap -> {
                        totalPlantCount = plantsSnap.size();
                        if (totalPlantCount == 0) {
                            onAllNotesLoaded();
                            return;
                        }

                        for (DocumentSnapshot plantDoc : plantsSnap) {
                            String plantId = plantDoc.getId();
                            final boolean[] first = { false };
                            db.collection("users")
                                    .document(userId)
                                    .collection("my_plants")
                                    .document(plantId)
                                    .collection("notes")
                                    .addSnapshotListener((snaps, e) -> {
                                        if (e != null || snaps == null) return;
                                        // Truyền đúng plantId
                                        handleRealtimeNoteData(snaps, plantId);
                                        Log.e("NoteActivity", "plantId in startListeningToNotes: " + plantId);
                                        // Sau lần snapshot đầu, tăng counter để biết khi nào hết cây
                                        if (!first[0]) {
                                            first[0] = true;
                                            loadedPlantCount++;
                                            if (loadedPlantCount == totalPlantCount) {
                                                onAllNotesLoaded();
                                            }
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        hideLoading();
                        Toast.makeText(this, "Lỗi tải cây: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void listenNotesForPlant(String plantId) {
        notesListener = db.collection("users")
                .document(userId)
                .collection("my_plants")
                .document(plantId)
                .collection("notes")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    handleRealtimeNoteData(snapshots, plantId);
                    // Với trường hợp 1 cây, ngay sau snapshot đầu ta coi là load xong
                    onAllNotesLoaded();
                });
    }
    @SuppressLint("NotifyDataSetChanged")
    private void handleRealtimeNoteData(QuerySnapshot snapshots, String plantId) {
        if (snapshots == null) return;
        boolean dataChanged = false;

        for (DocumentChange dc : snapshots.getDocumentChanges()) {
            DocumentSnapshot doc = dc.getDocument();
            String id    = doc.getId();
            String title = doc.getString("title");
            Timestamp ts = doc.getTimestamp("lastUpdated");

            if (title == null || ts == null) {
                Log.w("NoteActivity", "Ghi chú thiếu dữ liệu, bỏ qua: " + id);
                continue;
            }

            // 1) Chuyển timestamp → LocalDate
            LocalDate date = ts.toDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            // 2) Đọc List<Map> từ Firestore
            Object rawContent = doc.get("content");
            List<DetailNote> content = new ArrayList<>();

            if (rawContent instanceof List) {
                // Schema mới: content là List<Map<String,Object>>
                @SuppressWarnings("unchecked")
                List<Map<String,Object>> rawItems = (List<Map<String,Object>>) rawContent;
                for (Map<String,Object> m : rawItems) {
                    String text     = m.get("text")     != null ? m.get("text").toString()     : "";
                    String imageUrl = m.get("imageUrl") != null ? m.get("imageUrl").toString() : "";
                    content.add(new DetailNote(text, imageUrl));
                }
            } else if (rawContent instanceof String) {
                // Fallback schema cũ: content chỉ là một String
                String oldText = (String) rawContent;
                content.add(new DetailNote(oldText, ""));
            }
            // else: cả hai null hoặc loại khác, content = empty list


            // 3) Tạo Note với constructor mới
            Note note = new Note(id, title, content, date, plantId);

            // 4) Xử lý DocumentChange
            switch (dc.getType()) {
                case ADDED:
                    noteList.add(note);
                    dataChanged = true;
                    break;
                case MODIFIED:
                    for (int i = 0; i < noteList.size(); i++) {
                        if (noteList.get(i).getId().equals(id)) {
                            noteList.set(i, note);
                            dataChanged = true;
                            break;
                        }
                    }
                    break;
                case REMOVED:
                    if (noteList.removeIf(n -> n.getId().equals(id))) {
                        dataChanged = true;
                    }
                    break;
            }
        }

        if (dataChanged) {
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

//    private void showSelectPlantDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_plant, null);
//        builder.setView(dialogView);
//
//        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_plant_list);
//        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_dialog);
//        Button btnCreate = dialogView.findViewById(R.id.btn_create_note);
//
//        List<MyPlantModel> plantList = new ArrayList<>();
//        MyPlantAdapterForSelectDialog adapter = new MyPlantAdapterForSelectDialog(plantList, plant -> {
//            selectedPlant = plant;
//        });
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(adapter);
//
//        fetchMyPlants(plantList, adapter);
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//
//        btnCancel.setOnClickListener(v -> dialog.dismiss());
//
//        btnCreate.setOnClickListener(v -> {
//            if (selectedPlant != null) {
//                dialog.dismiss();
//                createNoteForPlant(selectedPlant);
//            } else {
//                Toast.makeText(this, "Vui lòng chọn một cây!", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void showSelectPlantDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_plant, null);
        builder.setView(dialogView);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_plant_list);
        EditText etSearchPlant = dialogView.findViewById(R.id.search_plant_for_note);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_dialog);
        Button btnCreate = dialogView.findViewById(R.id.btn_create_note);

        List<MyPlantModel> plantList = new ArrayList<>();

        // Dùng AtomicReference để giữ cây được chọn
        AtomicReference<MyPlantModel> selectedPlantRef = new AtomicReference<>();

        MyPlantAdapterForSelectDialog adapter = new MyPlantAdapterForSelectDialog(plantList, plant -> {
            selectedPlantRef.set(plant); // Gán cây được chọn vào biến tham chiếu
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fetchMyPlants(plantList, adapter); // Hàm này cần cập nhật adapter khi load xong

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            MyPlantModel selectedPlant = selectedPlantRef.get(); // Lấy cây được chọn
            if (selectedPlant != null) {
                dialog.dismiss();
                createNoteForPlant(selectedPlant);
            } else {
                Toast.makeText(this, "Vui lòng chọn một cây!", Toast.LENGTH_SHORT).show();
            }
        });

        etSearchPlant.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }


    // 2. Lấy danh sách cây của tôi
    @SuppressLint("NotifyDataSetChanged")
//    private void fetchMyPlants(List<MyPlantModel> plantList, MyPlantAdapterForSelectDialog adapter) {
//        db.collection("users").document(userId)
//                .collection("my_plants")
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    plantList.clear();
//                    for (QueryDocumentSnapshot doc : querySnapshot) {
//                        MyPlantModel plant = doc.toObject(MyPlantModel.class);
//                        plant.setId(doc.getId());
//                        plantList.add(plant);
//                    }
//                    adapter.notifyDataSetChanged();
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("NoteActivity", "Lỗi khi lấy danh sách cây: " + e.getMessage());
//                    Toast.makeText(this, "Lỗi tải cây của bạn!", Toast.LENGTH_SHORT).show();
//                });
//    }

    private void fetchMyPlants(List<MyPlantModel> plantList, MyPlantAdapterForSelectDialog adapter) {
        db.collection("users").document(userId)
                .collection("my_plants")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("DEBUG", "QuerySnapshot size: " + querySnapshot.size());
                    plantList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        MyPlantModel plant = doc.toObject(MyPlantModel.class);
                        plant.setId(doc.getId());
                        plantList.add(plant);
                        Log.d("DEBUG", "Plant added: " + plant.getNickname());
                    }
                    // Update the adapter after data is fetched
                    adapter.updateList(plantList);
                })
                .addOnFailureListener(e -> {
                    Log.e("ERROR", "Error fetching plants: ", e);
                    Toast.makeText(this, "Không thể tải danh sách cây!", Toast.LENGTH_SHORT).show();
                });
    }






    private void createNoteForPlant(MyPlantModel plant) {
        String noteId = UUID.randomUUID().toString();

        // 1) Khởi tạo list items rỗng
        List<Map<String, Object>> items = new ArrayList<>();
        // Nếu muốn khởi sẵn một dòng blank:
        Map<String, Object> blank = new HashMap<>();
        blank.put("text", "");
        blank.put("imageUrl", "");
        items.add(blank);

        // 2) Chuẩn bị data để set()
        Map<String, Object> noteData = new HashMap<>();
        noteData.put("title", "Tiêu đề");              // đặt title rỗng, người dùng chỉnh sau
        noteData.put("summary", "");            // summary có thể lấy từ items.get(0).text
        noteData.put("content", items);           // đây là mảng map mới
        noteData.put("lastUpdated", FieldValue.serverTimestamp());

        // 3) Viết lên Firestore
        db.collection("users").document(userId)
                .collection("my_plants").document(plant.getId())
                .collection("notes").document(noteId)
                .set(noteData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Tạo ghi chú thành công!", Toast.LENGTH_SHORT).show();
                    // Mở DetailNoteActivity ngay để người dùng bắt đầu nhập
                    Intent intent = new Intent(this, DetailNoteActivity.class);
                    intent.putExtra("userId",  userId);
                    intent.putExtra("id",      plant.getId());
                    intent.putExtra("noteId",  noteId);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Log.e("NoteActivity", "Lỗi khi tạo ghi chú: " + e.getMessage(), e);
                    Toast.makeText(this, "Tạo ghi chú thất bại!", Toast.LENGTH_SHORT).show();
                });
    }

    private String stripAccents(String s) {
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        // Xoá hết các ký tự dấu (Mark)
        return normalized.replaceAll("\\p{M}", "");
    }
}