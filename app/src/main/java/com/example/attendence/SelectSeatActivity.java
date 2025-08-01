package com.example.attendence;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
public class SelectSeatActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_select_seat);

        String subject = getIntent().getStringExtra("과목명");
        String professor = getIntent().getStringExtra("교수명");
        String classroom = getIntent().getStringExtra("강의실");
        String schedule = getIntent().getStringExtra("시간");

    }
}
