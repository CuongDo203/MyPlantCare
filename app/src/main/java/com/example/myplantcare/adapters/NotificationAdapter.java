package com.example.myplantcare.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myplantcare.R;
import com.example.myplantcare.models.Notification;
import java.util.List;
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notificationList;
    private OnItemClickListener onItemClickListener;
    public NotificationAdapter(List<Notification> notificationList, OnItemClickListener onItemClickListener) {
        this.notificationList = notificationList;
        this.onItemClickListener = onItemClickListener;
    }
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        // Set the data for each item
        holder.tvTitle.setText(notification.getTitle());
        holder.tvContent.setText(notification.getContent());
        holder.tvTime.setText(notification.getCreated_At());

        // Set the click listener
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(notification);  // Handle the click event
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvTime;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvContent);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Notification notification);
    }

    public void addNotification(Notification notification) {
        notificationList.add(notification);
        notifyItemInserted(notificationList.size() - 1);
    }
    public void clearNotifications() {
        notificationList.clear();
        notifyDataSetChanged();
    }
}
