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
// import com.example.myplantcare.utils.FileUtils; // Không cần thiết cho upload Uri
import com.example.myplantcare.utils.FirestoreCallback;
import com.example.myplantcare.viewmodels.AddPlantViewModel;
import com.example.myplantcare.viewmodels.ImageViewModel;
import com.example.myplantcare.viewmodels.SpeciesViewModel;
import com.google.firebase.auth.FirebaseAuth; // Import Firebase Auth
import com.google.firebase.auth.FirebaseUser; // Import FirebaseUser
import com.google.firebase.Timestamp; // Import Timestamp

import java.io.File; // Có thể vẫn cần nếu bạn dùng FileUtils ở đâu đó khác
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.DocumentReference;


public class AddPlantDialogFragment extends DialogFragment {
    private static final String TAG = "AddPlantDialogFragment"; // Sử dụng TAG nhất quán

    private ImageView imageViewPlantPreview;
    private EditText editTextPlantName, editTextLocation;
    private Spinner spinnerPlantType;
    private TextView textViewPlantingDate;
    private ImageButton buttonCloseDialogPlant, buttonOpenDatePicker;
    private Button buttonSavePlant;

    private Calendar plantingDateCalendar = Calendar.getInstance();
    private Uri selectedImageUri = null;

    private SpeciesViewModel speciesViewModel;
    private AddPlantViewModel addPlantViewModel;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private String userId;
    public interface OnSavePlantListener {
        void onSavePlant();
    }

    private OnSavePlantListener onSavePlantListener;
    private String myPlantId;

    public void setOnSavePlantListener(OnSavePlantListener listener) {
        this.onSavePlantListener = listener;
    }

