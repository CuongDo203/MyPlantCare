package com.example.myplantcare.data.services;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import android.util.Log;
import android.widget.Toast;

public class FirebaseMessageService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Kiểm tra nếu thông báo có dữ liệu
        if (remoteMessage.getData().size() > 0) {
            String message = remoteMessage.getData().get("message");
            showNotification(message);
        }

        // Kiểm tra nếu thông báo có thông điệp
        if (remoteMessage.getNotification() != null) {
            String body = remoteMessage.getNotification().getBody();
            showNotification(body);
        }
    }

    private void showNotification(String message) {
        // Hiển thị thông báo trên thiết bị
        Log.d("FCM", "Message: " + message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
