package com.example.myplantcare.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.myplantcare.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddScheduleDialogFragment extends DialogFragment {

    private Spinner spinnerPlant, spinnerTask, spinnerFrequency;
    private TextView textViewStartDate, textViewStartTime;
    private EditText editTextNote;
    private Button buttonAddPlant, buttonSaveSchedule;
    private ImageButton buttonCloseDialog;

    private Calendar selectedDateCalendar = Calendar.getInstance();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_schedule, null);
        initContents(view);
        setupSpinners();

        buttonCloseDialog.setOnClickListener(v -> dismiss());
        textViewStartDate.setOnClickListener(v -> showDatePickerDialog());
        textViewStartTime.setOnClickListener(v -> showTimePickerDialog());
        buttonSaveSchedule.setOnClickListener(v -> saveSchedule());
        buttonAddPlant.setOnClickListener(v -> addPlant());

        builder.setView(view);
        AlertDialog dialog = builder.create();
        // Set nền trong suốt để các góc bo tròn của layout XML hiển thị đúng
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        return dialog;
    }

    private void addPlant() {
        Toast.makeText(requireContext(), "Chức năng thêm cây update sau", Toast.LENGTH_SHORT).show();
    }

    private void saveSchedule() {

    }

    private void showTimePickerDialog() {
        TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minute) -> {
            selectedDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDateCalendar.set(Calendar.MINUTE, minute);
            updateStartTimeLabel();
        };

        new TimePickerDialog(requireContext(), timeSetListener,
                selectedDateCalendar.get(Calendar.HOUR_OF_DAY),
                selectedDateCalendar.get(Calendar.MINUTE),
                false) // false = 12h format (AM/PM), true = 24h format
                .show();
    }

    private void updateStartTimeLabel() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h : mm a", Locale.getDefault()); // a for AM/PM
        textViewStartTime.setText(timeFormat.format(selectedDateCalendar.getTime()));
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            selectedDateCalendar.set(Calendar.YEAR, year);
            selectedDateCalendar.set(Calendar.MONTH, month);
            selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateStartDateLabel();
        };

        new DatePickerDialog(requireContext(), dateSetListener,
                selectedDateCalendar.get(Calendar.YEAR),
                selectedDateCalendar.get(Calendar.MONTH),
                selectedDateCalendar.get(Calendar.DAY_OF_MONTH))
                .show();
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
    }

    private void setupSpinners() {
        // ---- Cây ----
        String[] plants = {"Chọn 1 cây...", "Xương rồng", "Sen đá", "Trầu bà"};
        ArrayAdapter<String> plantAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, plants);
        plantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlant.setAdapter(plantAdapter);

        // ---- Công việc ----
        String[] tasks = {"Chọn công việc...", "Tưới nước", "Bón phân", "Cắt tỉa", "Thay đất"};
        ArrayAdapter<String> taskAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tasks);
        taskAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTask.setAdapter(taskAdapter);

        // ---- Tần suất ----
        String[] frequencies = {"Hàng ngày", "2 ngày/lần", "Hàng tuần", "2 tuần/lần", "Hàng tháng"};
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, frequencies);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(frequencyAdapter);
    }
}
