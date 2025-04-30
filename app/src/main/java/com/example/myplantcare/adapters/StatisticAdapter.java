//package com.example.myplantcare.adapters;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import com.example.myplantcare.R;
//import com.example.myplantcare.models.ChartData;
//import com.example.myplantcare.models.StatisticItem;
//import com.github.mikephil.charting.charts.LineChart;
//import com.github.mikephil.charting.components.Description;
//import com.github.mikephil.charting.components.XAxis;
//import com.github.mikephil.charting.components.YAxis;
//import com.github.mikephil.charting.data.LineData;
//import com.github.mikephil.charting.data.LineDataSet;
//import com.github.mikephil.charting.formatter.ValueFormatter;
//import com.github.mikephil.charting.data.Entry;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class StatisticAdapter extends RecyclerView.Adapter<StatisticAdapter.StatisticViewHolder> {
//
//    private final Context context;
//    private final List<StatisticItem> statisticList;
//
//    public interface OnItemClickListener {
//        void onItemClick(StatisticItem item);
//    }
//
//    private final OnItemClickListener listener;
//
//    public StatisticAdapter(Context context, List<StatisticItem> statisticList, OnItemClickListener listener) {
//        this.context = context;
//        this.statisticList = statisticList;
//        this.listener = listener;
//    }
//
//    @NonNull
//    @Override
//    public StatisticViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_statistic, parent, false);
//        return new StatisticViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull StatisticViewHolder holder, int position) {
//        StatisticItem item = statisticList.get(position);
//        holder.bind(item, listener);
//    }
//
//    @Override
//    public int getItemCount() {
//        return statisticList.size();
//    }
//
//    public static class StatisticViewHolder extends RecyclerView.ViewHolder {
//        TextView textViewTreeName;
//        LineChart lineChart;
//
//        public StatisticViewHolder(@NonNull View itemView) {
//            super(itemView);
//            textViewTreeName = itemView.findViewById(R.id.textViewTreeName);
//            lineChart = itemView.findViewById(R.id.lineChartDetail);
//        }
//
//        public void bind(final StatisticItem item, final OnItemClickListener listener) {
//            textViewTreeName.setText(item.getTreeName());
//
//            // Chỉ dùng dữ liệu heights để hiển thị chart trong item
//            List<ChartData> heights = item.getHeights();
//            ArrayList<Entry> entries = new ArrayList<>();
//            for (ChartData data : heights) {
//                entries.add(new Entry(data.getXValue(), data.getYValue()));
//            }
//
//            LineDataSet dataSet = new LineDataSet(entries, "Chiều cao theo ngày");
//            dataSet.setColor(Color.GREEN);
//            dataSet.setLineWidth(2f);
//            dataSet.setCircleRadius(4f);
//            dataSet.setCircleColor(Color.GREEN);
//            dataSet.setValueTextColor(Color.BLACK);
//            dataSet.setDrawValues(false);
//
//            LineData lineData = new LineData(dataSet);
//            lineChart.setData(lineData);
//
//            XAxis xAxis = lineChart.getXAxis();
//            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//            xAxis.setDrawGridLines(false);
//            xAxis.setGranularity(1f);
//            xAxis.setTextColor(Color.DKGRAY);
//            xAxis.setTextSize(12f);
//            xAxis.setValueFormatter(new ValueFormatter() {
//                @Override
//                public String getFormattedValue(float value) {
//                    return "Ngày " + ((int) value);
//                }
//            });
//
//            YAxis yAxisLeft = lineChart.getAxisLeft();
//            yAxisLeft.setTextColor(Color.DKGRAY);
//            yAxisLeft.setTextSize(12f);
//            yAxisLeft.setValueFormatter(new ValueFormatter() {
//                @Override
//                public String getFormattedValue(float value) {
//                    return ((int) value) + " cm";
//                }
//            });
//
//            lineChart.getAxisRight().setEnabled(false);
//            Description description = new Description();
//            description.setText("");
//            lineChart.setDescription(description);
//            lineChart.invalidate();
//
//            itemView.setOnClickListener(v -> listener.onItemClick(item));
//        }
//    }
//}


package com.example.myplantcare.adapters;

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

public class StatisticAdapter extends RecyclerView.Adapter<StatisticAdapter.StatisticViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(StatisticItem item);
    }

    private final Context context;
    private final List<StatisticItem> statisticList;
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
            lineChart       = itemView.findViewById(R.id.lineChartDetail);
        }

        public void bind(final StatisticItem item, final OnItemClickListener listener) {
            // 1. Tree name
            textViewTreeName.setText(item.getTreeName());

            // 2. Build entries from heights only
            List<ChartData> heights = item.getHeights();
            ArrayList<Entry> entries = new ArrayList<>(heights.size());
            for (ChartData d : heights) {
                entries.add(new Entry(d.getXValue(), d.getYValue()));
            }

            // 3. DataSet styling
            LineDataSet dataSet = new LineDataSet(entries, "Chiều cao theo ngày");
            dataSet.setColor(Color.GREEN);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(4f);
            dataSet.setCircleColor(Color.GREEN);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setDrawValues(false);

            // 4. Apply to chart
            lineChart.setData(new LineData(dataSet));

            // 5. X axis
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

            // 6. Y axis
            YAxis yAxis = lineChart.getAxisLeft();
            yAxis.setTextColor(Color.DKGRAY);
            yAxis.setTextSize(12f);
            yAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return (int) value + " cm";
                }
            });
            lineChart.getAxisRight().setEnabled(false);

            // 7. Disable description
            Description desc = new Description();
            desc.setText("");
            lineChart.setDescription(desc);

            // 8. Refresh
            lineChart.invalidate();

            // 9. Click listener
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
