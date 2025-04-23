package com.example.myplantcare.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.ktx.Firebase;

import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements HomeTaskAdapter.OnTaskClickListener {
    private static final String TAG = "HomeFragment";
    private LinearLayout taskList;
    private CardView taskDetail;
    private HomeViewModel homeViewModel;
    private TextView tvTemperature, tvWeatherDescription, ivHumidity, ivWind, tvSeeMore;
    private ImageView ivWeatherIcon;
    private RecyclerView rvHomeTasks;
    private HomeTaskAdapter homeTaskAdapter;
    private String userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
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
        String apiKey = getString(R.string.api_key);
        homeViewModel.fetchWeatherData("Hanoi", apiKey, "metric", "vi");

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
        tvSeeMore.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_homeFragment_to_scheduleFragment);
        });
    }

    private void setupTaskListRecyclerView() {
        rvHomeTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        // Initialize adapter, passing 'this' as the click listener
        homeTaskAdapter = new HomeTaskAdapter(this); // Pass HomeFragment as listener
        rvHomeTasks.setAdapter(homeTaskAdapter);
        Log.d(TAG, "Task list RecyclerView setup complete.");
    }

    public void onResume() {
        super.onResume();
        // Optionally refresh tasks here if needed when returning from Schedule screen
         if (userId != null) {
             homeViewModel.fetchTodayUncompletedTasks(userId);
             Log.d(TAG, "onResume: Refreshing today's uncompleted tasks.");
         }
    }
    private void observeViewModel() {
        homeViewModel.weatherData.observe(getViewLifecycleOwner(), weatherResponse -> {
            // Được gọi khi dữ liệu thời tiết thay đổi
            if (weatherResponse != null) {
                updateWeatherUI(weatherResponse); // Gọi hàm cập nhật UI đã có
            }
        });

        homeViewModel.todayUncompletedTasks.observe(getViewLifecycleOwner(), tasks -> {
            Log.d(TAG, "Today's uncompleted tasks observed. Size: " + (tasks != null ? tasks.size() : "null"));
            if (homeTaskAdapter != null) {
                homeTaskAdapter.setTasks(tasks);
            }
        });

        homeViewModel.isLoadingTasks.observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG, "isLoadingTasks observed: " + isLoading);
            // TODO: Show/hide loading indicator for tasks
        });

        homeViewModel.errorMessage.observe(getViewLifecycleOwner(), errorMessage -> {
            // Được gọi khi có lỗi
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                // Có thể reset lỗi sau khi hiển thị
                // homeViewModel.clearError(); // Cần thêm hàm này trong ViewModel
            }
        });
    }

    private void updateWeatherUI(WeatherResponse weatherData) {
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
                        .error(R.drawable.cloudy) // Đảm bảo bạn có drawable này
                        .placeholder(R.drawable.cloudy) // Đảm bảo bạn có drawable này
                        .into(ivWeatherIcon);
            } else if (getContext() != null){
                // Đặt ảnh mặc định nếu không có icon hoặc context null
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
//        taskList = view.findViewById(R.id.task_list);
//        taskDetail = view.findViewById(R.id.task_detail);
        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvWeatherDescription = view.findViewById(R.id.tvWeatherDescription);
        ivHumidity = view.findViewById(R.id.tv_humidity);
        ivWind = view.findViewById(R.id.tv_wind_speed);
        ivWeatherIcon = view.findViewById(R.id.ivWeatherIcon);
        tvSeeMore = view.findViewById(R.id.tvSeeMore);
        rvHomeTasks = view.findViewById(R.id.rv_home_tasks);
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