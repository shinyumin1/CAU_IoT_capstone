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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.example.attendence.R;
import com.example.attendence.TakePost;
import com.example.attendence.TakePostAdapter;
import com.example.attendence.databinding.FragmentAttendenceBinding;
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
    private String userId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAttendenceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 오늘 날짜 표시
        String today = new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN).format(new Date());
        binding.todayDateTextView.setText(today);

        // 사용자 ID 가져오기
        userId = getUserIdFromPrefs();

        recyclerView = binding.rvTakePosts;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TakePostAdapter(getContext(), takeList, true, "ATTEND");
        adapter.setUserId(userId);
        recyclerView.setAdapter(adapter);

        adapter.setOnStudentAppealClickListener(post -> {
            binding.studentAppealEditbox.setVisibility(View.VISIBLE);
            binding.studentAppealButton.setVisibility(View.VISIBLE);
            currentSelectedPost = post;
        });

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
                            setupStudentUI();
                        } else if ("professor".equals(role)) {
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
        binding.attendenceCheckS.setVisibility(View.VISIBLE);
        binding.attendenceCheckS.setOnClickListener(v ->
                Toast.makeText(getContext(), "출결 이의 신청 클릭됨", Toast.LENGTH_SHORT).show()
        );

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

        loadProfessorAttendence(userId, selectedDateId);

        adapter.serOnProfessorClickListener(post -> {
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

                    String formattedDate = new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN)
                            .format(selectedDate.getTime());
                    binding.todayDateTextView.setText(formattedDate);

                    selectedDateId = new SimpleDateFormat("yyMMdd", Locale.KOREAN).format(selectedDate.getTime());

                    binding.etStudentAppeal.setText("");
                    binding.studentAppealEditbox.setVisibility(View.GONE);
                    binding.studentAppealButton.setVisibility(View.GONE);
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

                        TakePost takePost = new TakePost(subject, professor, classroom, schedule, "", doc.getId(), doc.getString("professorId"));
                        takeList.add(takePost);

                        // 출결/사유 불러오기
                        db.collection("users")
                                .document(userId)
                                .collection("takes")
                                .document(doc.getId())
                                .collection("date")
                                .document(dateId)
                                .get()
                                .addOnSuccessListener(dateDoc -> {
                                    if (dateDoc.exists()) {
                                        takePost.setAttendenceStandard(dateDoc.getString("status") != null ? dateDoc.getString("status") : "");
                                    }
                                    adapter.notifyDataSetChanged();
                                });
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void submitStudentAttendence(String appealText, String dateId) {
        if (currentSelectedPost == null) return;
        String studentId = getUserIdFromPrefs();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 학생 DB
        db.collection("users")
                .document(studentId)
                .collection("takes")
                .document(currentSelectedPost.getId())
                .collection("date")
                .document(dateId)
                .set(new HashMap<String, Object>() {{
                    put("reason", appealText);
                }}, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "이의신청이 제출되었습니다.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "제출 실패", Toast.LENGTH_SHORT).show());

        // 교수 DB
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
                        put("reason", appealText);
                        put("status", "appeal");
                    }}, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d("Submit", "Professor DB updated"))
                    .addOnFailureListener(e -> Log.e("Submit", "Professor DB failed", e));
        }
    }

    private void loadProfessorAttendence(String userId, String dateId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .collection("lecture")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    takeList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        TakePost takePost = new TakePost(
                                doc.getString("과목명"),
                                " ",
                                doc.getString("강의실"),
                                doc.getString("시간"),
                                "",
                                doc.getId(),
                                userId
                        );
                        takeList.add(takePost);
                    }
                    adapter.notifyDataSetChanged();

                    // 클릭 리스너
                    adapter.serOnProfessorClickListener(post -> {
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
                                    for (QueryDocumentSnapshot studentDoc : querySnapshot) {
                                        String studentId = studentDoc.getId();
                                        String reason = studentDoc.getString("reason");
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
                })
                .addOnFailureListener(e -> Log.e("Attendence", "교수 데이터 불러오기 실패: ", e));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
