package com.example.attendence.ui.Info;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.attendence.LoginActivity;
import com.example.attendence.databinding.FragmentInfoBinding;

public class Info extends Fragment {

    private FragmentInfoBinding binding;
    private SharedPreferences sharedPreferences;
    private static final String KEY_USER_ID = "userId";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        InfoViewModel notificationsViewModel =
                new ViewModelProvider(this).get(InfoViewModel.class);

        binding = FragmentInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.logoutButton.setOnClickListener(v->handleLogout());
        return root;
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