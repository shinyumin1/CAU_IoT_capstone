package com.example.attendence;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;
import android.graphics.PorterDuff;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;


public class SelectSeatActivity extends AppCompatActivity{
    private ImageButton selectedSeat = null;
    private String userId;
    private String takeId;


    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_select_seat);

        TextView tvClassInfo = findViewById(R.id.tv_class_info);

        // SharedPreferences에서 userId 가져오기
        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", null);

        // 이전 화면에서 takeId 전달받기
        takeId = getIntent().getStringExtra("takeId");
        if (takeId == null) {
            Toast.makeText(this, "수강 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        String subject = getIntent().getStringExtra("과목명");
        String professor = getIntent().getStringExtra("교수명");
        String classroom = getIntent().getStringExtra("강의실");
        String schedule = getIntent().getStringExtra("시간");

        String infoText = subject + " " + professor + "\n" + classroom + "\n" + schedule;
        tvClassInfo.setText(infoText);


        int[] seatIds = {
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

        for (int id : seatIds) {
            ImageButton seat = findViewById(id);
            seat.setOnClickListener(v -> handleSeatClick((ImageButton) v));
        }

        // firebase에서 기존 좌석 가져오기
        loadSeatFromFirestore();

        Button doneButton = findViewById(R.id.btn_done_select_seat);
        doneButton.setOnClickListener(v -> {
            if (selectedSeat == null) {
                Toast.makeText(this, "좌석을 선택해주세요!", Toast.LENGTH_SHORT).show();
                return;
            }

            String seatName = getSeatName(selectedSeat);
            saveSeatToFirestore(seatName);

            Intent intent = new Intent(SelectSeatActivity.this, MainActivity.class);
            intent.putExtra("selected_seat_id", selectedSeat != null ? selectedSeat.getId() : -1);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
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

    private String getSeatName(ImageButton seat) {
        String resName = getResources().getResourceEntryName(seat.getId());
        return resName.substring(resName.indexOf("_") + 1); // seat_a1 → a1
    }

    private void saveSeatToFirestore(String seatName) {
        if(userId == null || takeId == null) return;

        String dateId = new SimpleDateFormat("yyMMdd", Locale.KOREAN).format(new Date());

        Log.d("SelectSeat", "userId=" + userId + ", takeId=" + takeId + ", dateId=" + dateId + ", seat=" + seatName);


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 학생 본인 데이터 저장
        HashMap<String, Object> seatData = new HashMap<>();
        seatData.put("seat", seatName);

        db.collection("users")
                .document(userId)
                .collection("takes")
                .document(takeId)
                .collection("date")
                .document(dateId)
                .set(seatData, SetOptions.merge()) // merge: 기존 필드는 유지, seat만 업데이트
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "좌석이 저장되었습니다: " + seatName, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "학생 좌석 저장 실패", Toast.LENGTH_SHORT).show();
                    Log.e("SelectSeat", "Firestore 저장 실패", e);
                });

        // 교수 DB 에도 좌석 동기화
        // 교수 ID, Lecture Id를 takes 문서에서 불러오기
        db.collection("users")
                .document(userId)
                .collection("takes")
                .document(takeId)
                .get()
                .addOnSuccessListener(snapshot->{
                    if (snapshot.exists()){
                        String professorId=snapshot.getString("professorId");
                        String lectureId=snapshot.getString("lectureId");

                        Log.d("SelectSeat", "professorId=" + professorId + ", lectureId=" + lectureId);

                        if (professorId!=null&&lectureId!=null){
                            HashMap<String,Object> professorSeatData=new HashMap<>();
                            professorSeatData.put("studentId",userId);
                            professorSeatData.put("seat",seatName);

                            db.collection("users")
                                    .document(professorId)
                                    .collection("lecture")
                                    .document(lectureId)
                                    .collection("date")
                                    .document(dateId)
                                    .collection("seats")
                                    .document(userId)
                                    .set(professorSeatData,SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> Log.d("SelectSeat", "교수 seat 업데이트 성공"))
                                    .addOnFailureListener(e -> Log.e("SelectSeat", "교수 seat 업데이트 실패", e));
                        }
                    }
                });

    }

    private void loadSeatFromFirestore(){
        if (userId == null || takeId == null) return ;

        String dateId = new SimpleDateFormat("yyMMdd",Locale.KOREAN).format(new Date());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 본인 좌석 가져오기
        db.collection("users")
                .document(userId)
                .collection("takes")
                .document(takeId)
                .collection("date")
                .document(dateId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String mySeatTemp = null;
                    if (documentSnapshot.exists()) {
                        mySeatTemp = documentSnapshot.getString("seat");
                    }

                    final String mySeat = mySeatTemp;

                    if (mySeat != null) {
                        int resId = getResources().getIdentifier("seat_" + mySeat, "id", getPackageName());
                        ImageButton seatBtn = findViewById(resId);
                        if (mySeat != null && mySeat.equals(mySeat)) {
                            seatBtn.setColorFilter(getResources().getColor(R.color.blue, null), PorterDuff.Mode.SRC_IN);
                            selectedSeat = seatBtn;
                        }
                        else {
                            seatBtn.setColorFilter(getResources().getColor(R.color.red, null), PorterDuff.Mode.SRC_IN);
                        }
                    }


                    // 교수 db에서 모든 좌석 가져오기(선택된 좌석은 빨간색으로 표시)
                    String professorId = getIntent().getStringExtra("professorId");
                    String lectureId = getIntent().getStringExtra("lectureId");
                    if (professorId == null || lectureId == null) return;

                    db.collection("users")
                            .document(professorId)
                            .collection("lecture")
                            .document(lectureId)
                            .collection("date")
                            .document(dateId)
                            .collection("seats")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                for (DocumentSnapshot doc : querySnapshot) {
                                    String seatName = doc.getString("seat");
                                    String studentId = doc.getString("studentId");

                                    Log.d("SelectSeat", "좌석 확인: seatName=" + seatName + ", studentId=" + studentId + ", mySeat=" + mySeat);


                                    if (seatName != null) {
                                        int resId = getResources().getIdentifier("seat_" + seatName, "id", getPackageName());
                                        Log.d("SelectSeat", "resId=" + resId);
                                        ImageButton seatBtn = findViewById(resId);
                                        if (seatBtn != null) {
                                            if (mySeat != null && mySeat.equals(seatName)) {
                                                seatBtn.setColorFilter(getResources().getColor(R.color.blue, null),
                                                        PorterDuff.Mode.SRC_IN);
                                                selectedSeat = seatBtn;
                                                Log.d("SelectSeat", "내 좌석: " + seatName + " → 파란색");
                                            } else {
                                                seatBtn.setColorFilter(getResources().getColor(R.color.red, null),
                                                        PorterDuff.Mode.SRC_IN);
                                                Log.d("SelectSeat", "다른 학생 좌석: " + seatName + " → 빨간색");
                                            }
                                        }
                                    }
                                }
                            });
                })
                .addOnFailureListener(e -> Log.e("SelectSeat", "좌석 불러오기 실패", e));
    }
}

