package com.example.myplantcare.adapters;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantcare.R;
import com.example.myplantcare.data.responses.ScheduleWithMyPlantInfo;
import com.example.myplantcare.models.TaskModel;

import java.util.ArrayList;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private List<Pair<String, List<ScheduleWithMyPlantInfo>>> scheduleList;

    public ScheduleAdapter(List<Pair<String, List<ScheduleWithMyPlantInfo>>> scheduleList) {
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
        Pair<String, List<ScheduleWithMyPlantInfo>> schedule = scheduleList.get(position);
        List<ScheduleWithMyPlantInfo> tasks = schedule.second;
        for(ScheduleWithMyPlantInfo task : tasks){
            Log.d("ScheduleAdapter", "Binding task: "+task.getMyPlantNickname());
            Log.d("ScheduleAdapter", "Binding task: "+task.getScheduleTime());
            Log.d("ScheduleAdapter", "Binding task: "+task.getMyPlantLocation());
        }
        holder.taskName.setText(schedule.first);
        switch (schedule.first){
            case "Tưới nước":
                holder.taskIcon.setImageResource(R.drawable.ic_tuoi_nuoc);
                break;
            case "Bón phân":
                holder.taskIcon.setImageResource(R.drawable.ic_bon_phan);
                break;
            case "Kiểm tra sâu bệnh":
                holder.taskIcon.setImageResource(R.drawable.ic_kt_sau_benh);
                break;
            default:
                holder.taskIcon.setImageResource(R.drawable.ic_tuoi_nuoc);
                break;
        }
        // Danh sách công việc, Gồm list ScheduleWithMyPlantInfo
        ScheduleDetailAdapter scheduleDetailAdapter = new ScheduleDetailAdapter(tasks);
        holder.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerViewTasks.setAdapter(scheduleDetailAdapter);
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
            taskName = itemView.findViewById(R.id.taskName);
            recyclerViewTasks = itemView.findViewById(R.id.recyclerViewTasks);
            taskIcon = itemView.findViewById(R.id.ic_task);

        }
    }

}
