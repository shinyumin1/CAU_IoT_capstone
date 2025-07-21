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
        adapter = new TakePostAdapter(getContext(), takeList);
        recyclerView.setAdapter(adapter);

        // 파이어베이스에서 take 데이터 불러오기
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = getUserIdFromPrefs();

        db.collection("users")
                .document("students")
                .collection("userList")
                .document(userId)
                .collection("takes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("Home", "총 " + queryDocumentSnapshots.size() + "개의 과목이 있음");
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String subject = doc.getString("과목명");
                        String professor = doc.getString("교수명");
                        String classroom = doc.getString("강의실");
                        String schedule = doc.getString("시간");

                        if (classroom != "null") {
                            Log.d("Home", "과목: " + subject + ", 교수: " + professor + ", 강의실: " + classroom+", 시간 : "+schedule);
                            takeList.add(new TakePost(subject, professor, classroom,schedule));
                        } else {
                            Log.d("Home", "강의실 정보 없음, 과목: " + subject);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Home", "데이터 불러오기 실패: ", e);

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
}