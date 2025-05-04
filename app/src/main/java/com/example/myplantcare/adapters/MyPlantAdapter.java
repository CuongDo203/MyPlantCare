package com.example.myplantcare.adapters;

import android.content.Intent;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // Import AlertDialog nếu vẫn cần cho xác nhận xóa (tùy UI)
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myplantcare.R;
import com.example.myplantcare.activities.MyPlantDetailActivity;
import com.example.myplantcare.activities.NoteActivity;
import com.example.myplantcare.activities.PlantLogActivity;
// Import các lớp khác nếu cần cho model hoặc utils
import com.example.myplantcare.models.MyPlantModel;
import com.example.myplantcare.utils.DateUtils; // Import DateUtils nếu dùng

import java.util.ArrayList; // Import ArrayList
import java.util.List;

public class MyPlantAdapter extends RecyclerView.Adapter<MyPlantAdapter.MyPlantViewHolder> {

    private static final String TAG = "MyPlantAdapter"; // Thêm TAG

    private List<MyPlantModel> myPlantList = new ArrayList<>(); // Khởi tạo rỗng mặc định
    private OnPlantItemClickListener listener; // Biến lưu listener

    // Interface listener (giữ nguyên hoặc định nghĩa lại tùy nhu cầu chính xác của Fragment)
    public interface OnPlantItemClickListener {
        void onPlantClick(MyPlantModel plant); // Xử lý click vào item
        void onDeleteClick(MyPlantModel plant); // Xử lý click vào nút xóa
        // Thêm các sự kiện click khác nếu cần (ví dụ: click nút Logs, click nút Notes)
        // void onLogsClick(MyPlantModel plant);
        // void onNotesClick(MyPlantModel plant);
    }

    // Constructor mới: nhận danh sách ban đầu và listener
    public MyPlantAdapter(List<MyPlantModel> myPlantList, OnPlantItemClickListener listener) {
        // Gán danh sách. Sử dụng bản sao hoặc danh sách rỗng nếu danh sách đầu vào null
        this.myPlantList = myPlantList != null ? myPlantList : new ArrayList<>();
        this.listener = listener;
        Log.d(TAG, "Adapter initialized with " + this.myPlantList.size() + " plants and listener.");
    }

    // Phương thức để cập nhật dữ liệu cho Adapter (đã có và sử dụng tốt)
    public void setMyPlants(List<MyPlantModel> newPlantList) {
        this.myPlantList = newPlantList != null ? newPlantList : new ArrayList<>();
        notifyDataSetChanged(); // Thông báo cho RecyclerView cập nhật toàn bộ
        Log.d(TAG, "Adapter data updated. New size: " + this.myPlantList.size());
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

        // Bind dữ liệu lên Views trong item
        holder.myPlantName.setText(myPlant.getNickname());
        holder.myPlantLocation.setText(myPlant.getLocation());
        // Kiểm tra null trước khi sử dụng các giá trị double/Timestamp
        holder.myPlantProgress.setText(String.format("%d%%", (int) myPlant.getProgress())); // Ép kiểu int nếu progress là double
        Glide.with(holder.itemView.getContext())
                .load(myPlant.getImage())
                .placeholder(R.drawable.plant_sample)
                .error(R.drawable.ic_photo_error)
                .into(holder.myPlantImage);

        // --- Thiết lập Click Listener cho item View ---
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlantClick(myPlant); // Gọi phương thức onPlantClick của listener
                Log.d(TAG, "Item clicked for plant ID: " + myPlant.getId());
            } else {
                Log.w(TAG, "OnPlantItemClickListener is null. Item click not handled.");
            }
        });

        // --- Thiết lập Click Listener cho nút Xóa ---
        holder.btnDeleteMyPlant.setOnClickListener(v -> {
            if (listener != null) {
                // Thay vì tự xử lý xóa, gọi phương thức onDeleteClick của listener
                listener.onDeleteClick(myPlant);
                Log.d(TAG, "Delete clicked for plant ID: " + myPlant.getId());
            } else {
                Log.w(TAG, "OnPlantItemClickListener is null. Delete click not handled.");
                // Tùy chọn: Vẫn hiển thị AlertDialog xác nhận nếu listener null
                // showDeleteConfirmationDialog(holder.itemView.getContext(), myPlant); // Cần implement hàm này
            }
        });

        // --- Thiết lập Click Listener cho nút Logs ---
        holder.btnMyPlantLogs.setOnClickListener(v -> {
            // Nếu bạn muốn Fragment xử lý, thêm vào listener interface và gọi listener.onLogsClick(myPlant)
            // Nếu Adapter tự mở Activity, giữ nguyên code dưới đây:
            Log.d(TAG, "Logs clicked for plant ID: " + myPlant.getId());
            Intent intent = new Intent(holder.itemView.getContext(), PlantLogActivity.class);
            intent.putExtra("plantName", myPlant.getNickname());
            intent.putExtra("image", myPlant.getImage());
            intent.putExtra("id", myPlant.getId());
            holder.itemView.getContext().startActivity(intent);
        });

        // --- Thiết lập Click Listener cho nút Notes ---
        holder.btnMyPlantNotes.setOnClickListener(v -> {
            // Nếu bạn muốn Fragment xử lý, thêm vào listener interface và gọi listener.onNotesClick(myPlant)
            // Nếu Adapter tự mở Activity, giữ nguyên code dưới đây:
            Log.d(TAG, "Notes clicked for plant ID: " + myPlant.getId());
            Intent intent = new Intent(holder.itemView.getContext(), NoteActivity.class);
            intent.putExtra("plantName", myPlant.getNickname());
            intent.putExtra("image", myPlant.getImage());
            intent.putExtra("id", myPlant.getId()); // Dùng ID của MyPlantModel, không phải Plant template ID
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return myPlantList.size(); // Sử dụng size() trực tiếp, trả về 0 nếu list rỗng/null (do khởi tạo rỗng)
    }

    // --- ViewHolder Class (giữ nguyên) ---
    public static class MyPlantViewHolder extends RecyclerView.ViewHolder {
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