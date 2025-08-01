package com.example.attendence.ui.Home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.attendence.TakePost;
import com.example.attendence.TakePostAdapter;
import com.example.attendence.databinding.FragmentHomeBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

public class Home extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private TakePostAdapter adapter;
    private List<TakePost> takeList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        String today = new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN).format(new Date());
        binding.todayDateTextView.setText(today);

        recyclerView = binding.rvTakePosts;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TakePostAdapter(getContext(), takeList, false);
        recyclerView.setAdapter(adapter);

        // 파이어베이스에서 take 데이터 불러오기
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = getUserIdFromPrefs();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("student".equals(role)) {
                            adapter = new TakePostAdapter(getContext(), takeList, true);
                            recyclerView.setAdapter(adapter);
                            loadStudentHome(userId);  // 학생용 데이터 불러오기
                        } else if ("professor".equals(role)) {
                            adapter = new TakePostAdapter(getContext(), takeList, false);
                            recyclerView.setAdapter(adapter);
                            loadProfessorHome(userId); // 교수용 데이터 불러오기
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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadStudentHome(String userId) {
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

                        if (classroom != null && !"null".equals(classroom)) {
                            takeList.add(new TakePost(subject, professor, classroom, schedule));
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Home", "학생 데이터 불러오기 실패: ", e);
                });
    }

    private void loadProfessorHome(String userId) {
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

                        takeList.add(new TakePost(subject, " ", classroom, schedule));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Home", "교수 데이터 불러오기 실패: ", e);
                });
    }

}