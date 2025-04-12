package com.example.myplantcare.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantcare.R;
import com.example.myplantcare.activities.MyPlantDetailActivity;
import com.example.myplantcare.activities.PlantLogActivity;
import com.example.myplantcare.models.MyPlantModel;

import java.util.List;

public class MyPlantAdapter  extends RecyclerView.Adapter<MyPlantAdapter.MyPlantViewHolder> {

    List<MyPlantModel> myPlantList;

    public MyPlantAdapter(List<MyPlantModel> myPlantList) {
        this.myPlantList = myPlantList;
    }

    @NonNull
    @Override
    public MyPlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_plant, parent, false);
        return new MyPlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPlantViewHolder holder, int position) {
        MyPlantModel myPlant = myPlantList.get(position);
        holder.myPlantName.setText(myPlant.getNickname());
        holder.myPlantLocation.setText(myPlant.getLocation());
        holder.myPlantProgress.setText(myPlant.getProgress() + "%");
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), MyPlantDetailActivity.class);
            intent.putExtra("plantName", myPlant.getNickname());
            intent.putExtra("location", myPlant.getLocation());
            // Thêm thông tin khác sau
            holder.itemView.getContext().startActivity(intent);
        });
        holder.btnMyPlantLogs.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), PlantLogActivity.class);
            intent.putExtra("plantName", myPlant.getNickname());
            intent.putExtra("image", myPlant.getImage());
            intent.putExtra("id", myPlant.getPlantId());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return myPlantList != null ? myPlantList.size() : 0;
    }

    public static  class MyPlantViewHolder extends RecyclerView.ViewHolder {

        ImageView myPlantImage;
        ImageView btnDeleteMyPlant;
        TextView myPlantName;
        TextView myPlantLocation;
        TextView myPlantProgress;
        LinearLayout btnMyPlantNotes;
        LinearLayout btnMyPlantLogs;

        public MyPlantViewHolder(View itemView) {
            super(itemView);
            myPlantImage = itemView.findViewById(R.id.img_my_plant);
            btnDeleteMyPlant = itemView.findViewById(R.id.btn_delete_my_plant);
            myPlantName = itemView.findViewById(R.id.my_plant_name);
            myPlantLocation = itemView.findViewById(R.id.my_plant_location);
            myPlantProgress = itemView.findViewById(R.id.my_plant_progress);
            btnMyPlantNotes = itemView.findViewById(R.id.btn_my_plant_notes);
            btnMyPlantLogs = itemView.findViewById(R.id.btn_my_plant_logs);
        }

    }

}
