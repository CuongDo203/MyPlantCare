package com.example.myplantcare.activities;

import android.annotation.SuppressLint;
import android.net.Uri;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** @noinspection ALL*/
public class DetailNoteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DetailNoteAdapter adapter;
    private ArrayList<DetailNote> noteList;
    private int selectedPosition = -1;
    private ImageButton toolbarBackButton;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private EditText etNoteTitle;
    private TextView tvToolbarTitle;
    private TextView tvNoteDate;

    private String userId;
    private String myPlantId;
    private String noteId;

    private FirebaseStorage storage;
    private StorageReference storageRoot;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null && selectedPosition != -1) {
                            // sang bước 3: upload
                            uploadImageForItem(uri, selectedPosition);
                        }
                    }
            );


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_detail_note);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRoot = storage.getReference();

        // Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.insider_toolbar);
        setSupportActionBar(toolbar); // just added
        getSupportActionBar().setDisplayShowTitleEnabled(false);  // just added
        tvToolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        tvToolbarTitle.setText("Ghi chú chi tiết");
        etNoteTitle = findViewById(R.id.et_note_title);
        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);
        toolbarBackButton.setOnClickListener(v -> finish());

        // Other views
        tvNoteDate  = findViewById(R.id.tv_note_date);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recycler_detail_notes);
        FloatingActionButton fabAddItem = findViewById(R.id.fab_add_note_item);

        // Intent extras
        userId    = getIntent().getStringExtra("userId");
        myPlantId = getIntent().getStringExtra("id");
        noteId    = getIntent().getStringExtra("noteId");
        Log.d("DetailNoteActivity","myPlantId:" + myPlantId + " noteId:" +noteId +" userId:"+userId);

        if (userId == null || myPlantId == null || noteId == null) {
            Log.d("DetailNoteActivity","ko du thong tin");
            Toast.makeText(this, "Không đủ thông tin để hiển thị ghi chú", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Setup RecyclerView + Adapter
        noteList = new ArrayList<>();
        adapter = new DetailNoteAdapter(
                this,
                noteList,
                position -> {
                    selectedPosition = position;
                    imagePickerLauncher.launch("image/*");
                }
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Thêm item blank mới
        fabAddItem.setOnClickListener(v -> {
            noteList.add(new DetailNote("", ""));
            adapter.notifyItemInserted(noteList.size() - 1);
            recyclerView.smoothScrollToPosition(noteList.size() - 1);
        });

        // Tải dữ liệu từ Firestore
        fetchNoteDetails();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchNoteDetails() {
        showLoading();
        db.collection("users").document(userId)
                .collection("my_plants").document(myPlantId)
                .collection("notes").document(noteId)
                .get()
                .addOnSuccessListener(doc -> {
                    hideLoading();
                    if (!doc.exists()) return;

                    // Tiêu đề + ngày như cũ...
                    String title = doc.getString("title");
                    Timestamp ts = doc.getTimestamp("lastUpdated");
                    etNoteTitle.setText(title != null ? title : "");
                    if (ts != null) {
                        tvNoteDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(ts.toDate()));
                    }

                    // XÓA placeholder cũ
                    noteList.clear();

                    // 1) Thử đọc schema mới: "items"
                    List<Map<String,Object>> rawItems =
                            (List<Map<String,Object>>) doc.get("content"); //just changed
                    if (rawItems != null) {
                        for (Map<String,Object> m : rawItems) {
                            String text     = m.get("text")     != null ? (String)m.get("text")     : "";
                            String imageUrl = m.get("imageUrl") != null ? (String)m.get("imageUrl") : "";
                            noteList.add(new DetailNote(text, imageUrl));
                        }
                    } else {
                        // 2) Fallback với schema cũ: "content" là String
                        String oldContent = doc.getString("content");
                        if (oldContent != null) {
                            noteList.add(new DetailNote(oldContent, ""));
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void saveNoteDetails() {
        showLoading();
        // Thu thập dữ liệu
        String title = etNoteTitle.getText().toString().trim();
        List<Map<String,Object>> items = new ArrayList<>();
        for (DetailNote dn : noteList) {
            Map<String,Object> m = new HashMap<>();
            m.put("text", dn.getText());
            m.put("imageUrl", dn.getImageUrl());
            items.add(m);
        }

        Map<String,Object> data = new HashMap<>();
        data.put("title", title);
        data.put("content", items);
        data.put("lastUpdated", Timestamp.now());

        db.collection("users").document(userId)
                .collection("my_plants").document(myPlantId)
                .collection("notes").document(noteId)
                .update(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d("DetailNoteActivity", "Saving to Firestore: title=" + title + " items=" + items);
                    hideLoading();
                    Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.d("DetailNoteActivity", "Saving to Firestore: Failed");
                    hideLoading();
                    Toast.makeText(this, "Lỗi khi lưu: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void uploadImageForItem(Uri localUri, int position) {
        // 1) Tạo đường dẫn trên Storage
        String path = String.format(Locale.US,
                "users/%s/my_plants/%s/notes/%s/items/%d.jpg",
                userId, myPlantId, noteId, position
        );
        StorageReference ref = storageRoot.child(path);

        // 2) Upload file
        ref.putFile(localUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    // 3) Khi upload xong, lấy download URL
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    String url = downloadUri.toString();
                    // 4) Cập nhật model và adapter
                    DetailNote item = noteList.get(position);
                    item.setImageUrl(url);
                    adapter.notifyItemChanged(position);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload ảnh thất bại: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
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

