package com.example.myplantcare.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantcare.R;
import com.example.myplantcare.data.responses.ScheduleDisplayItem;
import com.example.myplantcare.models.ScheduleModel;
import com.example.myplantcare.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class MyPlantScheduleAdapter extends RecyclerView.Adapter<MyPlantScheduleAdapter.MyPlantScheduleViewHolder> {

    private static final String TAG = "MyPlantScheduleAdapter";
    private List<ScheduleDisplayItem> scheduleList = new ArrayList<>();
    private OnScheduleItemClickListener listener;

    public interface OnScheduleItemClickListener {
        void onDeleteClick(ScheduleDisplayItem schedule); // Xử lý click nút xóa
        void onScheduleItemClick(ScheduleDisplayItem schedule);
    }

    public MyPlantScheduleAdapter(OnScheduleItemClickListener listener) {
        this.listener = listener;
    }

    public void setSchedules(List<ScheduleDisplayItem> newScheduleList) {
        this.scheduleList = newScheduleList != null ? newScheduleList : new ArrayList<>();
        notifyDataSetChanged();
        Log.d(TAG, "ScheduleAdapter data updated. New size: " + this.scheduleList.size());
    }

    @NonNull
    @Override
    public MyPlantScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_plant_schedule, parent, false);
        return new MyPlantScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPlantScheduleViewHolder holder, int position) {
        ScheduleDisplayItem schedule = scheduleList.get(position);
        holder.taskName.setText(schedule.getTaskName());
        holder.frequency.setText(convertFrequencyToText(schedule.getFrequency()));
        String formatTime = DateUtils.formatTimestampToTimeString(schedule.getTime());
        holder.time.setText(formatTime);
        if (schedule.getStartDate() != null) {
            String formattedDate = DateUtils.formatTimestamp(schedule.getStartDate(), DateUtils.DATE_FORMAT_DISPLAY);
            String []tmp = formattedDate.split("/");
            String displayDate = tmp[0] + " th " + tmp[1];
            holder.startDate.setText(displayDate);
        } else {
            holder.startDate.setText("--/--/----");
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(schedule);
                Log.d(TAG, "Delete clicked for schedule ID: " + schedule.getScheduleId());
            }
            else {
                Log.w(TAG, "OnScheduleItemClickListener is null. Delete click not handled.");
            }
        });
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onScheduleItemClick(schedule);
                Log.d(TAG, "Item clicked for schedule ID: " + schedule.getScheduleId());
            }
            else {
                Log.w(TAG, "OnScheduleItemClickListener is null. Item click not handled.");
            }
        });
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    private String convertFrequencyToText(int frequency) {
        switch (frequency) {
            case 1:
                return "Hàng ngày";
            case 7:
                return "Hàng tuần";
            default:
                return String.format("%d ngày 1 lần", frequency);
        }
    }

    static class MyPlantScheduleViewHolder extends RecyclerView.ViewHolder {

        TextView taskName, frequency, time, startDate;
        ImageView btnDelete;

        MyPlantScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.task_name);
            frequency = itemView.findViewById(R.id.frequency);
            time = itemView.findViewById(R.id.time);
            startDate = itemView.findViewById(R.id.start_date);
            btnDelete = itemView.findViewById(R.id.btn_delete_schedule);
        }

    }
}
