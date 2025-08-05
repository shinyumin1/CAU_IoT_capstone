package com.example.attendence;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link appealS#newInstance} factory method to
 * create an instance of this fragment.
 */
public class appealS extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public appealS() {
        // Required empty public constructor
    }
    public static appealS newInstance(String param1, String param2) {
        appealS fragment = new appealS();
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
        View view = inflater.inflate(R.layout.fragment_appeal_s, container, false);
        Button button_attend_p = view.findViewById(R.id.student_appeal_button);

        button_attend_p.setOnClickListener(v -> {
            Toast.makeText(getContext(), "출결 상태가 변경되었습니다.", Toast.LENGTH_SHORT).show();
        });
        return  view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button appealStatusBtn = view.findViewById(R.id.attend_status_appeal);
        LinearLayout appealEditBox = view.findViewById(R.id.student_appeal_editbox);


        appealStatusBtn.setOnClickListener(v -> {
            if (appealEditBox.getVisibility() == View.GONE) {
                appealEditBox.setVisibility(View.VISIBLE);
            } else {
                appealEditBox.setVisibility(View.GONE);
            }
        });


    }
}