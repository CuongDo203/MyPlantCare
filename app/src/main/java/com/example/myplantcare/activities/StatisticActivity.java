package com.example.myplantcare.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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

        // RecyclerView + Adapter
        recyclerViewStatistics = findViewById(R.id.recyclerViewStatistics);
        recyclerViewStatistics.setLayoutManager(new LinearLayoutManager(this));
        statisticList = new ArrayList<>();
        adapter = new StatisticAdapter(this, statisticList, item -> {
            Intent intent = new Intent(StatisticActivity.this, DetailStatisticActivity.class);
            // Truyền Parcelable StatisticItem (không cast sang Serializable)
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
                .get()
                .addOnSuccessListener(queryGrowth -> {
                    for (QueryDocumentSnapshot growthDoc : queryGrowth) {
                        // --- heights ---
                        List<Map<String, Object>> rawHeights =
                                (List<Map<String, Object>>) growthDoc.get("heights");
                        List<ChartData> heightsData = new ArrayList<>();
//                        if (rawHeights != null) {
//                            for (int i = 0; i < rawHeights.size(); i++) {
//                                Map<String, Object> e = rawHeights.get(i);
//                                Number h = (Number) e.get("height");
//                                if (h != null) heightsData.add(new ChartData(i, h.floatValue()));
//                            }
//                        }
                        if (rawHeights != null) {
                            for (int i = 0; i < rawHeights.size(); i++) {
                                Map<String, Object> e = rawHeights.get(i);
                                Number h = (Number) e.get("height");
                                Timestamp ts = (Timestamp) e.get("date");
                                String label = ts != null
                                        ? new SimpleDateFormat("dd/MM").format(ts.toDate())
                                        : "N/A";

                                if (h != null) heightsData.add(new ChartData(i, h.floatValue(), label));
                            }
                        }

                        // --- leaf ---
                        List<Map<String, Object>> rawLeaf =
                                (List<Map<String, Object>>) growthDoc.get("leaf");
                        List<ChartData> leafData = new ArrayList<>();
//                        if (rawLeaf != null) {
//                            for (int i = 0; i < rawLeaf.size(); i++) {
//                                Map<String, Object> e = rawLeaf.get(i);
//                                Number n = (Number) e.get("number_of_leaf");
//                                if (n != null) leafData.add(new ChartData(i, n.floatValue()));
//                            }
//                        }
                        if (rawLeaf != null) {
                            for (int i = 0; i < rawLeaf.size(); i++) {
                                Map<String, Object> e = rawLeaf.get(i);
                                Number n = (Number) e.get("number_of_leaf");
                                Timestamp ts = (Timestamp) e.get("date");
                                String label = ts != null
                                        ? new SimpleDateFormat("dd/MM").format(ts.toDate())
                                        : "N/A";

                                if (n != null) leafData.add(new ChartData(i, n.floatValue(), label));
                            }
                        }

                        // --- flower ---
                        List<Map<String, Object>> rawFlower =
                                (List<Map<String, Object>>) growthDoc.get("flower");
                        List<ChartData> flowerData = new ArrayList<>();
//                        if (rawFlower != null) {
//                            for (int i = 0; i < rawFlower.size(); i++) {
//                                Map<String, Object> e = rawFlower.get(i);
//                                Number n = (Number) e.get("number_of_flower");
//                                if (n != null) flowerData.add(new ChartData(i, n.floatValue()));
//                            }
//                        }
                        if (rawFlower != null) {
                            for (int i = 0; i < rawFlower.size(); i++) {
                                Map<String, Object> e = rawFlower.get(i);
                                Number n = (Number) e.get("number_of_flower");
                                Timestamp ts = (Timestamp) e.get("date");
                                String label = ts != null
                                        ? new SimpleDateFormat("dd/MM").format(ts.toDate())
                                        : "N/A";

                                if (n != null) flowerData.add(new ChartData(i, n.floatValue(), label));
                            }
                        }

                        // --- fruit ---
                        List<Map<String, Object>> rawFruit =
                                (List<Map<String, Object>>) growthDoc.get("fruit");
                        List<ChartData> fruitData = new ArrayList<>();
//                        if (rawFruit != null) {
//                            for (int i = 0; i < rawFruit.size(); i++) {
//                                Map<String, Object> e = rawFruit.get(i);
//                                Number n = (Number) e.get("number_of_fruit");
//                                if (n != null) fruitData.add(new ChartData(i, n.floatValue()));
//                            }
//                        }
                        if (rawFruit != null) {
                            for (int i = 0; i < rawFruit.size(); i++) {
                                Map<String, Object> e = rawFruit.get(i);
                                Number n = (Number) e.get("number_of_fruit");
                                Timestamp ts = (Timestamp) e.get("date");
                                @SuppressLint("SimpleDateFormat") String label = ts != null
                                        ? new SimpleDateFormat("dd/MM").format(ts.toDate())
                                        : "N/A";

                                if (n != null) fruitData.add(new ChartData(i, n.floatValue(), label));
                            }
                        }

                        // Tạo StatisticItem với đủ 4 loại dữ liệu
                        StatisticItem item = new StatisticItem(
                                plantName,
                                heightsData,
                                leafData,
                                flowerData,
                                fruitData
                        );

                        statisticList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}

