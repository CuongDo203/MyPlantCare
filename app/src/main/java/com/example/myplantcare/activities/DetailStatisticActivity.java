package com.example.myplantcare.activities;

import android.os.Bundle;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailStatisticActivity extends AppCompatActivity {

    private static final String TAG = "DetailStatisticAct";
    private LineChart chartHeights, chartOthers;
    private TextView toolbarTitle;
    private ImageButton toolbarBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_statistic);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.insider_toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);
        toolbarBackButton.setOnClickListener(v -> finish());

        // Charts
        chartHeights = findViewById(R.id.lineChartHeights);
        chartOthers  = findViewById(R.id.lineChartOthers);

        // Nhận dữ liệu
        StatisticItem item = getIntent().getParcelableExtra("statisticItem");
        if (item == null) {
            Log.e(TAG, "Không nhận được StatisticItem từ Intent");
            return;
        }

        Log.d(TAG, "Nhận item treeName=" + item.getTreeName());
        toolbarTitle.setText("Thống kê " + item.getTreeName());

        // Chart chiều cao
        List<Entry> entriesH = new ArrayList<>();
        if (item.getHeights() != null) {
            for (ChartData d : item.getHeights()) {
                // 1) Dùng timestamp làm X
                float x = d.getDate().getTime();
                float y = d.getYValue();
                entriesH.add(new Entry(x, y));
            }
        }
        setupTimeLineChart(chartHeights, entriesH, "Chiều cao (cm)");

        // Chart lá, hoa, quả
        setupTimeLineMultiChart(chartOthers,
                new String[]{"Lá", "Hoa", "Quả"},
                new List[]{item.getLeaf(), item.getFlower(), item.getFruit()});
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

        // Cấu hình trục X
        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(24 * 60 * 60 * 1000f);      // 1 ngày = 24h*60m*60s*1000ms
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

        // Giữ trục Y như trước
        YAxis y = chart.getAxisLeft();
        y.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "";
            }
        });

        chart.animateX(800);
        chart.invalidate();
    }

    private void setupTimeLineMultiChart(LineChart chart,
                                         String[] titles,
                                         List<ChartData>[] dataLists) {
        List<LineDataSet> sets = new ArrayList<>();
        int[] colors = new int[]{ Color.MAGENTA, Color.YELLOW, Color.CYAN };

        for (int i = 0; i < titles.length; i++) {
            List<Entry> entries = new ArrayList<>();
            List<ChartData> list = dataLists[i];
            if (list == null) continue;

            for (ChartData d : list) {
                // x dùng timestamp
                float x = d.getDate().getTime();
                float y = d.getYValue();
                entries.add(new Entry(x, y));
            }

            LineDataSet ds = new LineDataSet(entries, titles[i]);
            ds.setLineWidth(2f);
            ds.setCircleRadius(3f);
            ds.setColor(colors[i]);
            ds.setCircleColor(colors[i]);
            sets.add(ds);
        }

        LineData data = new LineData();
        for (LineDataSet ds : sets) {
            data.addDataSet(ds);
        }
        chart.setData(data);

        // Thiết lập trục X thời gian
        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(24 * 60 * 60 * 1000f); // 1 ngày
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

        // Trục Y
        YAxis y = chart.getAxisLeft();
        y.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "";
            }
        });

        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.animateX(800);
        chart.invalidate();
    }
}

