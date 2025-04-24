package com.example.myplantcare.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myplantcare.R;

// SideBarActivity.java
public class SideBarActivity extends AppCompatActivity {
    ImageView ivReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.side_bar);
        ivReturn = findViewById(R.id.ivReturn);
        ivReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); // Optional: hiệu ứng chuyển cảnh
            }
        });
    }
}

