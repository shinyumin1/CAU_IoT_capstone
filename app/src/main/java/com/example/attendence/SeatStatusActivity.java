package com.example.attendence;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SeatStatusActivity extends AppCompatActivity {

    private String professorId;
    private String lectureId;

    private int[] seatIds = {
            R.id.seat_a1, R.id.seat_a2, R.id.seat_a3, R.id.seat_a4, R.id.seat_a5, R.id.seat_a6,
            R.id.seat_b1, R.id.seat_b2, R.id.seat_b3, R.id.seat_b4, R.id.seat_b5, R.id.seat_b6,
            R.id.seat_c1, R.id.seat_c2, R.id.seat_c3, R.id.seat_c4, R.id.seat_c5, R.id.seat_c6,
            R.id.seat_d1, R.id.seat_d2, R.id.seat_d3, R.id.seat_d4, R.id.seat_d5, R.id.seat_d6,
            R.id.seat_e1, R.id.seat_e2, R.id.seat_e3, R.id.seat_e4, R.id.seat_e5, R.id.seat_e6,
            R.id.seat_f1, R.id.seat_f2, R.id.seat_f3, R.id.seat_f4, R.id.seat_f5, R.id.seat_f6,
            R.id.seat_g1, R.id.seat_g2, R.id.seat_g3, R.id.seat_g4, R.id.seat_g5, R.id.seat_g6,
            R.id.seat_h1, R.id.seat_h2, R.id.seat_h3, R.id.seat_h4, R.id.seat_h5, R.id.seat_h6,
            R.id.seat_i1, R.id.seat_i2, R.id.seat_i3, R.id.seat_i4, R.id.seat_i5, R.id.seat_i6
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_status);

        professorId = getIntent().getStringExtra("professorId");
        lectureId = getIntent().getStringExtra("lectureId");

        if (professorId == null){
            Log.e("SeatStatus","professorId가 없음");
            finish();
            return;
        }

        if (lectureId == null){
            Log.e("SeatStatus","lectureId가 없음");
            finish();
            return;
        }

        // 강의 정보
        TextView tvClassInfo = findViewById(R.id.tv_class_info);

        String subject = getIntent().getStringExtra("과목명");
        String classroom = getIntent().getStringExtra("강의실");
        String schedule = getIntent().getStringExtra("시간");

        if (subject != null && classroom != null && schedule != null) {
            String infoText = subject + " " + classroom + "\n" + schedule;
            tvClassInfo.setText(infoText);
        }

        // firebase에서 좌석 현황 불러오기
        loadSeatsForProfessor(professorId, lectureId);
    }

    private void loadSeatsForProfessor(String professorId, String lectureId) {
        String dateId = new SimpleDateFormat("yyMMdd", Locale.KOREAN).format(new Date());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(professorId)
                .collection("lecture")
                .document(lectureId)
                .collection("date")
                .document(dateId)
                .collection("seats")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    resetSeats();

                    for (DocumentSnapshot doc : querySnapshot) {
                        String seatName = doc.getString("seat");
                        if (seatName != null) {
                            int resId = getResources().getIdentifier("seat_" + seatName, "id", getPackageName());
                            if (resId != 0) {
                                ImageButton seatBtn = findViewById(resId);
                                if (seatBtn != null) {
                                    seatBtn.setColorFilter(getResources().getColor(R.color.red, null));
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("SeatStatus", "좌석 불러오기 실패", e));
    }

    private void resetSeats() {
        for (int id : seatIds) {
            ImageButton seat = findViewById(id);
            if (seat != null) seat.setColorFilter(null); // 초기화
        }
    }
}
