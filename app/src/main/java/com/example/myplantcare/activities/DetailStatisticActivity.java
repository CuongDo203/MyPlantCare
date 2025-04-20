
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

        // Initialize views
        textViewDetailTitle = findViewById(R.id.textViewDetailTitle);
        lineChartDetail = findViewById(R.id.lineChartDetail);

        // Set up toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.insider_toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarBackButton = toolbar.findViewById(R.id.toolbar_back_button);
        toolbarTitle.setText("Thống kê chi tiết");
        toolbarBackButton.setOnClickListener(v -> finish());

        // Retrieve statistic item passed from previous activity
        StatisticItem statisticItem = getIntent().getParcelableExtra("statisticItem");

        if (statisticItem != null) {
            // Set title for the activity
            textViewDetailTitle.setText(statisticItem.getTreeName());

            // Prepare chart data from StatisticItem
            ArrayList<Entry> entries = new ArrayList<>();
            for (int i = 0; i < statisticItem.getChartData().size(); i++) {
                // Giả sử getChartData trả về danh sách các dữ liệu Entry (hoặc tương tự), chuyển đổi nó thành Entry
                ChartData data = statisticItem.getChartData().get(i);
                entries.add(new Entry(data.getXValue(), data.getYValue()));
            }

            // Log chart data for debugging
            Log.d("DetailStatisticActivity", "Chart Data: " + entries.toString());

            if (entries.isEmpty()) {
                lineChartDetail.setNoDataText("Không có dữ liệu để hiển thị");
                lineChartDetail.invalidate();
            } else {
                // Create dataset for LineChart
                LineDataSet dataSet = new LineDataSet(entries, "Chiều cao cây");
                dataSet.setColor(Color.BLUE);
                dataSet.setCircleColor(Color.GREEN);
                dataSet.setLineWidth(2f);
                dataSet.setValueTextSize(10f);
                dataSet.setValueTextColor(Color.BLACK);
                dataSet.setDrawFilled(true); // Adds a fill under the line chart

                // Set data for the LineChart
                LineData lineData = new LineData(dataSet);
                lineChartDetail.setData(lineData);

                // Customize the X-Axis
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

                // Customize the Y-Axis
                YAxis yAxisLeft = lineChartDetail.getAxisLeft();
                yAxisLeft.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return ((int) value) + " cm";
                    }
                });

                // Disable the right Y-Axis and description text
                lineChartDetail.getAxisRight().setEnabled(false);
                lineChartDetail.getDescription().setEnabled(false);

                // Refresh the chart to apply changes
                lineChartDetail.invalidate();
            }
        }
    }
}



