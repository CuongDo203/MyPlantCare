package com.example.myplantcare.activities;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myplantcare.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
public class InstructionDetailActivity extends AppCompatActivity {
    private LinearLayout scheduleContainer;
    private TextView noteTextView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction_detail);

        scheduleContainer = findViewById(R.id.scheduleContainer);
        noteTextView = findViewById(R.id.noteTextView);
        db = FirebaseFirestore.getInstance();

        String plantName = getIntent().getStringExtra("plantName");
        String city = getIntent().getStringExtra("city");
        String season = getIntent().getStringExtra("season");

        loadInstructionsFromFirestore(plantName, city, season);
        ImageView btn_back_instruction_detail = findViewById(R.id.arrow_back_instruction_detail);
        btn_back_instruction_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InstructionDetailActivity.this, CareInstructionActivity.class);
                intent.putExtra("plantName", plantName);
                intent.putExtra("city", city);
                intent.putExtra("season", season);
                startActivity(intent);
            }
        });
    }

    private void loadInstructionsFromFirestore(String plantName, String city, String season) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 🔹 1. Lấy note từ document chính
        db.collection("care_instructions")
                .document(plantName)
                .get()
                .addOnSuccessListener(plantDoc -> {
                    String note = plantDoc.getString("note");
                    if (note != null && !note.isEmpty()) {
                        noteTextView.setText("📝 Lưu ý\n" + note);
                        noteTextView.setVisibility(View.VISIBLE);
                    } else {
                        Log.w("Firestore", "Không có trường 'note' trong document chính.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi lấy note từ document chính: " + e.getMessage());
                });

        // 🔹 2. Truy vấn search_fillter để lấy danh sách lịch trình
        db.collection("care_instructions")
                .document(plantName)
                .collection("search_fillter")
                .whereEqualTo("city", city)
                .whereEqualTo("season", season)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (!querySnapshots.isEmpty()) {
                        DocumentSnapshot matchedDoc = querySnapshots.getDocuments().get(0);

                        // 🟢 Thêm tiêu đề lịch trình nếu có
                        TextView scheduleTitleTextView = findViewById(R.id.scheduleTitleTextView);
                        scheduleTitleTextView.setVisibility(View.VISIBLE);

                        // 🟢 Truy vấn các schedules
                        matchedDoc.getReference().collection("schedules")
                                .get()
                                .addOnSuccessListener(scheduleSnapshots -> {
                                    for (DocumentSnapshot doc : scheduleSnapshots) {
                                        String task = doc.getString("taskName");
                                        Long freq = doc.getLong("frequency");

                                        if (task != null && freq != null) {
                                            TextView tv = new TextView(this);
                                            tv.setText("• " + task + " (mỗi " + freq + " ngày)");
                                            tv.setTextSize(16f);
                                            tv.setTextColor(Color.parseColor("#00425A"));
                                            tv.setPadding(0, 8, 0, 8);
                                            scheduleContainer.addView(tv);
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Không tìm thấy lịch trình phù hợp", Toast.LENGTH_SHORT).show();
                        Log.w("Firestore", "Không có document nào thỏa mãn city & season.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi lấy lịch trình: " + e.getMessage());
                });
    }




}
