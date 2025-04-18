package com.example.myplantcare.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myplantcare.R;
import com.example.myplantcare.adapters.DetailNoteAdapter;
import com.example.myplantcare.models.DetailNote;

import java.util.ArrayList;

public class DetailNoteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DetailNoteAdapter adapter;
    private ArrayList<DetailNote> noteList;
    private int selectedPosition = -1;
    private TextView toolbarTitle;
    private ImageButton toolbarBackButton;

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
        setContentView(R.layout.fragment_detail_note); // hoặc chuyển layout thành activity_detail_note.xml nếu cần

        // Thiết lập Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.insider_toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);
        toolbarTitle.setText("Chi tiết ghi chú");
        toolbarBackButton.setVisibility(ImageButton.VISIBLE);
        toolbarBackButton.setOnClickListener(v -> finish());

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        String noteTitle = intent.getStringExtra("noteTitle");
        String noteContent = intent.getStringExtra("noteContent");
        String noteDate = intent.getStringExtra("noteDate");

        // Ánh xạ các view hiển thị tiêu đề và ngày
        TextView tvNoteTitle = findViewById(R.id.tv_note_title);
        TextView tvNoteDate = findViewById(R.id.tv_note_date);
        EditText etNoteContent = findViewById(R.id.tv_note_content);

        // Cập nhật nội dung hiển thị
        tvNoteTitle.setText(noteTitle);
        tvNoteDate.setText(noteDate);
        etNoteContent.setText(noteContent);

        // Thiết lập RecyclerView
        recyclerView = findViewById(R.id.recycler_detail_notes);
        noteList = new ArrayList<>();
        noteList.add(new DetailNote()); // Ghi chú trống khởi tạo ban đầu

        adapter = new DetailNoteAdapter(this, noteList, position -> {
            selectedPosition = position;
            imagePickerLauncher.launch("image/*");
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
