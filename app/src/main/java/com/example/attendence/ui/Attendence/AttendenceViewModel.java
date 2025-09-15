package com.example.attendence.ui.Attendence;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AttendenceViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<String> bluetoothData = new MutableLiveData<>();

    public AttendenceViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("attendence fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
    /*블루투스 데이터 관찰*/
    public LiveData<String> getBluetoothData(){
        return bluetoothData;
    }
    /*블루투스 데이터 설정*/
    public void setBluetoothData(String data){
        bluetoothData.postValue(data);
    }
}