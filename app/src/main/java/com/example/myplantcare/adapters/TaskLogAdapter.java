package com.example.myplantcare.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myplantcare.R;
import com.example.myplantcare.models.TaskLogModel;
import com.example.myplantcare.utils.DateUtils;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TaskLogAdapter extends RecyclerView.Adapter<TaskLogAdapter.TaskLogViewHolder> {

    private static String TAG = "TaskLogAdapter";
    private List<TaskLogModel> taskLogList;
    private Context context;
    public TaskLogAdapter(Context context, List<TaskLogModel> taskLogList, OnTaskLogImageClickListener listener) {
        this.taskLogList = taskLogList;
        this.context = context;
        this.listener = listener;
    }

    private OnTaskLogImageClickListener listener;
    public interface OnTaskLogImageClickListener {
        void onTaskLogImageClick(String taskLogId);
    }

    @NonNull
    @Override
    public TaskLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_log, parent, false);
        return new TaskLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskLogViewHolder holder, int position) {
        TaskLogModel taskLog = taskLogList.get(position);
        holder.textViewDate.setText(DateUtils.formatDate(taskLog.getDate().toDate(), DateUtils.DATE_FORMAT_DISPLAY));
        Calendar calendar = Calendar.getInstance();
        long numDayBefore = getDaysBetweenDates( taskLog.getDate().toDate(), calendar.getTime());
        holder.textViewRelativeTime.setText(String.valueOf(numDayBefore)+" ngày trước");
        Log.d(TAG, "onBindViewHolder: " + taskLog);
        if(taskLog.getTaskName() != null) {
            holder.textViewTaskName.setText(taskLog.getTaskName());
            switch (taskLog.getTaskName()){
                case "Tưới nước":
                    holder.imageViewTaskIcon.setImageResource(R.drawable.ic_tuoi_nuoc);
                    break;
                case "Bón phân":
                    holder.imageViewTaskIcon.setImageResource(R.drawable.ic_bon_phan);
                    break;
                case "Kiểm tra sâu bệnh":
                    holder.imageViewTaskIcon.setImageResource(R.drawable.ic_kt_sau_benh);
                    break;
                default:
                    holder.imageViewTaskIcon.setImageResource(R.drawable.ic_tuoi_nuoc);
                    break;
            }
        }

        String imageUrl = taskLog.getUserPhotoUrl();
        if(imageUrl != null && !imageUrl.isEmpty()) {
            holder.imageViewUserPhoto.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_photo_error)
                    .error(R.drawable.ic_photo_error)
                    .centerCrop()
                    .into(holder.imageViewUserPhoto);
        }
        else {
            Glide.with(context)
                    .load(R.drawable.plant_sample2)
                    .placeholder(R.drawable.ic_photo_error)
                    .error(R.drawable.ic_photo_error)
                    .centerCrop()
                    .into(holder.imageViewUserPhoto);
        }

        if (taskLog.getId() != null && listener != null) {
            holder.imageViewUserPhoto.setOnClickListener(v -> {
                Log.d(TAG, "Image clicked for task log ID: " + taskLog.getId());
                listener.onTaskLogImageClick(taskLog.getId()); // Gọi phương thức listener
            });
        } else {
            holder.imageViewUserPhoto.setOnClickListener(null);
            Log.w(TAG, "Cannot set image click listener for task log: ID is null or listener is null.");
        }
    }

    @Override
    public int getItemCount() {
        return taskLogList == null ? 0 : taskLogList.size();
    }

    private long getDaysBetweenDates(Date startDate, Date endDate) {
        long diff = endDate.getTime() - startDate.getTime();
        long days = diff / (24 * 60 * 60 * 1000);
        return days;
    }



    static class TaskLogViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate, textViewRelativeTime, textViewTaskName;
        ImageView imageViewTaskIcon, imageViewUserPhoto;
        public TaskLogViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.text_view_log_date);
            textViewRelativeTime = itemView.findViewById(R.id.text_view_log_relative_time);
            textViewTaskName = itemView.findViewById(R.id.text_view_log_task_name);
            imageViewTaskIcon = itemView.findViewById(R.id.image_view_log_task_icon);
            imageViewUserPhoto = itemView.findViewById(R.id.image_view_log_user_photo);
        }

    }

}
