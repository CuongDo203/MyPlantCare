//package com.example.myplantcare.adapters;
//
//import android.annotation.SuppressLint;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.myplantcare.models.MyPlantModel;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import com.example.myplantcare.R;
//
//public class MyPlantAdapterForSelectDialog extends RecyclerView.Adapter<MyPlantAdapterForSelectDialog.MyViewHolder> {
//    private final List<MyPlantModel> plantList;
//    private int selectedPosition = -1;
//    private OnPlantSelectedListener listener;
//
//    public interface OnPlantSelectedListener {
//        void onPlantSelected(MyPlantModel plant);
//    }
//
//    public MyPlantAdapterForSelectDialog(List<MyPlantModel> plantList, OnPlantSelectedListener listener) {
//        this.plantList = plantList;
//        this.listener = listener;
//    }
//
//    @NonNull
//    @Override
//    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_plant_select_for_note, parent, false);
//        return new MyViewHolder(view);
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    @Override
//    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
//        MyPlantModel plant = plantList.get(position);
//        holder.tvPlantName.setText(plant.getNickname());
//
//        // Kiểm tra xem item có được chọn không
//        boolean isSelected = selectedPosition == position;
//        holder.layoutPlantItem.setBackgroundResource(isSelected ? R.drawable.bg_gradient : R.drawable.bg_plant_normal);
//
//        // Cập nhật trạng thái khi người dùng chọn
//        holder.itemView.setOnClickListener(v -> {
//            // Nếu item đã được chọn rồi thì bỏ chọn
//            if (selectedPosition == position) {
//                selectedPosition = -1; // Deselect nếu đã chọn lại
//            } else {
//                selectedPosition = position; // Chọn mới
//            }
//            notifyDataSetChanged(); // Cập nhật lại danh sách
//            listener.onPlantSelected(plant); // Gọi listener khi chọn cây
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return plantList.size();
//    }
//
//    static class MyViewHolder extends RecyclerView.ViewHolder {
//        TextView tvPlantName;
//        View layoutPlantItem;
//
//        public MyViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvPlantName = itemView.findViewById(R.id.tv_plant_name);
//            layoutPlantItem = itemView.findViewById(R.id.layoutPlantItemForNote); // lấy LinearLayout
//        }
//    }
//}
//

package com.example.myplantcare.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantcare.models.MyPlantModel;

import java.util.ArrayList;
import java.util.List;

import com.example.myplantcare.R;

public class MyPlantAdapterForSelectDialog extends RecyclerView.Adapter<MyPlantAdapterForSelectDialog.MyViewHolder> {
    private final List<MyPlantModel> fullList; // danh sách đầy đủ
    private List<MyPlantModel> plantList;
    private int selectedPosition = -1;
    private OnPlantSelectedListener listener;

    public interface OnPlantSelectedListener {
        void onPlantSelected(MyPlantModel plant);
    }

    public MyPlantAdapterForSelectDialog(List<MyPlantModel> plantList, OnPlantSelectedListener listener) {
        this.fullList = new ArrayList<>(plantList);
        this.plantList = new ArrayList<>(plantList);
        this.listener = listener;
    }

    public void filter(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            plantList = new ArrayList<>(fullList);
        } else {
            String lower = keyword.toLowerCase();
            plantList = new ArrayList<>();
            for (MyPlantModel plant : fullList) {
                if (plant.getNickname().toLowerCase().contains(lower)) {
                    plantList.add(plant);
                }
            }
        }
        notifyDataSetChanged();
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
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MyPlantModel plant = plantList.get(position);
        holder.tvPlantName.setText(plant.getNickname());

        // Kiểm tra xem item có được chọn không
        boolean isSelected = selectedPosition == position;
        holder.layoutPlantItem.setBackgroundResource(isSelected ? R.drawable.bg_gradient : R.drawable.bg_plant_normal);

        // Cập nhật trạng thái khi người dùng chọn
        holder.itemView.setOnClickListener(v -> {
            // Nếu item đã được chọn rồi thì bỏ chọn
            if (selectedPosition == position) {
                selectedPosition = -1; // Deselect nếu đã chọn lại
            } else {
                selectedPosition = position; // Chọn mới
            }
            notifyDataSetChanged(); // Cập nhật lại danh sách
            listener.onPlantSelected(plant); // Gọi listener khi chọn cây
        });
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlantName;
        View layoutPlantItem;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlantName = itemView.findViewById(R.id.tv_plant_name);
            layoutPlantItem = itemView.findViewById(R.id.layoutPlantItemForNote); // lấy LinearLayout
        }
    }

    public void updateList(List<MyPlantModel> newList) {
        this.fullList.clear();
        this.fullList.addAll(newList);
        this.plantList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

}


