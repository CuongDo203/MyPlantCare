package com.example.myplantcare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantcare.R;
import com.example.myplantcare.models.ScheduleModel;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private List<ScheduleModel> scheduleList;

    public ScheduleAdapter(List<ScheduleModel> scheduleList) {
        this.scheduleList = scheduleList;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
//        ScheduleModel schedule = scheduleList.get(position);
//        holder.taskName.setText(schedule.getTaskName());
//        holder.taskDetails.setText(schedule.getTaskDetails());
//        holder.taskCompleted.setChecked(schedule.isCompleted());
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, taskDetails;
        CheckBox taskCompleted;

        public ScheduleViewHolder(View itemView) {
            super(itemView);
//            taskName = itemView.findViewById(R.id.taskName);
//            taskDetails = itemView.findViewById(R.id.taskDetails);
//            taskCompleted = itemView.findViewById(R.id.taskCompleted);
        }
    }

}
