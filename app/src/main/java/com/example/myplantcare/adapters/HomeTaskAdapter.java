package com.example.myplantcare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myplantcare.R;
import com.example.myplantcare.data.responses.ScheduleWithMyPlantInfo;
import com.example.myplantcare.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class HomeTaskAdapter extends RecyclerView.Adapter<HomeTaskAdapter.HomeTaskViewHolder> {

    private List<ScheduleWithMyPlantInfo> taskList = new ArrayList<>();
    private OnTaskClickListener taskClickListener; // Listener for item clicks

    // Listener interface
    public interface OnTaskClickListener {
        void onTaskClick(ScheduleWithMyPlantInfo task);
    }


    // Constructor
    public HomeTaskAdapter(OnTaskClickListener taskClickListener) {
        this.taskClickListener = taskClickListener;
    }

    // Method to update data
    public void setTasks(List<ScheduleWithMyPlantInfo> tasks) {
        taskList.clear();
        if (tasks != null) {
            taskList.addAll(tasks);
        }
        notifyDataSetChanged(); // Notify RecyclerView of data changes
    }

    @NonNull
    @Override
    public HomeTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_task, parent, false);
        return new HomeTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeTaskViewHolder holder, int position) {
        ScheduleWithMyPlantInfo task = taskList.get(position);

        if (task.getMyPlant() != null && task.getMyPlant().getImage() != null && holder.ivPlantImage.getContext() != null) {
            Glide.with(holder.ivPlantImage.getContext())
                    .load(task.getMyPlant().getImage())
                    .placeholder(R.drawable.plant_sample) // Add a default plant image drawable
                    .error(R.drawable.ic_photo_error)
                    .into(holder.ivPlantImage);
        } else {
            if (holder.ivPlantImage.getContext() != null) {
                holder.ivPlantImage.setImageResource(R.drawable.ic_photo_error);
            }
        }

        String taskName = task.getSchedule() != null ? task.getSchedule().getTaskId() : null;

        String currentTaskName = task.getTaskName();
        if (currentTaskName != null) {
            switch (currentTaskName){
                case "Tưới nước": holder.ivTaskIcon.setImageResource(R.drawable.ic_tuoi_nuoc); break;
                case "Bón phân": holder.ivTaskIcon.setImageResource(R.drawable.ic_bon_phan); break;
                case "Kiểm tra sâu bệnh": holder.ivTaskIcon.setImageResource(R.drawable.ic_kt_sau_benh); break;
                default: holder.ivTaskIcon.setImageResource(R.drawable.ic_tuoi_nuoc); break;
            }
        } else {
            holder.ivTaskIcon.setImageResource(R.drawable.ic_tuoi_nuoc); // Default or error icon
        }


        // Task Title (Plant Nickname + Task Name)
        String plantNickname = task.getMyPlantNickname();
        String taskTitle = (plantNickname != null ? plantNickname : "Cây") +
                (currentTaskName != null ? " cần " + currentTaskName.toLowerCase() : " - Task");
        holder.tvTaskTitle.setText(taskTitle);


        // Time and Location
        String timeLocation = "";
        if (task.getScheduleTime() != null) {
            timeLocation += DateUtils.formatTimestamp(task.getScheduleTime(), DateUtils.TIME_FORMAT_DISPLAY);
        }
        if (task.getMyPlantLocation() != null && !task.getMyPlantLocation().isEmpty()) {
            if (!timeLocation.isEmpty()) timeLocation += " - ";
            timeLocation += task.getMyPlantLocation();
        }
        if (timeLocation.isEmpty()) timeLocation = "--";
        holder.tvTaskTimeLocation.setText(timeLocation);


        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            if (taskClickListener != null) {
                taskClickListener.onTaskClick(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // ViewHolder class
    public static class HomeTaskViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlantImage;
        ImageView ivTaskIcon;
        TextView tvTaskTitle;
        TextView tvTaskTimeLocation;

        public HomeTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlantImage = itemView.findViewById(R.id.iv_plant_image);
            ivTaskIcon = itemView.findViewById(R.id.iv_task_icon);
            tvTaskTitle = itemView.findViewById(R.id.tv_task_title);
            tvTaskTimeLocation = itemView.findViewById(R.id.tv_task_time_location);
            // Assuming R.id.iv_plant_image, R.id.iv_task_icon, etc. exist in item_home_task.xml
        }
    }

}
