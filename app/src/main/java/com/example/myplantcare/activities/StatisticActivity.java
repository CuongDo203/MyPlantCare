package com.example.myplantcare.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantcare.R;
import com.example.myplantcare.adapters.StatisticAdapter;
import com.example.myplantcare.models.ChartData;
import com.example.myplantcare.models.StatisticItem;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @noinspection unchecked*/
public class StatisticActivity extends AppCompatActivity {

    private RecyclerView recyclerViewStatistics;
    private StatisticAdapter adapter;
    private List<StatisticItem> statisticList;
    private TextView toolbarTitle;
    private ImageButton toolbarBackButton;

    // Firebase
    private FirebaseFirestore db;
    private String userId;
    // Nếu Navbar truyền myPlantId, get từ Intent; null nghĩa lấy tất cả cây
    private String myPlantId;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        // Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.insider_toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);
        toolbarTitle.setText("Thống kê phát triển");
        toolbarBackButton.setOnClickListener(v -> finish());

        // Firebase init
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Nhận myPlantId (có thể null)
        myPlantId = getIntent().getStringExtra("id");
        Log.d("StatisticActivity", "myPlantId nhận được: " + myPlantId);

        // RecyclerView + Adapter
        recyclerViewStatistics = findViewById(R.id.recyclerViewStatistics);
        recyclerViewStatistics.setLayoutManager(new LinearLayoutManager(this));
        statisticList = new ArrayList<>();
        adapter = new StatisticAdapter(this, statisticList, item -> {
            Intent intent = new Intent(StatisticActivity.this, DetailStatisticActivity.class);
            intent.putExtra("id", item.getPlantId());
            intent.putExtra("statisticItem", item);
            startActivity(intent);
        });
        recyclerViewStatistics.setAdapter(adapter);

        // Bắt đầu load dữ liệu
        loadStatistics();
    }

    private void loadStatistics() {
        if (myPlantId == null) {
            // Lấy tất cả cây của user
            db.collection("users")
                    .document(userId)
                    .collection("my_plants")
                    .get()
                    .addOnSuccessListener(queryPlants -> {
                        statisticList.clear();
                        for (QueryDocumentSnapshot plantDoc : queryPlants) {
                            String plantId = plantDoc.getId();
                            String plantName = plantDoc.getString("nickname");
                            if (plantName == null) plantName = "Cây không tên";
                            // Lấy Growth của từng cây
                            fetchGrowthForPlant(plantId, plantName);
                        }
                    });
        } else {
            // Chỉ lấy một cây
            String plantName = getIntent().getStringExtra("nickname");
            if (plantName == null) plantName = "Cây của bạn";
            fetchGrowthForPlant(myPlantId, plantName);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchGrowthForPlant(String plantId, String plantName) {
        db.collection("users")
                .document(userId)
                .collection("my_plants")
                .document(plantId)
                .collection("Growth")
                .document(plantId)  // lấy đúng 1 document có id trùng plantId
                .get()
                .addOnSuccessListener(growthDoc -> {
                    if (growthDoc.exists()) {
                        // --- heights ---
                        List<Map<String, Object>> rawHeights = (List<Map<String, Object>>) growthDoc.get("heights");
                        List<ChartData> heightsData = new ArrayList<>();
                        if (rawHeights != null) {
                            for (Map<String, Object> e : rawHeights) {
                                Number h = (Number) e.get("height");
                                Timestamp ts = (Timestamp) e.get("date");
                                String label = ts != null ? new SimpleDateFormat("dd/MM").format(ts.toDate()) : "N/A";
                                if (h != null && ts != null) {
                                    heightsData.add(new ChartData(ts.toDate(), h.floatValue(), label));
                                }
                            }
                        }

                        // --- leaf ---
                        List<Map<String, Object>> rawLeaf = (List<Map<String, Object>>) growthDoc.get("leaf");
                        List<ChartData> leafData = new ArrayList<>();
                        if (rawLeaf != null) {
                            for (Map<String, Object> e : rawLeaf) {
                                Number n = (Number) e.get("number_of_leaf");
                                Timestamp ts = (Timestamp) e.get("date");
                                String label = ts != null ? new SimpleDateFormat("dd/MM").format(ts.toDate()) : "N/A";
                                if (n != null && ts != null) {
                                    leafData.add(new ChartData(ts.toDate(), n.floatValue(), label));
                                }
                            }
                        }

                        // --- flower ---
                        List<Map<String, Object>> rawFlower = (List<Map<String, Object>>) growthDoc.get("flower");
                        List<ChartData> flowerData = new ArrayList<>();
                        if (rawFlower != null) {
                            for (Map<String, Object> e : rawFlower) {
                                Number n = (Number) e.get("number_of_flower");
                                Timestamp ts = (Timestamp) e.get("date");
                                String label = ts != null ? new SimpleDateFormat("dd/MM").format(ts.toDate()) : "N/A";
                                if (n != null && ts != null) {
                                    flowerData.add(new ChartData(ts.toDate(), n.floatValue(), label));
                                }
                            }
                        }

                        // --- fruit ---
                        List<Map<String, Object>> rawFruit = (List<Map<String, Object>>) growthDoc.get("fruit");
                        List<ChartData> fruitData = new ArrayList<>();
                        if (rawFruit != null) {
                            for (Map<String, Object> e : rawFruit) {
                                Number n = (Number) e.get("number_of_fruit");
                                Timestamp ts = (Timestamp) e.get("date");
                                String label = ts != null ? new SimpleDateFormat("dd/MM").format(ts.toDate()) : "N/A";
                                if (n != null && ts != null) {
                                    fruitData.add(new ChartData(ts.toDate(), n.floatValue(), label));
                                }
                            }
                        }

                        // Add item
                        StatisticItem item = new StatisticItem(
                                plantId,
                                plantName,
                                heightsData,
                                leafData,
                                flowerData,
                                fruitData
                        );
                        statisticList.add(item);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.w("GrowthData", "No growth data for plantId: " + plantId);
                    }
                });
    }
}
