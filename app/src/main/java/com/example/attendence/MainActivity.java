package com.example.attendence;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.attendence.databinding.FragmentHomeBinding;
import com.example.attendence.ui.Attendence.AttendenceViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.fragment.NavHostFragment;


import com.example.attendence.databinding.ActivityMainBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import kotlin.ranges.IntProgressionIterator;

public class MainActivity extends AppCompatActivity {
    //블루투스 활성화 상태
    private static final int REQUEST_ENABLE_BT =10;
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
    private Set<BluetoothDevice> devices;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket=null;
    private OutputStream outputStream = null;
    private InputStream inputStream=null;
    private Thread workerThread = null;
    private byte[] readBuffer;
    private int readBufferPosition;
    String[] array= {"0"};
    int pairedDeviceCount;
    private TakePostAdapter takePostAdapter;

    public void selectBluetoothDevice() {
        // 권한 체크 (Android 12 이상 필요)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
            return; // 권한 없으면 아래 코드 실행하지 않음
        }

        devices = bluetoothAdapter.getBondedDevices();
        pairedDeviceCount = devices.size();

        if (pairedDeviceCount == 0) {
            Toast.makeText(getApplicationContext(), "먼저 페어링 해주세요", Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("페어링 된 블루투스 디바이스 목록");

            List<String> list = new ArrayList<>();
            for (BluetoothDevice bluetoothDevice : devices) {
                list.add(bluetoothDevice.getName());
            }
            list.add("취소");

            final CharSequence[] charSequences = list.toArray(new CharSequence[0]);

            builder.setItems(charSequences, (dialog, which) -> {
                if (!charSequences[which].equals("취소")) {
                    connectDevice(charSequences[which].toString());
                }
            });
            builder.setCancelable(true);
            AlertDialog alterDialog = builder.create();
            alterDialog.show();
        }
    }
    public void connectDevice(String deviceName) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 101);
            return;
        }

        BluetoothDevice selectedDevice = null;
        for (BluetoothDevice device : devices) {
            if (deviceName.equals(device.getName())) {
                selectedDevice = device;
                break;
            }
        }

        if (selectedDevice == null) {
            Toast.makeText(this, "선택한 디바이스를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getApplicationContext(),
                selectedDevice.getName() + " 연결 시도중...", Toast.LENGTH_SHORT).show();

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP UUID

        try {
            bluetoothSocket = selectedDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();

            outputStream =bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();

            Toast.makeText(getApplicationContext(),
                    selectedDevice.getName() + " 연결 완료", Toast.LENGTH_SHORT).show();

            receiveData();

        } catch (IOException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    "연결 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    public void receiveData(){
        final Handler handler = new Handler();
        readBufferPosition  = 0;
        readBuffer = new byte[1024];

        workerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    int byteAvailable = inputStream.available();
                    if (byteAvailable > 0) {
                        byte[] bytes = new byte[byteAvailable];
                        inputStream.read(bytes);

                        for (int i = 0; i < byteAvailable; i++) {
                            byte tempByte = bytes[i];
                            if (tempByte == '\n') {
                                byte[] encodeBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodeBytes, 0, encodeBytes.length);

                                final String text = new String(encodeBytes, "UTF-8");
                                readBufferPosition = 0;

                                handler.post(() -> {
                                    // 여기서 UI 업데이트
                                    Log.d("Bluetooth", "받은 데이터: " + text);
                                    /*attendence에 전달*/
                                    AttendenceViewModel viewModel = new ViewModelProvider(this).get(AttendenceViewModel.class);
                                    viewModel.setBluetoothData(text);
                                    /*
                                     if  (takePostAdapter  != null) {
                                            takePostAdapter.setBluetoothData(text);
                                     }else {
                                         Log.d("post?", "받은 데이터: " + takePostAdapter);
                                     }

                                    // 예시: TextView에 표시
                                    //TextView tv = findViewById(R.id.my_name);

                                    *if (tv != null) {
                                        tv.setText("수신: " + text);
                                    }
                                    * */

                                });
                            } else {
                                readBuffer[readBufferPosition++] = tempByte;
                            }
                        }
                    }

                    // Sleep while 안으로 이동
                    Thread.sleep(100);

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });

        workerThread.start();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //위치권한 허용  코드
        String[] permission_list = {
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION
        };
        //블루투스 활성화 코드
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (bluetoothAdapter == null){
//            Toast.makeText(getApplicationContext(), "Bluetooth 미지원 기기입니다.", Toast.LENGTH_SHORT).show();
//            // 처리코드 작성
//        } else {
//            //기기가 블루투스를 지원할 시
//            if (bluetoothAdapter.isEnabled()) {
//                selectBluetoothDevice();
//            } else {
//                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                //startActivityForResult(intent, REQUEST_ENABLE_BT);
//                selectBluetoothDevice();
//            }
//        }


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
                                    initBluetooth();
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
    /*블루투스로 받은 데이터를 activity랑 fragment와 같은 viewModel을 공유하도록 함*/

    private void initBluetooth(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter==null){
            Toast.makeText(getApplicationContext(), "Bluetooth 미지원 기기입니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_BLUETOOTH_PERMISSIONS);
            return;
        }

        if (bluetoothAdapter.isEnabled()){
            selectBluetoothDevice();
        }
//        else{
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(intent, REQUEST_ENABLE_BT);
//        }
    }
}