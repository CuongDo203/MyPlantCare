package com.example.myplantcare.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.myplantcare.R;
import com.example.myplantcare.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.myplantcare.fragments.HomeFragment;
import com.example.myplantcare.fragments.NoteFragment;
import com.example.myplantcare.fragments.PlantFragment;
import com.example.myplantcare.fragments.ScheduleFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.view.View;


import android.widget.ImageView;


public class MainActivity extends AppCompatActivity implements HomeFragment.HomeFragmentListener{

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    ImageView ivMenu;
    private static final int REQUEST_CODE_SIDEBAR = 100;

    public static final int RESULT_CODE_NAV_TO_PLANTS = 201;

    private BottomNavigationView bottomNavigationView;
    private ActivityResultLauncher<Intent> sidebarActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

//        // Xử lý nút menu
        toolbar.setNavigationOnClickListener(v ->
                Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
        );

        // Xử lý click vào menu item
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.notifications) {
                Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        bottomNavigationView = findViewById(R.id.bottomNavigation);

        loadFragment(new HomeFragment());;
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_schedule) {
                fragment = new ScheduleFragment();
            } else if (itemId == R.id.nav_plant) {
                fragment = new PlantFragment();
            } else if (itemId == R.id.nav_note) {
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                startActivity(intent);
                return true;
            }
            else if (itemId == R.id.nav_statics){
                Intent intent = new Intent(MainActivity.this, StatisticActivity.class);
                startActivity(intent);
                return true;
            }
            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });

        binding = ActivityMainBinding.inflate(getLayoutInflater());


//        xử lý Notification
        ImageView myImageView = findViewById(R.id.ivNotification);
        myImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClass(MainActivity.this, NotificationActivity.class);
                startActivity(i);
            }
        });

        sidebarActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Callback này sẽ được gọi khi SideBarActivity kết thúc và trả về kết quả
                    Log.d("MainActivity", "ActivityResultLauncher callback received. ResultCode: " + result.getResultCode());
                    if (result.getResultCode() == RESULT_CODE_NAV_TO_PLANTS) {
                        Log.d("MainActivity", "Received result to navigate to Plants Fragment.");
                        loadFragment(new PlantFragment());
                        // Cập nhật trạng thái selected trên BottomNavigationView
                        MenuItem plantMenuItem = bottomNavigationView.getMenu().findItem(R.id.nav_plant);
                        if (plantMenuItem != null) {
                            plantMenuItem.setChecked(true);
                            Log.d("MainActivity", "BottomNav item 'Plant' highlighted.");
                        } else {
                            Log.w("MainActivity", "BottomNav item with ID R.id.nav_plant not found.");
                        }
                    }
                }
        );

        ivMenu = findViewById(R.id.ivMenu);
        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang SideBarActivity
                Intent intent = new Intent(MainActivity.this, SideBarActivity.class);
                sidebarActivityResultLauncher.launch(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity", "onActivityResult received. RequestCode: " + requestCode + ", ResultCode: " + resultCode);
        if(requestCode == REQUEST_CODE_SIDEBAR) {
            if(resultCode == RESULT_CODE_NAV_TO_PLANTS) {
                Log.d("MainActivity", "onActivityResult: RESULT_CODE_NAV_TO_PLANTS");
                loadFragment(new PlantFragment());
                MenuItem plantMenuItem = bottomNavigationView.getMenu().findItem(R.id.nav_plant); // Tìm item menu Plant bằng ID
                if (plantMenuItem != null) {
                    plantMenuItem.setChecked(true);
                }
            }
        }


        // Yêu cầu quyền thông báo Android 13 trở lên
        requestNotificationPermission();
        // Tạo kênh thông báo cho app
        createNotificationChannel();
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }

    @Override
    public void onNavigateToScheduleFromHome() {
        loadFragment(new ScheduleFragment());
        MenuItem scheduleMenuItem = bottomNavigationView.getMenu().findItem(R.id.nav_schedule); // Tìm item menu Schedule bằng ID
        if (scheduleMenuItem != null) {
            scheduleMenuItem.setChecked(true);
        }
    }

    private void createNotificationChannel() {
        // Kiểm tra nếu phiên bản hệ điều hành >= Android 8.0 (API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Kiểm tra kênh thông báo đã tồn tại chưa
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                NotificationChannel existingChannel = notificationManager.getNotificationChannel("PLANTAPP_CHANNEL");
                if (existingChannel == null) {
                    // Nếu kênh chưa tồn tại, tạo kênh mới
                    NotificationChannel channel = new NotificationChannel(
                            "PLANTAPP_CHANNEL",
                            "Reminder Channel",
                            NotificationManager.IMPORTANCE_HIGH
                    );
                    // Mô tả kênh thông báo (tùy chọn)
                    channel.setDescription("This channel is used for daily reminders.");

                    // Tạo kênh thông báo
                    notificationManager.createNotificationChannel(channel);
                }
            }
        }
    }


    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
}