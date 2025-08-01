package com.example.attendence;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.attendence.databinding.FragmentHomeBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.fragment.NavHostFragment;


import com.example.attendence.databinding.ActivityMainBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ImageView profileImageView;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private SharedPreferences sharedPreferences;
    private TextView userNameTextView;
    private static final String TAG = "MainActivity";
    private static final String KEY_USER_ID = "userId";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // firebase 초기호ㅏ
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString(KEY_USER_ID, null);
        // NavController 가져오기
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        if (navHostFragment == null) {
            Log.e("MainActivity", "navHostFragment is NULL");
            return;
        }
        NavController navController = navHostFragment.getNavController();
        userNameTextView = findViewById(R.id.my_name);

        if (userId != null) {
            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            //role로 분기
                            String role = documentSnapshot.getString("role");
                            binding.getRoot().post(() -> {
                                if("student".equals(role)) {
                                    navController.navigate(R.id.navigation_attendence_s);
                                }
                                else if("professor".equals(role)) {
                                    navController.navigate(R.id.navigation_attendence_p);
                                }else {
                                    Log.e(TAG, "Unknown role" + role);
                                }

                            });
                            String userName = documentSnapshot.getString("userName");
                            if (userName != null && userNameTextView != null) {
                                userNameTextView.setText(userName);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "사용자 이름 불러오기 실패", e);
                    });
        } else {
            Log.w(TAG, "userId가 SharedPreferences에 없습니다.");
        }

        // AppBarConfiguration 설정
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_attendence, R.id.navigation_home, R.id.navigation_info)
                .build();

        // 액션바 + 바텀 네비게이션 연동
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // 한번만 연결되도록 체크
            if (binding.navView.getMenu().size() > 0) {
                NavigationUI.setupWithNavController(binding.navView, navController);
            }
        });
    }

}