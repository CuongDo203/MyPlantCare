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

import com.example.myplantcare.R;
import com.example.myplantcare.adapters.DayAdapter;
import com.example.myplantcare.adapters.ScheduleAdapter;
import com.example.myplantcare.models.DayModel;
import com.example.myplantcare.models.ScheduleModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleFragment extends Fragment {

    private RecyclerView recyclerViewDays, recyclerViewSchedule;
    private DayAdapter dayAdapter;
    private ScheduleAdapter scheduleAdapter;
    private List<DayModel> dayList;
    private List<ScheduleModel> scheduleList;
    private ImageView btnOpenDatePicker;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ScheduleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScheduleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScheduleFragment newInstance(String param1, String param2) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    // Tạo danh sách ngày
    private List<DayModel> generateDays() {
        List<DayModel> days = new ArrayList<>();
        String[] weekdays = {"Th 2", "Th 3", "Th 4", "Th 5", "Th 6", "Th 7", "CN"};
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
        schedules.add(new ScheduleModel("Tưới nước", "Cây EFG - 9:30 AM - Phòng khách", false));
        schedules.add(new ScheduleModel("Bón phân", "Cây EFG - 10:00 AM - Phòng khách", false));
        schedules.add(new ScheduleModel("Kiểm tra sâu bệnh", "Cây EFG - 11:00 AM - Phòng khách", false));
        return schedules;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    // Xử lý ngày đã chọn
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        recyclerViewDays = view.findViewById(R.id.recyclerViewDays);
        recyclerViewSchedule = view.findViewById(R.id.recyclerViewSchedule);
        btnOpenDatePicker = view.findViewById(R.id.btnOpenDatePicker);

        recyclerViewDays.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        dayList = generateDays();
        dayAdapter = new DayAdapter(dayList);
        recyclerViewDays.setAdapter(dayAdapter);

        recyclerViewSchedule.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduleList = generateSchedules();
        scheduleAdapter = new ScheduleAdapter(scheduleList);
        recyclerViewSchedule.setAdapter(scheduleAdapter);

        btnOpenDatePicker.setOnClickListener(v -> openDatePicker());
        return view;
    }
}