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

    // Constructor nhận danh sách các ngày (DayModel)
    public DayAdapter(List<DayModel> dayList) {
        this.dayList = dayList;
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
        holder.day.setText(String.valueOf(day.getDay()));

        // Kiểm tra nếu vị trí hiện tại là ngày đã chọn hoặc hôm nay
        if (holder.getAdapterPosition() == position || day.isToday()) {
            // Đặt background cho ngày hôm nay hoặc ngày được chọn
            holder.dayOfWeek.setBackgroundResource(R.drawable.bg_today);
        } else {
            // Đặt lại background nếu không phải ngày được chọn hay hôm nay
            holder.dayOfWeek.setBackgroundResource(0);
        }

        // Thay đổi màu chữ thành màu đỏ nếu đó là hôm nay, nếu không thì giữ màu đen
        holder.day.setTextColor(day.isToday() ? Color.RED : Color.BLACK);

        // Lắng nghe sự kiện click vào item và thay đổi vị trí ngày đã chọn
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = holder.getAdapterPosition();  // Lấy vị trí cũ
            // Thực hiện cập nhật vị trí mới
            notifyItemChanged(previousPosition);  // Cập nhật item cũ
            notifyItemChanged(position);  // Cập nhật item mới
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
            day = itemView.findViewById(R.id.day);  // TextView cho ngày trong tháng
        }
    }
}
