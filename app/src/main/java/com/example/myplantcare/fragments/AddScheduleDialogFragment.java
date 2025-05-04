package com.example.myplantcare.fragments;

import static android.content.Context.ALARM_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myplantcare.R;
import com.example.myplantcare.data.responses.ScheduleDisplayItem;
import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.models.ScheduleModel;
import com.example.myplantcare.models.TaskModel;

import com.example.myplantcare.receivers.NotificationReceiver;
import com.example.myplantcare.utils.DateUtils;
import com.example.myplantcare.viewmodels.AddScheduleViewModel;
import com.example.myplantcare.viewmodels.MyPlantListViewModel;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddScheduleDialogFragment extends DialogFragment implements AddPlantDialogFragment.OnSavePlantListener{
    private static final String TAG = "AddScheduleDialogFragment";
    FirebaseFirestore db;
    private Spinner spinnerPlant, spinnerTask, spinnerFrequency;
    private TextView textViewStartDate, textViewStartTime, title;
    private EditText editTextNote;
    private Button buttonAddPlant, buttonSaveSchedule;
    private ImageButton buttonCloseDialog;
    private LinearLayout layoutCustomFrequencyDays; // <-- Layout cha mới
    private EditText editTextCustomFrequencyDays;
    private Calendar selectedDateCalendar = Calendar.getInstance();
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
    private String scheduleIdToEdit;

    public interface OnScheduleSavedListener {
        void onScheduleSaved();
    }

    private OnScheduleSavedListener onScheduleSavedListener;

    public void setOnScheduleSavedListener(OnScheduleSavedListener listener) {
        this.onScheduleSavedListener = listener;
    }

    public static AddScheduleDialogFragment newInstance(String plantId, String userId, String scheduleId) {
        AddScheduleDialogFragment fragment = new AddScheduleDialogFragment();
        Bundle args = new Bundle();
        args.putString("myPlantId", plantId);
        args.putString("userId", userId);
        args.putString("scheduleId", scheduleId);
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
            scheduleIdToEdit = getArguments().getString("scheduleId");
        }
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(getContext(), "Lỗi: Không có thông tin người dùng.", Toast.LENGTH_SHORT).show();
            dismiss(); // Đóng dialog nếu không có user ID
        }
        db = FirebaseFirestore.getInstance();
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_schedule, null);
        initContents(view);
        addScheduleViewModel = new ViewModelProvider(this, new AddScheduleViewModel.Factory(userId, myPlantId, null)).get(AddScheduleViewModel.class);
        setupSpinners();
        setupObservers();
        setupClickListeners();

        if (scheduleIdToEdit != null && !scheduleIdToEdit.isEmpty()) {
            title.setText("Cập nhật lịch trình");
            buttonSaveSchedule.setText("Cập nhật lịch trình");
            buttonAddPlant.setVisibility(View.GONE); // Ẩn nút thêm cây khi chỉnh sửa
            spinnerPlant.setEnabled(false); // Không cho phép đổi cây khi chỉnh sửa lịch trình
        } else {
            title.setText("Thêm lịch trình mới");
            buttonSaveSchedule.setText("Lưu lịch trình");
            buttonAddPlant.setVisibility(View.VISIBLE);
            spinnerPlant.setEnabled(true);
        }

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
        addScheduleViewModel.scheduleToEdit.observe(this, schedule -> {
            if (schedule != null) {
                Log.d(TAG, "Observed scheduleToEdit: " + schedule.getScheduleId());
                // Điền dữ liệu lên UI khi nhận được schedule để chỉnh sửa
                fillDataWhenUpdate(schedule);
            } else {
                // Xử lý trường hợp không tải được schedule khi ở chế độ chỉnh sửa
                if (scheduleIdToEdit != null && !scheduleIdToEdit.isEmpty()) {
                    Toast.makeText(getContext(), "Không tải được dữ liệu lịch trình.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "scheduleToEdit observer received null for ID: " + scheduleIdToEdit);
                }
            }
        });
    }

    private void updateTaskSpinner(List<TaskModel> tasks) {
        Log.d("AddScheduleDialogFragment", "updateTaskSpinner called"+ tasks.size());
        if (tasks != null) {
            List<String> taskNames = new ArrayList<>();
            allTasks = tasks;
            for (TaskModel task : tasks) {
                taskNames.add(task.getName());
            }
            // Cập nhật Adapter cho Spinner task
            ArrayAdapter<String> taskAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, taskNames);
            taskAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTask.setAdapter(taskAdapter);

            // Thiết lập listener cho Spinner task (để lấy selectedTask)
            spinnerTask.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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

    public void fillDataWhenUpdate(ScheduleDisplayItem schedule) {
        Log.d(TAG, "fillDataWhenUpdate called");
//        if(schedule!=null) {
//            title.setText("Cập nhật lịch trình");
//            textViewStartDate.setText(DateUtils.formatTimestamp(schedule.getStartDate(), DateUtils.DATE_FORMAT_DISPLAY));
//            textViewStartTime.setText(DateUtils.formatTimestampToTimeString(schedule.getTime()));
//            selectedDateCalendar.setTime(schedule.getStartDate().toDate());
//            selectedStartTime = Calendar.getInstance();
//            selectedStartTime.setTime(schedule.getTime().toDate());
//        }
    }
    private void updatePlantSpinner(List<MyPlantModel> plants) {
        List<String> plantNames = new ArrayList<>();
        plantNames.add("Chọn 1 cây...");
        int selectedPlantPosition = -1;
        if (plants != null && !plants.isEmpty()) {
            userPlants = plants;
            for (int i = 0; i < plants.size(); i++) {
                MyPlantModel plant = plants.get(i);
                plantNames.add(plant.getNickname());
                // Nếu đang ở chi tiết cây cụ thể, tìm vị trí của cây đó
                if (!TextUtils.isEmpty(myPlantId) && plant.getId() != null && plant.getId().equals(myPlantId)) {
                    selectedPlantPosition = i+1;
                }
            }
            ArrayAdapter<String> plantAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, plantNames);
            plantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPlant.setAdapter(plantAdapter);

            // Nếu mở từ chi tiết cây, chọn sẵn cây đó trong Spinner
            if (selectedPlantPosition != -1) {
                Log.d(TAG, "selectedPlantPosition: " + selectedPlantPosition);
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
            // Đặt thông báo định kỳ
            setPeriodicNotification(selectedPlant, newSchedule);
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
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
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
        title = view.findViewById(R.id.title);
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

    private void setPeriodicNotification(MyPlantModel myPlant, ScheduleModel schedule) {
        // Kiểm tra tần suất lặp lại từ ScheduleModel (số ngày lặp lại)
        int frequencyInDays = schedule.getFrequency();  // frequency được lưu dưới dạng số ngày
        if (frequencyInDays <= 0) {
            // Nếu tần suất không hợp lệ, không tạo thông báo
            Log.d("Notification", "Tần suất không hợp lệ");
            return;
        }
        // Lấy thời gian thông báo từ ScheduleModel (biến time là Timestamp)
        Timestamp timestamp = schedule.getTime();  // Giả sử time là Timestamp
        if (timestamp == null) {
            Log.d("Notification", "Không có thời gian thông báo");
            return;
        }
        schedule.getTime();
        // Chuyển Timestamp thành Calendar
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp.toDate().getTime());  // Chuyển đổi Timestamp thành Date rồi lấy mili giây

        // Lấy giờ, phút từ Timestamp
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);  // Lấy giờ trong ngày
        int minute = calendar.get(Calendar.MINUTE);  // Lấy phút

        // Thiết lập thời gian gửi thông báo: giờ và phút từ Timestamp, giữ nguyên ngày hiện tại
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);  // Đặt giây = 0 để tránh việc trễ thông báo
        calendar.set(Calendar.MILLISECOND, 0);  // Đặt mili giây = 0
        // Nếu thời gian đã qua trong ngày, đặt thời gian cho ngày tiếp theo
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DATE, 1);  // Thêm một ngày nếu thời gian đã qua
        }
        // Kiểm tra getActivity() có null không trước khi sử dụng
        if (getActivity() == null) {
            Log.e("Notification", "Activity không được đính kèm!");
            return;
        }
        Intent intent = new Intent(getActivity(), NotificationReceiver.class);  // NotificationReceiver là BroadcastReceiver
        DocumentReference docRef = db.collection("tasks").document(schedule.getTaskId());
        // Lấy document từ Firestore
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Tạo nhắc nhở sau khi có kết quả từ Firestore
                    String taskName = document.getString("name");
                    intent.putExtra("plantName", myPlant.getNickname());
                    intent.putExtra("task", taskName);

                    String key = schedule.getId() + schedule.getTime().toDate().getTime();
                    int requestCode = key.hashCode();
                    if (requestCode == Integer.MIN_VALUE) requestCode = 0;
                    else requestCode = Math.abs(requestCode);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    long intervalMillis = frequencyInDays * 24 * 60 * 60 * 1000;
                    long triggerAtMillis = calendar.getTimeInMillis();
                    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

                    alarmManager.setInexactRepeating(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            intervalMillis,
                            pendingIntent);
                    Log.d("AlarmSetup", "" +requestCode);
                } else {
                    Log.d("Firestore", "Document không tồn tại!");
                }
            } else {
                Log.e("Firestore", "Lỗi get document", task.getException());
            }
        });
    }
}
