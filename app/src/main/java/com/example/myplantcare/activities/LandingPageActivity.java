package com.example.myplantcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myplantcare.R;

public class LandingPageActivity extends AppCompatActivity {
    Button btnStart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page);
        initContents();
        handleEvent();
    }
    private void initContents() {
        btnStart = findViewById(R.id.btnStart);
    }

    private void handleEvent() {
        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(LandingPageActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}