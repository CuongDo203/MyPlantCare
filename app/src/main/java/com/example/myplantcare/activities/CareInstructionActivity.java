package com.example.myplantcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;

public class CareInstructionActivity extends AppCompatActivity {
    private Spinner plantSpinner;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> names = new ArrayList<>();
    private HashMap<String, MyPlantModel> plantMap = new HashMap<>();

    private Spinner citySpinner;
    private ArrayAdapter<String> cityAdapter;
    private ArrayList<String> cityList = new ArrayList<>();

    private Spinner seasonSpinner;
    private ArrayAdapter<String> seasonAdapter;
    private ArrayList<String> seasonList = new ArrayList<>();

    private ImageView plantImageView;
    private TextView plantNameTextView;
    private TextView plantPositionTextView;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_care_instruction);

        ImageView btnBack = findViewById(R.id.arrow_back_care_instruction);
        btnBack.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));

        // View mapping
        plantSpinner = findViewById(R.id.plantCategorySpinner);
        plantImageView = findViewById(R.id.plantImageView);
        plantNameTextView = findViewById(R.id.plantNameTextView);
        plantPositionTextView = findViewById(R.id.plantPositionTextView);
        citySpinner = findViewById(R.id.citySpinner);
        seasonSpinner = findViewById(R.id.seasonSpinner);

        db = FirebaseFirestore.getInstance();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        plantSpinner.setAdapter(adapter);

        cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cityList);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(cityAdapter);

        seasonAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, seasonList);
        seasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seasonSpinner.setAdapter(seasonAdapter);

        loadPlantsFromFirestore();
        loadCitiesFromFirestore();
        loadSeasonsFromFirestore();

        plantSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedNickname = names.get(position);
                MyPlantModel selectedPlant = plantMap.get(selectedNickname);
                if (selectedPlant != null) {
                    displayPlantInfo(selectedPlant);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        Button createGuideButton = findViewById(R.id.btn_instruction_detail);
        createGuideButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, InstructionDetailActivity.class);
            intent.putExtra("plantName", plantSpinner.getSelectedItem().toString());
            intent.putExtra("city", citySpinner.getSelectedItem().toString());
            intent.putExtra("season", seasonSpinner.getSelectedItem().toString());
            startActivity(intent);
        });
    }

    private void displayPlantInfo(MyPlantModel selectedPlant) {
        String plantId = selectedPlant.getPlantId();
        if (plantId == null || plantId.isEmpty()) return;

        db.collection("plants").document(plantId)
                .get()
                .addOnSuccessListener(plantDoc -> {
                    if (!plantDoc.exists()) return;

                    String plantName = plantDoc.getString("name");
                    String imageUrl = plantDoc.getString("image");
                    String speciesId = plantDoc.getString("speciesId");

                    if (plantName != null) plantNameTextView.setText(plantName);

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.plant)
                                .into(plantImageView);
                    }

                    if (speciesId != null && !speciesId.isEmpty()) {
                        db.collection("species").document(speciesId)
                                .get()
                                .addOnSuccessListener(speciesDoc -> {
                                    String position = speciesDoc.getString("name");
                                    plantPositionTextView.setText(position != null ? position : "Không rõ vị trí");
                                })
                                .addOnFailureListener(e -> plantPositionTextView.setText("Lỗi tải vị trí"));
                    } else {
                        plantPositionTextView.setText("Không có speciesId");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Lỗi lấy dữ liệu cây: " + e.getMessage()));
    }

    private void loadPlantsFromFirestore() {
        db.collection("plants")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    names.clear();
                    plantMap.clear();

                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        String plantName = doc.getString("name");
                        if (plantName != null) {
                            names.add(plantName);
                            MyPlantModel mockPlant = new MyPlantModel();
                            mockPlant.setNickname(plantName);
                            mockPlant.setPlantId(doc.getId());
                            plantMap.put(plantName, mockPlant);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    if (!names.isEmpty()) {
                        plantSpinner.setSelection(0);
                        displayPlantInfo(plantMap.get(names.get(0)));
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải danh sách cây: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadCitiesFromFirestore() {
        db.collection("cities")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    cityList.clear();
                    for (DocumentSnapshot doc : querySnapshots) {
                        String cityName = doc.getString("name");
                        if (cityName != null && !cityName.isEmpty()) {
                            cityList.add(cityName);
                        }
                    }
                    cityAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải thành phố: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadSeasonsFromFirestore() {
        db.collection("seasons")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    seasonList.clear();
                    for (DocumentSnapshot doc : querySnapshots) {
                        String seasonName = doc.getString("name");
                        if (seasonName != null && !seasonName.isEmpty()) {
                            seasonList.add(seasonName);
                        }
                    }
                    seasonAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải mùa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}