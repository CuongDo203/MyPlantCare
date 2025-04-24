package com.example.myplantcare.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantcare.R;
import com.example.myplantcare.models.DayModel;

import java.util.List;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {

    private List<DayModel> dayList;  // Danh sách các ngày
    private int selectedPosition = RecyclerView.NO_POSITION; // Track selected position

    // --- Listener interface to communicate clicks back to Fragment ---
    public interface OnDaySelectedListener {
        void onDaySelected(DayModel dayModel, int position);
    }

    private OnDaySelectedListener listener;

    public void setOnDaySelectedListener(OnDaySelectedListener listener) {
        this.listener = listener;
    }
    // --- End Listener interface ---

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public DayAdapter(List<DayModel> dayList) {
        this.dayList = dayList;
        // Find the initial selected position (today)
        for (int i = 0; i < dayList.size(); i++) {
            if (dayList.get(i).isSelected()) {
                selectedPosition = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item_day thành View
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);
        return new DayViewHolder(view);  // Trả về ViewHolder với view đã inflate
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        // Lấy dữ liệu từ danh sách dayList tại vị trí hiện tại
        DayModel day = dayList.get(position);

        // Gán tên ngày trong tuần và ngày của tháng vào các TextView tương ứng
        holder.dayOfWeek.setText(day.getDayOfWeek());
        holder.day.setText(String.valueOf(day.getDayOfMonth()));

        // Thay đổi màu chữ thành màu đỏ nếu đó là hôm nay, nếu không thì giữ màu đen
        holder.day.setTextColor(day.isToday() ? Color.RED : Color.BLACK);
        // Kiểm tra nếu vị trí hiện tại là ngày đã chọn hoặc hôm nay

        if(day.isSelected()) {
            holder.dayOfWeek.setBackgroundResource(R.color.bg_selected_day);
        }
        else {
            holder.dayOfWeek.setBackgroundResource(android.R.color.transparent);
        }

        holder.itemView.setOnClickListener(v -> {
            if(selectedPosition != position) {
                if (selectedPosition != RecyclerView.NO_POSITION) {
                    dayList.get(selectedPosition).setSelected(false);
                    notifyItemChanged(selectedPosition); // Update old item UI
                }
                selectedPosition = position;
                day.setSelected(true);
                notifyItemChanged(selectedPosition); // Update new item UI
                // Notify the listener (Fragment) about the selection change
                if (listener != null) {
                    listener.onDaySelected(day, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        // Trả về số lượng item trong danh sách
        return dayList.size();
    }

    // ViewHolder để quản lý các view cho mỗi item
    public static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dayOfWeek, day;  // TextViews cho tên ngày trong tuần và ngày trong tháng

        public DayViewHolder(View itemView) {
            super(itemView);
            dayOfWeek = itemView.findViewById(R.id.dayOfWeek);  // TextView cho tên ngày trong tuần
            day = itemView.findViewById(R.id.dayOfMonth);  // TextView cho ngày trong tháng
        }
    }
}
