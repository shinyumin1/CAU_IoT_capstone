package com.example.attendence.ui.Attendence;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.example.attendence.TakePost;
import com.example.attendence.TakePostAdapter;
import com.example.attendence.databinding.FragmentAttendenceBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
//해당페이지에서 블루투스 통신으로 데이터 연결
public class Attendence extends Fragment {

    private FragmentAttendenceBinding binding;
    private RecyclerView recyclerView;
    private TakePostAdapter adapter;
    private List<TakePost> takeList = new ArrayList<>();
    private TakePost currentSelectedPost;
    private String selectedDateId = new SimpleDateFormat("yyMMdd", Locale.KOREAN).format(new Date());
    private String userId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAttendenceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 오늘 날짜 표시
        String today = new SimpleDateFormat("yyyy-MM-dd(E)", Locale.KOREAN).format(new Date());
        binding.todayDateTextView.setText(today);

        // 사용자 ID 가져오기
        userId = getUserIdFromPrefs();

        recyclerView = binding.rvTakePosts;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TakePostAdapter(getContext(), takeList, false, "ATTEND");
        adapter.setUserId(userId);
        recyclerView.setAdapter(adapter);


        // 달력 버튼
        binding.calendarButton.setVisibility(View.VISIBLE);
        binding.calendarButton.setOnClickListener(v -> showDatePicker());
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 사용자 role 확인
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("student".equals(role)) {
                            binding.standardAttendence.setVisibility(View.GONE);
                            binding.studentStatusButton.setVisibility(View.GONE);
                            setupStudentUI();
                        } else if ("professor".equals(role)) {
                            if (binding != null) {
                                binding.standardAttendence.setVisibility(View.VISIBLE);
                                binding.studentStatusButton.setVisibility(View.VISIBLE);
                            }
                            setupProfessorUI();
                        } else {
                            Log.e("Attendence", "알 수 없는 역할: " + role);
                        }
                    } else {
                        Log.e("Attendence", "해당 사용자 문서 없음");
                    }
                })
                .addOnFailureListener(e -> Log.e("Attendence", "사용자 role 가져오기 실패: ", e));

        return root;
    }


    private void setupStudentUI() {
        adapter = new TakePostAdapter(getContext(), takeList, true, "ATTEND");
        recyclerView.setAdapter(adapter);
        binding.attendenceCheckS.setVisibility(View.VISIBLE);
        binding.attendenceCheckS.setOnClickListener(v ->
                Toast.makeText(getContext(), "출결 이의 신청 클릭됨", Toast.LENGTH_SHORT).show()
        );
        adapter.setOnStudentAppealClickListener(post -> {
            binding.studentAppealEditbox.setVisibility(View.VISIBLE);
            binding.studentAppealButton.setVisibility(View.VISIBLE);
            currentSelectedPost = post;
        });
        binding.studentAppealButton.setOnClickListener(v -> {
            if (currentSelectedPost == null) {
                Toast.makeText(getContext(), "먼저 과목을 선택하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            String appealText = binding.etStudentAppeal.getText().toString().trim();
            if (appealText.isEmpty()) {
                Toast.makeText(getContext(), "사유를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            submitStudentAttendence(appealText, selectedDateId);
        });

        // 학생 데이터 불러오기
        loadStudentAttendence(userId, selectedDateId);
    }

    private void setupProfessorUI() {
        binding.attendenceCheckS.setVisibility(View.GONE);
        adapter = new TakePostAdapter(getContext(), takeList, false, "ATTEND");
        recyclerView.setAdapter(adapter);
        binding.studentStatusButton.setOnClickListener(v -> markAttendancePresent());

        adapter.setOnProfessorStudentClickListener(post -> {
            currentSelectedPost = post;
            Toast.makeText(getContext(), post.getStudentId() + " 학생 선택됨", Toast.LENGTH_SHORT).show();
        });

        // 교수용 데이터
        loadProfessorAttendence(userId, selectedDateId);

        // 학생 출결 사유 보기 클릭 리스너
        adapter.serOnProfessorClickListener(post -> {
            currentSelectedPost = post;
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .document(userId)
                    .collection("lecture")
                    .document(post.getId())
                    .collection("date")
                    .document(selectedDateId)
                    .collection("attendance")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        StringBuilder reasons = new StringBuilder();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String studentId = doc.getId();
                            String reason = doc.getString("reason");
                            if (reason != null && !reason.isEmpty()) {
                                reasons.append(studentId).append(": ").append(reason).append("\n");
                            }
                        }
                        if (reasons.length() > 0) {
                            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                                    .setTitle(post.getSubject() + " 출결 사유")
                                    .setMessage(reasons.toString())
                                    .setPositiveButton("확인", null)
                                    .show();
                        } else {
                            Toast.makeText(getContext(), "사유가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "사유 조회 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

    }

    private void showDatePicker() {
        final java.util.Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH);
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    java.util.Calendar selectedDate = java.util.Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd(E)", Locale.KOREAN)
                            .format(selectedDate.getTime());
                    binding.todayDateTextView.setText(formattedDate);

                    selectedDateId = new SimpleDateFormat("yyMMdd", Locale.KOREAN).format(selectedDate.getTime());

                    binding.etStudentAppeal.setText("");
                    //binding.studentAppealEditbox.setVisibility(View.GONE);
                    //binding.studentAppealButton.setVisibility(View.GONE);
                    currentSelectedPost = null;

                    adapter.setSelectedDate(selectedDateId);
                    adapter.notifyDataSetChanged();

                    // 다시 불러오기
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users").document(userId).get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists() && "student".equals(doc.getString("role"))) {
                                    loadStudentAttendence(userId, selectedDateId);
                                } else if (doc.exists() && "professor".equals(doc.getString("role"))) {
                                    loadProfessorAttendence(userId, selectedDateId);
                                }
                            });
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private String getUserIdFromPrefs() {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getString("userId", "");
    }

    private void loadStudentAttendence(String userId, String dateId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 선택한 날짜 기준으로 요일 계산
        Date selectedDate;
        try {
            selectedDate = new SimpleDateFormat("yyMMdd", Locale.KOREAN).parse(dateId);
        } catch (Exception e) {
            selectedDate = new Date();
        }
        String selectedWeekday = new SimpleDateFormat("E", Locale.KOREAN).format(selectedDate); // "일", "월", "화" ...

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

                        if (schedule == null) continue;

                        // schedule에서 요일만 추출
                        String scheduleDay = schedule.split("\\(")[0].trim();

                        // 선택한 날짜 요일과 매칭되는 경우만 처리
                        if (!scheduleDay.equals(selectedWeekday)) continue;

                        TakePost takePost = new TakePost(subject, professor, classroom, schedule, "", doc.getId(), doc.getString("professorId"));
                        takeList.add(takePost);
                        int position = takeList.size() - 1;

                        // Firestore에서 학생 출결 상태 가져오기
                        db.collection("users")
                                .document(userId)
                                .collection("takes")
                                .document(doc.getId())
                                .collection("date")
                                .document(dateId)
                                .get()
                                .addOnSuccessListener(dateDoc -> {
                                    String status = "미기록";
                                    if (dateDoc.exists()) {
                                        String dbStatus = dateDoc.getString("status");
                                        if (dbStatus != null && !dbStatus.isEmpty()) {
                                            status = dbStatus;
                                        }
                                    }
                                    takePost.setStudentAttendenceStatus(status);
                                    takePost.setCurrentTime(new SimpleDateFormat("HH:mm", Locale.KOREAN).format(new Date()));
                                    adapter.notifyItemChanged(position);
                                })
                                .addOnFailureListener(e -> {
                                    takePost.setStudentAttendenceStatus("불러오기 실패");
                                    takePost.setCurrentTime(new SimpleDateFormat("HH:mm", Locale.KOREAN).format(new Date()));
                                    adapter.notifyItemChanged(position);
                                });
                    }

                    adapter.notifyDataSetChanged(); // 전체 갱신
                })
                .addOnFailureListener(e -> Log.e("Attendence", "학생 데이터 불러오기 실패: ", e));
    }


    private void submitStudentAttendence(String appealText, String dateId) {
        if (currentSelectedPost == null) return;
        String studentId = getUserIdFromPrefs();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(studentId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) {
                        Toast.makeText(getContext(), "사용자 정보 없음", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String userName = userDoc.getString("userName");

                    db.collection("users")
                            .document(studentId)
                            .collection("takes")
                            .document(currentSelectedPost.getId())
                            .collection("date")
                            .document(dateId)
                            .get()
                            .addOnSuccessListener(dateDoc -> {
                                final String finalStudentStatus;
                                if (dateDoc.exists()) {
                                    String dbStatus = dateDoc.getString("status");
                                    finalStudentStatus = (dbStatus != null && !dbStatus.isEmpty()) ? dbStatus : "미기록";
                                } else {
                                    finalStudentStatus = "미기록";
                                }

                                // 학생 DB 업데이트
                                HashMap<String, Object> studentData = new HashMap<>();
                                studentData.put("name", userName);
                                studentData.put("reason", appealText);
                                studentData.put("status", finalStudentStatus);

                                db.collection("users")
                                        .document(studentId)
                                        .collection("takes")
                                        .document(currentSelectedPost.getId())
                                        .collection("date")
                                        .document(dateId)
                                        .set(studentData, SetOptions.merge())
                                        .addOnSuccessListener(aVoid ->
                                                Toast.makeText(getContext(), "이의신청이 제출되었습니다.", Toast.LENGTH_SHORT).show()
                                        )
                                        .addOnFailureListener(e ->
                                                Toast.makeText(getContext(), "제출 실패", Toast.LENGTH_SHORT).show()
                                        );
                                // 교수 DB
                                String profId = currentSelectedPost.getProfId();
                                if (profId != null && !profId.isEmpty()) {
                                    HashMap<String, Object> profData = new HashMap<>();
                                    profData.put("name", userName);
                                    profData.put("reason", appealText);
                                    profData.put("status", finalStudentStatus);

                                    db.collection("users")
                                            .document(profId)
                                            .collection("lecture")
                                            .document(currentSelectedPost.getId())
                                            .collection("date")
                                            .document(dateId)
                                            .collection("attendance")
                                            .document(studentId)
                                            .set(profData, SetOptions.merge())
                                            .addOnSuccessListener(aVoid -> Log.d("Submit", "Professor DB updated"))
                                            .addOnFailureListener(e -> Log.e("Submit", "Professor DB failed", e));
                                }

                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "학생 상태 가져오기 실패", Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "사용자 정보 가져오기 실패", Toast.LENGTH_SHORT).show()
                );
    }

    private void loadProfessorAttendence(String userId, String dateId) {
        String todayWeekday = new SimpleDateFormat("E", Locale.KOREAN).format(new Date());
        FirebaseFirestore db = FirebaseFirestore.getInstance();

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
                            String profId = doc.getString("professorId");
                            TakePost takePost = new TakePost(subject, " ", classroom, schedule, "", doc.getId(), profId);
                            takeList.add(takePost);

                            db.collection("users")
                                    .document(userId)
                                    .collection("lecture")
                                    .document(doc.getId())
                                    .collection("date")
                                    .document(dateId)
                                    .collection("attendance")
                                    .get()
                                    .addOnSuccessListener(studentSnapshots -> {
                                        for (QueryDocumentSnapshot studentDoc : studentSnapshots) {
                                            String studentId = studentDoc.getId();
                                            String reason = studentDoc.getString("reason");
                                            if (reason != null && !reason.isEmpty()) {
                                                TakePost reasonPost = new TakePost(
                                                        subject,
                                                        " ",  // 필요 시 교수 이름 넣기
                                                        classroom,
                                                        schedule,
                                                        "", // 필요 시 attendenceStandard 추가
                                                        doc.getId(),
                                                        profId
                                                );
                                                reasonPost.setStudentId(studentId);
                                                reasonPost.setStudentReasons(reason);
                                                takeList.add(reasonPost);
                                            }
                                        }
                                        adapter.notifyDataSetChanged();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Attendence", "교수 데이터 불러오기 실패: ", e));
    }

    private void markAttendancePresent() {
        if (currentSelectedPost == null) {
            Toast.makeText(getContext(), "먼저 학생을 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String studentId = currentSelectedPost.getStudentId(); // 학생 ID
        String takeId = currentSelectedPost.getId(); // 과목 ID
        String dateId = selectedDateId; // 선택 날짜
        String profId = currentSelectedPost.getProfId(); // 교수 ID

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        HashMap<String, Object> studentData = new HashMap<>();
        studentData.put("status", "출석");

        db.collection("users")
                .document(profId)
                .collection("lecture")
                .document(takeId)
                .collection("date")
                .document(dateId)
                .set(studentData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // 교수 DB 업데이트
                    if (profId != null && !profId.isEmpty()) {
                        HashMap<String, Object> profData = new HashMap<>();
                        profData.put("status", "출석");

                        db.collection("users")
                                .document(profId)
                                .collection("lecture")
                                .document(takeId)
                                .collection("date")
                                .document(dateId)
                                .collection("attendance")
                                .document(studentId)
                                .set(profData, SetOptions.merge())
                                .addOnSuccessListener(aVoid1 -> {
                                    currentSelectedPost.setStudentAttendenceStatus("출석");
                                    int position = takeList.indexOf(currentSelectedPost);
                                    if (position != -1) {
                                        adapter.notifyItemChanged(position);
                                    }
                                    Toast.makeText(getContext(), "출결 상태가 '출석'으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "교수 DB 업데이트 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    }

                    // 학생 DB 반영
                    db.collection("users")
                            .document(studentId)
                            .collection("takes")
                            .document(takeId)
                            .collection("date")
                            .document(dateId)
                            .update("status", "출석")
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d("Firestore", "status가 '출석'으로 변경됨");
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Firestore", "status 변경 실패", e);
                            });


                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "학생 DB 업데이트 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
        }


        @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
