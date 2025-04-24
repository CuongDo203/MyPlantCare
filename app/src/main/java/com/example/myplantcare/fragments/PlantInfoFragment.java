package com.example.myplantcare.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.myplantcare.R;
import com.example.myplantcare.adapters.PlantInfoAdapter;
import com.example.myplantcare.data.responses.PlantResponse;
import com.example.myplantcare.models.SpeciesModel;
import com.example.myplantcare.viewmodels.PlantInfoViewModel;

import java.util.ArrayList;
import java.util.List;

public class PlantInfoFragment extends Fragment {

    private RecyclerView recyclerView;
    private PlantInfoAdapter plantInfoAdapter;
    private PlantInfoViewModel plantInfoViewModel;
    List<PlantResponse> plantInfos;
    private LottieAnimationView lottieLoadingAnimation;
    private ImageView btnFilter;

    private LinearLayout layoutFilterOptions;
    private EditText editTextSearchPlantName;
    private Spinner spinnerFilterPlantType;
    private Button buttonHideFilter;

    // Empty state UI elements
    private LinearLayout layoutEmptyPlants;
    private LottieAnimationView lottieEmptyPlantsAnimation;
    private TextView textEmptyPlantsMessage;

    private List<SpeciesModel> speciesList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_info, container, false);
        initContents(view);
        setupViewModel();
        observeViewModel();
        btnFilter.setOnClickListener(v -> toggleFilterOptionsVisibility());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupFilterListeners();
    }

    private void toggleFilterOptionsVisibility() {
        if (layoutFilterOptions.getVisibility() == View.GONE) {
            layoutFilterOptions.setVisibility(View.VISIBLE);
        } else {
            layoutFilterOptions.setVisibility(View.GONE);
        }
    }

    private void setupFilterListeners() {
        editTextSearchPlantName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("PlantInfo", "Search text changed: " + s.toString());
                String selectedSpeciesId = null;
                int selectedPosition = spinnerFilterPlantType.getSelectedItemPosition();
                if (selectedPosition > 0 && selectedPosition - 1 < speciesList.size()) {
                    // Lấy SpeciesModel tương ứng và lấy ID của nó
                    SpeciesModel selectedSpecies = speciesList.get(selectedPosition - 1); // Điều chỉnh index vì có item "Tất cả" ở đầu
                    selectedSpeciesId = selectedSpecies.getId();
                }
                plantInfoViewModel.applyFilter(s.toString(), selectedSpeciesId);
            }
        });
        // Listener cho nút ẩn bộ lọc (nếu nút này tồn tại)
        if (buttonHideFilter != null) {
            buttonHideFilter.setOnClickListener(v -> toggleFilterOptionsVisibility());
        }
    }

    private void setupViewModel() {
        plantInfoViewModel = new ViewModelProvider(this).get(PlantInfoViewModel.class);
    }

    private void observeViewModel() {
        plantInfoViewModel.plants.observe(getViewLifecycleOwner(), plants -> {
            Log.d("PlantInfo", "plants LiveData updated. Size: " + (plants != null ? plants.size() : "null"));

            if (plants != null && !plants.isEmpty()) {
                // Có dữ liệu - hiển thị RecyclerView, ẩn empty state
                plantInfoAdapter.setPlantInfos(plants); // Sử dụng setPlantInfos (cần thêm vào Adapter)
                recyclerView.setVisibility(View.VISIBLE);
                layoutEmptyPlants.setVisibility(View.GONE); // Ẩn empty state
                lottieEmptyPlantsAnimation.pauseAnimation(); // Dừng animation rỗng

            } else {
                // Danh sách rỗng hoặc null - hiển thị empty state, ẩn RecyclerView
                plantInfoAdapter.setPlantInfos(new ArrayList<>()); // Cập nhật adapter với danh sách rỗng
                recyclerView.setVisibility(View.GONE); // Ẩn RecyclerView
                layoutEmptyPlants.setVisibility(View.VISIBLE); // Hiển thị empty state
                lottieEmptyPlantsAnimation.playAnimation(); // Bắt đầu animation rỗng
                textEmptyPlantsMessage.setText("Không tìm thấy cây phù hợp."); // Tùy chỉnh nếu do lọc
            }

        });

        plantInfoViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                // Hiển thị animation loading chính, ẩn RecyclerView và empty state
                lottieLoadingAnimation.setVisibility(View.VISIBLE);
                lottieLoadingAnimation.playAnimation();
                recyclerView.setVisibility(View.GONE);
                layoutFilterOptions.setVisibility(View.GONE); // Ẩn bộ lọc khi đang tải
                layoutEmptyPlants.setVisibility(View.GONE); // Ẩn empty state
                lottieEmptyPlantsAnimation.pauseAnimation(); // Tạm dừng animation rỗng
            } else {
                lottieLoadingAnimation.setVisibility(View.GONE);
                lottieLoadingAnimation.cancelAnimation();
            }
        });

        plantInfoViewModel.getSpeciesList().observe(getViewLifecycleOwner(), species -> { // Use getViewLifecycleOwner()
            if (species != null) {
                speciesList = species; // Lưu danh sách species gốc
                // Tạo Adapter cho Spinner loại cây
                List<String> typesList = new ArrayList<>();
                typesList.add("Tất cả loại"); // Thêm tùy chọn "Tất cả" ở đầu (Index 0)

                for (SpeciesModel spec : species) {
                    typesList.add(spec.getName()); // Sử dụng tên hiển thị của SpeciesModel
                }

                ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, typesList);
                typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerFilterPlantType.setAdapter(typeAdapter);

                // --- THIẾT LẬP LISTENER CHO SPINNER LỌC THEO LOẠI SAU KHI SET ADAPTER ---
                spinnerFilterPlantType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedTypeDisplayName = (String) parent.getItemAtPosition(position);
                        String filterSpeciesId = null; // Species ID để lọc (null cho "Tất cả")

                        // Nếu không phải "Tất cả loại" (tức là position > 0)
                        // và đảm bảo vị trí hợp lệ trong danh sách species gốc
                        if (position > 0 && position - 1 < speciesList.size()) {
                            // Lấy SpeciesModel tương ứng từ danh sách species gốc
                            SpeciesModel selectedSpecies = speciesList.get(position - 1); // Điều chỉnh index vì có item "Tất cả" ở đầu
                            filterSpeciesId = selectedSpecies.getId(); // Lấy ID của Species
                        }

                        Log.d("PlantInfo", "Filter by type selected: " + selectedTypeDisplayName + " -> Species ID: " + filterSpeciesId);
                        // Áp dụng bộ lọc trong ViewModel
                        // Lấy text hiện tại từ EditText tìm kiếm
                        String currentSearchText = editTextSearchPlantName.getText() != null ? editTextSearchPlantName.getText().toString() : "";
                        plantInfoViewModel.applyFilter(currentSearchText, filterSpeciesId); // Áp dụng filter
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Áp dụng filter với species ID null (tất cả loại)
                        String currentSearchText = editTextSearchPlantName.getText() != null ? editTextSearchPlantName.getText().toString() : "";
                        plantInfoViewModel.applyFilter(currentSearchText, null); // Áp dụng filter
                    }
                });

                // Mặc định chọn "Tất cả loại" (index 0)
                spinnerFilterPlantType.setSelection(0);


            } else {
                Log.w("PlantInfo", "Species list data is null.");
                // Xử lý trường hợp lỗi tải hoặc data null
                List<String> typesList = new ArrayList<>();
                typesList.add("Lỗi tải loại cây"); // Thông báo lỗi
                ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, typesList);
                emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerFilterPlantType.setAdapter(emptyAdapter);
                spinnerFilterPlantType.setEnabled(false); // Vô hiệu hóa spinner khi lỗi
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initContents(View view) {
        plantInfos = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerViewPlantInfo);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        plantInfoAdapter = new PlantInfoAdapter(plantInfos);
        recyclerView.setAdapter(plantInfoAdapter);
        lottieLoadingAnimation = view.findViewById(R.id.lottie_loading_animation);
        btnFilter = view.findViewById(R.id.ic_filter_infoplant);
        layoutFilterOptions = view.findViewById(R.id.layout_filter_options);
        editTextSearchPlantName = view.findViewById(R.id.edit_text_search_plant_name);
        spinnerFilterPlantType = view.findViewById(R.id.spinner_filter_plant_type);
        buttonHideFilter = view.findViewById(R.id.button_hide_filter);
        layoutEmptyPlants = view.findViewById(R.id.layout_empty_plants);
        lottieEmptyPlantsAnimation = view.findViewById(R.id.lottie_empty_plants_animation);
        textEmptyPlantsMessage = view.findViewById(R.id.text_empty_plants_message);

        editTextSearchPlantName.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (editTextSearchPlantName.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                    // Check if click is within the bounds of the drawable
                    if (event.getRawX() >= (editTextSearchPlantName.getRight() - editTextSearchPlantName.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // Clear text and apply filter
                        editTextSearchPlantName.setText("");
                        // applyFilter is called by TextWatcher when text changes
                        return true; // Consume the event
                    }
                }
            }
            return false;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hủy các observer khi view bị hủy
        if (plantInfoViewModel != null) {
            plantInfoViewModel.isLoading.removeObservers(getViewLifecycleOwner());
            plantInfoViewModel.plants.removeObservers(getViewLifecycleOwner());
            plantInfoViewModel.getSpeciesList().removeObservers(getViewLifecycleOwner());
            plantInfoViewModel.errorMessage.removeObservers(getViewLifecycleOwner()); // Hủy observer lỗi nếu có
        }
    }
}