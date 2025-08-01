package com.example.attendence;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;
import android.graphics.PorterDuff;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

public class SelectSeatActivity extends AppCompatActivity{
    private ImageButton selectedSeat = null;

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_select_seat);

        TextView tvClassInfo = findViewById(R.id.tv_class_info);

        String subject = getIntent().getStringExtra("과목명");
        String professor = getIntent().getStringExtra("교수명");
        String classroom = getIntent().getStringExtra("강의실");
        String schedule = getIntent().getStringExtra("시간");

        String infoText = subject + " " + professor + "\n" + classroom + "\n" + schedule;
        tvClassInfo.setText(infoText);


        int[] seatIds = {
                R.id.seat_a1, R.id.seat_a2, R.id.seat_a3,
                R.id.seat_a4, R.id.seat_a5, R.id.seat_a6,
                R.id.seat_b1, R.id.seat_b2, R.id.seat_b3,
                R.id.seat_b4, R.id.seat_b5, R.id.seat_b6,
                R.id.seat_c1, R.id.seat_c2, R.id.seat_c3,
                R.id.seat_c4, R.id.seat_c5, R.id.seat_c6,
                R.id.seat_d1, R.id.seat_d2, R.id.seat_d3,
                R.id.seat_d4, R.id.seat_d5, R.id.seat_d6,
                R.id.seat_e1, R.id.seat_e2, R.id.seat_e3,
                R.id.seat_e4, R.id.seat_e5, R.id.seat_e6,
                R.id.seat_f1, R.id.seat_f2, R.id.seat_f3,
                R.id.seat_f4, R.id.seat_f5, R.id.seat_f6,
        };

        for (int id : seatIds) {
            ImageButton seat = findViewById(id);
            seat.setOnClickListener(v -> handleSeatClick((ImageButton) v));
        }

        Button doneButton = findViewById(R.id.btn_done_select_seat);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 홈 화면(MainActivity)로 이동
                Intent intent = new Intent(SelectSeatActivity.this, MainActivity.class);
                intent.putExtra("selected_seat_id", selectedSeat != null ? selectedSeat.getId() : -1); // 선택된 좌석 id 전달 (선택 안 했으면 -1)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // 기존 MainActivity 재사용
                startActivity(intent);
                finish(); // 현재 액티비티 종료
            }
        });
    }

    private void handleSeatClick(ImageButton clickedSeat) {
        // 이전 선택한 좌석 원래 상태로
        if (selectedSeat != null) {
            selectedSeat.setColorFilter(null); // 색상 초기화
        }

        // 현재 클릭한 좌석을 파란색으로
        clickedSeat.setColorFilter(getResources().getColor(R.color.blue, null), PorterDuff.Mode.SRC_IN);

        selectedSeat = clickedSeat;
    }
}

