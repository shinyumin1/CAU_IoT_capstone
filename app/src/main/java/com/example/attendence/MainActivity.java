package com.example.attendence;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.attendence.databinding.FragmentHomeBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // firebase 초기화
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString(KEY_USER_ID, null);

        userNameTextView = findViewById(R.id.my_name);
        // NavController 가져오기
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        //블루투스 위치권한 허용
        String[] permission_list = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        //블루투스 활성화
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null ) {
            Toast.makeText(this, "이 기기는 블루투스를 지원하지 않습니다", Toast.LENGTH_SHORT).show();
        } else {
            checkBluetoothPermission();
        }

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
                            String name = documentSnapshot.getString("userName");

                            if (name != null && userNameTextView != null) {
                                userNameTextView.setText(name);
                            }

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
    //블루투스 권한 체크 및 요청
    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 이상
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                        },
                        REQUEST_BLUETOOTH_PERMISSIONS);
                return;
            }
        }
        enableBluetooth(); // 권한 있으면 실행
    }
    // 블루투스 활성화 요청
    @SuppressLint("MissingPermission") // 권한 확인 후 호출하므로 안전
    private void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }
    //블루투스 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableBluetooth();
            } else {
                Toast.makeText(this, "블루투스 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}