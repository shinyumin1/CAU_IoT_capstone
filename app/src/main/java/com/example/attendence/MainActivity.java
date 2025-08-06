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

        userNameTextView = findViewById(R.id.my_name);
        // NavController 가져오기
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);

        if (navHostFragment == null) {
            Log.e("MainActivity", "navHostFragment is NULL");
            return;
        }

        NavController navController = navHostFragment.getNavController();
        if (userId != null) {
            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            if (role != null) {
                                sharedPreferences.edit().putString("role", role).apply();

                                // role에 따라 초기화면 선택
                                if ("student".equals(role)) {
                                    navController.navigate(R.id.navigation_attendence);
                                } else if ("professor".equals(role)) {
                                    navController.navigate(R.id.navigation_attendence);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("MainActivity", "Firestore 실패", e));
        }

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_attendence, R.id.navigation_home, R.id.navigation_info)
                .build();

        NavigationUI.setupWithNavController(binding.navView, navController);
    }
}