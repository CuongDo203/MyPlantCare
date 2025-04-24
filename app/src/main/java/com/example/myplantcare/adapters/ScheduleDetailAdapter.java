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
    private static final String TAG = "ScheduleDetailAdapter";
    private List<ScheduleWithMyPlantInfo> taskList;

//    public interface OnTaskCompletionChangeListener {
//        void onTaskCompletionChanged(ScheduleWithMyPlantInfo task, boolean isCompleted);
//    }
//
//    private OnTaskCompletionChangeListener listener;

    // --- Constructor with Listener ---
//    public ScheduleDetailAdapter(List<ScheduleWithMyPlantInfo> taskList, OnTaskCompletionChangeListener listener) {
//        this.taskList = taskList;
//        this.listener = listener;
//    }
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
        holder.taskCompleted.setChecked(task.isCompletedOnDate());
        holder.taskCompleted.setOnCheckedChangeListener(null);
        holder.taskCompleted.setChecked(task.isCheckedForGroupAction());
        holder.taskCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Only trigger listener if checked state is changed by user interaction
            if (buttonView.isPressed()) {
                Log.d(TAG, "Checkbox state changed by user for schedule ID: " + task.getSchedule().getId() + ", isChecked: " + isChecked);

                // Update the temporary state in the model
                task.setCheckedForGroupAction(isChecked);

                // No call to ViewModel or repository from here.
                // The state change is stored in the model, to be read when the group button is clicked.

                // Optional: If ScheduleAdapter needs immediate UI feedback (e.g., change group button color)
                // scheduleAdapter.notifyItemChanged(getParentAdapterPosition()); // Requires parent adapter reference
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void setTasks(List<ScheduleWithMyPlantInfo> taskList) {
        this.taskList = taskList;
        // When data is set/updated, reset the temporary checked state for all items
        if (this.taskList != null) {
            for (ScheduleWithMyPlantInfo task : this.taskList) {
                task.setCheckedForGroupAction(false); // Reset checkbox state on data load
            }
        }
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
