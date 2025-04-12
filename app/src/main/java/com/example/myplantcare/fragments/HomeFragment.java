package com.example.myplantcare.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.myplantcare.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Objects;

public class HomeFragment extends Fragment {
    LinearLayout taskList;
    CardView taskDetail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initContents(view);
        taskDetail.setOnClickListener(v -> showBottomDialog());
        return view;
    }

    private void initContents(View view) {
        taskList = view.findViewById(R.id.task_list);
        taskDetail = view.findViewById(R.id.task_detail);
    }

    private void showBottomDialog() {
//        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.DialogAnimation);
//        View bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet, null);
//
//        // Cho phép kéo xuống để tắt
//        bottomSheetDialog.setCanceledOnTouchOutside(true);
//        bottomSheetDialog.setDismissWithAnimation(true); // Hiệu ứng animation khi tắt
//
//        bottomSheetDialog.setContentView(bottomSheetView);
//
//        // Đặt nền trong suốt để bo góc hiện ra
//        if (bottomSheetDialog.getWindow() != null) {
//            bottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        }
//
//        bottomSheetDialog.show();

        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.setCancelable(true);
        dialog.show();
    }
}