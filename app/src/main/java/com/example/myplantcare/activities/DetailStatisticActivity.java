package com.example.myplantcare.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

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
    private DocumentReference growthRef;

    private ImageView imageViewPlant;
    private ImageButton buttonChangeImage;
    private ActivityResultLauncher<String> pickImageLauncher;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_statistic);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.insider_toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);
        toolbarBackButton.setOnClickListener(v -> finish());

        // Firebase init
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();
        plantId = getIntent().getStringExtra("id");
        Log.d("DetailStatisticActivity","plantId nhận được là" + plantId);

        StatisticItem item = getIntent().getParcelableExtra("statisticItem");
        if (item == null) {
            Log.e(TAG, "Không nhận được StatisticItem");
            return;
        }
        toolbarTitle.setText("Thống kê " + item.getTreeName());

        // Reference to fixed Growth doc
        growthRef = db.collection("users")
                .document(userId)
                .collection("my_plants")
                .document(plantId)
                .collection("Growth")
                .document(plantId);

        // Charts
        chartHeights = findViewById(R.id.lineChartHeights);
        chartOthers  = findViewById(R.id.lineChartOthers);

        imageViewPlant    = findViewById(R.id.imageViewPlantStatistic);
        buttonChangeImage = findViewById(R.id.buttonChangeImageStatistic);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        // Hiển thị ảnh lên ImageView
                        imageViewPlant.setImageURI(uri);

                        // TODO: nếu cần lưu lên Firebase Storage hoặc Firestore, upload ở đây
                        // uploadImageToFirebase(uri);
                    }
                }
        );

        // FAB to add
        FloatingActionButton fab = findViewById(R.id.fab_add_growth);
        fab.setOnClickListener(v -> showAddDialog());

        fetchAndRedraw();
    }

    private void fetchAndRedraw() {
        growthRef.get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    // Parse heights
                    List<ChartData> heightsData = new ArrayList<>();
                    List<Map<String, Object>> rawHeights = (List<Map<String, Object>>) doc.get("heights");
                    if (rawHeights != null) {
                        for (Map<String, Object> e : rawHeights) {
                            Timestamp ts = (Timestamp) e.get("date");
                            Number val = (Number) e.get("height");
                            if (ts != null && val != null) {
                                String label = new SimpleDateFormat("dd/MM", Locale.getDefault())
                                        .format(ts.toDate());
                                heightsData.add(new ChartData(ts.toDate(), val.floatValue(), label));
                            }
                        }
                    }

                    // Parse others
                    List<ChartData> leafData = parseArrayField(doc, "leaf", "number_of_leaf");
                    List<ChartData> flowerData = parseArrayField(doc, "flower", "number_of_flower");
                    List<ChartData> fruitData = parseArrayField(doc, "fruit", "number_of_fruit");

                    // Draw heights chart
                    List<Entry> entriesH = new ArrayList<>();
                    for (ChartData d : heightsData) {
                        entriesH.add(new Entry(d.getDate().getTime(), d.getYValue()));
                    }
                    setupTimeLineChart(chartHeights, entriesH, "Chiều cao (cm)");

                    // Draw others chart
                    @SuppressWarnings("unchecked")
                    List<ChartData>[] otherLists = new List[]{leafData, flowerData, fruitData};
                    setupTimeLineMultiChart(chartOthers, new String[]{"Lá", "Hoa", "Quả"}, otherLists);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Fetch growth failed", e);
                    Toast.makeText(this, "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }

    private List<ChartData> parseArrayField(
            com.google.firebase.firestore.DocumentSnapshot doc,
            String fieldName,
            String valueKey) {
        List<ChartData> listData = new ArrayList<>();
        List<Map<String, Object>> raw = (List<Map<String, Object>>) doc.get(fieldName);
        if (raw != null) {
            for (Map<String, Object> e : raw) {
                Timestamp ts = (Timestamp) e.get("date");
                Number val = (Number) e.get(valueKey);
                if (ts != null && val != null) {
                    String label = new SimpleDateFormat("dd/MM", Locale.getDefault())
                            .format(ts.toDate());
                    listData.add(new ChartData(ts.toDate(), val.floatValue(), label));
                }
            }
        }
        return listData;
    }

    private void setupTimeLineChart(LineChart chart, List<Entry> entries, String label) {
        if (entries.isEmpty()) {
            chart.setNoDataText("Không có dữ liệu");
            chart.invalidate();
            return;
        }
        LineDataSet ds = new LineDataSet(entries, label);
        ds.setColor(0xFF74542E); // doi mau
        ds.setCircleColor(0xFF74542E); // doi mau
        ds.setLineWidth(2f);
        ds.setCircleRadius(4f);

        chart.setData(new LineData(ds));
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setExtraBottomOffset(16f);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setTextColor(Color.BLACK); //them moi
        x.setGranularity(24 * 60 * 60 * 1000f);
        x.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            @Override public String getFormattedValue(float value) {
                return sdf.format(new Date((long) value));
            }
        });
        x.setLabelRotationAngle(-45f);
        x.setDrawGridLines(false);

        YAxis y = chart.getAxisLeft();
        y.setTextColor(Color.BLACK); //them moi
        y.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        chart.animateX(800);
        chart.invalidate();
    }

    private void setupTimeLineMultiChart(LineChart chart, String[] titles, List<ChartData>[] dataLists) {
        LineData data = new LineData();
        int[] colors = new int[]{0xFF31E331, 0xFFDC6EC2, 0xFFDBE72E};

        for (int i = 0; i < titles.length; i++) {
            List<Entry> entries = new ArrayList<>();
            List<ChartData> list = dataLists[i];
            if (list != null) {
                for (ChartData d : list) {
                    entries.add(new Entry(d.getDate().getTime(), d.getYValue()));
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
            private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            @Override public String getFormattedValue(float value) {
                return sdf.format(new Date((long) value));
            }
        });
        x.setLabelRotationAngle(-45f);
        x.setDrawGridLines(false);

        YAxis y = chart.getAxisLeft();
        y.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float value) {
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

                    Map<String, Object> updates = new HashMap<>();
                    Timestamp nowTs = Timestamp.now();

                    if (!hStr.isEmpty()) {
                        Map<String, Object> heightEntry = new HashMap<>();
                        heightEntry.put("date", nowTs);
                        heightEntry.put("height", Double.parseDouble(hStr));
                        updates.put("heights", FieldValue.arrayUnion(heightEntry));
                    }
                    if (!lfStr.isEmpty()) {
                        Map<String, Object> leafEntry = new HashMap<>();
                        leafEntry.put("date", nowTs);
                        leafEntry.put("number_of_leaf", Integer.parseInt(lfStr));
                        updates.put("leaf", FieldValue.arrayUnion(leafEntry));
                    }
                    if (!flStr.isEmpty()) {
                        Map<String, Object> flowerEntry = new HashMap<>();
                        flowerEntry.put("date", nowTs);
                        flowerEntry.put("number_of_flower", Integer.parseInt(flStr));
                        updates.put("flower", FieldValue.arrayUnion(flowerEntry));
                    }
                    if (!frStr.isEmpty()) {
                        Map<String, Object> fruitEntry = new HashMap<>();
                        fruitEntry.put("date", nowTs);
                        fruitEntry.put("number_of_fruit", Integer.parseInt(frStr));
                        updates.put("fruit", FieldValue.arrayUnion(fruitEntry));
                    }

                    growthRef.set(updates, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Firestore merge OK – các mảng đã được cập nhật");
                                Toast.makeText(this, "Đã thêm thành công!", Toast.LENGTH_SHORT).show();
                                fetchAndRedraw();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Firestore merge lỗi", e);
                                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}


