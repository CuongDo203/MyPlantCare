package com.example.myplantcare.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantcare.R;
import com.example.myplantcare.activities.PlantInfoDetail;
import com.example.myplantcare.data.responses.PlantResponse;
import com.example.myplantcare.models.PlantModel;

import java.util.List;

public class PlantInfoAdapter extends RecyclerView.Adapter<PlantInfoAdapter.PlantInfoViewHolder>{

    List<PlantResponse> plantInfos;

    public PlantInfoAdapter(List<PlantResponse> plantInfos) {
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
        PlantResponse plantInfo = plantInfos.get(position);
        // bind data to view holder
        holder.plantName.setText(plantInfo.getPlantName());
        holder.speices.setText(plantInfo.getSpeciesName());
        holder.lightInfo.setText(plantInfo.getIdealLight());
//        holder.temperatureInfo.setText(String.format("%s - %s C", String.valueOf(plantInfo.getIdealTemperatureMin()), String.valueOf(plantInfo.getIdealTemperatureMax())));
//        holder.moistureInfo.setText(String.format("%s - %s %%", String.valueOf(plantInfo.getIdealMoistureMin()), String.valueOf(plantInfo.getIdealMoistureMax())));
//        holder.waterInfo.setText(String.format("%s - %s ml", String.valueOf(plantInfo.getIdealWaterMin()), String.valueOf(plantInfo.getIdealWaterMax())));

        holder.itemView.setOnClickListener(v -> {
            // xử lý sự kiện khi 1 cây được click -> chuyển sang trang chi tiết về cây đó
            Context context = v.getContext();
            Intent intent = new Intent(context, PlantInfoDetail.class);
            intent.putExtra("plantId", plantInfo.getPlant().getId());
            intent.putExtra("plantName", plantInfo.getPlantName());
            intent.putExtra("speciesName", plantInfo.getSpeciesName());
            intent.putExtra("idealLight", plantInfo.getIdealLight());
//            intent.putExtra("idealTemperatureMin", plantInfo.getIdealTemperatureMin());
//            intent.putExtra("idealTemperatureMax", plantInfo.getIdealTemperatureMax());
//            intent.putExtra("idealMoistureMin", plantInfo.getIdealMoistureMin());
//            intent.putExtra("idealMoistureMax", plantInfo.getIdealMoistureMax());
//            intent.putExtra("idealWaterMin", plantInfo.getIdealWaterMin());
//            intent.putExtra("idealWaterMax", plantInfo.getIdealWaterMax());

            Log.d("PlantInfoAdapter", "plantId: " + plantInfo.getPlant().getId());

            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return plantInfos.size();
    }

    public static class PlantInfoViewHolder extends RecyclerView.ViewHolder{

        TextView plantName, speices, lightInfo, temperatureInfo, moistureInfo, waterInfo  ;

        public PlantInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            plantName = itemView.findViewById(R.id.plant_name);
            speices = itemView.findViewById(R.id.species);
            lightInfo = itemView.findViewById(R.id.light_info);
            temperatureInfo = itemView.findViewById(R.id.temperature_info);
            moistureInfo = itemView.findViewById(R.id.moisture_info);
            waterInfo = itemView.findViewById(R.id.water_info);

        }
    }
}
