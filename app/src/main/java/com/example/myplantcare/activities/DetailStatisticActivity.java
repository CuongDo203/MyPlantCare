
package com.example.myplantcare.activities;


import android.os.Bundle;
import android.graphics.Color;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myplantcare.R;
import com.example.myplantcare.models.ChartData;
import com.example.myplantcare.models.StatisticItem;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import android.util.Log;

public class DetailStatisticActivity extends AppCompatActivity {

    private TextView textViewDetailTitle;
    private LineChart lineChartDetail;
    private TextView toolbarTitle;
    private ImageButton toolbarBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_statistic);


        textViewDetailTitle = findViewById(R.id.textViewDetailTitle);
        lineChartDetail = findViewById(R.id.lineChartDetail);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.insider_toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);
        toolbarTitle.setText("Thống kê chi tiết");
        toolbarBackButton.setOnClickListener(v -> finish());


        StatisticItem statisticItem = getIntent().getParcelableExtra("statisticItem");

        if (statisticItem != null) {

            textViewDetailTitle.setText(statisticItem.getTreeName());

            ArrayList<Entry> entries = new ArrayList<>();
            for (int i = 0; i < statisticItem.getChartData().size(); i++) {
                ChartData data = statisticItem.getChartData().get(i);
                entries.add(new Entry(data.getXValue(), data.getYValue()));
            }

            Log.d("DetailStatisticActivity", "Chart Data: " + entries.toString());

            if (entries.isEmpty()) {
                lineChartDetail.setNoDataText("Không có dữ liệu để hiển thị");
                lineChartDetail.invalidate();
            } else {
                LineDataSet dataSet = new LineDataSet(entries, "Chiều cao cây");
                dataSet.setColor(Color.BLUE);
                dataSet.setCircleColor(Color.GREEN);
                dataSet.setLineWidth(2f);
                dataSet.setValueTextSize(10f);
                dataSet.setValueTextColor(Color.BLACK);
                dataSet.setDrawFilled(true);

                LineData lineData = new LineData(dataSet);
                lineChartDetail.setData(lineData);

                XAxis xAxis = lineChartDetail.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);
                xAxis.setDrawGridLines(false);
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return "Ngày " + ((int) value);
                    }
                });


                YAxis yAxisLeft = lineChartDetail.getAxisLeft();
                yAxisLeft.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return ((int) value) + " cm";
                    }
                });


                lineChartDetail.getAxisRight().setEnabled(false);
                lineChartDetail.getDescription().setEnabled(false);


                lineChartDetail.invalidate();
            }
        }
    }
}



