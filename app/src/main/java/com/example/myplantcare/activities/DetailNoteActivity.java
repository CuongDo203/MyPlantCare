package com.example.myplantcare.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.Locale;


public class DetailNoteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DetailNoteAdapter adapter;
    private ArrayList<DetailNote> noteList;
    private int selectedPosition = -1;
    private TextView toolbarTitle;
    private ImageButton toolbarBackButton;
    private FirebaseFirestore db;

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

        db = FirebaseFirestore.getInstance();

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.insider_toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);
        toolbarTitle.setText("Chi tiết ghi chú");
        toolbarBackButton.setVisibility(ImageButton.VISIBLE);
        toolbarBackButton.setOnClickListener(v -> finish());


        String noteId = getIntent().getStringExtra("noteId");


        if (noteId != null) {
            fetchNoteDetails(noteId);
        }

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

        if (noteId != null) {
            fetchNoteDetails(noteId);
        }

    }

private void fetchNoteDetails(String noteId) {
    db.collection("notes").document(noteId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
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
                    TextView tvNoteTitle = findViewById(R.id.tv_note_title);
                    TextView tvNoteDate = findViewById(R.id.tv_note_date);
                    tvNoteTitle.setText(title);
                    tvNoteDate.setText(dateStr);

                    // Set content inside recycler item
                    noteList.clear(); // Xóa phần tử mặc định
                    DetailNote detailNote = new DetailNote();
                    detailNote.setNoteText(content); // Gán content từ Firestore
                    noteList.add(detailNote);
                    adapter.notifyDataSetChanged(); // Cập nhật lại adapter
                }
            })
            .addOnFailureListener(e -> {
                Log.e("DetailNoteActivity", "Lỗi khi tải ghi chú: " + e.getMessage());
                Toast.makeText(DetailNoteActivity.this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
