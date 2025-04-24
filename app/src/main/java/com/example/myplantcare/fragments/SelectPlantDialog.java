package com.example.myplantcare.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.myplantcare.R;
import com.example.myplantcare.activities.DetailNoteActivity;

import java.time.LocalDate;

public class SelectPlantDialog extends DialogFragment {

    private Button btnCancel, btnCreate;
    private String selectedPlant = "treee";

    // Phương thức để tạo giao diện Dialog
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_select_plant, null);

        btnCancel = view.findViewById(R.id.btn_cancel_dialog);
        btnCreate = view.findViewById(R.id.btn_create_note);

        // Nút Hủy
        btnCancel.setOnClickListener(v -> dismiss()); // Đóng Dialog khi nhấn Hủy

        // Nút Tạo
        btnCreate.setOnClickListener(v -> {
            // Logic khi tạo ghi chú, ví dụ truyền dữ liệu hoặc xử lý gì đó
            // Kiểm tra cây đã được chọn hay chưa
            if (selectedPlant != null && !selectedPlant.isEmpty()) {
                // Lấy thông tin ghi chú và cây
                String noteTitle = "Ghi chú cho " + selectedPlant;
                String noteContent = ""; // Nội dung ghi chú có thể lấy từ người dùng sau
                String noteDate = LocalDate.now().toString();

                // Tạo Intent để chuyển đến DetailNoteActivity
                Intent intent = new Intent(getContext(), DetailNoteActivity.class);
                intent.putExtra("noteTitle", noteTitle);
                intent.putExtra("noteContent", noteContent);
                intent.putExtra("noteDate", noteDate);
                intent.putExtra("selectedPlant", selectedPlant); // Cây được chọn

                // Mở DetailNoteActivity
                startActivity(intent);
            } else {
                // Nếu chưa chọn cây, hiển thị thông báo lỗi hoặc yêu cầu chọn cây
                Toast.makeText(getContext(), "Vui lòng chọn cây", Toast.LENGTH_SHORT).show();
            }

            // Đóng dialog sau khi nhấn Tạo ghi chú
            dismiss();
            dismiss(); // Đóng dialog
        });

        builder.setView(view);
        return builder.create();
    }
}
