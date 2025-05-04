package com.example.myplantcare.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myplantcare.adapters.DetailNoteAdapter;
import com.example.myplantcare.models.DetailNote;
import com.example.myplantcare.R;

import java.util.ArrayList;

public class DetailNoteFragment extends Fragment {

    private RecyclerView recyclerView;
    private DetailNoteAdapter adapter;
    private ArrayList<DetailNote> noteList;
    private int selectedPosition = -1;
    private TextView toolbarTitle;
    private ImageButton toolbarBackButton;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && selectedPosition != -1) {
                    //noteList.get(selectedPosition).setImageUri(uri);
                    adapter.notifyItemChanged(selectedPosition);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_note, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Thiết lập Toolbar
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.insider_toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);
        toolbarTitle.setText("Chi tiết ghi chú");
        toolbarBackButton.setVisibility(View.VISIBLE);
        toolbarBackButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Nhận dữ liệu từ Bundle (nếu có)
        Bundle bundle = getArguments();
        if (bundle != null) {
            String noteTitle = bundle.getString("noteTitle", "Tiêu đề ghi chú");
            String noteContent = bundle.getString("noteContent", "Nội dung ghi chú");
            String noteDate = bundle.getString("noteDate", "Ngày tạo");

            // Ánh xạ các view hiển thị tiêu đề và ngày
            EditText etNoteTitle = view.findViewById(R.id.et_note_title);
            TextView tvNoteDate = view.findViewById(R.id.tv_note_date);
            EditText etNoteContent = view.findViewById(R.id.tv_note_content);

            // Cập nhật nội dung hiển thị
            etNoteTitle.setText(noteTitle);
            tvNoteDate.setText(noteDate);
            etNoteContent.setText(noteContent);
        }

        // Thiết lập RecyclerView
        recyclerView = view.findViewById(R.id.recycler_detail_notes);
        noteList = new ArrayList<>();
        noteList.add(new DetailNote()); // Ghi chú trống khởi tạo ban đầu

        adapter = new DetailNoteAdapter(requireContext(), noteList, position -> {
            selectedPosition = position;
            imagePickerLauncher.launch("image/*");
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }
}
