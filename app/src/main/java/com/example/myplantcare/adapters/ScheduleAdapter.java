package com.example.myplantcare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantcare.R;
import com.example.myplantcare.models.ScheduleModel;
import com.example.myplantcare.models.TaskModel;

import java.util.ArrayList;
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
        ScheduleModel schedule = scheduleList.get(position);
        holder.taskName.setText(schedule.getTaskId());
//        switch (schedule.getTaskType()){
//            case "Tưới nước":
//                holder.taskIcon.setImageResource(R.drawable.ic_tuoi_nuoc);
//                break;
//            case "Bón phân":
//                holder.taskIcon.setImageResource(R.drawable.ic_bon_phan);
//                break;
//            case "Kiểm tra sâu bệnh":
//                holder.taskIcon.setImageResource(R.drawable.ic_kt_sau_benh);
//                break;
//            default:
//                holder.taskIcon.setImageResource(R.drawable.ic_tuoi_nuoc);
//                break;
//        }
        // Danh sách công việc
        List<TaskModel> tasks = new ArrayList<>();
        TaskAdapter taskAdapter = new TaskAdapter(tasks);
        holder.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerViewTasks.setAdapter(taskAdapter);
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView taskName;
        RecyclerView recyclerViewTasks;
        ImageView taskIcon;

        public ScheduleViewHolder(View itemView) {
            super(itemView);
//            taskName = itemView.findViewById(R.id.taskName);
            recyclerViewTasks = itemView.findViewById(R.id.recyclerViewTasks);
            taskIcon = itemView.findViewById(R.id.ic_task);

        }
    }

}
