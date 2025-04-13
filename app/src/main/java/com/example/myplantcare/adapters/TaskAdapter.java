package com.example.myplantcare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantcare.R;
import com.example.myplantcare.models.TaskModel;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<TaskModel> taskList;

    public TaskAdapter(List<TaskModel> taskList) {
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
        TaskModel task = taskList.get(position);
        holder.taskDetails.setText(task.getName());
        holder.taskCompleted.setChecked(task.isDescription());
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
