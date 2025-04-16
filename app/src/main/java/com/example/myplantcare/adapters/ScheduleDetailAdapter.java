package com.example.myplantcare.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantcare.R;
import com.example.myplantcare.data.responses.ScheduleWithMyPlantInfo;
import com.example.myplantcare.models.TaskModel;
import com.example.myplantcare.utils.DateUtils;

import java.util.List;

public class ScheduleDetailAdapter extends RecyclerView.Adapter<ScheduleDetailAdapter.TaskViewHolder> {

    private List<ScheduleWithMyPlantInfo> taskList;

    public ScheduleDetailAdapter(List<ScheduleWithMyPlantInfo> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_detail, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        ScheduleWithMyPlantInfo task = taskList.get(position);
        Log.d("ScheduleDetailAdapter", "Schedule ID: "+task.getSchedule().getId());
        String taskDetail = task.getMyPlantNickname()+" - "+
                DateUtils.formatTimestamp(task.getScheduleTime(), DateUtils.TIME_FORMAT_DISPLAY)
                +" - "+task.getMyPlantLocation();
        holder.taskDetails.setText(taskDetail);

        holder.taskCompleted.setChecked(false);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox taskCompleted;
        TextView taskDetails;

        public TaskViewHolder(View itemView) {
            super(itemView);
            taskCompleted = itemView.findViewById(R.id.taskCompleted);
            taskDetails = itemView.findViewById(R.id.taskDetails);
        }
    }
}
