package com.example.attendence.ui.Attendence;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import com.example.attendence.R;

import com.example.attendence.TakePost;
import com.example.attendence.TakePostAdapter;
import com.example.attendence.appealP;
import com.example.attendence.appealS;
import com.example.attendence.databinding.FragmentAttendenceBinding;
import com.example.attendence.databinding.FragmentHomeBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;


public class Attendence extends Fragment {

    private FragmentAttendenceBinding binding;
    private RecyclerView recyclerView;
    private TakePostAdapter adapter;
    private List<TakePost> takeList = new ArrayList<>();
    private TakePost currentSelectedPost;
    private String selectedDateId = new SimpleDateFormat("yyMMdd", Locale.KOREAN).format(new Date());

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AttendenceViewModel AttendenceViewModel =
                new ViewModelProvider(this).get(AttendenceViewModel.class);

        binding = FragmentAttendenceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 오늘 날짜 표시
        String today = new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN).format(new Date());
        binding.todayDateTextView.setText(today);
        // 사용자 ID 가져오기
        String userId = getUserIdFromPrefs();

        recyclerView = binding.rvTakePosts;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TakePostAdapter(getContext(), takeList, true, "ATTEND");
        adapter.setUserId(userId);
        adapter.setOnStudentAppealClickListener(post -> {
            binding.studentAppealEditbox.setVisibility(View.VISIBLE);
            binding.studentAppealButton.setVisibility(View.VISIBLE);
            currentSelectedPost = post; // 클릭한 과목정보 저장
        });
        recyclerView.setAdapter(adapter);

        // 오늘 요일 계산
        String todayWeekday = new SimpleDateFormat("E", Locale.KOREAN).format(new Date());



        // 초기 학생 데이터 불러오기
        loadStudentAttendence(userId, selectedDateId, todayWeekday);

        // 달력 버튼
        binding.calendarButton.setVisibility(View.VISIBLE);
        binding.calendarButton.setOnClickListener(v -> {
            final java.util.Calendar calendar = java.util.Calendar.getInstance();
            int year = calendar.get(java.util.Calendar.YEAR);
            int month = calendar.get(java.util.Calendar.MONTH);
            int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                    requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        java.util.Calendar selectedDate = java.util.Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);

                        // 화면에 보이는 날짜
                        String formattedDate = new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN)
                                .format(selectedDate.getTime());
                        binding.todayDateTextView.setText(formattedDate);

                        // Firebase 문서 dateId용
                        selectedDateId = new SimpleDateFormat("yyMMdd",Locale.KOREAN).format(selectedDate.getTime());

                        // EditText와 버튼 초기화
                        binding.etStudentAppeal.setText(""); // 사유 초기화
                        binding.studentAppealEditbox.setVisibility(View.GONE);
                        binding.studentAppealButton.setVisibility(View.GONE);
                        currentSelectedPost = null; // 선택된 과목 초기화

                        // 선택한 날짜 요일 계산
                        String selectedWeekday = new SimpleDateFormat("E", Locale.KOREAN).format(selectedDate.getTime());

                        // 어댑터에 전달
                        adapter.setSelectedDate(selectedDateId);
                        adapter.notifyDataSetChanged();

                        // 선택된 날짜 기준으로 데이터 다시 불러오기
                        loadStudentAttendence(userId, selectedDateId, selectedWeekday);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("student".equals(role)) {
                            binding.attendenceCheckS.setVisibility(View.VISIBLE);
                            binding.attendenceCheckS.setOnClickListener(v -> {
                                // 추후 구현해야 하는 부분.. 현재는 토스트만 띄우게 설정
                                Toast.makeText(getContext(), "출결 이의 신청 클릭됨", Toast.LENGTH_SHORT).show();
                            });

                            binding.studentAppealButton.setOnClickListener(v -> {
                                if(currentSelectedPost == null) {
                                    Toast.makeText(getContext(), "먼저 과목을 선택하세요.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                String appealText = binding.etStudentAppeal.getText().toString().trim();
                                if (appealText.isEmpty()) {
                                    Toast.makeText(getContext(), "사유를 입력해주세요.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                submitStudentAttendence(appealText, selectedDateId);
                                Toast.makeText(getContext(), "출석 이의신청되었습니다.",Toast.LENGTH_SHORT).show();
                            });
                        } else if ("professor".equals(role)) {
                            binding.attendenceCheckS.setVisibility(View.GONE);
                            adapter = new TakePostAdapter(getContext(), takeList, false, "ATTEND");
                            recyclerView.setAdapter(adapter);

                            loadProfessorAttendence(userId,selectedDateId); // 교수용 데이터 불러오기

                            adapter.serOnProfessorClickListener(post -> {
                                String dateId = new SimpleDateFormat("yyMMdd", Locale.KOREAN).format(new Date());
                                db.collection("users")
                                        .document(userId)
                                        .collection("lecture")
                                        .document(post.getId())
                                        .collection("date")
                                        .document(dateId)
                                        .collection("attendence")
                                        .get()
                                        .addOnSuccessListener(querySnapshot -> {
                                            StringBuilder reasons = new StringBuilder();
                                            for (QueryDocumentSnapshot doc : querySnapshot) {
                                                String studentId = doc.getId();
                                                String reason = doc.getString("reason");
                                                if(reason != null && !reason.isEmpty()) {
                                                    reasons.append(studentId).append(": ").append(reason).append("\n");
                                                }
                                            }
                                            if(reasons.length() > 0) {
                                                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                                                        .setTitle(post.getSubject() + " 출결 사유")
                                                        .setMessage(reasons.toString())
                                                        .setPositiveButton("확인", null)
                                                        .show();
                                            } else {
                                                Toast.makeText(getContext(), "사유가 없습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "사유 조회 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            });
                        } else {
                            Log.e("Home", "알 수 없는 역할: " + role);
                        }
                    } else {
                        Log.e("Home", "해당 사용자 문서 없음");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Home", "사용자 role 가져오기 실패: ", e);
                });

        return root;
    }
    private String getUserIdFromPrefs(){
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getString("userId","");
    }

    private void loadStudentAttendence(String userId,String dateId,String weekday) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .collection("takes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    takeList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String subject = doc.getString("과목명");
                        String professor = doc.getString("교수명");
                        String classroom = doc.getString("강의실");
                        String schedule = doc.getString("시간");

                        if (classroom != null && !"null".equals(classroom)
                                && schedule != null && schedule.contains(weekday)) {
                            String profId = (currentSelectedPost != null) ? currentSelectedPost.getProfId() : "";
                            TakePost takePost = new TakePost(subject, professor, classroom, schedule, "", doc.getId(), profId);

                            // 해당 날짜의 출결/사유 불러오기
                            db.collection("users")
                                    .document(userId)
                                    .collection("takes")
                                    .document(doc.getId())
                                    .collection("date")
                                    .document(dateId)
                                    .get()
                                    .addOnSuccessListener(dateDoc -> {
                                        if (dateDoc.exists()) {
                                            String reason = dateDoc.getString("reason");
                                            String status = dateDoc.getString("status"); // 출결 상태
                                            takePost.setAttendenceStandard(status != null ? status : "");
                                        }
                                        adapter.notifyDataSetChanged();
                                    });

                            takeList.add(takePost);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Home", "학생 데이터 불러오기 실패: ", e);
                });
    }
    private void submitStudentAttendence(String appealText, String dateId) {
        // text 필드 추가해서 db 넣기
        if (currentSelectedPost == null) return ;
        String studentId = getUserIdFromPrefs();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(studentId)
                .get()
                .addOnSuccessListener(studentDoc ->{
                    if(studentDoc.exists() && "student".equals(studentDoc.getString("role"))){
                        db.collection("users")
                                .document(studentId)
                                .collection("takes")
                                .document(currentSelectedPost.getId()) //수강 과목 id
                                .collection("date")
                                .document(dateId) // 사유 . 지각한 날짜
                                .set(new HashMap<String, Object>() {{
                                    put("reason", appealText);
                                }}, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "이의신청이 제출되었습니다.", Toast.LENGTH_SHORT).show();

                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "제출 실패", Toast.LENGTH_SHORT).show();
                                });
                        String profId = currentSelectedPost.getProfId();
                        if (profId != null && !profId.isEmpty()) {
                            db.collection("users")
                                    .document(profId)
                                    .collection("lecture")
                                    .document(currentSelectedPost.getId())
                                    .collection("date")
                                    .document(dateId)
                                    .collection("attendance")
                                    .document(studentId)
                                    .set(new HashMap<String, Object>() {{
                                        put("reason", appealText); // 필요시 출결 상태도 같이 저장
                                    }}, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> Log.d("Submit", "Professor DB updated"))
                                    .addOnFailureListener(e -> Log.e("Submit", "Professor DB failed", e));
                        }
                    } else {
                        Toast.makeText(getContext(), "학생 계정이 아닙니다",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "사용자 정보 조회 실패", Toast.LENGTH_SHORT).show();
                });

    }
    private void loadProfessorAttendence(String userId, String dateId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String todayWeekday = new SimpleDateFormat("yyMMdd", Locale.KOREAN).format(new Date());
        db.collection("users")
                .document(userId)
                .collection("lecture")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    takeList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String subject = doc.getString("과목명");
                        String classroom = doc.getString("강의실");
                        String schedule = doc.getString("시간");
                        if (schedule != null && schedule.contains(todayWeekday)) {
                            String profId = (currentSelectedPost != null) ? currentSelectedPost.getProfId() : "";
                            TakePost takePost = new TakePost(subject, " ", classroom, schedule, "", doc.getId(), profId);
                            takeList.add(takePost);
                            //takeList.add(new TakePost(subject, " ", classroom, schedule, "", doc.getId(), profId));
                            db.collection("users")
                                    .document(userId)
                                    .collection("lecture")
                                    .document(doc.getId())
                                    .collection("date")
                                    .document(dateId)
                                    .collection("attendance")
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        HashMap<String, String> studentReasons = new HashMap<>();
                                        for (QueryDocumentSnapshot studentDoc : querySnapshot) {
                                            String studentId = studentDoc.getId();
                                            String reason = studentDoc.getString("reason");
                                            if (reason != null && !reason.isEmpty()) {
                                                studentReasons.put(studentId, reason);
                                            }
                                        }
                                        takePost.setStudentReasons(studentReasons);
                                        adapter.notifyDataSetChanged();
                                    });
                            }

                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Log.e("Home", "교수 데이터 불러오기 실패: ", e));
                }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}