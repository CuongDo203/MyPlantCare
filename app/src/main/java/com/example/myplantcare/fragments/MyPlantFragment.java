package com.example.myplantcare.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.example.myplantcare.utils.DateUtils;
import com.example.myplantcare.viewmodels.MyPlantListViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class MyPlantFragment extends Fragment {

    private static final String TAG = "MyPlantFragment"; // Sử dụng TAG nhất quán

    private RecyclerView recyclerView;
    private MyPlantAdapter adapter;
    private List<MyPlantModel> myPlantList;
    private FloatingActionButton fabAddMyPlant;
    private MyPlantListViewModel myPlantListViewModel;
    // Repository nên được khởi tạo trong ViewModel hoặc Factory của ViewModel
    // private MyPlantRepositoryImpl repository = new MyPlantRepositoryImpl(); // Khởi tạo ở đây không đúng MVVM lắm
    LottieAnimationView lottieLoadingAnimation;

    // Khai báo Activity Result Launcher
    private ActivityResultLauncher<Intent> detailActivityLauncher;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_plant, container, false);

        initContents(view); // Tìm Views và setup RecyclerView/Adapter ban đầu
        setupViewModel(); // Setup ViewModel
        observeViewModel(); // Quan sát LiveData từ ViewModel
        registerActivityResults(); // Đăng ký Launchers

        fabAddMyPlant.setOnClickListener(v -> showAddPlantDialog());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // loadMyPlants() có thể gọi ở đây nếu ViewModel không tự load trong constructor
        // myPlantListViewModel.loadMyPlants();
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
            // myPlantList.clear(); // Không cần clear nếu adapter có phương thức setMyPlants
            // myPlantList.addAll(myPlants); // Adapter nên làm việc trực tiếp với danh sách mới

            if (myPlants != null && !myPlants.isEmpty()) {
                adapter.setMyPlants(myPlants); // Sử dụng phương thức setMyPlants của adapter
                recyclerView.setVisibility(View.VISIBLE);
                // TODO: Ẩn TextView "Không có cây nào" nếu có
            } else {
                adapter.setMyPlants(new ArrayList<>()); // Cập nhật adapter với danh sách rỗng
                recyclerView.setVisibility(View.GONE); // Ẩn RecyclerView
                // TODO: Hiển thị TextView "Không có cây nào" nếu có
            }

            // adapter.notifyDataSetChanged(); // setMyPlants() nên tự gọi các notify phù hợp (hoặc bạn có thể gọi ở đây)
            // notifyDataSetChanged() là đơn giản nhất nhưng kém hiệu quả
        });

        myPlantListViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG, "isLoading LiveData updated: " + isLoading);
            if (isLoading) {
                lottieLoadingAnimation.setVisibility(View.VISIBLE);
                lottieLoadingAnimation.playAnimation();
                recyclerView.setVisibility(View.GONE);
                // TODO: Ẩn TextView "Không có cây nào"
            } else {
                lottieLoadingAnimation.setVisibility(View.GONE);
                lottieLoadingAnimation.cancelAnimation();
                // Visibility của RecyclerView/TextViewNoPlants sẽ được xử lý bởi observer myPlants
            }
        });
        // TODO: Observe error LiveData nếu có
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

    private void initContents(View view) {
        fabAddMyPlant = view.findViewById(R.id.fab_add_my_plant);
        recyclerView = view.findViewById(R.id.recycler_my_plant);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        lottieLoadingAnimation = view.findViewById(R.id.lottie_loading_animation);
        // myPlantList = new ArrayList<>(); // ViewModel sẽ cung cấp danh sách, không cần quản lý thủ công ở đây

        // TODO: Cần lấy userId thật ở đây hoặc truyền từ ViewModel/Activity
        String userIdForAdapter = "eoWltJXzlBtC8U8QZx9G"; // Tạm thời hardcode

        // Adapter cần một callback khi item cây được click để mở MyPlantDetailActivity
        // Adapter cũng cần biết userId và Repository để xử lý delete (nếu logic delete nằm trong Adapter)
        // Tốt hơn hết là Adapter chỉ phát ra sự kiện click và Fragment/ViewModel xử lý delete/open detail.

        // Truyền lambda/interface vào Adapter để xử lý click item
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
                        // myPlantListViewModel.deleteMyPlant(plant.getId()); // Cần thêm phương thức deleteMyPlant vào ViewModel
                    }
                });


        recyclerView.setAdapter(adapter);
    }

    // Hàm mở MyPlantDetailActivity bằng launcher
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
            // TODO: Hủy các observer lỗi nếu có
        }
    }

    // TODO: Cần cập nhật MyPlantAdapter để có interface OnPlantItemClickListener
    // và constructor/setter để nhận listener này. Adapter sẽ gọi listener.onPlantClick()
    // khi một item được click.
}