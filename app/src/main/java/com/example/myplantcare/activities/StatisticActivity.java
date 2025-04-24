package com.example.myplantcare.activities;

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
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public class StatisticActivity extends AppCompatActivity {

    private RecyclerView recyclerViewStatistics;
    private StatisticAdapter adapter;
    private List<StatisticItem> statisticList;
    private TextView toolbarTitle;
    private ImageButton toolbarBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.insider_toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);
        toolbarTitle.setText("Thống kê phát triển");
        toolbarBackButton.setOnClickListener(v -> finish());

        recyclerViewStatistics = findViewById(R.id.recyclerViewStatistics);
        recyclerViewStatistics.setLayoutManager(new LinearLayoutManager(this));

        statisticList = new ArrayList<>();

        statisticList.add(createStatisticItem("Cây EFG"));
        statisticList.add(createStatisticItem("Cây ABC"));

        adapter = new StatisticAdapter(this, statisticList, item -> {
            // Chuyển Intent và truyền đối tượng StatisticItem qua putExtra
            Intent intent = new Intent(StatisticActivity.this, DetailStatisticActivity.class);
            intent.putExtra("statisticItem", item);
            startActivity(intent);
        });

        recyclerViewStatistics.setAdapter(adapter);
    }

    private StatisticItem createStatisticItem(String treeName) {
        List<ChartData> chartData = new ArrayList<>();
        chartData.add(new ChartData(0, 12));  // Ngày 1: cao 12 cm
        chartData.add(new ChartData(1, 14));  // Ngày 2: cao 14 cm
        chartData.add(new ChartData(2, 16));  // Ngày 3: cao 16 cm
        chartData.add(new ChartData(3, 18));  // Ngày 4: cao 18 cm

        return new StatisticItem(treeName, chartData);
    }
}

