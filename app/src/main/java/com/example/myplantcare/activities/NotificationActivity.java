package com.example.myplantcare.activities;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myplantcare.R;
import com.example.myplantcare.adapters.NotificationAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.myplantcare.models.Notification;
import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.notificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();
        adapter = new NotificationAdapter(new ArrayList<>(), notification -> {

            Toast.makeText(NotificationActivity.this, "Clicked: " + notification.getContent(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);


        Button clear = findViewById(R.id.clear_button_notification);
        clear.setOnClickListener(v -> showDeleteConfirmationDialog());

        ImageView image = findViewById(R.id.arrow_back_notification);
        image.setOnClickListener(v -> {
            Intent i = new Intent(NotificationActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        });
        fetchNotificationsByUserId(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc chắn muốn xóa tất cả thông báo?");
        builder.setNegativeButton("Quay lại", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("Đồng ý", (dialog, which) -> {
            deleteAllNotifications(FirebaseAuth.getInstance().getCurrentUser().getUid());
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void deleteAllNotifications(String userId) {
        db.collection("users") // Collection cha "users"
                .document(userId) // Lấy document của người dùng, dùng userId làm ID
                .collection("notifications") // Subcollection "notifications"
                .get() // Lấy tất cả các document trong subcollection "notifications"
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {

                            // Duyệt qua tất cả các document và xóa từng document theo ID
                            for (int i = 0; i < querySnapshot.size(); i++) {
                                String documentId = querySnapshot.getDocuments().get(i).getId();


                                db.collection("Notification").document(documentId)
                                        .delete()
                                        .addOnCompleteListener(deleteTask -> {
                                            if (deleteTask.isSuccessful()) {
                                                Log.d("NotificationActivity", "Document ID " + documentId + " xóa thành công");
                                            } else {
                                                Log.e("NotificationActivity", "lỗi xóa thông báo" + documentId, deleteTask.getException());
                                            }
                                        });
                            }

                            // xóa toàn bộ thông báo
                            adapter.clearNotifications();
                            Toast.makeText(NotificationActivity.this, "Đã xóa tất cả thông báo.", Toast.LENGTH_SHORT).show();


                        } else {

                            Toast.makeText(NotificationActivity.this, "Không có thông báo để xóa.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("NotificationActivity", "Lỗi get thông báo", task.getException());
                        Toast.makeText(NotificationActivity.this, "Lỗi khi xóa thông báo.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

private void fetchNotificationsByUserId(String userId) {
    // Truy vấn đến subcollection "notifications" của user với userId
    db.collection("users") // Collection cha "users"
            .document(userId) // Lấy document của người dùng, dùng userId làm ID
            .collection("notifications") // Subcollection "notifications"
            .get() // Lấy tất cả các document trong subcollection "notifications"
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        // Duyệt qua tất cả các document trong subcollection "notifications"
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String title = document.getString("title");
                            String content = document.getString("content");
                            String timestamp = document.getString("timestamp");

                            // In thông tin ra Log và hiển thị thông báo
                            Log.d("NotificationData", "Title: " + title);
                            Log.d("NotificationData", "Content: " + content);
                            Log.d("NotificationData", "Timestamp: " + timestamp);
                            displayNotification(title, content, timestamp);  // Hiển thị thông báo lên UI
                        }
                    } else {
                        Log.d("TAG", "No notifications found.");
                        Toast.makeText(NotificationActivity.this, "Không có thông báo.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("TAG", "Error getting notifications.");
                    Toast.makeText(NotificationActivity.this, "Lỗi khi lấy thông báo.", Toast.LENGTH_SHORT).show();
                }
            });
}
    private void displayNotification(String message, String time, String count) {
        Notification notification = new Notification(message, time, count);
        adapter.addNotification(notification);
    }
}
