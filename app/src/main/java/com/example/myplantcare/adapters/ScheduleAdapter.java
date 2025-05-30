package com.example.myplantcare.adapters;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
    private static final String TAG = "ScheduleAdapter";
    private List<Pair<String, List<ScheduleWithMyPlantInfo>>> scheduleList;
    private boolean isCompletedList;

    // --- Listener Interface for Group Action ---
    public interface OnTaskGroupActionListener {
        void onMarkCheckedInGroupCompleteClick(List<ScheduleWithMyPlantInfo> tasksInGroup);
        void onUnmarkCheckedInGroupCompleteClick(List<ScheduleWithMyPlantInfo> tasksInGroup);
        // Add other group actions if needed (e.g., onUnmarkGroupCompleteClick)
    }

    private OnTaskGroupActionListener groupActionListener;
//    private ScheduleDetailAdapter.OnTaskCompletionChangeListener itemCompletionListener;


    public ScheduleAdapter(List<Pair<String, List<ScheduleWithMyPlantInfo>>> scheduleList,
                           boolean isCompletedList,
                           OnTaskGroupActionListener groupActionListener
    ) {
        this.scheduleList = scheduleList;
        this.isCompletedList = isCompletedList;
        this.groupActionListener = groupActionListener;
    }

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

        if (isCompletedList) {
            // If this is the completed list, show rollback icon
            holder.markAsComplete.setImageResource(R.drawable.ic_rollback); // Assuming you have ic_rollback drawable
            // Optionally hide the markAsComplete button if the group is empty
            holder.markAsComplete.setVisibility(tasks != null && !tasks.isEmpty() ? View.VISIBLE : View.GONE);
            holder.backgroundTask.setBackgroundResource(R.color.bg_task_completed);

        } else {
            // If this is the uncompleted list, show mark all complete icon (or similar)
            holder.markAsComplete.setImageResource(R.drawable.ic_task_checked); // Assuming you have ic_done_all or similar
            holder.markAsComplete.setVisibility(tasks != null && !tasks.isEmpty() ? View.VISIBLE : View.GONE);
            holder.backgroundTask.setBackgroundResource(R.color.bg_task_uncompleted);
        }

        holder.markAsComplete.setOnClickListener(v -> {
            if (tasks == null || tasks.isEmpty()) {
                Log.w(TAG, "Group action clicked on empty group: " + schedule.first);
                return;
            }
            Log.d(TAG, "Group action ImageView clicked for task group: " + schedule.first + ". IsCompletedList: " + isCompletedList);
            List<ScheduleWithMyPlantInfo> checkedTasks = new ArrayList<>();
            for (ScheduleWithMyPlantInfo task : tasks) {
                if (task.isCheckedForGroupAction()) { // Check the temporary state in the model
                    checkedTasks.add(task);
                }
            }
            if (!checkedTasks.isEmpty()) {
                if (groupActionListener != null) {
                    if (isCompletedList) {
                        groupActionListener.onUnmarkCheckedInGroupCompleteClick(checkedTasks);
                    } else {
                        groupActionListener.onMarkCheckedInGroupCompleteClick(checkedTasks);
                    }
                }
            } else {
                Log.d(TAG, "Group action clicked but no tasks were checked.");
            }
        });
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public void setSchedules(List<Pair<String, List<ScheduleWithMyPlantInfo>>> scheduleList) {
        this.scheduleList = scheduleList;
        notifyDataSetChanged();
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView taskName;
        RecyclerView recyclerViewTasks;
        ImageView taskIcon, markAsComplete;
        RelativeLayout backgroundTask;

        public ScheduleViewHolder(View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.taskName);
            recyclerViewTasks = itemView.findViewById(R.id.recyclerViewTasks);
            taskIcon = itemView.findViewById(R.id.ic_task);
            markAsComplete = itemView.findViewById(R.id.mark_as_complete);
            backgroundTask = itemView.findViewById(R.id.background_task);
        }

    }

}
