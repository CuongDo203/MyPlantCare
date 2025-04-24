package com.example.myplantcare.receivers;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.myplantcare.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

//public class NotificationReceiver extends BroadcastReceiver {
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        Log.d("RECEIVER", "onReceive() được gọi!");
//        // Tạo thông báo với cùng ID kênh đã tạo trong MainActivity
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "PLANTAPP_CHANNEL")  // Đảm bảo ID kênh giống nhau
//                .setSmallIcon(R.drawable.ic_launcher_foreground)
//                .setContentTitle("Thông báo My Application")
//                .setContentText("Đừng quên tưới cây hôm nay nhé!")
//                .setPriority(NotificationCompat.PRIORITY_HIGH);
//
//        // Kiểm tra quyền POST_NOTIFICATIONS (Android 13 trở lên)
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
//                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
//                {
//
//            // Gửi thông báo nếu quyền đã được cấp
//            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
//            manager.notify(1, builder.build());
//            Log.d("RECEIVER", "Thông báo đã được gửi!");
//        } else {
//            Log.d("RECEIVER", "Chưa cấp quyền POST_NOTIFICATIONS.");
//        }
//    }
//}
public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NOTIIIIIFICATION", "HELLO");
        // Lấy thông tin từ Intent
        String plantName = intent.getStringExtra("plantName");  // Tên cây
        String taskName = intent.getStringExtra("task");        // Tên công việc

        // Kiểm tra nếu giá trị không null
        if (plantName != null && taskName != null) {
            // Tạo nội dung thông báo
            String notificationText = "Cây " + plantName + " cần làm công việc: " + taskName;

            // Tạo thông báo với kênh đã tạo trong MainActivity
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "PLANTAPP_CHANNEL")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Thông báo PlantCare Application")
                    .setContentText(notificationText)  // Sử dụng nội dung đã tạo
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            // Kiểm tra quyền POST_NOTIFICATIONS
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Nếu là Android 13 trở lên, kiểm tra quyền thông báo
                if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("RECEIVER", "Chưa cấp quyền POST_NOTIFICATIONS.");
                    return;  // Dừng lại nếu quyền chưa được cấp
                }
            }

            // Gửi thông báo nếu quyền đã được cấp (hoặc nếu Android < 13)
            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.notify(1, builder.build());
            Log.d("RECEIVER", "Thông báo đã được gửi!");
            // Lưu thông tin vào Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Tạo dữ liệu thông báo để lưu vào Firestore
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("title", "Thông báo PlantCare");
            notificationData.put("content",plantName + "cân làm công việc " + taskName);
            notificationData.put("number", 1);  // Tạo timestamp tự động

            // Lưu dữ liệu vào Firestore trong collection "notifications"
            db.collection("Notification")
                    .add(notificationData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "Lưu thông báo thành công");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Lỗi lưu thông báo", e);
                    });
        } else {
            Log.e("NotificationReceiver", "Thông tin cây hoặc công việc không hợp lệ.");
        }
    }
}

