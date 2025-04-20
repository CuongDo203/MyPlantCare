package com.example.myplantcare.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myplantcare.R;
import com.example.myplantcare.models.ChartData;
import com.example.myplantcare.models.StatisticItem;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public class StatisticAdapter extends RecyclerView.Adapter<StatisticAdapter.StatisticViewHolder> {

    private final Context context;
    private final List<StatisticItem> statisticList;

    public interface OnItemClickListener {
        void onItemClick(StatisticItem item);
    }

    private final OnItemClickListener listener;

    public StatisticAdapter(Context context, List<StatisticItem> statisticList, OnItemClickListener listener) {
        this.context = context;
        this.statisticList = statisticList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StatisticViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_statistic, parent, false);
        return new StatisticViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatisticViewHolder holder, int position) {
        StatisticItem item = statisticList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return statisticList.size();
    }

    public static class StatisticViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTreeName;
        LineChart lineChart;

        public StatisticViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTreeName = itemView.findViewById(R.id.textViewTreeName);
            lineChart = itemView.findViewById(R.id.lineChartDetail);
        }

        public void bind(final StatisticItem item, final OnItemClickListener listener) {
            // Set the tree name
            textViewTreeName.setText(item.getTreeName());

            // Prepare chart data
            ArrayList<Entry> entries = new ArrayList<>();
            for (int i = 0; i < item.getChartData().size(); i++) {
                ChartData data = item.getChartData().get(i);
                // Use getXValue and getYValue for chart data
                entries.add(new Entry(data.getXValue(), data.getYValue()));
            }

            // Create LineDataSet
            LineDataSet dataSet = new LineDataSet(entries, "Chiều cao theo ngày");
            dataSet.setColor(Color.GREEN);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(4f);
            dataSet.setCircleColor(Color.GREEN);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setDrawValues(false); // Hide values for the chart

            // Set LineData for the chart
            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);

            // X-Axis: Day count
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1f);
            xAxis.setTextColor(Color.DKGRAY);
            xAxis.setTextSize(12f);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return "Ngày " + (int) value;
                }
            });

            // Y-Axis: Height (cm)
            YAxis yAxisLeft = lineChart.getAxisLeft();
            yAxisLeft.setTextColor(Color.DKGRAY);
            yAxisLeft.setTextSize(12f);
            yAxisLeft.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return ((int) value) + " cm";
                }
            });

            lineChart.getAxisRight().setEnabled(false); // Hide the right Y-Axis

            // Disable description text for the chart
            Description description = new Description();
            description.setText("");
            lineChart.setDescription(description);

            lineChart.invalidate(); // Update the chart with new data

            // Set item click listener
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}

