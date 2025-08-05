package com.example.attendence;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.LinearLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import com.example.attendence.databinding.FragmentAppealPBinding;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link appealP#newInstance} factory method to
 * create an instance of this fragment.
 */
public class appealP extends Fragment {
    private FragmentAppealPBinding binding;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private SharedPreferences sharedPreferences;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public appealP() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment dental_p.
     */
    // TODO: Rename and change types and number of parameters
    public static appealP newInstance(String param1, String param2) {
        appealP fragment = new appealP();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_appeal_p, container, false);
        Button button_attend_p = view.findViewById(R.id.student_status_button);
        button_attend_p.setOnClickListener(v -> {
            Toast.makeText(getContext(), "출결 상태가 변경되었습니다.", Toast.LENGTH_SHORT).show();
        });
        return  view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button appealBtn = view.findViewById(R.id.attend_status_appeal);
        LinearLayout appealOptions = view.findViewById(R.id.appeal_options);
        TextView option1 = view.findViewById(R.id.option1);
        TextView option2 = view.findViewById(R.id.option2);
        TextView option3 = view.findViewById(R.id.option3);
        LinearLayout layoutItem= view.findViewById(R.id.student_appeal_item);
        TextView reasonText = view.findViewById(R.id.reason_text);

        appealBtn.setOnClickListener(v -> {
            if (appealOptions.getVisibility() == View.GONE) {
                appealOptions.setVisibility(View.VISIBLE);
            } else {
                appealOptions.setVisibility(View.GONE);
            }
        });

        View.OnClickListener optionClickListener = v -> {
            TextView option = (TextView) v;
            appealBtn.setText(option.getText());
            appealOptions.setVisibility(View.GONE);
        };
        option1.setOnClickListener(optionClickListener);
        option2.setOnClickListener(optionClickListener);
        option3.setOnClickListener(optionClickListener);

        layoutItem.setOnClickListener(v -> {
            if (reasonText.getVisibility() == View.GONE){
                reasonText.setVisibility(View.VISIBLE);
            } else {
                reasonText.setVisibility(View.GONE);
            }
        });
    }
    private RecyclerView recyclerView;
    private TakePostAdapter adapter;
    private List<TakePost> takePostList;
    // 파이어베이스 데리터 로드
    //private DatabaseReference databaseReference;
    @NonNull
    public View Oncreate(@NonNull LayoutInflater inflater,
                         @Nullable ViewGroup contatiner,
                         @Nullable Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appeal_p, contatiner, false);

        recyclerView = view.findViewById(R.id.recyclerView_p);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        takePostList = new ArrayList<>();
        //어댑터 연결
        //adapter = new TakePostAdapter(List<TakePost>)
        recyclerView.setAdapter(adapter);

        //databaseReference = FirebaseStorage.getInstance().getReference("attendence");
        //loadDataFromFireBase();
        return  view;
    }

}