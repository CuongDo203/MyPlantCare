package com.example.myplantcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.myplantcare.R;
import com.example.myplantcare.databinding.ActivityMainBinding;
import com.example.myplantcare.fragments.HomeFragment;
import com.example.myplantcare.fragments.NoteFragment;
import com.example.myplantcare.fragments.PlantFragment;
import com.example.myplantcare.fragments.ScheduleFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.view.View;


import android.widget.ImageView;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private BottomNavigationView bottomNavigationView;

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

                fragment = new NoteFragment();
            }
            else if (itemId == R.id.nav_statics){
                fragment = null;
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
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }

}