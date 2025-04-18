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
    private List<Note> originalNoteList; // Danh sách gốc
    private List<Note> filteredNoteList; // **Khai báo biến thành viên ở đây**
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private List<Note> noteList;
    private NavController navController;
    private TextView toolbarTitle;
    private ImageButton toolbarBackButton;

    private EditText etSearchNote;

    public NoteFragment() {
        // Required empty public constructor
    }

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
        // Trong NoteFragment.java (phương thức onViewCreated)
        etSearchNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý trước khi thay đổi
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Gọi phương thức tìm kiếm mỗi khi text thay đổi
//                filterNotes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần xử lý sau khi thay đổi
            }
        });
        navController = Navigation.findNavController(view);

        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.insider_toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);

        // Thiết lập tiêu đề cho trang danh sách ghi chú
        toolbarTitle.setText("Ghi chú");
        toolbarBackButton.setVisibility(View.GONE); // Ẩn nút back ở trang chính

        recyclerView = view.findViewById(R.id.recyclerViewNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        noteList = new ArrayList<>();
        loadSampleNotes(); // dữ liệu mẫu

        adapter = new NoteAdapter(getContext(), noteList);
        recyclerView.setAdapter(adapter);

        // Xử lý sự kiện click của item để chuyển đến trang chi tiết (ví dụ)
        adapter.setOnItemClickListener(note -> {
            // Ví dụ về cách truyền dữ liệu (nếu cần)
            Bundle bundle = new Bundle();
            bundle.putString("noteTitle", note.getTitle());
            // ... truyền các dữ liệu khác của note

            navController.navigate(R.id.action_noteFragment_to_detailNoteFragment, bundle);
        });
    }

    private void loadSampleNotes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            noteList.add(new Note("Cây EFG đang phát triển tốt", "Những thứ cần mua tiếp theo cho ...", LocalDate.of(2025, 2, 28)));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            noteList.add(new Note("BCD", "Các lá có dấu hiệu bị nhạt màu ...", LocalDate.of(2025, 2, 16)));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            noteList.add(new Note("Cây lam", "Sẽ tiến hành kế hoạch chăm sóc, setup chỗ ...", LocalDate.of(2025, 2, 5)));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            noteList.add(new Note("EFG ngày đầu về nhà setup", "Kế hoạch setup ánh sáng và nơi đặt cho cây ...", LocalDate.of(2025, 1, 29)));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            noteList.add(new Note("Mua cây ACB", "Hôm nay là ngày đầu tiên mua cây ACB ...", LocalDate.of(2024, 11, 25)));
        }

        // Hoặc bạn có thể parse từ String nếu bạn muốn giữ định dạng đó trong code:
        // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        // noteList.add(new Note("Cây EFG đang phát triển tốt", "Những thứ cần mua tiếp theo cho ...", LocalDate.parse("28/02/2025", formatter)));
        // noteList.add(new Note("BCD", "Các lá có dấu hiệu bị nhạt màu ...", LocalDate.parse("16/02/2025", formatter)));
        // ...
    }


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

}