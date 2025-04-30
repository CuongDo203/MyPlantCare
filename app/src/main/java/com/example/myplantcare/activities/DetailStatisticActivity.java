//package com.example.myplantcare.activities;
//
//import android.os.Bundle;
//import android.graphics.Color;
//import android.util.Log;
//import android.widget.ImageButton;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//
//import com.example.myplantcare.R;
//import com.example.myplantcare.models.ChartData;
//import com.example.myplantcare.models.StatisticItem;
//import com.github.mikephil.charting.charts.LineChart;
//import com.github.mikephil.charting.components.XAxis;
//import com.github.mikephil.charting.components.YAxis;
//import com.github.mikephil.charting.data.Entry;
//import com.github.mikephil.charting.data.LineData;
//import com.github.mikephil.charting.data.LineDataSet;
//import com.github.mikephil.charting.formatter.ValueFormatter;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class DetailStatisticActivity extends AppCompatActivity {
//
//    private static final String TAG = "DetailStatisticAct";
//    private LineChart chartHeights, chartOthers;
//    private TextView toolbarTitle;
//    private ImageButton toolbarBackButton;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_detail_statistic);
//
//        // Toolbar
//        Toolbar toolbar = findViewById(R.id.insider_toolbar);
//        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
//        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);
//        toolbarBackButton.setOnClickListener(v -> finish());
//
//        // Charts
//        chartHeights = findViewById(R.id.lineChartHeights);
//        chartOthers  = findViewById(R.id.lineChartOthers);
//
//        // Lấy data từ Intent
//        StatisticItem item = getIntent().getParcelableExtra("statisticItem");
//        if (item == null) {
//            Log.e(TAG, "Không nhận được StatisticItem từ Intent");
//            return;
//        }
//
//        // Debug log
//        Log.d(TAG, "Nhận item treeName=" + item.getTreeName());
//        Log.d(TAG, "Heights size=" + (item.getHeights() != null ? item.getHeights().size() : 0) +
//                ", Leaf size=" + (item.getLeaf() != null ? item.getLeaf().size() : 0) +
//                ", Flower size=" + (item.getFlower() != null ? item.getFlower().size() : 0) +
//                ", Fruit size=" + (item.getFruit() != null ? item.getFruit().size() : 0));
//
//        // Set toolbar title
//        toolbarTitle.setText("Thống kê " + item.getTreeName());
//
//        // Chart 1: heights
//        List<Entry> entriesH = new ArrayList<>();
//        if (item.getHeights() != null) {
//            for (ChartData d : item.getHeights()) {
//                entriesH.add(new Entry(d.getXValue(), d.getYValue()));
//            }
//        }
//        Log.d(TAG, "EntriesH size=" + entriesH.size());
//        setupSingleLineChart(chartHeights, entriesH, "Chiều cao (cm)");
//
//        // Chart 2: leaf, flower, fruit
//        List<Entry> entriesLeaf   = toEntries(item.getLeaf());
//        List<Entry> entriesFlower = toEntries(item.getFlower());
//        List<Entry> entriesFruit  = toEntries(item.getFruit());
//        Log.d(TAG, "EntriesLeaf=" + entriesLeaf.size() + ", Flower=" + entriesFlower.size() + ", Fruit=" + entriesFruit.size());
//        setupMultiLineChart(chartOthers,
//                new String[]{"Lá", "Hoa", "Quả"},
//                new List[]{entriesLeaf, entriesFlower, entriesFruit});
//    }
//
//    private List<Entry> toEntries(List<ChartData> list) {
//        List<Entry> e = new ArrayList<>();
//        if (list != null) {
//            for (ChartData d : list) {
//                e.add(new Entry(d.getXValue(), d.getYValue()));
//            }
//        }
//        return e;
//    }
//
//    private void setupSingleLineChart(LineChart chart, List<Entry> entries, String label) {
//        if (entries.isEmpty()) {
//            chart.setNoDataText("Không có dữ liệu");
//            chart.invalidate();
//            return;
//        }
//        LineDataSet ds = new LineDataSet(entries, label);
//        ds.setLineWidth(2f);
//        ds.setCircleRadius(4f);
//        ds.setDrawFilled(true);
//        LineData data = new LineData(ds);
//        chart.setData(data);
//        commonChartConfig(chart);
//    }
//
//    private void setupMultiLineChart(LineChart chart,
//                                     String[] labels,
//                                     List<Entry>[] allEntries) {
//        List<LineDataSet> sets = new ArrayList<>();
//        int[] colors = new int[]{ Color.MAGENTA, Color.YELLOW, Color.CYAN };
//        for (int i = 0; i < labels.length; i++) {
//            LineDataSet ds = new LineDataSet(allEntries[i], labels[i]);
//            ds.setLineWidth(2f);
//            ds.setCircleRadius(3f);
//            ds.setColor(colors[i]);
//            ds.setCircleColor(colors[i]);
//            sets.add(ds);
//        }
//        LineData data = new LineData();
//        for (LineDataSet ds : sets) data.addDataSet(ds);
//        chart.setData(data);
//        commonChartConfig(chart);
//    }
//
//    private void commonChartConfig(LineChart chart) {
//        chart.getDescription().setEnabled(false);
//        chart.getAxisRight().setEnabled(false);
//
//        XAxis x = chart.getXAxis();
//        x.setPosition(XAxis.XAxisPosition.BOTTOM);
//        x.setGranularity(1f);
//        x.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getFormattedValue(float value) {
//                return "Ngày " + (int)value;
//            }
//        });
//
//        YAxis y = chart.getAxisLeft();
//        y.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getFormattedValue(float value) {
//                return (int)value + "";
//            }
//        });
//
//        chart.invalidate();
//    }
//}

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
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

