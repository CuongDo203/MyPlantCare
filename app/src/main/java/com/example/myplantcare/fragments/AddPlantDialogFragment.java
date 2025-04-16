package com.example.myplantcare.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.bumptech.glide.Glide;
import com.example.myplantcare.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddPlantDialogFragment extends DialogFragment {

    private ImageView imageViewPlantPreview;
    private EditText editTextPlantName;
    private Spinner spinnerPlantType;
    private TextView textViewPlantingDate;
    private ImageButton buttonCloseDialogPlant, buttonOpenDatePicker;
    private Button buttonSavePlant;

    private Calendar plantingDateCalendar = Calendar.getInstance();
    private Uri selectedImageUri = null;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đăng ký launcher để xử lý kết quả chọn ảnh
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        // Hiển thị ảnh đã chọn (ví dụ dùng Glide)
                        Glide.with(requireContext())
                                .load(selectedImageUri)
                                .placeholder(R.drawable.ic_change_image) // Ảnh placeholder
                                .error(R.drawable.ic_photo_error) // Ảnh khi lỗi (tùy chọn)
                                .centerCrop()
                                .into(imageViewPlantPreview);
                    }
                });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_plant, null);

        // --- Tìm Views ---
        imageViewPlantPreview = view.findViewById(R.id.image_view_plant_preview);
        editTextPlantName = view.findViewById(R.id.edit_text_plant_name);
        spinnerPlantType = view.findViewById(R.id.spinner_plant_type);
        textViewPlantingDate = view.findViewById(R.id.text_view_planting_date);
        buttonOpenDatePicker = view.findViewById(R.id.button_open_date_picker);
        buttonCloseDialogPlant = view.findViewById(R.id.button_close_dialog_plant);
        buttonSavePlant = view.findViewById(R.id.button_save_plant);

        // --- Setup Spinner Loại cây ---
        setupPlantTypeSpinner();

        // --- Setup Click Listeners ---
        buttonCloseDialogPlant.setOnClickListener(v -> dismiss());
        buttonOpenDatePicker.setOnClickListener(v -> showDatePickerDialog());
        imageViewPlantPreview.setOnClickListener(v -> openImagePicker());
        buttonSavePlant.setOnClickListener(v -> savePlant());

        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    private void savePlant() {
        String plantName = editTextPlantName.getText().toString().trim();
        String plantingDate = textViewPlantingDate.getText().toString();
        String plantType = "";
        if (spinnerPlantType.getSelectedItemPosition() > 0) { // Bỏ qua item "Chọn loại cây..."
            plantType = spinnerPlantType.getSelectedItem().toString();
        }

        // --- Validate ---
        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }
        if (plantName.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập tên cây", Toast.LENGTH_SHORT).show();
            editTextPlantName.setError("Bắt buộc"); // Đặt lỗi trực tiếp trên EditText
            return;
        }
        if (spinnerPlantType.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Vui lòng chọn loại cây", Toast.LENGTH_SHORT).show();
            return;
        }
        if (plantingDate.equals("-- / -- / ----")) {
            Toast.makeText(requireContext(), "Vui lòng chọn ngày trồng", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Xử lý Lưu ---
        // TODO: Gửi dữ liệu (plantName, plantType, plantingDate, selectedImageUri)
        // đến Activity/Fragment/ViewModel để lưu trữ (có thể cần upload ảnh lên server/storage)
        String message = "Lưu cây:\nTên: " + plantName + "\nLoại: " + plantType +
                "\nNgày trồng: " + plantingDate + "\nẢnh URI: " + selectedImageUri.toString();
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

        dismiss();
    }

    private void openImagePicker() {
        // Intent để chọn ảnh từ thư viện
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Chỉ chọn ảnh
        intent.setType("image/*");
        // Khởi chạy activity chọn ảnh và chờ kết quả thông qua launcher đã đăng ký
        imagePickerLauncher.launch(intent);
        // Lưu ý: Cần xử lý quyền truy cập bộ nhớ (READ_EXTERNAL_STORAGE) cho Android đời cũ
        // và không cần quyền cho Android mới hơn khi dùng ACTION_PICK.
        // Bạn cũng có thể thêm lựa chọn chụp ảnh từ camera ở đây.
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            plantingDateCalendar.set(Calendar.YEAR, year);
            plantingDateCalendar.set(Calendar.MONTH, month);
            plantingDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updatePlantingDateLabel();
        };

        new DatePickerDialog(requireContext(), dateSetListener,
                plantingDateCalendar.get(Calendar.YEAR),
                plantingDateCalendar.get(Calendar.MONTH),
                plantingDateCalendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void updatePlantingDateLabel() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd / MM / yyyy", Locale.getDefault());
        textViewPlantingDate.setText(dateFormat.format(plantingDateCalendar.getTime()));
    }

    private void setupPlantTypeSpinner() {
        // TODO: Thay thế bằng dữ liệu loại cây thực tế
        String[] plantTypes = {"Chọn loại cây...", "Indoor", "Outdoor", "Herb", "Succulent", "Cactus"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, plantTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlantType.setAdapter(adapter);
    }


}
