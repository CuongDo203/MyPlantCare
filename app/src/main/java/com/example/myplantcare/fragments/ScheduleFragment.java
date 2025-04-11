package com.example.myplantcare.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myplantcare.R;
import com.example.myplantcare.adapters.DayAdapter;
import com.example.myplantcare.adapters.ScheduleAdapter;
import com.example.myplantcare.models.DayModel;
import com.example.myplantcare.models.ScheduleModel;
import com.example.myplantcare.models.TaskModel;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScheduleFragment extends Fragment {

    private RecyclerView recyclerViewDays, recyclerViewScheduleUncompleted, recyclerViewScheduleCompleted;
    private DayAdapter dayAdapter;
    private ScheduleAdapter scheduleAdapter;
    private List<DayModel> dayList;
    private List<ScheduleModel> scheduleListUncompleted, scheduleListCompleted;
    private ImageView btnOpenDatePicker;

    // Tạo danh sách ngày
    private List<DayModel> generateDays() {
        List<DayModel> days = new ArrayList<>();
        String[] weekdays = {"CN","Th 2", "Th 3", "Th 4", "Th 5", "Th 6", "Th 7"};
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_MONTH);

        for (int i = -3; i <= 3; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, i);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String dayOfWeek = weekdays[calendar.get(Calendar.DAY_OF_WEEK) - 1];
            boolean isToday = (day == today);
            days.add(new DayModel(dayOfWeek, day, isToday));
            calendar.add(Calendar.DAY_OF_MONTH, -i); // Reset lại ngày
        }
        return days;
    }

    // Tạo danh sách lịch trình mẫu
    private List<ScheduleModel> generateSchedules() {
        List<ScheduleModel> schedules = new ArrayList<>();

        List<TaskModel> taskList1 = new ArrayList<>();
        taskList1.add(new TaskModel("Cây A - 8:00 AM - Ban công", false));
        taskList1.add(new TaskModel("Cây B - 9:30 AM - Phòng khách", false));

        List<TaskModel> taskList2 = new ArrayList<>();
        taskList2.add(new TaskModel("Cây C - 10:00 AM - Sân vườn", false));
        taskList2.add(new TaskModel("Cây D - 11:30 AM - Ban công", false));

        schedules.add(new ScheduleModel("Tưới nước", taskList1, "Tưới nước"));
        schedules.add(new ScheduleModel("Bón phân", taskList2, "Bón phân"));

        return schedules;
    }


    private void openDatePicker() {
        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Chọn ngày")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Chuyển đổi timestamp sang ngày tháng
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1; // Tháng tính từ 0
            int year = calendar.get(Calendar.YEAR);

            // Cập nhật TextView hoặc RecyclerView với ngày đã chọn
            String selectedDate = day + "/" + month + "/" + year;
            Toast.makeText(getContext(), "Ngày đã chọn: " + selectedDate, Toast.LENGTH_SHORT).show();

            // Đóng date picker
            datePicker.dismiss();
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        recyclerViewDays = view.findViewById(R.id.recyclerViewDays);
        recyclerViewScheduleUncompleted = view.findViewById(R.id.recyclerViewScheduleUncompleted);
        recyclerViewScheduleCompleted = view.findViewById(R.id.recyclerViewScheduleCompleted);
        btnOpenDatePicker = view.findViewById(R.id.btnOpenDatePicker);

        recyclerViewDays.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        dayList = generateDays();
        dayAdapter = new DayAdapter(dayList);
        recyclerViewDays.setAdapter(dayAdapter);

        recyclerViewScheduleUncompleted.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduleListUncompleted = generateSchedules();
        scheduleAdapter = new ScheduleAdapter(scheduleListUncompleted);
        recyclerViewScheduleUncompleted.setAdapter(scheduleAdapter);

        recyclerViewScheduleCompleted.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduleListCompleted = generateSchedules();
        scheduleAdapter = new ScheduleAdapter(scheduleListCompleted);
        recyclerViewScheduleCompleted.setAdapter(scheduleAdapter);

        btnOpenDatePicker.setOnClickListener(v -> openDatePicker());
        return view;
    }
}