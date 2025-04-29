package com.example.myplantcare.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantcare.models.MyPlantModel;

import java.util.List;

import com.example.myplantcare.R;

public class MyPlantAdapterForSelectDialog extends RecyclerView.Adapter<MyPlantAdapterForSelectDialog.MyViewHolder> {
    private final List<MyPlantModel> plantList;
    private int selectedPosition = -1;
    private OnPlantSelectedListener listener;

    public interface OnPlantSelectedListener {
        void onPlantSelected(MyPlantModel plant);
    }

    public MyPlantAdapterForSelectDialog(List<MyPlantModel> plantList, OnPlantSelectedListener listener) {
        this.plantList = plantList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant_select_for_note, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MyPlantModel plant = plantList.get(position);
        holder.tvPlantName.setText(plant.getNickname());

        boolean isSelected = selectedPosition == position;
        holder.itemView.setBackgroundResource(isSelected ? R.drawable.bg_plant_selected : R.drawable.bg_plant_normal);

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
            listener.onPlantSelected(plant);
        });
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlantName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlantName = itemView.findViewById(R.id.tv_plant_name);
        }
    }
}
