package com.example.myplantcare.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myplantcare.R;

public class MyPlantDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView myPlantName, myPlantLocation, myPlantCreated, myPlantHeight,
            myPlantType, myPlantStatus, myPlantUpdated;
    private ImageView myPlantImg, btnDelete, btnUpdate, btnChangeImage, btnBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_plant_detail);
        initContents();
        btnBack.setOnClickListener(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            myPlantName.setText(extras.getString("plantName"));
            myPlantLocation.setText(extras.getString("location"));
            Glide.with(this).load(extras.getString("image")).into(myPlantImg);
            myPlantCreated.setText(extras.getString("my_plant_created"));
            myPlantUpdated.setText(extras.getString("my_plant_updated"));
            //cap nhat sau, hien tai dang fixed cung
            myPlantHeight.setText("--cm");
            myPlantType.setText(extras.getString("--"));
            myPlantStatus.setText(extras.getString("--"));
        }
    }

    private void initContents() {
        myPlantName = findViewById(R.id.my_plant_name_dt);
        myPlantLocation = findViewById(R.id.my_plant_location_dt);
        myPlantCreated = findViewById(R.id.my_plant_created_dt);
        myPlantHeight = findViewById(R.id.my_plant_height_dt);
        myPlantType = findViewById(R.id.my_plant_type_dt);
        myPlantStatus = findViewById(R.id.my_plant_status_dt);
        myPlantUpdated = findViewById(R.id.my_plant_updated_dt);
        myPlantImg = findViewById(R.id.my_plant_img_dt);
        btnDelete = findViewById(R.id.btn_delete_my_plant_detail);
        btnUpdate = findViewById(R.id.btn_update_my_plant_detail);
        btnChangeImage = findViewById(R.id.change_my_plant_image);
        btnBack = findViewById(R.id.ivBack);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.ivBack){
            finish();
        }
    }
}