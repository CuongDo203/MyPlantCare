package com.example.myplantcare.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantcare.R;
import com.example.myplantcare.adapters.NoteAdapter;
import com.example.myplantcare.fragments.DetailNoteFragment;
import com.example.myplantcare.models.Note;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NoteActivity extends AppCompatActivity {
    private List<Note> originalNoteList = new ArrayList<>();
    private List<Note> filteredNoteList = new ArrayList<>();
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private List<Note> noteList = new ArrayList<>();
    private TextView toolbarTitle;
    private ImageButton toolbarBackButton;
    private EditText etSearchNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note); // layout mới không phụ thuộc MainActivity

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

        loadSampleNotes();
        originalNoteList.addAll(noteList);
        filteredNoteList.addAll(noteList);

        adapter = new NoteAdapter(this, filteredNoteList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(note -> {
            Intent intent = new Intent(NoteActivity.this, DetailNoteActivity.class);
            intent.putExtra("noteTitle", note.getTitle());
            intent.putExtra("noteContent", note.getContent());
            intent.putExtra("noteDate", note.getDate().toString());
            startActivity(intent);
        });

        Button btnCreateNote = findViewById(R.id.btnCreateNote);
        btnCreateNote.setOnClickListener(v -> {
            Intent intent = new Intent(NoteActivity.this, DetailNoteActivity.class);
            intent.putExtra("noteTitle", "");
            intent.putExtra("noteContent", "");
            intent.putExtra("noteDate", LocalDate.now().toString());
            startActivity(intent);
        });
    }

    private void loadSampleNotes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            noteList.add(new Note("Cây EFG đang phát triển tốt", "Những thứ cần mua tiếp theo...", LocalDate.of(2025, 2, 28)));
            noteList.add(new Note("BCD", "Lá có dấu hiệu nhạt màu...", LocalDate.of(2025, 2, 16)));
            noteList.add(new Note("Cây lam", "Setup chỗ mới cho cây...", LocalDate.of(2025, 2, 5)));
            noteList.add(new Note("EFG ngày đầu về nhà", "Setup ánh sáng...", LocalDate.of(2025, 1, 29)));
            noteList.add(new Note("Mua cây ACB", "Ngày đầu tiên mua cây...", LocalDate.of(2024, 11, 25)));
            noteList.add(new Note("Cây EFG đang phát triển tốt", "Những thứ cần mua tiếp theo...", LocalDate.of(2025, 2, 28)));
            noteList.add(new Note("BCD", "Lá có dấu hiệu nhạt màu...", LocalDate.of(2025, 2, 16)));
            noteList.add(new Note("Cây lam", "Setup chỗ mới cho cây...", LocalDate.of(2025, 2, 5)));
            noteList.add(new Note("EFG ngày đầu về nhà", "Setup ánh sáng...", LocalDate.of(2025, 1, 29)));
            noteList.add(new Note("Mua cây ACB", "Ngày đầu tiên mua cây...", LocalDate.of(2024, 11, 25)));
            noteList.add(new Note("Cây EFG đang phát triển tốt", "Những thứ cần mua tiếp theo...", LocalDate.of(2025, 2, 28)));
            noteList.add(new Note("BCD", "Lá có dấu hiệu nhạt màu...", LocalDate.of(2025, 2, 16)));
            noteList.add(new Note("Cây lam", "Setup chỗ mới cho cây...", LocalDate.of(2025, 2, 5)));
            noteList.add(new Note("EFG ngày đầu về nhà", "Setup ánh sáng...", LocalDate.of(2025, 1, 29)));
            noteList.add(new Note("Mua cây ACB", "Ngày đầu tiên mua cây...", LocalDate.of(2024, 11, 25)));
        }
    }

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
        adapter.notifyDataSetChanged();
    }
}
