package com.example.attendence.ui.Info;

import android.content.Context;
import android.content.Intent;
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

import com.example.attendence.LoginActivity;
import com.example.attendence.R;
import com.example.attendence.databinding.FragmentInfoBinding;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;
import com.example.attendence.TakePost;
import com.example.attendence.TakePostAdapter;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

public class Info extends Fragment {

    private FragmentInfoBinding binding;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private SharedPreferences sharedPreferences;
    private static final String TAG = "InfoFragment";
    private static final String KEY_USER_ID = "userId";
    private RecyclerView recyclerView;
    private TakePostAdapter adapter;
    private List<TakePost> takeList = new ArrayList<>();
    private Spinner standardSpinner;
    private List<String> standardList = new ArrayList<>();
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // ViewBinding 설정
        binding = FragmentInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // firebase 초기호ㅏ
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // RecyclerView 초기화
        recyclerView = binding.rvTakePosts;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TakePostAdapter(getContext(), takeList, false, false, "");
        recyclerView.setAdapter(adapter);
        // Firestore에서 사용자 정보 불러오기
        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString(KEY_USER_ID, null);

        if (userId != null) {
            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userName = documentSnapshot.getString("userName");
                            String department = documentSnapshot.getString("department");
                            String gender = documentSnapshot.getString("gender");
                            String role = documentSnapshot.getString("role");
                            if (userName != null) {
                                binding.tvUserName.setText("이름 : " + userName);
                            }

                            if (department != null) {
                                binding.tvUserDept.setText("학과/학부 : " + department);
                            }
                            if (userId!=null){
                                binding.tvUserId.setText("학번 : "+ userId);
                            }
                            if (gender!=null){
                                binding.tvUserGender.setText("성별 : "+ gender);
                            }
                            if ("professor".equals(role)) {
                                adapter = new TakePostAdapter(getContext(), takeList, false, true, "");
                                adapter.setProfessorViewType(TakePostAdapter.ProfessorViewType.SPINNER);
                                recyclerView.setAdapter(adapter);

                                loadLecturePosts(userId); // 교수 전용

                                // 스피너 이벤트 리스너 등록 (교수 전용 기능)
                                adapter.setOnSpinnerItemSelectedListener((post, selectedStandard) -> {
                                    if (userId != null && post.getId() != null) {
                                        db.collection("users")
                                                .document(userId)
                                                .collection("lecture")
                                                .document(post.getId())
                                                .update("출석기준", selectedStandard)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d(TAG, "출석기준 업데이트 성공: " + selectedStandard);
                                                    post.setAttendenceStandard(selectedStandard);
                                                    adapter.notifyDataSetChanged();
                                                })
                                                .addOnFailureListener(e -> Log.e(TAG, "출석기준 업데이트 실패", e));
                                    }
                                });
                            } else {
                                adapter = new TakePostAdapter(getContext(), takeList, false, false, "");
                                recyclerView.setAdapter(adapter);
                                loadTakePosts(userId);    // 학생 전용
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "사용자 이름 불러오기 실패", e);
                    });
        } else {
            Log.w(TAG, "userId가 SharedPreferences에 없습니다.");
        }

        binding.logoutButton.setOnClickListener(v->handleLogout());
        return root;
    }

    private void loadTakePosts(String userId) {
        db.collection("users")
                .document(userId)
                .collection("takes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    takeList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String subject = doc.getString("과목명");
                        String professor = doc.getString("교수명");
                        String classroom = doc.getString("강의실");
                        String schedule = doc.getString("시간");


                        if (subject != null && classroom != null && schedule != null) {
                            String docId = doc.getId();
                            String profId = doc.getString("professorId");
                            takeList.add(new TakePost(subject, professor!=null?professor : "", classroom, schedule,"",docId,profId));
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "수강 과목 데이터 불러오기 실패", e);
                });
    }

    private void loadLecturePosts(String userId) {
        db.collection("users")
                .document(userId)
                .collection("lecture")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    takeList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String subject = doc.getString("과목명");
                        String professor = doc.getString("교수명");
                        String classroom = doc.getString("강의실");
                        String schedule = doc.getString("시간");
                        String attendenceStandard = doc.getString("출석기준");
                        String reason = doc.getString("reson");

                        if (subject != null && classroom != null && schedule != null) {
                            String docId = doc.getId();
                            String profId = doc.getString("professorId");
                            takeList.add(new TakePost(
                                    subject,
                                    professor != null ? professor : "", // 빈칸 처리
                                    classroom,
                                    schedule,
                                    attendenceStandard != null ? attendenceStandard : "0분",
                                    docId,
                                    profId
                            ));

                        }
                    }
                    // spinner에 대한 처리
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "강의 데이터 불러오기 실패", e));
    }

    // 로그아웃 처리 메서드
    private void handleLogout() {
        // SharedPreferences에서 사용자 ID 삭제
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USER_ID);
        editor.apply();

        // 로그아웃 메시지 표시
        Toast.makeText(getActivity(), "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();

        // 로그인 화면으로 이동
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // 현재 액티비티 종료
        getActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}