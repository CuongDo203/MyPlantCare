package com.example.myplantcare.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.myplantcare.R;
import com.example.myplantcare.data.repositories.MyPlantRepository;
import com.example.myplantcare.data.repositories.MyPlantRepositoryImpl;
import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.utils.FileUtils;
import com.example.myplantcare.utils.FirestoreCallback;
import com.example.myplantcare.viewmodels.ImageViewModel;
import com.example.myplantcare.viewmodels.SpeciesViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddPlantDialogFragment extends DialogFragment {

    private ImageView imageViewPlantPreview;
    private EditText editTextPlantName, editTextLocation;
    private Spinner spinnerPlantType;
    private TextView textViewPlantingDate;
    private ImageButton buttonCloseDialogPlant, buttonOpenDatePicker;
    private Button buttonSavePlant;

    private Calendar plantingDateCalendar = Calendar.getInstance();
    private Uri selectedImageUri = null;

    private SpeciesViewModel speciesViewModel;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private MyPlantRepository myPlantRepository = new MyPlantRepositoryImpl();
    private ImageViewModel imageViewModel;

    public interface OnSavePlantListener {
        void onSavePlant();
    }

    private OnSavePlantListener onSavePlantListener;

    public void setOnSavePlantListener(OnSavePlantListener listener) {
        this.onSavePlantListener = listener;
    }

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
        speciesViewModel = new ViewModelProvider(this).get(SpeciesViewModel.class);
        imageViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new ImageViewModel(requireContext());
            }
        }).get(ImageViewModel.class);
        // --- Tìm Views ---
        imageViewPlantPreview = view.findViewById(R.id.image_view_plant_preview);
        editTextPlantName = view.findViewById(R.id.edit_text_plant_name);
        spinnerPlantType = view.findViewById(R.id.spinner_plant_type);
        textViewPlantingDate = view.findViewById(R.id.text_view_planting_date);
        buttonOpenDatePicker = view.findViewById(R.id.button_open_date_picker);
        buttonCloseDialogPlant = view.findViewById(R.id.button_close_dialog_plant);
        buttonSavePlant = view.findViewById(R.id.button_save_plant);
        editTextLocation = view.findViewById(R.id.edit_text_plant_location);
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

        updatePlantingDateLabel();
        return dialog;
    }

    private void savePlant() {
        String plantName = editTextPlantName.getText().toString().trim();
        String plantingDate = textViewPlantingDate.getText().toString();
        String plantType = "";
        String location = editTextLocation.getText().toString().trim();
        if (spinnerPlantType.getSelectedItemPosition() > 0) { // Bỏ qua item "Chọn loại cây..."
            plantType = spinnerPlantType.getSelectedItem().toString();
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
        String speciesId = speciesViewModel.species.getValue().get(spinnerPlantType.getSelectedItemPosition() - 1).getId();
        MyPlantModel myPlant = new MyPlantModel(plantName, location, speciesId, null);
        String userId = "eoWltJXzlBtC8U8QZx9G";
        myPlant.setUserId(userId);
        myPlant.setProgress(0);

//        String message = "Lưu cây:\nTên: " + plantName + "\nLoại: " + plantType +
//                "\nNgày trồng: " + plantingDate + "\nẢnh URI: " + selectedImageUri.toString();
//        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

        if(selectedImageUri != null) {
            uploadImageToCloudinary(myPlant);
        }
//        else {
            myPlantRepository.addMyPlant(userId, myPlant, new FirestoreCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    Toast.makeText(requireContext(), "Cây đã được lưu", Toast.LENGTH_SHORT).show();
                    Log.d("AddPlantDialogFragment", "Cây đã được lưu");
                    // Gọi callback nếu được gán
                    if (onSavePlantListener != null) {
                        onSavePlantListener.onSavePlant();
                    }

                  dismiss();
                }
                @Override
                public void onError(Exception e) {
                    Toast.makeText(requireContext(), "Lỗi khi lưu cây: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("AddPlantDialogFragment", "Lỗi khi lưu cây: " + e.getMessage(), e);
                }
            });
//        }
    }

    private void uploadImageToCloudinary(MyPlantModel myPlant) {
        String realPath = FileUtils.getRealPathFromUri(requireContext(), selectedImageUri);
        if(realPath == null) {
            Toast.makeText(requireContext(), "Không thể lấy đường dẫn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("AddPlantDialogFragment", "Đường dẫn ảnh: " + realPath);
        File imageFile = new File(realPath);
        imageViewModel.imageUrl.observe(this, url -> {
            if(url!=null) {
                myPlant.setImage(url);
            }
        });
        imageViewModel.uploadImage(imageFile);
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
//        String[] plantTypes = {"Chọn loại cây...", "Indoor", "Outdoor", "Herb", "Succulent", "Cactus"};
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, plantTypes);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerPlantType.setAdapter(adapter);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Đang tải loại cây..."});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlantType.setAdapter(adapter);

        // Quan sát dữ liệu từ ViewModel
        speciesViewModel.species.observe(this, speciesList -> {
            if (speciesList != null && !speciesList.isEmpty()) {
                // Tạo danh sách tên loại cây
                String[] plantTypes = new String[speciesList.size() + 1];
                plantTypes[0] = "Chọn loại cây..."; // Placeholder
                for (int i = 0; i < speciesList.size(); i++) {
                    plantTypes[i + 1] = speciesList.get(i).getName();
                }

                ArrayAdapter<String> updatedAdapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item,
                        plantTypes);
                updatedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPlantType.setAdapter(updatedAdapter);
            } else {
                // Không có dữ liệu
                ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item,
                        new String[]{"Không tìm thấy loại cây"});
                emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPlantType.setAdapter(emptyAdapter);
            }
        });

        speciesViewModel.errorMessage.observe(this, error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
