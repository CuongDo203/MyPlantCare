package com.example.myplantcare.fragments;

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
import com.example.myplantcare.adapters.ScheduleDetailAdapter;
import com.example.myplantcare.data.responses.ScheduleWithMyPlantInfo;
import com.example.myplantcare.models.DayModel;
import com.example.myplantcare.viewmodels.ScheduleViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ScheduleFragment extends Fragment implements
//        ScheduleDetailAdapter.OnTaskCompletionChangeListener,
        ScheduleAdapter.OnTaskGroupActionListener
{
    private static final String TAG = "ScheduleFragment";
    private RecyclerView recyclerViewDays, recyclerViewScheduleUncompleted, recyclerViewScheduleCompleted;
    private DayAdapter dayAdapter;
    private ScheduleAdapter scheduleAdapterUncomplete, scheduleAdapterCompleted;
    private ScheduleViewModel scheduleViewModel;
    private List<DayModel> dayList;
    private List<Pair<String, List<ScheduleWithMyPlantInfo>>> scheduleListUncompleted, scheduleListCompleted;
    private ImageView btnOpenDatePicker;
    private FloatingActionButton fab;
    private String userId;

    private Calendar currentSelectedDate = Calendar.getInstance();
    // Tạo danh sách ngày
    private List<DayModel> generateDays() {
        List<DayModel> days = new ArrayList<>();
        // Lấy ngày hiện tại và set giờ về 0 để so sánh chính xác
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);


        Calendar calendar = (Calendar) todayCalendar.clone(); // Clone today's calendar

        // Di chuyển về 3 ngày trước ngày hiện tại
        calendar.add(Calendar.DAY_OF_MONTH, -3);

        String[] weekdays = {"CN", "Th 2", "Th 3", "Th 4", "Th 5", "Th 6", "Th 7"}; // Vietnamese weekdays

        for (int i = 0; i < 7; i++) { // Generate 7 days
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String dayOfWeek = weekdays[calendar.get(Calendar.DAY_OF_WEEK) - 1];

            // So sánh với ngày hiện tại (chỉ ngày, tháng, năm)
            boolean isToday = calendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == todayCalendar.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == todayCalendar.get(Calendar.DAY_OF_MONTH);

            // Mặc định ngày hiện tại là ngày được chọn ban đầu
            boolean isSelected = isToday;
            if (isSelected) {
                // Nếu đây là ngày hiện tại, lưu nó làm ngày được chọn mặc định
                currentSelectedDate = (Calendar) calendar.clone();
            }


            days.add(new DayModel(dayOfWeek, day, isToday, isSelected, (Calendar) calendar.clone())); // Pass isSelected and cloned Calendar
            calendar.add(Calendar.DAY_OF_MONTH, 1); // Move to the next day
        }
        return days;
    }

    private void openDatePicker() {
        // Use the currentSelectedDate as the initial selection in the picker
        long initialSelection = currentSelectedDate != null ? currentSelectedDate.getTimeInMillis() : MaterialDatePicker.todayInUtcMilliseconds();

        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Chọn ngày")
                        .setSelection(initialSelection) // Set initial selection
                        .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Chuyển đổi timestamp sang ngày tháng
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);

            // Cập nhật ngày được chọn
            currentSelectedDate = (Calendar) calendar.clone();
            Log.d(TAG, "Date selected from picker: " + currentSelectedDate.getTime().toString()); // Use TAG

            // TODO: Load schedules for the newly selected date
            // Bạn cần gọi phương thức trong ScheduleViewModel để tải lịch trình cho ngày này
            if (scheduleViewModel != null) {
                scheduleViewModel.loadSchedulesForDate(currentSelectedDate);
                Log.d(TAG, "Calling loadSchedulesForDate for selected date."); // Use TAG
            } else {
                Log.w(TAG, "ScheduleViewModel is null, cannot load schedules for selected date."); // Use TAG
            }
            // Cập nhật UI của danh sách ngày (để highlight ngày mới được chọn)
            updateDayListSelection(currentSelectedDate);
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
        dayAdapter.setOnDaySelectedListener(this::onDaySelected);
        recyclerViewDays.setAdapter(dayAdapter);

        int initialSelectedPosition = dayAdapter.getSelectedPosition(); // Assuming DayAdapter has getSelectedPosition()
        if (initialSelectedPosition != RecyclerView.NO_POSITION) {
            recyclerViewDays.scrollToPosition(initialSelectedPosition);
        }

        Log.d("ScheduleFragment", "Size cv can lam o onCreateView: " + scheduleListUncompleted.size());
        recyclerViewScheduleUncompleted.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduleAdapterUncomplete = new ScheduleAdapter(scheduleListUncompleted,false, this);
        recyclerViewScheduleUncompleted.setAdapter(scheduleAdapterUncomplete);

        recyclerViewScheduleCompleted.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduleAdapterCompleted = new ScheduleAdapter(scheduleListCompleted,true, this);
        recyclerViewScheduleCompleted.setAdapter(scheduleAdapterCompleted);

        btnOpenDatePicker.setOnClickListener(v -> openDatePicker());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load data for the currently selected date when the fragment becomes visible
        if (scheduleViewModel != null && currentSelectedDate != null) { // Check for null
            scheduleViewModel.loadSchedulesForDate(currentSelectedDate);
            Log.d(TAG, "onResume: Calling loadSchedulesForDate for selected date: " + currentSelectedDate.getTime().toString()); // Use TAG
        } else {
            Log.w(TAG, "onResume: ViewModel or selected date is null, cannot load schedules."); // Use TAG
        }
    }

    private void onDaySelected(DayModel dayModel, int position) {
        Log.d(TAG, "Day selected: " + dayModel.getDayOfMonth() + "/" + (dayModel.getCalendar().get(Calendar.MONTH) + 1)); // Use TAG
        // Update the Fragment's selected date
        currentSelectedDate = dayModel.getCalendar();

        // Load schedules for the newly selected date
        if (scheduleViewModel != null) {
            scheduleViewModel.loadSchedulesForDate(currentSelectedDate);
            Log.d(TAG, "Calling loadSchedulesForDate for selected date from adapter click."); // Use TAG
        } else {
            Log.w(TAG, "ScheduleViewModel is null, cannot load schedules for selected date from adapter click."); // Use TAG
        }
    }

    private void updateDayListSelection(Calendar selectedDate) {
        if (dayList == null || dayAdapter == null || selectedDate == null) {
            Log.w(TAG, "Cannot update day list selection: list, adapter or date is null.");
            return;
        }

        int oldSelectedPosition = dayAdapter.getSelectedPosition();
        int newSelectedPosition = RecyclerView.NO_POSITION;

        // Normalize selectedDate to zero time for accurate comparison with DayModel calendar
        Calendar selectedDateZeroTime = (Calendar) selectedDate.clone();
        selectedDateZeroTime.set(Calendar.HOUR_OF_DAY, 0);
        selectedDateZeroTime.set(Calendar.MINUTE, 0);
        selectedDateZeroTime.set(Calendar.SECOND, 0);
        selectedDateZeroTime.set(Calendar.MILLISECOND, 0);


        // Find the position of the newly selected date in the current dayList
        for (int i = 0; i < dayList.size(); i++) {
            DayModel day = dayList.get(i);
            Calendar dayCalendar = day.getCalendar(); // This calendar is already at zero time

            // Compare only Day, Month, Year
            if (dayCalendar.get(Calendar.YEAR) == selectedDateZeroTime.get(Calendar.YEAR) &&
                    dayCalendar.get(Calendar.MONTH) == selectedDateZeroTime.get(Calendar.MONTH) &&
                    dayCalendar.get(Calendar.DAY_OF_MONTH) == selectedDateZeroTime.get(Calendar.DAY_OF_MONTH)) {
                newSelectedPosition = i;
                break;
            }
        }

        // Update selection in the list and notify adapter
        if (newSelectedPosition != RecyclerView.NO_POSITION) {
            // Deselect old item if it exists and is different from the new one
            if (oldSelectedPosition != RecyclerView.NO_POSITION && oldSelectedPosition != newSelectedPosition) {
                dayList.get(oldSelectedPosition).setSelected(false);
                dayAdapter.notifyItemChanged(oldSelectedPosition);
            }
            // Select the new item
            dayList.get(newSelectedPosition).setSelected(true);
            dayAdapter.setSelectedPosition(newSelectedPosition); // Assuming DayAdapter has this setter
            dayAdapter.notifyItemChanged(newSelectedPosition);

            // Scroll to the newly selected position (optional but good UX)
            recyclerViewDays.scrollToPosition(newSelectedPosition);

        } else {
            // If the selected date is outside the current 7-day window,
            // deselect the old item and reset the selected position in the adapter.
            if (oldSelectedPosition != RecyclerView.NO_POSITION) {
                dayList.get(oldSelectedPosition).setSelected(false);
                dayAdapter.notifyItemChanged(oldSelectedPosition);
            }
            dayAdapter.setSelectedPosition(RecyclerView.NO_POSITION); // Reset selected position
            Log.d(TAG, "Selected date from picker is outside the current day list window.");
            // TODO: Optionally regenerate the day list centered around the new date here
            // if the user selects a date far in the past or future using the DatePicker.
            // This would involve calling generateDays() again with the new date and updating the adapter.
        }
    }
    private void observeViewModel() {
//        scheduleViewModel.todaySchedules.observe(getViewLifecycleOwner(), groupedSchedules  -> {
//            if (groupedSchedules == null || groupedSchedules.isEmpty()) {
//                Toast.makeText(getContext(), "Không có công việc hôm nay", Toast.LENGTH_SHORT).show();
//                scheduleListUncompleted.clear();
//                scheduleAdapterUncomplete.notifyDataSetChanged();
//                return;
//            }
//            scheduleListUncompleted.clear();
//            for (Map.Entry<String, List<ScheduleWithMyPlantInfo>> entry : groupedSchedules.entrySet()) {
//                scheduleListUncompleted.add(new Pair<>(entry.getKey(), entry.getValue()));
//            }
//            Log.d("ScheduleFragment", "Size cv can lam o observeViewModel: " + scheduleListUncompleted.size());
//            scheduleAdapterUncomplete.notifyDataSetChanged();
//        });
        scheduleViewModel.uncompletedSchedules.observe(getViewLifecycleOwner(), groupedUncompletedSchedules -> {
            Log.d(TAG, "Uncompleted Schedules LiveData updated. Groups size: " + (groupedUncompletedSchedules != null ? groupedUncompletedSchedules.size() : "null"));

            scheduleListUncompleted.clear(); // Xóa dữ liệu cũ
            if (groupedUncompletedSchedules != null && !groupedUncompletedSchedules.isEmpty()) {
                // Chuyển đổi Map entries sang List of Pairs cho adapter
                for (Map.Entry<String, List<ScheduleWithMyPlantInfo>> entry : groupedUncompletedSchedules.entrySet()) {
                    scheduleListUncompleted.add(new Pair<>(entry.getKey(), entry.getValue()));
                }
            } else {
                Log.d(TAG, "No uncompleted schedules found for the selected date.");
            }
            // Cập nhật adapter
            if (scheduleAdapterUncomplete != null) {
                scheduleAdapterUncomplete.setSchedules(scheduleListUncompleted); // Sử dụng setSchedules nếu có
                scheduleAdapterUncomplete.notifyDataSetChanged();
            }
        });

        scheduleViewModel.completedSchedules.observe(getViewLifecycleOwner(), groupedCompletedSchedules -> {
            Log.d(TAG, "Completed Schedules LiveData updated. Groups size: " + (groupedCompletedSchedules != null ? groupedCompletedSchedules.size() : "null"));

            scheduleListCompleted.clear(); // Xóa dữ liệu cũ
            if (groupedCompletedSchedules != null && !groupedCompletedSchedules.isEmpty()) {
                // Chuyển đổi Map entries sang List of Pairs cho adapter
                for (Map.Entry<String, List<ScheduleWithMyPlantInfo>> entry : groupedCompletedSchedules.entrySet()) {
                    scheduleListCompleted.add(new Pair<>(entry.getKey(), entry.getValue()));
                }
                // TODO: Cập nhật UI empty state/RecyclerView visibility
                // showCompletedSchedulesList();
            } else {
                Log.d(TAG, "No completed schedules found for the selected date.");
            }
            // Cập nhật adapter
            if (scheduleAdapterCompleted != null) {
                scheduleAdapterCompleted.setSchedules(scheduleListCompleted); // Sử dụng setSchedules nếu có
                scheduleAdapterCompleted.notifyDataSetChanged();
            }
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

        scheduleViewModel.isMarkingUnmarking.observe(getViewLifecycleOwner(), isMarkingUnmarking -> {
            Log.d(TAG, "isMarkingUnmarking LiveData updated: " + isMarkingUnmarking);
        });

        scheduleViewModel.operationResult.observe(getViewLifecycleOwner(), resultMessage -> {
            if (resultMessage != null && !resultMessage.isEmpty()) {
                Toast.makeText(getContext(), resultMessage, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Operation Result: " + resultMessage);
//                scheduleViewModel.clearOperationResult(); // Xóa thông báo sau khi hiển thị
            }
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

    @Override
    public void onMarkCheckedInGroupCompleteClick(List<ScheduleWithMyPlantInfo> checkedTasksInGroup) {
        Log.d(TAG, "Mark Checked in Group Complete clicked for " + (checkedTasksInGroup != null ? checkedTasksInGroup.size() : 0) + " checked tasks (UNCOMPLETED list).");

        if (scheduleViewModel != null && currentSelectedDate != null && checkedTasksInGroup != null && !checkedTasksInGroup.isEmpty()) {
            // Call ViewModel to mark each checked task as complete
            for (ScheduleWithMyPlantInfo task : checkedTasksInGroup) {
                // ViewModel will handle checking if it's already completed to avoid redundant writes
                scheduleViewModel.markScheduleCompleted(task);
            }
            Toast.makeText(getContext(), "Đang đánh dấu hoàn thành các công việc đã chọn...", Toast.LENGTH_SHORT).show();

        } else if (checkedTasksInGroup == null || checkedTasksInGroup.isEmpty()) {
            Log.d(TAG, "Mark Checked in Group Complete clicked, but no tasks were checked.");
            Toast.makeText(getContext(), "Vui lòng chọn công việc cần hoàn thành.", Toast.LENGTH_SHORT).show();
        } else {
            Log.w(TAG, "ViewModel, currentSelectedDate, or checkedTasksInGroup list is null/empty, cannot mark checked tasks complete.");
            Toast.makeText(getContext(), "Lỗi: Không thể hoàn thành các công việc đã chọn.", Toast.LENGTH_SHORT).show();
        }

        // ViewModel will handle reloading schedules.
    }

    @Override
    public void onUnmarkCheckedInGroupCompleteClick(List<ScheduleWithMyPlantInfo> checkedTasksInGroup) {
        Log.d(TAG, "Unmark Checked in Group (Rollback) clicked for " + (checkedTasksInGroup != null ? checkedTasksInGroup.size() : 0) + " checked tasks (COMPLETED list).");

        if (scheduleViewModel != null && currentSelectedDate != null && checkedTasksInGroup != null && !checkedTasksInGroup.isEmpty()) {
            // Call ViewModel to unmark each checked task as completed
            for (ScheduleWithMyPlantInfo task : checkedTasksInGroup) {
                // ViewModel will handle checking if it's already uncompleted to avoid redundant writes
                scheduleViewModel.unmarkScheduleCompleted(task);
            }
            Toast.makeText(getContext(), "Đang hoàn tác các công việc đã chọn...", Toast.LENGTH_SHORT).show();

        } else if (checkedTasksInGroup == null || checkedTasksInGroup.isEmpty()) {
            Log.d(TAG, "Unmark Checked in Group clicked, but no tasks were checked.");
            Toast.makeText(getContext(), "Vui lòng chọn công việc cần hoàn tác.", Toast.LENGTH_SHORT).show();
        }
        else {
            Log.w(TAG, "ViewModel, currentSelectedDate, or checkedTasksInGroup list is null/empty, cannot unmark checked tasks.");
            Toast.makeText(getContext(), "Lỗi: Không thể hoàn tác các công việc đã chọn.", Toast.LENGTH_SHORT).show();
        }

        // ViewModel will handle reloading schedules.
    }

//    @Override
//    public void onTaskCompletionChanged(ScheduleWithMyPlantInfo task, boolean isChecked) {
//        Log.d(TAG, "Task completion checkbox changed for Schedule ID: " + task.getSchedule().getId() + ", isChecked: " + isChecked);
//
//        if (scheduleViewModel != null && currentSelectedDate != null) {
//            // Call ViewModel method based on the checkbox state
//            if (isChecked) {
//                scheduleViewModel.markScheduleCompleted(task); // Call ViewModel to mark as complete
//            } else {
//                scheduleViewModel.unmarkScheduleCompleted(task); // Call ViewModel to unmark as complete
//            }
//        } else {
//            Log.w(TAG, "ViewModel or currentSelectedDate is null, cannot update task completion.");
//            Toast.makeText(getContext(), "Lỗi: Không thể cập nhật trạng thái công việc.", Toast.LENGTH_SHORT).show();
//        }
//    }
}