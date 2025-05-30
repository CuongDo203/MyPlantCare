package com.example.myplantcare.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myplantcare.R;
import com.example.myplantcare.adapters.HomeTaskAdapter;
import com.example.myplantcare.data.responses.ScheduleWithMyPlantInfo;
import com.example.myplantcare.models.weathers.MainInfo;
import com.example.myplantcare.models.weathers.WeatherDescription;
import com.example.myplantcare.models.weathers.WeatherResponse;
import com.example.myplantcare.models.weathers.WindInfo;
import com.example.myplantcare.utils.DateUtils;
import com.example.myplantcare.viewmodels.HomeViewModel;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment implements HomeTaskAdapter.OnTaskClickListener {
    private static final String TAG = "HomeFragment";
    private LinearLayout taskList;
    private CardView taskDetail;
    private HomeViewModel homeViewModel;
    private TextView tvTemperature, tvWeatherDescription, ivHumidity, ivWind, tvSeeMore, tvLocationCity, tvNoTasksMessage;
    private ProgressBar progressBarHomeTask;
    private ImageView ivWeatherIcon;
    private RecyclerView rvHomeTasks;
    private HomeTaskAdapter homeTaskAdapter;
    private String userId;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private CancellationTokenSource cancellationTokenSource; // Để hủy yêu cầu vị trí nếu cần

    public interface HomeFragmentListener {
        void onNavigateToScheduleFromHome(); // Phương thức để Activity implement và HomeFragment gọi
        // Thêm các phương thức khác nếu HomeFragment cần yêu cầu Activity làm gì đó
    }

    private HomeFragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "HomeFragment onAttach");
        if (context instanceof HomeFragmentListener) {
            listener = (HomeFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement HomeFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null; // Tránh rò rỉ bộ nhớ
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            userId = user.getUid();
            homeViewModel.setUserId(userId);
            homeViewModel.fetchTodayUncompletedTasks(userId);
        }
        else {
            Toast.makeText(getContext(), "Lỗi: Không thể tải danh sách công việc do thiếu thông tin người dùng.", Toast.LENGTH_LONG).show();
            Log.w(TAG, "User is not logged in, cannot fetch tasks.");
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        cancellationTokenSource = new CancellationTokenSource();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initContents(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupTaskListRecyclerView();
        observeViewModel();
        checkLocationPermissionAndFetchWeather();
        tvSeeMore.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNavigateToScheduleFromHome(); // <--- GỌI PHƯƠNG THỨC CỦA LISTENER
            }
        });
    }

    private void checkLocationPermissionAndFetchWeather() {
        Log.d(TAG, "Checking location permissions.");
        // Kiểm tra xem quyền truy cập vị trí đã được cấp chưa
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Quyền đã được cấp, lấy vị trí cuối cùng và fetch thời tiết
            Log.d(TAG, "Location permission already granted.");
            requestCurrentLocationAndFetchWeather();
        } else {
            // Quyền chưa được cấp, yêu cầu quyền từ người dùng
            Log.d(TAG, "Location permission not granted, requesting.");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        Log.d(TAG, "onRequestPermissionsResult received for request code: " + requestCode);
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Quyền đã được cấp sau khi yêu cầu
//                Log.d(TAG, "Location permission granted by user.");
//                getLastLocationAndFetchWeather();
//            } else {
//                // Quyền bị từ chối
//                Log.w(TAG, "Location permission denied by user.");
//                Toast.makeText(getContext(), "Ứng dụng cần quyền truy cập vị trí để hiển thị thời tiết địa phương.", Toast.LENGTH_LONG).show();
//                // Tùy chọn: Fetch thời tiết cho một thành phố mặc định nếu quyền bị từ chối
//                String apiKey = getString(R.string.api_key);
//                if (getContext() != null && !apiKey.isEmpty()) {
//                    homeViewModel.fetchWeatherDataByCityName("Hanoi", apiKey, "metric", "vi"); // Fetch thời tiết Hà Nội làm mặc định
//                    Log.d(TAG, "Fetching default weather for Hanoi due to permission denial.");
//                } else {
//                    Log.w(TAG, "Context is null or API key missing, cannot fetch default weather.");
//                }
//            }
//        }
//    }
private void requestCurrentLocationAndFetchWeather() {
    Log.d(TAG, "Attempting to get current location.");

    // Kiểm tra lại quyền trước khi gọi getCurrentLocation (Android Studio lint khuyến nghị)
    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        Log.w(TAG, "Location permission not available when trying to get current location.");
        // Nếu không có quyền ở đây, có thể do người dùng đã thu hồi quyền sau khi cấp
        // Fetch thời tiết mặc định
        fetchDefaultWeather();
        return;
    }

    CurrentLocationRequest currentLocationRequest = new CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY) // Yêu cầu độ chính xác cao
