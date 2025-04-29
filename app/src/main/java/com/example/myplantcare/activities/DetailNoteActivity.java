package com.example.myplantcare.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myplantcare.R;
import com.example.myplantcare.adapters.DetailNoteAdapter;
import com.example.myplantcare.models.DetailNote;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class DetailNoteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DetailNoteAdapter adapter;
    private ArrayList<DetailNote> noteList;
    private int selectedPosition = -1;
    private TextView toolbarTitle;
    private ImageButton toolbarBackButton;
    private FirebaseFirestore db;

    private ProgressBar progressBar;

    private String userId;
    private String myPlantId;
    private String noteId;


    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && selectedPosition != -1) {
                    noteList.get(selectedPosition).setImageUri(uri);
                    adapter.notifyItemChanged(selectedPosition);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_detail_note);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.insider_toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);
        toolbarTitle.setText("Chi tiết ghi chú");
        toolbarBackButton.setVisibility(ImageButton.VISIBLE);
        toolbarBackButton.setOnClickListener(v -> finish());
        progressBar = findViewById(R.id.progressBar);


        db = FirebaseFirestore.getInstance();

        userId = getIntent().getStringExtra("userId");
        Log.d("DetailNoteActivity", "userId nhận được: " + userId);
        myPlantId = getIntent().getStringExtra("id");
        Log.d("DetailNoteActivity", "myPlantId nhận được: " + myPlantId);
        noteId = getIntent().getStringExtra("noteId");
        Log.d("DetailNoteActivity", "noteId nhận được: " + noteId);

        if (userId == null || myPlantId == null || noteId == null) {

            Log.d("DetailNoteActivity", "Không đủ thông tin để hiển thị ghi chú ");
            Toast.makeText(this,
                    "Không đủ thông tin để hiển thị ghi chú",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        fetchNoteDetails(userId, myPlantId, noteId);

        recyclerView = findViewById(R.id.recycler_detail_notes);
        noteList = new ArrayList<>();
        noteList.add(new DetailNote());

        adapter = new DetailNoteAdapter(this, noteList, position -> {
            selectedPosition = position;
            imagePickerLauncher.launch("image/*");
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAddItem = findViewById(R.id.fab_add_note_item);
        fabAddItem.setOnClickListener(v -> {
            adapter.addNote(new DetailNote()); // tạo phương thức addNewNoteItem() trong adapter
            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        });

    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchNoteDetails(String userId, String plantId, String noteId) {
        showLoading();

        db.collection("users").document(userId)
                .collection("my_plants").document(plantId)
                .collection("notes").document(noteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    hideLoading();

                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String content = documentSnapshot.getString("content");
                        Timestamp timestamp = documentSnapshot.getTimestamp("lastUpdated");
                        String dateStr = null;

                        if (timestamp != null) {
                            Date date = timestamp.toDate();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            dateStr = sdf.format(date);
                        }

                        // Set title and date
                        EditText etNoteTitle = findViewById(R.id.et_note_title);
                        TextView tvNoteDate = findViewById(R.id.tv_note_date);
                        etNoteTitle.setText(title);
                        tvNoteDate.setText(dateStr);

                        // Set content inside recycler item
                        noteList.clear();
                        DetailNote detailNote = new DetailNote();
                        detailNote.setNoteText(content);
                        noteList.add(detailNote);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoading();

                    Log.e("DetailNoteActivity", "Lỗi khi tải ghi chú: " + e.getMessage());
                    Toast.makeText(DetailNoteActivity.this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveNoteDetails() {
        showLoading();

        // 1. Chuẩn bị dữ liệu items
        List<Map<String,Object>> items = new ArrayList<>();
        for (DetailNote d : noteList) {
            Map<String,Object> m = new HashMap<>();
            m.put("text", d.getNoteText());
            // nếu URI có, lưu String của nó; hoặc để null
            m.put("imageUri", d.getImageUri() != null ? d.getImageUri().toString() : null);
            items.add(m);
        }

        //just added
        EditText etNoteTitle = findViewById(R.id.et_note_title);
        String title = etNoteTitle.getText().toString();

        // 2. Tạo map để update
        Map<String,Object> data = new HashMap<>();
        data.put("title", title); // just added
        data.put("items", items);
        data.put("lastUpdated", Timestamp.now());

        // 3. Đẩy lên Firestore
        db.collection("users").document(userId)
                .collection("my_plants").document(myPlantId)
                .collection("notes").document(noteId)
                .update(data)
                .addOnSuccessListener(aVoid -> {
                    hideLoading();
                    Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(this, "Lỗi khi lưu: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save_detail_note) {
            saveNoteDetails();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }
}

