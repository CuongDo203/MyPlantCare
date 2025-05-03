package com.example.myplantcare.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
        db.collection("care_instructions")
                .document(plantName)
                .collection("search_fillter")
                .whereEqualTo("city", city)
                .whereEqualTo("season", season)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (!querySnapshots.isEmpty()) {
                        DocumentSnapshot filterDoc = querySnapshots.getDocuments().get(0);
                        String note = filterDoc.getString("note");

                        // Hiển thị ghi chú nếu có
                        if (note != null && !note.isEmpty()) {
                            noteTextView.setText("📝 Lưu ý\n" + note);
                            noteTextView.setVisibility(View.VISIBLE);
                        }

                        // Lấy tham chiếu tiêu đề và hiển thị nếu có lịch trình
                        TextView scheduleTitleTextView = findViewById(R.id.scheduleTitleTextView);
                        scheduleTitleTextView.setVisibility(View.VISIBLE);

                        // Lấy và hiển thị danh sách lịch trình
                        filterDoc.getReference().collection("schedules")
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
                    }
                });
    }

}