//            .setTimeout(TimeUnit.SECONDS.toMillis(10)) // Timeout sau 10 giây nếu không có kết quả
            .setMaxUpdateAgeMillis(TimeUnit.SECONDS.toMillis(10)) // Chấp nhận vị trí không quá 5 giây tuổi
            .build();

    // Hủy bỏ yêu cầu trước đó nếu có
    cancellationTokenSource.cancel();
    cancellationTokenSource = new CancellationTokenSource(); // Tạo CancellationTokenSource mới cho yêu cầu hiện tại
    CancellationToken cancellationToken = cancellationTokenSource.getToken();

    // Yêu cầu vị trí hiện tại
    fusedLocationClient.getCurrentLocation(currentLocationRequest, cancellationToken)
            .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got current location. This should be a fresh location.
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Log.d(TAG, "Current location obtained: Lat=" + latitude + ", Lon=" + longitude);

                        // Fetch weather data using coordinates
                        String apiKey = getString(R.string.api_key);
                        if (getContext() != null && !apiKey.isEmpty()) {
                            homeViewModel.fetchWeatherDataByCoordinates(latitude, longitude, apiKey, "metric", "vi");
                            Log.d(TAG, "Fetching weather data by coordinates.");
                        } else {
                            Log.w(TAG, "Context is null or API key missing, cannot fetch weather by coordinates.");
                            fetchDefaultWeather(); // Fetch mặc định nếu API key thiếu
                        }
                    } else {
                        // Location is null, possibly due to timeout or temporary issue
                        Log.w(TAG, "Current location is null after request.");
                        Toast.makeText(getContext(), "Không thể lấy vị trí hiện tại. Hiển thị thời tiết mặc định.", Toast.LENGTH_LONG).show();
                        fetchDefaultWeather(); // Fetch mặc định nếu vị trí null
                    }
                }
            })
            .addOnFailureListener(requireActivity(), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Xử lý lỗi khi yêu cầu vị trí thất bại
                    Log.e(TAG, "Failed to get current location.", e);
                    Toast.makeText(getContext(), "Lỗi khi lấy vị trí: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    fetchDefaultWeather(); // Fetch mặc định nếu xảy ra lỗi
                }
            })
            .addOnCompleteListener(requireActivity(), new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isCanceled()) {
                        Log.d(TAG, "Current location request was canceled.");
                    }
                }
            });
}

    private void fetchDefaultWeather() {
        Log.d(TAG, "Fetching default weather for Hanoi.");
        String apiKey = getString(R.string.api_key);
        if (getContext() != null && !apiKey.isEmpty()) {
            homeViewModel.fetchWeatherDataByCityName("Hanoi", apiKey, "metric", "vi");
            tvLocationCity.setText("Hà Nội"); // Cập nhật UI thành phố mặc định
        } else {
            Log.w(TAG, "Context is null or API key missing, cannot fetch default weather.");
        }
    }
    private void setupTaskListRecyclerView() {
        rvHomeTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        homeTaskAdapter = new HomeTaskAdapter(this); // Pass HomeFragment as listener
        rvHomeTasks.setAdapter(homeTaskAdapter);
        Log.d(TAG, "Task list RecyclerView setup complete.");
    }

    public void onResume() {
        super.onResume();
         if (userId != null) {
             homeViewModel.fetchTodayUncompletedTasks(userId);
             Log.d(TAG, "onResume: Refreshing today's uncompleted tasks.");
         }
    }
    private void observeViewModel() {
        homeViewModel.weatherData.observe(getViewLifecycleOwner(), weatherResponse -> {
            if (weatherResponse != null) {
                updateWeatherUI(weatherResponse); // Gọi hàm cập nhật UI đã có
            }
        });

        homeViewModel.todayUncompletedTasks.observe(getViewLifecycleOwner(), tasks -> {
            Log.d(TAG, "Today's uncompleted tasks observed. Size: " + (tasks != null ? tasks.size() : "null"));
            if (homeTaskAdapter != null) {
                homeTaskAdapter.setTasks(tasks);
            }
            if (tasks != null && !tasks.isEmpty()) {
                rvHomeTasks.setVisibility(View.VISIBLE);
                tvNoTasksMessage.setVisibility(View.GONE);
                Log.d(TAG, "Tasks found, showing RecyclerView.");
            } else {
                // Không có tasks -> Ẩn RecyclerView, hiển thị TextView thông báo
                rvHomeTasks.setVisibility(View.GONE);
                tvNoTasksMessage.setVisibility(View.VISIBLE);
                Log.d(TAG, "No tasks found, showing 'no tasks' message.");
            }
        });

        homeViewModel.isLoadingTasks.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                // Đang tải -> Hiển thị ProgressBar, ẩn RecyclerView và TextView thông báo
                progressBarHomeTask.setVisibility(View.VISIBLE);
                rvHomeTasks.setVisibility(View.GONE);
                tvNoTasksMessage.setVisibility(View.GONE);
                Log.d(TAG, "Loading tasks, showing ProgressBar.");
            } else {
                // Tải xong -> Ẩn ProgressBar. Visibility của RecyclerView/TextView sẽ được quyết định bởi todayUncompletedTasks observer
                progressBarHomeTask.setVisibility(View.GONE);
                Log.d(TAG, "Finished loading tasks, hiding ProgressBar.");
            }
        });

        homeViewModel.errorMessage.observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateWeatherUI(WeatherResponse weatherData) {
        tvLocationCity.setText(weatherData.getCityName());
        MainInfo mainInfo = weatherData.getMain();
        if (mainInfo != null && mainInfo.getTemp() != null) {
            String temperature = String.format(Locale.getDefault(), "%.0f°C", mainInfo.getTemp());
            tvTemperature.setText(temperature);
        } else {
            tvTemperature.setText("--°"); // Giá trị mặc định nếu null
        }

        // Cập nhật mô tả và icon
        List<WeatherDescription> weatherList = weatherData.getWeather();
        if (weatherList != null && !weatherList.isEmpty()) {
            WeatherDescription description = weatherList.get(0); // Lấy phần tử đầu tiên

            if (description.getDescription() != null) {
                String desc = description.getDescription();
                String formattedDesc = desc.substring(0, 1).toUpperCase() + desc.substring(1);
                tvWeatherDescription.setText(formattedDesc);
            } else {
                tvWeatherDescription.setText("Không rõ");
            }

            if (description.getIcon() != null && getContext() != null) {
                String iconUrl = "https://openweathermap.org/img/wn/" + description.getIcon() + "@2x.png";
                Glide.with(getContext())
                        .load(iconUrl)
                        .error(R.drawable.cloudy)
                        .placeholder(R.drawable.cloudy)
                        .into(ivWeatherIcon);
            } else if (getContext() != null){
                ivWeatherIcon.setImageResource(R.drawable.cloudy);
            }
        } else {
            tvWeatherDescription.setText("Không rõ");
            if (getContext() != null) {
                ivWeatherIcon.setImageResource(R.drawable.cloudy);
            }
        }
        // Cập nhật thông tin gió, độ ẩm
        WindInfo windInfo = weatherData.getWind();
        String windSpeed = "-- km/h";
        String humidity = "-- %";

        if (windInfo != null && windInfo.getSpeed() != null) {
            windSpeed = String.format(Locale.getDefault(), "%.1f km/h", windInfo.getSpeed() * 3.6);
            ivWind.setText(windSpeed);
        }
        if (mainInfo != null && mainInfo.getHumidity() != null) {
            humidity = String.format(Locale.getDefault(), "%d%%", mainInfo.getHumidity());
            ivHumidity.setText(humidity);
        }

    }

    private void initContents(View view) {
        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvWeatherDescription = view.findViewById(R.id.tvWeatherDescription);
        ivHumidity = view.findViewById(R.id.tv_humidity);
        ivWind = view.findViewById(R.id.tv_wind_speed);
        ivWeatherIcon = view.findViewById(R.id.ivWeatherIcon);
        tvLocationCity = view.findViewById(R.id.tvLocationCity);
        tvSeeMore = view.findViewById(R.id.tvSeeMore);
        rvHomeTasks = view.findViewById(R.id.rv_home_tasks);
        progressBarHomeTask = view.findViewById(R.id.progressBarHomeTasks);
        tvNoTasksMessage = view.findViewById(R.id.tvNoTasksMessage);
    }

    private void showBottomDialog(ScheduleWithMyPlantInfo task) {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet);

        TextView taskName = dialog.findViewById(R.id.task_name);
        TextView taskProgress = dialog.findViewById(R.id.task_progress);
        TextView taskTime = dialog.findViewById(R.id.task_time);
        TextView myPlantLocation = dialog.findViewById(R.id.my_plant_location);
        TextView myPlantName = dialog.findViewById(R.id.my_plant_name);
        Button btnComplete = dialog.findViewById(R.id.btn_complete);
        Button btnNote = dialog.findViewById(R.id.btn_note);

        if(task != null) {
            taskName.setText(task.getTaskName());
            taskProgress.setText(task.getMyPlant().getProgress() + "%");
            taskTime.setText(DateUtils.formatTimestamp(task.getScheduleTime(), DateUtils.TIME_FORMAT_DISPLAY));
            myPlantLocation.setText(task.getMyPlant().getLocation());
            myPlantName.setText(task.getMyPlantNickname());
            btnComplete.setOnClickListener(v -> {
                homeViewModel.markTaskCompleted(task);
                dialog.dismiss();
            });
            btnNote.setOnClickListener(v -> {
                //Chuyen sang add note
            });
        }

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.setCancelable(true);
        dialog.show();
    }

    @Override
    public void onTaskClick(ScheduleWithMyPlantInfo task) {
        showBottomDialog(task);
    }
}