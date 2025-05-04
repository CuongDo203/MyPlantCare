package com.example.myplantcare.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantcare.R;
import com.example.myplantcare.models.ChartData;
import com.example.myplantcare.models.StatisticItem;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StatisticAdapter extends RecyclerView.Adapter<StatisticAdapter.StatisticViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(StatisticItem item);
    }

    private final Context context;
    private List<StatisticItem> statisticList;
    private final OnItemClickListener listener;

    public StatisticAdapter(Context context, List<StatisticItem> statisticList, OnItemClickListener listener) {
        this.context = context;
        this.statisticList = statisticList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StatisticViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_statistic, parent, false);
        return new StatisticViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatisticViewHolder holder, int position) {
        holder.bind(statisticList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return statisticList.size();
    }

    static class StatisticViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTreeName;
        private final LineChart lineChart;

        public StatisticViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTreeName = itemView.findViewById(R.id.textViewTreeName);
            lineChart = itemView.findViewById(R.id.lineChartDetail);
        }

        public void bind(final StatisticItem item, final OnItemClickListener listener) {
            // Tree name
            textViewTreeName.setText(item.getTreeName());

            // Build entries using timestamp for X
            List<ChartData> heights = item.getHeights();
            ArrayList<Entry> entries = new ArrayList<>();
            for (ChartData d : heights) {
                long ms = d.getDate().getTime();
                entries.add(new Entry((float) ms, d.getYValue()));
            }

            // Configure DataSet
            LineDataSet dataSet = new LineDataSet(entries, "Chiều cao theo ngày");
            dataSet.setColor(0xFF74542E);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(4f);
            dataSet.setCircleColor(0xFF74542E);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setDrawValues(false);

            // Apply to chart
            lineChart.setData(new LineData(dataSet));

            // XAxis as date
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setAxisLineColor(0xff000000); //
            xAxis.setDrawGridLines(true);
            xAxis.setTextColor(0xff000000);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(24 * 60 * 60 * 1000f);
            xAxis.setValueFormatter(new ValueFormatter() {
                private final SimpleDateFormat sdf =
                        new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                @Override
                public String getFormattedValue(float value) {
                    return sdf.format(new Date((long) value));
                }
            });
            xAxis.setEnabled(false);

            // Y axis
            YAxis yAxis = lineChart.getAxisLeft();
            yAxis.setAxisLineColor(0xff000000);
            yAxis.setGridColor(0xFF666666);
            yAxis.setDrawGridLines(true);
            yAxis.setTextColor(0xff000000);
            yAxis.setTextColor(Color.DKGRAY);
            yAxis.setTextSize(12f);
            yAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return (int) value + " cm";
                }
            });
            lineChart.getAxisRight().setEnabled(false);

            // Remove description
            Description desc = new Description();
            desc.setText("");
            lineChart.setDescription(desc);

            lineChart.invalidate();

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<StatisticItem> newList) {
        this.statisticList.clear();
        this.statisticList.addAll(newList);
        notifyDataSetChanged();
    }
}

