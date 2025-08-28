package com.example.attendence;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService {
    // 해당장치UUID
    private static final UUID cau_iot_uuid =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Thread workerThread;
    private boolean isConnected = false;

    //콜백 인터페이스
    public interface OnDataReceivedListener {
        void onDataReceived(String data);
    }
    private OnDataReceivedListener listener;
    public void setOnDataReceivedListener(OnDataReceivedListener listener) {
        this.listener =  listener;
    }
    @SuppressWarnings("MissingPermission")
    public  void connect(BluetoothDevice device){
        try {
            socket = device.createRfcommSocketToServiceRecord(cau_iot_uuid);
            socket.connect();
            outputStream=socket.getOutputStream();
            inputStream =socket.getInputStream();
            isConnected =true;
            startListen();
            Log.d("BT", "블루투스 연결 성공");
        } catch (IOException e) {
            Log.e("BT", "블루투스 연결 실패");
        }
    }
    private void startListen(){
        workerThread = new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    Log.d("BT", "받은 데이터: " + line);
                    if (listener != null) {
                        listener.onDataReceived(line);
                    }
                }
            } catch (IOException e) {
                Log.e("BT", "데이터 수신 오류", e);
            }
        });
        workerThread.start();
    }
    public void sendData(String msg){
        if(isConnected && outputStream !=null){
            try{
                outputStream.write(msg.getBytes());
            }catch (IOException e){
                Log.e("BT","데이터 송신 오류");
            }
        }
    }
}
