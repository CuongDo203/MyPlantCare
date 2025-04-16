package com.example.myplantcare.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myplantcare.adapters.DetailNoteAdapter;
import com.example.myplantcare.models.DetailNote;
import com.example.myplantcare.R;

import java.util.ArrayList;
import java.util.List;

public class DetailNoteFragment extends Fragment {

    private RecyclerView recyclerView;
    private DetailNoteAdapter adapter;
    private ArrayList<DetailNote> noteList;
    private int selectedPosition = -1;
    private NavController navController;
    private TextView toolbarTitle;
    private ImageButton toolbarBackButton;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && selectedPosition != -1) {
                    noteList.get(selectedPosition).setImageUri(uri);
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
        navController = Navigation.findNavController(view);

        // Ánh xạ các view từ layout
        recyclerView = view.findViewById(R.id.recycler_detail_notes);
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.insider_toolbar); // ID của Toolbar bạn đã include
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title); // ID của TextView tiêu đề trong Toolbar
        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button); // ID của ImageButton back trong Toolbar

        // Khởi tạo danh sách ghi chú
        noteList = new ArrayList<>();
        // Thêm một ghi chú ban đầu (hoặc bạn có thể tải dữ liệu thực tế ở đây)
        noteList.add(new DetailNote());
        // Nếu bạn muốn sử dụng dữ liệu mẫu (chỉ để test UI), bạn có thể thêm như sau:
        // loadSampleNotes();

        // Khởi tạo Adapter
        adapter = new DetailNoteAdapter(requireContext(), noteList, position -> {
            selectedPosition = position;
            imagePickerLauncher.launch("image/*");
        });

        // Thiết lập Layout Manager và Adapter cho RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Thiết lập tiêu đề cho Toolbar
        toolbarTitle.setText("Chi tiết ghi chú");

        // Thiết lập nút back trên Toolbar
        toolbarBackButton.setVisibility(View.VISIBLE);
        toolbarBackButton.setOnClickListener(v -> navController.popBackStack());
    }

    // Phương thức để tải dữ liệu mẫu (tùy chọn)
    private void loadSampleNotes() {
        noteList.add(new DetailNote(Uri.parse("android.resource://com.example.myplantcare/" + R.drawable.ic_add), "Nội dung 1"));
        noteList.add(new DetailNote(null, ""));
        noteList.add(new DetailNote(Uri.parse("android.resource://com.example.myplantcare/" + R.drawable.ic_add), "Thêm nội dung khác"));
        // ... thêm dữ liệu mẫu của bạn
    }
}