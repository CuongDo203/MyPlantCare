package com.example.myplantcare.activities;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.myplantcare.R;
import com.example.myplantcare.models.MyPlantModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;

public class CareInstructionActivity extends AppCompatActivity {
    private Spinner plantSpinner;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> nicknames = new ArrayList<>();
    private HashMap<String, MyPlantModel> plantMap = new HashMap<>();

    private Spinner citySpinner;
    private ArrayAdapter<String> cityAdapter;
    private ArrayList<String> cityList = new ArrayList<>();
    private FirebaseFirestore db;

    private ImageView plantImageView;
    private TextView plantNameTextView;
    private TextView plantAgeTextView;
    private TextView plantSizeTextView;
    private FirebaseAuth auth;


    private Spinner seasonSpinner;
    private ArrayAdapter<String> seasonAdapter;
    private ArrayList<String> seasonList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_care_instruction);
        // back button

        ImageView btnBack = findViewById(R.id.arrow_back_care_instruction);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClass(CareInstructionActivity.this, MainActivity.class);
                startActivity(i);
            }
        });


        // Ánh xạ view
        plantSpinner = findViewById(R.id.plantCategorySpinner);
        plantImageView = findViewById(R.id.plantImageView);
        plantNameTextView = findViewById(R.id.plantNameTextView);
        plantAgeTextView = findViewById(R.id.plantAgeTextView);
        plantSizeTextView = findViewById(R.id.plantSizeTextView);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nicknames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        plantSpinner.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadPlantsFromFirestore();

        plantSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedNickname = nicknames.get(position);
                MyPlantModel selectedPlant = plantMap.get(selectedNickname);

                if (selectedPlant != null) {
                    plantNameTextView.setText(selectedPlant.getNickname());
                    plantAgeTextView.setText(selectedPlant.getLocation());
                    plantSizeTextView.setText(selectedPlant.getStatus());

                    Glide.with(CareInstructionActivity.this)
                            .load(selectedPlant.getImage())
                            .placeholder(R.drawable.plant)
                            .into(plantImageView);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }


        });

        citySpinner = findViewById(R.id.citySpinner);
        db = FirebaseFirestore.getInstance();

        // Adapter cho Spinner
        cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cityList);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(cityAdapter);

        loadCitiesFromFirestore();


        // Ánh xạ Spinner mùa
        seasonSpinner = findViewById(R.id.seasonSpinner);

        // Khởi tạo adapter cho Spinner mùa
        seasonAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, seasonList);
        seasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seasonSpinner.setAdapter(seasonAdapter);

        // Load dữ liệu mùa từ Firestore
        loadSeasonsFromFirestore();
        // Xem hướng dẫn
        Button createGuideButton = findViewById(R.id.btn_instruction_detail);
        createGuideButton.setOnClickListener(v -> {
            Intent intent = new Intent(CareInstructionActivity.this, InstructionDetailActivity.class);
            intent.putExtra("plantName", plantSpinner.getSelectedItem().toString());
            intent.putExtra("city", citySpinner.getSelectedItem().toString());
            intent.putExtra("season", seasonSpinner.getSelectedItem().toString());
            startActivity(intent);
        });
    }

    private void loadPlantsFromFirestore() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("my_plants")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    nicknames.clear();
                    plantMap.clear();

                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        MyPlantModel plant = doc.toObject(MyPlantModel.class);
                        String nickname = plant.getNickname();

                        if (nickname != null) {
                            nicknames.add(nickname);
                            plantMap.put(nickname, plant);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    if (!nicknames.isEmpty()) {
                        plantSpinner.setSelection(0); // chọn sẵn item đầu tiên
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải cây: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void loadCitiesFromFirestore() {
        db.collection("cities")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    cityList.clear();
                    for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                        // Lấy tên thành phố từ Document ID
                        String cityName = doc.getId();
                        cityList.add(cityName);
                    }
                    cityAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải thành phố: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void loadSeasonsFromFirestore() {
        db.collection("seasons")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    seasonList.clear();
                    for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                        String seasonName = doc.getId(); // document ID chính là tên mùa
                        seasonList.add(seasonName);
                    }
                    seasonAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải mùa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}
