package com.example.myplantcare.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myplantcare.R;
import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.models.ScheduleModel;
import com.example.myplantcare.models.TaskModel;
import com.example.myplantcare.viewmodels.AddScheduleViewModel;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddScheduleDialogFragment extends DialogFragment implements AddPlantDialogFragment.OnSavePlantListener{

    private Spinner spinnerPlant, spinnerTask, spinnerFrequency;
    private TextView textViewStartDate, textViewStartTime;
    private EditText editTextNote;
    private Button buttonAddPlant, buttonSaveSchedule;
    private ImageButton buttonCloseDialog;

    private LinearLayout layoutCustomFrequencyDays; // <-- Layout cha mới
    private EditText editTextCustomFrequencyDays;

    private Calendar selectedDateCalendar = Calendar.getInstance();
//    private MyPlantListViewModel myPlantListViewModel;
    private AddScheduleViewModel addScheduleViewModel;

    private List<MyPlantModel> userPlants = new ArrayList<>();
    private List<TaskModel> allTasks = new ArrayList<>();
    private List<String> frequencyOptions = new ArrayList<>();
    private String userId;
    private String myPlantId;
    // Selected values (có thể lưu tạm)
    private MyPlantModel selectedPlant = null;
    private TaskModel selectedTask = null;
    private String selectedFrequency = null;
    private String selectedFrequencyText = null;
    private Calendar selectedStartTime;
    private int selectedCustomDays = -1;

    public interface OnScheduleSavedListener {
        void onScheduleSaved();
    }

    private OnScheduleSavedListener onScheduleSavedListener;

    public void setOnScheduleSavedListener(OnScheduleSavedListener listener) {
        this.onScheduleSavedListener = listener;
    }

    public static AddScheduleDialogFragment newInstance(String plantId, String userId) {
        AddScheduleDialogFragment fragment = new AddScheduleDialogFragment();
        Bundle args = new Bundle();
        args.putString("myPlantId", plantId);
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy ID cây và User ID từ arguments
        if (getArguments() != null) {
            myPlantId = getArguments().getString("myPlantId");
            userId = getArguments().getString("userId");
        }
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(getContext(), "Lỗi: Không có thông tin người dùng.", Toast.LENGTH_SHORT).show();
            dismiss(); // Đóng dialog nếu không có user ID
        }
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_schedule, null);
        initContents(view);
        addScheduleViewModel = new ViewModelProvider(this, new AddScheduleViewModel.Factory(userId, myPlantId)).get(AddScheduleViewModel.class);
        setupSpinners();
        setupObservers();
        setupClickListeners();

        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        return dialog;
    }

    private void setupClickListeners() {
        buttonCloseDialog.setOnClickListener(v -> dismiss());
        textViewStartDate.setOnClickListener(v -> showDatePickerDialog());
        textViewStartTime.setOnClickListener(v -> showTimePickerDialog());
        buttonSaveSchedule.setOnClickListener(v -> saveSchedule());
        buttonAddPlant.setOnClickListener(v -> showAddPlantDialog());

    }

    private void setupObservers() {
        addScheduleViewModel.userPlants.observe(this, plants -> {
            if(plants != null) {
                userPlants = plants;
                updatePlantSpinner(plants);
            }
            else {
                userPlants = new ArrayList<>();
                updatePlantSpinner(new ArrayList<>());
            }
        });
        addScheduleViewModel.allTasks.observe(this, tasks -> {
            updateTaskSpinner(tasks);
        });
        addScheduleViewModel.saveResult.observe(this, result -> {
            if(result != null && result) {
                if (onScheduleSavedListener != null) {
                    onScheduleSavedListener.onScheduleSaved();
                }
                Toast.makeText(getContext(), "Lưu lịch trình thành công.", Toast.LENGTH_SHORT).show();
                dismiss();
            }
            else {
                Toast.makeText(getContext(), "Lỗi lưu lịch trình.", Toast.LENGTH_SHORT).show();
            }
            addScheduleViewModel.clearSaveResult();
        });
        addScheduleViewModel.errorMessage.observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e("AddScheduleDialogFragment", "ViewModel Error: " + errorMessage);
                addScheduleViewModel.clearErrorMessage(); // Xóa lỗi sau khi hiển thị
            }
        });
    }

    private void updateTaskSpinner(List<TaskModel> tasks) {
        Log.d("AddScheduleDialogFragment", "updateTaskSpinner called"+ tasks.size());
        if (tasks != null) {
            List<String> taskNames = new ArrayList<>();
            // Thêm item gợi ý/placeholder đầu tiên nếu cần thiết
            // taskNames.add("Chọn công việc...");
            allTasks = tasks;
            for (TaskModel task : tasks) {
                taskNames.add(task.getName()); // Sử dụng tên hiển thị
            }
            // Cập nhật Adapter cho Spinner task
            ArrayAdapter<String> taskAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, taskNames);
            taskAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTask.setAdapter(taskAdapter);

            // Thiết lập listener cho Spinner task (để lấy selectedTask)
            spinnerTask.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Bỏ qua item placeholder nếu có
                    // if (position > 0) { // Nếu có placeholder ở vị trí 0
                    //      selectedTask = allTasks.get(position - 1);
                    // } else {
                    //      selectedTask = null; // placeholder
                    // }
                    if (position < allTasks.size()) { // Đảm bảo vị trí hợp lệ
                        selectedTask = allTasks.get(position); // Lưu task được chọn
                        Log.d("AddScheduleDialogFragment", "Task selected: " + selectedTask.getName() + ", ID: " + selectedTask.getId());
                    } else {
                        selectedTask = null;
                        Log.d("AddScheduleDialogFragment", "No task selected.");
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedTask = null;
                }
            });

        } else {
            allTasks = new ArrayList<>();
            // Cập nhật Adapter với danh sách rỗng nếu không có tasks
            ArrayAdapter<String> emptyTaskAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
            emptyTaskAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTask.setAdapter(emptyTaskAdapter);
            Log.w("AddScheduleDialogFragment", "Tasks data is null or empty.");
            Toast.makeText(getContext(), "Không tải được danh sách công việc.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePlantSpinner(List<MyPlantModel> plants) {
        List<String> plantNames = new ArrayList<>();
        plantNames.add("Chọn 1 cây..."); // Thêm item gợi ý/placeholder đầu tiên
        int selectedPlantPosition = -1;
        if (plants != null && !plants.isEmpty()) {
            userPlants = plants;
            for (int i = 0; i < plants.size(); i++) {
                MyPlantModel plant = plants.get(i);
                plantNames.add(plant.getNickname()); // Sử dụng nickname
                // Nếu đang ở chi tiết cây cụ thể, tìm vị trí của cây đó
                if (!TextUtils.isEmpty(myPlantId) && plant.getId() != null && plant.getId().equals(myPlantId)) {
                    selectedPlantPosition = i;
                }
            }
            ArrayAdapter<String> plantAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, plantNames);
            plantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPlant.setAdapter(plantAdapter);


            // Nếu mở từ chi tiết cây, chọn sẵn cây đó trong Spinner
            if (!TextUtils.isEmpty(myPlantId) && selectedPlantPosition != -1) {
                spinnerPlant.setSelection(selectedPlantPosition);
            } else if (plants.size() > 0) {
                // Nếu không mở từ chi tiết cây, chọn cây đầu tiên mặc định
                // spinnerPlant.setSelection(0); // Tùy chọn, hoặc để người dùng tự chọn
            }

            spinnerPlant.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d("AddScheduleDialogFragment", "plant selected position "+position);
                    // Bỏ qua item placeholder nếu có
                     if (position > 0) { // Nếu có placeholder ở vị trí 0
                          selectedPlant = userPlants.get(position-1);
                         Log.d("AddScheduleDialogFragment", "Plant selected: " + selectedPlant.getNickname() + ", ID: " + selectedPlant.getId());
                     } else {
                          selectedPlant = null; // placeholder
                         Log.d("AddScheduleDialogFragment", "No plant selected.");
                     }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedPlant = null;
                }
            });
        } else {
            Log.d("AddScheduleDialogFragment", "No plants found or error loading plants.");
            userPlants = new ArrayList<>(); // Đặt danh sách rỗng khi data null
            ArrayAdapter<String> emptyPlantAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
            emptyPlantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPlant.setAdapter(emptyPlantAdapter);
            Toast.makeText(getContext(), "Không tìm thấy cây nào.", Toast.LENGTH_SHORT).show();
        }


        ArrayAdapter<String> plantAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, plantNames);
        plantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlant.setAdapter(plantAdapter);
    }

    private void showAddPlantDialog() {
        //Mở dialog thêm cây
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Lỗi: Không có thông tin người dùng.", Toast.LENGTH_SHORT).show();
            return;
        }
        AddPlantDialogFragment addPlantDialog = AddPlantDialogFragment.newInstance(null, userId);
        // --- Thiết lập Listener để nhận thông báo khi cây được lưu thành công ---
        addPlantDialog.setOnSavePlantListener(this); // 'this' là AddScheduleDialogFragment
        addPlantDialog.show(getParentFragmentManager(), "AddPlantDialog");
    }

    @Override
    public void onSavePlant() {
        // Phương thức này được gọi khi cây được lưu thành công trong AddPlantDialogFragment
        Log.d("AddScheduleDialogFragment", "onSavePlant() called in AddScheduleDialogFragment. Refreshing plant list.");
        addScheduleViewModel.loadUserPlants();
        Toast.makeText(requireContext(), "Danh sách cây đã được làm mới.", Toast.LENGTH_SHORT).show(); // Thông báo tạm
    }

    private void saveSchedule() {
        if (selectedPlant == null || TextUtils.isEmpty(selectedPlant.getId())) {
            Toast.makeText(getContext(), "Vui lòng chọn cây.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTask == null || TextUtils.isEmpty(selectedTask.getId())) {
            Toast.makeText(getContext(), "Vui lòng chọn công việc.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (textViewStartDate.getText().toString().equals("Chọn ngày")) {
            Toast.makeText(getContext(), "Vui lòng chọn ngày bắt đầu.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (textViewStartTime.getText().toString().equals("Chọn giờ")) {
            Toast.makeText(getContext(), "Vui lòng chọn giờ.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(selectedFrequencyText)) {
            Toast.makeText(getContext(), "Vui lòng chọn tần suất.", Toast.LENGTH_SHORT).show();
            return;
        }

        int customDays = -1;
        if (selectedFrequencyText.equals("x ngày 1 lần")) {
            String daysText = editTextCustomFrequencyDays.getText().toString().trim();
            if (TextUtils.isEmpty(daysText)) {
                Toast.makeText(getContext(), "Vui lòng nhập số ngày cho tần suất tùy chỉnh.", Toast.LENGTH_SHORT).show();
                editTextCustomFrequencyDays.requestFocus();
                return;
            }
            try {
                customDays = Integer.parseInt(daysText);
                if (customDays <= 0) {
                    Toast.makeText(getContext(), "Số ngày phải lớn hơn 0.", Toast.LENGTH_SHORT).show();
                    editTextCustomFrequencyDays.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Số ngày không hợp lệ.", Toast.LENGTH_SHORT).show();
                editTextCustomFrequencyDays.requestFocus();
                return;
            }
        }

        ScheduleModel newSchedule = new ScheduleModel();
        newSchedule.setTaskId(selectedTask.getId());
        newSchedule.setFrequency(convertFrequencyToDays(selectedFrequencyText));
        newSchedule.setStartDate(new Timestamp(selectedDateCalendar.getTime()));
        newSchedule.setTime(new Timestamp(selectedStartTime.getTime()));
        if (userId != null && selectedPlant != null && selectedPlant.getId() != null) {
            Log.d("AddScheduleDialogFragment", "Saving schedule for plant ID: " + selectedPlant.getId() + " and user ID: " + userId);
            addScheduleViewModel.addSchedule(selectedPlant.getId(), newSchedule);
        } else {
            Toast.makeText(getContext(), "Lỗi: Thiếu thông tin cây hoặc người dùng để lưu.", Toast.LENGTH_SHORT).show();
        }


    }
    private int convertFrequencyToDays(String frequency) {
        switch (frequency){
            case "Hàng ngày":
                return 1;
            case "Hàng tuần":
                return 7;
            default:
                return Integer.parseInt(editTextCustomFrequencyDays.getText().toString().trim());
        }
    }
    private void showTimePickerDialog() {
        if (selectedStartTime == null) {
            selectedStartTime = Calendar.getInstance();
        }

        final Calendar c = (Calendar) selectedStartTime.clone();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute1) -> {
                    selectedStartTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedStartTime.set(Calendar.MINUTE, minute1);
                    updateStartTimeLabel();
                }, hour, minute, true);

        timePickerDialog.show();
    }
    private void updateStartTimeLabel() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h : mm a", Locale.getDefault()); // a for AM/PM
        textViewStartTime.setText(timeFormat.format(selectedDateCalendar.getTime()));
    }

    private void showDatePickerDialog() {
        final Calendar c = selectedDateCalendar != null ? selectedDateCalendar : Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Lưu ngày đã chọn vào selectedDateCalendar
                    selectedDateCalendar.set(Calendar.YEAR, year1);
                    selectedDateCalendar.set(Calendar.MONTH, monthOfYear);
                    selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    // Cập nhật TextView hiển thị ngày
                    updateStartDateLabel();
                }, year, month, day);

        // Giới hạn ngày có thể chọn (tùy chọn)
        // datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void updateStartDateLabel() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd / MM / yyyy", Locale.getDefault());
        textViewStartDate.setText(dateFormat.format(selectedDateCalendar.getTime()));
    }
    private void initContents(View view) {
        spinnerPlant = view.findViewById(R.id.spinner_plant);
        spinnerTask = view.findViewById(R.id.spinner_task);
        spinnerFrequency = view.findViewById(R.id.spinner_frequency);
        textViewStartDate = view.findViewById(R.id.text_view_start_date);
        textViewStartTime = view.findViewById(R.id.text_view_start_time);
        editTextNote = view.findViewById(R.id.edit_text_note);
        buttonAddPlant = view.findViewById(R.id.button_add_plant);
        buttonSaveSchedule = view.findViewById(R.id.button_save_schedule);
        buttonCloseDialog = view.findViewById(R.id.button_close_dialog);
        layoutCustomFrequencyDays = view.findViewById(R.id.layout_custom_frequency_days);
        editTextCustomFrequencyDays = view.findViewById(R.id.edit_text_custom_frequency_days);
    }
    private void setupSpinners() {
        // ---- Cây ----
        ArrayAdapter<String> plantAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        plantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlant.setAdapter(plantAdapter);

        // ---- Công việc ----
        ArrayAdapter<String> taskAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        taskAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTask.setAdapter(taskAdapter);

        // ---- Tần suất ----
        frequencyOptions.clear();
        frequencyOptions.add("Hàng ngày");
        frequencyOptions.add("Hàng tuần");
        frequencyOptions.add("x ngày 1 lần");
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, frequencyOptions);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(frequencyAdapter);

        spinnerFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = (String) parent.getItemAtPosition(position);
                Log.d("AddScheduleDialogFragment", "Frequency selected: " + selectedOption);
                selectedFrequencyText = selectedOption; // Lưu text tùy chọn đã chọn

                // *** Logic hiển thị/ẩn EditText số ngày ***
                if ("x ngày 1 lần".equals(selectedOption)) {
                    layoutCustomFrequencyDays.setVisibility(View.VISIBLE);
                     editTextCustomFrequencyDays.requestFocus();
                } else {
                    layoutCustomFrequencyDays.setVisibility(View.GONE);
                    editTextCustomFrequencyDays.setText("");
                    selectedCustomDays = -1;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                layoutCustomFrequencyDays.setVisibility(View.GONE);
                editTextCustomFrequencyDays.setText("");
                selectedCustomDays = -1;
            }
        });
    }
}
