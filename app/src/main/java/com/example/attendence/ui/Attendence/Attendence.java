package com.example.attendence.ui.Attendence;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.example.attendence.R;

import com.example.attendence.appealP;
import com.example.attendence.appealS;
import com.example.attendence.databinding.FragmentHomeBinding;


public class Attendence extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AttendenceViewModel homeViewModel =
                new ViewModelProvider(this).get(AttendenceViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        String today = new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN).format(new Date());
        binding.todayDateTextView.setText(today);

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

        binding.attendenceCheckS.setVisibility(View.VISIBLE);
        binding.attendenceCheckS.setOnClickListener(v -> {
            // 추후 구현해야 하는 부분.. 현재는 토스트만 띄우게 설정
            Toast.makeText(getContext(), "출결 이의 신청 클릭됨", Toast.LENGTH_SHORT).show();
        });

        String role = getActivity().getSharedPreferences("UserPrefs", getActivity().MODE_PRIVATE)
                .getString("role", "student"); // 기본값 student

        if ("student".equals(role)) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new appealS())
                    .commit();
        } else if ("professor".equals(role)) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new appealP())
                    .commit();
        }


        return root;
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}