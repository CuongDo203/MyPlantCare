package com.example.myplantcare.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myplantcare.R;
import com.example.myplantcare.models.TaskLogModel;
import com.example.myplantcare.utils.DateUtils;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TaskLogAdapter extends RecyclerView.Adapter<TaskLogAdapter.TaskLogViewHolder> {

    private static String TAG = "TaskLogAdapter";
    private List<TaskLogModel> taskLogList;
    private Context context;
    private OnTaskLogActionListener listener;

     public interface OnTaskLogActionListener {
         void onUploadImageClick(String taskLogId);
         void onExpandImageClick(TaskLogModel taskLog);
     }

    public TaskLogAdapter(Context context, List<TaskLogModel> taskLogList, OnTaskLogActionListener listener) {
        this.taskLogList = taskLogList;
        this.context = context;
        this.listener = listener;
    }

    public void setTaskLogs(List<TaskLogModel> taskLogs) {
        this.taskLogList = taskLogs != null ? taskLogs : new ArrayList<>();
        notifyDataSetChanged(); // Thông báo cho RecyclerView cập nhật giao diện
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
                // --- Hiển thị dialog tùy chọn ---
                showImageActionDialog(taskLog); // Gọi phương thức hiển thị dialog
            });
            // Đảm bảo ImageView có thể click được
            holder.imageViewUserPhoto.setClickable(true);
            holder.imageViewUserPhoto.setFocusable(true);
        } else {
            // Nếu không có ID hoặc listener, remove listener (nếu có) và làm cho nó không thể click
            holder.imageViewUserPhoto.setOnClickListener(null);
            holder.imageViewUserPhoto.setClickable(false);
            holder.imageViewUserPhoto.setFocusable(false);
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

    private void showImageActionDialog(final TaskLogModel taskLog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Tùy chọn ảnh");

        // Các tùy chọn trong dialog
        // Hiển thị "Mở rộng ảnh" chỉ khi có URL ảnh
        List<String> optionsList = new ArrayList<>();
        if (taskLog.getUserPhotoUrl() != null && !taskLog.getUserPhotoUrl().isEmpty()) {
            optionsList.add("Mở rộng ảnh");
        }
        optionsList.add("Tải ảnh mới"); // Luôn có tùy chọn tải ảnh mới

        String[] options = optionsList.toArray(new String[0]);


        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedOption = options[which]; // Lấy text của tùy chọn đã chọn

                if ("Mở rộng ảnh".equals(selectedOption)) {
                    Log.d(TAG, "Option 'Mở rộng ảnh' selected for ID: " + taskLog.getId());
                    if (listener != null) {
                        listener.onExpandImageClick(taskLog); // Gọi listener để mở rộng ảnh
                    }
                } else if ("Tải ảnh mới".equals(selectedOption)) {
                    Log.d(TAG, "Option 'Tải ảnh mới' selected for ID: " + taskLog.getId());
                    if (listener != null && taskLog.getId() != null) {
                        listener.onUploadImageClick(taskLog.getId()); // Gọi listener để tải ảnh mới
                    } else {
                        Log.w(TAG, "Cannot call onUploadImageClick: listener is null or taskLogId is null.");
                        Toast.makeText(context, "Lỗi: Không thể tải ảnh mới.", Toast.LENGTH_SHORT).show();
                    }
                }
                // dialog.dismiss(); // Dialog tự đóng sau khi chọn item
            }
        });

        // Thêm nút Cancel
        builder.setNegativeButton("Hủy", (dialog, which) -> {
            dialog.dismiss(); // Đóng dialog
        });

        // Hiển thị dialog
        builder.create().show();
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
