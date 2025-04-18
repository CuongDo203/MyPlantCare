package com.example.myplantcare.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myplantcare.R;
import com.example.myplantcare.activities.MyPlantDetailActivity;
import com.example.myplantcare.activities.PlantLogActivity;
import com.example.myplantcare.data.repositories.MyPlantRepositoryImpl;
import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.utils.DateUtils;
import com.example.myplantcare.utils.FirestoreCallback;

import java.util.List;

public class MyPlantAdapter  extends RecyclerView.Adapter<MyPlantAdapter.MyPlantViewHolder> {

    List<MyPlantModel> myPlantList;
    private String userId;
    private MyPlantRepositoryImpl repository;
    private Runnable onDeleteSuccess;

    public MyPlantAdapter(List<MyPlantModel> myPlantList, String userId, MyPlantRepositoryImpl repository, Runnable onDeleteSuccess) {
        this.myPlantList = myPlantList;
        this.userId = userId;
        this.repository = repository;
        this.onDeleteSuccess = onDeleteSuccess;
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
        Glide.with(holder.itemView.getContext()).load(myPlant.getImage()).into(holder.myPlantImage);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), MyPlantDetailActivity.class);
            intent.putExtra("plantName", myPlant.getNickname());
            intent.putExtra("location", myPlant.getLocation());
            intent.putExtra("progress", myPlant.getProgress());
            intent.putExtra("image", myPlant.getImage());
            intent.putExtra("id", myPlant.getPlantId());
            intent.putExtra("my_plant_created", DateUtils.formatTimestamp(myPlant.getCreatedAt(), DateUtils.DATE_FORMAT_DISPLAY));
            intent.putExtra("my_plant_updated", DateUtils.formatTimestamp(myPlant.getUpdatedAt(), DateUtils.DATE_FORMAT_DISPLAY));
            Log.d("MyPlantAdapter", "Ngày tạo: " + DateUtils.formatTimestamp(myPlant.getCreatedAt(), DateUtils.DATE_FORMAT_DISPLAY));
            Log.d("MyPlantAdapter", "Ngày cập nhật: " + DateUtils.formatTimestamp(myPlant.getUpdatedAt(), DateUtils.DATE_FORMAT_DISPLAY));
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
        holder.btnDeleteMyPlant.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Xác nhận xoá")
                    .setMessage("Bạn có chắc chắn muốn xoá cây \"" + myPlant.getNickname() + "\" không?")
                    .setPositiveButton("Xoá", (dialog, which) -> {
                        String plantId = myPlant.getId(); // ID document trong Firestore

                        repository.deleteMyPlant(userId, plantId, new FirestoreCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                myPlantList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, myPlantList.size());
                                if (onDeleteSuccess != null) onDeleteSuccess.run();
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("DeletePlant", "Lỗi khi xoá cây: " + e.getMessage());
                            }
                        });
                    })
                    .setNegativeButton("Huỷ", (dialog, which) -> dialog.dismiss())
                    .show();
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
