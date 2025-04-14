package com.example.myplantcare.activities;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.myplantcare.R;
public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.notification_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        LinearLayout container = findViewById(R.id.notificationContainer);
        View itemView = getLayoutInflater().inflate(R.layout.item_notification, container, false);

        TextView tvMessage = itemView.findViewById(R.id.tvContent);
        TextView tvTime = itemView.findViewById(R.id.tvTime);
        TextView tvCount = itemView.findViewById(R.id.tvNumber);

        tvMessage.setText("Cây xương rồng cần được tưới nước.");
        tvTime.setText("Vừa xong");
        tvCount.setText("1");

        container.addView(itemView);

        Button clear = findViewById(R.id.clear_button_notification);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
    }
    // Gọi hàm này khi bạn muốn mở hộp thoại xác nhận
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc chắn muốn xóa không?");

        // Nút Quay lại
        builder.setNegativeButton("Quay lại", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Đóng dialog
            }
        });

        // Nút Đồng ý
        builder.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Xử lý xóa dữ liệu tại đây
                Toast.makeText(NotificationActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}