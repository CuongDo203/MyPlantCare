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

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder>{

    private List<DayModel> dayList;

    public DayAdapter(List<DayModel> dayList) {
        this.dayList = dayList;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DayModel day = dayList.get(position);
        holder.dayOfWeek.setText(day.getDayOfWeek());
        holder.day.setText(String.valueOf(day.getDay()));

        if (day.isToday()) {
            holder.day.setTextColor(Color.RED);
//            holder.day.setBackgroundResource(R.drawable.bg_today);
        } else {
            holder.day.setTextColor(Color.BLACK);
            holder.day.setBackgroundResource(0);
        }
    }

    @Override
    public int getItemCount() {
        return dayList.size();
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dayOfWeek, day;

        public DayViewHolder(View itemView) {
            super(itemView);
            dayOfWeek = itemView.findViewById(R.id.dayOfWeek);
            day = itemView.findViewById(R.id.day);
        }
    }

}
