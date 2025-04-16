package com.example.myplantcare.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.myplantcare.R;
import com.example.myplantcare.models.weathers.MainInfo;
import com.example.myplantcare.models.weathers.WeatherDescription;
import com.example.myplantcare.models.weathers.WeatherResponse;
import com.example.myplantcare.models.weathers.WindInfo;
import com.example.myplantcare.viewmodels.HomeViewModel;

import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private LinearLayout taskList;
    private CardView taskDetail;
    private HomeViewModel homeViewModel;
    private TextView tvTemperature, tvWeatherDescription, ivHumidity, ivWind;
    private ImageView ivWeatherIcon;

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
        taskDetail.setOnClickListener(v -> showBottomDialog());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeViewModel();
        String apiKey = getString(R.string.api_key);
        homeViewModel.fetchWeatherData("Hanoi", apiKey, "metric", "vi");
    }

    private void observeViewModel() {
        homeViewModel.weatherData.observe(getViewLifecycleOwner(), weatherResponse -> {
            // Được gọi khi dữ liệu thời tiết thay đổi
            if (weatherResponse != null) {
                updateWeatherUI(weatherResponse); // Gọi hàm cập nhật UI đã có
            }
        });

//        homeViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
//            // Được gọi khi trạng thái loading thay đổi
//            if (isLoading != null) {
//                loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
//            }
//        });

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
        taskList = view.findViewById(R.id.task_list);
        taskDetail = view.findViewById(R.id.task_detail);
        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvWeatherDescription = view.findViewById(R.id.tvWeatherDescription);
        ivHumidity = view.findViewById(R.id.tv_humidity);
        ivWind = view.findViewById(R.id.tv_wind_speed);
        ivWeatherIcon = view.findViewById(R.id.ivWeatherIcon);
    }

    private void showBottomDialog() {
//        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.DialogAnimation);
//        View bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet, null);
//
//        // Cho phép kéo xuống để tắt
//        bottomSheetDialog.setCanceledOnTouchOutside(true);
//        bottomSheetDialog.setDismissWithAnimation(true); // Hiệu ứng animation khi tắt
//
//        bottomSheetDialog.setContentView(bottomSheetView);
//
//        // Đặt nền trong suốt để bo góc hiện ra
//        if (bottomSheetDialog.getWindow() != null) {
//            bottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        }
//
//        bottomSheetDialog.show();

        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.setCancelable(true);
        dialog.show();
    }
}