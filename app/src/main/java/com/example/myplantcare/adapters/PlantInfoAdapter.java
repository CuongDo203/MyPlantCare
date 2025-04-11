package com.example.myplantcare.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantcare.R;
import com.example.myplantcare.activities.PlantInfoDetail;
import com.example.myplantcare.models.PlantModel;

import java.util.List;

public class PlantInfoAdapter extends RecyclerView.Adapter<PlantInfoAdapter.PlantInfoViewHolder>{

    List<PlantModel> plantInfos;

    public PlantInfoAdapter(List<PlantModel> plantInfos) {
        this.plantInfos = plantInfos;
    }

    @NonNull
    @Override
    public PlantInfoAdapter.PlantInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant_info, parent, false);
        return new PlantInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantInfoAdapter.PlantInfoViewHolder holder, int position) {
        PlantModel plantInfo = plantInfos.get(position);
        // bind data to view holder
        holder.plantName.setText(plantInfo.getName());
        holder.plantLocation.setText(plantInfo.getLocation());
//        holder.lightInfo.setText(plantInfo.getIdealLight());
//        holder.temperatureInfo.setText(plantInfo.getIdealTemperature());
//        holder.moistureInfo.setText(plantInfo.getIdealMoisture());
//        holder.waterInfo.setText(plantInfo.getIdealWater());

        holder.itemView.setOnClickListener(v -> {
            // xử lý sự kiện khi 1 cây được click -> chuyển sang trang chi tiết về cây đó
            Context context = v.getContext();
            Intent intent = new Intent(context, PlantInfoDetail.class);

            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return plantInfos.size();
    }

    public static class PlantInfoViewHolder extends RecyclerView.ViewHolder{

        TextView plantName, plantLocation, lightInfo, temperatureInfo, moistureInfo, waterInfo  ;

        public PlantInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            plantName = itemView.findViewById(R.id.plant_name);
            plantLocation = itemView.findViewById(R.id.plant_location);
            lightInfo = itemView.findViewById(R.id.light_info);
            temperatureInfo = itemView.findViewById(R.id.temperature_info);
            moistureInfo = itemView.findViewById(R.id.moisture_info);
            waterInfo = itemView.findViewById(R.id.water_info);

        }
    }
}
