package com.example.myplantcare.fragments;

import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.myplantcare.R;
import com.example.myplantcare.adapters.NoteAdapter;
import com.example.myplantcare.models.Note;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;

public class NoteFragment extends Fragment {
    private List<Note> originalNoteList = new ArrayList<>();
    private List<Note> filteredNoteList = new ArrayList<>();
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private List<Note> noteList = new ArrayList<>();
    private TextView toolbarTitle;
    private ImageButton toolbarBackButton;
    private EditText etSearchNote;

    public NoteFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note, container, false);
        // Trong NoteFragment.java (phương thức onViewCreated)
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearchNote = view.findViewById(R.id.etSearchNote);
        etSearchNote.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        navController = Navigation.findNavController(view);

        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.insider_toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);

        // Thiết lập tiêu đề cho trang danh sách ghi chú
        toolbarTitle.setText("Ghi chú");
        toolbarBackButton.setVisibility(View.GONE);

        recyclerView = view.findViewById(R.id.recyclerViewNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadSampleNotes();
        originalNoteList.addAll(noteList); // sao lưu danh sách gốc
        filteredNoteList.addAll(noteList); // hiển thị mặc định

        adapter = new NoteAdapter(getContext(), filteredNoteList);
        recyclerView.setAdapter(adapter);

        // Xử lý sự kiện click của item để chuyển đến trang chi tiết (ví dụ)
        adapter.setOnItemClickListener(note -> {
            // Gửi dữ liệu sang DetailNoteFragment bằng bundle
            Bundle bundle = new Bundle();
            bundle.putString("noteTitle", note.getTitle());
            bundle.putString("noteContent", note.getContent());
            bundle.putString("noteDate", note.getDate().toString());

            DetailNoteFragment detailFragment = new DetailNoteFragment();
            detailFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        Button btnCreateNote = view.findViewById(R.id.btnCreateNote); // đảm bảo ID này có trong layout
        btnCreateNote.setOnClickListener(v -> {
            DetailNoteFragment detailFragment = new DetailNoteFragment();
            Bundle bundle = new Bundle();
            bundle.putString("noteTitle", "");
            bundle.putString("noteContent", "");
            bundle.putString("noteDate", LocalDate.now().toString());
            detailFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void loadSampleNotes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            noteList.add(new Note("Cây EFG đang phát triển tốt", "Những thứ cần mua tiếp theo cho ...", LocalDate.of(2025, 2, 28)));
            noteList.add(new Note("BCD", "Các lá có dấu hiệu bị nhạt màu ...", LocalDate.of(2025, 2, 16)));
            noteList.add(new Note("Cây lam", "Sẽ tiến hành kế hoạch chăm sóc, setup chỗ ...", LocalDate.of(2025, 2, 5)));
            noteList.add(new Note("EFG ngày đầu về nhà setup", "Kế hoạch setup ánh sáng và nơi đặt cho cây ...", LocalDate.of(2025, 1, 29)));
            noteList.add(new Note("Mua cây ACB", "Hôm nay là ngày đầu tiên mua cây ACB ...", LocalDate.of(2024, 11, 25)));
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
