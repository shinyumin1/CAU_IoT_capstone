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


public class Attendence extends Fragment {

    private FragmentAttendenceBinding binding;
    private RecyclerView recyclerView;
    private TakePostAdapter adapter;
    private List<TakePost> takeList = new ArrayList<>();
    private TakePost currentSelectedPost;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AttendenceViewModel AttendenceViewModel =
                new ViewModelProvider(this).get(AttendenceViewModel.class);

        binding = FragmentAttendenceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        String today = new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN).format(new Date());
        binding.todayDateTextView.setText(today);
        recyclerView = binding.rvTakePosts;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TakePostAdapter(getContext(), takeList, false, "ATTEND");
        recyclerView.setAdapter(adapter);


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
                        String formattedDate = new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN)
                                .format(selectedDate.getTime());
                        binding.todayDateTextView.setText(formattedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = getUserIdFromPrefs();

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
                            adapter = new TakePostAdapter(getContext(), takeList, true, "ATTEND");
                            recyclerView.setAdapter(adapter);
                            loadStudentAttendence(userId);  // 학생용 데이터 불러오기

                            adapter.setOnStudentAppealClickListener(post -> {
                                binding.studentAppealEditbox.setVisibility(View.VISIBLE);
                                binding.studentAppealButton.setVisibility(View.VISIBLE);
                                currentSelectedPost = post; // 클릭한 과목정보 저장
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
                                submitStudentAttendence(appealText);
                            });
                        } else if ("professor".equals(role)) {
                            binding.attendenceCheckS.setVisibility(View.GONE);
                            adapter = new TakePostAdapter(getContext(), takeList, false, "ATTEND");
                            recyclerView.setAdapter(adapter);
                            loadProfessorAttendence(userId); // 교수용 데이터 불러오기
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

    private void loadStudentAttendence(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String todayWeekday = new SimpleDateFormat("E", Locale.KOREAN).format(new Date());
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
                                && schedule != null && schedule.contains(todayWeekday)) {
                            takeList.add(new TakePost(subject, professor, classroom, schedule, "", doc.getId()));
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Home", "학생 데이터 불러오기 실패: ", e);
                });
    }
    private void submitStudentAttendence(String userId) {
        // text 필드 추가해서 db 넣기
    }
    private void loadProfessorAttendence(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String todayWeekday = new SimpleDateFormat("E", Locale.KOREAN).format(new Date());
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
                            takeList.add(new TakePost(subject, " ", classroom, schedule,"",doc.getId()));
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Home", "교수 데이터 불러오기 실패: ", e);
                });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}