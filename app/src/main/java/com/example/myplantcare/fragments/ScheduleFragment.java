package com.example.myplantcare.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myplantcare.R;
import com.example.myplantcare.adapters.DayAdapter;
import com.example.myplantcare.adapters.ScheduleAdapter;
import com.example.myplantcare.data.responses.ScheduleWithMyPlantInfo;
import com.example.myplantcare.models.DayModel;
import com.example.myplantcare.models.ScheduleModel;
import com.example.myplantcare.models.TaskModel;
import com.example.myplantcare.viewmodels.ScheduleViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ScheduleFragment extends Fragment {

    private RecyclerView recyclerViewDays, recyclerViewScheduleUncompleted, recyclerViewScheduleCompleted;
    private DayAdapter dayAdapter;
    private ScheduleAdapter scheduleAdapter;
    private ScheduleViewModel scheduleViewModel;
    private List<DayModel> dayList;
    private List<Pair<String, List<ScheduleWithMyPlantInfo>>> scheduleListUncompleted, scheduleListCompleted;
    private ImageView btnOpenDatePicker;
    private FloatingActionButton fab;
//    List<Pair<String, List<ScheduleWithMyPlantInfo>>> todaySchedulesList;
    private String userId;

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
        taskList1.add(new TaskModel("Cây A - 8:00 AM - Ban công", ""));
        taskList1.add(new TaskModel("Cây B - 9:30 AM - Phòng khách", ""));

        List<TaskModel> taskList2 = new ArrayList<>();
        taskList2.add(new TaskModel("Cây C - 10:00 AM - Sân vườn", ""));
        taskList2.add(new TaskModel("Cây D - 11:30 AM - Ban công", ""));

//        schedules.add(new ScheduleModel("Tưới nước"));
//        schedules.add(new ScheduleModel("Bón phân"));

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
        initContents(view);
        setupViewModel();
        observeViewModel();
        fab.setOnClickListener(v -> {
            showAddScheduleDialog();
                });

        recyclerViewDays.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        dayList = generateDays();
        dayAdapter = new DayAdapter(dayList);
        recyclerViewDays.setAdapter(dayAdapter);

        recyclerViewScheduleUncompleted.setLayoutManager(new LinearLayoutManager(getContext()));
        Log.d("ScheduleFragment", "Size cv can lam o onCreateView: " + scheduleListUncompleted.size());
        scheduleAdapter = new ScheduleAdapter(scheduleListUncompleted);
        recyclerViewScheduleUncompleted.setAdapter(scheduleAdapter);

        recyclerViewScheduleCompleted.setLayoutManager(new LinearLayoutManager(getContext()));
        ScheduleAdapter completedAdapter = new ScheduleAdapter(scheduleListCompleted);
        recyclerViewScheduleCompleted.setAdapter(completedAdapter);

        btnOpenDatePicker.setOnClickListener(v -> openDatePicker());
        return view;
    }

    private void observeViewModel() {
        scheduleViewModel.todaySchedules.observe(getViewLifecycleOwner(), groupedSchedules  -> {
            if (groupedSchedules == null || groupedSchedules.isEmpty()) {
                Toast.makeText(getContext(), "Không có công việc hôm nay", Toast.LENGTH_SHORT).show();
                scheduleListUncompleted.clear();
                scheduleAdapter.notifyDataSetChanged();
                return;
            }

            scheduleListUncompleted.clear();

            for (Map.Entry<String, List<ScheduleWithMyPlantInfo>> entry : groupedSchedules.entrySet()) {
                scheduleListUncompleted.add(new Pair<>(entry.getKey(), entry.getValue()));
            }
            Log.d("ScheduleFragment", "Size cv can lam o observeViewModel: " + scheduleListUncompleted.size());
            scheduleAdapter.notifyDataSetChanged();
        });
        scheduleViewModel.errorMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                Log.d("ScheduleFragment", "Lỗi: " + message);
            }
        });

        scheduleViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            // Có thể thêm ProgressBar sau nếu bạn muốn
            Log.d("ScheduleFragment", "Đang tải: " + isLoading);
        });
    }

    private void setupViewModel() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        userId = user.getUid();
        scheduleViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new ScheduleViewModel(userId);
            }
        }).get(ScheduleViewModel.class);
    }

    private void showAddScheduleDialog() {
        AddScheduleDialogFragment addScheduleDialogFragment = AddScheduleDialogFragment.newInstance(null, userId);
        addScheduleDialogFragment.show(getParentFragmentManager(), "ADD_SCHEDULE_DIALOG");
    }

    private void initContents(View view) {
        recyclerViewDays = view.findViewById(R.id.recyclerViewDays);
        recyclerViewScheduleUncompleted = view.findViewById(R.id.recyclerViewScheduleUncompleted);
        recyclerViewScheduleCompleted = view.findViewById(R.id.recyclerViewScheduleCompleted);
        btnOpenDatePicker = view.findViewById(R.id.btnOpenDatePicker);
        fab = view.findViewById(R.id.fab_add_schedule);
        scheduleListUncompleted = new ArrayList<>();
        scheduleListCompleted = new ArrayList<>();
    }
}