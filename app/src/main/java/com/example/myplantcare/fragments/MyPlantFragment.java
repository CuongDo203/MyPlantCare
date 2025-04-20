package com.example.myplantcare.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.myplantcare.R;
import com.example.myplantcare.activities.MyPlantDetailActivity;
import com.example.myplantcare.adapters.MyPlantAdapter;
import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.models.SpeciesModel;
import com.example.myplantcare.utils.DateUtils;
import com.example.myplantcare.viewmodels.MyPlantListViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class MyPlantFragment extends Fragment {

    private static final String TAG = "MyPlantFragment"; // Sử dụng TAG nhất quán

    //Filter
    private LinearLayout layoutFilterOptions;
    private EditText editTextSearchPlantName;
    private Spinner spinnerFilterPlantType;
    private Button buttonHideFilter;
    private ImageView clearSearchIcon;

    private RecyclerView recyclerView;
    private MyPlantAdapter adapter;
    private List<MyPlantModel> myPlantList;
    private FloatingActionButton fabAddMyPlant;
    private MyPlantListViewModel myPlantListViewModel;
    LottieAnimationView lottieLoadingAnimation;

    // Empty state UI elements
    private LinearLayout layoutEmptyPlants;
    private LottieAnimationView lottieEmptyPlantsAnimation;
    private TextView textEmptyPlantsMessage;
    private ImageView btnFilter;
    // Khai báo Activity Result Launcher
    private ActivityResultLauncher<Intent> detailActivityLauncher;
    private List<SpeciesModel> speciesList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_plant, container, false);

        initContents(view); // Tìm Views và setup RecyclerView/Adapter ban đầu
        setupViewModel(); // Setup ViewModel
        observeViewModel(); // Quan sát LiveData từ ViewModel
        registerActivityResults(); // Đăng ký Launchers

        fabAddMyPlant.setOnClickListener(v -> showAddPlantDialog());
        btnFilter.setOnClickListener(v -> toggleFilterOptionsVisibility());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupFilterListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        myPlantListViewModel.loadMyPlants();
        Log.d(TAG, "onResume: Calling loadMyPlants() to refresh data.");
    }

    private void registerActivityResults() {
        // Đăng ký launcher cho MyPlantDetailActivity
        detailActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "Result received from MyPlantDetailActivity");
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "Result code is OK. Data updated.");
                        // Nếu Activity trả về RESULT_OK, nghĩa là có sự thay đổi (ví dụ: ảnh cập nhật)
                        // Yêu cầu ViewModel tải lại danh sách cây để cập nhật UI
                        myPlantListViewModel.loadMyPlants();
                        Log.d(TAG, "Calling loadMyPlants() to refresh data.");
                    } else {
                        Log.d(TAG, "Result code is not OK (" + result.getResultCode() + "). No refresh needed.");
                    }
                });
    }


    private void observeViewModel() {
        myPlantListViewModel.myPlants.observe(getViewLifecycleOwner(), myPlants -> {
            Log.d(TAG, "myPlants LiveData updated. Size: " + (myPlants != null ? myPlants.size() : "null"));
            if (myPlants != null && !myPlants.isEmpty()) {
                adapter.setMyPlants(myPlants); // Update adapter
                recyclerView.setVisibility(View.VISIBLE);
                layoutEmptyPlants.setVisibility(View.GONE); // Hide empty state
                lottieEmptyPlantsAnimation.pauseAnimation();
            } else {
                adapter.setMyPlants(new ArrayList<>()); // Clear adapter
                recyclerView.setVisibility(View.GONE); // Hide RecyclerView
                layoutEmptyPlants.setVisibility(View.VISIBLE); // Show empty state
                lottieEmptyPlantsAnimation.playAnimation();
            }
        });

        myPlantListViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG, "isLoading LiveData updated: " + isLoading);
            if (isLoading) {
                lottieLoadingAnimation.setVisibility(View.VISIBLE);
                lottieLoadingAnimation.playAnimation();
                recyclerView.setVisibility(View.GONE);
                layoutFilterOptions.setVisibility(View.GONE);
                layoutEmptyPlants.setVisibility(View.GONE);
                lottieEmptyPlantsAnimation.pauseAnimation();
            } else {
                lottieLoadingAnimation.setVisibility(View.GONE);
                lottieLoadingAnimation.cancelAnimation();
            }
        });

        myPlantListViewModel.getSpeciesList().observe(getViewLifecycleOwner(), species -> {
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
                        if (position > 0 && position - 1 < speciesList.size()) {
                            // Lấy SpeciesModel tương ứng và lấy ID của nó
                            SpeciesModel selectedSpecies = speciesList.get(position - 1); // Điều chỉnh index vì có item "Tất cả" ở đầu
                            filterSpeciesId = selectedSpecies.getId();
                        }

                        Log.d(TAG, "Filter by type selected: " + selectedTypeDisplayName + " -> Species ID: " + filterSpeciesId);
                        // Áp dụng bộ lọc trong ViewModel
                        myPlantListViewModel.applyFilter(editTextSearchPlantName.getText().toString(), filterSpeciesId);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Áp dụng filter với species ID null (tất cả loại)
                        myPlantListViewModel.applyFilter(editTextSearchPlantName.getText().toString(), null);
                    }
                });

                // Mặc định chọn "Tất cả loại" (index 0)
                spinnerFilterPlantType.setSelection(0);


            } else {
                Log.w(TAG, "Species list data is null.");
                List<String> typesList = new ArrayList<>();
                typesList.add("Lỗi tải loại cây"); // Thông báo lỗi
                ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, typesList);
                emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerFilterPlantType.setAdapter(emptyAdapter);
                spinnerFilterPlantType.setEnabled(false); // Vô hiệu hóa spinner khi lỗi
            }
        });
    }

    private void setupViewModel() {
        String userId = "eoWltJXzlBtC8U8QZx9G";  //tam thoi de hard code
        // TODO: Lấy userId thật từ Firebase Auth ở đây
        // FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // String userId = currentUser != null ? currentUser.getUid() : null;
        // if (userId == null) { /* Xử lý trường hợp user chưa đăng nhập */ }

        // ViewModelProvider nên dùng scope của Activity nếu ViewModel quản lý dữ liệu cho toàn bộ screen
        // hoặc nếu ViewModel cần sống sót qua Fragment recreation.
        // MyPlantListViewModel cần userId, nên cần truyền nó qua Factory.
        myPlantListViewModel = new ViewModelProvider(requireActivity(), new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                // Đảm bảo userId đã có giá trị hợp lệ trước khi tạo ViewModel
                if (userId == null) {
                    throw new IllegalStateException("User ID is not available to create ViewModel");
                }
                if (modelClass.isAssignableFrom(MyPlantListViewModel.class)) {
                    return (T) new MyPlantListViewModel(userId); // Truyền userId vào constructor ViewModel
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }).get(MyPlantListViewModel.class);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initContents(View view) {
        fabAddMyPlant = view.findViewById(R.id.fab_add_my_plant);
        recyclerView = view.findViewById(R.id.recycler_my_plant);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        lottieLoadingAnimation = view.findViewById(R.id.lottie_loading_animation);
        btnFilter = view.findViewById(R.id.ic_filter_myplant);
        // TODO: Cần lấy userId thật ở đây hoặc truyền từ ViewModel/Activity
        String userIdForAdapter = "eoWltJXzlBtC8U8QZx9G"; // Tạm thời hardcode

        layoutFilterOptions = view.findViewById(R.id.layout_filter_options);
        editTextSearchPlantName = view.findViewById(R.id.edit_text_search_plant_name);
        spinnerFilterPlantType = view.findViewById(R.id.spinner_filter_plant_type);
        buttonHideFilter = view.findViewById(R.id.button_hide_filter);

        layoutEmptyPlants = view.findViewById(R.id.layout_empty_plants);
        lottieEmptyPlantsAnimation = view.findViewById(R.id.lottie_empty_plants_animation);
        textEmptyPlantsMessage = view.findViewById(R.id.text_empty_plants_message);


        adapter = new MyPlantAdapter(new ArrayList<>(), // Adapter bắt đầu với danh sách rỗng
                new MyPlantAdapter.OnPlantItemClickListener() {
                    @Override
                    public void onPlantClick(MyPlantModel plant) {
                        // Khi item cây được click, mở MyPlantDetailActivity
                        openPlantDetailActivity(plant);
                    }

                    @Override
                    public void onDeleteClick(MyPlantModel plant) {
                        // Xử lý delete click (có thể gọi ViewModel ở đây)
                        // myPlantListViewModel.deleteMyPlant(plant.getId());
                    }
                });
        recyclerView.setAdapter(adapter);

        editTextSearchPlantName.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(editTextSearchPlantName.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                    // Check if click is within the bounds of the drawable
                    if(event.getRawX() >= (editTextSearchPlantName.getRight() - editTextSearchPlantName.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
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

    private void toggleFilterOptionsVisibility() {
        if(layoutFilterOptions.getVisibility() == View.GONE) {
            layoutFilterOptions.setVisibility(View.VISIBLE);
        } else {
            layoutFilterOptions.setVisibility(View.GONE);
        }
    }

    private void setupFilterListeners() {
        editTextSearchPlantName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "Search text changed: " + s.toString());
                // Apply filter when text changes
                // Pass current search text and selected spinner item
                String selectedType = spinnerFilterPlantType.getSelectedItemPosition() > 0 ?
                        (String) spinnerFilterPlantType.getSelectedItem() : null;
                myPlantListViewModel.applyFilter(s.toString(), selectedType);
            }
        });

        if (buttonHideFilter != null) {
            buttonHideFilter.setOnClickListener(v -> toggleFilterOptionsVisibility());
        }
    }

    private void openPlantDetailActivity(MyPlantModel plant) {
        Intent intent = new Intent(requireContext(), MyPlantDetailActivity.class);
        // Đóng gói dữ liệu cây vào Intent
        // Cần đảm bảo các key ở đây khớp với các key trong MyPlantDetailActivity.getAndDisplayPlantData()
        intent.putExtra("id", plant.getId()); // Truyền ID cây
        intent.putExtra("plantName", plant.getNickname()); // Hoặc nickname
        intent.putExtra("location", plant.getLocation());
        intent.putExtra("image", plant.getImage());
        intent.putExtra("status", plant.getStatus());
        intent.putExtra("my_plant_created", plant.getCreatedAt() != null ?
                DateUtils.formatTimestamp(plant.getCreatedAt(), DateUtils.DATE_FORMAT_DISPLAY)
                : null);
        intent.putExtra("my_plant_updated", plant.getUpdatedAt() != null ?
                DateUtils.formatTimestamp(plant.getUpdatedAt(), DateUtils.DATE_FORMAT_DISPLAY)
                : null);

        // Khởi chạy Activity bằng launcher đã đăng ký
        detailActivityLauncher.launch(intent);
        Log.d(TAG, "Launched MyPlantDetailActivity with launcher for plant ID: " + plant.getId());
    }

    private void showAddPlantDialog() {
        AddPlantDialogFragment dialogFragment = new AddPlantDialogFragment();
        dialogFragment.setOnSavePlantListener(() -> {
            Log.d(TAG, "AddPlantDialog closed. Refreshing plants.");
            // Khi cây được lưu thành công, yêu cầu ViewModel tải lại danh sách
            myPlantListViewModel.loadMyPlants();
        });
        dialogFragment.show(getChildFragmentManager(), "AddPlantDialogTag");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hủy các observer khi view bị hủy
        if (myPlantListViewModel != null) {
            myPlantListViewModel.isLoading.removeObservers(getViewLifecycleOwner());
            myPlantListViewModel.myPlants.removeObservers(getViewLifecycleOwner());
        }
    }

}