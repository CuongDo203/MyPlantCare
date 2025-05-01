package com.example.myplantcare.activities;

import android.os.Bundle;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myplantcare.R;
import com.example.myplantcare.models.ChartData;
import com.example.myplantcare.models.StatisticItem;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailStatisticActivity extends AppCompatActivity {

    private static final String TAG = "DetailStatisticAct";

    private LineChart chartHeights, chartOthers;
    private TextView toolbarTitle;
    private ImageButton toolbarBackButton;

    private String plantId;
    private String userId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_statistic);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.insider_toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);
        toolbarBackButton.setOnClickListener(v -> finish());

        // Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();
        plantId = getIntent().getStringExtra("id"); // phải truyền "id" trong Intent
        Log.d("DetailStatisticActivity", "myPlantId nhận được: " + plantId);

        // Charts
        chartHeights = findViewById(R.id.lineChartHeights);
        chartOthers  = findViewById(R.id.lineChartOthers);

        // Lấy StatisticItem
        StatisticItem item = getIntent().getParcelableExtra("statisticItem");
        if (item == null) {
            Log.e(TAG, "Không nhận được StatisticItem");
            return;
        }
        toolbarTitle.setText("Thống kê " + item.getTreeName());

        // --- Chart Chiều cao ---
        List<Entry> entriesH = new ArrayList<>();
        for (ChartData d : item.getHeights()) {
            entriesH.add(new Entry(
                    d.getDate().getTime(),
                    d.getYValue()
            ));
        }
        setupTimeLineChart(chartHeights, entriesH, "Chiều cao (cm)");

        // --- Chart Lá / Hoa / Quả ---
        // Mảng List<ChartData>[] truyền vào đúng kiểu List
        @SuppressWarnings("unchecked")
        List<ChartData>[] others = new List[]{
                item.getLeaf(),
                item.getFlower(),
                item.getFruit()
        };
        setupTimeLineMultiChart(chartOthers,
                new String[]{"Lá", "Hoa", "Quả"},
                others
        );

        // --- FAB thêm thông số ---
        FloatingActionButton fab = findViewById(R.id.fab_add_growth);
        fab.setOnClickListener(v -> showAddDialog());
    }

    private void setupTimeLineChart(LineChart chart, List<Entry> entries, String label) {
        if (entries.isEmpty()) {
            chart.setNoDataText("Không có dữ liệu");
            chart.invalidate();
            return;
        }
        LineDataSet ds = new LineDataSet(entries, label);
        ds.setColor(Color.GREEN);
        ds.setCircleColor(Color.GREEN);
        ds.setLineWidth(2f);
        ds.setCircleRadius(4f);

        chart.setData(new LineData(ds));
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setExtraBottomOffset(16f);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(24 * 60 * 60 * 1000f); // 1 ngày = 86400000 ms
        x.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat sdf =
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            @Override
            public String getFormattedValue(float value) {
                return sdf.format(new Date((long) value));
            }
        });
        x.setLabelRotationAngle(-45f);
        x.setDrawGridLines(false);

        YAxis y = chart.getAxisLeft();
        y.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        chart.animateX(800);
        chart.invalidate();
    }

    private void setupTimeLineMultiChart(LineChart chart, String[] titles, List<ChartData>[] dataLists) {
        LineData data = new LineData();
        int[] colors = new int[]{ Color.MAGENTA, Color.YELLOW, Color.CYAN };

        for (int i = 0; i < titles.length; i++) {
            List<Entry> entries = new ArrayList<>();
            List<ChartData> list = dataLists[i];
            if (list != null) {
                for (ChartData d : list) {
                    entries.add(new Entry(
                            d.getDate().getTime(),
                            d.getYValue()
                    ));
                }
            }
            LineDataSet ds = new LineDataSet(entries, titles[i]);
            ds.setColor(colors[i]);
            ds.setCircleColor(colors[i]);
            ds.setLineWidth(2f);
            ds.setCircleRadius(3f);
            data.addDataSet(ds);
        }

        chart.setData(data);
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(24 * 60 * 60 * 1000f);
        x.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat sdf =
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            @Override
            public String getFormattedValue(float value) {
                return sdf.format(new Date((long) value));
            }
        });
        x.setLabelRotationAngle(-45f);
        x.setDrawGridLines(false);

        YAxis y = chart.getAxisLeft();
        y.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        chart.animateX(800);
        chart.invalidate();
    }

    private void showAddDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_growth, null);
        TextInputEditText etHeight = view.findViewById(R.id.etHeight);
        TextInputEditText etLeaf   = view.findViewById(R.id.etLeaf);
        TextInputEditText etFlower = view.findViewById(R.id.etFlower);
        TextInputEditText etFruit  = view.findViewById(R.id.etFruit);
        TextView tvDate            = view.findViewById(R.id.tvDate);

        // Hiển thị ngày giờ hiện tại
        Date now = new Date();
        tvDate.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(now));

        new AlertDialog.Builder(this)
                .setTitle("Thêm thông số phát triển")
                .setView(view)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String hStr  = etHeight.getText().toString().trim();
                    String lfStr = etLeaf.getText().toString().trim();
                    String flStr = etFlower.getText().toString().trim();
                    String frStr = etFruit.getText().toString().trim();

                    Map<String, Object> payload = new HashMap<>();
                    payload.put("date", Timestamp.now());
                    if (!hStr.isEmpty())  payload.put("height", Double.parseDouble(hStr));
                    if (!lfStr.isEmpty()) payload.put("number_of_leaf", Integer.parseInt(lfStr));
                    if (!flStr.isEmpty()) payload.put("number_of_flower", Integer.parseInt(flStr));
                    if (!frStr.isEmpty()) payload.put("number_of_fruit", Integer.parseInt(frStr));

                    db.collection("users")
                            .document(userId)
                            .collection("my_plants")
                            .document(plantId)
                            .collection("Growth")
                            .add(payload)
                            .addOnSuccessListener(ref -> {
                                Toast.makeText(this, "Đã thêm thành công!", Toast.LENGTH_SHORT).show();
                                recreate(); // load lại toàn bộ activity để fetch + redraw
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}