// ... các import giữ nguyên

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
        List<String> heightLabels = new ArrayList<>();
        if (item.getHeights() != null) {
            for (int i = 0; i < item.getHeights().size(); i++) {
                ChartData d = item.getHeights().get(i);
                entriesH.add(new Entry(i, d.getYValue()));
                // ✅ Tránh null label
                heightLabels.add(d.getLabel() != null ? d.getLabel() : "");
            }
        }
        Log.d(TAG, "EntriesH size=" + entriesH.size());
        setupSingleLineChart(chartHeights, entriesH, heightLabels, "Chiều cao (cm)");

        // Chart lá, hoa, quả
        setupMultiLineChart(chartOthers,
                new String[]{"Lá", "Hoa", "Quả"},
                new List[]{item.getLeaf(), item.getFlower(), item.getFruit()});
    }

    private void setupSingleLineChart(LineChart chart, List<Entry> entries, List<String> labels, String label) {
        if (entries.isEmpty()) {
            chart.setNoDataText("Không có dữ liệu");
            chart.invalidate();
            return;
        }

        LineDataSet ds = new LineDataSet(entries, label);
        ds.setLineWidth(2f);
        ds.setCircleRadius(4f);
        ds.setDrawFilled(true);
        ds.setColor(Color.GREEN);
        ds.setCircleColor(Color.GREEN);

        LineData data = new LineData(ds);
        chart.setData(data);
        commonChartConfig(chart, labels);
    }

    private void setupMultiLineChart(LineChart chart,
                                     String[] titles,
                                     List<ChartData>[] dataLists) {
        List<LineDataSet> sets = new ArrayList<>();
        int[] colors = new int[]{Color.MAGENTA, Color.YELLOW, Color.CYAN};
        List<String> xLabels = new ArrayList<>();

        for (int i = 0; i < titles.length; i++) {
            List<Entry> entries = new ArrayList<>();
            List<ChartData> list = dataLists[i];
            if (list == null) continue;

            for (int j = 0; j < list.size(); j++) {
                ChartData d = list.get(j);
                entries.add(new Entry(j, d.getYValue()));
                // ✅ Chỉ thêm label 1 lần từ dòng đầu tiên và kiểm tra null
                if (i == 0) xLabels.add(d.getLabel() != null ? d.getLabel() : "");
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
        commonChartConfig(chart, xLabels);
    }

    private void commonChartConfig(LineChart chart, List<String> xLabels) {
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setExtraBottomOffset(10f);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setLabelRotationAngle(-45);

        // ✅ Tránh NullPointerException khi label null
        for (int i = 0; i < xLabels.size(); i++) {
            if (xLabels.get(i) == null) xLabels.set(i, "");
        }

        x.setValueFormatter(new IndexAxisValueFormatter(xLabels));

        YAxis y = chart.getAxisLeft();
        y.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "";
            }
        });

        chart.invalidate();
    }
}