    public AddPlantDialogFragment() {

    }
    public static AddPlantDialogFragment newInstance(String myPlantId, String userId) { // Thêm userId
        AddPlantDialogFragment fragment = new AddPlantDialogFragment();
        Bundle args = new Bundle();
        args.putString("my_plant_id", myPlantId);
        args.putString("user_id", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("user_id");
            myPlantId = getArguments().getString("my_plant_id");
        }
        // Đăng ký launcher để xử lý kết quả chọn ảnh
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // Hiển thị ảnh đã chọn (ví dụ dùng Glide)
                            Glide.with(requireContext())
                                    .load(selectedImageUri)
                                    .placeholder(R.drawable.ic_change_image) // Ảnh placeholder
                                    .error(R.drawable.ic_photo_error) // Ảnh khi lỗi (tùy chọn)
                                    .centerCrop()
                                    .into(imageViewPlantPreview);
                            Log.d(TAG, "Image selected: " + selectedImageUri.toString());
                        } else {
                            Log.d(TAG, "Selected image Uri is null.");
                        }
                    } else {
                        Log.d(TAG, "Image selection cancelled or failed.");
                    }
                });

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_plant, null);

        speciesViewModel = new ViewModelProvider(this).get(SpeciesViewModel.class);

        addPlantViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new AddPlantViewModel();
            }
        }).get(AddPlantViewModel.class);

        imageViewPlantPreview = view.findViewById(R.id.image_view_plant_preview);
        editTextPlantName = view.findViewById(R.id.edit_text_plant_name);
        spinnerPlantType = view.findViewById(R.id.spinner_plant_type);
        textViewPlantingDate = view.findViewById(R.id.text_view_planting_date);
        buttonOpenDatePicker = view.findViewById(R.id.button_open_date_picker);
        buttonCloseDialogPlant = view.findViewById(R.id.button_close_dialog_plant);
        buttonSavePlant = view.findViewById(R.id.button_save_plant);
        editTextLocation = view.findViewById(R.id.edit_text_plant_location);

        setupPlantTypeSpinner();

        buttonCloseDialogPlant.setOnClickListener(v -> dismiss());
        buttonOpenDatePicker.setOnClickListener(v -> showDatePickerDialog());
        imageViewPlantPreview.setOnClickListener(v -> openImagePicker());
        buttonSavePlant.setOnClickListener(v -> savePlant());
        observeViewModels();
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        updatePlantingDateLabel();

        return dialog;
    }

    private void observeViewModels() {
        addPlantViewModel.isLoading.observe(this, isLoading -> {
            showLoading(isLoading);
        });
        addPlantViewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                addPlantViewModel.clearErrorMessage();
            }
        });
        addPlantViewModel.saveSuccess.observe(this, isSuccess -> {
            Log.d(TAG, "AddPlantViewModel saveSuccess: " + isSuccess);
            if (isSuccess != null && isSuccess) { // Kiểm tra isSuccess không null và là true
                Log.d(TAG, "AddPlantViewModel saveSuccess: true");
                Toast.makeText(requireContext(), "Cây đã được lưu", Toast.LENGTH_SHORT).show();
                if (onSavePlantListener != null) {
                    onSavePlantListener.onSavePlant();
                }
                dismiss(); // Đóng dialog sau khi lưu thành công
                addPlantViewModel.clearSaveSuccess(); // Xóa trạng thái thành công
            }
        });
    }


    // --- Logic Lưu cây ---
    private void savePlant() {
        if (this.userId == null || this.userId.isEmpty()) {
            Log.e(TAG, "User ID is null or empty. Cannot save plant.");
            Toast.makeText(requireContext(), "Lỗi: Không có thông tin người dùng.", Toast.LENGTH_SHORT).show();
             dismiss();
            return;
        }

        String plantName = editTextPlantName.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();

        // Lấy ID loại cây đã chọn (speciesId)
        String speciesId = null;
        if (spinnerPlantType.getSelectedItemPosition() > 0) {
            if (speciesViewModel.species.getValue() != null &&
                    spinnerPlantType.getSelectedItemPosition() - 1 < speciesViewModel.species.getValue().size()) {
                speciesId = speciesViewModel.species.getValue().get(spinnerPlantType.getSelectedItemPosition() - 1).getId();
                Log.d(TAG, "Selected speciesId: " + speciesId);
            } else {
                Log.e(TAG, "Species list or selected position is invalid.");
                Toast.makeText(requireContext(), "Lỗi chọn loại cây", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // --- Validate Input ---
        if (plantName.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập tên cây", Toast.LENGTH_SHORT).show();
            editTextPlantName.setError("Bắt buộc");
            return;
        }
        if (speciesId == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn loại cây", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Current User ID: " + userId);

        MyPlantModel myPlantToSave = new MyPlantModel(); // Sử dụng constructor rỗng nếu có setters
        myPlantToSave.setNickname(plantName); // Giả sử nickname là tên cây nhập vào
        myPlantToSave.setLocation(location);
        myPlantToSave.setSpeciesId(speciesId); // Lưu speciesId vào trường plantId trong MyPlantModel
        myPlantToSave.setUserId(userId);
        // myPlantToSave.setProgress(0); // Set progress nếu cần
        // myPlantToSave.setCreatedAt(new Timestamp(plantingDateCalendar.getTime())); // Lưu ngày trồng vào created_at hoặc trường riêng

        addPlantViewModel.savePlant(userId, myPlantToSave, selectedImageUri);
    }

    // Hàm hiển thị/ẩn trạng thái tải
    private void showLoading(boolean isLoading) {
        buttonSavePlant.setEnabled(!isLoading); // Disable nút Save khi đang tải
    }


    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            plantingDateCalendar.set(Calendar.YEAR, year);
            plantingDateCalendar.set(Calendar.MONTH, month);
            plantingDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updatePlantingDateLabel();
        };

        Calendar now = Calendar.getInstance();
        new DatePickerDialog(requireContext(), dateSetListener,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void updatePlantingDateLabel() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd / MM /yyyy", Locale.getDefault());
        textViewPlantingDate.setText(dateFormat.format(plantingDateCalendar.getTime()));
    }

    private void setupPlantTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Đang tải loại cây..."});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlantType.setAdapter(adapter);

        speciesViewModel.species.observe(this, speciesList -> {
            if (speciesList != null && !speciesList.isEmpty()) {
                Log.d(TAG, "Species data received: " + speciesList.size() + " items.");
                String[] plantTypes = new String[speciesList.size() + 1];
                plantTypes[0] = "Chọn loại cây...";
                for (int i = 0; i < speciesList.size(); i++) {
                    plantTypes[i + 1] = speciesList.get(i).getName();
                }

                ArrayAdapter<String> updatedAdapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item,
                        plantTypes);
                updatedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPlantType.setAdapter(updatedAdapter);
            } else {
                Log.d(TAG, "Species list is null or empty.");
                ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item,
                        new String[]{"Không tìm thấy loại cây"});
                emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPlantType.setAdapter(emptyAdapter);
                Toast.makeText(requireContext(), "Không thể tải danh sách loại cây", Toast.LENGTH_SHORT).show();
            }
        });

        speciesViewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "SpeciesViewModel error: " + error);
                Toast.makeText(requireContext(), "Lỗi tải loại cây: " + error, Toast.LENGTH_SHORT).show();
                // speciesViewModel.clearErrorMessage(); // Đảm bảo ViewModel có phương thức này
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Loại bỏ các observer khi View bị hủy
        if (speciesViewModel.species.hasObservers()) speciesViewModel.species.removeObservers(this);
        if (speciesViewModel.errorMessage.hasObservers()) speciesViewModel.errorMessage.removeObservers(this);
    }
}