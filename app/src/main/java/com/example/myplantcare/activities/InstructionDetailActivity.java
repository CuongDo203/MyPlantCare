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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
public class InstructionDetailActivity extends AppCompatActivity {
    private LinearLayout scheduleContainer;
    private TextView noteTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction_detail);

        scheduleContainer = findViewById(R.id.scheduleContainer);
        noteTextView = findViewById(R.id.noteTextView);

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
        boolean[] matchedFound = {false};

        db.collection("care_instructions")
                .get()
                .addOnSuccessListener(careDocs -> {
                    if (careDocs.isEmpty()) {
                        Toast.makeText(this, "Không có dữ liệu hướng dẫn.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentSnapshot doc : careDocs) {
                        String plantId = doc.getString("plantId");
                        String cityId = doc.getString("cityId");
                        String seasonId = doc.getString("seasonId");

                        if (plantId == null || cityId == null || seasonId == null) continue;

                        Task<DocumentSnapshot> plantTask = db.collection("plants").document(plantId).get();
                        Task<DocumentSnapshot> cityTask = db.collection("cities").document(cityId).get();
                        Task<DocumentSnapshot> seasonTask = db.collection("seasons").document(seasonId).get();

                        Tasks.whenAllSuccess(plantTask, cityTask, seasonTask)
                                .addOnSuccessListener(results -> {
                                    DocumentSnapshot plantDoc = (DocumentSnapshot) results.get(0);
                                    DocumentSnapshot cityDoc = (DocumentSnapshot) results.get(1);
                                    DocumentSnapshot seasonDoc = (DocumentSnapshot) results.get(2);

                                    String plantNameFromDb = plantDoc.getString("name");
                                    String cityNameFromDb = cityDoc.getString("name");
                                    String seasonNameFromDb = seasonDoc.getString("name");

                                    if (plantName.equals(plantNameFromDb)
                                            && city.equals(cityNameFromDb)
                                            && season.equals(seasonNameFromDb))
                                    {

                                        matchedFound[0] = true;

                                        String instruction = doc.getString("instruction");
                                        if (instruction != null && !instruction.isEmpty()) {
                                            noteTextView.setText("📝 Lưu ý\n" + instruction);
                                            noteTextView.setVisibility(View.VISIBLE);
                                        }

                                        TextView scheduleTitleTextView = findViewById(R.id.scheduleTitleTextView);
                                        scheduleTitleTextView.setVisibility(View.VISIBLE);

                                        doc.getReference().collection("schedules")
                                                .get()
                                                .addOnSuccessListener(scheduleSnapshots -> {
                                                    for (DocumentSnapshot scheduleDoc : scheduleSnapshots) {
                                                        Long freq = scheduleDoc.getLong("frequency");
                                                        String taskId = scheduleDoc.getString("taskId") ;

                                                        if (taskId == null || freq == null) continue;

                                                        db.collection("tasks").document(taskId)
                                                                .get()
                                                                .addOnSuccessListener(taskDoc -> {
                                                                    String taskName = taskDoc.getString("name");
                                                                    if (taskName != null) {
                                                                        TextView tv = new TextView(this);
                                                                        tv.setText("• " + taskName + " (mỗi " + freq + " ngày)");
                                                                        tv.setTextSize(16f);
                                                                        tv.setTextColor(Color.parseColor("#00425A"));
                                                                        tv.setPadding(0, 8, 0, 8);
                                                                        scheduleContainer.addView(tv);
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                })
                                .addOnCompleteListener(task -> {
//                                    // Nếu sau khi tất cả complete mà chưa tìm thấy bản ghi phù hợp
//                                    if (!matchedFound[0]) {
//                                        Toast.makeText(this, "Không tìm thấy hướng dẫn phù hợp.", Toast.LENGTH_SHORT).show();
//                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi lấy dữ liệu hướng dẫn: " + e.getMessage());
                    Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }


}

